package file.handling.parser.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CellParseException extends RuntimeException {
    private String cellCode;
}
