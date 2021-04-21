package iv81.dtbl.noteapp.services;

import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.models.VerificationToken;
import iv81.dtbl.noteapp.repositories.UserRepository;
import iv81.dtbl.noteapp.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService implements IUserService{

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VerificationTokenRepository tokenRepo;

    @Override
    public User registerUser(User user) {
        return null;
    }

    @Override
    public User getUser(String verificationToken) {
        User user = userRepo.findById(tokenRepo.findByToken(verificationToken).getUserId()).get();
        return user;
    }

    @Override
    public void saveRegisteredUser(User user) {
        userRepo.save(user);
    }

    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken newToken = new VerificationToken(token, user.getId());
        tokenRepo.save(newToken);
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        return tokenRepo.findByToken(token);
    }
}
