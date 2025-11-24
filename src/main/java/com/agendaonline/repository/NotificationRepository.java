package com.agendaonline.repository;

import com.agendaonline.domain.enums.NotificationStatus;
import com.agendaonline.domain.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByProfessionalId(Long professionalId);

    List<Notification> findByStatus(NotificationStatus status);
}
