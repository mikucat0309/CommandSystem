package com.github.mikucat0309.commandsystem.command.args.parsing

import com.github.mikucat0309.commandsystem.command.args.ArgumentParseException

internal class TokenizerState(private val buffer: String, val isLenient: Boolean) {
    var index = -1
        private set

    // Utility methods
    fun hasMore(): Boolean {
        return this.index + 1 < this.buffer.length
    }

    @Throws(ArgumentParseException::class)
    fun peek(): Int {
        if (!hasMore()) {
            throw createException("Buffer overrun while parsing args")
        }
        return this.buffer.codePointAt(this.index + 1)
    }

    @Throws(ArgumentParseException::class)
    operator fun next(): Int {
        if (!hasMore()) {
            throw createException("Buffer overrun while parsing args")
        }
        return this.buffer.codePointAt(++this.index)
    }

    fun createException(message: String): ArgumentParseException {
        return ArgumentParseException(message, this.buffer, this.index)
    }
}
