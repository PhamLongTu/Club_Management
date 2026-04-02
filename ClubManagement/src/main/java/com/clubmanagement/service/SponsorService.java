package com.clubmanagement.service;

import com.clubmanagement.dao.SponsorDAO;
import com.clubmanagement.dto.SponsorDTO;
import com.clubmanagement.entity.Sponsor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SponsorService {

    private final SponsorDAO sponsorDAO = new SponsorDAO();

    public SponsorDTO createSponsor(String name, String contactPerson, String email, 
                                    String phone, String type, BigDecimal amount, String address) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên Nhà tài trợ không được để trống!");
        }
        
        Sponsor sponsor = new Sponsor(name.trim(), contactPerson, email, phone, type, amount);
        sponsor.setAddress(address);
        
        return toDTO(sponsorDAO.save(sponsor));
    }

    public List<SponsorDTO> getAllSponsors() {
        return sponsorDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public SponsorDTO updateSponsor(Integer id, String name, String contactPerson, String email, 
                                    String phone, String type, BigDecimal amount, String address) {
        Sponsor sponsor = sponsorDAO.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Nhà tài trợ!"));

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên Nhà tài trợ không được để trống!");
        }

        sponsor.setSponsorName(name.trim());
        sponsor.setContactPerson(contactPerson);
        sponsor.setEmail(email);
        sponsor.setPhone(phone);
        sponsor.setSponsorshipType(type);
        sponsor.setTotalAmount(amount);
        sponsor.setAddress(address);
        
        return toDTO(sponsorDAO.update(sponsor));
    }

    public void deleteSponsor(Integer id) {
        if (!sponsorDAO.deleteById(id)) {
            throw new IllegalArgumentException("Không thể tìm thấy Nhà tài trợ để xóa!");
        }
    }

    private SponsorDTO toDTO(Sponsor s) {
        if (s == null) return null;
        return new SponsorDTO(
            s.getSponsorId(),
            s.getSponsorName(),
            s.getContactPerson(),
            s.getEmail(),
            s.getPhone(),
            s.getAddress(),
            s.getSponsorshipType(),
            s.getTotalAmount()
        );
    }
}
