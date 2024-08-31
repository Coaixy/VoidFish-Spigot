package ren.lawliet.voidFish.listeners

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ren.lawliet.voidFish.configs.PluginConfig
import ren.lawliet.voidFish.game.GameManager
import ren.lawliet.voidFish.game.GameState
import ren.lawliet.voidFish.utils.WaterUtils
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


/**
 *@author        Coaixy
 *@createTime    2024-08-23
 *@packageName   ren.lawliet.voidFish.listeners
 */

class InGameEvent : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (GameManager.getGameRoomByPlayer(event.player) != null && GameManager.getGameRoomByPlayer(event.player)?.gameState == GameState.Waiting) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (GameManager.getGameRoomByPlayer(event.player) != null &&
            (GameManager.getGameRoomByPlayer(event.player)?.gameState == GameState.Gaming
                    || GameManager.getGameRoomByPlayer(event.player)?.gameState == GameState.Waiting)
        ) {
            val player = event.player
            GameManager.getGameRoomByPlayer(player)?.onPlayerRespawn(player)
        }
        if (!GameManager.getPlayerMap().containsKey(event.player.uniqueId.toString())) {
            event.player.teleport(Bukkit.getServer().worlds[0].spawnLocation)
            event.player.inventory.clear()
        }
    }

    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        if (GameManager.getGameRoomByPlayer(event.player) == null) return
        val random = java.util.Random()
        val player = event.player
        if (!WaterUtils.isWaterBlock(event.hook.location.block)) return
        if (event.state == PlayerFishEvent.State.REEL_IN) {
            val generateRandom: Int = random.nextInt(100) + 1
            val entityProbability = PluginConfig.entity_probability
            if (generateRandom >= entityProbability) {
                val randomItem = Material.entries[random.nextInt(Material.entries.size)]
                if (!isItemBlacklisted(randomItem)) {
                    val itemStack = ItemStack(randomItem)
                    val droppedItem: Item = player.world.dropItem(event.hook.location, itemStack)

                    droppedItem.pickupDelay = 0
                    val playerLocation: Vector = player.location.toVector()
                    val itemLocation: Vector = droppedItem.location.toVector()
                    applyVelocityWithParabola(itemLocation, playerLocation, droppedItem)

                    event.hook.hookedEntity = droppedItem
                }
            } else {
                val randomEntity = EntityType.entries[random.nextInt(EntityType.entries.size)]
                if (!isEntityBlacklisted(randomEntity)) {
                    try {
                        val entity: Entity = player.world.spawnEntity(event.hook.location, randomEntity)

                        val playerLocation: Vector = player.location.toVector()
                        val entityLocation: Vector = entity.location.toVector()
                        applyVelocityWithParabola(entityLocation, playerLocation, entity)

                        event.hook.hookedEntity = entity
                    } catch (ignored: Exception) {
                    }
                }
            }

            if (event.caught != null) {
                event.caught!!.remove()
            }
        }
    }

    private fun isEntityBlacklisted(randomEntity: EntityType): Boolean {
        return PluginConfig.black_entity_list.contains(randomEntity.name)
    }

    private fun isItemBlacklisted(randomItem: Material): Boolean {
        return PluginConfig.black_item_list.contains(randomItem.name)
    }

    private fun applyVelocityWithParabola(origin: Vector, target: Vector, entity: Entity) {
        val direction = target.subtract(origin)
        val distance = sqrt(direction.x.pow(2.0) + direction.z.pow(2.0))
        val yBoost = min(distance * 0.55, 3.0)
        val speedMultiplier = min(distance * 0.11, 2.5)
        direction.setY(yBoost)
        entity.velocity = direction.normalize().multiply(speedMultiplier)
    }

}