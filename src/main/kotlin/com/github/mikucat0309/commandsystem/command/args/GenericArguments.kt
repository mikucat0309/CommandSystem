package com.github.mikucat0309.commandsystem.command.args

import com.github.mikucat0309.commandsystem.command.CommandMessageFormatting
import com.github.mikucat0309.commandsystem.command.CommandSource
import com.github.mikucat0309.commandsystem.command.util.startsWith
import com.github.mikucat0309.commandsystem.misc.Tristate
import com.google.common.base.Joiner
import com.google.common.collect.*
import java.math.BigDecimal
import java.math.BigInteger
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.streams.toList


/**
 * Class containing factory methods for common command elements.
 */
object GenericArguments {

    private val NONE = SequenceCommandElement(ImmutableList.of())
    private val BOOLEAN_CHOICES = ImmutableMap.builder<String, Boolean>()
        .put("true", true)
        .put("t", true)
        .put("T", true)
        .put("y", true)
        .put("Y", true)
        .put("yes", true)
        .put("verymuchso", true)
        .put("1", true)
        .put("false", false)
        .put("f", false)
        .put("F", false)
        .put("n", false)
        .put("N", false)
        .put("no", false)
        .put("notatall", false)
        .put("0", false)
        .build()

    /**
     * Expects no arguments, returns no values.
     *
     * @return An expectation of no arguments
     */
    fun none(): CommandElement {
        return NONE
    }

    /**
     * Expects no arguments. Adds 'true' to the context when parsed.
     *
     *
     * This will return only one value.
     *
     * @param key the key to store 'true' under
     * @return the argument
     */
    fun markTrue(key: String): CommandElement {
        return MarkTrueCommandElement(key)
    }

    /**
     * Expect an argument to represent a [Vector].
     *
     *
     * This will return one value.
     *
     * @param key The key to store under
     * @return the argument
     */
    fun vector(key: String): CommandElement {
        return VectorCommandElement(key)
    }

    /**
     * Gets a builder to create a command element that parses flags.
     *
     *
     * There should only be ONE of these in a command element sequence if you
     * wish to use flags. A [CommandFlags.Builder] can handle multiple flags that have different
     * behaviors. Using multiple builders in the same sequence may cause unexpected behavior.
     *
     *
     * Any command elements that are not associated with flags should be
     * placed into the [CommandFlags.Builder.buildWith] parameter, allowing the
     * flags to be used throughout the argument string.
     *
     *
     * @return the newly created builder
     */
    fun flags(): CommandFlags.Builder {
        return CommandFlags.Builder()
    }

    /**
     * Consumes a series of arguments. Usage is the elements concatenated
     *
     * @param elements The series of arguments to expect
     * @return the element to match the input
     */
    fun seq(vararg elements: CommandElement): CommandElement {
        return SequenceCommandElement(ImmutableList.copyOf(elements))
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     *
     * If there are 5 or fewer choices available, the choices will be shown
     * in the command usage. Otherwise, the usage will only display only the key.
     *
     *
     * Choices are **not case sensitive**. If you require
     * case sensitivity, see [.choices]
     *
     *
     * To override this behavior, see
     * [.choices].
     *
     *
     * When parsing, only one choice may be selected, returning its
     * associated value.
     *
     * @param key     The key to store the resulting value under
     * @param choices The choices users can choose from
     * @return the element to match the input
     */
    fun choicesInsensitive(key: String, choices: Map<String, *>): CommandElement {
        return choices(
            key,
            choices,
            choices.size <= ChoicesCommandElement.CUTOFF,
            false
        )
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     *
     * Unless `choicesInUsage` is true, general command usage will only
     * display the provided key.
     *
     *
     * When parsing, only one choice may be selected, returning its
     * associated value.
     *
     * @param key            The key to store the resulting value under
     * @param choices        The choices users can choose from
     * @param choicesInUsage Whether to display the available choices, or simply the provided key, as
     * part of usage
     * @param caseSensitive  Whether the matches should be case sensitive
     * @return the element to match the input
     */
    @JvmOverloads
    fun choices(
        key: String,
        choices: Map<String, *>,
        choicesInUsage: Boolean = choices.size <= ChoicesCommandElement.CUTOFF,
        caseSensitive: Boolean = true
    ): CommandElement {
        if (!caseSensitive) {
            return choices(
                key,
                Supplier<Collection<String>> { choices.keys },
                Function<String, Any?> { selection -> choices[selection.toLowerCase()] },
                choicesInUsage
            )
        }
        val immChoices = ImmutableMap.copyOf<String, Any>(choices)
        return choices(
            key,
            Supplier<Collection<String>> { immChoices.keys },
            Function<String, Any?> { immChoices[it] },
            choicesInUsage
        )
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     *
     * If there are 5 or fewer choices available, the choices will be shown
     * in the command usage. Otherwise, the usage will only display only the key.
     *
     *
     * To override this behavior, see [.choices].
     *
     *
     *
     * Only one choice may be selected, returning its associated value.
     *
     * @param key    The key to store the resulting value under
     * @param keys   The function that will supply available keys
     * @param values The function that maps an element of `key` to a value and any other key to
     * `null`
     * @return the element to match the input
     */
    fun choices(
        key: String, keys: Supplier<Collection<String>>,
        values: Function<String, *>
    ): CommandElement {
        return ChoicesCommandElement(
            key,
            keys,
            values,
            Tristate.UNDEFINED
        )
    }

    /**
     * Return an argument that allows selecting from a limited set of values. Unless `choicesInUsage` is true, general command usage will only display the provided key.
     *
     *
     * Only one choice may be selected, returning its associated value.
     *
     * @param key            The key to store the resulting value under
     * @param keys           The function that will supply available keys
     * @param values         The function that maps an element of `key` to a value and any other
     * key to `null`
     * @param choicesInUsage Whether to display the available choices, or simply the provided key, as
     * part of usage
     * @return the element to match the input
     */
    fun choices(
        key: String, keys: Supplier<Collection<String>>,
        values: Function<String, *>, choicesInUsage: Boolean
    ): CommandElement {
        return ChoicesCommandElement(
            key, keys, values,
            if (choicesInUsage) Tristate.TRUE else Tristate.FALSE
        )
    }

    /**
     * Returns a command element that matches the first of the provided elements that parses tab
     * completion matches from all options.
     *
     * @param elements The elements to check against
     * @return The command element matching the first passing of the elements provided
     */
    fun firstParsing(vararg elements: CommandElement): CommandElement {
        return FirstParsingCommandElement(
            ImmutableList.copyOf(
                elements
            )
        )
    }

    /**
     * Make the provided command element optional.
     *
     *
     * This means the command element is not required. However, if the
     * element is provided with invalid format and there are no more args specified, any errors will
     * still be passed on.
     *
     * @param element The element to optionally require
     * @return the element to match the input
     */
    fun optional(element: CommandElement): CommandElement {
        return OptionalCommandElement(element, null, false)
    }

    /**
     * Make the provided command element optional.
     *
     *
     * This means the command element is not required. However, if the
     * element is provided with invalid format and there are no more args specified, any errors will
     * still be passed on. If the given element's key and `value` are not null and this element
     * is not provided the element's key will be set to the given value.
     *
     * @param element The element to optionally require
     * @param value   The default value to set
     * @return the element to match the input
     */
    fun optional(
        element: CommandElement,
        value: Any
    ): CommandElement {
        return OptionalCommandElement(element, value, false)
    }

    /**
     * Make the provided command element optional This means the command element is not required. If
     * the argument is provided but of invalid format, it will be skipped.
     *
     * @param element The element to optionally require
     * @return the element to match the input
     */
    fun optionalWeak(element: CommandElement): CommandElement {
        return OptionalCommandElement(element, null, true)
    }

    /**
     *
     * Make the provided command element optional.
     *
     *
     * This means the command element is not required.
     *
     *
     *  * If the argument is provided but of invalid format, it will be
     * skipped.
     *  * If the given element's key and `value` are not null and
     * this element is not provided the element's key will be set to the
     * given value.
     *
     *
     * @param element The element to optionally require
     * @param value   The default value to set
     * @return the element to match the input
     */
    fun optionalWeak(
        element: CommandElement,
        value: Any
    ): CommandElement {
        return OptionalCommandElement(element, value, true)
    }

    /**
     * Require a given command element to be provided a certain number of times.
     *
     *
     * Command values will be stored under their provided keys in the
     * <tt>CommandContext</tt>.
     *
     * @param element The element to repeat
     * @param times   The number of times to repeat the element.
     * @return the element to match the input
     */
    fun repeated(
        element: CommandElement,
        times: Int
    ): CommandElement {
        return RepeatedCommandElement(element, times)
    }

    /**
     * Require all remaining args to match as many instances of [CommandElement] as will fit.
     * Command element values will be stored under their provided keys in the CommandContext.
     *
     * @param element The element to repeat
     * @return the element to match the input
     */
    fun allOf(element: CommandElement): CommandElement {
        return AllOfCommandElement(element)
    }

    /**
     * Require an argument to be a string. Any provided argument will fit in under this argument.
     *
     *
     * Gives values of type [String]. This will return only one value.
     *
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    fun string(key: String): CommandElement {
        return StringElement(key)
    }

    /**
     * Require an argument to be an integer (base 10).
     *
     *
     * Gives values of type [Integer]. This will return only one value.
     *
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    fun integer(key: String): CommandElement {
        return NumericElement(key,
            Function { Integer.parseInt(it) },
            BiFunction<String, Int, Int> { s, radix -> Integer.parseInt(s, radix) },
            Function { input -> "Expected an integer, but input '$input' was not" })
    }

    /**
     * Require an argument to be a long (base 10).
     *
     *
     * Gives values of type [Long]. This will return only one value.
     *
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    fun longNum(key: String): CommandElement {
        return NumericElement(key,
            Function { java.lang.Long.parseLong(it) },
            BiFunction<String, Int, Long> { s, radix -> java.lang.Long.parseLong(s, radix) },
            Function { input -> "Expected a long, but input '$input' was not" })
    }

    /**
     * Require an argument to be an double-precision floating point number.
     *
     *
     * Gives values of type [Double]. This will return only one value.
     *
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    fun doubleNum(key: String): CommandElement {
        return NumericElement(key,
            Function<String, Double> { java.lang.Double.parseDouble(it) },
            null,
            Function { input -> "Expected a number, but input '$input' was not" })
    }

    /**
     * Require an argument to be a boolean.
     *
     *
     * The recognized true values are:
     *
     *
     *  * true
     *  * t
     *  * yes
     *  * y
     *  * verymuchso
     *
     *
     *
     *
     * The recognized false values are:
     *
     *
     *  * false
     *  * f
     *  * no
     *  * n
     *  * notatall
     *
     *
     *
     * Gives values of type [Boolean]. This will return only one value.
     *
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    fun bool(key: String): CommandElement {
        return choices(
            key,
            BOOLEAN_CHOICES
        )
    }

    /**
     * Require the argument to be a key under the provided enum.
     *
     *
     * Gives values of type <tt>T</tt>. This will return only one value.
     *
     * @param key  The key to store the matched enum value under
     * @param type The enum class to get enum constants from
     * @param <T>  The type of enum
     * @return the element to match the input
    </T> */
    fun <T : Enum<T>> enumValue(key: String, type: Class<T>): CommandElement {
        return EnumValueElement(key, type)
    }

    // -- Argument types for basic java types

    /**
     * Require one or more strings, which are combined into a single, space-separated string.
     *
     *
     * Gives values of type [String]. This will return only one value.
     *
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    fun remainingJoinedStrings(key: String): CommandElement {
        return RemainingJoinedStringsCommandElement(key, false)
    }

    /**
     * Require one or more strings, without any processing, which are combined into a single,
     * space-separated string.
     *
     *
     * Gives values of type [String]. This will return only one value.
     *
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    fun remainingRawJoinedStrings(key: String): CommandElement {
        return RemainingJoinedStringsCommandElement(key, true)
    }

    /**
     * Expect a literal sequence of arguments. This element matches the input against a predefined
     * array of arguments expected to be present, case-insensitively.
     *
     *
     * This will return only one value.
     *
     * @param key          The key to add to the context. Will be set to a value of true if this
     * element matches
     * @param expectedArgs The sequence of arguments expected
     * @return the appropriate command element
     */
    fun literal(key: String, vararg expectedArgs: String): CommandElement {
        return LiteralCommandElement(
            key,
            ImmutableList.copyOf(expectedArgs),
            true
        )
    }

    /**
     * Expect a literal sequence of arguments. This element matches the input against a predefined
     * array of arguments expected to be present, case-insensitively.
     *
     *
     * This will return only one value.
     *
     * @param key          The key to store this argument as
     * @param putValue     The value to put at key if this argument matches. May be
     * <tt>null</tt>
     * @param expectedArgs The sequence of arguments expected
     * @return the appropriate command element
     */
    fun literal(
        key: String, putValue: Any?,
        vararg expectedArgs: String
    ): CommandElement {
        return LiteralCommandElement(
            key,
            ImmutableList.copyOf(expectedArgs),
            putValue
        )
    }

    /**
     * Restricts the given command element to only insert one value into the context at the provided
     * key.
     *
     *
     * If more than one value is returned by an element, or the target key
     * already contains a value, this will throw an [ArgumentParseException]
     *
     * @param element The element to restrict
     * @return the restricted element
     */
    fun onlyOne(element: CommandElement): CommandElement {
        return OnlyOneCommandElement(element)
    }

    /**
     * Expect an argument to represent a [URL].
     *
     *
     * This will return only one value.
     *
     * @param key The key to store under
     * @return the argument
     */
    fun url(key: String): CommandElement {
        return UrlElement(key)
    }

    /**
     * Expect an argument to return a [BigDecimal].
     *
     * @param key The key to store under
     * @return the argument
     */
    fun bigDecimal(key: String): CommandElement {
        return BigDecimalElement(key)
    }

    /**
     * Expect an argument to return a [BigInteger].
     *
     *
     * This will return only one value.
     *
     * @param key The key to store under
     * @return the argument
     */
    fun bigInteger(key: String): CommandElement {
        return BigIntegerElement(key)
    }

    /**
     * Expect an argument to be a [UUID].
     *
     *
     * This will return only one value.
     *
     * @param key The key to store under
     * @return the argument
     */
    fun uuid(key: String): CommandElement {
        return UuidElement(key)
    }

    /**
     * Expect an argument to be a date-time, in the form of a [LocalDateTime]. If no date is
     * specified, [LocalDate.now] is used; if no time is specified, [LocalTime.MIDNIGHT]
     * is used.
     *
     *
     * Date-times are expected in the ISO-8601 format.
     *
     *
     * This will return only one value.
     *
     * @param key The key to store under
     * @return the argument
     * @see [ISO-8601](https://en.wikipedia.org/wiki/ISO_8601)
     */
    fun dateTime(key: String): CommandElement {
        return DateTimeElement(key, false)
    }

    /**
     * Expect an argument to be a date-time, in the form of a [LocalDateTime]. If no date is
     * specified, [LocalDate.now] is used; if no time is specified, [LocalTime.MIDNIGHT]
     * is used.
     *
     *
     * If no argument at all is specified, defaults to
     * [LocalDateTime.now].
     *
     *
     * Date-times are expected in the ISO-8601 format.
     *
     *
     * This will return only one value.
     *
     * @param key The key to store under
     * @return the argument
     */
    fun dateTimeOrNow(key: String): CommandElement {
        return DateTimeElement(key, true)
    }

    /**
     * Expect an argument to be a [Duration].
     *
     *
     * Durations are expected in the following format: `#D#H#M#S`.
     * This is not case sensitive.
     *
     *
     * This will return only one value.
     *
     * @param key The key to store under
     * @return the argument
     */
    fun duration(key: String): CommandElement {
        return DurationElement(key)
    }

    /**
     * Uses a custom set of suggestions for an argument. The provided suggestions will replace the
     * regular ones.
     *
     *
     * If `requireBegin` is false, then the already typed argument
     * will not be used to filter the provided suggestions.
     *
     * @param argument     The element to replace the suggestions of
     * @param suggestions  The suggestions to use
     * @param requireBegin Whether or not to require the current argument to begin provided arguments
     * @return the argument
     */
    @JvmOverloads
    fun withSuggestions(
        argument: CommandElement,
        suggestions: Iterable<String>, requireBegin: Boolean = true
    ): CommandElement {
        return withSuggestions(
            argument,
            Function { suggestions },
            requireBegin
        )
    }

    /**
     * Uses a custom set of suggestions for an argument. The provided suggestions will replace the
     * regular ones.
     *
     *
     * If `requireBegin` is false, then the already typed argument
     * will not be used to filter the provided suggestions.
     *
     * @param argument     The element to replace the suggestions of
     * @param suggestions  A function to return the suggestions to use
     * @param requireBegin Whether or not to require the current argument to begin provided arguments
     * @return the argument
     */
    @JvmOverloads
    fun withSuggestions(
        argument: CommandElement,
        suggestions: Function<CommandSource, Iterable<String>>,
        requireBegin: Boolean = true
    ): CommandElement {
        return WithSuggestionsElement(
            argument,
            suggestions,
            requireBegin
        )
    }

    /**
     * Filters an argument's suggestions. A suggestion will only be used if it matches the predicate.
     *
     * @param argument  The element to filter the suggestions of
     * @param predicate The predicate to test suggestions against
     * @return the argument
     */
    fun withConstrainedSuggestions(
        argument: CommandElement,
        predicate: Predicate<String>
    ): CommandElement {
        return FilteredSuggestionsElement(argument, predicate)
    }

    internal class MarkTrueCommandElement(key: String) : CommandElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return true
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            return emptyList()
        }

        override fun getUsage(src: CommandSource): String {
            return ""
        }
    }

    private class SequenceCommandElement internal constructor(private val elements: List<CommandElement>) :
        CommandElement(null) {

        @Throws(ArgumentParseException::class)
        override fun parse(
            source: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ) {
            for (element in this.elements) {
                element.parse(source, args, context)
            }
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
            val completions = Sets.newHashSet<String>()
            for (element in elements) {
                val state = args.snapshot
                try {
                    element.parse(src, args, context)
                    if (state == args.snapshot) {
                        completions.addAll(element.complete(src, args, context))
                        args.applySnapshot(state)
                    } else if (args.hasNext()) {
                        completions.clear()
                    } else {
                        args.applySnapshot(state)
                        completions.addAll(element.complete(src, args, context))
                        if (element !is OptionalCommandElement) {
                            break
                        }
                        args.applySnapshot(state)
                    }
                } catch (ignored: ArgumentParseException) {
                    args.applySnapshot(state)
                    completions.addAll(element.complete(src, args, context))
                    break
                }

            }
            return Lists.newArrayList(completions)
        }

        override fun getUsage(src: CommandSource): String {
            val build = StringBuilder()
            val it = this.elements.iterator()
            while (it.hasNext()) {
                val usage = it.next().getUsage(src)
                if (usage.isNotEmpty()) {
                    build.append(usage)
                    if (it.hasNext()) {
                        build.append(CommandMessageFormatting.SPACE_TEXT)
                    }
                }
            }
            return build.toString()
        }
    }

    private class ChoicesCommandElement internal constructor(
        key: String, private val keySupplier: Supplier<Collection<String>>,
        private val valueSupplier: Function<String, *>, private val choicesInUsage: Tristate
    ) : CommandElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return valueSupplier.apply(args.next())
                ?: throw args.createError(
                    "Argument was not a valid choice. Valid choices: " + keySupplier.get().toString()
                )
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            val prefix = args.nextIfPresent().orElse("")
            return this.keySupplier.get().stream().filter { startsWith(it, prefix) }.toList()
        }

        override fun getUsage(src: CommandSource): String {
            val keys = this.keySupplier.get()
            if (this.choicesInUsage === Tristate.TRUE || this.choicesInUsage === Tristate.UNDEFINED && keys.size <= CUTOFF) {
                val build = StringBuilder()
                build.append(CommandMessageFormatting.LT_TEXT)
                val it = keys.iterator()
                while (it.hasNext()) {
                    build.append(it.next())
                    if (it.hasNext()) {
                        build.append(CommandMessageFormatting.PIPE_TEXT)
                    }
                }
                build.append(CommandMessageFormatting.GT_TEXT)
                return build.toString()
            }
            return super.getUsage(src)
        }

        companion object {

            const val CUTOFF = 5
        }
    }

    private class FirstParsingCommandElement internal constructor(private val elements: List<CommandElement>) :
        CommandElement(null) {

        @Throws(ArgumentParseException::class)
        override fun parse(
            source: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ) {
            var lastException: ArgumentParseException? = null
            for (element in this.elements) {
                val startState = args.snapshot
                val contextSnapshot = context.createSnapshot()
                try {
                    element.parse(source, args, context)
                    return
                } catch (ex: ArgumentParseException) {
                    lastException = ex
                    args.applySnapshot(startState)
                    context.applySnapshot(contextSnapshot)
                }

            }
            if (lastException != null) {
                throw lastException
            }
        }

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return null
        }

        override fun complete(
            src: CommandSource, args: CommandArgs,
            context: CommandContext
        ): List<String> {
            return ImmutableList.copyOf(
                Iterables.concat(Iterables.transform(
                    this.elements
                ) { input ->
                    if (input == null) {
                        return@transform ImmutableList.of<String>()
                    }

                    val snapshot = args.snapshot
                    val ret = input.complete(src, args, context)
                    args.applySnapshot(snapshot)
                    ret
                })
            )
        }

        override fun getUsage(src: CommandSource): String {
            val ret = StringBuilder()
            val it = this.elements.iterator()
            while (it.hasNext()) {
                ret.append(it.next().getUsage(src))
                if (it.hasNext()) {
                    ret.append(CommandMessageFormatting.PIPE_TEXT)
                }
            }
            return ret.toString()
        }
    }

    private class OptionalCommandElement internal constructor(
        private val element: CommandElement, private val value: Any?,
        private val considerInvalidFormatEmpty: Boolean
    ) : CommandElement(null) {

        @Throws(ArgumentParseException::class)
        override fun parse(
            source: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ) {
            if (!args.hasNext()) {
                val key = this.element.key
                if (key != null && this.value != null) {
                    context.putArg(key, this.value)
                }
                return
            }
            val startState = args.snapshot
            try {
                this.element.parse(source, args, context)
            } catch (ex: ArgumentParseException) {
                if (this.considerInvalidFormatEmpty || args
                        .hasNext()
                ) { // If there are more args, suppress. Otherwise, throw the error
                    args.applySnapshot(startState)
                    if (this.element.key != null && this.value != null) {
                        context.putArg(this.element.key, this.value)
                    }
                } else {
                    throw ex
                }
            }

        }

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return if (args.hasNext()) null else this.element.parseValue(source, args)
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            return this.element.complete(src, args, context)
        }

        override fun getUsage(src: CommandSource): String {
            return "[" + this.element.getUsage(src) + "]"
        }
    }

    private class RepeatedCommandElement(
        private val element: CommandElement,
        private val times: Int
    ) :
        CommandElement(null) {

        @Throws(ArgumentParseException::class)
        override fun parse(
            source: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ) {
            for (i in 0 until this.times) {
                this.element.parse(source, args, context)
            }
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
            for (i in 0 until this.times) {
                val startState = args.snapshot
                try {
                    this.element.parse(src, args, context)
                } catch (e: ArgumentParseException) {
                    args.applySnapshot(startState)
                    return this.element.complete(src, args, context)
                }

            }
            return emptyList()
        }

        override fun getUsage(src: CommandSource): String {
            return this.times.toString() + '*'.toString() + this.element.getUsage(src)
        }
    }

    private class AllOfCommandElement(private val element: CommandElement) :
        CommandElement(null) {

        @Throws(ArgumentParseException::class)
        override fun parse(
            source: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ) {
            while (args.hasNext()) {
                this.element.parse(source, args, context)
            }
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
            while (args.hasNext()) {
                val startState = args.snapshot
                try {
                    this.element.parse(src, args, context)
                } catch (e: ArgumentParseException) {
                    args.applySnapshot(startState)
                    return this.element.complete(src, args, context)
                }

            }
            return emptyList()
        }

        override fun getUsage(src: CommandSource): String {
            return this.element.getUsage(src) + CommandMessageFormatting.STAR_TEXT
        }
    }

    /**
     * Parent class that specifies elements as having no tab completions. Useful for inputs with a
     * very large domain, like strings and integers.
     */
    private abstract class KeyElement(key: String) : CommandElement(key) {

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            return emptyList()
        }
    }

    private class StringElement internal constructor(key: String) :
        GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return args.next()
        }
    }

    private class NumericElement<T : Number>(
        key: String, private val parseFunc: Function<String, T>,
        private val parseRadixFunction: BiFunction<String, Int, T>?,
        private val errorSupplier: Function<String, String>
    ) : GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            val input = args.next()
            try {
                if (this.parseRadixFunction != null) {
                    if (input.startsWith("0x")) {
                        return this.parseRadixFunction.apply(input.substring(2), 16)
                    } else if (input.startsWith("0b")) {
                        return this.parseRadixFunction.apply(input.substring(2), 2)
                    }
                }
                return this.parseFunc.apply(input)
            } catch (ex: NumberFormatException) {
                throw args.createError(this.errorSupplier.apply(input))
            }

        }
    }


    private class EnumValueElement<T : Enum<T>> internal constructor(key: String, private val type: Class<T>) :
        PatternMatchingCommandElement(key) {
        private val values: Map<String, T> = type.enumConstants.map { it.name.toLowerCase() to it }.toMap()

        override fun getChoices(source: CommandSource): Iterable<String> {
            return this.values.keys
        }

        @Throws(IllegalArgumentException::class)
        override fun getValue(choice: String): Any {

            return values[choice.toLowerCase()]
                ?: throw IllegalArgumentException(
                    "No enum constant " + type.canonicalName + "." + choice
                )
        }
    }


    private class RemainingJoinedStringsCommandElement internal constructor(key: String, private val raw: Boolean) :
        GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            if (this.raw) {
                args.next()
                val ret = args.raw.substring(args.rawPosition)
                while (args.hasNext()) { // Consume remaining args
                    args.next()
                }
                return ret
            }
            val ret = StringBuilder(args.next())
            while (args.hasNext()) {
                ret.append(' ').append(args.next())
            }
            return ret.toString()
        }

        override fun getUsage(src: CommandSource): String {
            return (CommandMessageFormatting.LT_TEXT
                    + key + CommandMessageFormatting.ELLIPSIS_TEXT
                    + CommandMessageFormatting.GT_TEXT)
        }
    }

    private class LiteralCommandElement(
        key: String?, expectedArgs: List<String>,
        private val putValue: Any?
    ) : CommandElement(key) {

        private val expectedArgs: List<String>

        init {
            this.expectedArgs = ImmutableList.copyOf(expectedArgs)
        }

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            for (arg in this.expectedArgs) {
                val current = args.next()
                if (!current.equals(arg, ignoreCase = true)) {
                    throw args
                        .createError("Argument $current did not match expected next argument $arg")
                }
            }
            return this.putValue
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            for (arg in this.expectedArgs) {
                val next = args.nextIfPresent()
                if (!next.isPresent) {
                    break
                } else if (args.hasNext()) {
                    if (!next.get().equals(arg, ignoreCase = true)) {
                        break
                    }
                } else {
                    if (arg.toLowerCase().startsWith(next.get().toLowerCase())) { // Case-insensitive compare
                        return ImmutableList
                            .of(arg) // TODO: Possibly complete all remaining args? Does that even work
                    }
                }
            }
            return ImmutableList.of()
        }

        override fun getUsage(src: CommandSource): String {
            return Joiner.on(' ').join(this.expectedArgs)
        }
    }

    private class VectorCommandElement(key: String?) : CommandElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            var xStr: String
            val yStr: String
            val zStr: String
            xStr = args.next()
            if (xStr.contains(",")) {
                val split = xStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (split.size != 3) {
                    throw args
                        .createError("Comma-separated location must have 3 elements, not " + split.size)
                }
                xStr = split[0]
                yStr = split[1]
                zStr = split[2]
            } else {
                yStr = args.next()
                zStr = args.next()
            }
            val x = parseDouble(args, xStr)
            val y = parseDouble(args, yStr)
            val z = parseDouble(args, zStr)
            val vector = Vector<Double>()
            vector[0] = x
            vector[1] = y
            vector[2] = z
            return vector
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            var arg = args.nextIfPresent()
            // Traverse through the possible arguments. We can't really complete arbitrary integers
            if (arg.isPresent) {
                return if (arg.get().contains(",") || !args.hasNext()) {
                    ImmutableList.of(arg.get())
                } else {
                    arg = args.nextIfPresent()
                    if (args.hasNext()) {
                        ImmutableList.of(args.nextIfPresent().get())
                    } else ImmutableList.of(arg.get())
                }
            }
            return ImmutableList.of()
        }

        @Throws(ArgumentParseException::class)
        private fun parseDouble(args: CommandArgs, arg: String): Double {
            try {
                return java.lang.Double.parseDouble(arg)
            } catch (e: NumberFormatException) {
                throw args.createError("Expected input $arg to be a double, but was not")
            }

        }
    }

    private class OnlyOneCommandElement(private val element: CommandElement) :
        CommandElement(element.key) {

        @Throws(ArgumentParseException::class)
        override fun parse(
            source: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ) {
            this.element.parse(source, args, context)
            if (this.element.key?.let { context.getAll<Any>(it).size }!! > 1) {
                val key = this.element.key
                throw args.createError("Argument $key may have only one value!")
            }
        }

        override fun getUsage(src: CommandSource): String {
            return this.element.getUsage(src)
        }

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return this.element.parseValue(source, args)
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            return this.element.complete(src, args, context)
        }
    }

    private class UrlElement(key: String) : GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            val str = args.next()
            val url: URL
            try {
                url = URL(str)
            } catch (ex: MalformedURLException) {
                throw ArgumentParseException("Invalid URL!", ex, str, 0)
            }

            try {
                url.toURI()
            } catch (ex: URISyntaxException) {
                throw ArgumentParseException("Invalid URL!", ex, str, 0)
            }

            return url
        }
    }

    private class BigDecimalElement(key: String) :
        GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            val next = args.next()
            try {
                return BigDecimal(next)
            } catch (ex: NumberFormatException) {
                throw args.createError("Expected a number, but input $next was not")
            }

        }
    }

    private class BigIntegerElement(key: String) :
        GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            val integerString = args.next()
            try {
                return BigInteger(integerString)
            } catch (ex: NumberFormatException) {
                throw args.createError("Expected an integer, but input $integerString was not")
            }

        }
    }

    private class UuidElement(key: String) : GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            try {
                return UUID.fromString(args.next())
            } catch (ex: IllegalArgumentException) {
                throw args.createError("Invalid UUID!")
            }

        }

    }

    private class DateTimeElement(key: String, private val returnNow: Boolean) :
        CommandElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            if (!args.hasNext() && this.returnNow) {
                return LocalDateTime.now()
            }
            val state = args.snapshot
            val date = args.next()
            try {
                return LocalDateTime.parse(date)
            } catch (ex: DateTimeParseException) {
                try {
                    return LocalDateTime.of(LocalDate.now(), LocalTime.parse(date))
                } catch (ex2: DateTimeParseException) {
                    try {
                        return LocalDateTime.of(LocalDate.parse(date), LocalTime.MIDNIGHT)
                    } catch (ex3: DateTimeParseException) {
                        if (this.returnNow) {
                            args.applySnapshot(state)
                            return LocalDateTime.now()
                        }
                        throw args.createError("Invalid date-time!")
                    }

                }

            }

        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            val date = LocalDateTime.now().withNano(0).toString()
            return if (date.startsWith(args.nextIfPresent().orElse(""))) {
                ImmutableList.of(date)
            } else {
                ImmutableList.of()
            }
        }

        override fun getUsage(src: CommandSource): String {
            return if (!this.returnNow) {
                super.getUsage(src)
            } else {
                "[" + this.key + "]"
            }
        }
    }

    private class DurationElement(key: String) : GenericArguments.KeyElement(key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            var s = args.next().toUpperCase()
            if (!s.contains("T")) {
                if (s.contains("D")) {
                    if (s.contains("H") || s.contains("M") || s.contains("S")) {
                        s = s.replace("D", "DT")
                    }
                } else {
                    s = if (s.startsWith("P")) {
                        "PT" + s.substring(1)
                    } else {
                        "T$s"
                    }
                }
            }
            if (!s.startsWith("P")) {
                s = "P$s"
            }
            try {
                return Duration.parse(s)
            } catch (ex: DateTimeParseException) {
                throw args.createError("Invalid duration!")
            }

        }
    }

    private class WithSuggestionsElement(
        private val wrapped: CommandElement,
        private val suggestions: Function<CommandSource, Iterable<String>>,
        private val requireBegin: Boolean
    ) : CommandElement(wrapped.key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return this.wrapped.parseValue(source, args)
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            return if (this.requireBegin) {
                val arg = args.nextIfPresent().orElse("")
                ImmutableList
                    .copyOf(Iterables.filter(this.suggestions.apply(src)) { f -> f!!.startsWith(arg) })
            } else {
                ImmutableList.copyOf(this.suggestions.apply(src))
            }
        }

    }

    private class FilteredSuggestionsElement(
        private val wrapped: CommandElement,
        private val predicate: Predicate<String>
    ) : CommandElement(wrapped.key) {

        @Throws(ArgumentParseException::class)
        override fun parseValue(
            source: CommandSource,
            args: CommandArgs
        ): Any? {
            return this.wrapped.parseValue(source, args)
        }

        override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String> {
            return this.wrapped.complete(src, args, context).stream().filter(this.predicate)
                .toList()
        }

    }

}