package com.artemie.chatbot.logic;

import com.artemie.chatbot.dialog.VKEvent;

/**
 * Created by Тема on 30.10.2016.
 */
public class RemoveUserEvent implements VKEvent {

    Integer chatID;
    Integer userID;

    public RemoveUserEvent (Integer chatID, Integer userID) {
        this.chatID = chatID;
        this.userID = userID;
    }

    public Integer getChatID () {
        return chatID;
    }

    public Integer getUserID () {
        return userID;
    }

}
