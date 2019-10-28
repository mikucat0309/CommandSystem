package com.github.mikucat0309.commandsystem.command.source

import com.github.mikucat0309.commandsystem.command.CommandSource
import com.github.mikucat0309.commandsystem.locale.Locales
import java.util.*

open class ConsoleCommandSource(
    override val name: String,
    override val locale: Locale = Locales.DEFAULT
) : CommandSource {

    override fun sendMessage(message: String) {
        println(message)
    }
}