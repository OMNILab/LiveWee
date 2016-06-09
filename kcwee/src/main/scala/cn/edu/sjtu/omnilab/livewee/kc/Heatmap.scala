package cn.edu.sjtu.omnilab.livewee.kc

import com.redis.RedisClient
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable.HashMap

/**
 * Summarize user distribution in WIFI networks.
 *
 * @param redis the redis database connection
 */
class Heatmap(redis: RedisClient) {

  final val UPDATE_FRQ = 2
  final val WINDOW_LEN = 60
  final val MAX_TIME_SHIFT = 10 * 60 // ten minutes
  var lastUpdate = System.currentTimeMillis() / 1000

  case class LocationInfo(location: String, lat: String, lon: String)
  case class UserRecord(systime: Long, mac: String)
  var hmap: HashMap[LocationInfo, Set[UserRecord]] = new HashMap[LocationInfo, Set[UserRecord]]

  /**
   * Add a new movement data point to heatmap statistics
   */
  def update(r: ManPoint): Unit = {
    val systime = System.currentTimeMillis() / 1000

    if (r != null && systime - r.time <= MAX_TIME_SHIFT) {
      // add user record
      val key = LocationInfo(r.location, r.lat, r.lon)
      if ( !hmap.contains(key) )
        hmap.put(key, Set[UserRecord]())
      hmap(key) += UserRecord(systime, r.mac)
    }

    // update to redis
    if (systime - lastUpdate >= UPDATE_FRQ) {
      hmap.map { case (loc, users) => {
        users.filter(u => systime - u.systime < WINDOW_LEN)
      }}
      syncToRedis(systime)
      lastUpdate = systime
    }
  }

  /**
   * Write a heatmap snapshot into redis
   */
  def syncToRedis(time: Long): Unit = {
    // TODO: replace location with lat,lat
    val stat = hmap.mapValues(_.size)
    val statJSON = stat.map{ case (loc, count) => {
      ("location" -> loc.location) ~
        ("count" -> count) ~
        ("lat" -> loc.lat) ~
        ("lon" -> loc.lon)
    }}
    val snapshot = ("time" -> time) ~ ("heatmap" -> statJSON)

    val jsonstr = compact(render(snapshot))
    redis.lpush(RedisUtils.heatmapStatusHistory, jsonstr)
  }

}
