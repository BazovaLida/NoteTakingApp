package iv81.dtbl.noteapp.controllers;

import iv81.dtbl.noteapp.email.events.registration.OnRegistrationCompleteEvent;
import iv81.dtbl.noteapp.models.File;
import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.repositories.FileRepository;
import iv81.dtbl.noteapp.repositories.UserRepository;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class UserFileController {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private FileRepository fileRepo;
    @Autowired
    private SessionRegistry sessionRegistry;
    @Autowired
    private AppUserDetailsService userService;

    @GetMapping("/loggedin/{uid}")
    public RedirectView choosefile(@PathVariable String uid, HttpServletRequest request) {
        RedirectView result = new RedirectView();
        Optional<User> user = userRepo.findById(uid);
        if (user.isPresent()) {
            User userFound = user.get();
            if (userFound.getLastUsedPageId() == null) {
                File firstFile = new File("Untitled", userFound.getId(), null);
                fileRepo.save(firstFile);
                result.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + firstFile.getId());
                result.setHosts();
                return result;
            } else {
                Optional<File> lastUsedFile = fileRepo.findById(userFound.getLastUsedPageId());
                if (lastUsedFile.isPresent()) {
                    File fileFound = lastUsedFile.get();
                    result.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + fileFound.getId());
                    result.setHosts();
                    return result;
                } else {
                    result.setUrl("http://localhost:8088/err");
                    result.setHosts();
                    return result;
                }
            }
        } else {
            result.setUrl("http://localhost:8088/err");
            result.setHosts();
            return result;
        }
    }

    @GetMapping("/loggedin/{uid}/{fid}")
    public String loadfile(@PathVariable String uid, @PathVariable String fid, HttpServletRequest request, Model model) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        if (!file.isPresent()) {
            model.addAttribute("errmsg", "File not found.");
            return "errorpage";
        } else if (!user.isPresent()) {
            model.addAttribute("errmsg", "User not found.");
            return "errorpage";
        } else {
            File fileFound = file.get();
            User userFound = user.get();
            if (fileFound.getAuthorId().equals(uid)) {
                userFound.setLastUsedPageId(fid);
                userService.saveUser(userFound);
                model.addAttribute("rights", "ALL");
                model.addAttribute("user", userFound);
                model.addAttribute("pages", fileRepo.findAllByAuthorId(userFound.getId()));
                model.addAttribute("title", fileFound.getTitle());
                model.addAttribute("body", fileFound.getBody());
                return "logged_in";
            } else if (fileFound.getUsersIds().contains(uid)) {
                userFound.setLastUsedPageId(fid);
                userService.saveUser(userFound);
                model.addAttribute("rights", "EDIT");
                model.addAttribute("user", userFound);
                model.addAttribute("pages", fileRepo.findAllByAuthorId(userFound.getId()));
                model.addAttribute("title", fileFound.getTitle());
                model.addAttribute("body", fileFound.getBody());
                return "logged_in";
            } else if (fileFound.isPublic()) {
                userFound.setLastUsedPageId(fid);
                userService.saveUser(userFound);
                model.addAttribute("rights", "VIEW");
                model.addAttribute("user", userFound);
                model.addAttribute("pages", fileRepo.findAllByAuthorId(userFound.getId()));
                model.addAttribute("title", fileFound.getTitle());
                model.addAttribute("body", fileFound.getBody());
                return "logged_in";
            } else {
                model.addAttribute("user", userFound);
                model.addAttribute("author", userRepo.findById(fileFound.getAuthorId()).get().getName());
                return "no_access";
            }
        }
    }

    @PostMapping("/loggedin/{uid}/{fid}/title")
    public void title_change(@RequestParam String title, @PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        if (file.isPresent() && user.isPresent()) {
            File fileFound = file.get();
            if (fileFound.getAuthorId().equals(uid) || fileFound.getUsersIds().contains(uid)) {
                fileFound.setTitle(title);
                fileRepo.save(fileFound);
            }
        }
    }

    @PostMapping("/loggedin/{uid}/{fid}/body")
    public void body_change(@RequestParam String body, @PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        if (file.isPresent() && user.isPresent()) {
            File fileFound = file.get();
            if (fileFound.getAuthorId().equals(uid) || fileFound.getUsersIds().contains(uid)) {
                fileFound.setBody(body);
                fileRepo.save(fileFound);
            }
        }
    }

}
