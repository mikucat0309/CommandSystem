package com.github.mikucat0309.commandsystem.command

/**
 * Provides information about a mapping between a command and its aliases.
 *
 *
 * Implementations are not required to implement a sane
 * [Object.equals] but may choose to do so.
 */
interface CommandMapping {

    /**
     * Gets the primary alias.
     *
     * @return The primary alias
     */
    val primaryAlias: String

    /**
     * Gets an immutable list of all aliases.
     *
     *
     * The returned list must contain at least one entry, of which one must
     * be the one returned by [.getPrimaryAlias].
     *
     *
     * There may be several versions of the same alias with different
     * casing, although generally implementations should ignore the casing of aliases.
     *
     * @return A set of aliases
     */
    val allAliases: Set<String>

    /**
     * Gets the callable.
     *
     * @return The callable
     */
    val callable: CommandCallable

}
