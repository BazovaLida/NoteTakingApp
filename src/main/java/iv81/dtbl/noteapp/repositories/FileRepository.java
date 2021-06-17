package iv81.dtbl.noteapp.repositories;

import iv81.dtbl.noteapp.models.File;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileRepository extends MongoRepository<File, String> {
    public List<File> findAllByAuthorId(String uid);
}
