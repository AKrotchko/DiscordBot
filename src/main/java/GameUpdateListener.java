import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.requests.restaction.RoleAction;

import javax.security.auth.login.LoginException;

public class GameUpdateListener implements EventListener {

    //    Secret: dnxA1L8Hn_Y_h4-ckQehfCcGShry6Nmo
    //    Token: NTkyODQ5NDA0MTQ3NDAwNzA1.XRFULQ.oyIcG-GGZIFJPBjyROyIjSDqALY

    public static void main(String[] args) throws LoginException {
        JDA jda = new JDABuilder(AccountType.BOT).setToken("NTkyODQ5NDA0MTQ3NDAwNzA1.XRFULQ.oyIcG-GGZIFJPBjyROyIjSDqALY").buildAsync();
        jda.addEventListener(new GameUpdateListener());
    }

    public void onEvent(Event event) {
        if (event instanceof UserUpdateGameEvent) { //If the event is a Rich Presence update
            TextChannel channel = ((UserUpdateGameEvent) event).getGuild().getTextChannelsByName("testing", true).get(0);
//            TextChannel channel = ((UserUpdateGameEvent) event).getGuild().getDefaultChannel(); //Typical output


            try {
                Member member = ((UserUpdateGameEvent) event).getMember();
                String gameName = ((UserUpdateGameEvent) event).getNewGame().getName(); // throws NPE
                channel.sendMessage(gameName).queue();
                GuildController guildController = channel.getGuild().getController();


                String roleName = (gameName + " tempRole");
//
//                for (VoiceChannel vc : guildController.getGuild().getVoiceChannelsByName(roleName, true).
//                     ) {
//
//                }
                if (guildController.getGuild().getRolesByName(roleName, true).isEmpty()) { // If the name of the role doesn't exist already.
                    channel.sendMessage("The role does not exist. We'll create one now").queue();

                    //Creating the role.
                    RoleAction roleAction = guildController.createRole();
                    roleAction.setName(((UserUpdateGameEvent) event).getNewGame().getName() + " tempRole");
                    roleAction.setMentionable(false);
                    Role role = roleAction.complete();

                    //Giving the person whose game just changed that role
                    guildController.addSingleRoleToMember(member, role).queue();
                } else { // if the role name already exists
                    channel.sendMessage("The role already exists. Giving it to you.");
                    Role role = guildController.getGuild().getRolesByName(roleName, true).get(0);
                    guildController.addSingleRoleToMember(member, role).queue();
                }
            } catch (NullPointerException e) { //When the user STOPS playing a game.
                channel.sendMessage("You have stopped playing the game. Removing the role from you.");

                Member member = ((UserUpdateGameEvent) event).getMember();
                String gameName = ((UserUpdateGameEvent) event).getOldGame().getName();

                GuildController guildController = channel.getGuild().getController();
                String roleName = (gameName + " tempRole");

                Role role = ((UserUpdateGameEvent) event).getGuild().getRolesByName(roleName, true).get(0);

                for (Role temprole : member.getRoles()) { //For each role in the users set of roles...
                    if (temprole.getName().equalsIgnoreCase(roleName)){ //Check if the role we're looking at has the same name as the one we want to remove
                        guildController.removeSingleRoleFromMember(member, role).queue(); //Remove the role
                        channel.sendMessage("Removed the  " + role.getName() + " role from " + member.getUser().getName());

//                        Role deleteRole = guildController.getGuild().getRolesByName(roleName, true).get(0);

//                        channel.sendMessage("You were the last person with the " + role.getName() + " role. Deleting it now.").queue();
                          //ToDo -  Cannot find a way to get a list of the users with a given role. Will likely have to get all of the channels roles, and delete empty ones ever X hours.
                    }
                }
            }
        }
    }
}