package iv81.dtbl.noteapp;

import iv81.dtbl.noteapp.models.*;
import iv81.dtbl.noteapp.repositories.*;
import iv81.dtbl.noteapp.session.NoteAppSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.http.HttpSessionListener;

@ServletComponentScan
@SpringBootApplication
public class NoteappApplication {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private FileRepository fileRepo;

    public static void main(String[] args) {
        SpringApplication.run(NoteappApplication.class, args);
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> httpSessionListener() {
        ServletListenerRegistrationBean<HttpSessionListener> listenerRegBean =
                new ServletListenerRegistrationBean<>();

        listenerRegBean.setListener(new NoteAppSessionListener());
        return listenerRegBean;
    }


}
