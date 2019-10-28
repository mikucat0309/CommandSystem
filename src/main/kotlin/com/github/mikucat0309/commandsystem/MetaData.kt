package com.github.mikucat0309.commandsystem

/**
 * An annotation used to describe and mark a Sponge plugin.
 */
data class MetaData(
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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MetaData

        if (id != other.id) return false
        if (name != other.name) return false
        if (version != other.version) return false
        if (description != other.description) return false
        if (url != other.url) return false
        if (!authors.contentEquals(other.authors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + authors.contentHashCode()
        return result
    }
}
