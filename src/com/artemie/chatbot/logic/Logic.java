package com.artemie.chatbot.logic;

import com.artemie.chatbot.Chatbot;
import com.artemie.chatbot.DatabaseManager;
import com.artemie.chatbot.PublicProperties;
import com.artemie.chatbot.dialog.NewCommandEvent;
import com.artemie.chatbot.dialog.NewUserEvent;
import com.artemie.chatbot.dialog.VKEvent;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Тема on 30.10.2016.
 */
public class Logic extends Thread{

    private BlockingQueue<VKEvent> inPipe;
    private BlockingQueue<VKEvent> outPipe;
    private DatabaseManager db;
    private Chatbot parent;

    private static final Logger logger = LogManager.getLogger(Logic.class);

    private Boolean running;

    public Logic (BlockingQueue<VKEvent> inPipe, BlockingQueue<VKEvent> outPipe, Chatbot parent) {
        db = DatabaseManager.getInstance();
        this.inPipe = inPipe;
        this.outPipe = outPipe;
        this.parent = parent;
        running = true;
    }

    public void terminate(){
        running = false;
    }


    @Override
    public void run() {

        while (running) {

            if (!db.isConnected()) {
                logger.error("Data base not connected");
            }else {

                VKEvent event = null;

                try {
                    event = inPipe.poll((long) (1000 / PublicProperties.getUsersPollFrequency()), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }

                if (event != null) {

                    if (event instanceof NewUserEvent) {
                        newUserEvent((NewUserEvent) event);
                    } else if (event instanceof NewCommandEvent) {
                        newCommandEvent((NewCommandEvent) event);
                    }

                }
            }

        }

        //logger.warn("Logic terminated");

    }

    private void newUserEvent(NewUserEvent event) {
        ArrayList<Integer> banned = db.getUsersBanned(event.getChatID());

        for (Integer user : event.getUserID()) {
            if(banned.contains(user))
                pushForRemove(user, event.getChatID());

        }
    }

    private void newCommandEvent(NewCommandEvent event) {

        Boolean isUserAdmin = db.isUserAdmin(event.getUserID(), event.getChatID());
        Boolean isUserModer = db.isUserModer(event.getUserID(), event.getChatID());

        String []args = event.getCommand().split("\\s+");
        args = Arrays.copyOfRange(args, 1, args.length);

        Options options = new Options();

        options.addOption("c", true, "command");
        options.addOption("a", true, "argument");

        CommandLineParser parser = new DefaultParser();

        String command = null;
        String argument = null;

        try {
            CommandLine result = parser.parse(options, args);

            command = result.getOptionValues("c")[0];
            argument = result.getOptionValues("a")[0];
        } catch (ParseException | NullPointerException e) {
            logger.error(e.getMessage());
        }

        if(command == null || argument == null)
            return;

        if(command.equalsIgnoreCase("banID") && Integer.valueOf(argument) != null && isUserModer) {

            String[] name = parent.findUserInDialog(Integer.valueOf(argument), event.getChatID());

            if(name.length < 2 || name == null) {
                pushForSay("User was not found", event.getChatID());
                return;
            }

            if(!db.isUserAdmin(Integer.valueOf(argument), event.getChatID()) && !db.isUserModer(Integer.valueOf(argument), event.getChatID())) {
                db.addUserBanned(Integer.valueOf(argument), event.getChatID(), name[0], name[1]);
                logger.warn("Banned user " + name[0] + " " + name[1] + " in chat " +event.getChatID());
            }else if(!db.isUserAdmin(Integer.valueOf(argument), event.getChatID()) && db.isUserModer(Integer.valueOf(argument), event.getChatID())){
                db.removeUserModer(name[0], name[1], event.getChatID());
                db.addUserBanned(Integer.valueOf(argument), event.getChatID(), name[0], name[1]);
                logger.warn("Banned user " + name[0] + " " + name[1] + " in chat " +event.getChatID());
            }

        }else if(command.equalsIgnoreCase("unBanID") && Integer.valueOf(argument) != null && isUserModer){

            db.removeUserBanned(Integer.valueOf(argument), event.getChatID());
            logger.warn("Unbanned user " + Integer.valueOf(argument) + " in chat " +event.getChatID());

        }else if(command.equalsIgnoreCase("ban") && isUserModer){

            String[] name = argument.split("_");

            if(name.length < 2) {
                pushForSay("Invalid name", event.getChatID());
                return;
            }

            Integer id = parent.findUserInDialog(name[0], name[1], event.getChatID());

            if(id == null) {
                pushForSay("User was not found", event.getChatID());
                return;
            }

            if(!db.isUserAdmin(id, event.getChatID()) && !db.isUserModer(id, event.getChatID())) {
                db.addUserBanned(id, event.getChatID(), name[0], name[1]);
                logger.warn("Banned user " + name[0] + " " + name[1] + " in chat " +event.getChatID());
            }else if(!db.isUserAdmin(id, event.getChatID()) && db.isUserModer(id, event.getChatID()) && isUserAdmin) {
                db.removeUserModer(name[0], name[1], event.getChatID());
                db.addUserBanned(id, event.getChatID(), name[0], name[1]);
                logger.warn("Banned user " + name[0] + " " + name[1] + " in chat " +event.getChatID());
            }

        }else if(command.equalsIgnoreCase("unban") && isUserModer) {

            String[] name = argument.split("_");

            if(name.length < 2) {
                pushForSay("Invalid name", event.getChatID());
                return;
            }

            db.removeUserBanned(name[0], name[1], event.getChatID());
            logger.warn("Unbanned user " + name[0] + " " + name[1] + " in chat " +event.getChatID());

        }else if(command.equalsIgnoreCase("addModer") && isUserAdmin) {

            String[] name = argument.split("_");

            if(name.length < 2) {
                pushForSay("Invalid name", event.getChatID());
                return;
            }

            Integer id = parent.findUserInDialog(name[0], name[1], event.getChatID());

            if(id == null) {
                pushForSay("User was not found", event.getChatID());
                return;
            }

            if(db.isUserBanned(id , event.getChatID())) {
                db.removeUserBanned(name[0], name[1], event.getChatID());
                db.addUserModer(id, event.getChatID(), name[0], name[1]);
                logger.warn("Added moder " + name[0] + " " + name[1] + " in chat " +event.getChatID());
            }else if(!db.isUserAdmin(id, event.getChatID())){
                db.addUserModer(id, event.getChatID(), name[0], name[1]);
                logger.warn("Added moder " + name[0] + " " + name[1] + " in chat " +event.getChatID());
            }

        }else if(command.equalsIgnoreCase("removeModer") && isUserAdmin) {

            String[] name = argument.split("_");

            if(name.length < 2) {
                pushForSay("Invalid name", event.getChatID());
                return;
            }

            db.removeUserModer(name[0], name[1] , event.getChatID());
            logger.warn("Removed moder user " + name[0] + " " + name[1] + " in chat " +event.getChatID());

        }



    }

    private void pushForRemove(Integer userID, Integer chatID) {
        outPipe.add(new RemoveUserEvent(chatID, userID));
    }

    private void pushForSay(String message, Integer chatID) {
        outPipe.add(new WriteEvent(message, chatID));
    }

}
