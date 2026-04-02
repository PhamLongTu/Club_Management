package com.clubmanagement.controller;

import com.clubmanagement.dto.DocumentDTO;
import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.service.DocumentService;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.view.DocumentView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class DocumentController {

    private final DocumentView view;
    private final MemberDTO currentUser;
    private final DocumentService documentService = new DocumentService();
    private final EventService eventService = new EventService();
    private final ProjectService projectService = new ProjectService();

    public DocumentController(DocumentView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllDocuments();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllDocuments());
        view.getBtnOpen().addActionListener(e -> handleOpenFile());

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
        } else {
            view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) handleOpenFile();
                }
            });
        }
    }

    public void loadAllDocuments() {
        view.setStatusMessage("Đang tải danh sách Tài liệu...");
        SwingWorker<List<DocumentDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DocumentDTO> doInBackground() {
                return documentService.getAllDocuments();
            }
            @Override
            protected void done() {
                try {
                    view.loadData(get());
                } catch (Exception e) {
                    view.setStatusMessage("Lỗi: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleOpenFile() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Tài liệu để mở!");
            return;
        }

        List<DocumentDTO> all = documentService.getAllDocuments();
        Optional<DocumentDTO> opt = all.stream().filter(d -> d.getDocumentId().equals(id)).findFirst();
        if (opt.isEmpty() || opt.get().getFilePath() == null || opt.get().getFilePath().isBlank()) {
            JOptionPane.showMessageDialog(null, "File không tồn tại trên hệ thống!");
            return;
        }

        try {
            File pdfFile = new File(opt.get().getFilePath());
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(null, "Hệ điều hành không hỗ trợ Action Desktop!");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Không tìm thấy file ở đường dẫn: " + opt.get().getFilePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Lỗi khi mở file: " + ex.getMessage());
        }
    }

    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleEdit() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Tài liệu cần sửa!");
            return;
        }

        List<DocumentDTO> all = documentService.getAllDocuments();
        Optional<DocumentDTO> opt = all.stream().filter(d -> d.getDocumentId().equals(id)).findFirst();
        if (opt.isEmpty()) return;

        showFormDialog(opt.get());
    }

    private void showFormDialog(DocumentDTO document) {
        JDialog dialog = new JDialog((Frame) null, document == null ? "📂 Upload Tài liệu" : "✏ Sửa Thông tin Tài liệu", true);
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(8, 2, 8, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField txtTitle = new JTextField(); txtTitle.setFont(f);
        if (document != null) txtTitle.setText(document.getTitle());

        // File picker
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        JTextField txtFile = new JTextField(); txtFile.setFont(f);
        txtFile.setEditable(false);
        if (document != null) txtFile.setText(document.getFilePath());
        JButton btnBrowse = new JButton("...");
        btnBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                txtFile.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        filePanel.add(txtFile, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);

        JTextField txtType = new JTextField(); txtType.setFont(f);
        if(document != null) txtType.setText(document.getFileType());

        JTextField txtDesc = new JTextField(); txtDesc.setFont(f);
        if(document != null) txtDesc.setText(document.getDescription());

        JComboBox<String> cbAccess = new JComboBox<>(new String[]{"Public (Công khai)", "Private (Nội bộ lãnh đạo)"});
        cbAccess.setFont(f);
        if(document != null && Boolean.FALSE.equals(document.getIsPublic())) cbAccess.setSelectedIndex(1);

        JComboBox<EventDTO> cbEvent = new JComboBox<>(eventService.getAllEvents().toArray(new EventDTO[0]));
        cbEvent.insertItemAt(null, 0); cbEvent.setFont(f);
        
        JComboBox<ProjectDTO> cbProject = new JComboBox<>(projectService.getAllProjects().toArray(new ProjectDTO[0]));
        cbProject.insertItemAt(null, 0); cbProject.setFont(f);

        if (document != null) {
            cbEvent.setEnabled(false); // Không đổi liên kết sau khi up
            cbProject.setEnabled(false);
            for (int i = 0; i < cbEvent.getItemCount(); i++) {
                if (cbEvent.getItemAt(i) != null && cbEvent.getItemAt(i).getEventId().equals(document.getEventId())) {
                    cbEvent.setSelectedIndex(i); break;
                }
            }
            // Tương tự cho Project
            for (int i = 0; i < cbProject.getItemCount(); i++) {
                if (cbProject.getItemAt(i) != null && cbProject.getItemAt(i).getProjectId().equals(document.getProjectId())) {
                    cbProject.setSelectedIndex(i); break;
                }
            }
        } else {
            cbEvent.setSelectedIndex(0); cbProject.setSelectedIndex(0);
        }

        panel.add(new JLabel("Tiêu đề *:"));      panel.add(txtTitle);
        panel.add(new JLabel("Chọn File:"));      panel.add(filePanel);
        panel.add(new JLabel("Đuôi mở rộng:"));   panel.add(txtType);
        panel.add(new JLabel("Sự kiện LH:"));     panel.add(cbEvent);
        panel.add(new JLabel("Dự án LH:"));       panel.add(cbProject);
        panel.add(new JLabel("Quyền truy cập:")); panel.add(cbAccess);
        panel.add(new JLabel("Mô tả thêm:"));     panel.add(txtDesc);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                String title = txtTitle.getText();
                String path = txtFile.getText();
                String type = txtType.getText();
                String desc = txtDesc.getText();
                boolean isPublic = cbAccess.getSelectedIndex() == 0;
                
                EventDTO ext = (EventDTO) cbEvent.getSelectedItem();
                ProjectDTO pro = (ProjectDTO) cbProject.getSelectedItem();
                Integer eid = ext != null ? ext.getEventId() : null;
                Integer pid = pro != null ? pro.getProjectId() : null;

                if (document == null) {
                    documentService.uploadDocument(title, path, type, desc, isPublic, currentUser.getMemberId(), eid, pid);
                    JOptionPane.showMessageDialog(dialog, "Tải lên thành công!");
                } else {
                    documentService.updateDocument(document.getDocumentId(), title, path, type, desc, isPublic);
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                }
                dialog.dispose();
                loadAllDocuments();
            } catch (Exception ex) {
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
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Tài liệu cần xóa!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc muốn xóa ?", "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                documentService.deleteDocument(id);
                JOptionPane.showMessageDialog(null, "Đã xóa!");
                loadAllDocuments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
