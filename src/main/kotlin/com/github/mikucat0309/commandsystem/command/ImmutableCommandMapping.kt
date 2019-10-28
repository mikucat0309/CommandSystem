package com.github.mikucat0309.commandsystem.command

import com.google.common.base.Preconditions.checkNotNull
import java.util.*

/**
 * An immutable command mapping instance that returns the same objects that this instance is
 * constructed with.
 */
class ImmutableCommandMapping
/**
 * Create a new instance.
 *
 * @param callable The command callable
 * @param primaryAlias  The primary alias
 * @param aliases  A collection of all aliases
 * @throws IllegalArgumentException Thrown if aliases are duplicated
 */
    (
    callable: CommandCallable, override val primaryAlias: String,
    aliases: Collection<String>
) : CommandMapping {
    private val aliases: MutableSet<String>
    override val callable: CommandCallable

    override val allAliases: Set<String>
        get() = Collections.unmodifiableSet(this.aliases)

    /**
     * Create a new instance.
     *
     * @param callable The command callable
     * @param primary  The primary alias
     * @param alias    A list of all aliases
     * @throws IllegalArgumentException Thrown if aliases are duplicated
     */
    constructor(callable: CommandCallable, primary: String, vararg alias: String) : this(
        callable,
        primary,
        alias.asList()
    )

    init {
        checkNotNull(primaryAlias, "primary")
        checkNotNull(aliases, "aliases")
        this.aliases = HashSet(aliases)
        this.aliases.add(primaryAlias)
        this.callable = checkNotNull(callable, "callable")
    }

    override fun toString(): String {
        return ("ImmutableCommandMapping{" +
                "primary='${this.primaryAlias}', aliases=${this.aliases}, spec=${this.callable}" +
                "}")
    }
}
