package cn.edu.sjtu.omnilab.livewee.kc

import com.redis.RedisClient

object RedisUtils {

  final val INTERVAL_LEN = 10

  final val userCounter = "livewee_user_num"
  final val allUsers = "livewee_all_users"
  final val allAps = "livewee_all_aps"
  final val allLocations = "livewee_all_locations"
  final val userStatusLive = "livewee_user_status"
  final val heatmapStatusLive = "livewee_heatmap_status"
  final val heatmapStatusHistory = "livewee_heatmap_history"

  final val APDetector = new APTranslator()

  def userStatusHistory(uid: Int): String = {
    "livewee_u%d_history".format(uid)
  }

  def getOrCreateUser(mac: String, redis: RedisClient): Int = {
    var uid = -1
    if (!redis.hexists(RedisUtils.allUsers, mac)) {
      redis.incr(RedisUtils.userCounter)
      uid = redis.get(RedisUtils.userCounter).get.toInt
      redis.hset(RedisUtils.allUsers, mac, uid)
    } else {
      uid = redis.hget(RedisUtils.allUsers, mac).get.toInt
    }
    uid
  }

}
