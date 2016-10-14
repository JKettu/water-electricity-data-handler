package file.handling.parser;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServerFileParseResult {
    private boolean parsedSuccessfully;
    private boolean clientHeadlineNotEqualsToServer;
    private boolean clientRegionAlreadyExistInServerFile;
    private String errorCellCode;
    private List<Integer> serverFileRegions;
}
