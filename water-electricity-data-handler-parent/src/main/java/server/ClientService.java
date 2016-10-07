package server;

import common.logger.LogCategory;
import common.logger.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

    private static final String CLIENT_ID_FILE_NAME = "id.txt";
    public static Integer CLIENT_ID = 1;

    public static void registerClient() {
        FTPController ftpController = new FTPController();
        Logger logger = Logger.getLogger(ClientService.class.toString(),"registerClient");
        try {
            InputStream inputStream = ftpController.getInputFileStream(CLIENT_ID_FILE_NAME);
            if(inputStream == null){
                return;
            }
            List<Integer> clientIds = readClientIds(inputStream);
            if (clientIds.isEmpty()) {
                clientIds.add(1);
            } else {
                int lastClientId = clientIds.get(clientIds.size() - 1);
                clientIds.add(lastClientId + 1);
                CLIENT_ID = lastClientId + 1;
            }
            OutputStream clientIdsOs = new ByteArrayOutputStream();
            writeClientIds(clientIds, clientIdsOs);
            InputStream clientIdsIs = new ByteArrayInputStream(((ByteArrayOutputStream) clientIdsOs).toByteArray());
            ftpController.sendFile(clientIdsIs, CLIENT_ID_FILE_NAME);
            logger.log(LogCategory.INFO, "Register client with id = '" + CLIENT_ID + "'");
        } catch (IOException e) {
            logger.log(LogCategory.ERROR, "Register client error: " + e);
        }
    }

    public static void unregisterClient() {
        Logger logger = Logger.getLogger(ClientService.class.toString(),"unregisterClient");
        FTPController ftpController = new FTPController();
        try {
            InputStream inputStream = ftpController.getInputFileStream(CLIENT_ID_FILE_NAME);
            if(inputStream == null){
                return;
            }
            List<Integer> clientIds = readClientIds(inputStream);
            if (clientIds.isEmpty()) {
                return;
            } else {
                clientIds.remove(CLIENT_ID);
            }
            OutputStream clientIdsOs = new ByteArrayOutputStream();
            writeClientIds(clientIds, clientIdsOs);
            InputStream clientIdsIs = new ByteArrayInputStream(((ByteArrayOutputStream) clientIdsOs).toByteArray());
            ftpController.sendFile(clientIdsIs, CLIENT_ID_FILE_NAME);
            logger.log(LogCategory.INFO, "Unregister client with id = '" + CLIENT_ID + "'");
        } catch (IOException e) {
            logger.log(LogCategory.ERROR, "Unregister client error: " + e);
        }
    }

    private static List<Integer> readClientIds(InputStream inputStream) {
        Logger logger = Logger.getLogger(ClientService.class.toString(), "readClientIds");
        List<Integer> ids = new ArrayList<>();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        try {
            while (dataInputStream.available() > 0) {
                int id = dataInputStream.readInt();
                ids.add(id);
            }
            dataInputStream.close();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during read client ids: " + e);
        }
        return ids;
    }

    private static void writeClientIds(List<Integer> ids, OutputStream clientIdsOs) {
        Logger logger = Logger.getLogger(ClientService.class.toString(), "writeClientIds");
        DataOutputStream dataOutputStream = new DataOutputStream(clientIdsOs);
        try {
            for (int id : ids) {
                dataOutputStream.writeInt(id);
            }
            dataOutputStream.close();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during write client ids: " + e);

        }
    }

}
