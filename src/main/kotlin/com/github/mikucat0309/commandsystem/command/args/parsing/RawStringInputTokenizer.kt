package com.github.mikucat0309.commandsystem.command.args.parsing

internal class RawStringInputTokenizer private constructor() : InputTokenizer {

    override fun tokenize(
        arguments: String,
        lenient: Boolean
    ): List<SingleArg> {
        return listOf(SingleArg(arguments, 0, arguments.length))
    }

    companion object {
        val INSTANCE = RawStringInputTokenizer()
    }
}
