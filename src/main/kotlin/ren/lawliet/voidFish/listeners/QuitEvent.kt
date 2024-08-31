package ren.lawliet.voidFish.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import ren.lawliet.voidFish.game.GameManager
import ren.lawliet.voidFish.game.PlayerState
import ren.lawliet.voidFish.utils.PlayerUtils

/**
 *@author        Coaixy
 *@createTime    2024-08-23
 *@packageName   ren.lawliet.voidFish.listeners
 */

class QuitEvent : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (GameManager.getPlayerMap().containsKey(player.uniqueId.toString())) {
            val room = GameManager.getPlayerMap()[player.uniqueId.toString()]
            if (room != null) {
                room.playerStateMap[player.uniqueId.toString()] = PlayerState.Disconnected
            }
        }
    }

    @EventHandler
    fun joinServer(event: PlayerJoinEvent) {
        val player = event.player
        if (!GameManager.getPlayerMap().containsKey(player.uniqueId.toString())) {
            PlayerUtils.setPlayerNew(player)
        }
    }

    @EventHandler
    fun onKick(event: PlayerKickEvent) {
        val player = event.player
        val uuid = player.uniqueId.toString()
        if (GameManager.getPlayerMap().containsKey(uuid)) {
            GameManager.getPlayerMap()[uuid]?.removePlayer(uuid)
        }
    }
}