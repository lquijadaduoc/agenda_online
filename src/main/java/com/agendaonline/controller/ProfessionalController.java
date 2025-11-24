package com.agendaonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ProfessionalController {

    @GetMapping("/me")
    public ResponseEntity<String> me() {
        // TODO: devolver perfil autenticado.
        return ResponseEntity.ok("me");
    }

    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody Object body) {
        // TODO: actualizar perfil.
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/professionals/{slug}/public")
    public ResponseEntity<String> publicProfile(@PathVariable String slug) {
        // TODO: devolver datos publicos por slug.
        return ResponseEntity.ok("public profile " + slug);
    }
}
