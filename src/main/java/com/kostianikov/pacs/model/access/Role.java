package com.kostianikov.pacs.model.access;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    GUEST(Set.of(Permission.UPLOAD_FILE)),
    USER(Set.of(Permission.READ_SELF, Permission.WRIRE_SELF)),
    ADMIN(Set.of(Permission.READ_SELF, Permission.WRIRE_SELF, Permission.READ_ALL));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public Set<SimpleGrantedAuthority> getAuthorities(){
        return getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermision()))
                .collect(Collectors.toSet());
    }

}
