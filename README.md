# Gamr Bot - Discord Hack Week
### Contributors
- Andy#1590
- Mr.Midnight#0001
- Mo_Bros#8443

## About

### Gamr is a Discord bot designed to read and respond to Rich Presence updates inside a guild. The bot leverages Java Discord API (JDA) through the Kotlin language.

#### Primary Functionalities:

- Read updates in Rich Presence on the server, and assign corresponding roles
- Create custom voice and text channels when enough people are playing the same game
- Automatically move players into their newly created room if they are sitting in the lobby
- Generates his own set of channels to be used as a "waiting room", and a place for bot commands (Bot commands work anywhere for the time being)
- Deletes any roles that were generated and no longer have any users. When everyone stops playing a game, the role will be deleted.
- Cleans up channels after roles are deleted. Gamr always cleans up at the end of the party.

#### ToDo: (I ran out of time sorry)

- Let other people know when you start playing the same game as them (I wanted this to be opt in, so it got delayed)
- Send players an instant invite to the corresponding Discord guild when they boot up a game (Where is the Discord server search API? Hit me up.)
- Additional text commands for channel "rentals", so you don't have to rely on rich presence for a temporary voice channel.
