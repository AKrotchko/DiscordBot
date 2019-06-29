package me.camdenorrb.botr

import java.io.File

fun main() {

    val tokenFile = File("token.txt")

    if (tokenFile.createNewFile()) {
        return println("Please fill in the token.txt and start again!")
    }

    Botr(tokenFile.readText().trimEnd()).enable()
}