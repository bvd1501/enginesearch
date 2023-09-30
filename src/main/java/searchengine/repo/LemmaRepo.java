package searchengine.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

import java.util.Collection;
import java.util.Set;


@Repository
public interface LemmaRepo extends CrudRepository<LemmaEntity, Integer> {
    @Query("select l from LemmaEntity l where l.id in ?1")
    Set<LemmaEntity> findByIdIn(Collection<Integer> ids);

    @Override
    void deleteById(Integer integer);
}
