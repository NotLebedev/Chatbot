package com.artemie.chatbot.dialog;

import java.util.List;

/**
 * Created by Тема on 30.10.2016.
 */
public class NewUserEvent implements VKEvent {

    private List<Integer> userID;
    private Integer chatID;

    public NewUserEvent (List<Integer> userID, Integer chatID){
        this.userID=userID;
        this.chatID=chatID;
    }

    public Integer getChatID () {
        return chatID;
    }

    public List<Integer> getUserID () {
        return userID;
    }


}
