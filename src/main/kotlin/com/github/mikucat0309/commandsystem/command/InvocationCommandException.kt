package com.github.mikucat0309.commandsystem.command

/**
 * Thrown when invocation of a command fails, wrapping the exception that is thrown.
 */
class InvocationCommandException

/**
 * Constructs a new exception with the given message and the given cause.
 *
 * @param message The detail message
 * @param cause   The cause
 */
    (message: String, cause: Throwable) : CommandException(message, cause)
