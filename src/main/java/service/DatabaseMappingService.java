package main.java.service;

import main.java.logger.LoggerFacade;

import java.util.HashMap;

/**
 * Service responsible for managing mappings between interface IDs and database IDs
 */
public class DatabaseMappingService {
    private static DatabaseMappingService instance;

    // Mapare intre ID-urile din interfată si ID-urile reale din baza de date pentru postari
    private final HashMap<Integer, Integer> postInterfaceToDbIdMap = new HashMap<>();

    // Mapare pentru comentarii: cheia este "postId_commentId", valoarea este DB comment ID
    private final HashMap<String, Integer> commentInterfaceToDbIdMap = new HashMap<>();

    private DatabaseMappingService() {}

    public static DatabaseMappingService getInstance() {
        if (instance == null) {
            instance = new DatabaseMappingService();
        }
        return instance;
    }

    // Post mapping methods
    public void storePostMapping(Integer interfaceId, Integer databaseId) {
        postInterfaceToDbIdMap.put(interfaceId, databaseId);
        LoggerFacade.debug("Post mapping stored: " + interfaceId + " -> " + databaseId);
    }

    public Integer getPostDatabaseId(Integer interfaceId) {
        return postInterfaceToDbIdMap.get(interfaceId);
    }

    // Comment mapping methods
    public void storeCommentMapping(Integer postId, Integer commentId, Integer databaseId) {
        String commentKey = postId + "_" + commentId;
        commentInterfaceToDbIdMap.put(commentKey, databaseId);
        LoggerFacade.debug("Comment mapping stored: " + commentKey + " -> " + databaseId);
    }

    public Integer getCommentDatabaseId(Integer postId, Integer commentId) {
        String commentKey = postId + "_" + commentId;
        return commentInterfaceToDbIdMap.get(commentKey);
    }

    // Cleanup methods
    public void clearMappings() {
        postInterfaceToDbIdMap.clear();
        commentInterfaceToDbIdMap.clear();
        LoggerFacade.info("All database mappings cleared");
    }
}
