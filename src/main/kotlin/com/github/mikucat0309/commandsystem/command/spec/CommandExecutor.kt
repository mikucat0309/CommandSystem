package com.github.mikucat0309.commandsystem.command.spec

import com.github.mikucat0309.commandsystem.command.CommandException
import com.github.mikucat0309.commandsystem.command.CommandResult
import com.github.mikucat0309.commandsystem.command.CommandSource
import com.github.mikucat0309.commandsystem.command.args.CommandContext

/**
 * Interface containing the method directing how a certain command will be executed.
 */
@FunctionalInterface
interface CommandExecutor {

    /**
     * Callback for the execution of a command.
     *
     * @param src  The commander who is executing this command
     * @param args The parsed command arguments for this command
     * @return the result of executing this command
     * @throws CommandException If a user-facing error occurs while executing this command
     */
    @Throws(CommandException::class)
    fun execute(
            src: CommandSource,
            args: CommandContext
    ): CommandResult
}
