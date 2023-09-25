package searchengine.repo;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;

@Repository
public interface PageRepo extends CrudRepository<PageEntity, Integer> {
    @Transactional
    boolean existsBySite_IdAndPath(Integer id, String path);

    @Transactional
    long countBySite_Id(Integer id);


}
