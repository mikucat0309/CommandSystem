package com.github.mikucat0309.commandsystem.command

/**
 * This exception is thrown when a subject does not have permission to execute a command.
 */
class CommandPermissionException : CommandException {

    /**
     * Create a permissions exception with a custom message.
     *
     * @param message The message
     */
    @JvmOverloads
    constructor(message: String = "You do not have permission to use this command!") : super(message)

    /**
     * Create a permissions exception with a custom message and cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    constructor(message: String, cause: Throwable) : super(message, cause)
}
