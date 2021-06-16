package iv81.dtbl.noteapp.controllers;

import iv81.dtbl.noteapp.email.events.registration.OnRegistrationCompleteEvent;
import iv81.dtbl.noteapp.models.File;
import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.models.VerificationToken;
import iv81.dtbl.noteapp.repositories.FileRepository;
import iv81.dtbl.noteapp.repositories.UserRepository;
import iv81.dtbl.noteapp.security.DataValidator;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import iv81.dtbl.noteapp.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
    @Autowired
    private IUserService service;

    private DataValidator dataValidator = new DataValidator();

    @GetMapping("/loggedin/{uid}")
    public RedirectView choosefile(@PathVariable String uid, HttpServletRequest request) {
        RedirectView result = new RedirectView();
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (user.isPresent()) {
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        if (userFound.getLastUsedPageId() == null) {
                            List<File> userFiles = fileRepo.findAllByAuthorId(uid);
                            if (userFiles.size() == 0) {
                                File firstFile = new File("Untitled", userFound.getId(), null);
                                fileRepo.save(firstFile);
                                result.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + firstFile.getId());
                            } else {
                                File file = userFiles.get(0);
                                result.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + file.getId());
                            }
                        } else {
                            Optional<File> lastUsedFile = fileRepo.findById(userFound.getLastUsedPageId());
                            if (lastUsedFile.isPresent()) {
                                File fileFound = lastUsedFile.get();
                                result.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + fileFound.getId());
                            } else {
                                result.setUrl("http://localhost:8088/err");
                            }
                        }
                    } else {
                        result.setUrl("http://localhost:8088/loggedin/" + userAuth.getId());
                    }
                } else {
                    result.setUrl("http://localhost:8088/err");
                }
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else {
            result.setUrl("http://localhost:8088/err");
        }
        result.setHosts();
        return result;
    }

    @GetMapping("/loggedin/{uid}/{fid}")
    public String loadfile(@PathVariable String uid, @PathVariable String fid, HttpServletRequest request, Model model) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (!file.isPresent()) {
            model.addAttribute("errmsg", "File not found.");
            return "errorpage";
        } else if (!user.isPresent()) {
            model.addAttribute("errmsg", "User not found.");
            return "errorpage";
        } else if (sessionInfo == null || sessionInfo.isExpired()) {
            return "login";
        } else {
            File fileFound = file.get();
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        if (fileFound.getAuthorId().equals(uid)) {
                            userFound.setLastUsedPageId(fid);
                            userRepo.save(userFound);
                            model.addAttribute("rights", "ALL");
                            model.addAttribute("user", userFound);
                            model.addAttribute("pages", fileRepo.findAllByAuthorId(userFound.getId()));
                            model.addAttribute("file", fileFound);
                            ArrayList<User> contributors = new ArrayList<>();
                            for (String userID : fileFound.getUsersIds()) {
                                Optional<User> contributor = userRepo.findById(userID);
                                if (contributor.isPresent()) {
                                    contributors.add(contributor.get());
                                }
                            }
                            model.addAttribute("editors", contributors);
                            return "logged_in";
                        } else if (fileFound.getUsersIds().contains(uid)) {
                            userFound.setLastUsedPageId(fid);
                            userRepo.save(userFound);
                            model.addAttribute("rights", "EDIT");
                            model.addAttribute("user", userFound);
                            model.addAttribute("pages", fileRepo.findAllByAuthorId(userFound.getId()));
                            model.addAttribute("file", fileFound);
                            return "logged_in";
                        } else if (fileFound.isPublic()) {
                            userFound.setLastUsedPageId(fid);
                            userRepo.save(userFound);
                            model.addAttribute("rights", "VIEW");
                            model.addAttribute("user", userFound);
                            model.addAttribute("pages", fileRepo.findAllByAuthorId(userFound.getId()));
                            model.addAttribute("file", fileFound);
                            return "logged_in";
                        } else {
                            userFound.setLastUsedPageId(null);
                            userRepo.save(userFound);
                            model.addAttribute("user", userFound);
                            model.addAttribute("author", userRepo.findById(fileFound.getAuthorId()).get().getName());
                            return "no_access";
                        }
                    } else {
                        String newURL = "forward:/loggedin/" + userAuth.getId() + "/" + fid;
                        return newURL;
                    }
                } else {
                    return "forward:/err";
                }
            } else {
                return "forward:/err";
            }
        }
    }

    @PostMapping("/loggedin/{uid}/{fid}/title")
    @ResponseBody
    public void title_change(@RequestBody String data, @PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (file.isPresent() && user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            File fileFound = file.get();
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        data = data.substring(data.indexOf("\"new_version\":\"")+"\"new_version\":\"".length());
                        data = data.substring(0, data.indexOf("}")-1);
                        if (fileFound.getAuthorId().equals(uid) || fileFound.getUsersIds().contains(uid)) {
                            fileFound.setTitle(data);
                            fileRepo.save(fileFound);
                        }
                    }
                }
            }
        }
    }

    @PostMapping("/loggedin/{uid}/{fid}/body")
    @ResponseBody
    public void body_change(@RequestBody String data, @PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (file.isPresent() && user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            File fileFound = file.get();
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        data = data.substring(data.indexOf("\"new_version\":\"")+"\"new_version\":\"".length());
                        data = data.substring(0, data.indexOf("}")-1);
                        if (fileFound.getAuthorId().equals(uid) || fileFound.getUsersIds().contains(uid)) {
                            fileFound.setBody(data);
                            fileRepo.save(fileFound);
                        }
                    }
                }
            }
        }
    }

    @GetMapping("/loggedin/{uid}/{fid}/delete")
    public RedirectView deletePage(@PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        RedirectView result = new RedirectView();
        if (file.isPresent() && user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            File fileFound = file.get();
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        if (fileFound.getAuthorId().equals(uid)) {
                            fileFound.setAuthorId(null);
                            fileFound.setPublic(false);
                            fileFound.removeUsers(fileFound.getUsersIds());
                            fileRepo.save(fileFound);
                            result.setUrl("http://localhost:8088/loggedin/" + uid + "/page_deleted");
                        } else {
                            result.setUrl("http://localhost:8088/loggedin/" + uid + "/" + fid);
                        }
                    } else {
                        result.setUrl("http://localhost:8088/loggedin/" + userAuth.getId());
                    }
                } else {
                    result.setUrl("http://localhost:8088/err");
                }
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else if (sessionInfo == null || sessionInfo.isExpired()) {
            result.setUrl("http://localhost:8088/login");
        } else {
            result.setUrl("http://localhost:8088/err");
        }
        result.setHosts();
        return result;
    }

    @GetMapping("/loggedin/{uid}/{fid}/make_public")
    public RedirectView publicPage(@PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        RedirectView result = new RedirectView();
        if (file.isPresent() && user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            File fileFound = file.get();
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        if (fileFound.getAuthorId().equals(uid)) {
                            fileFound.setPublic(true);
                            fileRepo.save(fileFound);
                        }
                        result.setUrl("http://localhost:8088/loggedin/" + uid + "/" + fid);
                    } else {
                        result.setUrl("http://localhost:8088/loggedin/" + userAuth.getId());
                    }
                } else {
                    result.setUrl("http://localhost:8088/err");
                }
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else if (sessionInfo == null || sessionInfo.isExpired()) {
            result.setUrl("http://localhost:8088/login");
        } else {
            result.setUrl("http://localhost:8088/err");
        }
        result.setHosts();
        return result;
    }

    @GetMapping("/loggedin/{uid}/{fid}/make_private")
    public RedirectView privatePage(@PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        RedirectView result = new RedirectView();
        if (file.isPresent() && user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            File fileFound = file.get();
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        if (fileFound.getAuthorId().equals(uid)) {
                            fileFound.setPublic(false);
                            fileRepo.save(fileFound);
                        }
                        result.setUrl("http://localhost:8088/loggedin/" + uid + "/" + fid);
                    } else {
                        result.setUrl("http://localhost:8088/loggedin/" + userAuth.getId());
                    }
                } else {
                    result.setUrl("http://localhost:8088/err");
                }
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else if (sessionInfo == null || sessionInfo.isExpired()) {
            result.setUrl("http://localhost:8088/login");
        } else {
            result.setUrl("http://localhost:8088/err");
        }
        result.setHosts();
        return result;
    }

    @GetMapping("/loggedin/{uid}/page_deleted")
    public String pageDeleted(@PathVariable String uid, Model model, HttpServletRequest request) {
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        List<File> userFiles = fileRepo.findAllByAuthorId(uid);
                        model.addAttribute("user", userFound);
                        model.addAttribute("pages", userFiles);
                        return "page_deleted";
                    } else {
                        String newURL = "forward:/loggedin/" + userAuth.getId();
                        return newURL;
                    }
                }
            }
            return "errorpage";
        } else if (sessionInfo == null || sessionInfo.isExpired()){
            return "forward:/login";
        } else {
            return "errorpage";
        }
    }

    @GetMapping("/loggedin/{uid}/new_page")
    public RedirectView new_page(@PathVariable String uid, HttpServletRequest request) {
        RedirectView result = new RedirectView();
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        File newFile = new File("Untitled", userFound.getId(), null);
                        fileRepo.save(newFile);
                        result.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + newFile.getId());
                    } else {
                        result.setUrl("http://localhost:8088/loggedin/" + userAuth.getId());
                    }
                } else {
                    result.setUrl("http://localhost:8088/err");
                }
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else if (sessionInfo == null || sessionInfo.isExpired()){
            result.setUrl("http://localhost:8088/login");
        } else {
            result.setUrl("http://localhost:8088/err");
        }
        result.setHosts();
        return result;
    }

    @GetMapping("/loggedin/{uid}/{fid}/share")
    @ResponseBody
    public String share_page(@PathVariable String uid, @PathVariable String fid, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        Optional<User> user = userRepo.findById(uid);
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (file.isPresent() && user.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            File fileFound = file.get();
            User userFound = user.get();
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                User userAuth = userRepo.findByEmail(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (userAuth.getId().equals(userFound.getId())) {
                        if (fileFound.getAuthorId().equals(uid)) {
                            String token = UUID.randomUUID().toString();
                            service.createVerificationToken(userFound, token);
                            String sharelink = "localhost:8088/shared/" + fid + "?token=" + token;
                            return sharelink;
                        }
                    }
                }
            }
        }
        return "";
    }

    @GetMapping("/shared/{fid}")
    @ResponseBody
    public RedirectView goToShared(@PathVariable String fid, @RequestParam String token, HttpServletRequest request) {
        Optional<File> file = fileRepo.findById(fid);
        RedirectView redirectView = new RedirectView();
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        VerificationToken tokenFound = service.getVerificationToken(token);
        if (file.isPresent() && sessionInfo != null && !sessionInfo.isExpired()) {
            File fileFound = file.get();
            String email = sessionInfo.getPrincipal().toString();
            if (dataValidator.emailIsValid(email)) {
                User userFound = userRepo.findByEmail(email);
                if (fileFound.getAuthorId().equals(userFound.getId()) || fileFound.getUsersIds().contains(userFound.getId())) {
                    redirectView.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + fid);
                } else {
                    Calendar cal = Calendar.getInstance();
                    if(tokenFound == null || (tokenFound.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
                        redirectView.setUrl("http://localhost:8088/err");
                    } else if (!fileFound.getAuthorId().equals(userFound.getId()) && !fileFound.getUsersIds().contains(userFound.getId())) {
                        fileFound.addUser(userFound.getId());
                        fileRepo.save(fileFound);
                        redirectView.setUrl("http://localhost:8088/loggedin/" + userFound.getId() + "/" + fid);
                    }
                }
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else if (sessionInfo == null || sessionInfo.isExpired()) {
            redirectView.setUrl("http://localhost:8088/login");
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }
}
