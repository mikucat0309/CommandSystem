package com.github.mikucat0309.commandsystem.command.args

import com.github.mikucat0309.commandsystem.command.CommandSource
import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables
import java.util.*
import java.util.regex.Pattern
import java.util.stream.StreamSupport
import kotlin.streams.toList

/**
 * Abstract command element that matches values based on pattern.
 */
abstract class PatternMatchingCommandElement protected constructor(key: String?) :
        CommandElement(key) {

    @Throws(ArgumentParseException::class)
    override fun parseValue(
            source: CommandSource,
            args: CommandArgs
    ): Any? {
        val unformattedPattern = args.next()
        val choices = getChoices(source)

        // Check for an exact match before we create the regex.
        // We do this because anything with ^[abc] would not match [abc]
        val exactMatch = getExactMatch(choices, unformattedPattern)
        if (exactMatch.isPresent) {
            // Return this as a collection as this can get transformed by the subclass.
            return setOf(exactMatch.get())
        }

        val pattern = getFormattedPattern(unformattedPattern)

        val ret = choices.filter { pattern.matcher(it).find() }.map { this.getValue(it) }

        if (!ret.iterator().hasNext()) {
            throw args.createError(
                    "No values matching pattern \'$unformattedPattern\' present for ${key
                            ?: nullKeyArg}!"
            )
        }
        return ret
    }

    override fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
    ): List<String> {
        var choices = getChoices(src)
        val nextArg = args.nextIfPresent()
        if (nextArg.isPresent) {
            choices = StreamSupport.stream(choices.spliterator(), false)
                    .filter { getFormattedPattern(nextArg.get()).matcher(it).find() }
                    .toList()
        }
        return ImmutableList.copyOf(choices)
    }

    internal fun getFormattedPattern(input: String): Pattern {
        var input = input
        if (!input.startsWith("^")) { // Anchor matches to the beginning -- this lets us use find()
            input = "^$input"
        }
        return Pattern.compile(input, Pattern.CASE_INSENSITIVE)

    }

    /**
     * Tests a string against a set of valid choices to see if it is a case-insensitive match.
     *
     * @param choices         The choices available to match against
     * @param potentialChoice The potential choice
     * @return If matched, an [Optional] containing the matched value
     */
    protected fun getExactMatch(
            choices: Iterable<String>,
            potentialChoice: String
    ): Optional<Any> {

        return Iterables.tryFind(choices) { potentialChoice.equals(it, true) }
                .toJavaUtil().map { this.getValue(it) }
    }

    /**
     * Gets the available choices for this command source.
     *
     * @param source The source requesting choices
     * @return the possible choices
     */
    protected abstract fun getChoices(source: CommandSource): Iterable<String>

    /**
     * Gets the value for a given choice. For any result in [.getChoices], this
     * must return a non-null value. Otherwise, an [IllegalArgumentException] may be throw.
     *
     * @param choice The specified choice
     * @return the choice's value
     * @throws IllegalArgumentException if the input string is not any return value of [getChoices]
     */
    @Throws(IllegalArgumentException::class)
    protected abstract fun getValue(choice: String): Any

    companion object {

        private const val nullKeyArg = "argument"
    }
}
