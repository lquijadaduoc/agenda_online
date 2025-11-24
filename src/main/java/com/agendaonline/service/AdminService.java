package com.agendaonline.service;

import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.User;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public AdminService(ProfessionalRepository professionalRepository, UserRepository userRepository) {
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Professional> listProfessionals() {
        return professionalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Professional getProfessional(Long id) {
        return professionalRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {
        Professional professional = getProfessional(id);
        User user = professional.getUser();
        user.setActive(active);
        userRepository.save(user);
    }
}
