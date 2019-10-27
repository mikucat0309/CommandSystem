package com.github.mikucat0309.commandsystem.plugin

import org.slf4j.Logger

/**
 * The manager that manages plugins. This manager can retrieve
 * [PluginContainer]s from [Plugin] instances, getting
 * [Logger]s, etc.
 */
interface PluginManager {

    /**
     * Gets a [Collection] of all [PluginContainer]s.
     *
     * @return The plugins
     */
    val pluginContainers: Collection<PluginContainer>

    /**
     * Gets the plugin container from an instance.
     *
     * @param instance The instance
     * @return The container
     */
    fun fromInstance(instance: Any): PluginContainer?

    /**
     * Retrieves a [PluginContainer] based on its ID.
     *
     * @param id The plugin ID
     * @return The plugin, if available
     */
    fun getPlugin(id: String): PluginContainer?

    /**
     * Checks if a plugin is loaded based on its ID.
     * This may contain plugins/mods from other systems in some implementations.
     *
     * @param id the id of the [Plugin]
     * @return `true` if loaded `false` if not loaded.
     */
    fun isLoaded(id: String): Boolean

}