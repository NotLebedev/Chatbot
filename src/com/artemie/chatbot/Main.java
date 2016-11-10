package com.artemie.chatbot;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static java.lang.Thread.sleep;

/**
 * Created by Тема on 09.10.2016.
 */

//Use this to get token
//https://oauth.vk.com/authorize?client_id=5654671&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=friends,messages,wall,offline&response_type=token&v=5.59&state=123456

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String args[]) throws ClientException, ApiException {

        PublicProperties.init("resources" + File.separator + "properties.properties", "resources" + File.separator + "login.properties");

        logger.warn("Chatbot start");

        while(true) {

            TransportClient transportClient = HttpTransportClient.getInstance();
            VkApiClient vk = new VkApiClient(transportClient);

            UserActor actor = new UserActor(PublicProperties.getUserId(), PublicProperties.getCode());

            Chatbot chatbot = new Chatbot(actor, vk);

            chatbot.start();

            try {
                sleep(120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chatbot.terminate();

            //logger.warn("Rebooting");

        }

 /*       while (true) {
            Scanner in = new Scanner(System.in);

            if(in.nextLine().equalsIgnoreCase("Terminate"))
                break;

        }

        chatbot.terminate();
        try {
            sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DatabaseManager.close();
*/
    }

}
