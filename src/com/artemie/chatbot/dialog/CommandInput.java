package com.artemie.chatbot.dialog;

import com.artemie.chatbot.PublicProperties;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.Actor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.responses.SearchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Тема on 31.10.2016.
 */
public class CommandInput extends Thread{

    private VkApiClient vk;
    private Actor actor;
    private Integer chatID; //In format 2000000000 + chatID
    private BlockingQueue<VKEvent> outPipe;

    private static final Logger logger = LogManager.getLogger(CommandInput.class);

    private Boolean running;

    public CommandInput (VkApiClient vk, Actor actor, Integer chatID, BlockingQueue<VKEvent> outPipe) {
        this.vk = vk;
        this.actor = actor;
        this.chatID = chatID + 2000000000;
        this.outPipe = outPipe;
        this.running = true;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run () {

        while (running) {

            SearchResponse response = null;

            try {
                 response = vk.messages().search(actor).q("$chatbot").peerId(chatID).count(1).execute();
            } catch (ApiException | ClientException e) {
                logger.error(e.getMessage());
            }

            if(!response.getItems().get(0).isReadState()) {
                try {
                    vk.messages().markAsRead(actor).messageIds(response.getItems().get(0).getId()).peerId(chatID.toString()).execute();
                } catch (ApiException | ClientException e) {
                    logger.error(e.getMessage());
                }

                pushInPipe(new NewCommandEvent(response.getItems().get(0).getBody(), response.getItems().get(0).getUserId(), chatID - 2000000000));
            }

            try {
                sleep((long) (1000f / PublicProperties.getCommandsPollFrequency()));
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }

        }

        //logger.warn("Command input for dialog c" + (chatID - 2000000000) + " terminated");

    }

    private void pushInPipe(VKEvent event) {
        outPipe.add(event);
    }

}
