package com.agendaonline.domain.model;

import com.agendaonline.domain.common.BaseEntity;
import com.agendaonline.domain.enums.DefaultAppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "professional_settings")
public class ProfessionalSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professional_id", nullable = false, unique = true)
    private Professional professional;

    @Column(name = "allow_public_booking", nullable = false)
    private boolean allowPublicBooking = true;

    @Column(name = "booking_advance_days")
    private Integer bookingAdvanceDays;

    @Column(name = "cancellation_policy_hours")
    private Integer cancellationPolicyHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_appointment_status", nullable = false)
    private DefaultAppointmentStatus defaultAppointmentStatus = DefaultAppointmentStatus.PENDING;

    @Column(name = "email_notification_enabled", nullable = false)
    private boolean emailNotificationEnabled = true;

    @Column(name = "reminder_time_before_appointment_minutes")
    private Integer reminderTimeBeforeAppointmentMinutes;

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public boolean isAllowPublicBooking() {
        return allowPublicBooking;
    }

    public void setAllowPublicBooking(boolean allowPublicBooking) {
        this.allowPublicBooking = allowPublicBooking;
    }

    public Integer getBookingAdvanceDays() {
        return bookingAdvanceDays;
    }

    public void setBookingAdvanceDays(Integer bookingAdvanceDays) {
        this.bookingAdvanceDays = bookingAdvanceDays;
    }

    public Integer getCancellationPolicyHours() {
        return cancellationPolicyHours;
    }

    public void setCancellationPolicyHours(Integer cancellationPolicyHours) {
        this.cancellationPolicyHours = cancellationPolicyHours;
    }

    public DefaultAppointmentStatus getDefaultAppointmentStatus() {
        return defaultAppointmentStatus;
    }

    public void setDefaultAppointmentStatus(DefaultAppointmentStatus defaultAppointmentStatus) {
        this.defaultAppointmentStatus = defaultAppointmentStatus;
    }

    public boolean isEmailNotificationEnabled() {
        return emailNotificationEnabled;
    }

    public void setEmailNotificationEnabled(boolean emailNotificationEnabled) {
        this.emailNotificationEnabled = emailNotificationEnabled;
    }

    public Integer getReminderTimeBeforeAppointmentMinutes() {
        return reminderTimeBeforeAppointmentMinutes;
    }

    public void setReminderTimeBeforeAppointmentMinutes(Integer reminderTimeBeforeAppointmentMinutes) {
        this.reminderTimeBeforeAppointmentMinutes = reminderTimeBeforeAppointmentMinutes;
    }
}
