package cn.edu.sjtu.omnilab.livewee.kc

import java.io.{BufferedWriter, FileWriter, File}
import java.util.UUID

import com.redis.RedisClient
import kafka.consumer.KafkaConsumer
import org.joda.time.format.DateTimeFormat

import scala.collection.mutable.HashMap

case class ManPoint(mac: String, time: Long, code: Int, ap: String,
                    location: String, lat: String, lon: String)
case class AuthPoint(mac: String, time: Long, code: Int, uname: String)
case class IPPoint(mac: String, time: Long, code: Int, ip: String)

object MobilityInspector {

  val redis = new RedisClient("localhost", 6379)
  val heatmap = new Heatmap(redis)
  var allUserStatus: HashMap[String, UserStatus] = new HashMap[String, UserStatus]()
  val file = new File("unidentified_ap.txt")
  val bw = new BufferedWriter(new FileWriter(file))
  var lastUnidentifiedAP: String = null

  def main(args: Array[String]): Unit = {

    val topic = "arubasyslog.etled"
    val zookeeper = "10.50.4.73:2181"
    val groupID = UUID.randomUUID().toString

    val consumer = new KafkaConsumer(topic, groupID, zookeeper, false)
    consumer.read(readStreaming)
    consumer.close()
    bw.close()
  }

  /**
   * Process live streaming logs read from Kafka server
   * @param binaryObject
   */
  def readStreaming(binaryObject: Array[Byte]): Unit = {
    val message = new String(binaryObject)
    val point = parseRawLog(message)
    point match {
      case x: ManPoint => {
        if (!allUserStatus.contains(x.mac))
          allUserStatus.put(x.mac, new UserStatus(x.mac, redis))

        // use new record to update user status and statistics
        allUserStatus(x.mac).updateWithRecord(x)
        heatmap.update(x)
      }
      case _ => {
        // TODO: manipulate other message type
      }
    }
  }

  /**
   * Parse and extract fields from raw logs
   * @param log
   * @return
   */
  def parseRawLog(log: String): Any = {
    if ( log == null || log.length == 0)
      return null

    val parts = log.split(',')
    if (parts.length < 3)
      return null

    val mac = parts(0)
    val time = ISO2Unix(parts(1)) / 1000
    val code = parts(2).toInt

    code match {
      case 0 | 2 | 7 | 8 | 9 | 10 | 11 => {
        val detector = RedisUtils.APDetector.parse(parts(3))
        if ( detector != null )
          ManPoint(mac, time, code, parts(3), detector.get("name"),
            detector.get("lat"), detector.get("lon"))
        else {
          // record new AP names
          val parts = log.split(',')
          if (parts(3) != lastUnidentifiedAP) {
            println("Unknown: " + parts(3))
            bw.write(parts(3) + "\n")
            bw.flush
          }
          lastUnidentifiedAP = parts(3)
        }
      }
      case 5 | 6 => IPPoint(mac, time, code, parts(3))
      case 4     => AuthPoint(mac, time, code, parts(4))
    }
  }

  /**
   * Convert ISO time string to UNIX milliseconds
   * @param timestr a string in form of "2015-05-08 17:41:40"
   * @return A long number of milliseconds from UNIX epoch.
   */
  def ISO2Unix(timestr: String): Long = {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    fmt.parseDateTime(timestr).getMillis
  }

}
