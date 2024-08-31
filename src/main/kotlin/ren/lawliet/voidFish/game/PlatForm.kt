package ren.lawliet.voidFish.game

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import ren.lawliet.voidFish.configs.GameMessage
import ren.lawliet.voidFish.configs.GameMessage.sendMsg
import ren.lawliet.voidFish.configs.PluginConfig
import ren.lawliet.voidFish.utils.WaterUtils
import kotlin.math.abs
import kotlin.random.Random


/**
 *@author        Coaixy
 *@createTime    2024-08-23
 *@packageName   ren.lawliet.voidFish.game
 */

class PlatForm {
    // Game Room Instance
    var roomInstance: GameRoom? = null

    // platform y
    val y = 5.0

    // world instance
    var world: MultiverseWorld? = null

    // owner
    var player: Player? = null

    // platform location
    var location: Location = Location(world?.cbWorld, 0.0, 0.0, 0.0)

    // wait space location
    var waitSpaceLocation = Location(world?.cbWorld, 0.0, y, 0.0)

    // reward chest location
    var chestLocationList = ArrayList<Location>()

    // is dry
    var isDry = false

    // true died
    var isDied = false

    fun generateWaitSpace() {
        waitSpaceLocation = world?.spawnLocation?.clone()!!
        waitSpaceLocation.y = y + PluginConfig.waitSpaceAdditionY
        val size = 8
        for (i in -size / 2 until size / 2) {
            for (j in -size / 2 until size / 2) {
                // bedrock
                val block: Block = waitSpaceLocation.clone().add(i.toDouble(), 0.0, j.toDouble()).block
                block.type = Material.BEDROCK
                // reward chest
                if (
                    (i == -3 && j == 0) ||
                    (i == 3 && j == 0) ||
                    (i == 0 && j == -3) ||
                    (i == 0 && j == 3)
                ) {
                    val chestLocation = waitSpaceLocation.clone().add(i.toDouble(), 1.0, j.toDouble())
                    chestLocation.block.type = Material.CHEST
                    chestLocationList.add(chestLocation)
                }
            }
        }
    }

    fun checkDry(timer: Boolean = false) {
        val size = 4
        var waterCount = 0
        // Check four squares of water, including the water cubes.
        for (i in 0 until size) {
            for (j in 0 until size) {
                val block: Block = location.clone().add(i.toDouble(), 0.0, j.toDouble()).block
                if ((i == 1 && j == 1) || (i == 1 && j == 2) || (i == 2 && j == 1) || (i == 2 && j == 2)) {
                    if (!WaterUtils.isWaterBlock(block)) {
                        waterCount++
                    }
                }
            }
        }
        if (waterCount == 4) {
            if (!timer) {
                player?.sendTitle(
                    GameMessage.getMessage("dieMainTitle", player!!),
                    GameMessage.getMessage("dieSubTitle", player!!),
                    0,
                    20,
                    0
                )
                player?.gameMode = GameMode.SPECTATOR
                roomInstance?.playerDieList?.add(player?.uniqueId.toString())
            }
            isDry = true
        }
    }

    private fun generatePlatformLocation(world: World, index: Int, total: Int): Location {
        val distance = PluginConfig.pluginInstance?.getConfig()?.getInt("platform_distance", 20)!!
        val spawnLocation = world.spawnLocation

        // Calculate the angle between each platform (in radians)
        val angleBetweenPlatforms = 2 * Math.PI / total
        // Calculate the current platform angle
        val angle = index * angleBetweenPlatforms

        val x = spawnLocation.x + distance * Math.cos(angle)
        val z = spawnLocation.z + distance * Math.sin(angle)
        return Location(world, x, y, z)
    }

    fun generatePlatform(index: Int, total: Int) {
        val loc = generatePlatformLocation(world?.cbWorld!!, index, total)
        location = loc
        val size = 4
        for (i in 0 until size) {
            for (j in 0 until size) {
                val block: Block = loc.clone().add(i.toDouble(), 0.0, j.toDouble()).block
                if (i == 1 && j == 1) {
                    block.type = Material.WATER
                    block.location.clone().add(0.0, -1.0, 0.0).block.type = Material.BEDROCK
                } else if (i == 1 && j == 2) {
                    block.type = Material.WATER
                    block.location.clone().add(0.0, -1.0, 0.0).block.type = Material.BEDROCK
                } else if (i == 2 && j == 1) {
                    block.type = Material.WATER
                    block.location.clone().add(0.0, -1.0, 0.0).block.type = Material.BEDROCK
                } else if (i == 2 && j == 2) {
                    block.type = Material.WATER
                    block.location.clone().add(0.0, -1.0, 0.0).block.type = Material.BEDROCK
                } else {
                    block.type = Material.BEDROCK
                }
            }
        }
    }

    fun checkAndGiveTestFishingRod() {
        val player = player!!
        var hasFishingRod = false
        for (item in player.inventory.contents) {
            if (item != null && item.type == Material.FISHING_ROD) {
                val meta = item.itemMeta
                if (meta != null && PluginConfig.fishing_rod_name == meta.displayName) {
                    hasFishingRod = true
                    break
                }
            }
        }

        if (!hasFishingRod) {
            val fishingRod = ItemStack(Material.FISHING_ROD)
            val meta = fishingRod.itemMeta
            if (meta != null) {
                meta.setDisplayName(PluginConfig.fishing_rod_name)
                meta.isUnbreakable = true
                fishingRod.setItemMeta(meta)
            }
            player.inventory.addItem(fishingRod)
            player.sendMessage(
                GameMessage.getMessage("getFishingRod", player).replace("{RodName}", PluginConfig.fishing_rod_name)
            )
        }
    }

    fun deletePlatform() {
        val size = 4
        for (i in 0 until size) {
            for (j in 0 until size) {
                for (k in -2 until 1) {
                    val block: Block = location.clone().add(i.toDouble(), k.toDouble(), j.toDouble()).block
                    block.type = Material.AIR
                }
            }
        }
        isDry = true
    }

    fun rewardChestTasker() {
        object : BukkitRunnable() {
            override fun run() {
                if (roomInstance?.gameState == GameState.Gaming) {
                    player?.sendMsg("rewardRefresh")
                    chestLocationList.forEach {
                        if (it.block.type == Material.CHEST) {
                            val chest = it.block.state as org.bukkit.block.Chest
                            val inv = chest.inventory
                            val randomItemType =
                                PluginConfig.reward_item_list.random()
                            val item = ItemStack(Material.valueOf(randomItemType.uppercase()))
                            if (item.type == Material.ARROW) {
                                item.amount = 4
                            } else {
                                item.amount = 1
                            }
                            inv.addItem(ItemStack(item))
                        } else {
                            it.block.type = Material.CHEST
                        }
                    }
                }
                if (roomInstance?.gameState == GameState.Ending || isDry) {
                    this.cancel()
                }
            }
        }.runTaskTimer(PluginConfig.pluginInstance!!, 20, 20 * PluginConfig.rewardRefreshTime.toLong())
    }
}