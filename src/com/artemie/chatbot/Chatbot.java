package com.artemie.chatbot;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.artemie.chatbot.dialog.Dialog;
import com.artemie.chatbot.dialog.VKEvent;
import com.artemie.chatbot.logic.Logic;
import com.artemie.chatbot.vkManager.VKManager;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Тема on 30.10.2016.
 */
public class Chatbot {

    private ArrayList<Dialog> dialogs;
    private Logic logic;
    private VKManager vkManager;

    private VkApiClient vk;
    private UserActor actor;
    private ArrayList<Integer> dialogIDs;

    public Chatbot (UserActor actor, VkApiClient vk) {
        this.dialogIDs = DatabaseManager.getInstance().getModeratedChats();
        this.actor = actor;
        this.vk = vk;
    }

    public void start() {

        ArrayBlockingQueue<VKEvent> firstPipe = new ArrayBlockingQueue<>(1000, true);
        ArrayBlockingQueue<VKEvent> secondPipe = new ArrayBlockingQueue<>(1000, true);

        dialogs = new ArrayList<>();

        for (Integer dialogID : dialogIDs) {
            dialogs.add(new Dialog(dialogID, vk, actor, firstPipe, DatabaseManager.getInstance().getDialogType(dialogID)));
        }

        logic = new Logic(firstPipe, secondPipe, this);
        vkManager = new VKManager(vk, actor, secondPipe);

        for (Dialog dialog : dialogs) {
            dialog.start();
        }
        logic.start();
        vkManager.start();
    }

    public void terminate() {

        for (Dialog dialog : dialogs) {
            dialog.terminate();
        }

        logic.terminate();
        vkManager.terminate();

    }

    public Integer findUserInDialog(String firsName, String lastName, Integer chatID) {

        for (Dialog dialog : dialogs) {
            if(dialog.getChatID() == chatID)
                return dialog.getUser(firsName, lastName);
        }

        return null;

    }

    public String[] findUserInDialog(Integer id, Integer chatID) {

        for (Dialog dialog : dialogs) {
            if(dialog.getChatID() == chatID)
                return dialog.getUser(id);
        }

        return null;

    }
}
