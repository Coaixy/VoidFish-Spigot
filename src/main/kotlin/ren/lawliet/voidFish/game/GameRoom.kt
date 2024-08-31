package ren.lawliet.voidFish.game

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import ren.lawliet.voidFish.configs.GameMessage.getMessage
import ren.lawliet.voidFish.configs.GameStat
import ren.lawliet.voidFish.configs.PluginConfig
import ren.lawliet.voidFish.utils.PlayerUtils
import ren.lawliet.voidFish.utils.PlayerUtils.setPlayerNew
import ren.lawliet.voidFish.utils.VoidWorldGenerate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class GameRoom {
    // Room Player List
    val playerList = ArrayList<String>()

    // Player Die List
    var playerDieList = ArrayList<String>()

    // Player PlatForm Map
    val playerPlatFormMap = HashMap<String, PlatForm>()

    // Game State
    var gameState: GameState = GameState.Waiting

    // Player State Map
    val playerStateMap = HashMap<String, PlayerState>()

    // Room Max Player Count
    val maxPlayerCount = 8

    // Room UUID
    var gameUUID = ""

    // Room World
    private var world: MultiverseWorld? = null

    // reward timer
    var rewardTimer = PluginConfig.rewardRefreshTime

    var endTimeStr = ""
    var endTimeCount = 0

    // Initial Room
    fun init(uuid: String) {
        gameUUID = uuid
        gameState = GameState.Waiting
        endTimeStr = PlayerUtils.formatTime(PluginConfig.endGameCountDown)
        endTimeCount = PluginConfig.endGameCountDown
        // Create World
        if (VoidWorldGenerate.generateWorld("VoidGame-$gameUUID")) {
            world = VoidWorldGenerate.worldManagerInstance?.getMVWorld("VoidGame-$gameUUID")
        }
    }

    // End Game
    fun endGame() {
        //Set State
        gameState = GameState.Ending
        //Clear Some Data
        playerList.clear()
        playerPlatFormMap.clear()
        playerDieList.clear()
        playerStateMap.clear()
        //Delete World
        VoidWorldGenerate.worldManagerInstance?.deleteWorld("VoidGame-$gameUUID")
    }

    // add player to Room
    fun addPlayer(playerUUID: String): Boolean {
        // If Room is Full and player is in room
        if (playerList.size >= maxPlayerCount) return false
        if (playerList.contains(playerUUID)) return false
        // add to player lists
        playerList.add(playerUUID)
        // set state for player
        playerStateMap[playerUUID] = PlayerState.Living
        // Create PlatForm
        val platForm = PlatForm()
        platForm.world = world
        platForm.roomInstance = this
        platForm.player = Bukkit.getPlayer(UUID.fromString(playerUUID))
        platForm.generateWaitSpace()
        playerPlatFormMap[playerUUID] = platForm
        // Teleport to Wait Space and set score board
        Bukkit.getPlayer(UUID.fromString(playerUUID))?.let {
            safeTeleport(it, platForm.waitSpaceLocation, 20)
            it.scoreboard = PlayerUtils.createScoreboard(this)
        }
        val player = Bukkit.getPlayer(UUID.fromString(playerUUID))
        // Clear Inventory
        player?.inventory?.clear()
        // set score board
        return true
    }

    // remove player from Room
    fun removePlayer(playerUUID: String): Boolean {
        if (playerList.size <= 0) return false
        if (!playerList.contains(playerUUID)) return false
        Bukkit.getPlayer(UUID.fromString(playerUUID))?.let {
            setPlayerNew(it)
            it.gameMode = GameMode.SURVIVAL
            it.scoreboard = Bukkit.getScoreboardManager()?.newScoreboard!!
        }
        if (gameState == GameState.Gaming) {
            playerPlatFormMap[playerUUID]?.deletePlatform()
            playerDieList.add(playerUUID)
        }
        playerList.remove(playerUUID)
        playerPlatFormMap.remove(playerUUID)
        playerDieList.remove(playerUUID)
        playerStateMap.remove(playerUUID)
        return true
    }

    fun getRoomPlayerList(): ArrayList<String> {
        return playerList
    }

    fun getPlatFormByPlayer(player: Player): PlatForm? {
        return playerPlatFormMap[player.uniqueId.toString()]
    }

    // Player Die
    fun onPlayerRespawn(player: Player) {
        // get platform
        val platForm = playerPlatFormMap[player.uniqueId.toString()]
        if (platForm != null) {
            // if not starting tp to wait space
            if (gameState == GameState.Waiting) {
                object : BukkitRunnable() {
                    override fun run() {
                        safeTeleport(player, platForm.waitSpaceLocation.clone().add(0.0, 1.0, 0.0), 20)
                    }
                }.runTaskLater(PluginConfig.pluginInstance!!, 5)
                return
            }
            // check dry
            platForm.checkDry()
            // respawn timer
            var timer = PluginConfig.respawnTime
            val maxTimer = PluginConfig.respawnTime
            // set player state dead
            playerStateMap[player.uniqueId.toString()] = PlayerState.Dead
            object : BukkitRunnable() {
                override fun run() {
                    //true respawn
                    if (timer <= 0) {
                        object : BukkitRunnable() {
                            override fun run() {
                                safeTeleport(player, platForm.location, 20)
                                // player is true dead
                                if (platForm.isDry) {
                                    setPlayerNew(player)
                                    player.gameMode = GameMode.SPECTATOR
                                    playerStateMap[player.uniqueId.toString()] = PlayerState.Watching
                                    platForm.isDied = true
                                } else {
                                    setPlayerNew(player)
                                    platForm.checkAndGiveTestFishingRod()
                                    playerStateMap[player.uniqueId.toString()] = PlayerState.Living
                                }
                                checkWin()
                            }
                        }.runTaskLater(PluginConfig.pluginInstance!!, 5)
                        cancel()
                    } else {
                        player.gameMode = GameMode.SPECTATOR
                        // teleport to watching
                        object : BukkitRunnable() {
                            override fun run() {
                                if (player.killer != null) {
                                    safeTeleport(player, player.killer!!.location, 20)
                                } else {
                                    safeTeleport(player, platForm.location, 20)
                                }
                            }
                        }.runTaskLater(PluginConfig.pluginInstance!!, 5)
                        // timer title
                        player.sendTitle(
                            getMessage("respawnTitle", player).replace("{timer}", timer.toString())
                                .replace("{maxTimer}", maxTimer.toString()),
                            getMessage("respawnSubTitle", player).replace("{timer}", timer.toString())
                                .replace("{maxTimer}", maxTimer.toString()),
                            0,
                            20,
                            0
                        )
                        timer--
                    }
                }
            }.runTaskTimer(PluginConfig.pluginInstance!!, 0, 20)
        }
    }

    /**
     * Check that the player is standing in a safe position
     */
    private fun isSafeLocation(loc: Location): Boolean {
        val block = loc.block
        val blockAbove = block.getRelative(0, 1, 0)
        val blockBelow = block.getRelative(0, -1, 0)
        if (block.type != Material.AIR) {
            return false
        }
        if (blockAbove.type != Material.AIR) {
            return false
        }
        return blockBelow.type.isSolid
    }

    private fun checkWin() {
        fun checkDisconnectionWin(): Boolean {
            var livingCount = 0
            var otherCount = 0
            playerStateMap.forEach {
                if (it.value == PlayerState.Living) {
                    livingCount++
                } else if (it.value == PlayerState.Watching || it.value == PlayerState.Disconnected) {
                    otherCount++
                }
            }
            if (livingCount + otherCount == playerList.size) {
                return true
            } else {
                return false
            }
        }
        // if only player living
        if ((playerDieList.size == playerList.size - 1) || (gameState == GameState.Gaming && checkDisconnectionWin())) {
            val sub = playerList.subtract(playerDieList)
            val winner: Player? = if (!sub.isEmpty()) {
                Bukkit.getPlayer(
                    UUID.fromString(
                        sub.first().toString()
                    )
                )
            } else {
                Bukkit.getPlayer(UUID.fromString(playerDieList[playerDieList.size - 1]))
            }
            // send winner title
            winner?.let {
                winner.sendTitle(getMessage("winTitle", winner), getMessage("winSubTitle", winner), 0, 40, 0)
                // add stat
                GameStat.addWin(winner.uniqueId)
                setPlayerNew(winner)
            }
            // clear game
            object : BukkitRunnable() {
                override fun run() {
                    playerList.forEach { it ->
                        val otherPlayer = Bukkit.getPlayer(UUID.fromString(it))
                        otherPlayer?.let {
                            // teleport to main world
                            safeTeleport(it, Bukkit.getWorlds()[0].spawnLocation, 20)
                            // send end game message
                            val index = playerDieList.indexOf(it.uniqueId.toString())
                            val rank: String
                            if (index == -1) {
                                rank = "1"
                            } else if (playerDieList.size == playerList.size) {
                                rank = (playerDieList.size - index).toString()
                            } else {
                                rank = (playerDieList.size - index + 1).toString()
                            }
                            it.sendMessage(
                                getMessage("gameEnd", it).replace("{rank}", rank)
                                    .replace("{winner}", winner?.name.toString())
                            )
                            setPlayerNew(otherPlayer)
                        }
                    }
                    GameManager.endGame(gameUUID)
                }
            }.runTaskLater(PluginConfig.pluginInstance!!, 20 * 5)
        }
    }

    // Teleport Player to Safe Location
    private fun safeTeleport(player: Player, targetLocation: Location, maxHeightOffset: Int = 20): Boolean {
        val safeLocation = findSafeLocation(targetLocation.clone().add(0.0, 2.0, 0.0), maxHeightOffset)
        player.teleport(safeLocation)
        return true
    }

    // Find Safe Location
    private fun findSafeLocation(originalLocation: Location, maxHeightOffset: Int): Location {
        val safeLocation = originalLocation.clone()

        for (i in 0..maxHeightOffset) {
            safeLocation.add(0.0, 0.0, i.toDouble())
            if (isSafeLocation(safeLocation)) {
                return safeLocation
            }
            safeLocation.subtract(0.0, i.toDouble(), 0.0)
        }

        for (i in 1..maxHeightOffset) {
            safeLocation.subtract(i.toDouble(), 0.0, 0.0)
            if (isSafeLocation(safeLocation)) {
                return safeLocation
            }
            safeLocation.add(0.0, i.toDouble(), 0.0)
        }
        return originalLocation
    }

    // start game
    fun startGame() {
        gameState = GameState.Gaming
        var index = 0
        rewardTimerBack()
        playerList.forEach {
            index++
            val player = Bukkit.getPlayer(UUID.fromString(it))
            if (player != null) {
                setPlayerNew(player)
                playerPlatFormMap[it]?.checkAndGiveTestFishingRod()
                playerPlatFormMap[it]?.generatePlatform(index, playerList.size)
                playerPlatFormMap[it]?.rewardChestTasker()
                safeTeleport(player, playerPlatFormMap[it]?.location!!, 20)
                player.sendTitle(
                    getMessage("gameStartTitle", player), getMessage("gameStartSubTitle", player), 0, 20 * 3, 0
                )
            }
        }
        playerDieList.clear()
        //add stat
        GameStat.increaseCount()
        endGameCountDownTimer()
    }

    // send data to score board
    private fun rewardTimerBack() {
        object : BukkitRunnable() {
            override fun run() {
                if (gameState == GameState.Gaming) {
                    rewardTimer--
                    if (rewardTimer <= 0) {
                        rewardTimer = PluginConfig.rewardRefreshTime

                    }
                }
                if (gameState == GameState.Ending) {
                    cancel()
                }
                for (player in playerList) {
                    val p = Bukkit.getPlayer(UUID.fromString(player))
                    if (p != null) {
                        p.scoreboard = PlayerUtils.createScoreboard(this@GameRoom)
                    }
                    Bukkit.getPlayer(UUID.fromString(player))?.let {
                        getPlatFormByPlayer(it)?.checkDry(timer = true)
                    }
                }
            }
        }.runTaskTimer(PluginConfig.pluginInstance!!, 0, 20)
    }

    fun endGameCountDownTimer() {
        endTimeCount = PluginConfig.endGameCountDown
        object : BukkitRunnable() {
            override fun run() {
                if (endTimeCount <= 0) {
                    GameManager.endGame(gameUUID)
                    cancel()
                }
                playerList.forEach {
                    val player = Bukkit.getPlayer(UUID.fromString(it))
                    player?.scoreboard = PlayerUtils.createScoreboard(this@GameRoom)
                }
                endTimeCount--
            }
        }.runTaskTimer(PluginConfig.pluginInstance!!, 0, 20)
    }
}
