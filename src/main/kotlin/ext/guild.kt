package ext

import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.requests.restaction.ChannelAction
import net.dv8tion.jda.core.requests.restaction.RoleAction


fun Guild.getRoleByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: RoleAction.() -> Unit = {}): Role {
    return getRolesByName(name, ignoreCase).firstOrNull()
        ?: controller.createRole().setName(name).apply(onCreate).complete()
}

fun Guild.getTextChannelByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: ChannelAction.() -> Unit = {}): TextChannel {
    return getTextChannelsByName(name, ignoreCase).firstOrNull()
        ?: controller.createTextChannel(name).apply(onCreate).complete() as TextChannel
}

fun Guild.getVoiceChannelByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: ChannelAction.() -> Unit = {}): VoiceChannel {
    return getVoiceChannelsByName(name, ignoreCase).firstOrNull()
        ?: controller.createVoiceChannel(name).apply(onCreate).complete() as VoiceChannel
}

fun Guild.getCategoryByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: ChannelAction.() -> Unit = {}): Category {
    return getCategoriesByName(name, ignoreCase).firstOrNull()
        ?: controller.createCategory(name).apply(onCreate).complete() as Category
}