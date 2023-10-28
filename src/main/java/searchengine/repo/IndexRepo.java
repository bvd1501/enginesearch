package searchengine.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.Set;

@Repository
public interface IndexRepo extends JpaRepository<IndexEntity, Integer> {

    @Async
    Set<IndexEntity> findByPage_Id(Integer id);


}
