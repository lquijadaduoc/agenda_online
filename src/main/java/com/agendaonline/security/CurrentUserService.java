package com.agendaonline.security;

import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.User;
import com.agendaonline.repository.ProfessionalRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    private final ProfessionalRepository professionalRepository;

    public CurrentUserService(ProfessionalRepository professionalRepository) {
        this.professionalRepository = professionalRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new IllegalStateException("No authenticated user");
        }
        return principal.getUser();
    }

    public Professional getCurrentProfessional() {
        User user = getCurrentUser();
        return professionalRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalStateException("Professional profile not found"));
    }
}
