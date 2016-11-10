package com.artemie.chatbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Тема on 30.10.2016.
 */
public class PublicProperties {

    private static Properties properties;
    private static Properties login;

    private static final Logger logger = LogManager.getLogger(PublicProperties.class);

    public static void init (String path, String loginPath) {
        properties = new Properties();
        login = new Properties();

        try {

            //System.out.println(new File(".").getCanonicalPath() + File.);

            //InputStream fileInputStream = PublicProperties.class.getClassLoader().getResourceAsStream(path);

            InputStream fileInputStream = new FileInputStream(new File(".").getCanonicalPath() + File.separator + path);
            properties.load(fileInputStream);

            //fileInputStream = PublicProperties.class.getClassLoader().getResourceAsStream(loginPath);

            fileInputStream = new FileInputStream(new File(".").getCanonicalPath() + File.separator + loginPath);
            login.load(fileInputStream);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static String getCode() {
        return login.getProperty("CODE");
    }

    public static Integer getUserId() {
        //System.out.println(login.getProperty("USER_ID"));
        return Integer.valueOf(login.getProperty("USER_ID"));
    }

    public static String getDBPath() { //TODO: different path based on OS
        return properties.getProperty("DB_PATH");
    }

    public static Float getUsersPollFrequency() {
        return Float.valueOf(properties.getProperty("USERS_POLL_FREQUENCY"));
    }

    public static Float getCommandsPollFrequency() {
        return Float.valueOf(properties.getProperty("COMMANDS_POLL_FREQUENCY"));
    }

}
