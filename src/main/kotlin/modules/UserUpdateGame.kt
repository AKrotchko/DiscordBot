package modules




// Please burn this

/*
fun addRoleToChannel(newRole: Role, channel: VoiceChannel) {
    //For each user in the lobby channel, if they have the same role as the newly handed out role AND they are in the lobby, put them in the new voice channel.

    val lobbyChannel = (channel.guild.getVoiceChannelsByName("🎮 Lobby", true)).firstOrNull()
    val controller = channel.guild.controller



    if (lobbyChannel != null) {

        for (member in lobbyChannel.members) {

            var roles = member.roles


            for (role in roles) {

                println(role.name)

                if (role.name.equals(newRole.name, true)) {
                    controller.moveVoiceMember(member, channel).complete()
                }
            }
        }
    }
}*/