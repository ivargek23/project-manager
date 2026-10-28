package hr.algebra.project_manager.security;

import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AppUserPrincipal(Long id,
                               String username,
                               String passwordHash,
                               Role role,
                               boolean enabled) implements UserDetails {

    public static AppUserPrincipal from(AppUser appUser) {
        return new AppUserPrincipal(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getPasswordHash(),
                appUser.getRole(),
                appUser.isEnabled()
        );
    }

    private static final String ROLE_PREFIX = "ROLE_";
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, username=%s, role=%s]",
                AppUserPrincipal.class.getSimpleName(), id, username, role);
    }
}
