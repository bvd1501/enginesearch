package searchengine.repo;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;

import java.util.Optional;

@Repository
public interface PageRepo extends CrudRepository<PageEntity, Integer> {
    @Query("select p from PageEntity p where p.site.id = ?1 and p.path = ?2")
    Optional<PageEntity> findBySite_IdAndPath(Integer id, String path);
    @Transactional
    boolean existsBySite_IdAndPath(Integer id, String path);

    @Transactional
    long countBySite_Id(Integer id);


    @Override
    void deleteById(Integer integer);
}
