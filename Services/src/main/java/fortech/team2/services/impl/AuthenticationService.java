package fortech.team2.services.impl;

import fortech.team2.model.Medic;
import fortech.team2.model.RefreshToken;
import fortech.team2.model.User;
import fortech.team2.model.dto.builders.AuthenticationDTOBuilder;
import fortech.team2.model.dto.request.LoginRequest;
import fortech.team2.model.dto.request.SignupMedicRequest;
import fortech.team2.model.dto.request.SignupRequest;
import fortech.team2.model.dto.request.TokenRefreshRequest;
import fortech.team2.model.dto.response.JwtResponse;
import fortech.team2.model.dto.response.TokenRefreshResponse;
import fortech.team2.persistence.RefreshTokenRepository;
import fortech.team2.persistence.UserRepository;
import fortech.team2.services.AccountsService;
import fortech.team2.services.EmailService;
import fortech.team2.services.jwt.JwtUtils;
import fortech.team2.validation.RegisterAccountValidator;
import fortech.team2.validation.utils.TokenRefreshException;
import fortech.team2.validation.utils.ValidatorException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class AuthenticationService implements AccountsService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private RegisterAccountValidator registerAccountValidator;
    @Autowired
    private EmailService emailService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    public Medic registerMedic(SignupMedicRequest medicSignupRequest) throws ValidatorException {
        registerAccountValidator.validate(medicSignupRequest);
        if (Boolean.TRUE.equals(userRepository.existsByEmail(medicSignupRequest.getEmail()))) {
            throw new ValidatorException("Account already exists");
        }

        Medic medic = AuthenticationDTOBuilder.fromSignupRequest(medicSignupRequest);
        String password = medicSignupRequest.getPassword();
        medic.setPassword(encoder.encode(password));
        medic.setDaysOff(25);
        userRepository.save(medic);
        medic.setPassword(password);
        emailService.sendPassword(medic);
        return medic;
    }

    @Override
    public User registerUser(SignupRequest signUpRequest) throws ValidatorException {
        registerAccountValidator.validate(signUpRequest);
        if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.getEmail()))) {
            throw new ValidatorException("Account already exists");
        }

        User user = AuthenticationDTOBuilder.fromSignupRequest(signUpRequest);
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        userRepository.save(user);

        return userRepository.save(user);
    }

    @Override
    public JwtResponse signIn(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getEmail(), roles);
    }

    @Override
    public ResponseEntity<?> refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in the database!"));
    }

    @Override
    public ResponseEntity<?> signOut() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.ok(userId);

    }

    public String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    @Override
    public String generatePasswordResetToken(String email) {

        System.out.println("Received email address: " + email);

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found"); // Handle this as needed
        }

        if (user.getId() != null ){
            refreshTokenService.deleteByUserId(user.getId());
        }
        String token = jwtUtils.generateRandomToken(); // Use the TokenService to generate a random token

        String resetLink = "http://localhost:3000/reset-password/" + token;

        emailService.sendPasswordResetLink(user.getEmail(), resetLink);

        Instant expiryDate = Instant.now().plus(Duration.ofHours(1)); // Expires in 1 hour
        RefreshToken resetToken = new RefreshToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(expiryDate); // Set the expiration date
        refreshTokenRepository.save(resetToken);

        // Save the resetToken entity to the database
        refreshTokenRepository.save(resetToken);

        return token;
    }

    @Override
    public Boolean resetPasswordWithToken(String token, String newPassword) throws ValidatorException {
        //  Validate the token
        RefreshToken refreshToken = refreshTokenService.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Token not found"));
        // Verify if the token is expired and handle expiration if needed
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        //  Reset the user's password
        User user = refreshToken.getUser();
        registerAccountValidator.validatePassword(newPassword);
        user.setPassword(encoder.encode(newPassword)); // Encode the new password
        // Save the updated user entity
        userRepository.save(user);
        //  Remove the token from the RefreshToken table (if it's not expired)
        refreshTokenService.deleteByUserId(user.getId());

        return true; // Password reset successful
    }



}
