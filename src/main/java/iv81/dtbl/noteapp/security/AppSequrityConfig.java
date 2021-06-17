package iv81.dtbl.noteapp.security;

import iv81.dtbl.noteapp.security.handlers.SecurityErrorHandler;
import iv81.dtbl.noteapp.security.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class AppSequrityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppSequrityConfig(PasswordEncoder pE) {
        this.passwordEncoder = pE;
    }

    /*@Bean
    public AppUserDetailsService mongoUserService() { return new AppUserDetailsService(passwordEncoder); }*/
    @Autowired
    private AppUserDetailsService mongoUserService;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Autowired
    public void confGlobalAuthManager(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsService uDS = mongoUserService;
        auth
                .userDetailsService(uDS)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/home", "/images/**", "/login", "/register", "/err", "/pswd_reset/**", "/pswd_reset_form").permitAll()
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
                    .logoutSuccessUrl("/")
                .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                    .invalidSessionUrl("/logout")
                    .maximumSessions(3)
                    .maxSessionsPreventsLogin(false)
                    .sessionRegistry(sessionRegistry())
                .and()
                .sessionAuthenticationFailureHandler(new SecurityErrorHandler());
    }
}
