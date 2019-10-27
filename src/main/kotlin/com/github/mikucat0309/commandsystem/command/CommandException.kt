package com.github.mikucat0309.commandsystem.command

/**
 * Thrown when an executed command raises an error or when execution of the command failed.
 */
open class CommandException : Exception {

    private val includeUsage: Boolean

    /**
     * Constructs a new [CommandException] with the given message.
     *
     * @param message      The detail message
     * @param includeUsage Whether to include usage in the exception
     */
    @JvmOverloads
    constructor(message: String, includeUsage: Boolean = false) : super(message) {
        this.includeUsage = includeUsage
    }

    /**
     * Constructs a new [CommandException] with the given message and the given cause.
     *
     * @param message      The detail message
     * @param cause        The cause
     * @param includeUsage Whether to include the usage in the exception
     */
    @JvmOverloads
    constructor(message: String?, cause: Throwable?, includeUsage: Boolean = false) : super(message, cause) {
        this.includeUsage = includeUsage
    }

    /**
     * Gets whether the exception should include usage in the presentation of the
     * exception/stack-trace.
     *
     * @return Whether to include usage in the exception
     */
    fun shouldIncludeUsage(): Boolean {
        return this.includeUsage
    }
}
/**
 * Constructs a new [CommandException] with the given message.
 *
 * @param message The detail message
 */
/**
 * Constructs a new [CommandException] with the given message and the given cause.
 *
 * @param message The detail message
 * @param cause   The cause
 */
