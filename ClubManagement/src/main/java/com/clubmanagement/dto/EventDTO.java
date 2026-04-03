package com.clubmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO: EventDTO (Data Transfer Object cho Sự kiện)
 * Chứa thông tin hiển thị sự kiện trên UI bao gồm số lượng đăng ký.
 */
public class EventDTO {

    private Integer       eventId;
    private String        eventName;
    private String        description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String        location;
    private String        status;
    private BigDecimal    budget;
    private Integer       maxParticipants;
    private LocalDateTime registrationDeadline;
    private String        createdByName;    // Tên người tạo (từ Member)
    private Integer       registeredCount;  // Số lượng đã đăng ký (computed)

    // ============ CONSTRUCTORS ============
    public EventDTO() {}

    public EventDTO(Integer eventId, String eventName, String description,
                    LocalDateTime startDate, LocalDateTime endDate, String location,
                    String status, BigDecimal budget, Integer maxParticipants,
                    LocalDateTime registrationDeadline,
                    String createdByName, Integer registeredCount) {
        this.eventId          = eventId;
        this.eventName        = eventName;
        this.description      = description;
        this.startDate        = startDate;
        this.endDate          = endDate;
        this.location         = location;
        this.status           = status;
        this.budget           = budget;
        this.maxParticipants  = maxParticipants;
        this.registrationDeadline = registrationDeadline;
        this.createdByName    = createdByName;
        this.registeredCount  = registeredCount;
    }

    // ============ GETTERS & SETTERS ============
    public Integer getEventId()              { return eventId; }
    public void setEventId(Integer v)        { this.eventId = v; }

    public String getEventName()             { return eventName; }
    public void setEventName(String v)       { this.eventName = v; }

    public String getDescription()           { return description; }
    public void setDescription(String v)     { this.description = v; }

    public LocalDateTime getStartDate()          { return startDate; }
    public void setStartDate(LocalDateTime v)    { this.startDate = v; }

    public LocalDateTime getEndDate()            { return endDate; }
    public void setEndDate(LocalDateTime v)      { this.endDate = v; }

    public String getLocation()              { return location; }
    public void setLocation(String v)        { this.location = v; }

    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }

    public BigDecimal getBudget()            { return budget; }
    public void setBudget(BigDecimal v)      { this.budget = v; }

    public Integer getMaxParticipants()      { return maxParticipants; }
    public void setMaxParticipants(Integer v){ this.maxParticipants = v; }

    public LocalDateTime getRegistrationDeadline()       { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDateTime v) { this.registrationDeadline = v; }

    public String getCreatedByName()         { return createdByName; }
    public void setCreatedByName(String v)   { this.createdByName = v; }

    public Integer getRegisteredCount()      { return registeredCount; }
    public void setRegisteredCount(Integer v){ this.registeredCount = v; }

    @Override
    public String toString() { return eventName; }
}
