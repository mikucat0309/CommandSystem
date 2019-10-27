package com.github.mikucat0309.commandsystem.command.args

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import java.util.*

/**
 * Context that a command is executed in. This object stores parsed arguments from other commands
 */
class CommandContext {

    private val parsedArgs: Multimap<String, Any>

    /**
     * Create a new empty CommandContext.
     */
    init {
        this.parsedArgs = ArrayListMultimap.create()
    }

    /**
     * Gets all values for the given argument. May return an empty list if no values are present.
     *
     * @param key The key to get values for
     * @param <T> the type of value to get
     * @return the collection of all values
    </T> */
    fun <T> getAll(key: String): Collection<T> {
        return this.parsedArgs.get(key) as Collection<T>
    }

    /**
     * Gets the value for the given key if the key has only one value.
     *
     *
     * An empty [Optional] indicates that there are either zero or more
     * than one values for the given key. Use hasAny(Text) to verify which.
     *
     * @param key the key to get
     * @param <T> the expected type of the argument
     * @return the argument
    </T> */
    fun <T> getOne(key: String): T? {
        val values = this.parsedArgs.get(key)
        return if (values.size != 1) null else values.iterator().next() as T
    }

    /**
     * Gets the value for the given key if the key has only one value, throws an exception otherwise.
     *
     * @param key the key to get
     * @param <T> the expected type of the argument
     * @return the argument
     * @throws java.util.NoSuchElementException if there is no element with the associated key
     * @throws IllegalArgumentException         if there are more than one element associated with the
     * key (thus, the argument is illegal in this context)
     * @throws ClassCastException               if the element type is not what is expected by the
     * caller
    </T> */
    @Throws(NoSuchElementException::class, IllegalArgumentException::class, ClassCastException::class)
    fun <T> requireOne(key: String): T {
        val values = this.parsedArgs.get(key)
        if (values.size == 1) {
            return values.iterator().next() as T
        } else if (values.isEmpty()) {
            throw NoSuchElementException()
        }

        throw IllegalArgumentException()
    }

    /**
     * Insert an argument into this context.
     *
     * @param key   the key to store the arg under
     * @param value the value for this argument
     */
    fun putArg(key: String, value: Any) {
        checkNotNull(value, "value")
        this.parsedArgs.put(key, value)
    }

    /**
     * Returns whether this context has any value for the given argument key.
     *
     * @param key The key to look up
     * @return whether there are any values present
     */
    fun hasAny(key: String): Boolean {
        return this.parsedArgs.containsKey(key)
    }

    /**
     * Gets a snapshot of the data inside this context to allow it to be restored later.
     *
     *
     * This is only guaranteed to create a *shallow copy* of the
     * backing store. If any value is mutable, any changes to that value will be reflected in this
     * snapshot. It is therefore not recommended that you keep this snapshot around for longer than is
     * necessary.
     *
     * @return The [Snapshot] containing the current state of the [CommandContext]
     */
    fun createSnapshot(): com.github.mikucat0309.commandsystem.command.args.CommandContext.Snapshot {
        return Snapshot(this.parsedArgs)
    }

    /**
     * Resets a [CommandContext] to a previous state using a previously created [ ].
     *
     * @param snapshot The [Snapshot] to restore this context with
     */
    fun applySnapshot(snapshot: com.github.mikucat0309.commandsystem.command.args.CommandContext.Snapshot) {
        this.parsedArgs.clear()
        this.parsedArgs.putAll(snapshot.args)
    }

    /**
     * A snapshot of a [CommandContext]. This object does not contain any public API methods, a
     * snapshot should be considered a black box.
     */
    inner class Snapshot internal constructor(args: Multimap<String, Any>) {

        internal val args: Multimap<String, Any>

        init {
            this.args = ArrayListMultimap.create(args)
        }

    }

    companion object {
        /**
         * The argument key to indicate that a tab completion is taking place.
         */
        const val TAB_COMPLETION =
                "tab-complete-99999" // Random junk afterwards so we don't accidentally conflict with other args
    }
}
