package ren.lawliet.voidFish.configs

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import ren.lawliet.voidFish.game.GameManager
import java.io.File

/**
 *@author        Coaixy
 *@createTime    2024-08-24
 *@packageName   ren.lawliet.voidFish.configs
 */

object GameMessage {
    private val messageMap = HashMap<String, String>()
    fun init() {
        val plugin = PluginConfig.pluginInstance
        val messageFile = File(plugin?.dataFolder, "message.yml")
        if (!messageFile.exists()) {
            plugin?.saveResource("message.yml", false)
        }
        val config = YamlConfiguration.loadConfiguration(messageFile)
        config.getKeys(false).forEach {
            messageMap[it] = config.getString(it).toString()
        }
    }

    fun getMessage(key: String, player: Player): String {
        val roomPlayerMap = GameManager.getPlayerMap()
        return (messageMap[key])
            ?.replace("&", "§")
            ?.replace("{prefix}", messageMap["prefix"].toString())
            ?.replace("{player}", player.name)
            ?.replace("{roomId", roomPlayerMap[player.uniqueId.toString()]?.gameUUID ?: "")
            ?.replace(
                "{roomPlayerCount}",
                roomPlayerMap[player.uniqueId.toString()]?.getRoomPlayerList()?.size.toString()
            )
            ?.replace(
                "{roomMaxPlayerCount}",
                roomPlayerMap[player.uniqueId.toString()]?.maxPlayerCount.toString()
            )
            ?.replace("{roomState}", roomPlayerMap[player.uniqueId.toString()]?.gameState.toString())
            ?.replace("{killer}", player.killer?.name ?: "")
            ?: "§cNot Found Message In Message.yml"
    }

    fun Player.sendMsg(message: String) {
        this.sendMessage(getMessage(message, this))
    }
}