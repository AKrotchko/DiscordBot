package modules

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import java.awt.Color

fun createWelcomeWebhook(guild: Guild, channelName: String) {


    val channel = guild.getTextChannelsByName(channelName, true)[0]

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