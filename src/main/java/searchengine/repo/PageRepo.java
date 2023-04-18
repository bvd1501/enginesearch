package searchengine.repo;

import org.springframework.data.repository.CrudRepository;
import searchengine.model.PageEntity;

public interface PageRepo extends CrudRepository<PageEntity, Integer> {
}
