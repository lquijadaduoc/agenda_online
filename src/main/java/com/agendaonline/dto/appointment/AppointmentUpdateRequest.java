package com.agendaonline.dto.appointment;

import com.agendaonline.domain.enums.AppointmentStatus;

public class AppointmentUpdateRequest {
    private AppointmentStatus status;
    private String notes;

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
}
