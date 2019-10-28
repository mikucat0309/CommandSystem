package com.github.mikucat0309.commandsystem.command.dispatcher

import com.github.mikucat0309.commandsystem.command.CommandMapping
import com.github.mikucat0309.commandsystem.command.CommandSource

@FunctionalInterface
interface Disambiguator {

    /**
     * Disambiguate an alias in cases where there are multiple command mappings registered for a given
     * alias.
     *
     * @param source           The CommandSource executing the command, if any
     * @param aliasUsed        The alias input by the user
     * @param availableOptions The commands registered to this alias
     * @return The specific command to use
     */
    fun disambiguate(
        source: CommandSource?, aliasUsed: String,
        availableOptions: List<CommandMapping>
    ): CommandMapping?

}
