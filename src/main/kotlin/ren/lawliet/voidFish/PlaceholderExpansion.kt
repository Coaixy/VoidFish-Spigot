package ren.lawliet.voidFish

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ren.lawliet.voidFish.game.GameManager

/**
 *@author        Coaixy
 *@createTime    2024-08-30
 *@packageName   ren.lawliet.voidFish
 */

open class PlaceholderExpansion(private val plugin: JavaPlugin) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "void_fish"
    }

    override fun getAuthor(): String {
        return plugin.description.authors.joinToString()
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun persist(): Boolean {
        return true
    }

    override fun canRegister(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return ""
        val room = GameManager.getGameRoomByPlayer(player)
        return when (params) {
            "room_id" -> room?.gameUUID ?: ""
            "room_state" -> room?.gameState.toString() ?: ""
            "room_player_count" -> room?.getRoomPlayerList()?.size.toString() ?: ""
            else -> null
        }
    }
}