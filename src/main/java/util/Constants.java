package main.java.util;

import java.util.Objects;

public class Constants {
    public static final String USERS_FILE_PATH = Objects.isNull(System.getenv("IS_DEV"))? "/mnt/resources/usersData.txt" : "resources/usersData.txt";
    public static final String CONTENT_FILE_PATH = Objects.isNull(System.getenv("IS_DEV"))? "/mnt/resources/contentData.txt" :"resources/contentData.txt";
}