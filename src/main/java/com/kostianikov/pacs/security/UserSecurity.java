package com.kostianikov.pacs.security;


import com.kostianikov.pacs.model.access.Status;
import com.kostianikov.pacs.model.access.User;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Data
public class UserSecurity implements UserDetails {

//    UsernamePasswordAuthenticationToken;
//            AuthenticationProvider;

    private String password;
    private final String username;
    private final Set<SimpleGrantedAuthority> authorities;
    private final boolean accountisActive;

    public UserSecurity(String password, String username, Set<SimpleGrantedAuthority> authorities, boolean accountisActive) {
        this.password = password;
        this.username = username;
        this.authorities = authorities;
        this.accountisActive = accountisActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountisActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountisActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return accountisActive;
    }

    @Override
    public boolean isEnabled() {
        return accountisActive;
    }

    public static UserDetails fromUser(User user){
        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getPassword(),
                user.getStatus().equals(Status.ACTIVE),
                user.getStatus().equals(Status.ACTIVE),
                user.getStatus().equals(Status.ACTIVE),
                user.getStatus().equals(Status.ACTIVE),
                user.getRole().getAuthorities());
    }
}
