package com.github.mikucat0309.commandsystem.command

object CommandMessageFormatting {

    const val PIPE_TEXT = "|"
    const val SPACE_TEXT = " "
    const val STAR_TEXT = "*"
    const val LT_TEXT = "<"
    const val GT_TEXT = ">"
    const val ELLIPSIS_TEXT = "…"

    /**
     * Format text to be output as an error directly to a sender. Not necessary when creating an
     * exception to be thrown
     *
     * @param error The error message
     * @return The formatted error message.
     */
    fun error(error: String): String {
        return error
    }

    /**
     * Format text to be output as a debug message directly to a sender.
     *
     * @param debug The debug message
     * @return The formatted debug message.
     */
    fun debug(debug: String): String {
        return debug
    }
}
