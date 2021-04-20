package iv81.dtbl.noteapp.controllers;

import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.models.VerificationToken;
import iv81.dtbl.noteapp.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Calendar;
import java.util.Locale;

@Controller
public class RegistrationController {

    @Autowired
    private IUserService service;

    @GetMapping("/registrationConfirm")
    public RedirectView confirmRegistration(WebRequest request, @RequestParam("token") String token) {

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
