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

    /*@Override
    public void run(String ... args) throws Exception {
        userRepo.deleteAll();

        User user1 = new User("example1@ukr.net", "jldhfgalhfg");
        user1.setName("User1");
        user1 = userRepo.save(user1);
        //System.out.println("user1 id: " + user1.getId());
        User user2 = new User("example2@ukr.net", "auifgefglaksd");
        user2.setName("User2");
        user2 =userRepo.save(user2);

        File file1us1 = new File("File1", user1.getId(), null);
        file1us1.addElement("Element1");
        file1us1.addElement("Element2");
        file1us1 = fileRepo.save(file1us1);
        System.out.println(file1us1.getElements());
        //File file2us1 = new File("File 1.1", user1.getId(), file1us1.getId());



    }*/

    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> httpSessionListener() {
        ServletListenerRegistrationBean<HttpSessionListener> listenerRegBean =
                new ServletListenerRegistrationBean<>();

        listenerRegBean.setListener(new NoteAppSessionListener());
        return listenerRegBean;
    }


}
