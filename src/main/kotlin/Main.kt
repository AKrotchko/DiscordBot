
import modules.PlayerModule
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.hooks.EventListener
import java.io.File

object Main : EventListener {

    val tokenFile = File("token.txt")

    lateinit var jda: JDA
        private set


    @JvmStatic
    fun main(args: Array<String>) {

        if (tokenFile.createNewFile()) {
            return println("Please fill in the token.txt and start again!")
        }

        jda = JDABuilder(AccountType.BOT).setToken(tokenFile.readText()).addEventListener(this).build()
    }

    override fun onEvent(event: Event) {
        if (event !is ReadyEvent) return
        PlayerModule(jda).enable()
    }

}