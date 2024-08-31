package ren.lawliet.voidFish.configs

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

/**
 *@author        Coaixy
 *@createTime    2024-08-30
 *@packageName   ren.lawliet.voidFish.configs
 */

object GameStat {
    var config: YamlConfiguration? = null
    var file: File? = null
    fun init() {
        val plugin = PluginConfig.pluginInstance
        val statConfigFile = File(plugin?.dataFolder, "stat.yml")
        file = statConfigFile
        if (!statConfigFile.exists()) {
            plugin?.saveResource("stat.yml", false)
        }
        config = YamlConfiguration.loadConfiguration(statConfigFile)
    }

    fun addWin(playerUUID: UUID) {
        val win = config?.getInt("player.$playerUUID.win") ?: 0
        config?.set("player.$playerUUID.win", win + 1)
        file?.let { config?.save(it) }
    }

    fun addKill(playerUUID: UUID) {
        val kill = config?.getInt("player.$playerUUID.kill") ?: 0
        config?.set("player.$playerUUID.kill", kill + 1)
        file?.let { config?.save(it) }
    }

    fun increaseCount() {
        val count = config?.getInt("count") ?: 0
        config?.set("count", count + 1)
        file?.let { config?.save(it) }
    }

}