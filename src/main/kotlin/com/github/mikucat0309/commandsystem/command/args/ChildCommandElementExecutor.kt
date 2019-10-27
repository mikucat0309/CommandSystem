package com.github.mikucat0309.commandsystem.command.args

import com.github.mikucat0309.commandsystem.command.*
import com.github.mikucat0309.commandsystem.command.CommandMessageFormatting.error
import com.github.mikucat0309.commandsystem.command.dispatcher.SimpleDispatcher
import com.github.mikucat0309.commandsystem.command.spec.CommandExecutor
import com.github.mikucat0309.commandsystem.command.spec.CommandSpec
import com.github.mikucat0309.commandsystem.command.util.startsWith
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import java.util.concurrent.atomic.AtomicInteger

class ChildCommandElementExecutor
/**
 * Create a new combined argument element and executor to handle the parsing and execution of
 * child commands.
 *
 * @param fallbackExecutor The executor to execute if the child command has been marked optional
 * (Generally when this is wrapped in a [GenericArguments.optional]
 * @param fallbackElements The alternate [CommandElement]s that should be parsed if a child
 * element fails to be parsed
 * @param fallbackOnFail   If true, then if a child command cannot parse the elements, the
 * exception is discarded and the parent command attempts to parse the
 * elements. If false, a child command will not pass control back to the
 * parent, displaying its own exception message
 */
(
        private val fallbackExecutor: CommandExecutor?,
        fallbackElements: CommandElement?,
        private val fallbackOnFail: Boolean
) : CommandElement("child" + COUNTER.getAndIncrement()),
        CommandExecutor {
    private val fallbackElements: CommandElement?
    private val dispatcher = SimpleDispatcher(
            SimpleDispatcher.FIRST_DISAMBIGUATOR
    )

    /**
     * Create a new combined argument element and executor to handle the parsing and execution of
     * child commands.
     *
     * @param fallbackExecutor The executor to execute if the child command has been marked optional
     * (Generally when this is wrapped in a [GenericArguments.optional]
     */
    @Deprecated(
            "Use the other constructor instead. Note: this entire system will be replaced in API\n" +
                    "    8."
    )
    constructor(fallbackExecutor: CommandExecutor?) : this(
            fallbackExecutor,
            null,
            true
    )

    init {
        this.fallbackElements =
                if (NONE === fallbackElements) null else fallbackElements
    }

    /**
     * Register a child command for a given set of aliases.
     *
     * @param callable The command to register
     * @param aliases  The aliases to register it as
     * @return The child command's mapping, if present
     */
    fun register(
            callable: CommandCallable,
            aliases: List<String>
    ): CommandMapping? {
        return this.dispatcher.register(callable, aliases)
    }

    /**
     * Register a child command for a given set of aliases.
     *
     * @param callable The command to register
     * @param aliases  The aliases to register it as
     * @return The child command's mapping, if present
     */
    fun register(
            callable: CommandCallable,
            vararg aliases: String
    ): CommandMapping? {
        return this.dispatcher.register(callable, *aliases)
    }

    override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
    ): List<String> {
        val completions = Lists.newArrayList<String>()
        if (this.fallbackElements != null) {
            val state = args.snapshot
            completions.addAll(this.fallbackElements.complete(src, args, context))
            args.applySnapshot(state)
        }

        val commandComponent = args.nextIfPresent()
        if (!commandComponent.isPresent) {
            return ImmutableList.copyOf(filterCommands(src))
        }
        if (args.hasNext()) {
            val child = this.dispatcher[commandComponent.get(), src] ?: return ImmutableList.of()
            if (child.callable is CommandSpec) {
                return (child.callable as CommandSpec).complete(
                        src,
                        args,
                        context
                )
            }
            args.nextIfPresent()
            val arguments = args.raw.substring(args.rawPosition)
            while (args.hasNext()) {
                args.nextIfPresent()
            }
            return try {
                child.callable.getSuggestions(src, arguments)
            } catch (e: CommandException) {
                val eText = e.message
                if (eText != null) {
                    src.sendMessage(error(eText))
                }
                ImmutableList.of()
            }
        }

        completions.addAll(filterCommands(src).filter { startsWith(it, commandComponent.get()) })
        return completions
    }

    private fun filterCommands(src: CommandSource): Set<String> {
        return this.dispatcher.getAll().entries().filter { it.value != null }.map { it.key }.toSet()
    }

    @Throws(ArgumentParseException::class)
    override fun parse(
            source: CommandSource,
            args: CommandArgs,
            context: CommandContext
    ) {
        if (this.fallbackExecutor != null && !args.hasNext()) {
            if (this.fallbackElements != null) {
                // there might be optionals to take account of that would parse this successfully.
                this.fallbackElements.parse(source, args, context)
            }

            return  // execute the fallback regardless in this scenario.
        }

        val state = args.snapshot
        val key = args.next()
        val optionalCommandMapping = this.dispatcher[key, source]
        if (optionalCommandMapping != null) {
            try {
                if (optionalCommandMapping.callable is CommandSpec) {
                    val spec = optionalCommandMapping.callable as CommandSpec
                    spec.populateContext(source, args, context)
                } else {
                    if (args.hasNext()) {
                        args.next()
                    }

                    context.putArg(key + "_args", args.raw.substring(args.rawPosition))
                    while (args.hasNext()) {
                        args.next()
                    }
                }

                // Success, add to context now so that we don't execute the wrong executor in the first place.
                context.putArg(key, optionalCommandMapping)
            } catch (ex: ArgumentParseException) {
                // If we get here, fallback to the elements, if they exist.
                args.applySnapshot(state)
                if (this.fallbackOnFail && this.fallbackElements != null) {
                    this.fallbackElements.parse(source, args, context)
                    return
                }

                // Get the usage
                args.next()
                if (ex is ArgumentParseException.WithUsage) {
                    // This indicates a previous child failed, so we just prepend our child
                    throw ArgumentParseException.WithUsage(
                            ex,
                            key + " " + ex.usage
                    )
                }

                throw ArgumentParseException.WithUsage(
                        ex,
                        key + " " + optionalCommandMapping.callable.getUsage(source)
                )
            }

        } else {
            // Not a child, so let's continue with the fallback.
            if (this.fallbackExecutor != null && this.fallbackElements != null) {
                args.applySnapshot(state)
                this.fallbackElements.parse(source, args, context)
            } else {
                // If we have no elements to parse, then we throw this error - this is the only element
                // so specifying it implicitly means we have a child command to execute.
                throw args.createError("Input command " + key + "was not a valid subcommand!")
            }
        }
    }

    @Throws(ArgumentParseException::class)
    override fun parseValue(
            source: CommandSource,
            args: CommandArgs
    ): Any? {
        return null
    }

    @Throws(CommandException::class)
    override fun execute(
            src: CommandSource,
            args: CommandContext
    ): CommandResult {
        val mapping = key?.let { args.getOne<CommandMapping>(it) }
        if (mapping == null) {
            if (this.fallbackExecutor == null) {
                throw CommandException(
                        "Invalid subcommand state -- no more than one mapping may be provided for child arg " + key!!
                )
            }
            return this.fallbackExecutor.execute(src, args)
        }
        if (mapping.callable is CommandSpec) {
            val spec = mapping.callable as CommandSpec
            return spec.executor.execute(src, args)
        }
        val arguments = args.getOne<String>(key!! + "_args") ?: ""
        return mapping.callable.process(src, arguments)
    }

    override fun getUsage(src: CommandSource): String {
        val usage = this.dispatcher.getUsage(src)
        if (this.fallbackElements == null) {
            return usage
        }

        val elementUsage = this.fallbackElements.getUsage(src)
        return if (elementUsage.isEmpty()) {
            usage
        } else usage + CommandMessageFormatting.PIPE_TEXT + elementUsage

    }

    companion object {
        private val COUNTER = AtomicInteger()
        private val NONE = GenericArguments.none()
    }
}


