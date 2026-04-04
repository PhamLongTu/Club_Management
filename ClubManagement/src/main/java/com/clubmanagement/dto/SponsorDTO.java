package com.clubmanagement.dto;

import java.math.BigDecimal;

/**
 * SponsorDTO - Data Transfer Object của Nhà tài trợ
 */
public class SponsorDTO {
    
    private Integer sponsorId;
    private String sponsorName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String sponsorshipType;
    private BigDecimal totalAmount;

    /** Constructor mặc định. */
    public SponsorDTO() {}

    /**
     * Constructor đầy đủ.
     */
    public SponsorDTO(Integer sponsorId, String sponsorName, String contactPerson, 
                      String email, String phone, String address, 
                      String sponsorshipType, BigDecimal totalAmount) {
        this.sponsorId = sponsorId;
        this.sponsorName = sponsorName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.sponsorshipType = sponsorshipType;
        this.totalAmount = totalAmount;
    }

    /** @return ID nhà tài trợ. */
    public Integer getSponsorId() { return sponsorId; }
    /** @param sponsorId ID nhà tài trợ mới. */
    public void setSponsorId(Integer sponsorId) { this.sponsorId = sponsorId; }

    /** @return Tên nhà tài trợ. */
    public String getSponsorName() { return sponsorName; }
    /** @param sponsorName Tên nhà tài trợ mới. */
    public void setSponsorName(String sponsorName) { this.sponsorName = sponsorName; }

    /** @return Người liên hệ. */
    public String getContactPerson() { return contactPerson; }
    /** @param contactPerson Người liên hệ mới. */
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    /** @return Email liên hệ. */
    public String getEmail() { return email; }
    /** @param email Email liên hệ mới. */
    public void setEmail(String email) { this.email = email; }

    /** @return Số điện thoại. */
    public String getPhone() { return phone; }
    /** @param phone Số điện thoại mới. */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return Địa chỉ. */
    public String getAddress() { return address; }
    /** @param address Địa chỉ mới. */
    public void setAddress(String address) { this.address = address; }

    /** @return Hình thức tài trợ. */
    public String getSponsorshipType() { return sponsorshipType; }
    /** @param sponsorshipType Hình thức tài trợ mới. */
    public void setSponsorshipType(String sponsorshipType) { this.sponsorshipType = sponsorshipType; }

    /** @return Tổng giá trị tài trợ. */
    public BigDecimal getTotalAmount() { return totalAmount; }
    /** @param totalAmount Tổng giá trị tài trợ mới. */
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    /**
     * Hiển thị tên nhà tài trợ trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() {
        return sponsorName;
    }
}
