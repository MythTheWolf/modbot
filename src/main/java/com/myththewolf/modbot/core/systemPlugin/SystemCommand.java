package com.myththewolf.modbot.core.systemPlugin;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;

public interface SystemCommand {
    public void onCommand(MessageAuthor author, Message message);
}
