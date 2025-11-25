package com.agendaonline.dto.appointment;

import com.agendaonline.domain.enums.AppointmentRequestStatus;
import java.time.OffsetDateTime;

public class AppointmentRequestResponse {

    private Long id;
    private Long professionalId;
    private Long serviceId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private OffsetDateTime requestedDateTime;
    private AppointmentRequestStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
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

    public OffsetDateTime getRequestedDateTime() {
        return requestedDateTime;
    }

    public void setRequestedDateTime(OffsetDateTime requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
    }

    public AppointmentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentRequestStatus status) {
        this.status = status;
    }
}
