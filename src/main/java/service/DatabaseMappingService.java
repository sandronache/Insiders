//package main.java.service;
//
//import main.java.logger.LoggerFacade;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.UUID;
//
///**
// * Service responsible for managing mappings between interface IDs and database IDs
// */
//@Service
//public class DatabaseMappingService {
//    private static DatabaseMappingService instance;
//
//    // Mapare intre ID-urile reale din baza de date pentru postari (doar UUID)
//    private final HashMap<UUID, UUID> postInterfaceToDbIdMap = new HashMap<>();
//
//    private DatabaseMappingService() {}
//
//    // Post mapping methods
//    public void storePostMapping(UUID interfaceId, UUID databaseId) {
//        postInterfaceToDbIdMap.put(interfaceId, databaseId);
//        LoggerFacade.debug("Post mapping stored: " + interfaceId + " -> " + databaseId);
//    }
//
//}
