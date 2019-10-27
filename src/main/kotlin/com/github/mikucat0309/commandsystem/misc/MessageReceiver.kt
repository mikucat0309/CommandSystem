package com.github.mikucat0309.commandsystem.misc

import com.google.common.base.Preconditions.checkNotNull

/**
 * Represents something that can receive (and send) messages.
 */
interface MessageReceiver {

    /**
     * Sends a message to this receiver.
     *
     *
     * If text formatting is not supported in the implementation
     * it will be displayed as plain text.
     *
     * @param message The message
     */
    fun sendMessage(message: String)

    /**
     * Sends the message(s) to this receiver.
     *
     *
     * If text formatting is not supported in the implementation
     * it will be displayed as plain text.
     *
     * @param messages The message(s)
     */
    fun sendMessages(vararg messages: String) {
        checkNotNull(messages, "messages")
        for (message in messages) {
            this.sendMessage(message)
        }
    }

    /**
     * Sends the message(s) to this receiver.
     *
     *
     * If text formatting is not supported in the implementation
     * it will be displayed as plain text.
     *
     * @param messages The messages
     */
    fun sendMessages(messages: Iterable<String>) {
        for (message in checkNotNull(messages, "messages")) {
            this.sendMessage(message)
        }
    }

}
