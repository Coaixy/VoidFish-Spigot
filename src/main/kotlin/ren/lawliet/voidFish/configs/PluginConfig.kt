package ren.lawliet.voidFish.configs

import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import ren.lawliet.voidFish.VoidFish
import java.util.*


/**
 *@author        Coaixy
 *@createTime    2024-08-22
 *@packageName   ren.lawliet.voidFish.configs
 */

object PluginConfig {
    var pluginInstance: VoidFish? = null
    lateinit var fishing_rod_name: String
    lateinit var black_item_list: List<String>
    lateinit var black_entity_list: List<String>
    lateinit var reward_item_list: List<String>
    var entity_probability: Double = 0.0
    var platform_distance: Int = 30
    var respawnTime = 0
    var waitSpaceAdditionY = 0
    var rewardRefreshTime = 0
    var endGameCountDown = 0

    fun init(plugin: VoidFish) {
        pluginInstance = plugin
        plugin.saveDefaultConfig()
        fishing_rod_name = plugin.config.getString("fishing-rod-name", "Magic Rod").toString()
        entity_probability = plugin.config.getDouble("entity-probability", 0.0)
        platform_distance = plugin.config.getInt("platform-distance", 30)
        black_item_list = plugin.config.getStringList("item-blacklist")
        black_entity_list = plugin.config.getStringList("entity-blacklist")
        respawnTime = plugin.config.getInt("respawn-time", 0)
        reward_item_list = plugin.config.getStringList("reward-item-list")
        waitSpaceAdditionY = plugin.config.getInt("wait-space-addition-y")
        rewardRefreshTime = plugin.config.getInt("reward-refresh-time", 120)
        endGameCountDown = plugin.config.getInt("end-game-time", 15 * 60)

    }

}
