package listeners
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class GameListener : ListenerAdapter() {

    override fun onUserUpdateGame(event: UserUpdateGameEvent) {
        val channel = event.guild.defaultChannel
        channel?.sendMessage(event.newGame.name)?.queue()
    }

}