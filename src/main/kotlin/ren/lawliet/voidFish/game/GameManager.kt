package ren.lawliet.voidFish.game

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import ren.lawliet.voidFish.configs.GameMessage
import ren.lawliet.voidFish.configs.PluginConfig
import ren.lawliet.voidFish.utils.PlayerUtils
import ren.lawliet.voidFish.utils.VoidWorldGenerate
import java.util.*

/**
 *@author        Coaixy
 *@createTime    2024-08-23
 *@packageName   ren.lawliet.voidFish.game
 */

object GameManager {
    // Map <Game UUid , Game Room Instance >
    private val gameMap = HashMap<String, GameRoom>()

    // Map <Player UUid , Game Room Instance >
    private val playerMap = HashMap<String, GameRoom>()

    //  Get the game map
    fun getGameMap(): HashMap<String, GameRoom> {
        return gameMap
    }

    // Get the player map
    fun getPlayerMap(): HashMap<String, GameRoom> {
        return playerMap
    }

    // Get Wait Room if not exist create new game
    fun getWaitRoom(): GameRoom? {
        for (game in gameMap) {
            if (game.value.gameState == GameState.Waiting) {
                return game.value
            }
        }
        return newGame(UUID.randomUUID().toString())
    }

    // Create New Game
    private fun newGame(uuid: String): GameRoom? {
        gameMap[uuid] = GameRoom()
        gameMap[uuid]?.init(uuid)
        startCountDown(uuid)
        return gameMap[uuid]
    }

    // CountDown to start Game
    private fun startCountDown(uuid: String) {
        val gameRoom = gameMap[uuid]
        var timer = 15
        object : BukkitRunnable() {
            override fun run() {
                if (gameRoom?.gameState == GameState.Waiting && gameRoom.getRoomPlayerList().size >= 2) {
                    gameRoom.getRoomPlayerList().forEach {
                        val player = PluginConfig.pluginInstance?.server?.getPlayer(UUID.fromString(it))
                        player?.sendTitle(
                            GameMessage.getMessage("startCountDownTitle", player),
                            GameMessage.getMessage("startCountDownSubTitle", player)
                                .replace("{timer}", timer.toString()),
                            0, 20, 0
                        )
                    }
                    timer -= 1
                    if (timer <= 0) {
                        startGame(uuid)
                        this.cancel()
                    }
                } else {
                    timer = 15
                }
            }
        }.runTaskTimer(PluginConfig.pluginInstance!!, 0, 20)
    }

    // remove Player
    fun removePlayer(player: Player): Boolean {
        if (playerMap.containsKey(player.uniqueId.toString())) {
            val gameRoom = playerMap[player.uniqueId.toString()]
            Bukkit.getLogger().info(gameRoom.toString())
            gameRoom?.removePlayer(player.uniqueId.toString())
            playerMap.remove(player.uniqueId.toString())
            if (player.world.name.startsWith("VoidGame")) {
                player.teleport(Bukkit.getWorlds()[0].spawnLocation)
            }
            if (gameRoom?.playerList?.size == 0) {
                gameRoom.gameState = GameState.Ending
            }
            return true
        } else {
            return false
        }
    }

    // start Game
    fun startGame(gameUUID: String) {
        val gameRoom = gameMap[gameUUID]
        gameRoom?.gameState = GameState.Gaming
        gameRoom?.startGame()
    }

    // end game
    fun endGame(gameUUID: String): Boolean {
        val gameRoom = gameMap[gameUUID]
        if (gameRoom != null) {
            for (playerUUID in gameRoom.getRoomPlayerList()) {
                val p = Bukkit.getPlayer(UUID.fromString(playerUUID))
                if (p != null) {
                    PlayerUtils.clearScoreboard(p)
                    playerMap.remove(playerUUID)
                }
            }
            gameMap.remove(gameUUID)
            gameRoom.endGame()
            gameRoom.gameState = GameState.Ending
        } else {
            return false
        }
        return true
    }

    // delete game
    fun delGame(gameUUID: String): Boolean {
        endGame(gameUUID)
        VoidWorldGenerate.worldManagerInstance?.deleteWorld("VoidGame-$gameUUID")
        Bukkit.getLogger().info("删除游戏$gameUUID")
        return true
    }

    // get game room by player
    fun getGameRoomByPlayer(player: Player): GameRoom? {
        return playerMap[player.uniqueId.toString()]
    }
}

