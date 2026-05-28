package net.f1v.fileway.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.f1v.fileway.user.entity.User;
import net.f1v.fileway.user.entity.UserRole;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@AllArgsConstructor
@NullMarked
public class UserDetailsImpl implements UserDetails {
    @Getter
    @Setter
    private Long id;
    private String email;
    private String password;
    @Getter
    private UserRole role;

    public static UserDetailsImpl build(final User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
