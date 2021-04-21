package iv81.dtbl.noteapp.repositories;

import iv81.dtbl.noteapp.models.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {
    VerificationToken findByToken(String token);
    VerificationToken findByUserId(String userId);
}
