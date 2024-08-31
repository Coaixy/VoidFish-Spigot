package ren.lawliet.voidFish.listeners

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import ren.lawliet.voidFish.configs.GameMessage
import ren.lawliet.voidFish.configs.GameStat
import ren.lawliet.voidFish.game.GameManager
import ren.lawliet.voidFish.game.GameState
import java.util.*

/**
 *@author        Coaixy
 *@createTime    2024-08-24
 *@packageName   ren.lawliet.voidFish.listeners
 */

class SocializeEvent : Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val message = event.message
        if (GameManager.getPlayerMap().containsKey(player.uniqueId.toString())) {
            if (!message.startsWith("/")) {
                GameManager.getPlayerMap()[player.uniqueId.toString()]?.getRoomPlayerList()?.forEach {
                    val p = Bukkit.getPlayer(UUID.fromString(it))
                    p?.sendMessage("§7[§a${player.name}§7] §f$message")
                }
                event.isCancelled = true
                // can not exec other command
            } else if (message.startsWith("/") && message.replace("/vg ", "") != "leave") {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (GameManager.getGameRoomByPlayer(event.entity) != null && GameManager.getGameRoomByPlayer(event.entity)?.gameState == GameState.Gaming) {
            val player = event.entity
            if (GameManager.getPlayerMap().containsKey(player.uniqueId.toString())) {
                if (player.killer is Player) {
                    event.deathMessage = GameMessage.getMessage("dieMessage", player)
                    GameStat.addKill((player.killer as Player).uniqueId)
                }
            }
        }
    }
}