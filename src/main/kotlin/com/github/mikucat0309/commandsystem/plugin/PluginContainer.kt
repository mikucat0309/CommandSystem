package com.github.mikucat0309.commandsystem.plugin

import org.slf4j.Logger
import java.nio.file.Path

/**
 * A wrapper around a class marked with an [Plugin] annotation to retrieve
 * information from the annotation for easier use.
 */
interface PluginContainer {
    val id: String
    val name: String
    val version: String
    val description: String
    val url: String
    val authors: Array<String>
    val source: Path
    val instance: Plugin?
    val logger: Logger
}
