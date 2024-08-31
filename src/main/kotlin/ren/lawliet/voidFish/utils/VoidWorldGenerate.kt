package ren.lawliet.voidFish.utils

import com.onarandombox.MultiverseCore.api.MVWorldManager
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldType

/**
 *@author        Coaixy
 *@createTime    2024-08-22
 *@packageName   ren.lawliet.voidFish.utils
 */

object VoidWorldGenerate {
    var worldManagerInstance: MVWorldManager? = null
    fun initWorldManager(worldManager: MVWorldManager) {
        worldManagerInstance = worldManager
    }

    fun generateWorld(worldName: String): Boolean {
        if (worldManagerInstance == null) {
            return false
        }
        val flag = worldManagerInstance!!.addWorld(
            worldName,
            World.Environment.NORMAL,
            null,
            WorldType.FLAT,
            false,
            "VoidFish",
        )
        worldManagerInstance!!.getMVWorld(worldName)?.let {
            setDayAndClearWeather(it.cbWorld)
        }
        return flag
    }

    private fun setDayAndClearWeather(world: World) {
        world.time = 1000L
        world.setGameRuleValue("doDaylightCycle", "false")
        world.setStorm(false)
        world.isThundering = false
        world.setGameRuleValue("doWeatherCycle", "false")
    }
}
