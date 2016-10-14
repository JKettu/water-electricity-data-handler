package file.handling.parser;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocalFileParseResult {
    private boolean parsedSuccessfully;
    private String errorCellCode;
}
