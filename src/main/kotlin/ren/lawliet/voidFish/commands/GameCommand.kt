package ren.lawliet.voidFish.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ren.lawliet.voidFish.configs.GameMessage.getMessage
import ren.lawliet.voidFish.configs.GameMessage.sendMsg
import ren.lawliet.voidFish.game.GameManager
import ren.lawliet.voidFish.game.GameManager.getWaitRoom
import ren.lawliet.voidFish.game.GameState
import java.util.*


/**
 *@author        Coaixy
 *@createTime    2024-08-23
 *@packageName   ren.lawliet.voidFish.commands
 */

class GameCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender is Player) {
            if (args?.size == 1 && sender.hasPermission("voidFish.game")) {
                when (args[0]) {
                    "match" -> {
                        sender.sendMsg("matching")
                        if (GameManager.getPlayerMap().containsKey(sender.uniqueId.toString())) {
                            sender.sendMsg("isInRoom")
                            return true
                        }
                        // Get Wait Room
                        val waitRoom = getWaitRoom()
                        if (waitRoom != null) {
                            // Add Player
                            waitRoom.addPlayer(sender.uniqueId.toString())
                            GameManager.getPlayerMap()[sender.uniqueId.toString()] = waitRoom
                            // Send Message
                            val message = TextComponent(getMessage("roomInfo", sender))
                            message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, waitRoom.gameUUID)
                            sender.spigot().sendMessage(message)
                            // notify other player
                            waitRoom.getRoomPlayerList().forEach {
                                if (it != sender.uniqueId.toString()) {
                                    val player = sender.server.getPlayer(UUID.fromString(it))
                                    player?.sendMessage(
                                        getMessage("newPlayerJoin", player).replace(
                                            "{otherPlayer}",
                                            sender.name
                                        )
                                    )
                                }
                            }
                        }
                    }

                    "leave" -> {
                        if (GameManager.removePlayer(sender)) {
                            sender.sendMsg("leaveRoom")
                        } else {
                            sender.sendMsg("isNotInRoom")
                        }
                    }

                    "list" -> {
                        if (!sender.isOp) {
                            sender.sendMessage("§cPermission Denied")
                            return true
                        }
                        val gameList = GameManager.getGameMap()
                        gameList.forEach {
                            sender.sendMessage("§a${it.key} - ${it.value.getRoomPlayerList().size} - ${it.value.gameState}")
                        }
                        sender.sendMessage("")
                    }

                    "start" -> {
                        if (!sender.isOp) {
                            sender.sendMessage("§cUnknown Parameter")
                            return true
                        }
                        val room = GameManager.getPlayerMap()[sender.uniqueId.toString()]
                        if (room != null && room.gameState == GameState.Waiting) {
                            room.startGame()
                        }
                    }

                    else -> {
                        sender.sendMessage("§cUnknown Parameter")
                    }
                }
            } else if (args?.size == 2 && sender.isOp) {
                when (args[0]) {
                    "end" -> {
                        sender.sendMessage("§Ending ${args[1]}")
                        if (GameManager.endGame(args[1])) {
                            sender.sendMessage("§aSuccess")
                        } else {
                            sender.sendMessage("§cFailed")
                        }
                        sender.sendMessage("")
                    }

                    "del" -> {
                        sender.sendMessage("§aDeleting ${args[1]}")
                        if (GameManager.delGame(args[1])) {
                            sender.sendMessage("§aSuccess")
                        } else {
                            sender.sendMessage("§cFailed")
                        }
                        sender.sendMessage("")
                    }
                }
            }
        }
        return true
    }

}