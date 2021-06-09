package iv81.dtbl.noteapp.controllers;

import iv81.dtbl.noteapp.email.events.pswdreset.OnPasswordResetEvent;
import iv81.dtbl.noteapp.email.events.registration.OnRegistrationCompleteEvent;
import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.models.VerificationToken;
import iv81.dtbl.noteapp.repositories.UserRepository;
import iv81.dtbl.noteapp.security.DataValidator;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import iv81.dtbl.noteapp.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Optional;


@Controller
@RequestMapping
public class PageController {

    @Autowired
    private IUserService service;
    @Autowired
    private UserRepository userRepo;

    private DataValidator dataValidator = new DataValidator();

    @Autowired
    private AppUserDetailsService userService;
    @Autowired
    ApplicationEventPublisher eventPublisher;

    @GetMapping("/")
    public String index() { return "home_page.html"; }

    @GetMapping("/login")
    public String login() { return "login_form.html"; }

    @GetMapping("/register")
    public String register() { return "registration_form.html"; }

    @GetMapping("/pswd_reset")
    public String resetPswd() { return "pswd_reset_req.html"; }

    @GetMapping("/pswd_reset/{id}/passwordResetConfirm")
    public String resetPswdPage(@PathVariable String id, @RequestParam String token) {
        VerificationToken verificationToken = service.getVerificationToken(token);
        if(verificationToken == null) {
            return "forward:/bad_token.html";
        }
        User user = service.getUser(token);
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return "forward:/token_expired.html";
        }
        return "forward:/pswd_reset_form.html";
    }

    @PostMapping("/register")
    @ResponseBody
    public RedirectView log_res(@RequestParam String email, @RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        User existing = userService.findUserByEmail(email);
        if (existing == null) {
            if (dataValidator.emailPswdIsValid(email, password)) {
                User newUser = new User(username, email, password);
                userService.saveUser(newUser);
                String appUrl = request.getContextPath();
                eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, request.getLocale(), appUrl));
                return new RedirectView("login_form");
            }
        }
        return new RedirectView("err");
    }

    @PostMapping("/pswd_reset/{id}/passwordResetConfirm")
    @ResponseBody
    public RedirectView pswd_reset_apply(@PathVariable String id, @RequestParam String password, HttpServletRequest request) {
        Optional<User> existing = userRepo.findById(id);
        RedirectView redirectView = new RedirectView();
        if (existing.get() == null) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        } else {
            User usertoUpdate = existing.get();
            usertoUpdate.setPassHash(password);
            userService.saveUser(usertoUpdate);

        }
        redirectView.setUrl("http://localhost:8088/login");
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("/pswd_reset")
    @ResponseBody
    public RedirectView pswd_reset(@RequestParam String email, HttpServletRequest request) {
        User existing = userService.findUserByEmail(email);
        if (existing == null) {
            return new RedirectView("pswd_reset?err");
        }
        eventPublisher.publishEvent(new OnPasswordResetEvent(existing, request.getLocale(), request.getContextPath()));
        return new RedirectView("login_form");
    }

    @RequestMapping("/loggedin")
    public String logged_in() {
        return "logged_in.html";
    }
    @GetMapping("/err")
    public String errPage() {
        return "err.html";
    }

    @GetMapping("/registrationConfirm")
    public RedirectView confirmRegistration(@RequestParam String token) {
        VerificationToken verificationToken = service.getVerificationToken(token);
        if(verificationToken == null) {
            return new RedirectView("bad_token");
        }
        User user = service.getUser(token);
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return new RedirectView("token_expired");
        }
        user.setEnabled(true);
        service.saveRegisteredUser(user);
        return new RedirectView("login");
    }


}
