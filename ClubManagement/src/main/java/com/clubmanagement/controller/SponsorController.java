package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.SponsorDTO;
import com.clubmanagement.service.SponsorService;
import com.clubmanagement.view.SponsorView;

public class SponsorController {

    private final SponsorView view;
    private final MemberDTO currentUser;
    private final SponsorService sponsorService = new SponsorService();

    public SponsorController(SponsorView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllSponsorsInternal();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllSponsors());

        if (currentUser.isLeader()) {
            view.getBtnAdd().addActionListener(e -> handleAdd());
            view.getBtnEdit().addActionListener(e -> handleEdit());
            view.getBtnDelete().addActionListener(e -> handleDelete());

            view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) handleEdit();
                }
            });
        }
    }

    private void loadAllSponsorsInternal() {
        view.setStatusMessage("Đang tải danh sách Nhà tài trợ...");
        SwingWorker<List<SponsorDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SponsorDTO> doInBackground() {
                return sponsorService.getAllSponsors();
            }
            @Override
            protected void done() {
                try {
                    view.loadData(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusMessage("Lỗi: " + ex.getMessage());
                } catch (ExecutionException ex) {
                    view.setStatusMessage("Lỗi: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    public final void loadAllSponsors() {
        loadAllSponsorsInternal();
    }

    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleEdit() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Tài trợ cần sửa!");
            return;
        }

        List<SponsorDTO> all = sponsorService.getAllSponsors();
        Optional<SponsorDTO> opt = all.stream().filter(s -> s.getSponsorId().equals(id)).findFirst();
        if (opt.isEmpty()) return;

        showFormDialog(opt.get());
    }

    private void showFormDialog(SponsorDTO sponsor) {
        JDialog dialog = new JDialog((Frame) null, sponsor == null ? "Thêm Nhà Tài Trợ" : "Sửa Thông Tin", true);
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField txtName = new JTextField(); txtName.setFont(f);
        if (sponsor != null) txtName.setText(sponsor.getSponsorName());

        JTextField txtContact = new JTextField(); txtContact.setFont(f);
        if (sponsor != null) txtContact.setText(sponsor.getContactPerson());

        JTextField txtEmail = new JTextField(); txtEmail.setFont(f);
        if (sponsor != null) txtEmail.setText(sponsor.getEmail());

        JTextField txtPhone = new JTextField(); txtPhone.setFont(f);
        if (sponsor != null) txtPhone.setText(sponsor.getPhone());
        
        JTextField txtAddr = new JTextField(); txtAddr.setFont(f);
        if (sponsor != null) txtAddr.setText(sponsor.getAddress());

        JComboBox<String> cbType = new JComboBox<>(new String[]{"Cash", "InKind", "Media", "Other"});
        cbType.setFont(f);
        if (sponsor != null) cbType.setSelectedItem(sponsor.getSponsorshipType());
        
        JTextField txtAmount = new JTextField(); txtAmount.setFont(f);
        if (sponsor != null && sponsor.getTotalAmount() != null) txtAmount.setText(sponsor.getTotalAmount().toString());
        else txtAmount.setText("0");

        panel.add(new JLabel("Tên nhà tài trợ *:")); panel.add(txtName);
        panel.add(new JLabel("Người liên hệ:"));     panel.add(txtContact);
        panel.add(new JLabel("Email:"));             panel.add(txtEmail);
        panel.add(new JLabel("Điện thoại:"));        panel.add(txtPhone);
        panel.add(new JLabel("Địa chỉ:"));           panel.add(txtAddr);
        panel.add(new JLabel("Hình thức:"));         panel.add(cbType);
        panel.add(new JLabel("Tổng GT (VNĐ):"));     panel.add(txtAmount);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText();
                String contact = txtContact.getText();
                String email = txtEmail.getText();
                String phone = txtPhone.getText();
                String addr = txtAddr.getText();
                String type = (String) cbType.getSelectedItem();
                
                BigDecimal amount;
                try {
                    amount = new BigDecimal(txtAmount.getText().trim());
                } catch (NumberFormatException ex) {
                    amount = BigDecimal.ZERO;
                }

                if (sponsor == null) {
                    sponsorService.createSponsor(name, contact, email, phone, type, amount, addr);
                    JOptionPane.showMessageDialog(dialog, "Đã thêm nhà tài trợ mới!");
                } else {
                    sponsorService.updateSponsor(sponsor.getSponsorId(), name, contact, email, phone, type, amount, addr);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật nhà tài trợ!");
                }
                dialog.dispose();
                loadAllSponsors();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnCancel);
        bottom.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleDelete() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Nhà tài trợ cần xóa!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc muốn xóa ?", "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                sponsorService.deleteSponsor(id);
                JOptionPane.showMessageDialog(null, "Đã xóa!");
                loadAllSponsors();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi Ràng Buộc", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
