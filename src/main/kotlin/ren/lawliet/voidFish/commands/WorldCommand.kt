package ren.lawliet.voidFish.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import ren.lawliet.voidFish.utils.VoidWorldGenerate


/**
 *@author        Coaixy
 *@createTime    2024-08-23
 *@packageName   ren.lawliet.voidFish.commands
 */

// The commands here are used for debugging
class WorldCommand : CommandExecutor {

    override fun onCommand(
        commandSender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>?
    ): Boolean {
        if (commandSender.isOp) {
            when (args?.size) {
                2 -> {
                    val worldName = args[1]
                    when (args[0]) {
                        "create" -> {
                            if (VoidWorldGenerate.generateWorld(worldName)) {
                                commandSender.sendMessage("§aSuccess")
                            }
                        }

                        "delete" -> {
                            if (VoidWorldGenerate.worldManagerInstance?.deleteWorld(worldName, true) == true) {
                                commandSender.sendMessage("§aSuccess")
                            } else {
                                commandSender.sendMessage("§cFailed")
                            }
                        }

                        else -> {
                            commandSender.sendMessage("§cUnknown Parameter")
                        }
                    }
                }

                1 -> {
                    when (args[0]) {
                        "list" -> {
                            val worldList = VoidWorldGenerate.worldManagerInstance?.mvWorlds
                            val defaultWorldName = Bukkit.getWorlds()[0].name
                            worldList?.forEach {
                                if (!it.name.startsWith(defaultWorldName)) {
                                    commandSender.sendMessage("§a${it.name}")
                                }
                            }
                        }

                        "clear" -> {
                            VoidWorldGenerate.worldManagerInstance?.mvWorlds?.forEach {
                                if (it.name.startsWith("VoidGame-")) {
                                    VoidWorldGenerate.worldManagerInstance?.deleteWorld(it.name, true)
                                }
                            }
                            commandSender.sendMessage("§aSuccess")
                        }

                        else -> {
                            commandSender.sendMessage("§cUnknown Parameter")
                        }
                    }
                }

                else -> {
                    commandSender.sendMessage("§cParameter error")
                }
            }
        } else {
            commandSender.sendMessage("§cPermission denied")
        }
        return true
    }
}