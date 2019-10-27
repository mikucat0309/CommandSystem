package com.github.mikucat0309.commandsystem.command.source

import com.github.mikucat0309.commandsystem.command.CommandSource

class ConsoleCommandSource(override val name: String) : CommandSource {

    override fun sendMessage(message: String) {
        println(message)
    }
}