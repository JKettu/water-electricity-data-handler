package file.handling.handler.server.file.builder;

import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.model.BaseDataModel;
import lombok.val;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.ByteArrayOutputStream;
import java.util.List;

abstract class BaseServerFileBuilder<DataModelType extends BaseDataModel> {
    protected List<DataModelType> data;

    BaseServerFileBuilder(List<DataModelType> data) {
        this.data = data;
    }

    public abstract ByteArrayOutputStream build();

    //заполнение данных из массива в файл
    protected abstract void addDataToFile(Sheet sheet, CellStyle cellStyle);

    ByteArrayOutputStream writeDataToStream(HSSFWorkbook workbook) {
        ByteArrayOutputStream serverFileDataStream = null;
        val logger = Logger.getLogger(getClass().toString(), "writeDataToStream");
        try {
            serverFileDataStream = new ByteArrayOutputStream();
            workbook.write(serverFileDataStream);
            serverFileDataStream.close();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during writing server file on local machine: " + e);
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
                logger.log(LogCategory.ERROR, "Error during writing server file on local machine: " + e);
            }
        }
        return serverFileDataStream;
    }
}
