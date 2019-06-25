package modules

import ext.formatName
import ext.getCategoryByNameOrCreate
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent
import net.dv8tion.jda.core.hooks.EventListener
import struct.ModuleStruct

class PlayerModule(val jda: JDA) : ModuleStruct(), EventListener {

    val players = mutableMapOf<User, Game>()


    override fun onEnable() {

        jda.guilds.flatMap { it.members }.toSet().filter { it.game != null }.forEach {
            addPlayer(it.user, it.game)
        }

        jda.addEventListener(this)
    }

    override fun onDisable() {
        jda.removeEventListener(this)
        clearPlayers()
    }

    override fun onEvent(event: Event) {
        when(event) {
            is UserUpdateGameEvent -> event.onCall()
            else -> return
        }
    }


    fun addPlayer(player: User, game: Game) {

        val gameName = game.formatName()

        val mutualGuilds = player.mutualGuilds

        val similarPlayers = players.filterValues { it == game }.keys

        val similarGuilds = similarPlayers.flatMap { it.mutualGuilds.intersect(mutualGuilds) }.toSet()


        similarGuilds.forEach { guild ->

            val member = guild.getMember(player)

            var role = guild.getRolesByName(gameName, true).firstOrNull()

            if (role == null) {

                role = guild.controller.createRole().setName(gameName).setMentionable(true).complete()

                val category = guild.getCategoryByNameOrCreate(gameName) {

                    guild.roles.forEach {
                        addPermissionOverride(it, emptyList(), BASIC_PERMISSIONS)
                    }

                    this
                }

                category.createTextChannel(gameName).addPermissionOverride(role, BASIC_PERMISSIONS, emptyList()).queue()
                category.createVoiceChannel(gameName).addPermissionOverride(role, BASIC_PERMISSIONS, emptyList()).queue()

                guild.members.filter { it.user in similarPlayers }.forEach {
                    guild.controller.addSingleRoleToMember(it, role).queue()
                }

                return@forEach
            }

            guild.controller.addSingleRoleToMember(member, role)
        }


        players[player] = game
    }

    fun remPlayer(player: User) {

        val gameName = players.remove(player)?.formatName() ?: return
        val oldRoles = player.mutualGuilds.mapNotNull { it.getRolesByName(gameName, true)?.firstOrNull() }

        oldRoles.forEach { role ->

            val guild = role.guild

            guild.controller.removeSingleRoleFromMember(guild.getMember(player), role).queue {

                if (guild.getMembersWithRoles(role).size >= 2) return@queue

                role.delete().queue()

                guild.getTextChannelsByName(gameName, true).firstOrNull()?.delete()?.queue()
                guild.getVoiceChannelsByName(gameName, true).firstOrNull()?.delete()?.queue()
            }
        }
    }

    fun clearPlayers() {

        val games = players.values.toSet().associateBy { it.formatName() }

        players.flatMap { it.key.mutualGuilds }.toSet().forEach { guild ->
            guild.roles.filter { it.name in games }.forEach { it.delete().queue() }
            guild.textChannels.filter { it.name in games }.forEach { it.delete().queue() }
            guild.voiceChannels.filter { it.name in games }.forEach { it.delete().queue() }
        }

        players.clear()
    }


    fun UserUpdateGameEvent.onCall() {

        remPlayer(member.user)

        if (newGame == null) {
            return
        }

        val testingChannel = checkNotNull(guild.getTextChannelsByName("testing", true).firstOrNull()) {
            "Testing channel not found"
        }

        testingChannel.sendMessage(newGame.formatName()).queue()

        addPlayer(member.user, newGame)
    }



    companion object {

        val BASIC_PERMISSIONS = listOf(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)

    }

}