package com.github.mikucat0309.commandsystem.command.args.parsing

import com.google.common.collect.ImmutableList
import java.util.*
import java.util.regex.Pattern

internal class SpaceSplitInputTokenizer private constructor() :
    InputTokenizer {

    override fun tokenize(
        arguments: String,
        lenient: Boolean
    ): List<SingleArg> {
        var arguments = arguments
        if (SPACE_REGEX.matcher(arguments).matches()) {
            return ImmutableList.of()
        }

        val ret = ArrayList<SingleArg>()
        var lastIndex = 0
        var spaceIndex = arguments.indexOf(" ")
        while (spaceIndex != -1) {
            arguments = if (spaceIndex != 0) {
                ret.add(
                    SingleArg(
                        arguments.substring(0, spaceIndex),
                        lastIndex,
                        lastIndex + spaceIndex
                    )
                )
                arguments.substring(spaceIndex)
            } else {
                arguments.substring(1)
            }
            lastIndex += spaceIndex + 1
            spaceIndex = arguments.indexOf(" ")
        }

        ret.add(
            SingleArg(
                arguments,
                lastIndex,
                lastIndex + arguments.length
            )
        )
        return ret
    }

    companion object {
        val INSTANCE = SpaceSplitInputTokenizer()
        private val SPACE_REGEX = Pattern.compile("^[ ]*$")
    }
}
