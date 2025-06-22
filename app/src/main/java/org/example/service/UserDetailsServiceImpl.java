package org.example.service;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import org.example.entities.UserInfo;
import org.example.models.UserInfoDto;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Load user by username (used by Spring Security)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Could not find user with username: " + username);
        }
        return new CustomUserDetails(user); // This class must implement UserDetails
    }

    // Check if user already exists by email
    public UserInfo checkIfUserAlreadyExist(UserInfoDto userInfoDto) {
        return userRepository.findByEmail(userInfoDto.getEmail());
    }

    // Sign up a new user
    public Boolean signupUser(UserInfoDto userInfoDto) {
        // Check for existing user by email
        if (Objects.nonNull(checkIfUserAlreadyExist(userInfoDto))) {
            return false;
        }

        // Encrypt password
        String encodedPassword = passwordEncoder.encode(userInfoDto.getPassword());
        String userId = UUID.randomUUID().toString();

        // Create and save new user
        UserInfo newUser = new UserInfo(
                userId,
                userInfoDto.getUsername(),
                userInfoDto.getEmail(),
                encodedPassword,
                new HashSet<>());
        newUser.setEmail(userInfoDto.getEmail()); // Make sure UserInfo has setEmail()

        userRepository.save(newUser);
        return true;
    }

    // Check if email and password match (for manual login validation)
    public boolean isValidEmailAndPassword(String email, String rawPassword) {
        UserInfo user = userRepository.findByEmail(email);
        if (user == null)
            return false;
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
