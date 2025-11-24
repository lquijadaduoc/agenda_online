package com.agendaonline.service;

import com.agendaonline.domain.enums.Role;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.ProfessionalSettings;
import com.agendaonline.domain.model.User;
import com.agendaonline.dto.auth.AuthLoginRequest;
import com.agendaonline.dto.auth.AuthRegisterRequest;
import com.agendaonline.dto.auth.AuthResponse;
import com.agendaonline.dto.auth.ForgotPasswordRequest;
import com.agendaonline.dto.auth.ResetPasswordRequest;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.ProfessionalSettingsRepository;
import com.agendaonline.repository.PasswordResetRepository;
import com.agendaonline.repository.UserRepository;
import com.agendaonline.security.CustomUserDetails;
import com.agendaonline.security.JwtService;
import java.util.Map;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalSettingsRepository settingsRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       ProfessionalRepository professionalRepository,
                       ProfessionalSettingsRepository settingsRepository,
                       PasswordResetRepository passwordResetRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.settingsRepository = settingsRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        professionalRepository.findByPublicSlug(request.getPublicSlug()).ifPresent(p -> {
            throw new IllegalArgumentException("Slug público ya en uso");
        });
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PROFESSIONAL);
        user.setActive(true);

        Professional professional = new Professional();
        professional.setUser(user);
        professional.setPublicSlug(request.getPublicSlug());
        professional.setBusinessName(request.getBusinessName());

        ProfessionalSettings settings = new ProfessionalSettings();
        settings.setProfessional(professional);

        userRepository.save(user);
        professionalRepository.save(professional);
        settingsRepository.save(settings);

        return generateTokens(user);
    }

    public AuthResponse login(AuthLoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            return generateTokens(principal.getUser());
        } catch (Exception ex) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            var reset = new com.agendaonline.domain.model.PasswordReset();
            reset.setUser(user);
            reset.setToken(UUID.randomUUID().toString());
            reset.setExpiresAt(OffsetDateTime.now().plusHours(24));
            passwordResetRepository.save(reset);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        com.agendaonline.domain.model.PasswordReset reset = passwordResetRepository.findByToken(request.getToken())
            .filter(pr -> pr.getExpiresAt().isAfter(OffsetDateTime.now()) && pr.getUsedAt() == null)
            .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));
        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        reset.setUsedAt(OffsetDateTime.now());
        userRepository.save(user);
        passwordResetRepository.save(reset);
    }

    private AuthResponse generateTokens(User user) {
        Map<String, Object> claims = Map.of("role", user.getRole().name(), "uid", user.getId());
        String accessToken = jwtService.generateAccessToken(user.getEmail(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), claims);
        return new AuthResponse(accessToken, refreshToken);
    }
}
