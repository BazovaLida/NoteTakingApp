package iv81.dtbl.noteapp.controllers;

import iv81.dtbl.noteapp.events.OnRegistrationCompleteEvent;
import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.models.VerificationToken;
import iv81.dtbl.noteapp.security.DataValidator;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import iv81.dtbl.noteapp.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;


@Controller
@RequestMapping
public class PageController {

    @Autowired
    private IUserService service;

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
