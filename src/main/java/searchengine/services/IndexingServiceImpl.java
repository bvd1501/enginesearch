package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    @Override
    public IndexingResponse getStartIndexing() {

        return null;
    }

    @Override
    public IndexingResponse getStopIndexing() {
        return null;
    }
}
