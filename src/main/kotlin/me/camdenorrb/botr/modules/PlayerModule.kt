package me.camdenorrb.botr.modules

import me.camdenorrb.botr.Botr
import me.camdenorrb.botr.ext.formatName
import me.camdenorrb.botr.struct.ModuleStruct
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Category
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent
import net.dv8tion.jda.core.hooks.EventListener

class PlayerModule(val botr: Botr) : ModuleStruct(), EventListener {

    // UUID --> Game
    val players = mutableMapOf<Long, Game>()


    override fun onEnable() {

        botr.jda.guilds.flatMap { it.members }.toSet().filter { it.game != null }.forEach {
            addPlayer(it.user, it.game)
        }

        botr.jda.addEventListener(this)
    }

    override fun onDisable() {
        botr.jda.removeEventListener(this)
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

        //val similarPlayers = players.filterValues { it.name == game.name }.keys.associateWith { jda.getUserById(it) }

        players[player.idLong] = game

        mutualGuilds.forEach { guild ->

            var role = guild.getRolesByName(gameName, true).firstOrNull()

            if (role == null) {
                role = guild.controller.createRole().setName(gameName).setMentionable(true).complete()
            }

            val roleCount = guild.members.count { players[it.user.idLong]?.name == game.name }

            if (roleCount > 1 && guild.getCategoriesByName(gameName, true).isEmpty()) {

                val category = guild.controller.createCategory(gameName).apply {
                    guild.roles.forEach {
                        addPermissionOverride(it, emptyList(), BASIC_PERMISSIONS)
                    }
                }.complete() as Category

                category.createTextChannel(gameName).addPermissionOverride(role, BASIC_PERMISSIONS, emptyList()).complete()
                category.createVoiceChannel(gameName).addPermissionOverride(role, BASIC_PERMISSIONS, emptyList()).complete()
            }

            guild.controller.addSingleRoleToMember(guild.getMember(player), role).complete()
        }

    }

    fun remPlayer(player: User) {

        val gameName = players.remove(player.idLong)?.formatName() ?: return
        val oldRoles = player.mutualGuilds.mapNotNull { it.getRolesByName(gameName, true)?.firstOrNull() }

        oldRoles.forEach { role ->

            val guild = role.guild

            guild.controller.removeSingleRoleFromMember(guild.getMember(player), role).complete()

            val count = guild.members.count { players[it.user.idLong]?.formatName() == gameName }

            if (count < 1) {
                role.delete().queue()
            }

            if (count < 2) {
                guild.getCategoriesByName(gameName, true).firstOrNull()?.delete()?.queue()
                guild.getTextChannelsByName(gameName.replace(' ', '-'), true).firstOrNull()?.delete()?.queue()
                guild.getVoiceChannelsByName(gameName, true).firstOrNull()?.delete()?.queue()
            }
        }
    }

    fun clearPlayers() {

        val games = players.values.map { it.formatName() }.toSet()

        players.keys.map { botr.jda.getUserById(it) }.flatMap { it.mutualGuilds }.toSet().forEach { guild ->
            //guild.getCategoriesByName("Games", true).firstOrNull()?.delete()?.queue()
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

        addPlayer(member.user, newGame)
    }



    companion object {

        val BASIC_PERMISSIONS = listOf(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)

    }

}