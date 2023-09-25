package searchengine.repo;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;
@Repository
public interface PageRepo extends CrudRepository<PageEntity, Integer> {
    @Transactional
    boolean existsBySite_IdAndPath(Integer id, String path);

    @Transactional
    long countBySite_Id(Integer id);


}
