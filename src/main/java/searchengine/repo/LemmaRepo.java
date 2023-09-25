package searchengine.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;


@Repository
public interface LemmaRepo extends CrudRepository<LemmaEntity, Integer> {
}
