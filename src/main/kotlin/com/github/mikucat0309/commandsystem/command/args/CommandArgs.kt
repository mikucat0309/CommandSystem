package com.github.mikucat0309.commandsystem.command.args

import com.github.mikucat0309.commandsystem.command.args.parsing.SingleArg
import com.google.common.collect.ImmutableList
import java.util.*

/**
 * Holder for command arguments.
 */
class CommandArgs
/**
 * Create a new CommandArgs instance with the given raw input and arguments.
 *
 * @param rawInput Raw input
 * @param args     Arguments extracted from the raw input
 */
(
        /**
         * Return the raw string used to provide input to this arguments object.
         *
         * @return The raw input
         */
        val raw: String, args: List<SingleArg>
) {
    private val args: MutableList<SingleArg>
    private var index = -1

    /**
     * Gets a list of all arguments as a string. The returned list is immutable.
     *
     * @return all arguments
     */
    val all: List<String>
        get() = this.args.map { it.value }.toList()

    /**
     * Return this arguments object's current state. Can be used to reset with the [ ][.setState] method.
     *
     * @return The current state
     */
    /**
     * Restore the arguments object's state to a state previously used.
     *
     * @param state the previous state
     */
    // keep parity with before
    var state: Any
        @Deprecated("Use {@link #getSnapshot()} and {@link #applySnapshot(Snapshot)} instead")
        get() = snapshot
        @Deprecated("Use {@link #getSnapshot()} and {@link #applySnapshot(Snapshot)} instead")
        set(state) {
            require(state is Snapshot) { "Provided state was not of appropriate format returned by getState!" }

            applySnapshot(state, false)
        }

    /**
     * Gets the current position in raw input.
     *
     * @return the raw position
     */
    val rawPosition: Int
        get() = if (this.index < 0) 0 else this.args[this.index].startIdx

    /**
     * Gets a snapshot of the data inside this context to allow it to be restored later.
     *
     * @return The [CommandArgs.Snapshot] containing the current state of the [ ]
     */
    val snapshot: Snapshot
        get() = Snapshot(this.index, this.args)

    init {
        this.args = ArrayList(args)
    }

    /**
     * Return whether more arguments remain to be read.
     *
     * @return Whether more arguments remain
     */
    operator fun hasNext(): Boolean {
        return this.index + 1 < this.args.size
    }

    /**
     * Try to read the next argument without advancing the current index.
     *
     * @return The next argument
     * @throws ArgumentParseException if not enough arguments are present
     */
    @Throws(ArgumentParseException::class)
    fun peek(): String {
        if (!hasNext()) {
            throw createError("Not enough arguments")
        }
        return this.args[this.index + 1].value
    }

    /**
     * Try to read the next argument, advancing the current index if successful.
     *
     * @return The next argument
     * @throws ArgumentParseException if not enough arguments are present
     */
    @Throws(ArgumentParseException::class)
    operator fun next(): String {
        if (!hasNext()) {
            throw createError("Not enough arguments!")
        }
        return this.args[++this.index].value
    }

    /**
     * Try to read the next argument, advancing the current index if successful or returning an absent
     * optional if not.
     *
     * @return The optional next argument.
     */
    fun nextIfPresent(): Optional<String> {
        return if (hasNext())
            Optional.of(this.args[++this.index].value)
        else
            Optional.empty()
    }

    /**
     * Create a parse exception with the provided message which has the position of the last parsed
     * argument attached. The returned exception must be thrown at the target
     *
     * @param message The message for the exception
     * @return the newly created, but unthrown exception
     */
    fun createError(message: String): ArgumentParseException {
        return ArgumentParseException(
                message, this.raw,
                if (this.index < 0) 0 else this.args[this.index].startIdx
        )
    }

    internal fun getArgs(): List<SingleArg> {
        return this.args
    }

    /**
     * Get an arg at the specified position.
     *
     * @param index index of the element to return
     */
    operator fun get(index: Int): String {
        return this.args[index].value
    }

    /**
     * Insert an arg as the next arg to be returned by [.next].
     *
     * @param value The argument to insert
     */
    fun insertArg(value: String) {
        val index = if (this.index < 0) 0 else this.args[this.index].endIdx
        this.args.add(this.index + 1, SingleArg(value, index, index))
    }

    /**
     * Remove the arguments parsed between startState and endState.
     *
     * @param startState The starting state
     * @param endState   The ending state
     */
    @Deprecated(
            "Use with {@link #getSnapshot()} instead of {@link #getState()} with {@link\n" +
                    "   * #removeArgs(Snapshot, Snapshot)}"
    )
    fun removeArgs(startState: Any, endState: Any) {
        require(!(startState !is Int || endState !is Int)) { "One of the states provided was not of the correct type!" }

        removeArgs(startState, endState)
    }

    /**
     * Remove the arguments parsed between two snapshots.
     *
     * @param startSnapshot The starting state
     * @param endSnapshot   The ending state
     */
    fun removeArgs(
            startSnapshot: Snapshot,
            endSnapshot: Snapshot
    ) {
        removeArgs(startSnapshot.index, endSnapshot.index)
    }

    private fun removeArgs(startIdx: Int, endIdx: Int) {
        if (this.index >= startIdx) {
            if (this.index < endIdx) {
                this.index = startIdx - 1
            } else {
                this.index -= endIdx - startIdx + 1
            }
        }
        for (i in startIdx..endIdx) {
            this.args.removeAt(startIdx)
        }
    }

    /**
     * Returns the number of arguments
     *
     * @return the number of arguments
     */
    fun size(): Int {
        return this.args.size
    }

    /**
     * Go back to the previous argument.
     */
    internal fun previous() {
        if (this.index > -1) {
            --this.index
        }
    }

    /**
     * Resets a [CommandArgs] to a previous state using a previously created [ ].
     *
     *
     * If resetArgs is set to false, this snapshot will not reset the
     * argument list to its previous state, only the index.
     *
     * @param snapshot  The [CommandArgs.Snapshot] to restore this context with
     * @param resetArgs Whether to restore the argument list
     */
    @JvmOverloads
    fun applySnapshot(snapshot: Snapshot, resetArgs: Boolean = true) {
        this.index = snapshot.index
        if (resetArgs) {
            this.args.clear()
            this.args.addAll(snapshot.args)
        }
    }

    /**
     * A snapshot of a [CommandArgs]. This object does not contain any public API methods, a
     * snapshot should be considered a black box.
     */
    inner class Snapshot internal constructor(
            internal val index: Int,
            args: List<SingleArg>
    ) {
        internal val args: ImmutableList<SingleArg> =
                ImmutableList.copyOf(args)

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val snapshot = other as Snapshot?
            return this.index == snapshot!!.index && this.args == snapshot.args
        }

        override fun hashCode(): Int {
            return Objects.hash(this.index, this.args)
        }
    }
}
