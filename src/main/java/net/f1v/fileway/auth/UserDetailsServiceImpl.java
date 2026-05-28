package net.f1v.fileway.auth;

import lombok.RequiredArgsConstructor;
import net.f1v.fileway.user.entity.User;
import net.f1v.fileway.user.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);

        return UserDetailsImpl.build(user.orElseThrow(() -> new UsernameNotFoundException("User not found")));
    }
}
