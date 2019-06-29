package modules

import ext.formatName
import net.dv8tion.jda.core.entities.*

fun remPlayer(player: User, players: MutableMap<Long, Game>) {

    val gameName = players.remove(player.idLong)?.formatName() ?: return
    val oldRoles = player.mutualGuilds.mapNotNull { it.getRolesByName(gameName, true)?.firstOrNull() }

    oldRoles.forEach { role ->

        val guild = role.guild

        guild.controller.removeSingleRoleFromMember(guild.getMember(player), role).queue {

            val count = guild.getMembersWithRoles(role).size

            if (count <= 1) {
                role.delete().queue()
            }

            if (count <= 2) {
                guild.getCategoriesByName(gameName, true).firstOrNull()?.delete()?.queue()
                guild.getTextChannelsByName(gameName.replace(' ', '-'), true).firstOrNull()?.delete()?.queue()
                guild.getVoiceChannelsByName(gameName, true).firstOrNull()?.delete()?.queue()
            }
        }
    }
}

fun addPlayer(player: User, game: Game, players: MutableMap<Long, Game>) {


    val gameName = game.formatName()

    val mutualGuilds = player.mutualGuilds

    //val similarPlayers = players.filterValues { it.name == game.name }.keys.associateWith { jda.getUserById(it) }

    players[player.idLong] = game

    mutualGuilds.forEach { guild ->

        var role = guild.getRolesByName(gameName, true).firstOrNull()

        if (role == null) {
            role = guild.controller.createRole().setName(gameName).setMentionable(true).complete()
        }

        if (guild.getMembersWithRoles(role).size >= 1 && guild.getCategoriesByName(gameName, true).isEmpty()) {

            val category = guild.controller.createCategory(gameName).apply {
                guild.roles.forEach {
                    addPermissionOverride(it, emptyList(), PlayerModule.BASIC_PERMISSIONS)
                }
            }.complete() as Category

            category.createTextChannel(gameName).addPermissionOverride(role, PlayerModule.BASIC_PERMISSIONS, emptyList()).complete()
            category.createVoiceChannel(gameName).addPermissionOverride(role, PlayerModule.BASIC_PERMISSIONS, emptyList()).complete()
        }

        guild.controller.addSingleRoleToMember(guild.getMember(player), role).complete()

        val channels = (guild.getVoiceChannelsByName(gameName, true))
        if (role != null && channels.size > 0) {
            addRoleToChannel(role, channels[0])
        }
    }

}

fun addRoleToChannel(newRole: Role, channel: VoiceChannel) {
    //For each user in the lobby channel, if they have the same role as the newly handed out role AND they are in the lobby, put them in the new voice channel.

    val lobbyChannels = (channel.guild.getVoiceChannelsByName("🎮 Lobby", true))
    val controller = channel.guild.controller



    if (lobbyChannels.size > 0) {
        var lobbyChannel = lobbyChannels[0]

        for (member in lobbyChannel.members) {
            for (role in member.roles) {
                if (role == newRole) {
                    controller.moveVoiceMember(member, channel)
                }
            }
        }
    }



}