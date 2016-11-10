package com.artemie.chatbot;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Тема on 30.10.2016.
 */

@Deprecated
public class BlackListMonitor extends Thread {

    VkApiClient vk;
    UserActor actor;
    Boolean stop;
    DatabaseManager db;

    private static final Logger logger = LogManager.getLogger(BlackListMonitor.class);

    public BlackListMonitor (VkApiClient vk, UserActor actor) {

        this.vk = vk;
        this.actor = actor;
        this.stop = false;
        this.db = DatabaseManager.getInstance();

    }

    public void shutdown() {

        stop = true;

    }

    @Override
    public void run() {

        while (!stop) {

            moderate();

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void moderate () {

        ArrayList<Integer> chats = db.getModeratedChats();

        for (Integer chat : chats) {

            List<Integer> users = null;

            try {

                 users = vk.messages().getChatUsers(actor).chatId(chat).execute();

            } catch (ApiException e) {
                System.err.println("ERROR: failed to moderate, caught ApiException");
                e.printStackTrace();
                return;
            } catch (ClientException e) {
                logger.error(e.getMessage());
                return;
            }

            ArrayList<Integer> banned = db.getUsersBanned(chat);

            //System.out.println(banned.toString());
            //System.out.println(users.toString());
            //System.out.println(chat);

            for (Integer user : users) {

                if(banned.contains(user)) {
                    try {
                        vk.messages().removeChatUser(actor, chat, user.toString()).execute();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
                    //System.out.println(user.toString());
                }

            }

        }

    }

}
