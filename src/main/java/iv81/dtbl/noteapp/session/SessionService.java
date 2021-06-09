package iv81.dtbl.noteapp.session;

import iv81.dtbl.noteapp.models.User;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("expireUsereService")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionService {

    @Autowired
    private AppUserDetailsService userDetailsService;

    private SessionRegistry sessionRegistry;
    @Autowired
    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void expireUserSessions(String email) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof String) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(principal.toString());
                if (userDetails.getUsername().equals(email)) {
                    boolean killedFirst = false;
                    for (SessionInformation information : sessionRegistry
                            .getAllSessions(userDetails, true)) {
                        if (information.isExpired()) {
                            killExpiredSessionForSure(information.getSessionId());
                        } else if (!killedFirst) {
                            information.expireNow();
                            killExpiredSessionForSure(information.getSessionId());
                        }
                    }
                }
            }
        }
    }

    public void killExpiredSessionForSure(String id) {
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Cookie", "JSESSIONID=" + id);
            HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
            RestTemplate rt = new RestTemplate();
            rt.exchange("http://localhost:8080", HttpMethod.GET,
                    requestEntity, String.class);
        } catch (Exception ex) {}
    }

}

