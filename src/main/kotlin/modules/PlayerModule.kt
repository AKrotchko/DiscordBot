package modules

import ext.formatName
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent
import net.dv8tion.jda.core.hooks.EventListener
import struct.ModuleStruct
import java.awt.Color
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent


class PlayerModule(val jda: JDA) : ModuleStruct(), EventListener {

    // UUID --> Game
    val players = mutableMapOf<Long, Game>()


    override fun onEnable() {

        setUp()

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
            is MessageReceivedEvent -> event.onMessage(event)
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

            val channel = checkNotNull(guild.getVoiceChannelsByName(gameName, true)[0])
            if (role != null) {
                addRoleToChannel(role, channel)
            }
        }

    }

    fun addRoleToChannel(newRole: Role, channel: VoiceChannel){
        //For each user in the lobby channel, if they have the same role as the newly handed out role AND they are in the lobby, put them in the new voice channel.

        val lobbyChannel = checkNotNull(channel.guild.getVoiceChannelsByName("🎮-chat", true)[0])
        val controller = channel.guild.controller

        for (member in lobbyChannel.members) {
            for (role in member.roles) {
                if (role == newRole) {
                    controller.moveVoiceMember(member, channel)
                }
            }
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

    fun MessageReceivedEvent.onMessage(event: MessageReceivedEvent){

        if(event.isFromType(ChannelType.TEXT)) {
            if (event.message.contentRaw.equals("?help")){
                createWelcomeWebhook(event.guild, event.channel.name)
            }
        }

    }

    fun setUp() { //
        val channelName = "🎮"

        for (guild in jda.guilds) { //check each guild in the list of guilds this bot is assigned to

            var controller = guild.controller
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

    fun createWelcomeWebhook(guild: Guild, channelName: String) {


        var channel = guild.getTextChannelsByName(channelName, true)[0]

        MessageBuilder()
                .append("Thanks for inviting me to your Discord channel! I see what games you're playing,\n and try to help you find friends to play with.")
                .setEmbed(EmbedBuilder()
                        .setTitle("What do I do?")
                        .setDescription("Whenever you and your friends play games together, I will make channels for you to chat and talk in!")
                        .setColor(Color(2409760))
                        .setFooter("Discord Hack Week", "https://cdn.discordapp.com/attachments/592802691340697611/593964305263624212/hack_week.jpg")
                        .setThumbnail("https://cdn.discordapp.com/attachments/592802691340697611/593958561911013396/JDA_Hack_Week.png")
                        .setAuthor("Gaymr Bot", "https://github.com/AKrotchko/DiscordBot", "https://cdn.discordapp.com/attachments/592802691340697611/593958561911013396/JDA_Hack_Week.png")
                        .addField("When two or more people play the same game...", "You get your own channels and role!", false)
                        .addField("If you're hanging out in the lobby...", "I will put you in your new channel!", false)
                        .addField("Still confused?", "Just start playing some games with friends and try it out. If you need to see this again, type ``?help``", false)
//                        .addField("<:thonkang:219069250692841473>", "these last two", true) //How to do inline fields.
//                        .addField("<:thonkang:219069250692841473>", "are empty fields", true)
                        .build())
                .sendTo(channel).complete()
    }

    companion object {

        val BASIC_PERMISSIONS = listOf(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)

    }

}