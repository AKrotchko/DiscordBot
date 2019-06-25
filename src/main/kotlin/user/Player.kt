package user

import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.User

data class Player(val user: User, var game: Game, val mutualGuilds: MutableList<Guild> = mutableListOf())