
import listeners.GameListener
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import java.io.File

object Main {

    val tokenFile = File("token.txt")


    @JvmStatic
    fun main(args: Array<String>) {

        if (tokenFile.createNewFile()) {
            return println("Please fill in the token.txt and start again!")
        }

        val jda = JDABuilder(AccountType.BOT).setToken(tokenFile.readText()).buildAsync()
        jda.addEventListener(GameListener())

    }

}