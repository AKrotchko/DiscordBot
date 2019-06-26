package modules

import ext.formatName
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Category
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent
import net.dv8tion.jda.core.hooks.EventListener
import struct.ModuleStruct

class PlayerModule(val jda: JDA) : ModuleStruct(), EventListener {

    // UUID --> Game
    val players = mutableMapOf<Long, Game>()


    override fun onEnable() {

        init()

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
        when (event) {
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

            if (guild.getMembersWithRoles(role).size >= 1 && guild.getCategoriesByName(gameName, true).isEmpty()) {

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

    fun clearPlayers() {

        val games = players.values.map { it.formatName() }.toSet()

        players.keys.map { jda.getUserById(it) }.flatMap { it.mutualGuilds }.toSet().forEach { guild ->
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

        val testingChannel = checkNotNull(guild.getTextChannelsByName("testing", true).firstOrNull()) {
            "Testing channel not found"
        }

        testingChannel.sendMessage(newGame.formatName()).queue()

        addPlayer(member.user, newGame)
    }

    fun init() {
        val channelName = "🎮"

        for (guild in jda.guilds) { //check each guild in the list of guilds this bot is assigned to

            var controller = guild.controller
            var doesChannelExist = false //false by default, set to true if we find the channel

            for (voiceChannel in guild.voiceChannels) { //check each channel in this particular guild

                if (voiceChannel.name == "$channelName Lobby") { //If the channels name matchers our default (🎮 Lobby)
                    doesChannelExist = true
                }
//
//                    var categories = guild.categories
//
//                    for (category in categories) {
//                        if (category.name == channelName) {
//                            print("The category name was already created, move on")
//                        } else {
////                            guild.controller.createCategory(channelName).queue()
//
//                            controller.createCategory(channelName).queue() //The category has been created here
//
//                        }
//                    }
//
//
//                    for (category in guild.categories) {
//                        if (category.name == channelName) {
////                            controller.createVoiceChannel(channelName).setParent(category).complete()
//                           var channel = controller.createVoiceChannel(channelName).setParent(category).complete()
//                            return //voice channel has been created, stop the method.
//                        }
//                    }
//                    var category = guild.getCategoriesByName(channelName, true)[0]
//                    guild.getVoiceChannelByNameOrCreate(channelName, true, )

//                    guild.getCategoriesByName(channelName, true)[0].createVoiceChannel(channelName).setParent(guild.getCategoriesByName(channelName, true)[0]).queue()
            }
            if (!doesChannelExist) {
                    controller.createCategory(channelName).complete() //Creating the channel
                println("Just queued the category creation.")

                for (category in guild.categories) {
                    println("The category named " + category.name + " compared to channelName " + channelName + " equals " + (category.name == channelName))

                    if (category.name == channelName) {
                        controller.createVoiceChannel("$channelName Lobby").setParent(category).complete()
                        controller.createTextChannel("$channelName Chat").setParent(category).complete()
                        return //return from the method, we've made the category and the channel.
                    }
                }
            }
        }
    }


    companion object {

        val BASIC_PERMISSIONS = listOf(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)

    }

}