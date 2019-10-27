package com.github.mikucat0309.commandsystem.command.args

import com.github.mikucat0309.commandsystem.command.CommandException
import com.google.common.base.Strings

/**
 * Exception thrown when an error occurs while parsing arguments.
 */
open class ArgumentParseException : CommandException {

    /**
     * Returns the source string arguments are being parsed from.
     *
     * @return The source string
     */
    val sourceString: String
    /**
     * Gets the position of the last fetched argument in the provided source string.
     *
     * @return The source string to get position for
     */
    val position: Int

    /**
     * Return a string pointing to the position of the arguments when this exception occurs.
     *
     * @return The appropriate position string
     */
    val annotatedPosition: String
        get() {
            var source = this.sourceString
            var position = this.position
            if (source.length > 80) {
                if (position >= 37) {
                    val startPos = position - 37
                    val endPos = source.length.coerceAtMost(position + 37)
                    source = if (endPos < source.length) {
                        "..." + source.substring(startPos, endPos) + "..."
                    } else {
                        "..." + source.substring(startPos, endPos)
                    }
                    position -= 40
                } else {
                    source = source.substring(0, 77) + "..."
                }
            }
            return source + "\n" + Strings.repeat(" ", position) + "^"
        }

    /**
     * Return a new [ArgumentParseException] with the given message, source and position.
     *
     * @param message  The message to use for this exception
     * @param source   The source string being parsed
     * @param position The current position in the source string
     */
    constructor(message: String, source: String, position: Int) : super(message, true) {
        this.sourceString = source
        this.position = position
    }

    /**
     * Return a new [ArgumentParseException] with the given message, cause, source and
     * position.
     *
     * @param message  The message to use for this exception
     * @param cause    The cause for this exception
     * @param source   The source string being parsed
     * @param position The current position in the source string
     */
    constructor(message: String?, cause: Throwable?, source: String, position: Int) : super(message, cause, true) {
        this.sourceString = source
        this.position = position
    }

    /**
     * An [ArgumentParseException] where the usage is already specified.
     */
    class WithUsage(
            wrapped: ArgumentParseException,

            /**
             * Gets the usage associated with this exception.
             *
             * @return The usage
             */
            val usage: String
    ) : ArgumentParseException(
            wrapped.message,
            wrapped.cause,
            wrapped.sourceString,
            wrapped.position
    )

}
