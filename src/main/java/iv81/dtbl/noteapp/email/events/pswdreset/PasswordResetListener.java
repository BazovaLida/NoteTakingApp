package iv81.dtbl.noteapp.email.events.pswdreset;

import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.services.IUserService;
import iv81.dtbl.noteapp.session.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PasswordResetListener implements
        ApplicationListener<OnPasswordResetEvent> {

    @Autowired
    private IUserService service;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private SessionService sessionService;

    @Override
    public void onApplicationEvent(OnPasswordResetEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnPasswordResetEvent event) {
        User user = event.getUser();
        sessionService.expireAllUserSessions(user.getEmail());
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        String subject = "Password Reset";
        String confirmationUrl
                = event.getAppUrl() + "/pswd_reset/" + user.getId() + "/passwordResetConfirm?token=" + token;
        String message = "Please, follow the link to confirm password reset for Noteapp.";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setFrom("test_post_nta@ukr.net");
        email.setSubject(subject);
        email.setText(message + "\r\n" + "http://localhost:8088" + confirmationUrl);
        mailSender.send(email);
    }
}