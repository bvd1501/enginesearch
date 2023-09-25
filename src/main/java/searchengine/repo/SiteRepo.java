package searchengine.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

import java.util.Date;


@Repository
public interface SiteRepo extends CrudRepository<SiteEntity, Integer> {
    @Modifying
    @Query("update SiteEntity s set s.statusTime = :statusTime where s.id = :id")
    @Transactional
    void updateStatusTimeById(@Param("statusTime") Date statusTime, @Param("id") Integer id);


    @Modifying
    @Transactional
    void deleteByUrlAndName(String url, String name);

//    @Modifying
//    @Query("delete from SiteEntity s where s.url = :url and s.name = :name")
//    void deleteByUrlAndName(String url, String name);


}
