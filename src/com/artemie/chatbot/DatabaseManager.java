package com.artemie.chatbot;

import com.artemie.chatbot.dialog.DialogType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

/**
 * Created by Тема on 30.10.2016.
 */
public class DatabaseManager {

    private static Connection db;
    private static DatabaseManager instance = null;

    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

    private DatabaseManager (){
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }

        try {
            db = DriverManager.getConnection(PublicProperties.getDBPath());
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static DatabaseManager getInstance() {

        if(instance == null) {
            instance = new DatabaseManager();
        }

        return instance;

    }

    public static void close() {

        try {
            db.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } catch (NullPointerException e) {
            logger.error("Database wasn`t opend");
        }

    }

    public Boolean isConnected() {

        try {
            return db.isValid(10);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return false;

    }

    public ArrayList<Integer> getModeratedChats() {

        ArrayList<Integer> result = null;

        try {
            Statement statement = db.createStatement();

            String query = "SELECT CHAT_ID_VK FROM MODERATED_CHATS";

            ResultSet resultSet = statement.executeQuery(query);

            result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(resultSet.getInt("CHAT_ID_VK"));
            }

            statement.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return result;

    }

    public void addModeratedChat(Integer chatId) {

        try {
            Statement statement = db.createStatement();

            String update = "INSERT INTO MODERATED_CHATS (CHAT_ID_VK) VALUES (" + chatId + ")";

            statement.executeUpdate(update);

            statement.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

    }

    public DialogType getDialogType(Integer chatID) {

        try {
            Statement statement = db.createStatement();

            String query = "SELECT IS_WHITELIST FROM MODERATED_CHATS WHERE CHAT_ID_VK = " + chatID;

            ResultSet resultSet = statement.executeQuery(query);

            statement.close();

            if(resultSet.getBoolean("IS_WHITELIST")) {
                return DialogType.WHITE_LIST;
            }else {
                return DialogType.BLACK_LIST;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return null;

    }

    public ArrayList<Integer> getUsersBanned(Integer chatID) {

        ArrayList<Integer> result = null;

        try {
            Statement statement = db.createStatement();

            String query = "SELECT USER_ID_VK FROM (SELECT * FROM MODERATED_CHATS LEFT JOIN  USER_STATE ON ID = CHAT_ID WHERE STATE = 0 AND CHAT_ID_VK = " + chatID + ") LEFT JOIN MODERATED_USERS ON USER_ID = ID";

            ResultSet resultSet = statement.executeQuery(query);

            result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(resultSet.getInt("USER_ID_VK"));
            }

            statement.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return result;

    }

    public void addUserBanned(Integer userID, Integer chatID, String firstName, String lastName) {

        firstName = prepareInput(firstName);
        lastName = prepareInput(lastName);

        try {

            Statement statement = db.createStatement();

            String query = "SELECT ID FROM MODERATED_USERS WHERE USER_ID_VK = " + userID;

            ResultSet resultSet = statement.executeQuery(query);

            if(!resultSet.next()) {

                query = "INSERT INTO MODERATED_USERS (USER_ID_VK, FIRST_NAME, LAST_NAME) VALUES (" + userID + ",'" + firstName+ "','" + lastName + "')";
                statement.executeUpdate(query);

            }

            query = "INSERT INTO USER_STATE (CHAT_ID, STATE, USER_ID) VALUES ((SELECT ID FROM MODERATED_CHATS WHERE CHAT_ID_VK = " +
                    chatID + "), 0, (SELECT ID FROM MODERATED_USERS WHERE  USER_ID_VK = " + userID + "))";

            statement.executeUpdate(query);
            statement.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

    }

    public void removeUserBanned(Integer userID, Integer chatID) {

        try {
            Statement statement = db.createStatement();

            String query = "DELETE FROM USER_STATE WHERE USER_ID = (SELECT ID FROM MODERATED_USERS WHERE USER_ID_VK = " + userID + ") AND CHAT_ID = (SELECT ID FROM MODERATED_CHATS WHERE CHAT_ID_VK = " + chatID + ")";

            statement.executeUpdate(query);

            statement.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

    }

    public void removeUserBanned(String firstName, String lastName, Integer chatID) {

        firstName = prepareInput(firstName);
        lastName = prepareInput(lastName);

        try {
            Statement statement = db.createStatement();

            String query = "DELETE FROM USER_STATE WHERE USER_ID = (SELECT ID FROM MODERATED_USERS WHERE FIRST_NAME = '" + firstName + "' AND LAST_NAME = '" + lastName + "') AND CHAT_ID = (SELECT ID FROM MODERATED_CHATS WHERE CHAT_ID_VK = " + chatID + ") AND STATE = 0";

            statement.executeUpdate(query);

            statement.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

    }

    public Boolean isUserAdmin(Integer userID, Integer chatID) {

        try {

            Statement statement = db.createStatement();

            String query = "SELECT USER_ID_VK FROM (SELECT * FROM MODERATED_CHATS LEFT JOIN  USER_STATE ON ID = CHAT_ID WHERE STATE = 2 AND CHAT_ID_VK = " + chatID + ") LEFT JOIN MODERATED_USERS ON USER_ID = ID";

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                if(resultSet.getInt("USER_ID_VK") == userID)
                    return true;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return false;

    }

    public Boolean isUserModer(Integer userID, Integer chatID) {

        try {

            Statement statement = db.createStatement();

            String query = "SELECT USER_ID_VK FROM (SELECT * FROM MODERATED_CHATS LEFT JOIN  USER_STATE ON ID = CHAT_ID WHERE (STATE = 1 OR STATE = 2) AND CHAT_ID_VK = " + chatID + ") LEFT JOIN MODERATED_USERS ON USER_ID = ID";

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                if(resultSet.getInt("USER_ID_VK") == userID)
                    return true;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return false;

    }

    public Boolean isUserBanned(Integer userID, Integer chatID) {

        try {

            Statement statement = db.createStatement();

            String query = "SELECT USER_ID_VK FROM (SELECT * FROM MODERATED_CHATS LEFT JOIN  USER_STATE ON ID = CHAT_ID WHERE (STATE = 0 OR STATE = 2) AND CHAT_ID_VK = " + chatID + ") LEFT JOIN MODERATED_USERS ON USER_ID = ID";

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                if(resultSet.getInt("USER_ID_VK") == userID)
                    return true;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return false;

    }

    public void addUserModer(Integer userID, Integer chatID, String firstName, String lastName) {

        firstName = prepareInput(firstName);
        lastName = prepareInput(lastName);

        try {

            Statement statement = db.createStatement();

            String query = "SELECT ID FROM MODERATED_USERS WHERE USER_ID_VK = " + userID;

            ResultSet resultSet = statement.executeQuery(query);

            if(!resultSet.next()) {

                query = "INSERT INTO MODERATED_USERS (USER_ID_VK, FIRST_NAME, LAST_NAME) VALUES (" + userID + ",'" + firstName+ "','" + lastName + "')";
                statement.executeUpdate(query);

            }

            query = "INSERT INTO USER_STATE (CHAT_ID, STATE, USER_ID) VALUES ((SELECT ID FROM MODERATED_CHATS WHERE CHAT_ID_VK = " +
                    chatID + "), 1, (SELECT ID FROM MODERATED_USERS WHERE  USER_ID_VK = " + userID + "))";

            statement.executeUpdate(query);
            statement.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

    }

    public void removeUserModer(String firstName, String lastName, Integer chatID) {

        firstName = prepareInput(firstName);
        lastName = prepareInput(lastName);

        try {

            Statement statement = db.createStatement();

            String query = "DELETE FROM USER_STATE WHERE USER_ID = (SELECT ID FROM MODERATED_USERS WHERE FIRST_NAME = '" + firstName + "' AND LAST_NAME = '" + lastName + "') AND CHAT_ID = (SELECT ID FROM MODERATED_CHATS WHERE CHAT_ID_VK = " + chatID + ") AND STATE = 1";

            statement.executeUpdate(query);

            statement.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

    }

    private String prepareInput(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int len = input.length();

        final StringBuilder result = new StringBuilder(len + len / 4);
        final StringCharacterIterator iterator = new StringCharacterIterator(input);
        char ch = iterator.current();

        while (ch != CharacterIterator.DONE) {
            if (ch == '\n') {
                result.append("\\n");
            } else if (ch == '\r') {
                result.append("\\r");
            } else if (ch == '\'') {
                result.append("\\\'");
            } else if (ch == '"') {
                result.append("\\\"");
            } else {
                result.append(ch);
            }
            ch = iterator.next();
        }

        return result.toString();
    }

}
