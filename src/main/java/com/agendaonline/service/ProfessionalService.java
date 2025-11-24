package com.agendaonline.service;

import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.User;
import com.agendaonline.dto.professional.ProfessionalProfileUpdateRequest;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.UserRepository;
import com.agendaonline.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ProfessionalService(ProfessionalRepository professionalRepository,
                               UserRepository userRepository,
                               CurrentUserService currentUserService) {
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Professional me() {
        return currentUserService.getCurrentProfessional();
    }

    @Transactional
    public void updateProfile(ProfessionalProfileUpdateRequest request) {
        Professional professional = currentUserService.getCurrentProfessional();
        User user = professional.getUser();
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBusinessName() != null) {
            professional.setBusinessName(request.getBusinessName());
        }
        if (request.getPhone() != null) {
            professional.setPhone(request.getPhone());
        }
        if (request.getTimezone() != null) {
            professional.setTimezone(request.getTimezone());
        }
        if (request.getBio() != null) {
            professional.setBio(request.getBio());
        }
        if (request.getAddress() != null) {
            professional.setAddress(request.getAddress());
        }
        userRepository.save(user);
        professionalRepository.save(professional);
    }

    @Transactional(readOnly = true)
    public Professional publicBySlug(String slug) {
        return professionalRepository.findByPublicSlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
    }
}
