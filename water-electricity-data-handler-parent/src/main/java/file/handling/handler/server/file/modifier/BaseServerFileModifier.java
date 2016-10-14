package file.handling.handler.server.file.modifier;

import common.DataFileType;
import common.error.info.ErrorInfo;
import common.error.info.ErrorType;
import common.error.info.FTPErrorInfo;
import common.error.info.WorkbookErrorInfo;
import file.handling.parser.ServerFileParseResult;
import lombok.Data;
import lombok.val;
import server.connector.ftp.FTPConnector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

@Data
public abstract class BaseServerFileModifier {
    protected String serverFileName;
    protected File localFile;
    protected DataFileType dataFileType;

    public abstract ErrorInfo modifyServerFile();

    protected ErrorInfo createErrorInfo(ServerFileParseResult serverFileParseResult) {
        if (serverFileParseResult.isClientHeadlineNotEqualsToServer()) {
            return ErrorInfo.builder()
                    .errorType(ErrorType.SERVER_FILE_AND_LOCAL_FILE_STRUCTURE_NOT_EQUALS)
                    .build();
        }
        if (serverFileParseResult.isClientRegionAlreadyExistInServerFile()) {
            return ErrorInfo.builder()
                    .errorType(ErrorType.CLIENT_REGION_ALREADY_IN_SERVER_FILE)
                    .build();
        }
        return ErrorInfo.builder()
                .errorType(ErrorType.WORKBOOK_SERVER_FILE_READING_ERROR)
                .workbookErrorInfo(new WorkbookErrorInfo(serverFileParseResult.getErrorCellCode()))
                .build();
    }

    protected ErrorInfo writeServerFileDataToServer(ByteArrayOutputStream serverFileData) {
        if (serverFileData == null) {
            return ErrorInfo.builder()
                    .errorType(ErrorType.WORKBOOK_SERVER_FILE_WRITING_ERROR)
                    .build();
        }
        val inputStream = new ByteArrayInputStream(serverFileData.toByteArray());
        val ftpConnector = new FTPConnector();
        if (!ftpConnector.sendFile(inputStream, serverFileName)) {
            return ErrorInfo.builder()
                    .errorType(ErrorType.FTP_ERROR)
                    .ftpErrorInfo(new FTPErrorInfo(ftpConnector.getFtpErrorCode()))
                    .build();
        }
        return null;
    }
}
