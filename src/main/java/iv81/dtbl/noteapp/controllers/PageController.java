package iv81.dtbl.noteapp.controllers;

import iv81.dtbl.noteapp.email.events.pswdreset.OnPasswordResetEvent;
import iv81.dtbl.noteapp.email.events.registration.OnRegistrationCompleteEvent;
import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.models.VerificationToken;
import iv81.dtbl.noteapp.repositories.FileRepository;
import iv81.dtbl.noteapp.repositories.UserRepository;
import iv81.dtbl.noteapp.security.DataValidator;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import iv81.dtbl.noteapp.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Optional;


@Controller
@RequestMapping
public class PageController {

    @Autowired
    private IUserService service;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private FileRepository fileRepo;
    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private DataValidator dataValidator = new DataValidator();

    @Autowired
    private AppUserDetailsService userService;
    @Autowired
    ApplicationEventPublisher eventPublisher;

    @GetMapping("/")
    public RedirectView index(HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (sessionInfo != null) {
            String email = sessionInfo.getPrincipal().toString();
            if (dataValidator.emailIsValid(email)) {
                User userFound = userRepo.findByEmail(email);
                if (userFound != null) {
                    redirectView.setUrl("http://localhost:8088/loggedin/" + userFound.getId());
                } else {
                    redirectView.setUrl("http://localhost:8088/home");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/home");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/home");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping ("/home")
    public String home() {
        return "home_page";
    }

    @GetMapping("/login")
    public String login() { return "login_form"; }

    @GetMapping("/register")
    public String register() { return "registration_form"; }

    @GetMapping("/pswd_reset")
    public String resetPswd() { return "pswd_reset_req"; }

    @GetMapping("/pswd_reset/{id}/passwordResetConfirm")
    public String resetPswdPage(@PathVariable String id, @RequestParam String token, Model model) {
        VerificationToken verificationToken = service.getVerificationToken(token);
        if(verificationToken == null) {
            model.addAttribute("msg", "Bad token. Please, make sure to use the appropriate link.");
            model.addAttribute("loggedin", false);
            return "errorpage";
        }
        User user = service.getUser(token);
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            model.addAttribute("msg", "Token expired. Please, request a new link.");
            model.addAttribute("loggedin", false);
            return "errorpage";
        }
        return "pswd_reset_form";
    }

    @PostMapping("/register")
    @ResponseBody
    public RedirectView log_res(@RequestParam String email, @RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        User existing = userService.findUserByEmail(email);
        RedirectView redirectView = new RedirectView();
        if (existing == null) {
            if (dataValidator.emailPswdIsValid(email, password)) {
                User newUser = new User(username, email, password);
                userService.saveUser(newUser);
                String appUrl = request.getContextPath();
                eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, request.getLocale(), appUrl));
                redirectView.setUrl("http://localhost:8088/login");
                redirectView.setHosts();
                return redirectView;
            } else {
                redirectView.setUrl("http://localhost:8088/register?error");
                redirectView.setHosts();
                return redirectView;
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err?msg=userExists");
            redirectView.setHosts();
            return redirectView;
        }
    }

    @PostMapping("/pswd_reset/{id}/passwordResetConfirm")
    @ResponseBody
    public RedirectView pswd_reset_apply(@PathVariable String id, @RequestParam String password, HttpServletRequest request) {
        Optional<User> existing = userRepo.findById(id);
        RedirectView redirectView = new RedirectView();
        if (existing.get() == null) {
            redirectView.setUrl("http://localhost:8088/err?msg=userNotFound");
            redirectView.setHosts();
            return redirectView;
        } else {
            User usertoUpdate = existing.get();
            if (dataValidator.pswdIsValid(password)) {
                usertoUpdate.setPassHash(password);
                usertoUpdate.setEnabled(true);
                userService.saveUser(usertoUpdate);
            }
        }
        redirectView.setUrl("http://localhost:8088/login");
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("/pswd_reset")
    @ResponseBody
    public RedirectView pswd_reset(@RequestParam String email, HttpServletRequest request) {
        User existing = userService.findUserByEmail(email);
        RedirectView redirectView = new RedirectView();
        if (existing == null) {
            redirectView.setUrl("http://localhost:8088/err?msg=userNotFound");
            redirectView.setHosts();
            return redirectView;
        }
        eventPublisher.publishEvent(new OnPasswordResetEvent(existing, request.getLocale(), request.getContextPath()));
        existing.setEnabled(false);
        userRepo.save(existing);
        redirectView.setUrl("http://localhost:8088/login");
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping("/loggedin")
    public RedirectView logged_in(HttpServletRequest request) {
        RedirectView result = new RedirectView();
        SecurityContext sc = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        Object pr = sc.getAuthentication().getPrincipal();
        if (pr instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userAuth = (org.springframework.security.core.userdetails.User) pr;
            sessionRegistry.registerNewSession(request.getSession().getId(), userAuth.getUsername());
            User user = userRepo.findByEmail(userAuth.getUsername());
            result.setUrl("http://localhost:8088/loggedin/" + user.getId());
            result.setHosts();
            return result;
        }
        result.setUrl("http://localhost:8088/err?msg=userNotFound");
        result.setHosts();
        return result;
    }

    @GetMapping("/err")
    public String errPage(@RequestParam String msg, Model model, HttpServletRequest request) {
        if (msg.equals("userExists")) {
            model.addAttribute("msg", "User already exists. Please, log in or register with another e-mail.");
            model.addAttribute("loggedin", false);
        } else if (msg.equals("userNotFound")) {
            model.addAttribute("msg", "User not found. Please, log in again.");
            model.addAttribute("loggedin", false);
        } else if (msg.equals("badToken")) {
            model.addAttribute("msg", "Bad token. Please, make sure to use the appropriate link.");
            model.addAttribute("loggedin", false);
        } else if (msg.equals("tokenExpired")) {
            model.addAttribute("msg", "Token expired. Please, request a new link.");
            model.addAttribute("loggedin", false);
        } else if (msg.equals("fileNotFound")) {
            SecurityContext sc = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
            Object pr = sc.getAuthentication().getPrincipal();
            if (pr instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User userAuth = (org.springframework.security.core.userdetails.User) pr;
                User user = userRepo.findByEmail(userAuth.getUsername());
                model.addAttribute("msg", "File not found. Please, try another file.");
                model.addAttribute("loggedin", true);
                model.addAttribute("user", user);
                model.addAttribute("pages", fileRepo.findAllByAuthorId(user.getId()));
            } else {
                model.addAttribute("msg", "User not found. Please, log in again.");
                model.addAttribute("loggedin", false);
            }
        } else {
            model.addAttribute("msg", "Unexpected error.");
            model.addAttribute("loggedin", false);
        }
        return "errorpage";
    }

    @GetMapping("/registrationConfirm")
    public RedirectView confirmRegistration(@RequestParam String token) {
        RedirectView result = new RedirectView();
        VerificationToken verificationToken = service.getVerificationToken(token);
        if(verificationToken == null) {
            result.setUrl("http://localhost:8088/err?msg=badToken");
            result.setHosts();
            return result;
        }
        User user = service.getUser(token);
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            result.setUrl("http://localhost:8088/err?msg=tokenExpired");
            result.setHosts();
            return result;
        }
        user.setEnabled(true);
        service.saveRegisteredUser(user);
        result.setUrl("http://localhost:8088/login");
        result.setHosts();
        return result;
    }

    @RequestMapping(value = "/images/{img}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public void getImage(HttpServletResponse response, @PathVariable String img) throws IOException {
        String path = "images/" + img;
        var imgFile = new ClassPathResource(path);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
    }

}
