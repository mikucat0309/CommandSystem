package com.github.mikucat0309.commandsystem.command.args.parsing

import com.github.mikucat0309.commandsystem.command.args.ArgumentParseException
import java.util.*

/**
 * Parser for converting a quoted string into a list of arguments.
 *
 *
 * Grammar is roughly (yeah, this is not really a proper grammar but it gives
 * you an idea of what's happening:
 *
 * <blockquote><pre> WHITESPACE = Character.isWhiteSpace(codePoint)
 * CHAR := (all unicode)
 * ESCAPE := '\' CHAR
 * QUOTE = ' | "
 * UNQUOTED_ARG := (CHAR | ESCAPE)+ WHITESPACE
 * QUOTED_ARG := QUOTE (CHAR | ESCAPE)+ QUOTE
 * ARGS := ((UNQUOTED_ARG | QUOTED_ARG) WHITESPACE+)+</pre></blockquote>
 */
internal class QuotedStringTokenizer(
    private val handleQuotedStrings: Boolean, private val forceLenient: Boolean,
    private val trimTrailingSpace: Boolean
) : InputTokenizer {

    @Throws(ArgumentParseException::class)
    override fun tokenize(
        arguments: String,
        lenient: Boolean
    ): List<SingleArg> {
        if (arguments.isEmpty()) {
            return emptyList()
        }

        val state = TokenizerState(arguments, lenient)
        val returnedArgs = ArrayList<SingleArg>(arguments.length / 4)
        if (this.trimTrailingSpace) {
            skipWhiteSpace(state)
        }
        while (state.hasMore()) {
            if (!this.trimTrailingSpace) {
                skipWhiteSpace(state)
            }
            val startIdx = state.index + 1
            val arg = nextArg(state)
            returnedArgs.add(SingleArg(arg, startIdx, state.index))
            if (this.trimTrailingSpace) {
                skipWhiteSpace(state)
            }
        }
        return returnedArgs
    }

    // Parsing methods

    @Throws(ArgumentParseException::class)
    private fun skipWhiteSpace(state: TokenizerState) {
        if (!state.hasMore()) {
            return
        }
        while (state.hasMore() && Character.isWhitespace(state.peek())) {
            state.next()
        }
    }

    @Throws(ArgumentParseException::class)
    private fun nextArg(state: TokenizerState): String {
        val argBuilder = StringBuilder()
        if (state.hasMore()) {
            val codePoint = state.peek()
            if (this.handleQuotedStrings && (codePoint == CHAR_DOUBLE_QUOTE || codePoint == CHAR_SINGLE_QUOTE)) {
                // quoted string
                parseQuotedString(state, codePoint, argBuilder)
            } else {
                parseUnquotedString(state, argBuilder)
            }
        }
        return argBuilder.toString()
    }

    @Throws(ArgumentParseException::class)
    private fun parseQuotedString(
        state: TokenizerState,
        startQuotation: Int,
        builder: StringBuilder
    ) {
        // Consume the start quotation character
        var nextCodePoint = state.next()
        if (nextCodePoint != startQuotation) {
            throw state.createException(
                String
                    .format(
                        "Actual next character '%c' did not match expected quotation character '%c'",
                        nextCodePoint, startQuotation
                    )
            )
        }

        while (true) {
            if (!state.hasMore()) {
                if (state.isLenient || this.forceLenient) {
                    return
                }
                throw state.createException("Unterminated quoted string found")
            }
            nextCodePoint = state.peek()
            when (nextCodePoint) {
                startQuotation -> {
                    state.next()
                    return
                }
                CHAR_BACKSLASH -> parseEscape(
                    state,
                    builder
                )
                else -> builder.appendCodePoint(state.next())
            }
        }
    }

    @Throws(ArgumentParseException::class)
    private fun parseUnquotedString(
        state: TokenizerState,
        builder: StringBuilder
    ) {
        while (state.hasMore()) {
            val nextCodePoint = state.peek()
            when {
                Character.isWhitespace(nextCodePoint) -> return
                nextCodePoint == CHAR_BACKSLASH -> parseEscape(
                    state,
                    builder
                )
                else -> builder.appendCodePoint(state.next())
            }
        }
    }

    @Throws(ArgumentParseException::class)
    private fun parseEscape(state: TokenizerState, builder: StringBuilder) {
        state.next() // Consume \
        builder.appendCodePoint(state.next()) // TODO: Unicode character escapes (\u00A7 type thing)?
    }

    companion object {

        private const val CHAR_BACKSLASH: Int = '\\'.toInt()
        private const val CHAR_SINGLE_QUOTE: Int = '\''.toInt()
        private const val CHAR_DOUBLE_QUOTE: Int = '"'.toInt()
    }

}
