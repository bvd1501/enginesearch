package searchengine.repo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface LemmaRepo extends CrudRepository<LemmaEntity, Integer> {
    Optional<LemmaEntity> findBySite_IdAndLemma(Integer id, String lemma);

    @Modifying
    @Query(value = "insert INTO lemma (site_id, lemma, frequency) " +
            "values (:lemma_site_id, :lemma_lemma, 1) " +
            "on duplicate key update frequency = frequency+1",
            nativeQuery = true)
    void  insertOrUpdate(Integer lemma_site_id, String lemma_lemma);


    @Query("select l from LemmaEntity l where l.id in ?1")
    Set<LemmaEntity> findByIdIn(Collection<Integer> ids);

//    @Override
//    void deleteById(Integer integer);


}
