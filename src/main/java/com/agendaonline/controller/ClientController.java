package com.agendaonline.controller;

import com.agendaonline.domain.model.Client;
import com.agendaonline.dto.client.ClientRequest;
import com.agendaonline.dto.client.ClientResponse;
import com.agendaonline.service.ClientService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> list() {
        List<ClientResponse> result = clientService.list().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        Client created = clientService.create(request);
        return ResponseEntity.ok(toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(clientService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(toResponse(clientService.update(id, request)));
    }

    private ClientResponse toResponse(Client client) {
        ClientResponse resp = new ClientResponse();
        resp.setId(client.getId());
        resp.setName(client.getName());
        resp.setEmail(client.getEmail());
        resp.setPhone(client.getPhone());
        resp.setNotes(client.getNotes());
        return resp;
    }
}
