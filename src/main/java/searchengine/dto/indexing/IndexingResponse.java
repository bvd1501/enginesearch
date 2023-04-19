package searchengine.dto.indexing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexingResponse {
    private boolean result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public IndexingResponse(boolean result) {
        this.result = result;
    }

}
