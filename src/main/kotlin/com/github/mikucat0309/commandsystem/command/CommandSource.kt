package com.github.mikucat0309.commandsystem.command

import com.github.mikucat0309.commandsystem.locale.Locales
import com.github.mikucat0309.commandsystem.misc.MessageReceiver
import java.util.*

/**
 * Something that can execute commands.
 *
 *
 * Examples of potential implementations include players, the server console,
 * Rcon clients, web-based clients, command blocks, and so on.
 */
interface CommandSource : MessageReceiver {

    /**
     * Gets the name identifying this command source.
     *
     * @return The name of this command source
     */
    val name: String

    /**
     * Gets the locale used by this command source. If this [CommandSource] does have a [ ] configured or does not support configuring a [Locale], [Locales.DEFAULT] is
     * used.
     *
     * @return The locale used by this command source
     */
    val locale: Locale
        get() = Locales.DEFAULT

}
