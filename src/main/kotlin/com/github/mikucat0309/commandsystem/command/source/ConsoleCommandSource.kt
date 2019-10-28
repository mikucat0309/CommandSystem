package com.github.mikucat0309.commandsystem.command.source

import com.github.mikucat0309.commandsystem.command.CommandSource
import java.util.*

class ConsoleCommandSource(override val name: String) : CommandSource {

    override fun sendMessage(message: String) {
        println(message)
    }

    override val locale: Locale
        get() = super.locale
}