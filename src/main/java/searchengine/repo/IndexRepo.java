package searchengine.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

@Repository
public interface IndexRepo extends CrudRepository<IndexEntity, Integer> {

}
