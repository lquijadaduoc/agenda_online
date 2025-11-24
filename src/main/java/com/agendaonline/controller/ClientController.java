package com.agendaonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @GetMapping
    public ResponseEntity<String> list(@RequestParam(required = false) String name,
                                       @RequestParam(required = false) String email) {
        return ResponseEntity.ok("clients");
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable Long id) {
        return ResponseEntity.ok("client " + id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }
}
