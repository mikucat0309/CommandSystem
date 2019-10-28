package com.github.mikucat0309.commandsystem.command.args.parsing

import com.google.common.base.MoreObjects
import com.google.common.base.Objects

/**
 * This represents a single argument with its start and end indexes in the associated raw input
 * string.
 */
class SingleArg
/**
 * Create a new argument.
 *
 * @param value    The argument string
 * @param startIdx The starting index of `value` in an input string
 * @param endIdx   The ending index of `value` in an input string
 */
    (
    /**
     * Gets the string used.
     *
     * @return The string used
     */
    val value: String,
    /**
     * Gets the starting index.
     *
     * @return The starting index
     */
    val startIdx: Int,
    /**
     * Gets the ending index.
     *
     * @return The ending index
     */
    val endIdx: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is SingleArg) {
            return false
        }
        val singleArg = other as SingleArg?
        return (this.startIdx == singleArg!!.startIdx
                && this.endIdx == singleArg.endIdx
                && Objects.equal(this.value, singleArg.value))
    }

    override fun hashCode(): Int {
        return Objects.hashCode(this.value, this.startIdx, this.endIdx)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("value", this.value)
            .add("startIdx", this.startIdx)
            .add("endIdx", this.endIdx)
            .toString()
    }
}
