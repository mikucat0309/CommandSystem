package com.github.mikucat0309.commandsystem.command.spec

import com.github.mikucat0309.commandsystem.command.CommandCallable
import com.github.mikucat0309.commandsystem.command.CommandException
import com.github.mikucat0309.commandsystem.command.CommandResult
import com.github.mikucat0309.commandsystem.command.CommandSource
import com.github.mikucat0309.commandsystem.command.args.*
import com.github.mikucat0309.commandsystem.command.args.parsing.InputTokenizer
import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ImmutableList
import java.util.*

/**
 * Specification for how command arguments should be parsed.
 */
class CommandSpec internal constructor(
    private val args: CommandElement,
    /**
     * Gets the active executor for this command. Generally not a good idea to call this directly,
     * unless you are handling arg parsing specially
     *
     * @return The active executor for this command
     */
    val executor: CommandExecutor,
    description: String?,
    extendedDescription: String?,
    private val permission: String?,
    /**
     * Gets the active input tokenizer used for this command.
     *
     * @return This command's input tokenizer
     */
    val inputTokenizer: InputTokenizer
) : CommandCallable {
    private val description: String?
    private val extendedDescription: String?

    init {
        this.description = description
        this.extendedDescription = extendedDescription
    }

    /**
     * Process this command with existing arguments and context objects.
     *
     * @param source  The source to populate the context with
     * @param args    The arguments to process with
     * @param context The context to put data in
     * @throws ArgumentParseException if an invalid argument is provided
     */
    @Throws(ArgumentParseException::class)
    fun populateContext(
        source: CommandSource,
        args: CommandArgs,
        context: CommandContext
    ) {
        this.args.parse(source, args, context)
        if (args.hasNext()) {
            args.next()
            throw args.createError("Too many arguments!")
        }
    }

    /**
     * Return tab completion results using the existing parsed arguments and context. Primarily useful
     * when including a subcommand in an existing specification.
     *
     * @param source  The source to parse arguments for
     * @param args    The arguments object
     * @param context The context object
     * @return possible completions, or an empty list if none
     */
    fun complete(
        source: CommandSource,
        args: CommandArgs,
        context: CommandContext
    ): List<String> {
        checkNotNull(source, "source")
        val ret = this.args.complete(source, args, context)
        return ImmutableList.copyOf(ret)
    }

    @Throws(CommandException::class)
    override fun process(
        source: CommandSource,
        arguments: String
    ): CommandResult {
        val args = CommandArgs(
            arguments,
            inputTokenizer.tokenize(arguments, false)
        )
        val context = CommandContext()
        this.populateContext(source, args, context)
        return executor.execute(source, context)
    }

    @Throws(CommandException::class)
    override fun getSuggestions(source: CommandSource, arguments: String): List<String> {
        val args = CommandArgs(arguments, inputTokenizer.tokenize(arguments, true))
        val ctx = CommandContext()
        ctx.putArg(CommandContext.TAB_COMPLETION, true)
        return complete(source, args, ctx)
    }

    /**
     * Gets a short, one-line description used with this command if any is present.
     *
     * @return the short description.
     */
    override fun getShortDescription(source: CommandSource): String? {
        return this.description
    }

    /**
     * Gets the extended description used with this command if any is present.
     *
     * @param source The source to get the description for
     * @return the extended description.
     */
    fun getExtendedDescription(source: CommandSource): String? {
        return this.extendedDescription
    }

    /**
     * Gets the usage for this command appropriate for the provided command source.
     *
     * @param source The source
     * @return the usage for the source
     */
    override fun getUsage(source: CommandSource): String {
        checkNotNull(source, "source")
        return this.args.getUsage(source)
    }

    /**
     * Return a longer description for this command. This description is composed of at least all
     * present of the short description, the usage statement, and the extended description
     *
     * @param source The source to get the extended description for
     * @return the extended description
     */
    override fun getHelp(source: CommandSource): String? {
        checkNotNull(source, "source")
        val builder = StringBuilder()
        this.getShortDescription(source)?.let { builder.append(it).append('\n') }

        builder.append(getUsage(source))
        this.getExtendedDescription(source).let { builder.append(it).append('\n') }
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as CommandSpec?
        return (Objects.equal(this.args, that!!.args)
                && Objects.equal(this.executor, that.executor)
                && Objects.equal(this.description, that.description)
                && Objects.equal(this.extendedDescription, that.extendedDescription)
                && Objects.equal(this.permission, that.permission)
                && Objects.equal(this.inputTokenizer, that.inputTokenizer))
    }

    override fun hashCode(): Int {
        return Objects.hashCode(
            this.args, this.executor, this.description, this.extendedDescription,
            this.permission, this.inputTokenizer
        )
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("args", this.args)
            .add("executor", this.executor)
            .add("description", this.description)
            .add("extendedDescription", this.extendedDescription)
            .add("permission", this.permission)
            .add("argumentParser", this.inputTokenizer)
            .toString()
    }

    /**
     * Builder for command specs.
     */
    class Builder internal constructor() {
        private var args = DEFAULT_ARG
        private var description: String? = null
        private var extendedDescription: String? = null
        private var permission: String? = null
        private var executor: CommandExecutor? = null
        private var childCommandMap: MutableMap<List<String>, CommandCallable>? = null
        private var childCommandFallback = true
        private var argumentParser = InputTokenizer.quotedStrings(false)

        /**
         * Sets the permission that will be checked before using this command.
         *
         * @param permission The permission to check
         * @return this
         */
        fun permission(permission: String): Builder {
            this.permission = permission
            return this
        }

        /**
         * Sets the callback that will handle this command's execution.
         *
         * @param executor The executor that will be called with this command's parsed arguments
         * @return this
         */
        fun executor(executor: CommandExecutor): Builder {
            checkNotNull(executor, "executor")
            this.executor = executor
            return this
        }

        /**
         * Adds more child arguments for this command. If an executor or arguments are set, they are
         * used as fallbacks.
         *
         * @param children The children to use
         * @return this
         */
        fun children(children: Map<List<String>, CommandCallable>): Builder {
            if (this.childCommandMap == null) {
                this.childCommandMap = HashMap()
            }
            this.childCommandMap!!.putAll(children)
            return this
        }

        /**
         * Add a single child command to this command.
         *
         * @param child   The child to add
         * @param aliases Aliases to make the child available under. First one is primary and is the
         * only one guaranteed to be listed in usage outputs.
         * @return this
         */
        fun child(
            child: CommandCallable,
            vararg aliases: String
        ): Builder {
            if (this.childCommandMap == null) {
                this.childCommandMap = HashMap()
            }
            this.childCommandMap!![ImmutableList.copyOf(aliases)] = child
            return this
        }

        /**
         * Add a single child command to this command.
         *
         * @param child   The child to add.
         * @param aliases Aliases to make the child available under. First one is primary and is the
         * only one guaranteed to be listed in usage outputs.
         * @return this
         */
        fun child(
            child: CommandCallable,
            aliases: Collection<String>
        ): Builder {
            if (this.childCommandMap == null) {
                this.childCommandMap = HashMap()
            }
            this.childCommandMap!![ImmutableList.copyOf(aliases)] = child
            return this
        }

        /**
         * A short, one-line description of this command's purpose.
         *
         * @param description The description to set
         * @return this
         */
        fun description(description: String?): Builder {
            this.description = description
            return this
        }

        /**
         * Sets an extended description to use in longer help listings for this command. Will be
         * appended to the short description and the command's usage.
         *
         * @param extendedDescription The description to set
         * @return this
         */
        fun extendedDescription(extendedDescription: String?): Builder {
            this.extendedDescription = extendedDescription
            return this
        }

        /**
         * If a child command is selected but fails to parse arguments passed to it, the following
         * determines the behavior.
         *
         *
         *  * If this is set to **false**, this command (the
         * parent) will not attempt to parse the command, and will send back
         * the error from the child.
         *  * If this is set to **true**, the error from the
         * child will simply be discarded, and the parent command will
         * execute.
         *
         *
         *
         * The default for this is **true**, which emulates the
         * behavior from previous API revisions.
         *
         * @param childCommandFallback Whether to fallback on argument parse failure
         * @return this
         */
        fun childArgumentParseExceptionFallback(childCommandFallback: Boolean): Builder {
            this.childCommandFallback = childCommandFallback
            return this
        }

        /**
         * Sets the argument specification for this command. Generally, for a multi-argument command the
         * [GenericArguments.seq] method is used to parse a sequence of args.
         *
         * @param args The arguments object to use
         * @return this
         * @see GenericArguments
         */
        fun arguments(args: CommandElement): Builder {
            this.args = GenericArguments.seq(args)
            return this
        }

        /**
         * Sets the argument specification for this command. This method accepts a sequence of
         * arguments. This is equivalent to calling `arguments(seq(args))`.
         *
         * @param args The arguments object to use
         * @return this
         * @see GenericArguments
         */
        fun arguments(vararg args: CommandElement): Builder {
            this.args = GenericArguments.seq(*args)
            return this
        }

        /**
         * Sets the input tokenizer to be used to convert input from a string into a list of argument
         * tokens.
         *
         * @param parser The parser to use
         * @return this
         * @see InputTokenizer for common input parser implementations
         */
        fun inputTokenizer(parser: InputTokenizer): Builder {
            this.argumentParser = parser
            return this
        }

        /**
         * Create a new [CommandSpec] based on the data provided in this builder.
         *
         * @return the new spec
         */
        fun build(): CommandSpec {
            if (this.childCommandMap == null || this.childCommandMap!!.isEmpty()) {
                checkNotNull(executor!!, "An executor is required")
            } else if (this.executor == null) {
                val childCommandElementExecutor = registerInDispatcher(
                    ChildCommandElementExecutor(
                        null,
                        null,
                        false
                    )
                )
                if (this.args === DEFAULT_ARG) {
                    arguments(childCommandElementExecutor)
                } else {
                    arguments(this.args, childCommandElementExecutor)
                }
            } else {
                arguments(
                    registerInDispatcher(
                        ChildCommandElementExecutor(
                            this.executor,
                            this.args,
                            this.childCommandFallback
                        )
                    )
                )
            }
            return CommandSpec(
                this.args, this.executor!!, this.description, this.extendedDescription,
                this.permission,
                this.argumentParser
            )
        }

        private fun registerInDispatcher(
            childDispatcher: ChildCommandElementExecutor
        ): ChildCommandElementExecutor {
            for ((key, value) in this.childCommandMap!!) {
                childDispatcher.register(value, key)
            }

            executor(childDispatcher)
            return childDispatcher
        }

        companion object {
            private val DEFAULT_ARG = GenericArguments.none()
        }
    }

    companion object {

        /**
         * Return a new builder for a CommandSpec.
         *
         * @return a new builder
         */
        fun builder(): Builder {
            return Builder()
        }
    }
}
