package iv81.dtbl.noteapp.services;

import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.models.VerificationToken;

public interface IUserService {
    User registerUser(User user);
    User getUser(String verificationToken);
    void saveRegisteredUser(User user);
    void createVerificationToken(User user, String token);
    VerificationToken getVerificationToken(String token);
}
