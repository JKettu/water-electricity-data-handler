package file.handling.handler.server.file.deleter;

import common.DataFileType;
import common.DataType;
import lombok.val;

import java.io.File;


public class ServerFileRegionDeleterBuilder {
    private String serverFileName;
    private File localFile;
    private DataFileType dataFileType;

    public ServerFileRegionDeleterBuilder setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
        return this;
    }

    public ServerFileRegionDeleterBuilder setLocalFile(File localFile) {
        this.localFile = localFile;
        return this;
    }

    public ServerFileRegionDeleterBuilder setDataFileType(DataFileType dataFileType) {
        this.dataFileType = dataFileType;
        return this;
    }

    public BaseServerFileRegionDeleter build(DataType dataType) {
        switch (dataType) {
            case WATER:
                val waterServerFileModifier = new WaterServerFileRegionDeleter();
                waterServerFileModifier.setLocalFile(localFile);
                waterServerFileModifier.setServerFileName(serverFileName);
                waterServerFileModifier.setDataFileType(dataFileType);
                return waterServerFileModifier;
            case ELECTRICITY:
                val electricityServerFileModifier = new ElectricityServerFileRegionDeleter();
                electricityServerFileModifier.setLocalFile(localFile);
                electricityServerFileModifier.setServerFileName(serverFileName);
                electricityServerFileModifier.setDataFileType(dataFileType);
                return electricityServerFileModifier;
        }
        return null;
    }
}
