package ren.lawliet.voidFish.utils

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged

/**
 *@author        Coaixy
 *@createTime    2024-08-24
 *@packageName   ren.lawliet.voidFish.utils
 */

object WaterUtils {
    fun isWaterBlock(block: Block): Boolean {
        val type = block.type
        // Check if it's a water cube
        if (type == Material.WATER || type == Material.KELP || type == Material.KELP_PLANT ||
            type == Material.SEAGRASS || type == Material.TALL_SEAGRASS
        ) {
            return true
        }
        // Check for water-laden stairs or other cubes
        if (block.blockData is Waterlogged) {
            val waterlogged = block.blockData as Waterlogged
            return waterlogged.isWaterlogged
        }
        return false
    }


}