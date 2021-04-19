package iv81.dtbl.noteapp.security;

import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
//import iv81.dtbl.noteapp.models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class AppSequrityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppSequrityConfig(PasswordEncoder pE) {
        this.passwordEncoder = pE;
    }

    @Bean
    public UserDetailsService mongoUserDetails() { return new AppUserDetailsService(passwordEncoder); }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsService uDS = mongoUserDetails();
        auth
                .userDetailsService(uDS)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/login", "/register", "/err").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/loggedin")
                    .usernameParameter("email")
                    .passwordParameter("password")
                .and()
                .rememberMe()
                    .tokenValiditySeconds((int)TimeUnit.DAYS.toSeconds(30))
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "remember-me")
                    .logoutSuccessUrl("/");
    }

//    @Override
//    @Bean
//    protected UserDetailsService userDetailsService() {
//        UserDetails testUsr = User.builder()
//                .username("test@test.com")
//                .password(passwordEncoder.encode("1234"))
//                .roles("test")
//                .build();
//
//        return new InMemoryUserDetailsManager(testUsr);
//    }
}
