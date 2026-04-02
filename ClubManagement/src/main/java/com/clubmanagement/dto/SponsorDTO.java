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

    public SponsorDTO() {}

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

    public Integer getSponsorId() { return sponsorId; }
    public void setSponsorId(Integer sponsorId) { this.sponsorId = sponsorId; }

    public String getSponsorName() { return sponsorName; }
    public void setSponsorName(String sponsorName) { this.sponsorName = sponsorName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSponsorshipType() { return sponsorshipType; }
    public void setSponsorshipType(String sponsorshipType) { this.sponsorshipType = sponsorshipType; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    @Override
    public String toString() {
        return sponsorName;
    }
}
