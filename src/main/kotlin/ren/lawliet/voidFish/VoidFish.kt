package ren.lawliet.voidFish

import com.onarandombox.MultiverseCore.MultiverseCore
import org.bukkit.Bukkit
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import ren.lawliet.voidFish.commands.GameCommand
import ren.lawliet.voidFish.commands.WorldCommand
import ren.lawliet.voidFish.configs.GameMessage
import ren.lawliet.voidFish.configs.GameStat
import ren.lawliet.voidFish.configs.PluginConfig
import ren.lawliet.voidFish.listeners.InGameEvent
import ren.lawliet.voidFish.listeners.QuitEvent
import ren.lawliet.voidFish.listeners.SocializeEvent
import ren.lawliet.voidFish.utils.Schedule
import ren.lawliet.voidFish.utils.VoidWorldGenerate


class VoidFish : JavaPlugin() {

    override fun onEnable() {
        PluginConfig.init(this)
        GameMessage.init()
        GameStat.init()
        // Register Multiverse Core
        Bukkit.getServer().pluginManager.getPlugin("Multiverse-Core")?.let {
            (it as MultiverseCore).mvWorldManager
        }?.let { VoidWorldGenerate.initWorldManager(it) }
        // Register PlaceholderAPI
        Bukkit.getServer().pluginManager.getPlugin("PlaceholderAPI")?.let {
            registerPlaceholderApi(it)
        }
        // Register Plugin Something
        registerCommands()
        registerListeners()
        registerSchedulers()
        // clear Old Game World
        clearWorlds()
    }

    private fun registerPlaceholderApi(placeholderAPI: Plugin) {
        PlaceholderExpansion(this).register()
    }

    private fun clearWorlds() {
        VoidWorldGenerate.worldManagerInstance?.mvWorlds?.forEach {
            if (it.name.contains("VoidGame")) {
                VoidWorldGenerate.worldManagerInstance?.deleteWorld(it.name, true)
            }
        }
    }

    private fun registerSchedulers() {
        val schedule = Schedule()
        schedule.deleteRoomTask()
    }

    private fun registerCommands() {
        getCommand("vfWorld")?.setExecutor(WorldCommand())
        getCommand("vfGame")?.setExecutor(GameCommand())
    }

    private fun registerListeners() {
        Bukkit.getPluginManager().registerEvents(QuitEvent(), this)
        Bukkit.getPluginManager().registerEvents(InGameEvent(), this)
        Bukkit.getPluginManager().registerEvents(SocializeEvent(), this)
    }

    // Void World to Multiverse Core
    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}
