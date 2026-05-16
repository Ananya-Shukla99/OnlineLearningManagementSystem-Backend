package com.edulearn.auth.config;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.entity.UserRole;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.util.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * Called by Spring Security after Google successfully authenticates the user.
 * Creates/updates the local User record, generates a JWT, and redirects the
 * browser back to the Angular app with the token as a query parameter.
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String FRONTEND_REDIRECT = "http://localhost:4200/oauth2/callback";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email    = oAuth2User.getAttribute("email");
        String name     = oAuth2User.getAttribute("name");
        String picture  = oAuth2User.getAttribute("picture");

        // Find existing user or register a new one (STUDENT by default)
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : email);
            newUser.setPasswordHash("OAUTH2_NO_PASSWORD");
            newUser.setRole(UserRole.STUDENT);
            newUser.setProvider("google");
            return newUser;
        });

        // Keep profile picture in sync
        if (picture != null) {
            user.setProfilePicUrl(picture);
        }
        user = userRepository.save(user);

        // Mint a JWT for the Angular app
        String token = jwtTokenProvider.generateToken(user);
        String role  = user.getRole().name();

        // Redirect to Angular with token and role embedded so AuthService can store them
        String redirectUrl = FRONTEND_REDIRECT
                + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&role="  + URLEncoder.encode(role,  StandardCharsets.UTF_8)
                + "&userId=" + user.getUserId();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
