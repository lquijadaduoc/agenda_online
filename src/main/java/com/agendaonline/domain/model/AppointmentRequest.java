package com.agendaonline.domain.model;

import com.agendaonline.domain.common.BaseEntity;
import com.agendaonline.domain.enums.AppointmentRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "appointment_requests")
public class AppointmentRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;

    @Column(name = "requested_datetime", nullable = false)
    private OffsetDateTime requestedDateTime;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_phone")
    private String clientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentRequestStatus status = AppointmentRequestStatus.PENDING;

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public ServiceOffering getService() {
        return service;
    }

    public void setService(ServiceOffering service) {
        this.service = service;
    }

    public OffsetDateTime getRequestedDateTime() {
        return requestedDateTime;
    }

    public void setRequestedDateTime(OffsetDateTime requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public AppointmentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentRequestStatus status) {
        this.status = status;
    }
}
