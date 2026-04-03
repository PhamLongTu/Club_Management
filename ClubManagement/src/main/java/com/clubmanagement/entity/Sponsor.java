package com.clubmanagement.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity: Sponsor (Nhà tài trợ)
 * Map tới bảng 'sponsors'.
 */
@Entity
@Table(name = "sponsors")
public class Sponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sponsor_id")
    private Integer sponsorId;

    /** Tên tổ chức/cá nhân tài trợ */
    @Column(name = "sponsor_name", nullable = false, length = 200)
    private String sponsorName;

    /** Người liên hệ */
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    /** Email liên hệ */
    @Column(name = "email", length = 100)
    private String email;

    /** Số điện thoại */
    @Column(name = "phone", length = 15)
    private String phone;

    /** Địa chỉ */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Hình thức tài trợ: Cash / InKind / Media / Other
     */
    @Column(name = "sponsorship_type", length = 20)
    private String sponsorshipType = "Cash";

    /** Tổng giá trị tài trợ */
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // ============ CONSTRUCTORS ============
    public Sponsor() {}

    public Sponsor(String sponsorName, String contactPerson, String email,
                   String phone, String sponsorshipType, BigDecimal totalAmount) {
        this.sponsorName     = sponsorName;
        this.contactPerson   = contactPerson;
        this.email           = email;
        this.phone           = phone;
        this.sponsorshipType = sponsorshipType;
        this.totalAmount     = totalAmount;
    }

    // ============ GETTERS & SETTERS ============
    public Integer getSponsorId()            { return sponsorId; }
    public void setSponsorId(Integer v)      { this.sponsorId = v; }

    public String getSponsorName()           { return sponsorName; }
    public void setSponsorName(String v)     { this.sponsorName = v; }

    public String getContactPerson()         { return contactPerson; }
    public void setContactPerson(String v)   { this.contactPerson = v; }

    public String getEmail()                 { return email; }
    public void setEmail(String v)           { this.email = v; }

    public String getPhone()                 { return phone; }
    public void setPhone(String v)           { this.phone = v; }

    public String getAddress()               { return address; }
    public void setAddress(String v)         { this.address = v; }

    public String getSponsorshipType()       { return sponsorshipType; }
    public void setSponsorshipType(String v) { this.sponsorshipType = v; }

    public BigDecimal getTotalAmount()        { return totalAmount; }
    public void setTotalAmount(BigDecimal v)  { this.totalAmount = v; }

    @Override
    public String toString() { return sponsorName; }
}
