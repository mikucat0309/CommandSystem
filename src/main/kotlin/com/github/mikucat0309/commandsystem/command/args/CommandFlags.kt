package com.github.mikucat0309.commandsystem.command.args

import com.github.mikucat0309.commandsystem.command.CommandSource
import com.github.mikucat0309.commandsystem.command.args.GenericArguments.markTrue
import com.github.mikucat0309.commandsystem.command.util.startsWith
import com.google.common.collect.ImmutableList
import java.util.*
import kotlin.streams.toList

class CommandFlags private constructor(
    private val childElement: CommandElement?,
    private val usageFlags: Map<List<String>, CommandElement>,
    private val shortFlags: Map<String, CommandElement>,
    private val longFlags: Map<String, CommandElement>,
    private val unknownShortFlagBehavior: UnknownFlagBehavior,
    private val unknownLongFlagBehavior: UnknownFlagBehavior,
    private val anchorFlags: Boolean
) : CommandElement(null) {

    @Throws(ArgumentParseException::class)
    override fun parse(
        source: CommandSource,
        args: CommandArgs,
        context: CommandContext
    ) {
        val state = args.snapshot
        while (args.hasNext()) {
            val arg = args.next()
            if (arg.startsWith("-")) {
                val start = args.snapshot
                val remove: Boolean = if (arg.startsWith("--")) { // Long flag
                    parseLongFlag(source, arg.substring(2), args, context)
                } else {
                    parseShortFlags(source, arg.substring(1), args, context)
                }
                if (remove) {
                    args.removeArgs(start, args.snapshot)
                }
            } else if (this.anchorFlags) {
                break
            }
        }
        // We removed the arguments so we don't parse them as they have already been parsed as flags,
        // so don't restore them here!
        args.applySnapshot(state, false)
        if (this.childElement != null) {
            this.childElement.parse(source, args, context)
        }
    }

    @Throws(ArgumentParseException::class)
    private fun parseLongFlag(
        source: CommandSource,
        longFlag: String,
        args: CommandArgs,
        context: CommandContext
    ): Boolean {
        val flagSplit = longFlag.split("=".toRegex(), 2).toTypedArray()
        val flag = flagSplit[0].toLowerCase()
        val element = this.longFlags[flag]
        if (element == null) {
            return when (this.unknownLongFlagBehavior) {
                UnknownFlagBehavior.ERROR -> throw args.createError("Unknown long flag " + flagSplit[0] + " specified")
                UnknownFlagBehavior.ACCEPT_NONVALUE -> {
                    context.putArg(flag, if (flagSplit.size == 2) flagSplit[1] else true)
                    true
                }
                UnknownFlagBehavior.ACCEPT_VALUE -> {
                    context.putArg(flag, if (flagSplit.size == 2) flagSplit[1] else args.next())
                    true
                }
                UnknownFlagBehavior.IGNORE -> false
            }
        } else if (flagSplit.size == 2) {
            args.insertArg(flagSplit[1])
        }
        element.parse(source, args, context)
        return true
    }

    @Throws(ArgumentParseException::class)
    private fun parseShortFlags(
        source: CommandSource,
        shortFlags: String,
        args: CommandArgs,
        context: CommandContext
    ): Boolean {
        for (i in shortFlags.indices) {
            val shortFlag = shortFlags.substring(i, i + 1)
            val element = this.shortFlags[shortFlag]
            if (element == null) {
                when (this.unknownShortFlagBehavior) {
                    UnknownFlagBehavior.IGNORE -> {
                        if (i == 0) {
                            return false
                        }
                        throw args.createError("Unknown short flag $shortFlag specified")
                    }
                    UnknownFlagBehavior.ERROR -> throw args.createError(
                        "Unknown short flag $shortFlag specified"
                    )
                    UnknownFlagBehavior.ACCEPT_NONVALUE -> context.putArg(
                        shortFlag,
                        true
                    )
                    UnknownFlagBehavior.ACCEPT_VALUE -> context.putArg(
                        shortFlag,
                        args.next()
                    )
                }
            } else {
                element.parse(source, args, context)
            }
        }
        return true
    }

    override fun getUsage(src: CommandSource): String {
        val builder = ArrayList<String>()
        for ((key, value) in this.usageFlags) {
            builder.add("[")
            val it = key.iterator()
            while (it.hasNext()) {
                val flag = it.next()
                builder.add(if (flag.length > 1) "--" else "-")
                builder.add(flag)
                if (it.hasNext()) {
                    builder.add("|")
                }
            }
            val usage = value.getUsage(src)
            if (usage.trim { it <= ' ' }.isNotEmpty()) {
                builder.add(" ")
                builder.add(usage)
            }
            builder.add("]")
            builder.add(" ")
        }

        if (this.childElement != null) {
            builder.add(this.childElement.getUsage(src))
        }
        return builder.toString()
    }

    @Throws(ArgumentParseException::class)
    override fun parseValue(
        source: CommandSource,
        args: CommandArgs
    ): Any? {
        return null
    }

    override fun complete(
        src: CommandSource,
        args: CommandArgs,
        context: CommandContext
    ): List<String> {
        val state = args.snapshot
        while (args.hasNext()) {
            val next = args.nextIfPresent().get()
            if (next.startsWith("-")) {
                val start = args.snapshot
                val ret: List<String>? = if (next.startsWith("--")) {
                    tabCompleteLongFlag(next.substring(2), src, args, context)
                } else {
                    tabCompleteShortFlags(next.substring(1), src, args, context)
                }
                if (ret != null) {
                    return ret
                }
                args.removeArgs(start, args.snapshot)
            } else if (this.anchorFlags) {
                break
            }
        }
        // the modifications are intentional
        args.applySnapshot(state, false)

        // Prevent tab completion gobbling up an argument if the value parsed.
        if (!args.hasNext() && !args.raw.matches("\\s+$".toRegex())) {
            return ImmutableList.of()
        }
        return if (this.childElement != null)
            childElement.complete(src, args, context)
        else
            ImmutableList.of()
    }

    private fun tabCompleteLongFlag(
        longFlag: String,
        src: CommandSource,
        args: CommandArgs,
        context: CommandContext
    ): List<String>? {
        val flagSplit = longFlag.split("=".toRegex(), 2).toTypedArray()
        val isSplitFlag = flagSplit.size == 2
        val element = this.longFlags[flagSplit[0].toLowerCase()]
        if (element == null || !isSplitFlag && !args.hasNext()) {
            return this.longFlags.keys.stream()
                .filter { startsWith(it, flagSplit[0]) }
                .map { f -> "--$f" }
                .toList()
        } else if (isSplitFlag) {
            args.insertArg(flagSplit[1])
        }
        val state = args.snapshot
        var completion: List<String>
        try {
            element.parse(src, args, context)
            if (args.snapshot == state) {
                // Not iterated, but succeeded. Check completions to account for optionals
                completion = element.complete(src, args, context)
            } else {
                args.previous()
                val res = args.peek()
                completion = element.complete(src, args, context)
                if (!completion.contains(res)) {
                    completion = ImmutableList.builder<String>().addAll(completion).add(res).build()
                }
            }
        } catch (ex: ArgumentParseException) {
            args.applySnapshot(state)
            completion = element.complete(src, args, context)
        }

        if (completion.isEmpty()) {
            return if (isSplitFlag) {
                ImmutableList.of() // so we don't overwrite the flag
            } else null
        }

        return if (isSplitFlag) {
            completion.stream().map { x -> "--" + flagSplit[0] + "=" + x }
                .toList()
        } else completion
    }

    private fun tabCompleteShortFlags(
        shortFlags: String,
        src: CommandSource,
        args: CommandArgs,
        context: CommandContext
    ): List<String>? {
        for (i in shortFlags.indices) {
            val element = this.shortFlags[shortFlags.substring(i, i + 1)]
            if (element == null) {
                if (i == 0 && this.unknownShortFlagBehavior == UnknownFlagBehavior.ACCEPT_VALUE) {
                    args.nextIfPresent()
                    return null
                }
            } else {
                val start = args.snapshot
                try {
                    element.parse(src, args, context)

                    // if the iterator hasn't moved, then just try to complete, no point going backwards.
                    if (args.snapshot == start) {
                        return element.complete(src, args, context)
                    }

                    // if we managed to parse this, then go back to get the completions for it.
                    args.previous()
                    val currentText = args.peek()

                    // ensure this is returned as a valid option
                    val elements = element.complete(src, args, context)
                    return if (!elements.contains(currentText)) {
                        ImmutableList.builder<String>().add(args.peek())
                            .addAll(element.complete(src, args, context)).build()
                    } else {
                        elements
                    }
                } catch (ex: ArgumentParseException) {
                    args.applySnapshot(start)
                    return element.complete(src, args, context)
                }

            }
        }
        return null
    }

    /**
     * Indicates to the flag parser how it should treat an argument that looks like a flag that it
     * does not recognise.
     */
    enum class UnknownFlagBehavior {
        /**
         * Throw an [ArgumentParseException] when an unknown flag is encountered.
         */
        ERROR,
        /**
         * Mark the flag as a non-value flag.
         */
        ACCEPT_NONVALUE,

        /**
         * Mark the flag as a string-valued flag.
         */
        ACCEPT_VALUE,
        /**
         * Act as if the unknown flag is an ordinary argument, allowing the parsers specified in [ ][Builder.buildWith] to attempt to parse the element instead.
         */
        IGNORE

    }

    class Builder internal constructor() {
        private val usageFlags = HashMap<List<String>, CommandElement>()
        private val shortFlags = HashMap<String, CommandElement>()
        private val longFlags = HashMap<String, CommandElement>()
        private var unknownLongFlagBehavior = UnknownFlagBehavior.ERROR
        private var unknownShortFlagBehavior =
            UnknownFlagBehavior.ERROR
        private var anchorFlags = false

        private fun flag(
            func: (String) -> CommandElement,
            vararg specs: String
        ): Builder {
            val availableFlags = ArrayList<String>(specs.size)
            var el: CommandElement? = null
            for (spec in specs) {
                if (spec.startsWith("-")) {
                    val flagKey = spec.substring(1)
                    if (el == null) {
                        el = func.invoke(flagKey)
                    }
                    availableFlags.add(flagKey)
                    this.longFlags[flagKey.toLowerCase()] = el
                } else {
                    for (i in spec.indices) {
                        val flagKey = spec.substring(i, i + 1)
                        if (el == null) {
                            el = func.invoke(flagKey)
                        }
                        availableFlags.add(flagKey)
                        this.shortFlags[flagKey] = el
                    }
                }
            }
            if (el != null) {
                this.usageFlags[availableFlags] = el
            }
            return this
        }

        /**
         * Allow a flag with any of the provided specifications that has no value. This flag will be
         * exposed in a [CommandContext] under the key equivalent to the first flag in the
         * specification array. The specifications are handled as so for each element in the `specs` array:
         *
         *  * If the element starts with -, the remainder of the element
         * is interpreted as a long flag (so, "-flag" means "--flag" will
         * be matched in an argument string)
         *  * Otherwise, each code point of the element is interpreted
         * as a short flag (meaning "flag" will cause "-f", "-l", "-a" and
         * "-g" to be matched in an argument string, storing "true" under
         * the key "f".)
         *
         *
         * @param specs The flag specifications
         * @return this
         */
        fun flag(vararg specs: String): Builder {
            return flag({ markTrue(it) }, *specs)
        }

        /**
         * Allow a flag with any of the provided specifications, with the given command element. The
         * flag may be present multiple times, and may therefore have multiple values.
         *
         * @param value The command element used to parse any occurrences
         * @param specs The flag specifications
         * @return this
         * @see .flag
         */
        fun valueFlag(
            value: CommandElement,
            vararg specs: String
        ): Builder {
            return flag({ value }, *specs)
        }

        /**
         * If this is true, any long flag (--) will be accepted and added as a flag. If false, unknown
         * long flags are considered errors.
         *
         * @param acceptsArbitraryLongFlags Whether any long flag is accepted
         * @return this
         */
        @Deprecated("in favor of {@link #setUnknownLongFlagBehavior(UnknownFlagBehavior)}.")
        fun setAcceptsArbitraryLongFlags(acceptsArbitraryLongFlags: Boolean): Builder {
            setUnknownLongFlagBehavior(
                if (acceptsArbitraryLongFlags)
                    UnknownFlagBehavior.ACCEPT_NONVALUE
                else
                    UnknownFlagBehavior.ERROR
            )
            return this
        }

        /**
         * Sets how long flags that are not registered should be handled when encountered.
         *
         * @param behavior The behavior to use
         * @return this
         */
        fun setUnknownLongFlagBehavior(behavior: UnknownFlagBehavior): Builder {
            this.unknownLongFlagBehavior = behavior
            return this
        }

        /**
         * Sets how long flags that are not registered should be handled when encountered.
         *
         *
         * If a command that supports flags accepts negative numbers (or
         * arguments that may begin with a dash), setting this to [UnknownFlagBehavior.IGNORE]
         * will cause these elements to be ignored by the flag parser and will be parsed by the
         * command's non-flag elements instead.
         *
         * @param behavior The behavior to use
         * @return this
         */
        fun setUnknownShortFlagBehavior(behavior: UnknownFlagBehavior): Builder {
            this.unknownShortFlagBehavior = behavior
            return this
        }

        /**
         * Whether flags should be anchored to the beginning of the text (so flags will only be picked
         * up if they are at the beginning of the input).
         *
         * @param anchorFlags Whether flags are anchored
         * @return this
         */
        fun setAnchorFlags(anchorFlags: Boolean): Builder {
            this.anchorFlags = anchorFlags
            return this
        }

        /**
         * Build a flag command element using the given command element to handle all non-flag
         * arguments.
         *
         *
         * If you wish to add multiple elements here, wrap them in
         * [GenericArguments.seq]
         *
         * @param wrapped The wrapped command element
         * @return the new command element
         */
        fun buildWith(wrapped: CommandElement): CommandElement {
            return CommandFlags(
                wrapped, this.usageFlags, this.shortFlags, this.longFlags,
                this.unknownShortFlagBehavior,
                this.unknownLongFlagBehavior, this.anchorFlags
            )
        }
    }
}
