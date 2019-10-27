package com.github.mikucat0309.commandsystem.misc

/**
 * Represents a simple tristate.
 */
enum class Tristate(private val value: Boolean) {
    TRUE(true) {
        override fun and(other: Tristate): Tristate {
            return if (other === TRUE || other === UNDEFINED) TRUE else FALSE
        }

        override fun or(other: Tristate): Tristate {
            return TRUE
        }
    },
    FALSE(false) {
        override fun and(other: Tristate): Tristate {
            return FALSE
        }

        override fun or(other: Tristate): Tristate {
            return if (other === TRUE) TRUE else FALSE
        }
    },
    UNDEFINED(false) {
        override fun and(other: Tristate): Tristate {
            return other
        }

        override fun or(other: Tristate): Tristate {
            return other
        }
    };

    /**
     * ANDs this tristate with another tristate.
     *
     * @param other The tristate to AND with
     * @return The result
     */
    abstract fun and(other: Tristate): Tristate

    /**
     * ORs this tristate with another tristate.
     *
     * @param other The tristate to OR with
     * @return The result
     */
    abstract fun or(other: Tristate): Tristate

    /**
     * Returns the boolean representation of this tristate.
     *
     * @return The boolean tristate representation
     */
    fun asBoolean(): Boolean {
        return this.value
    }

    companion object {

        /**
         * Return the appropriate tristate for a given boolean value.
         *
         * @param `value` The boolean value
         * @return The appropriate tristate
         */
        fun fromBoolean(value: Boolean): Tristate {
            return if (value) TRUE else FALSE
        }
    }
}
