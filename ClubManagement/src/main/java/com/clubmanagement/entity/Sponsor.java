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
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Sponsor() {}

    /**
     * Constructor khởi tạo nhanh nhà tài trợ.
     *
     * @param sponsorName     Tên nhà tài trợ
     * @param contactPerson   Người liên hệ
     * @param email           Email liên hệ
     * @param phone           Số điện thoại
     * @param sponsorshipType Hình thức tài trợ
     * @param totalAmount     Tổng giá trị tài trợ
     */
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
    /** @return ID nhà tài trợ. */
    public Integer getSponsorId()            { return sponsorId; }
    /** @param v ID nhà tài trợ mới. */
    public void setSponsorId(Integer v)      { this.sponsorId = v; }

    /** @return Tên nhà tài trợ. */
    public String getSponsorName()           { return sponsorName; }
    /** @param v Tên nhà tài trợ mới. */
    public void setSponsorName(String v)     { this.sponsorName = v; }

    /** @return Người liên hệ. */
    public String getContactPerson()         { return contactPerson; }
    /** @param v Người liên hệ mới. */
    public void setContactPerson(String v)   { this.contactPerson = v; }

    /** @return Email liên hệ. */
    public String getEmail()                 { return email; }
    /** @param v Email liên hệ mới. */
    public void setEmail(String v)           { this.email = v; }

    /** @return Số điện thoại. */
    public String getPhone()                 { return phone; }
    /** @param v Số điện thoại mới. */
    public void setPhone(String v)           { this.phone = v; }

    /** @return Địa chỉ. */
    public String getAddress()               { return address; }
    /** @param v Địa chỉ mới. */
    public void setAddress(String v)         { this.address = v; }

    /** @return Hình thức tài trợ. */
    public String getSponsorshipType()       { return sponsorshipType; }
    /** @param v Hình thức tài trợ mới. */
    public void setSponsorshipType(String v) { this.sponsorshipType = v; }

    /** @return Tổng giá trị tài trợ. */
    public BigDecimal getTotalAmount()        { return totalAmount; }
    /** @param v Tổng giá trị tài trợ mới. */
    public void setTotalAmount(BigDecimal v)  { this.totalAmount = v; }

    /**
     * Hiển thị tên nhà tài trợ trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return sponsorName; }
}
