package com.github.mikucat0309.commandsystem.command.args.parsing

import com.github.mikucat0309.commandsystem.command.args.ArgumentParseException

interface InputTokenizer {

    /**
     * Take the input string and split it as appropriate into argument tokens.
     *
     * @param arguments The provided arguments
     * @param lenient   Whether to parse leniently
     * @return The tokenized strings. Empty list if error occurs
     * @throws ArgumentParseException if an invalid input is provided
     */
    @Throws(ArgumentParseException::class)
    fun tokenize(arguments: String, lenient: Boolean): List<SingleArg>

    companion object {

        /**
         * Use an input string tokenizer that supports quoted arguments and character escapes.
         *
         *
         * Forcing lenient to true makes the following apply:
         *
         *
         *  * Unclosed quotations are treated as a single string from the
         * opening quotation to the end of the arguments rather than throwing
         * an exception
         *
         *
         * @param forceLenient Whether the tokenizer is forced into lenient mode
         * @return the appropriate tokenizer
         */
        fun quotedStrings(forceLenient: Boolean): InputTokenizer {
            return QuotedStringTokenizer(true, forceLenient, false)
        }

        /**
         * Returns an input tokenizer that takes input strings and splits them by space.
         *
         * @return The appropriate tokenizer
         */
        fun spaceSplitString(): InputTokenizer {
            return SpaceSplitInputTokenizer.INSTANCE
        }

        /**
         * Returns an input tokenizer that returns the input string as a single argument.
         *
         * @return The appropriate tokenizer
         */
        fun rawInput(): InputTokenizer {
            return RawStringInputTokenizer.INSTANCE
        }
    }

}
