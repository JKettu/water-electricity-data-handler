package file.handling.handler.server.file.creator;

import common.DataFileType;
import common.DataType;
import lombok.val;

import java.io.File;


public class ServerFileCreatorBuilder {
    private String serverFileName;
    private File localFile;
    private DataFileType dataFileType;

    public ServerFileCreatorBuilder setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
        return this;
    }

    public ServerFileCreatorBuilder setLocalFile(File localFile) {
        this.localFile = localFile;
        return this;
    }

    public ServerFileCreatorBuilder setDataFileType(DataFileType dataFileType) {
        this.dataFileType = dataFileType;
        return this;
    }

    public BaseServerFileCreator build(DataType dataType) {
        switch (dataType) {
            case WATER:
                val waterServerFileCreator = new WaterServerFileCreator();
                waterServerFileCreator.setLocalFile(localFile);
                waterServerFileCreator.setServerFileName(serverFileName);
                waterServerFileCreator.setDataFileType(dataFileType);
                return waterServerFileCreator;
            case ELECTRICITY:
                val electricityServerFileCreator = new ElectricityServerFileCreator();
                electricityServerFileCreator.setLocalFile(localFile);
                electricityServerFileCreator.setServerFileName(serverFileName);
                electricityServerFileCreator.setDataFileType(dataFileType);
                return electricityServerFileCreator;

        }
        return null;
    }
}
