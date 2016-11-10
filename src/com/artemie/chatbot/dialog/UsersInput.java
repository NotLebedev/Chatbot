package com.artemie.chatbot.dialog;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import com.artemie.chatbot.PublicProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Тема on 30.10.2016.
 */
public class UsersInput extends Thread {

    private VkApiClient vk;
    private UserActor actor;
    private Integer chatID;
    private List<Integer> users;
    private BlockingQueue<VKEvent> outPipe;

    private static final Logger logger = LogManager.getLogger(UsersInput.class);

    Boolean running;

    public UsersInput (VkApiClient vk, UserActor actor, Integer chatID, BlockingQueue<VKEvent> outPipe) {
        this.vk = vk;
        this.users = new ArrayList<>();
        this.actor = actor;
        this.chatID = chatID;
        this.outPipe = outPipe;
        running = true;
    }

    public void terminate(){
        running = false;
    }

    @Override
    public void run() {

        while (running) {

            List<Integer> currentUsers = null;

            try {
                currentUsers = vk.messages().getChatUsers(actor).chatId(chatID).execute();
            } catch (ApiException | ClientException e) {
                logger.error(e.getMessage());
            }

            if(!users.equals(currentUsers)) {

                List<Integer> newUsers = new ArrayList<>();

                for (Integer currentUser : currentUsers) {
                    if(!users.contains(currentUser))
                        newUsers.add(currentUser);
                }

                pushInPipe(new NewUserEvent(newUsers, chatID));

                //users.addAll(newUsers);

            }

            try {
                sleep((long) (1000f / PublicProperties.getUsersPollFrequency()));
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }

        }

        //logger.warn("User input for dialog c" + chatID + " terminated");

    }

    public Integer getUser(String firstName, String lastName) {

        List<Integer> allUsersInt = null;

        try {
            allUsersInt = vk.messages().getChatUsers(actor).chatId(chatID).execute();
        } catch (ApiException | ClientException e) {
            logger.error(e.getMessage());
        }

        ArrayList<String> allUsers = new ArrayList<>();

        for (Integer integer : allUsersInt) {
            allUsers.add(integer.toString());
        }

        List<UserField> fields = new ArrayList<>();
        fields.add(UserField.NICKNAME);

        List<UserXtrCounters> response = null;

        try {
            response = vk.users().get().userIds(allUsers).fields(fields).execute();
        } catch (ApiException | ClientException e) {
            logger.error(e.getMessage());
        }

        for (UserXtrCounters user : response) {

            if(user.getFirstName().equalsIgnoreCase(firstName) && user.getLastName().equalsIgnoreCase(lastName))
                return user.getId();

        }

        return null;

    }

    public String[] getUser(Integer id) {

        List<UserXtrCounters> response = null;
        List<String> user = new ArrayList<>();
        user.add(id.toString());
        List<UserField> fields = new ArrayList<>();
        fields.add(UserField.NICKNAME);

        try {
            response = vk.users().get().userIds(user).fields(fields).execute();
        } catch (ApiException | ClientException e) {
            logger.error(e.getMessage());
        }

        String[] name = new String[2];

        try {
            name[0] = response.get(0).getFirstName();
            name[1] = response.get(0).getLastName();
        }catch (NullPointerException e){
            logger.error(e.getMessage());
            return null;
        }

        return name;

    }

    private void pushInPipe(VKEvent event) {
        outPipe.add(event);
    }

}
