package com.github.mikucat0309.commandsystem

import com.github.mikucat0309.commandsystem.command.*
import com.github.mikucat0309.commandsystem.command.CommandMessageFormatting.error
import com.github.mikucat0309.commandsystem.command.args.ArgumentParseException
import com.github.mikucat0309.commandsystem.command.dispatcher.Disambiguator
import com.github.mikucat0309.commandsystem.command.dispatcher.SimpleDispatcher
import com.github.mikucat0309.commandsystem.plugin.PluginContainer
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multimap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.regex.Pattern

/**
 * A simple implementation of [CommandManager].
 * This service calls the appropriate events for a command.
 */
class CommandManagerImpl
/**
 * Construct a simple [CommandManager].
 *
 * @param logger The logger to log error messages to
 * @param disambiguator The function to resolve a single command when multiple options are available
 */
(
        private val logger: Logger = LoggerFactory.getLogger(CommandManagerImpl::class.java),
        disambiguator: Disambiguator = SimpleDispatcher.FIRST_DISAMBIGUATOR
) : CommandManager {
    override fun getCommands(): Set<CommandMapping> {
        return this.dispatcher.getCommands()
    }

    override fun getPrimaryAliases(): Set<String> {
        return this.dispatcher.getPrimaryAliases()
    }

    override fun getAliases(): Set<String> {
        return this.dispatcher.getAliases()
    }

    override fun getAll(): Multimap<String, CommandMapping> {
        return this.dispatcher.getAll()
    }

    private val dispatcher: SimpleDispatcher = SimpleDispatcher(disambiguator)
    private val owners = HashMultimap.create<PluginContainer, CommandMapping>()
    private val reverseOwners = ConcurrentHashMap<CommandMapping, PluginContainer>()
    private val lock = Any()

    val pluginContainers: Set<PluginContainer>
        get() = synchronized(this.lock) {
            return ImmutableSet.copyOf(this.owners.keySet())
        }

    override fun register(
            plugin: Any,
            callable: CommandCallable,
            vararg alias: String
    ): CommandMapping? {
        return register(plugin, callable, listOf(*alias))
    }

    override fun register(
            plugin: Any,
            callable: CommandCallable,
            aliases: List<String>
    ): CommandMapping? {
        return register(plugin, callable, aliases, Function.identity())
    }

    override fun register(
            plugin: Any,
            callable: CommandCallable,
            aliases: List<String>,
            callback: Function<List<String>, List<String>>
    ): CommandMapping? {
        checkNotNull(plugin, "plugin")

        val container = CommandSystem.pluginManager.fromInstance(plugin)
        requireNotNull(container) { "The provided plugin object does not have an associated plugin container " + "(in other words, is 'plugin' actually your plugin object?" }
        synchronized(this.lock) {
            // <namespace>:<alias> for all commands
            val aliasesWithPrefix = ArrayList<String>(aliases.size * 3)
            for (originalAlias in aliases) {
                val alias = this.fixAlias(container, originalAlias)
                if (aliasesWithPrefix.contains(alias)) {
                    this.logger
                            .debug("Plugin '${container.id}' is attempting to register duplicate alias '$alias'")
                    continue
                }
                val ownedCommands = this.owners.get(container)
                for (mapping in this.dispatcher.getAll(alias)) {
                    require(!ownedCommands.contains(mapping)) { "A plugin may not register multiple commands for the same alias ('$alias')!" }
                }

                aliasesWithPrefix.add(alias)
                aliasesWithPrefix.add(container.id + ':' + alias)
            }

            val mapping = this.dispatcher.register(callable, aliasesWithPrefix, callback)
                    ?: return null
            this.owners.put(container, mapping)
            this.reverseOwners[mapping] = container

            return mapping
        }
    }

    private fun fixAlias(pluginContainer: PluginContainer, original: String): String {
        var fixed = original.toLowerCase(Locale.ENGLISH)
        val caseChanged = original != fixed
        val spaceFound = original.indexOf(' ') > -1
        if (spaceFound) {
            fixed = SPACE_PATTERN.matcher(fixed).replaceAll("")
        }
        if (caseChanged || spaceFound) {
            val description =
                    buildAliasDescription(
                            caseChanged,
                            spaceFound
                    )
            this.logger.warn(
                    "Plugin '${pluginContainer.id}' is attempting to register command '$original' with $description - adjusting to '$fixed'"
            )
        }
        return fixed
    }

    override fun removeMapping(mapping: CommandMapping): Optional<CommandMapping> {
        synchronized(this.lock) {
            val removed = this.dispatcher.removeMapping(mapping)

            if (removed.isPresent) {
                forgetMapping(removed.get())
            }

            return removed
        }
    }

    private fun forgetMapping(mapping: CommandMapping) {
        val it = this.owners.values().iterator()
        while (it.hasNext()) {
            if (it.next() == mapping) {
                it.remove()
                break
            }
        }
    }

    override fun getOwnedBy(instance: Any): Set<CommandMapping> {
        val container = CommandSystem.pluginManager.fromInstance(instance)
        require(container != null) { "The provided plugin object does not have an associated plugin container " + "(in other words, is 'plugin' actually your plugin object?)" }

        synchronized(this.lock) {
            return ImmutableSet.copyOf(this.owners.get(container))
        }
    }

    fun getOwner(mapping: CommandMapping): PluginContainer? {
        return this.reverseOwners[checkNotNull(mapping, "mapping")]
    }

    override fun get(alias: String): CommandMapping? {
        return this.dispatcher[alias]
    }

    override fun get(
            alias: String,
            source: CommandSource?
    ): CommandMapping? {
        return this.dispatcher[alias, source]
    }

    override fun getAll(alias: String): Set<CommandMapping> {
        return this.dispatcher.getAll(alias)
    }

    override fun containsAlias(alias: String): Boolean {
        return this.dispatcher.containsAlias(alias)
    }

    override fun containsMapping(mapping: CommandMapping): Boolean {
        return this.dispatcher.containsMapping(mapping)
    }

    override fun process(
            source: CommandSource,
            arguments: String
    ): com.github.mikucat0309.commandsystem.command.CommandResult {
        val argSplit = arguments.split(" ".toRegex(), 2).toTypedArray()

        try {
            try {
                dispatcher.process(source, arguments)
            } catch (ex: com.github.mikucat0309.commandsystem.command.InvocationCommandException) {
                ex.cause?.let { throw ex.cause }
            } catch (ex: com.github.mikucat0309.commandsystem.command.CommandPermissionException) {
                ex.message?.let { source.sendMessage(error(it)) }
            } catch (ex: com.github.mikucat0309.commandsystem.command.CommandException) {
                ex.message?.let { source.sendMessage(error(it)) }

                if (ex.shouldIncludeUsage()) {
                    val mapping = this.dispatcher[argSplit[0], source]
                    if (mapping != null) {
                        val usage: String =
                                if (ex is ArgumentParseException.WithUsage) {
                                    ex.usage
                                } else {
                                    mapping.callable.getUsage(source)
                                }

                        source.sendMessage(error("Usage: /${argSplit[0]} $usage"))
                    }
                }
            }
            // Since we know we are in the main thread, this is safe to do without a thread check
        } catch (thr: Throwable) {
            val excBuilder: StringBuilder
            excBuilder = if (thr is com.github.mikucat0309.commandsystem.command.TextMessageException) {
                val text = thr.text
                if (text == null) StringBuilder("null") else StringBuilder()
            } else {
                StringBuilder(thr.message.toString())
            }

            val writer = StringWriter()
            thr.printStackTrace(PrintWriter(writer))
            excBuilder.append('\n').append(
                    writer.toString()
                            .replace("\t", "    ")
                            .replace("\r\n", "\n")
                            .replace("\r", "\n")
            ) // I mean I guess somebody could be running this on like OS 9?
            source.sendMessage(error("Error occurred while executing command: $excBuilder"))
            this.logger
                    .error("Error occurred while executing command '$arguments' for source $source: ${thr.message}", thr)
        }

        return com.github.mikucat0309.commandsystem.command.CommandResult.empty()
    }

    override fun getSuggestions(source: CommandSource, arguments: String): List<String> {
        return try {
            ArrayList(dispatcher.getSuggestions(source, arguments))
        } catch (e: CommandException) {
            source.sendMessage(error("Error getting suggestions: " + e.message))
            emptyList()
        } catch (e: Exception) {
            throw RuntimeException("Error occured while tab completing '$arguments'", e)
        }
    }

    override fun getShortDescription(source: CommandSource): String? {
        return this.dispatcher.getShortDescription(source)
    }

    override fun getHelp(source: CommandSource): String? {
        return this.dispatcher.getHelp(source)
    }

    override fun getUsage(source: CommandSource): String {
        return this.dispatcher.getUsage(source)
    }

    override fun size(): Int {
        return this.dispatcher.size()
    }

    companion object {

        private val SPACE_PATTERN = Pattern.compile(" ", Pattern.LITERAL)

        private fun buildAliasDescription(caseChanged: Boolean, spaceFound: Boolean): String {
            var description = if (caseChanged) "an uppercase character" else ""
            if (spaceFound) {
                if (description.isNotEmpty()) {
                    description += " and "
                }
                description += "a space"
            }
            return description
        }
    }

}
