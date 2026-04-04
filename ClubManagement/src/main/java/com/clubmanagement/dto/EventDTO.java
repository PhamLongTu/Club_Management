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
    /** Constructor mặc định. */
    public EventDTO() {}

    /**
     * Constructor đầy đủ.
     */
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
    /** @return ID sự kiện. */
    public Integer getEventId()              { return eventId; }
    /** @param v ID sự kiện mới. */
    public void setEventId(Integer v)        { this.eventId = v; }

    /** @return Tên sự kiện. */
    public String getEventName()             { return eventName; }
    /** @param v Tên sự kiện mới. */
    public void setEventName(String v)       { this.eventName = v; }

    /** @return Mô tả sự kiện. */
    public String getDescription()           { return description; }
    /** @param v Mô tả mới. */
    public void setDescription(String v)     { this.description = v; }

    /** @return Thời gian bắt đầu. */
    public LocalDateTime getStartDate()          { return startDate; }
    /** @param v Thời gian bắt đầu mới. */
    public void setStartDate(LocalDateTime v)    { this.startDate = v; }

    /** @return Thời gian kết thúc. */
    public LocalDateTime getEndDate()            { return endDate; }
    /** @param v Thời gian kết thúc mới. */
    public void setEndDate(LocalDateTime v)      { this.endDate = v; }

    /** @return Địa điểm tổ chức. */
    public String getLocation()              { return location; }
    /** @param v Địa điểm mới. */
    public void setLocation(String v)        { this.location = v; }

    /** @return Trạng thái sự kiện. */
    public String getStatus()                { return status; }
    /** @param v Trạng thái mới. */
    public void setStatus(String v)          { this.status = v; }

    /** @return Ngân sách sự kiện. */
    public BigDecimal getBudget()            { return budget; }
    /** @param v Ngân sách mới. */
    public void setBudget(BigDecimal v)      { this.budget = v; }

    /** @return Số lượng tham gia tối đa. */
    public Integer getMaxParticipants()      { return maxParticipants; }
    /** @param v Số lượng tham gia tối đa mới. */
    public void setMaxParticipants(Integer v){ this.maxParticipants = v; }

    /** @return Hạn đăng ký. */
    public LocalDateTime getRegistrationDeadline()       { return registrationDeadline; }
    /** @param v Hạn đăng ký mới. */
    public void setRegistrationDeadline(LocalDateTime v) { this.registrationDeadline = v; }

    /** @return Tên người tạo. */
    public String getCreatedByName()         { return createdByName; }
    /** @param v Tên người tạo mới. */
    public void setCreatedByName(String v)   { this.createdByName = v; }

    /** @return Số lượng đã đăng ký. */
    public Integer getRegisteredCount()      { return registeredCount; }
    /** @param v Số lượng đã đăng ký mới. */
    public void setRegisteredCount(Integer v){ this.registeredCount = v; }

    /**
     * Hiển thị tên sự kiện trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return eventName; }
}
