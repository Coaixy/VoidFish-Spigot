package ren.lawliet.voidFish

import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import java.util.*

/**
 *@author        Coaixy
 *@createTime    2024-08-22
 *@packageName   ren.lawliet.voidFish.utils
 */

class VoidWorldGenerator : ChunkGenerator() {
    @Deprecated("Deprecated in Java", ReplaceWith("createChunkData(world)"))
    override fun generateChunkData(world: World, random: Random, x: Int, z: Int, biome: BiomeGrid): ChunkData {
        return createChunkData(world)
    }
}