package com.artemie.chatbot.logic;

import com.artemie.chatbot.dialog.VKEvent;

/**
 * Created by Тема on 31.10.2016.
 */
public class WriteEvent implements VKEvent {

    private String message;
    private Integer chatID;

    public WriteEvent (String message, Integer chatID) {
        this.message = message;
        this.chatID = chatID;
    }

    public String getMessage () {
        return message;
    }

    public Integer getChatID () {
        return chatID;
    }
}
