package file.handling.handler.server.file.modifier;

import common.DataFileType;
import common.DataType;
import lombok.val;

import java.io.File;


public class ServerFileModifierBuilder {
    private String serverFileName;
    private File localFile;
    private DataFileType dataFileType;

    public ServerFileModifierBuilder setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
        return this;
    }

    public ServerFileModifierBuilder setLocalFile(File localFile) {
        this.localFile = localFile;
        return this;
    }

    public ServerFileModifierBuilder setDataFileType(DataFileType dataFileType) {
        this.dataFileType = dataFileType;
        return this;
    }

    public BaseServerFileModifier build(DataType dataType) {
        switch (dataType) {
            case WATER:
                val waterServerFileModifier = new WaterServerFileModifier();
                waterServerFileModifier.setLocalFile(localFile);
                waterServerFileModifier.setServerFileName(serverFileName);
                waterServerFileModifier.setDataFileType(dataFileType);
                return waterServerFileModifier;
            case ELECTRICITY:
                val electricityServerFileModifier = new ElectricityServerFileModifier();
                electricityServerFileModifier.setLocalFile(localFile);
                electricityServerFileModifier.setServerFileName(serverFileName);
                electricityServerFileModifier.setDataFileType(dataFileType);
                return electricityServerFileModifier;
        }
        return null;
    }
}
