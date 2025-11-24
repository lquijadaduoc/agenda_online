package com.agendaonline.service;

import com.agendaonline.domain.model.Client;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.dto.client.ClientRequest;
import com.agendaonline.repository.ClientRepository;
import com.agendaonline.security.CurrentUserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final CurrentUserService currentUserService;

    public ClientService(ClientRepository clientRepository, CurrentUserService currentUserService) {
        this.clientRepository = clientRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<Client> list() {
        Professional professional = currentUserService.getCurrentProfessional();
        return clientRepository.findByProfessionalId(professional.getId());
    }

    @Transactional
    public Client create(ClientRequest request) {
        Professional professional = currentUserService.getCurrentProfessional();
        Client client = new Client();
        client.setProfessional(professional);
        applyRequest(client, request);
        return clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public Client get(Long id) {
        Professional professional = currentUserService.getCurrentProfessional();
        return clientRepository.findById(id)
            .filter(c -> c.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    @Transactional
    public Client update(Long id, ClientRequest request) {
        Client client = get(id);
        applyRequest(client, request);
        return clientRepository.save(client);
    }

    private void applyRequest(Client client, ClientRequest request) {
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setNotes(request.getNotes());
    }
}
