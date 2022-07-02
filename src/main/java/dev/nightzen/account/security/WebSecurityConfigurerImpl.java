package dev.nightzen.account.security;

import dev.nightzen.account.constants.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {
    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(getEncoder());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/auth/changepass")
                .hasAnyAuthority(UserRole.ROLE_USER.name(), UserRole.ROLE_ACCOUNTANT.name(), UserRole.ROLE_ADMINISTRATOR.name())
                .mvcMatchers(HttpMethod.GET, "/api/empl/payment")
                .hasAnyAuthority(UserRole.ROLE_USER.name(), UserRole.ROLE_ACCOUNTANT.name())
                .mvcMatchers(HttpMethod.POST, "/api/acct/payments")
                .hasAuthority(UserRole.ROLE_ACCOUNTANT.name())
                .mvcMatchers(HttpMethod.PUT, "/api/acct/payments")
                .hasAuthority(UserRole.ROLE_ACCOUNTANT.name())
                .mvcMatchers(HttpMethod.GET, "/api/admin/user")
                .hasAuthority(UserRole.ROLE_ADMINISTRATOR.name())
                .mvcMatchers(HttpMethod.DELETE, "/api/admin/user/**")
                .hasAuthority(UserRole.ROLE_ADMINISTRATOR.name())
                .mvcMatchers(HttpMethod.PUT, "/api/admin/user/role")
                .hasAuthority(UserRole.ROLE_ADMINISTRATOR.name())
                .mvcMatchers(HttpMethod.PUT, "/api/admin/user/access")
                .hasAuthority(UserRole.ROLE_ADMINISTRATOR.name())
                .mvcMatchers(HttpMethod.GET, "/api/security/events")
                .hasAuthority(UserRole.ROLE_AUDITOR.name())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler());
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}
