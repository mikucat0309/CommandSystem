package com.github.mikucat0309.commandsystem.command.dispatcher

import com.github.mikucat0309.commandsystem.command.CommandCallable
import com.github.mikucat0309.commandsystem.command.CommandMapping
import com.github.mikucat0309.commandsystem.command.CommandSource
import com.google.common.collect.Multimap

/**
 * Executes a command based on user input.
 */
interface Dispatcher : CommandCallable {

    /**
     * Gets a list of commands. Each command, regardless of how many aliases it may have, will only
     * appear once in the returned set.
     *
     *
     * The returned collection cannot be modified.
     *
     * @return A list of registrations
     */
    fun getCommands(): Set<CommandMapping>

    /**
     * Gets a list of primary aliases.
     *
     *
     * The returned collection cannot be modified.
     *
     * @return A list of aliases
     */
    fun getPrimaryAliases(): Set<String>

    /**
     * Gets a list of all the command aliases, which includes the primary alias.
     *
     *
     * A command may have more than one alias assigned to it. The returned
     * collection cannot be modified.
     *
     * @return A list of aliases
     */
    fun getAliases(): Set<String>

    /**
     * Gets all commands currently registered with this dispatcher. The returned value is immutable.
     *
     * @return a multimap from alias to mapping of every registered command
     */
    fun getAll(): Multimap<String, CommandMapping>

    /**
     * Gets the [CommandMapping] associated with an alias. Returns null if no command is named
     * by the given alias.
     *
     * @param alias The alias
     * @return The command mapping, if available
     */
    operator fun get(alias: String): CommandMapping?

    /**
     * Gets the [CommandMapping] associated with an alias in the context of a given [ ]. Returns null if no command is named by the given alias.
     *
     * @param alias  The alias to look up
     * @param source The source this alias is being looked up for
     * @return The command mapping, if available
     */
    operator fun get(
            alias: String,
            source: CommandSource?
    ): CommandMapping?

    /**
     * Gets all the [CommandMapping]s associated with an alias.
     *
     * @param alias The alias
     * @return The command mappings associated with the alias
     */
    fun getAll(alias: String): Set<CommandMapping>

    /**
     * Returns whether the dispatcher contains a registered command for the given alias.
     *
     * @param alias The alias
     * @return True if a registered command exists
     */
    fun containsAlias(alias: String): Boolean

    /**
     * Returns whether the dispatcher contains the given mapping.
     *
     * @param mapping The mapping
     * @return True if a mapping exists
     */
    fun containsMapping(mapping: CommandMapping): Boolean
}
