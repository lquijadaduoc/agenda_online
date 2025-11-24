package com.agendaonline.domain.model;

import com.agendaonline.domain.common.BaseEntity;
import com.agendaonline.domain.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;

    @Column(name = "start_datetime", nullable = false)
    private OffsetDateTime startDateTime;

    @Column(name = "end_datetime", nullable = false)
    private OffsetDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_from_public_link", nullable = false)
    private boolean createdFromPublicLink = false;

    @Column(name = "cancellation_token", unique = true)
    private String cancellationToken;

    @OneToOne(mappedBy = "appointment")
    private PublicLink publicLink;

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ServiceOffering getService() {
        return service;
    }

    public void setService(ServiceOffering service) {
        this.service = service;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCreatedFromPublicLink() {
        return createdFromPublicLink;
    }

    public void setCreatedFromPublicLink(boolean createdFromPublicLink) {
        this.createdFromPublicLink = createdFromPublicLink;
    }

    public String getCancellationToken() {
        return cancellationToken;
    }

    public void setCancellationToken(String cancellationToken) {
        this.cancellationToken = cancellationToken;
    }

    public PublicLink getPublicLink() {
        return publicLink;
    }

    public void setPublicLink(PublicLink publicLink) {
        this.publicLink = publicLink;
    }
}
