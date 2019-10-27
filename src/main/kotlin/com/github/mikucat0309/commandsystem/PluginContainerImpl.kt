package com.github.mikucat0309.commandsystem

import com.github.mikucat0309.commandsystem.plugin.Plugin
import com.github.mikucat0309.commandsystem.plugin.PluginContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

class PluginContainerImpl private constructor(
        override val id: String,
        override val name: String,
        override val version: String,
        override val description: String,
        override val url: String,
        override val authors: Array<String>,
        override val source: Path,
        override val instance: Plugin?,
        override val logger: Logger
) : PluginContainer {
    constructor(
            source: Path,
            instance: Plugin,
            logger: Logger = LoggerFactory.getLogger(instance.id)
    ) : this(
            instance.id,
            instance.name,
            instance.version,
            instance.description,
            instance.url,
            instance.authors,
            source,
            instance,
            logger
    )

}