package com.agendaonline.service;

import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.ServiceOffering;
import com.agendaonline.dto.service.ServiceOfferingRequest;
import com.agendaonline.repository.ServiceOfferingRepository;
import com.agendaonline.security.CurrentUserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final CurrentUserService currentUserService;

    public ServiceOfferingService(ServiceOfferingRepository serviceOfferingRepository,
                                  CurrentUserService currentUserService) {
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ServiceOffering> list() {
        Professional professional = currentUserService.getCurrentProfessional();
        return serviceOfferingRepository.findByProfessionalIdAndActiveTrue(professional.getId());
    }

    @Transactional
    public ServiceOffering create(ServiceOfferingRequest request) {
        Professional professional = currentUserService.getCurrentProfessional();
        ServiceOffering offering = new ServiceOffering();
        offering.setProfessional(professional);
        applyRequest(offering, request);
        if (request.getActive() != null) {
            offering.setActive(request.getActive());
        }
        return serviceOfferingRepository.save(offering);
    }

    @Transactional
    public ServiceOffering update(Long id, ServiceOfferingRequest request) {
        Professional professional = currentUserService.getCurrentProfessional();
        ServiceOffering offering = serviceOfferingRepository.findById(id)
            .filter(s -> s.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        applyRequest(offering, request);
        if (request.getActive() != null) {
            offering.setActive(request.getActive());
        }
        return serviceOfferingRepository.save(offering);
    }

    @Transactional
    public void delete(Long id) {
        Professional professional = currentUserService.getCurrentProfessional();
        ServiceOffering offering = serviceOfferingRepository.findById(id)
            .filter(s -> s.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        offering.setActive(false);
        serviceOfferingRepository.save(offering);
    }

    private void applyRequest(ServiceOffering offering, ServiceOfferingRequest request) {
        offering.setName(request.getName());
        offering.setDescription(request.getDescription());
        offering.setDurationMinutes(request.getDurationMinutes());
        offering.setPrice(request.getPrice());
    }
}
