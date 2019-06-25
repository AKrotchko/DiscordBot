import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import javax.security.auth.login.LoginException;

public class GameUpdateListener implements EventListener {

    //    Secret: dnxA1L8Hn_Y_h4-ckQehfCcGShry6Nmo
    //    Token: NTkyODQ5NDA0MTQ3NDAwNzA1.XRFULQ.oyIcG-GGZIFJPBjyROyIjSDqALY

    public static void main(String[] args) throws LoginException {
        JDA jda = new JDABuilder(AccountType.BOT).setToken("NTkyODQ5NDA0MTQ3NDAwNzA1.XRFULQ.oyIcG-GGZIFJPBjyROyIjSDqALY").buildAsync();
        jda.addEventListener(new GameUpdateListener());
    }

    public void onEvent(Event event) {
        if (event instanceof UserUpdateGameEvent) {
            TextChannel channel = ((UserUpdateGameEvent) event).getGuild().getDefaultChannel();

            try {
                channel.sendMessage(((UserUpdateGameEvent) event).getNewGame().getName()).queue();
            } catch (NullPointerException e) {
                //Log that a NPE was thrown. Odds are that they went from having rich presence to no rich presence.
            }
        }
    }
}