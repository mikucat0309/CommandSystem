package com.github.mikucat0309.commandsystem.command.dispatcher

import com.github.mikucat0309.commandsystem.command.*
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.*
import java.util.*
import java.util.function.Function
import kotlin.streams.toList


/**
 * A simple implementation of a [Dispatcher].
 */
class SimpleDispatcher
/**
 * Creates a new dispatcher with a specific disambiguator.
 *
 * @param disambiguatorFunc Function that returns the preferred command if multiple exist for a
 * given alias
 */
constructor(private val disambiguatorFunc: Disambiguator = FIRST_DISAMBIGUATOR) :
        Dispatcher {

    val commands: ArrayListMultimap<String, CommandMapping> = ArrayListMultimap.create<String, CommandMapping>()

    @Synchronized
    override fun getCommands(): Set<CommandMapping> {
        return this.commands.values().toSet()
    }

    @Synchronized
    override fun getPrimaryAliases(): Set<String> {
        val aliases = HashSet<String>()

        for (mapping in this.commands.values()) {
            aliases.add(mapping.primaryAlias)
        }

        return Collections.unmodifiableSet(aliases)
    }

    @Synchronized
    override fun getAliases(): Set<String> {
        val aliases = HashSet<String>()

        for (mapping in this.commands.values()) {
            aliases.addAll(mapping.allAliases)
        }

        return Collections.unmodifiableSet(aliases)
    }

    override fun getAll(): Multimap<String, CommandMapping> {
        return ImmutableMultimap.copyOf(this.commands)
    }

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
     * @param callable The command
     * @param alias    An array of aliases
     * @return The registered command mapping, unless no aliases could be registered
     */
    fun register(
            callable: CommandCallable,
            vararg alias: String
    ): CommandMapping? {
        checkNotNull(alias, "alias")
        return register(callable, listOf(*alias))
    }

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
     * @param callable The command
     * @param aliases  A list of aliases
     * @return The registered command mapping, unless no aliases could be registered
     */
    fun register(
            callable: CommandCallable,
            aliases: List<String>
    ): CommandMapping? {
        return register(callable, aliases, Function.identity())
    }

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
     * @param callable The command
     * @param aliases  A list of aliases
     * @param callback The callback
     * @return The registered command mapping, unless no aliases could be registered
     */
    @Synchronized
    fun register(
            callable: CommandCallable,
            aliases: List<String>,
            callback: Function<List<String>, List<String>>
    ): CommandMapping? {
        var aliases = aliases
        checkNotNull(aliases, "aliases")
        checkNotNull(callable, "callable")
        checkNotNull(callback, "callback")

        // Invoke the callback with the commands that /can/ be registered

        aliases = ImmutableList.copyOf(callback.apply(aliases))
        if (aliases.isEmpty()) {
            return null
        }
        val primary = aliases[0]
        val secondary = aliases.subList(1, aliases.size)
        val mapping = ImmutableCommandMapping(callable, primary, secondary)

        for (alias in aliases) {
            this.commands.put(alias.toLowerCase(), mapping)
        }

        return mapping
    }

    /**
     * Remove a mapping identified by the given alias.
     *
     * @param alias The alias
     * @return The previous mapping associated with the alias, if one was found
     */
    @Synchronized
    fun remove(alias: String): Collection<CommandMapping> {
        return this.commands.removeAll(alias.toLowerCase())
    }

    /**
     * Remove all mappings identified by the given aliases.
     *
     * @param aliases A collection of aliases
     * @return Whether any were found
     */
    @Synchronized
    fun removeAll(aliases: Collection<*>): Boolean {
        checkNotNull(aliases, "aliases")

        var found = false

        for (alias in aliases) {
            if (this.commands.removeAll(alias.toString().toLowerCase()).isNotEmpty()) {
                found = true
            }
        }

        return found
    }

    /**
     * Remove a command identified by the given mapping.
     *
     * @param mapping The mapping
     * @return The previous mapping associated with the alias, if one was found
     */
    @Synchronized
    fun removeMapping(mapping: CommandMapping): Optional<CommandMapping> {
        checkNotNull(mapping, "mapping")

        var found: CommandMapping? = null

        val it = this.commands.values().iterator()
        while (it.hasNext()) {
            val current = it.next()
            if (current == mapping) {
                it.remove()
                found = current
            }
        }

        return Optional.ofNullable(found)
    }

    /**
     * Remove all mappings contained with the given collection.
     *
     * @param mappings The collection
     * @return Whether the at least one command was removed
     */
    @Synchronized
    fun removeMappings(mappings: Collection<*>): Boolean {
        checkNotNull(mappings, "mappings")

        var found = false

        val it = this.commands.values().iterator()
        while (it.hasNext()) {
            if (mappings.contains(it.next())) {
                it.remove()
                found = true
            }
        }

        return found
    }

    override fun get(alias: String): CommandMapping? {
        return get(alias, null)
    }

    @Synchronized
    override fun get(
            alias: String,
            source: CommandSource?
    ): CommandMapping? {
        val results = this.commands.get(alias.toLowerCase())
        var result: CommandMapping? = null
        if (results.size == 1) {
            result = results[0]
        } else if (results.size > 1) {
            result = this.disambiguatorFunc.disambiguate(source, alias, results)
        }
        return result
    }

    @Synchronized
    override fun containsAlias(alias: String): Boolean {
        return this.commands.containsKey(alias.toLowerCase())
    }

    override fun containsMapping(mapping: CommandMapping): Boolean {
        checkNotNull(mapping, "mapping")

        for (test in this.commands.values()) {
            if (mapping == test) {
                return true
            }
        }

        return false
    }

    @Throws(CommandException::class)
    override fun process(
            source: CommandSource,
            arguments: String
    ): CommandResult {
        val argSplit = arguments.split(" ".toRegex(), 2).toTypedArray()
        val cmd = get(argSplit[0], source) ?: throw CommandNotFoundException(
                "commands.generic.notFound", argSplit[0]) // TODO: Fix properly to use a SpongeTranslation??

        val args = if (argSplit.size > 1) argSplit[1] else ""
        val spec = cmd.callable
        try {
            return spec.process(source, args)
        } catch (e: CommandNotFoundException) {
            throw CommandException("No such child command: " + e.command)
        }

    }

    @Throws(CommandException::class)
    override fun getSuggestions(source: CommandSource, arguments: String): List<String> {
        val argSplit = arguments.split(" ".toRegex(), 2).toTypedArray()
        val cmd = get(argSplit[0], source)
        if (argSplit.size == 1) {
            return filterCommands(source, argSplit[0]).stream().toList()
        } else if (cmd == null) {
            return ImmutableList.of()
        }
        return cmd.callable.getSuggestions(source, argSplit[1])
    }

    override fun getShortDescription(source: CommandSource): String? {
        return null
    }

    override fun getHelp(source: CommandSource): String? {
        if (this.commands.isEmpty) {
            return null
        }
        val build = StringBuilder("Available commands:\n")
        val it = filterCommands(source).iterator()
        while (it.hasNext()) {
            val mappingOpt = get(it.next(), source) ?: continue
            val description = mappingOpt.callable.getShortDescription(source)
            build.append(mappingOpt.primaryAlias)
            if (it.hasNext()) {
                build.append('\n')
            }
        }
        return build.toString()
    }

    private fun filterCommands(src: CommandSource): Set<String> {
        return this.commands.keys().elementSet()
    }

    // Filter out commands by String first
    private fun filterCommands(src: CommandSource, start: String): Set<String> {
        val map = Multimaps.filterKeys(
                this.commands
        ) { input -> input != null && input.toLowerCase().startsWith(start.toLowerCase()) }
        return map.keys().elementSet()
    }

    /**
     * Gets the number of registered aliases.
     *
     * @return The number of aliases
     */
    @Synchronized
    fun size(): Int {
        return this.commands.size()
    }

    override fun getUsage(source: CommandSource): String {
        val build = StringBuilder()
        val filteredCommands = filterCommands(source)
                .filter {
                    val ret = get(it, source)
                    return@filter (ret != null && ret.primaryAlias == it)
                }
                .toList()

        val iterator = filteredCommands.iterator()
        while (iterator.hasNext()) {
            build.append(iterator.next())
            if (iterator.hasNext()) {
                build.append(CommandMessageFormatting.PIPE_TEXT)
            }
        }
        return build.toString()
    }

    @Synchronized
    override fun getAll(alias: String): Set<CommandMapping> {
        return ImmutableSet.copyOf(this.commands.get(alias))
    }

    companion object {

        /**
         * This is a disambiguator function that returns the first matching command.
         */
        val FIRST_DISAMBIGUATOR = object : Disambiguator {
            override fun disambiguate(
                    source: CommandSource?,
                    aliasUsed: String,
                    availableOptions: List<CommandMapping>
            ): CommandMapping? {
                for (mapping in availableOptions) {
                    if (mapping.primaryAlias.toLowerCase() == aliasUsed.toLowerCase()) {
                        return mapping
                    }
                }
                return availableOptions[0]
            }
        }
    }
}
