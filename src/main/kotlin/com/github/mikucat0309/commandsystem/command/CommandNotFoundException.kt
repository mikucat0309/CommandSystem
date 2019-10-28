package com.github.mikucat0309.commandsystem.command

import com.google.common.base.Preconditions

/**
 * This exception is thrown when a sender tries to execute a command that does not exist.
 */
class CommandNotFoundException
/**
 * Create an exception with a custom message.
 *
 * @param message The message
 * @param command The command that was queried for
 */
    (message: String, command: String) : CommandException(message) {

    /**
     * Returns the command that was queried for.
     *
     * @return The command
     */
    val command: String

    /**
     * Create an exception with the default message.
     *
     * @param command The command that was queried for
     */
    constructor(command: String) : this("No such command", command)

    init {
        this.command = Preconditions.checkNotNull(command, "command")
    }
}
