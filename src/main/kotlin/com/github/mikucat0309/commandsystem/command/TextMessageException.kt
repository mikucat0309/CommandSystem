package com.github.mikucat0309.commandsystem.command

class TextMessageException : Exception {

    /**
     * Returns the text message for this exception, or null if nothing is present.
     *
     * @return The text for this message
     */
    val text: String?

    /**
     * Constructs a new [TextMessageException].
     */
    constructor() {
        this.text = null
    }

    /**
     * Constructs a new [TextMessageException] with the given message.
     *
     * @param message The detail message
     */
    constructor(message: String?) {
        this.text = message
    }

    /**
     * Constructs a new [TextMessageException] with the given message and cause.
     *
     * @param message   The detail message
     * @param throwable The cause
     */
    constructor(message: String?, throwable: Throwable) : super(throwable) {
        this.text = message
    }

    /**
     * Constructs a new [TextMessageException] with the given cause.
     *
     * @param throwable The cause
     */
    constructor(throwable: Throwable) : super(throwable) {
        this.text = null
    }

    override fun getLocalizedMessage(): String? {
        return message
    }
}
