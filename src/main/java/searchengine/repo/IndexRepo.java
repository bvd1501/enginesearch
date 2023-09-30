package searchengine.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.Set;

@Repository
public interface IndexRepo extends CrudRepository<IndexEntity, Integer> {
    @Query("select i from IndexEntity i where i.page.id = ?1")
    Set<IndexEntity> findByPage_Id(Integer id);

}
