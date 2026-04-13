package tn.beecoders.elderly.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.dto.AuthRequest;
import tn.beecoders.elderly.dto.AuthResponse;
import tn.beecoders.elderly.dto.RegisterRequest;
import tn.beecoders.elderly.exception.BadRequestException;
import tn.beecoders.elderly.repository.UserRepository;
import tn.beecoders.elderly.service.UserAccountService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserAccountService userAccountService;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        var user = userAccountService.createUserFromRegister(request);
        return buildAuthResponse(user);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new BadRequestException("Not a refresh token");
            }
            String email = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new BadRequestException("Refresh token expired or invalid");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadRequestException("User not found"));
            return buildAuthResponse(user);
        } catch (JwtException ex) {
            throw new BadRequestException("Invalid refresh token");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .type("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
