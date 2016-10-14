package common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataFileType {
    XLS(".xls"),
    XSLX(".xlsx");

    private final String fileType;
}
