package file.handling.handler.server.file.deleter;

import common.DataType;
import lombok.val;


public class ServerFileRegionDeleterBuilder {
    private String serverFileName;
    private int region;


    public ServerFileRegionDeleterBuilder setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
        return this;
    }

    public ServerFileRegionDeleterBuilder setRegion(int region) {
        this.region = region;
        return this;
    }

    public BaseServerFileRegionDeleter build(DataType dataType) {
        switch (dataType) {
            case WATER:
                val waterServerFileRegionDeleter = new WaterServerFileRegionDeleter();
                waterServerFileRegionDeleter.setServerFileName(serverFileName);
                waterServerFileRegionDeleter.setRegionToDelete(region);
                return waterServerFileRegionDeleter;
            case ELECTRICITY:
                val electricityServerFileRegionDeleter = new ElectricityServerFileRegionDeleter();
                electricityServerFileRegionDeleter.setServerFileName(serverFileName);
                electricityServerFileRegionDeleter.setRegionToDelete(region);
                return electricityServerFileRegionDeleter;
        }
        return null;
    }
}
