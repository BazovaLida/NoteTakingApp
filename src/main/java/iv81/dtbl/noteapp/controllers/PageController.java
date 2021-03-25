package iv81.dtbl.noteapp.controllers;

import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpClient;


@Controller
@RequestMapping
public class PageController {

    //@GetMapping("/")
    //public String index() { return "home"; }
    @Autowired
    private AppUserDetailsService userService;


    @GetMapping("/login")
    public String login() {
        return "login_form.html"; }

    @GetMapping("/register")
    public String register() { return "registration_form.html"; }

    @PostMapping("/register")
    @ResponseBody
    public RedirectView log_res(@RequestParam String email, @RequestParam String username, @RequestParam String password) {
        User existing = userService.findUserByEmail(email);
        if (existing == null) {
            User newUser = new User(username, email, password);
            userService.saveUser(newUser);
            return new RedirectView("login_form");
        } else {
            return new RedirectView("err");
        }
    }

    @RequestMapping("/loggedin")
    public String logged_in() {
        return "logged_in.html";
    }
    @GetMapping("/err")
    public String errPage() {
        return "err.html";
    }

}
