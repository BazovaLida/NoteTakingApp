package iv81.dtbl.noteapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;



@Controller
@RequestMapping
public class PageController {

    //@GetMapping("/")
    //public String index() { return "home"; }


    @GetMapping("/login")
    public String login(HttpServletRequest hsr) {
        System.out.println(hsr.getMethod() + "\n" + hsr.getQueryString() + "\n" + hsr.getRequestURL());
        return "login_form.html"; }
    @GetMapping("/error")
    public String error_pg() { return "err.html"; }
    @GetMapping("/register")
    public String register() { return "registration_form.html"; }
    @RequestMapping("/loggedin")
    public String logged_in(HttpServletRequest hsr) {
        System.out.println(hsr.getMethod() + "\n" + hsr.getQueryString() + "\n" + hsr.getRequestURL());
        return "logged_in.html"; }

    @PostMapping("/login")
    public void log_res(HttpServletRequest hsr) {
        System.out.println(hsr.getMethod() + "\n" + hsr.getQueryString() + "\n" + hsr.getRequestURL());
    }
}
