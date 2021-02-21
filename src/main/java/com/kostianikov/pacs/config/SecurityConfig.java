package com.kostianikov.pacs.config;

import com.kostianikov.pacs.security.DoubleImageAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final  UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(@Qualifier("UserDetailsServiceImpl")UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/uploadForm", "GET").permitAll()
                .anyRequest()
                .authenticated()
                //.and()
//                .httpBasic();
                //.formLogin()
                //.loginPage("/uploadForm").permitAll()
                //.defaultSuccessUrl("/user")
                .and()
                .addFilterBefore(getDoubleImageAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                //.and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/uploadForm");

//                .authorizeRequests()
//                .antMatchers("/", "/updateForm", "/users","/login").permitAll()
//                .anyRequest().authenticated();
//                .and()
//                .formLogin()
//                .loginPage("/login.html")
//                .permitAll()
//                .and()
//                .logout()
//                .permitAll();
    }

    @Bean
    public UsernamePasswordAuthenticationFilter getDoubleImageAuthFilter() throws Exception{
        DoubleImageAuthenticationFilter doubleImageAuthenticationFilter = new DoubleImageAuthenticationFilter();
        doubleImageAuthenticationFilter.setAuthenticationSuccessHandler(this::loginSuccessHandler);
        doubleImageAuthenticationFilter.setAuthenticationFailureHandler(this::loginFailureHandler);
        doubleImageAuthenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/uploadForm", "POST"));
        doubleImageAuthenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return doubleImageAuthenticationFilter;
    }

    private void loginFailureHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        //httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/reject");
        httpServletRequest.getSession().setAttribute("Message", e.toString());
    }

    private void loginSuccessHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/user");
    }

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        PasswordEncoder encoder =
//                PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        auth
//                .inMemoryAuthentication()
//                .withUser("user")
//                .password(encoder.encode("password"))
//                .roles("USER")
//                .and()
//                .withUser("admin")
//                .password(encoder.encode("admin"))
//                .roles("USER", "ADMIN");
//    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(getCustomDaoAuthenticationProvider());
    }



    @Bean
    protected PasswordEncoder passwordEncoder(){ return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();}

    @Bean
    protected DaoAuthenticationProvider getCustomDaoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }


}
