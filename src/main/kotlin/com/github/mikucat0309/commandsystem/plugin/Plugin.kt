package com.github.mikucat0309.commandsystem.plugin

/**
 * An annotation used to describe and mark a Sponge plugin.
 */
abstract class Plugin(
        /**
         * An ID for the plugin to be used internally. The ID should be unique as to
         * not conflict with other plugins.
         *
         *
         * The plugin ID must match the [.ID_PATTERN].
         *
         * @return The plugin identifier
         * @see [Java package naming conventions](https://goo.gl/MRRYSJ)
         */
        val id: String,
        /**
         * The human readable name of the plugin as to be used in descriptions and
         * similar things.
         *
         * @return The plugin name, or an empty string if unknown
         */
        val name: String = "",
        /**
         * The version of the plugin.
         *
         * @return The plugin version, or an empty string if unknown
         */
        val version: String = "",

        /**
         * The description of the plugin, explaining what it can be used for.
         *
         * @return The plugin description, or an empty string if unknown
         */
        val description: String = "",
        /**
         * The URL or website of the plugin.
         *
         * @return The plugin url, or an empty string if unknown
         */
        val url: String = "",
        /**
         * The authors of the plugin.
         *
         * @return The plugin authors, or empty if unknown
         */
        val authors: Array<String> = emptyArray()
) {
    open fun onEnable() {}
}
