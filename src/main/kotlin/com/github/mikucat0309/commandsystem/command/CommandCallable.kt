package com.github.mikucat0309.commandsystem.command

import com.github.mikucat0309.commandsystem.command.spec.CommandSpec

/**
 * A low-level interface for commands that can be executed. For almost all use cases, higher-level
 * tools should be used instead, like [CommandSpec].
 *
 *
 * Implementations are not required to implement a sane
 * [Object.equals] but really should.
 */
interface CommandCallable {

    /**
     * Execute the command based on input arguments.
     *
     *
     * The implementing class must perform the necessary permission
     * checks.
     *
     * @param source    The caller of the command
     * @param arguments The raw arguments for this command
     * @return The result of a command being processed
     * @throws CommandException Thrown on a command error
     */
    @Throws(CommandException::class)
    fun process(
        source: CommandSource,
        arguments: String
    ): CommandResult

    /**
     * Gets a list of suggestions based on input.
     *
     *
     * If a suggestion is chosen by the user, it will replace the last
     * word.
     *
     * @param source    The command source
     * @param arguments The arguments entered up to this point performing tab completion
     * @return A list of suggestions
     * @throws CommandException Thrown if there was a parsing error
     */
    @Throws(CommandException::class)
    fun getSuggestions(source: CommandSource, arguments: String): List<String>

    /**
     * Gets a short one-line description of this command.
     *
     *
     * The help system may display the description in the command list.
     *
     * @param source The source of the help request
     * @return A description
     */
    fun getShortDescription(source: CommandSource): String?

    /**
     * Gets a longer formatted help message about this command.
     *
     *
     * It is recommended to use the default text color and style. Sections
     * with text actions (e.g. hyperlinks) should be underlined.
     *
     *
     * Multi-line messages can be created by separating the lines with
     * `\n`.
     *
     *
     * The help system may display this message when a source requests
     * detailed information about a command.
     *
     * @param source The source of the help request
     * @return A help text
     */
    fun getHelp(source: CommandSource): String?

    /**
     * Gets the usage string of this command.
     *
     *
     * A usage string may look like
     * `[-w &lt;world&gt;] &lt;var1&gt; &lt;var2&gt;`.
     *
     *
     * The string must not contain the command alias.
     *
     * @param source The source of the help request
     * @return A usage string
     */
    fun getUsage(source: CommandSource): String

}
