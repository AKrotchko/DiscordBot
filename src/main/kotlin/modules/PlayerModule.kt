package modules

import ext.formatName
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent
import net.dv8tion.jda.core.hooks.EventListener
import struct.ModuleStruct

import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent


class PlayerModule(private val jda: JDA) : ModuleStruct(), EventListener {

    // UUID --> Game
    private val players = mutableMapOf<Long, Game>()

    override fun onEnable() {

        generateDefaultChannels() //The moment that the bot enters a server it will create the default channels.

        jda.guilds.flatMap { it.members }.toSet().filter { it.game != null }.forEach {
            addPlayer(it.user, it.game, players)
        }

        jda.addEventListener(this)
    }

    override fun onDisable() {
        jda.removeEventListener(this)
        clearPlayers()
    }

    override fun onEvent(event: Event) {
        //Events rely on the default channels, so we check if they exist, and generate them any time an event occurs.
        // If this slows the bot down at any point, I will rethink this.
        generateDefaultChannels()

        when (event) {
            is UserUpdateGameEvent -> event.onCall()
            is MessageReceivedEvent -> event.onMessage() //I don't need to pass the event in. Duh. It's implied.
            else -> return
        }
    }

    private fun clearPlayers() {

        val games = players.values.map { it.formatName() }.toSet()

        players.keys.map { jda.getUserById(it) }.flatMap { it.mutualGuilds }.toSet().forEach { guild ->
            //guild.getCategoriesByName("Games", true).firstOrNull()?.delete()?.queue()
            guild.roles.filter { it.name in games }.forEach { it.delete().queue() }
            guild.textChannels.filter { it.name in games }.forEach { it.delete().queue() }
            guild.voiceChannels.filter { it.name in games }.forEach { it.delete().queue() }
        }

        players.clear()
    }


    private fun UserUpdateGameEvent.onCall() {
        remPlayer(member.user, players)

        if (newGame == null) {
            return
        }

        val testingChannel = checkNotNull(guild.getTextChannelsByName("testing", true).firstOrNull()) {
            "Testing channel not found"
        }


        testingChannel.sendMessage(newGame.formatName()).queue()

        addPlayer(member.user, newGame, players)
    }

    private fun MessageReceivedEvent.onMessage() {
        remPlayer(member.user, players)

        if (message.channelType == ChannelType.TEXT) {
            if (message.contentRaw == "?help") {
                createWelcomeWebhook(message.guild, message.channel.name)
            }
        }

    }

    //leaving this function here, since it's called often.
    private fun generateDefaultChannels() {
        val channelName = "🎮"

        for (guild in jda.guilds) { //check each guild in the list of guilds this bot is assigned to

            val controller = guild.controller
            var doesChannelExist = false //false by default, set to true if we find the channel

//            if(checkNotNull(guild.getTextChannelsByName(newGame.name, true).firstOrNull()))

            for (voiceChannel in guild.voiceChannels) { //check each channel in this particular guild

                if (voiceChannel.name == "$channelName Lobby") { //If the channels name matchers our default (🎮 Lobby)
                    doesChannelExist = true
                }
            }

            if (!doesChannelExist) { //If the channel does not exist yet

                controller.createCategory(channelName).complete() //Creating the channel

                for (category in guild.categories) {

                    if (category.name == channelName) {
                        controller.createVoiceChannel("$channelName Lobby").setParent(category).complete()
                        controller.createTextChannel("$channelName-chat").setParent(category).complete()

                        createWelcomeWebhook(guild, "$channelName-chat")
                    }
                }
            }
        }
    }

    companion object {

        val BASIC_PERMISSIONS = listOf(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)

    }

}