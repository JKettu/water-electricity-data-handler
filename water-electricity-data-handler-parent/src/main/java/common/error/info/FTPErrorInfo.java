package common.error.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import server.connector.ftp.FTPErrorCode;

@Getter
@AllArgsConstructor
public class FTPErrorInfo {
    private FTPErrorCode ftpErrorCode;
}
