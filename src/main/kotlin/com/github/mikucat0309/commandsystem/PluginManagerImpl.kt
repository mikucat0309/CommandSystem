package com.github.mikucat0309.commandsystem

import com.github.mikucat0309.commandsystem.plugin.PluginContainer
import com.github.mikucat0309.commandsystem.plugin.PluginManager

class PluginManagerImpl internal constructor() : PluginManager {

    override val pluginContainers = ArrayList<PluginContainer>()

    fun register(container: PluginContainer) {
        pluginContainers.add(container)
        container.instance!!.onEnable()
    }

    override fun fromInstance(instance: Any): PluginContainer? {
        return pluginContainers.firstOrNull { it.instance == instance }
    }

    override fun getPlugin(id: String): PluginContainer? {
        return pluginContainers.firstOrNull { it.id == id }
    }

    override fun isLoaded(id: String): Boolean {
        return pluginContainers.firstOrNull { it.id == id }?.instance != null ?: false
    }
}