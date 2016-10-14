package common.error.info;

import lombok.Builder;

@Builder
public class ErrorInfo {
    private FTPErrorInfo ftpErrorInfo;
    private WorkbookErrorInfo workbookErrorInfo;
    private ErrorType errorType;
}
