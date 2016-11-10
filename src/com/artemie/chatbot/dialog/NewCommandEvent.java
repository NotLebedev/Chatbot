package com.artemie.chatbot.dialog;

/**
 * Created by Тема on 30.10.2016.
 */
public class NewCommandEvent implements VKEvent{

    private String command;
    private Integer userID;
    private Integer chatID;

    public NewCommandEvent (String command, Integer userID, Integer chatID) {
        this.command = command;
        this.userID = userID;
        this.chatID = chatID;
    }

    public String getCommand () {
        return command;
    }

    public Integer getUserID () {
        return userID;
    }

    public Integer getChatID () {
        return chatID;
    }
}
