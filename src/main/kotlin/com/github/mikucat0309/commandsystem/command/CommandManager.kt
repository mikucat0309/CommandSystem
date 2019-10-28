package com.github.mikucat0309.commandsystem.command

import com.github.mikucat0309.commandsystem.MetaData
import com.github.mikucat0309.commandsystem.command.dispatcher.Dispatcher
import java.util.*
import java.util.function.Function

/**
 * A command dispatcher watches for commands (such as those said in chat) and dispatches them to the
 * correct command handler.
 */
interface CommandManager : Dispatcher {

    /**
     * Register a given command using the given list of aliases.
     *
     *
     * If there is a conflict with one of the aliases (i.e. that alias
     * is already assigned to another command), then the alias will be skipped. It is possible for
     * there to be no alias to be available out of the provided list of aliases, which would mean that
     * the command would not be assigned to any aliases.
     *
     *
     * The first non-conflicted alias becomes the "primary alias."
     *
     * @param metaData   A metaData instance
     * @param callable The command
     * @param alias    An array of aliases
     * @return The registered command mapping, unless no aliases could be registered
     * @throws IllegalArgumentException Thrown if `metaData` is not a metaData instance
     */
    fun register(
        metaData: MetaData,
        callable: CommandCallable,
        vararg alias: String
    ): CommandMapping?

    /**
     * Register a given command using the given list of aliases.
     *
     *
     * If there is a conflict with one of the aliases (i.e. that alias
     * is already assigned to another command), then the alias will be skipped. It is possible for
     * there to be no alias to be available out of the provided list of aliases, which would mean that
     * the command would not be assigned to any aliases.
     *
     *
     * The first non-conflicted alias becomes the "primary alias."
     *
     * @param metaData   A metadata instance
     * @param callable The command
     * @param aliases  A list of aliases
     * @return The registered command mapping, unless no aliases could be registered
     * @throws IllegalArgumentException Thrown if `metaData` is not a metaData instance
     */
    fun register(
        metaData: MetaData,
        callable: CommandCallable,
        aliases: List<String>
    ): CommandMapping?

    /**
     * Register a given command using a given list of aliases.
     *
     *
     * The provided callback function will be called with a list of aliases
     * that are not taken (from the list of aliases that were requested) and it should return a list
     * of aliases to actually register. Aliases may be removed, and if no aliases remain, then the
     * command will not be registered. It may be possible that no aliases are available, and thus the
     * callback would receive an empty list. New aliases should not be added to the list in the
     * callback as this may cause [IllegalArgumentException] to be thrown.
     *
     *
     * The first non-conflicted alias becomes the "primary alias."
     *
     * @param metaData   A metaData instance
     * @param callable The command
     * @param aliases  A list of aliases
     * @param callback The callback
     * @return The registered command mapping, unless no aliases could be registered
     * @throws IllegalArgumentException Thrown if new conflicting aliases are added in the callback
     * @throws IllegalArgumentException Thrown if `metaData` is not a metaData instance
     */
    fun register(
        metaData: MetaData, callable: CommandCallable, aliases: List<String>,
        callback: Function<List<String>, List<String>>
    ): CommandMapping?

    /**
     * Remove a command identified by the given mapping.
     *
     * @param mapping The mapping
     * @return The previous mapping associated with the alias, if one was found
     */
    fun removeMapping(mapping: CommandMapping): Optional<CommandMapping>

//    /**
//     * Gets a set of commands owned by the given plugin instance.
//     *
//     * @param instance The plugin instance
//     * @return A set of mappings
//     */
//    fun getOwnedBy(instance: Any): Set<CommandMapping>

    /**
     * Gets the number of registered aliases.
     *
     * @return The number of aliases
     */
    fun size(): Int

    /**
     * Execute the command based on input arguments.
     *
     *
     * The implementing class must perform the necessary permission
     * checks.
     *
     * @param source    The caller of the command
     * @param arguments The raw arguments for this command
     * @return The result of a command being processed
     */
    override fun process(
        source: CommandSource,
        arguments: String
    ): CommandResult

}
