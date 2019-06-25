package ext

import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.requests.restaction.ChannelAction
import net.dv8tion.jda.core.requests.restaction.RoleAction


fun Guild.getRoleByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: RoleAction.() -> RoleAction): Role {
    return getRolesByName(name, ignoreCase).firstOrNull()
        ?: controller.createRole().setName(name).onCreate().complete()
}

fun Guild.getTextChannelByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: ChannelAction.() -> ChannelAction): TextChannel {
    return getTextChannelsByName(name, ignoreCase).firstOrNull()
        ?: controller.createTextChannel(name).onCreate().complete() as TextChannel
}

fun Guild.getVoiceChannelByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: ChannelAction.() -> ChannelAction): VoiceChannel {
    return getVoiceChannelsByName(name, ignoreCase).firstOrNull()
        ?: controller.createVoiceChannel(name).onCreate().complete() as VoiceChannel
}

fun Guild.getCategoryByNameOrCreate(name: String, ignoreCase: Boolean = true, onCreate: ChannelAction.() -> ChannelAction): Category {
    return getCategoriesByName(name, ignoreCase).firstOrNull()
        ?: controller.createCategory(name).onCreate().complete() as Category
}