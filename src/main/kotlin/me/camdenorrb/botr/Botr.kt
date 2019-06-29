package me.camdenorrb.botr
import me.camdenorrb.botr.modules.LobbyModule
import me.camdenorrb.botr.modules.PlayerModule
import me.camdenorrb.botr.struct.ModuleStruct
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.hooks.EventListener

class Botr(token: String) : ModuleStruct(), EventListener {

    val jda by lazy {
        JDABuilder(AccountType.BOT).setToken(token).build()
    }


    override fun onEnable() {
        jda.addEventListener(this)
    }

    override fun onDisable() {
        jda.removeEventListener(this)
    }


    override fun onEvent(event: Event) {

        if (event.jda == jda && event !is ReadyEvent) return

        LobbyModule(this).enable()
        PlayerModule(this).enable()
    }

}