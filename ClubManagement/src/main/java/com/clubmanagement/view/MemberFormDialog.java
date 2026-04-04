package com.clubmanagement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.TeamDTO;
import com.clubmanagement.entity.Role;
import com.clubmanagement.util.ImageUtil;
import com.toedter.calendar.JDateChooser;

/**
 * MemberFormDialog - Dialog nhập liệu Thêm/Sửa thành viên.
 *
 * Dùng chung cho cả ADD và EDIT:
 * - Khi ADD: tất cả field rỗng (trừ mặc định)
 * - Khi EDIT: điền sẵn thông tin thành viên cần sửa
 *
 * Trả về dữ liệu qua getter sau khi người dùng nhấn Lưu.
 */
public class MemberFormDialog extends JDialog {

    // ====== Form fields ======
    private JTextField  tfFullName, tfStudentId, tfEmail, tfPhone;
    private JTextField  tfAvatarPath;
    private JPasswordField pfPassword;
    private JPasswordField pfOldPassword;
    private JPasswordField pfNewPassword;
    private JComboBox<String> cbGender, cbStatus;
    private JComboBox<Role>   cbRole;
    private JDateChooser dcBirthDate;   // Format: yyyy-MM-dd
    private JList<TeamDTO> listTeams;
    private JLabel avatarPreview;
    private String avatarUrl;

    // ====== State ======
    private boolean confirmed = false; // true nếu nhấn nút Lưu
    private boolean isEdit    = false; // true nếu đang sửa (ẩn field password)
    private boolean selfEdit  = false; // true nếu user tự chỉnh sửa profile
    private boolean allowPasswordReset = false; // true nếu admin được đặt lại mật khẩu

    private static final Color PRIMARY   = new Color(37, 99, 235);
    private static final Color TEXT_DARK = new Color(15, 23, 42);

    /**
     * Constructor cho chế độ THÊM MỚI.
     * @param parent   Cửa sổ cha
     * @param roles    Danh sách vai trò từ database
     */
    public MemberFormDialog(Frame parent, List<Role> roles, List<TeamDTO> teams) {
        super(parent, "Thêm thành viên mới", true);  // modal=true: chặn cửa sổ cha
        this.isEdit = false;
        buildUI(roles, teams, null, java.util.Collections.emptyList());
    }

    /**
     * Constructor cho chế độ CHỈNH SỬA.
     * @param parent   Cửa sổ cha
     * @param roles    Danh sách vai trò
     * @param member   MemberDTO cần chỉnh sửa (điền sẵn vào form)
     */
    public MemberFormDialog(Frame parent, List<Role> roles, List<TeamDTO> teams,
                            MemberDTO member, List<Integer> selectedTeamIds) {
        super(parent, "Chỉnh sửa thành viên", true);
        this.isEdit = true;
        buildUI(roles, teams, member, selectedTeamIds);
    }

    /**
     * Constructor cho chế độ CHỈNH SỬA (Admin có thể đặt lại mật khẩu).
     */
    public MemberFormDialog(Frame parent, List<Role> roles, List<TeamDTO> teams,
                            MemberDTO member, List<Integer> selectedTeamIds,
                            boolean allowPasswordReset) {
        super(parent, "Chỉnh sửa thành viên", true);
        this.isEdit = true;
        this.allowPasswordReset = allowPasswordReset;
        buildUI(roles, teams, member, selectedTeamIds);
    }

    /**
     * Constructor cho chế độ tự chỉnh sửa thông tin cá nhân.
     */
    public MemberFormDialog(Frame parent, MemberDTO member, boolean selfEdit) {
        super(parent, "Chỉnh sửa thông tin cá nhân", true);
        this.isEdit = true;
        this.selfEdit = selfEdit;
        buildUI(null, null, member, java.util.Collections.emptyList());
    }

    /**
     * Xây dựng giao diện form. Nếu member != null → điền sẵn dữ liệu.
     */
    private void buildUI(List<Role> roles, List<TeamDTO> teams,
                         MemberDTO member, List<Integer> selectedTeamIds) {
        int baseHeight = selfEdit ? 560 : (isEdit ? 640 : 720);
        setPreferredSize(new Dimension(560, baseHeight));
        setMinimumSize(new Dimension(520, selfEdit ? 520 : 600));
        setResizable(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(24, 32, 24, 32));
        mainPanel.setBackground(Color.WHITE);

        // ---- Tiêu đề ----
        JLabel titleLabel = new JLabel(isEdit ? "Chỉnh sửa thành viên" : "Thêm thành viên mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // ---- Form fields ----
        // Họ tên
        tfFullName = addRow(mainPanel, "Họ và tên *", "Nguyễn Văn A");

        // Mã sinh viên (chỉ hiển thị khi THÊM, không cho sửa khi EDIT)
        if (!isEdit) {
            tfStudentId = addRow(mainPanel, "Mã sinh viên *", "SV001");
        } else {
            tfStudentId = null;
        }

        // Email (không cho sửa email khi EDIT để tránh mất tài khoản)
        tfEmail = addRow(mainPanel, "Email *", "example@email.com");
        if (isEdit) tfEmail.setEditable(false);

        // SĐT
        tfPhone = addRow(mainPanel, "Số điện thoại", "0901234567");

        // Ngày sinh
        dcBirthDate = addDateRow(mainPanel, "Ngày sinh");

        // Ảnh đại diện
        addAvatarRow(mainPanel);

        // Giới tính
        cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cbGender.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbGender.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbGender.setAlignmentX(Component.LEFT_ALIGNMENT);
        addLabeledRow(mainPanel, "Giới tính", cbGender);

        if (!selfEdit) {
            // Vai trò
            cbRole = new JComboBox<>();
            if (roles != null) {
                for (Role r : roles) cbRole.addItem(r);
            }
            cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
            addLabeledRow(mainPanel, "Vai trò *", cbRole);

            // Ban / Nhóm
            if (teams != null) {
                listTeams = new JList<>(teams.toArray(TeamDTO[]::new));
                listTeams.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                listTeams.setVisibleRowCount(4);
                listTeams.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                JScrollPane teamScroll = new JScrollPane(listTeams);
                teamScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
                addLabeledRow(mainPanel, "Ban / Nhóm", teamScroll);
            }

            // Trạng thái (chỉ hiện khi EDIT)
            if (isEdit) {
                cbStatus = new JComboBox<>(new String[]{"Active", "Inactive", "Suspended"});
                cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
                addLabeledRow(mainPanel, "Trạng thái", cbStatus);
            }
        }

        // Mật khẩu (chỉ hiện khi THÊM MỚI)
        if (!isEdit) {
            pfPassword = new JPasswordField();
            pfPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            pfPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
            pfPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(4, 10, 4, 10)
            ));
            addLabeledRow(mainPanel, "Mật khẩu * (≥6 ký tự)", pfPassword);
        }

        // Đổi mật khẩu (self edit)
        if (selfEdit) {
            pfOldPassword = new JPasswordField();
            pfOldPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pfOldPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            pfOldPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
            pfOldPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(4, 10, 4, 10)
            ));
            addLabeledRow(mainPanel, "Mật khẩu cũ *", pfOldPassword);

            pfNewPassword = new JPasswordField();
            pfNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pfNewPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            pfNewPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
            pfNewPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(4, 10, 4, 10)
            ));
            addLabeledRow(mainPanel, "Mật khẩu mới (≥6 ký tự)", pfNewPassword);
        }

        // Admin đặt lại mật khẩu (tùy chọn)
        if (!selfEdit && isEdit && allowPasswordReset) {
            pfNewPassword = new JPasswordField();
            pfNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pfNewPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            pfNewPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
            pfNewPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(4, 10, 4, 10)
            ));
            addLabeledRow(mainPanel, "Đặt lại mật khẩu (tùy chọn)", pfNewPassword);
        }

        // ---- Điền sẵn dữ liệu nếu là EDIT ----
        if (member != null) {
            tfFullName.setText(member.getFullName());
            tfEmail.setText(member.getEmail());
            if (member.getPhone() != null) tfPhone.setText(member.getPhone());
            if (member.getBirthDate() != null) dcBirthDate.setDate(toDate(member.getBirthDate()));
            if (member.getGender() != null) cbGender.setSelectedItem(member.getGender());
            if (member.getStatus() != null && cbStatus != null) cbStatus.setSelectedItem(member.getStatus());
            if (member.getAvatarUrl() != null) {
                avatarUrl = member.getAvatarUrl();
                tfAvatarPath.setText(member.getAvatarUrl());
            }

            // Chọn Role tương ứng trong ComboBox
            if (cbRole != null) {
                for (int i = 0; i < cbRole.getItemCount(); i++) {
                    if (cbRole.getItemAt(i).getRoleName().equals(member.getRoleName())) {
                        cbRole.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        if (listTeams != null && selectedTeamIds != null && !selectedTeamIds.isEmpty()) {
            int[] indices = java.util.stream.IntStream.range(0, listTeams.getModel().getSize())
                .filter(i -> selectedTeamIds.contains(listTeams.getModel().getElementAt(i).getTeamId()))
                .toArray();
            listTeams.setSelectedIndices(indices);
        }

        updateAvatarPreview();

        if (cbRole != null) {
            cbRole.addActionListener(e -> updateTeamSelectionState());
            updateTeamSelectionState();
        }

        // ---- Buttons ----
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buildButtonRow());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        setContentPane(scrollPane);
        pack();
        setLocationRelativeTo(getParent());
    }

    /** Thêm một hàng TextField có label vào panel. */
    private JTextField addRow(JPanel panel, String labelText, String placeholder) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        tf.setToolTipText(placeholder);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(tf);
        panel.add(Box.createVerticalStrut(12));
        return tf;
    }

    /** Thêm một ComboBox/PasswordField có label. */
    private void addLabeledRow(JPanel panel, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(component);
        panel.add(Box.createVerticalStrut(12));
    }

    /** Tạo hàng nút Lưu + Hủy. */
    private JPanel buildButtonRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setBackground(new Color(241, 245, 249));
        btnCancel.setForeground(TEXT_DARK);
        btnCancel.setBorderPainted(true);
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(90, 38));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(100, 38));
        btnSave.addActionListener(e -> onSaveClicked());

        // Enter key = nhấn Save
        getRootPane().setDefaultButton(btnSave);

        panel.add(btnCancel);
        panel.add(btnSave);
        return panel;
    }

    /**
     * Xử lý khi nhấn Lưu: validate dữ liệu cơ bản trước khi đóng dialog.
     */
    private void onSaveClicked() {
        // Validate họ tên
        if (tfFullName.getText().isBlank()) {
            showError("Vui lòng nhập Họ và tên!");
            tfFullName.requestFocus();
            return;
        }
        // Validate mã SV (chỉ khi ADD)
        if (!isEdit && tfStudentId != null && tfStudentId.getText().isBlank()) {
            showError("Vui lòng nhập Mã sinh viên!");
            return;
        }
        // Validate email
        if (tfEmail.getText().isBlank() || !tfEmail.getText().contains("@")) {
            showError("Email không hợp lệ!");
            tfEmail.requestFocus();
            return;
        }
        // Validate ngày sinh
        if (dcBirthDate.getDate() != null) {
            // Valid date chosen from picker
        }
        // Validate mật khẩu (chỉ khi ADD)
        if (!isEdit && pfPassword != null && new String(pfPassword.getPassword()).length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự!");
            pfPassword.requestFocus();
            return;
        }

        // Validate đổi mật khẩu (self edit)
        if (selfEdit && pfNewPassword != null) {
            String newPass = new String(pfNewPassword.getPassword()).trim();
            String oldPass = pfOldPassword != null ? new String(pfOldPassword.getPassword()).trim() : "";
            if (!newPass.isEmpty()) {
                if (oldPass.isEmpty()) {
                    showError("Vui lòng nhập mật khẩu cũ!");
                    pfOldPassword.requestFocus();
                    return;
                }
                if (newPass.length() < 6) {
                    showError("Mật khẩu mới phải có ít nhất 6 ký tự!");
                    pfNewPassword.requestFocus();
                    return;
                }
            }
        }

        // Validate đặt lại mật khẩu (admin edit)
        if (!selfEdit && isEdit && allowPasswordReset && pfNewPassword != null) {
            String newPass = new String(pfNewPassword.getPassword()).trim();
            if (!newPass.isEmpty() && newPass.length() < 6) {
                showError("Mật khẩu mới phải có ít nhất 6 ký tự!");
                pfNewPassword.requestFocus();
                return;
            }
        }
        confirmed = true;
        dispose();
    }

    /** Hiển thị hộp thoại lỗi nhỏ. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }

    // ===================================================
    // PUBLIC GETTERS (dùng bởi MemberController)
    // ===================================================
    public boolean isConfirmed()    { return confirmed; }
    public String getFullName()     { return tfFullName.getText().trim(); }
    public String getStudentId()    { return tfStudentId != null ? tfStudentId.getText().trim() : ""; }
    public String getEmail()        { return tfEmail.getText().trim(); }
    public String getPhone()        { return tfPhone.getText().trim(); }
    public String getGender()       { return (String) cbGender.getSelectedItem(); }
    public String getStatus()       { return cbStatus != null ? (String) cbStatus.getSelectedItem() : "Active"; }
    public Role   getSelectedRole() { return cbRole != null ? (Role) cbRole.getSelectedItem() : null; }
    public String getPassword()     { return pfPassword != null ? new String(pfPassword.getPassword()) : ""; }
    public String getAvatarUrl()    { return avatarUrl; }
    public String getOldPassword()  { return pfOldPassword != null ? new String(pfOldPassword.getPassword()) : ""; }
    public String getNewPassword()  { return pfNewPassword != null ? new String(pfNewPassword.getPassword()) : ""; }

    public List<Integer> getSelectedTeamIds() {
        if (listTeams == null) return java.util.Collections.emptyList();
        return listTeams.getSelectedValuesList().stream()
            .map(TeamDTO::getTeamId)
            .toList();
    }

    public LocalDate getBirthDate() {
        return toLocalDate(dcBirthDate.getDate());
    }

    private void updateTeamSelectionState() {
        if (listTeams != null) {
            listTeams.setEnabled(true);
        }
    }

    private void addAvatarRow(JPanel panel) {
        avatarPreview = new JLabel();
        avatarPreview.setPreferredSize(new Dimension(72, 72));
        avatarPreview.setMinimumSize(new Dimension(72, 72));
        avatarPreview.setMaximumSize(new Dimension(72, 72));
        avatarPreview.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        avatarPreview.setOpaque(true);
        avatarPreview.setBackground(new Color(241, 245, 249));

        tfAvatarPath = new JTextField();
        tfAvatarPath.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tfAvatarPath.setEditable(false);
        tfAvatarPath.setPreferredSize(new Dimension(200, 36));
        tfAvatarPath.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tfAvatarPath.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(4, 10, 4, 10)
        ));

        JButton btnChoose = new JButton("Chọn ảnh");
        btnChoose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChoose.addActionListener(e -> chooseAvatar());

        JButton btnClear = new JButton("Xóa");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.addActionListener(e -> clearAvatar());

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        row.add(avatarPreview, BorderLayout.WEST);
        row.add(tfAvatarPath, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(btnChoose);
        actions.add(btnClear);
        row.add(actions, BorderLayout.EAST);

        addLabeledRow(panel, "Ảnh đại diện", row);
    }

    private void chooseAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Image files", "png", "jpg", "jpeg", "gif", "webp"
        ));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                avatarUrl = file.getAbsolutePath();
                tfAvatarPath.setText(avatarUrl);
                updateAvatarPreview();
            }
        }
    }

    private void clearAvatar() {
        avatarUrl = null;
        tfAvatarPath.setText("");
        updateAvatarPreview();
    }

    private void updateAvatarPreview() {
        if (avatarPreview == null) return;
        String initials = ImageUtil.buildInitials(tfFullName != null ? tfFullName.getText() : null);
        avatarPreview.setIcon(ImageUtil.loadSquareAvatar(
            avatarUrl, 72, initials, new Color(226, 232, 240), TEXT_DARK
        ));
    }

    private JDateChooser addDateRow(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("yyyy-MM-dd");
        chooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chooser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        chooser.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(chooser);
        panel.add(Box.createVerticalStrut(12));
        return chooser;
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
