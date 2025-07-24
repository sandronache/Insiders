package main.java.service;

import main.java.logger.LoggerFacade;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Service responsible for managing mappings between interface IDs and database IDs
 */
@Service
public class DatabaseMappingService {
    private static DatabaseMappingService instance;

    // Mapare intre ID-urile din interfată si ID-urile reale din baza de date pentru postari
    private final HashMap<Integer, Integer> postInterfaceToDbIdMap = new HashMap<>();

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

}
