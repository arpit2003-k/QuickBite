package com.quickbite.auth.config;

import com.quickbite.auth.entity.User;
import com.quickbite.auth.repository.UserRepository;
import com.quickbite.auth.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final BCryptPasswordEncoder oauthPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user info
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String provider = determineProvider(request);

        // GitHub may not provide email if private; handle gracefully
        if (email == null) {
            String login = oauth2User.getAttribute("login");
            email = login + "@github.com";
            if (name == null) name = login;
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getProvider() == null) {
                user.setProvider(provider);
                userRepository.save(user);
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setFullName(name != null ? name : email.split("@")[0]);
            // Keep DB constraints satisfied for first-time OAuth users.
            user.setPasswordHash(oauthPasswordEncoder.encode(UUID.randomUUID().toString()));
            user.setProvider(provider);
            user.setRole(User.Role.CUSTOMER);
            user.setIsActive(true);
            user = userRepository.save(user);
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());

        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/oauth2-redirect?token=" + token +
                "&userId=" + user.getUserId() +
                "&role=" + user.getRole().name() +
                "&email=" + email +
                "&fullName=" + user.getFullName();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String determineProvider(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("google")) return "GOOGLE";
        else if (path.contains("github")) return "GITHUB";
        else return "OAUTH2";
    }
}
