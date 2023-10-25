package searchengine.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.Collection;
import java.util.Set;


@Repository
public interface LemmaRepo extends CrudRepository<LemmaEntity, Integer> {
    long countBySite(SiteEntity site);
    LemmaEntity findBySiteAndLemma(SiteEntity site, String lemma);

    @Modifying
    @Query(value = "insert INTO lemma (site_id, lemma, frequency) " +
            "values (:#{#lemmaEnt.site.id}, :#{#lemmaEnt.lemma}, 1) " +
            "on duplicate key update frequency = frequency+1",
            nativeQuery = true)
    void   insertOrUpdate( @Param("lemmaEnt") LemmaEntity lemmaEntity);

    @Query("select l from LemmaEntity l where l.id in ?1")
    Set<LemmaEntity> findByIdIn(Collection<Integer> ids);




}
