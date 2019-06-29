package me.camdenorrb.botr.modules

import me.camdenorrb.botr.Botr
import me.camdenorrb.botr.ext.CONTROLLER_SYMBOL
import me.camdenorrb.botr.ext.getCategoryByNameOrCreate
import me.camdenorrb.botr.struct.ModuleStruct
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.EventListener
import java.awt.Color

class LobbyModule(val botr: Botr) : ModuleStruct(), EventListener {

    override fun onEnable() {

        botr.jda.guilds.forEach { guild ->

            val category = guild.getCategoryByNameOrCreate(LOBBY_CATEGORY_CHANNEL)

            if (category.textChannels.none { it.name == LOBBY_TEXT_CHANNEL }) {
                category.createTextChannel(LOBBY_TEXT_CHANNEL).queue {
                    (it as TextChannel).sendMessage(defaultMessage).queue()
                }
            }

            if (category.voiceChannels.none { it.name == LOBBY_VOICE_CHANNEL }) {
                category.createTextChannel(LOBBY_VOICE_CHANNEL).queue()
            }

        }

        botr.jda.addEventListener(this)
    }

    override fun onDisable() {
        botr.jda.removeEventListener(this)
    }

    override fun onEvent(event: Event) {
        when (event) {
            // TODO: Listen for voice channel join
            is MessageReceivedEvent -> event.onCall()
            else -> return
        }
    }


    fun MessageReceivedEvent.onCall() {
        if (message.channelType != ChannelType.TEXT || !message.contentRaw.equals("?help", true)) return
        message.channel.sendMessage(defaultMessage).queue()
    }


    companion object {

        const val LOBBY_TEXT_CHANNEL = CONTROLLER_SYMBOL

        const val LOBBY_VOICE_CHANNEL = CONTROLLER_SYMBOL

        const val LOBBY_CATEGORY_CHANNEL = "$CONTROLLER_SYMBOL Lobby"


        val defaultMessage by lazy {

            val messageBuilder = MessageBuilder().append("Thanks for inviting me to your Discord channel! I see what games you're playing, \nand try to help you find friends to play with.")

            val embed = EmbedBuilder().setTitle("What do I do?")
                .setDescription("Whenever you and your friends play games together, I will make channels for you to chat and talk in!")
                .setColor(Color(2409760))
                .setFooter("Discord Hack Week", "https://cdn.discordapp.com/attachments/592802691340697611/593964305263624212/hack_week.jpg")
                .setThumbnail("https://cdn.discordapp.com/attachments/592802691340697611/593958561911013396/JDA_Hack_Week.png")
                .setAuthor("Gamr", "https://github.com/AKrotchko/DiscordBot", "https://cdn.discordapp.com/attachments/592802691340697611/593958561911013396/JDA_Hack_Week.png")
                .addField("When two or more people play the same game...", "You get your own channels and role!", false)
                .addField("If you're hanging out in the lobby...", "I will put you in your new channel!", false)
                .addField("Still confused?", "Just start playing some games with friends and try it out. If you need to see this again, type ``?help``", false)
                .build()

            messageBuilder.setEmbed(embed).build()
        }

    }

}