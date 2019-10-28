package com.github.mikucat0309.commandsystem.command

import java.util.*

/**
 * Represents the result of a command in Sponge.
 */
class CommandResult
/**
 * Constructs a new command result.
 *
 * @param successCount The success count
 * @param affectedBlocks The number of affected blocks
 * @param affectedEntities The number of affected entities
 * @param affectedItems The number of affected items
 * @param queryResult The query result
 */
internal constructor(
    successCount: Int?, affectedBlocks: Int?, affectedEntities: Int?,
    affectedItems: Int?, queryResult: Int?
) {
    /**
     * Gets the success count of the command.
     *
     * @return The success count of the command
     */
    val successCount: Optional<Int>
    /**
     * Gets the number of blocks affected by the command.
     *
     * @return The number of blocks affected by the command, if such a count
     * exists
     */
    val affectedBlocks: Optional<Int>
    /**
     * Gets the number of entities affected by the command.
     *
     * @return The number of entities affected by the command, if such a count
     * exists
     */
    val affectedEntities: Optional<Int>
    /**
     * Gets the number of items affected by the command.
     *
     * @return The number of items affected by the command, if such a count
     * exists
     */
    val affectedItems: Optional<Int>
    /**
     * Gets the query result of the command, e.g. the time of the day,
     * an amount of money or a player's amount of XP.
     *
     * @return The query result of the command, if one exists
     */
    val queryResult: Optional<Int>

    init {
        this.successCount = Optional.ofNullable(successCount)
        this.affectedBlocks = Optional.ofNullable(affectedBlocks)
        this.affectedEntities = Optional.ofNullable(affectedEntities)
        this.affectedItems = Optional.ofNullable(affectedItems)
        this.queryResult = Optional.ofNullable(queryResult)
    }

    /**
     * A builder for [CommandResult]s.
     */
    class Builder internal constructor() {
        private var successCount: Int? = null
        private var affectedBlocks: Int? = null
        private var affectedEntities: Int? = null
        private var affectedItems: Int? = null
        private var queryResult: Int? = null

        /**
         * Sets if the command has been processed.
         *
         * @param successCount If the command has been processed
         * @return This builder, for chaining
         */
        fun successCount(successCount: Int?): Builder {
            this.successCount = successCount
            return this
        }

        /**
         * Sets the amount of blocks affected by the command.
         *
         * @param affectedBlocks The amount of blocks affected by the command
         * @return This builder, for chaining
         */
        fun affectedBlocks(affectedBlocks: Int?): Builder {
            this.affectedBlocks = affectedBlocks
            return this
        }

        /**
         * Sets the amount of entities affected by the command.
         *
         * @param affectedEntities The amount of entities affected by the
         * command
         * @return This builder, for chaining
         */
        fun affectedEntities(affectedEntities: Int?): Builder {
            this.affectedEntities = affectedEntities
            return this
        }

        /**
         * Sets the amount of items affected by the command.
         *
         * @param affectedItems The amount of items affected by the command
         * @return This builder, for chaining
         */
        fun affectedItems(affectedItems: Int?): Builder {
            this.affectedItems = affectedItems
            return this
        }

        /**
         * Sets the query result of the command, e.g. the time of the day,
         * an amount of money or a player's amount of XP.
         *
         * @param queryResult The query result of the command
         * @return This builder, for chaining
         */
        fun queryResult(queryResult: Int?): Builder {
            this.queryResult = queryResult
            return this
        }

        /**
         * Builds the [CommandResult].
         *
         * @return A CommandResult with the specified settings
         */
        fun build(): CommandResult {
            return CommandResult(
                this.successCount,
                this.affectedBlocks,
                this.affectedEntities,
                this.affectedItems,
                this.queryResult
            )
        }
    }

    companion object {
        private val EMPTY = builder().build()
        private val SUCCESS = builder().successCount(1).build()

        /**
         * Returns a [Builder].
         *
         * @return A new command result builder
         */
        fun builder(): Builder {
            return Builder()
        }

        /**
         * Returns a new [CommandResult] indicating that a command was
         * processed.
         *
         * @return The command result
         */
        fun empty(): CommandResult {
            return EMPTY
        }

        /**
         * Returns a result indicating the command was processed with a single
         * success.
         *
         * @return The result
         */
        fun success(): CommandResult {
            return SUCCESS
        }

        /**
         * Returns a result indicating the command was processed with a single
         * success.
         *
         * @param count The success count
         * @return The result
         */
        fun successCount(count: Int): CommandResult {
            return builder().successCount(count).build()
        }

        /**
         * Returns a result indicating the command was processed with an
         * amount of affected blocks.
         *
         * @param count The amount of blocks affected
         * @return The result
         */
        fun affectedBlocks(count: Int): CommandResult {
            return builder().affectedBlocks(count).build()
        }

        /**
         * Returns a result indicating the command was processed with an
         * amount of affected entities.
         *
         * @param count The amount of entities affected
         * @return The result
         */
        fun affectedEntities(count: Int): CommandResult {
            return builder().affectedEntities(count).build()
        }

        /**
         * Returns a result indicating the command was processed with an
         * amount of affected items.
         *
         * @param count The amount of items affected
         * @return The result
         */
        fun affectedItems(count: Int): CommandResult {
            return builder().affectedItems(count).build()
        }

        /**
         * Returns a result indicating the command was processed with an
         * amount of queries.
         *
         * @param count The amount of queries
         * @return The result
         */
        fun queryResult(count: Int): CommandResult {
            return builder().queryResult(count).build()
        }
    }
}
