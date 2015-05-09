package cn.edu.sjtu.omnilab.livewee.kc

import com.redis.RedisClient
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
 * Record and maintain the live status of an individual user.
 *
 * @param mac the MAC address of user
 * @param redis the redis database connection
 */
class UserStatus(mac: String, redis: RedisClient) {

  val SESSION_GAP = 5 * 60
  val SESSION_MAX = 3 * 3600

  case class LocationRecord(systime: Long, stime: Long, duration: Long, ap: String, location: String) {
    def toJson(): String = {
      val detector = RedisUtils.APDetector.parse(ap)
      val json = ("session" ->
        ("systime" -> systime) ~
          ("stime" -> stime) ~
          ("duration" -> duration) ~
          ("ap" -> ap) ~
          ("location" -> location) ~
          ("lat" -> detector.get("lat")) ~
          ("lon" -> detector.get("lon"))
        )
      compact(render(json))
    }
  }
  var last: ManPoint = null
  val uid = RedisUtils.getOrCreateUser(mac, redis)
  var session: LocationRecord = null

  /**
   * Add a new location point to user's session status.
   * @param r
   */
  def updateWithRecord(r: ManPoint): Unit = {
    val systime = System.currentTimeMillis() / 1000

    if ( session == null
      || r.location != last.location // has PingPang effect when considering APs
      || r.time - last.time > SESSION_GAP
      || r.time - session.stime > SESSION_MAX) {

      flushToRedis()
      session = LocationRecord(systime, r.time, 0, r.ap, r.location) // a new session
      updateLiveStatus() // update user live status

    } else {
      if ( r.time > session.stime + session.duration)
        session = session.copy(duration = r.time-session.stime, ap = r.ap)
        updateLiveStatus()
    }

    last = r
  }

  /**
   * Dump a movement session finished into redis.
   */
  def flushToRedis(): Unit = {
    if ( session != null ) {
      val jsonstr = session.toJson
      // println("%s: %s".format(mac, jsonstr))

      redis.lpush(RedisUtils.userStatusHistory(uid), jsonstr)
      session = null
    }
  }

  /**
   * Update user's session status in redis.
   */
  def updateLiveStatus(): Unit = {
    val status = session.toJson
    redis.hset(RedisUtils.userStatusLive, uid, status)
  }

  def liveStatus(): String = {
    session.toString
  }

}
