package searchengine.repo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Repository
public interface LemmaRepo extends CrudRepository<LemmaEntity, Integer> {

    @Query("select l from LemmaEntity l where l.site.id = ?1 and l.lemma in ?2")
    Set<LemmaEntity> findBySite_IdAndLemmaIn(Integer id, Collection<String> lemmata);

    @Query("select l from LemmaEntity l where l.id in ?1")
    Set<LemmaEntity> findByIdIn(Collection<Integer> ids);

    @Override
    void deleteById(Integer integer);
}
