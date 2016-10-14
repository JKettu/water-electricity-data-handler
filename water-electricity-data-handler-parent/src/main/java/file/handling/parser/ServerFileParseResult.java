package file.handling.parser;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServerFileParseResult {
    private boolean parsedSuccessfully;
    private boolean clientHeadlineNotEqualsToServer;
    private String cellCode;
}
