package server.connector;

import common.logger.LogCategory;
import common.logger.Logger;
import lombok.val;
import server.connector.ftp.FTPConnector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

    private static final String CLIENT_ID_FILE_NAME = "id.txt";
    public static int CLIENT_ID = 1;

    public static void registerClient() {
        val ftpController = new FTPConnector();
        val logger = Logger.getLogger(ClientService.class.toString(), "registerClient");

        val inputStream = ftpController.getInputFileStream(CLIENT_ID_FILE_NAME);
        if (inputStream == null) {
            return;
        }
        val clientIds = readClientIds(inputStream);
        if (clientIds.isEmpty()) {
            clientIds.add(1);
        } else {
            val lastClientId = clientIds.get(clientIds.size() - 1);
            clientIds.add(lastClientId + 1);
            CLIENT_ID = lastClientId + 1;
        }
        val clientIdsOs = new ByteArrayOutputStream();
        writeClientIds(clientIds, clientIdsOs);
        val clientIdsIs = new ByteArrayInputStream(clientIdsOs.toByteArray());
        ftpController.sendFile(clientIdsIs, CLIENT_ID_FILE_NAME);
        logger.log(LogCategory.INFO, "Register client with id = '" + CLIENT_ID + "'");

    }

    public static void unregisterClient() {
        val logger = Logger.getLogger(ClientService.class.toString(), "unregisterClient");
        val ftpController = new FTPConnector();
        val inputStream = ftpController.getInputFileStream(CLIENT_ID_FILE_NAME);
        if (inputStream == null) {
            return;
        }
        val clientIds = readClientIds(inputStream);
        if (clientIds.isEmpty()) {
            return;
        } else {
            clientIds.remove(CLIENT_ID);
        }
        val clientIdsOs = new ByteArrayOutputStream();
        writeClientIds(clientIds, clientIdsOs);
        val clientIdsIs = new ByteArrayInputStream(clientIdsOs.toByteArray());
        ftpController.sendFile(clientIdsIs, CLIENT_ID_FILE_NAME);
        logger.log(LogCategory.INFO, "Unregister client with id = '" + CLIENT_ID + "'");

    }


    private static List<Integer> readClientIds(InputStream inputStream) {
        val logger = Logger.getLogger(ClientService.class.toString(), "readClientIds");
        List<Integer> ids = new ArrayList<>();
        val dataInputStream = new DataInputStream(inputStream);
        try {
            while (dataInputStream.available() > 0) {
                val id = dataInputStream.readInt();
                ids.add(id);
            }
            dataInputStream.close();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during read client ids: " + e);
        }
        return ids;
    }

    private static void writeClientIds(List<Integer> ids, OutputStream clientIdsOs) {
        val logger = Logger.getLogger(ClientService.class.toString(), "writeClientIds");
        val dataOutputStream = new DataOutputStream(clientIdsOs);
        try {
            for (val id : ids) {
                dataOutputStream.writeInt(id);
            }
            dataOutputStream.close();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during write client ids: " + e);

        }
    }

}
