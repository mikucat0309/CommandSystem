package com.github.mikucat0309.commandsystem

import org.slf4j.LoggerFactory

class CommandSystem {

    companion object {
        val pluginManager = PluginManagerImpl()

        val commandManager = CommandManagerImpl()

        val logger = LoggerFactory.getLogger(CommandSystem::class.java)
    }
}