package iv81.dtbl.noteapp.security.service;

import iv81.dtbl.noteapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import iv81.dtbl.noteapp.models.User;

import java.util.ArrayList;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private final PasswordEncoder passwordEncoder;


    public AppUserDetailsService(PasswordEncoder pE) {
        this.passwordEncoder = pE;
    }

    public User findUserByEmail(String email) { return userRepo.findByEmail(email); }

    public void saveUser(User user) {
        user.setPassHash(passwordEncoder.encode(user.getPassHash()));
        userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email);
        if(user != null) {
            UserDetails userDetails = buildUserForAuthentication(user, new ArrayList<GrantedAuthority>());
            return userDetails;
        } else {
            throw new UsernameNotFoundException("username not found");
        }
    }

    private UserDetails buildUserForAuthentication(User user, ArrayList<GrantedAuthority> grantedAuthorities) {
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassHash(), user.isEnabled(), true, true, true, grantedAuthorities);
    }
}
