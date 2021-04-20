package iv81.dtbl.noteapp.repositories;

import iv81.dtbl.noteapp.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, String> {
    public User findByEmail(String email);

}
