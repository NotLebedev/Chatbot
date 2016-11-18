package com.artemie.chatbot.dialog;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Тема on 30.10.2016.
 */
public class Dialog {

    private UsersInput usersInput;
    private CommandInput commandInput;
    private Integer chatID;
    private DialogType dialogType;

    public Dialog(Integer chatID, VkApiClient ck, UserActor actor, BlockingQueue<VKEvent> outPipe, DialogType dialogType) {
        this.chatID = chatID;
        usersInput = new UsersInput(ck, actor, chatID, outPipe);
        commandInput = new CommandInput(ck, actor, chatID, outPipe);
        this.dialogType = dialogType;
    }

    public Integer getChatID () {
        return chatID;
    }

    public void start() {
        usersInput.start();
        commandInput.start();
    }

    public void terminate() {
        usersInput.terminate();
        commandInput.terminate();
    }

    public Integer getUser(String firstName, String lastName) {
        return usersInput.getUser(firstName, lastName);
    }

    public String[] getUser(Integer id){
        return usersInput.getUser(id);
    }

}
