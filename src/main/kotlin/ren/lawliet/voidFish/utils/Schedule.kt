package ren.lawliet.voidFish.utils

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import ren.lawliet.voidFish.configs.PluginConfig
import ren.lawliet.voidFish.game.GameManager

/**
 *@author        Coaixy
 *@createTime    2024-08-23
 *@packageName   ren.lawliet.voidFish.utils
 */

class Schedule {
    // Timed deletion of ended games
    fun deleteRoomTask() {
        val runnable = object : BukkitRunnable() {
            override fun run() {
                GameManager.getGameMap().forEach {
                    if (it.value.gameState == ren.lawliet.voidFish.game.GameState.Ending ||
                        (it.value.gameState == ren.lawliet.voidFish.game.GameState.Gaming && it.value.getRoomPlayerList().size == 0)
                    ) {
                        Bukkit.getLogger().info("Delete room ${it.key}")
                        GameManager.delGame(it.key)
                    }
                }
            }
        }
        // 5 minutes
        PluginConfig.pluginInstance?.let { runnable.runTaskTimer(it, 0, 20 * 60 * 5) }
    }
}