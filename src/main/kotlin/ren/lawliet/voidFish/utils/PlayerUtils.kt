package ren.lawliet.voidFish.utils

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.ScoreboardManager
import ren.lawliet.voidFish.game.GameRoom
import ren.lawliet.voidFish.game.GameState
import java.util.*

/**
 *@author        Coaixy
 *@createTime    2024-08-24
 *@packageName   ren.lawliet.voidFish.utils
 */

object PlayerUtils {
    fun setPlayerNew(player: Player) {
        player.gameMode = GameMode.SURVIVAL
        player.apply {
            health = maxHealth
            foodLevel = 20
            saturation = 20.0f
            activePotionEffects.forEach { removePotionEffect(it.type) }
            fireTicks = 0
            noDamageTicks = 20 * 2
            exhaustion = 0.0f
        }
        player.inventory.clear()
    }

    fun createScoreboard(room: GameRoom): Scoreboard {
        val manager: ScoreboardManager = Bukkit.getScoreboardManager()!!
        val scoreboard: Scoreboard = manager.newScoreboard

        val objective =
            scoreboard.registerNewObjective("voidFishBoard", "dummy", "${ChatColor.GOLD}Void Fish")
        objective.displaySlot = DisplaySlot.SIDEBAR
        room.getRoomPlayerList().forEach {
            val player = Bukkit.getPlayer(UUID.fromString(it))
            if (player != null) {
                if (room.gameState == GameState.Gaming) {
                    if (room.getPlatFormByPlayer(player)?.isDry == true) {
                        objective.getScore("${ChatColor.GREEN}${player.name} ${ChatColor.RED}Dry").score = 0
                    } else {
                        objective.getScore("${ChatColor.GREEN}${player.name} ${ChatColor.GREEN}Wet").score = 0
                    }
                } else if (room.gameState == GameState.Waiting) {
                    objective.getScore("${ChatColor.GREEN}${player.name}").score = 0
                }
            }
        }
        room.endTimeStr = formatTime(room.endTimeCount)
        val rewardTimer = objective.getScore("${ChatColor.RED}Reward Timer ${room.rewardTimer} ")
        val endTime = objective.getScore("${ChatColor.RED}End Time   ${room.endTimeStr}  ")
        endTime.score = 0
        rewardTimer.score = 0
        return scoreboard
    }

    fun clearScoreboard(player: Player) {
        player.scoreboard.clearSlot(DisplaySlot.SIDEBAR)
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "$minutes m $remainingSeconds s"
    }
}