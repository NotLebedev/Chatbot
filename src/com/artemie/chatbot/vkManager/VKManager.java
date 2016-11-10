package com.artemie.chatbot.vkManager;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.artemie.chatbot.PublicProperties;
import com.artemie.chatbot.dialog.VKEvent;
import com.artemie.chatbot.logic.RemoveUserEvent;
import com.artemie.chatbot.logic.WriteEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Тема on 30.10.2016.
 */
public class VKManager extends Thread{

    private VkApiClient vk;
    private UserActor actor;
    private BlockingQueue<VKEvent> inPipe;

    private static final Logger logger = LogManager.getLogger(VKManager.class);

    private Boolean running;

    public VKManager (VkApiClient vk, UserActor actor, BlockingQueue<VKEvent> inPipe) {
        this.vk = vk;
        this.actor = actor;
        this.inPipe = inPipe;
        this.running = true;
    }

    public void terminate(){
        running = false;
    }

    @Override
    public void run() {

        while (running) {

            VKEvent event;

            event = inPipe.poll();

            if(event != null) {

                if(event instanceof RemoveUserEvent){
                    removeUser((RemoveUserEvent)event);
                }else if(event instanceof WriteEvent){
                    write((WriteEvent) event);
                }

            }

            try {
                sleep((long) (1000 / PublicProperties.getUsersPollFrequency()));
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }

        }

        //logger.warn("VK manager terminated");

    }

    private void removeUser(RemoveUserEvent event) {
        try {
            vk.messages().removeChatUser(actor, event.getChatID(), event.getUserID().toString()).execute();
        } catch (ApiException | ClientException e) {
            logger.error(e.getMessage());
        }
    }

    private void write(WriteEvent event){
        try {
            vk.messages().send(actor).message(event.getMessage()).peerId(2000000000 + event.getChatID()).execute();
        } catch (ApiException | ClientException e) {
            logger.error(e.getMessage());
        }
    }

}
