package com.github.mikucat0309.commandsystem.command.args

import com.github.mikucat0309.commandsystem.command.CommandSource

/**
 * Represents a command argument element.
 */
abstract class CommandElement protected constructor(
    /**
     * Return the key to be used for this object.
     *
     * @return the user-facing representation of the key
     */
    val key: String?
) {

    /**
     * Attempt to extract a value for this element from the given arguments and put it in the given
     * context. This method normally delegates to [.parseValue] for
     * getting the values. This method is expected to have no side-effects for the source, meaning
     * that executing it will not change the state of the [CommandSource] in any way.
     *
     * @param source  The source to parse for
     * @param args    The args to extract from
     * @param context The context to supply to
     * @throws ArgumentParseException if unable to extract a value
     */
    @Throws(ArgumentParseException::class)
    open fun parse(
        source: CommandSource,
        args: CommandArgs,
        context: CommandContext
    ) {
        val value = parseValue(source, args)
        val key = this.key
        if (key != null && value != null) {
            if (value is Iterable<*>) {
                for (ent in value) {
                    if (ent != null) {
                        context.putArg(key, ent)
                    }
                }
            } else {
                context.putArg(key, value)
            }
        }
    }

    /**
     * Attempt to extract a value for this element from the given arguments. This method is expected
     * to have no side-effects for the source, meaning that executing it will not change the state of
     * the [CommandSource] in any way.
     *
     * @param source The source to parse for
     * @param args   the arguments
     * @return The extracted value
     * @throws ArgumentParseException if unable to extract a value
     */
    @Throws(ArgumentParseException::class)
    abstract fun parseValue(
        source: CommandSource,
        args: CommandArgs
    ): Any?

    /**
     * Fetch completions for command arguments.
     *
     * @param src     The source requesting tab completions
     * @param args    The arguments currently provided
     * @param context The context to store state in
     * @return Any relevant completions
     */
    abstract fun complete(
        src: CommandSource, args: CommandArgs,
        context: CommandContext
    ): List<String>

    /**
     * Return a usage message for this specific argument.
     *
     * @param src The source requesting usage
     * @return The formatted usage
     */
    open fun getUsage(src: CommandSource): String {
        return if (key == null) "" else "<$key>"
    }
}
