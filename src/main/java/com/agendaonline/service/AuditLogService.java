package com.agendaonline.service;

import com.agendaonline.domain.model.AuditLog;
import com.agendaonline.domain.model.User;
import com.agendaonline.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User user, String action, String entityType, Long entityId, String metadata) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMetadata(metadata);
        auditLogRepository.save(log);
    }
}
