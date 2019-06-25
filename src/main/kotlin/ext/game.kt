package ext

import net.dv8tion.jda.core.entities.Game


fun String.formatGameName(): String {
    return "🎮 ${this}"
}

fun Game.formatName(): String {
    return name.formatGameName()
}