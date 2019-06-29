package me.camdenorrb.botr.ext

import net.dv8tion.jda.core.entities.Game


const val CONTROLLER_SYMBOL = "🎮"


fun String.formatGameName(): String {
    return "$CONTROLLER_SYMBOL ${this}"
}

fun Game.formatName(): String {
    return name.formatGameName()
}