package com.clubmanagement.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.TeamDTO;
import com.clubmanagement.entity.Role;

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
    private JPasswordField pfPassword;
    private JComboBox<String> cbGender, cbStatus;
    private JComboBox<Role>   cbRole;
    private JTextField  tfBirthDate;   // Format: yyyy-MM-dd
    private JList<TeamDTO> listTeams;

    // ====== State ======
    private boolean confirmed = false; // true nếu nhấn nút Lưu
    private boolean isEdit    = false; // true nếu đang sửa (ẩn field password)

    private static final Color PRIMARY   = new Color(37, 99, 235);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
     * Xây dựng giao diện form. Nếu member != null → điền sẵn dữ liệu.
     */
    private void buildUI(List<Role> roles, List<TeamDTO> teams,
                         MemberDTO member, List<Integer> selectedTeamIds) {
        setSize(480, isEdit ? 540 : 600);
        setLocationRelativeTo(getParent());
        setResizable(false);

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
        tfBirthDate = addRow(mainPanel, "Ngày sinh (yyyy-MM-dd)", "2002-01-01");

        // Giới tính
        cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cbGender.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbGender.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbGender.setAlignmentX(Component.LEFT_ALIGNMENT);
        addLabeledRow(mainPanel, "Giới tính", cbGender);

        // Vai trò
        cbRole = new JComboBox<>();
        for (Role r : roles) cbRole.addItem(r);
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        addLabeledRow(mainPanel, "Vai trò *", cbRole);

        // Ban / Nhóm
        listTeams = new JList<>(teams.toArray(TeamDTO[]::new));
        listTeams.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listTeams.setVisibleRowCount(4);
        listTeams.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane teamScroll = new JScrollPane(listTeams);
        teamScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        addLabeledRow(mainPanel, "Ban / Nhóm (Member)", teamScroll);

        // Trạng thái (chỉ hiện khi EDIT)
        if (isEdit) {
            cbStatus = new JComboBox<>(new String[]{"Active", "Inactive", "Suspended"});
            cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
            addLabeledRow(mainPanel, "Trạng thái", cbStatus);
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

        // ---- Điền sẵn dữ liệu nếu là EDIT ----
        if (member != null) {
            tfFullName.setText(member.getFullName());
            tfEmail.setText(member.getEmail());
            if (member.getPhone() != null) tfPhone.setText(member.getPhone());
            if (member.getBirthDate() != null) tfBirthDate.setText(member.getBirthDate().toString());
            if (member.getGender() != null) cbGender.setSelectedItem(member.getGender());
            if (member.getStatus() != null && cbStatus != null) cbStatus.setSelectedItem(member.getStatus());

            // Chọn Role tương ứng trong ComboBox
            for (int i = 0; i < cbRole.getItemCount(); i++) {
                if (cbRole.getItemAt(i).getRoleName().equals(member.getRoleName())) {
                    cbRole.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (selectedTeamIds != null && !selectedTeamIds.isEmpty()) {
            int[] indices = java.util.stream.IntStream.range(0, listTeams.getModel().getSize())
                .filter(i -> selectedTeamIds.contains(listTeams.getModel().getElementAt(i).getTeamId()))
                .toArray();
            listTeams.setSelectedIndices(indices);
        }

        cbRole.addActionListener(e -> updateTeamSelectionState());
        updateTeamSelectionState();

        // ---- Buttons ----
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buildButtonRow());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        setContentPane(scrollPane);
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
        if (!tfBirthDate.getText().isBlank()) {
            try {
                LocalDate.parse(tfBirthDate.getText(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                showError("Ngày sinh sai định dạng! Dùng: yyyy-MM-dd");
                tfBirthDate.requestFocus();
                return;
            }
        }
        // Validate mật khẩu (chỉ khi ADD)
        if (!isEdit && pfPassword != null && new String(pfPassword.getPassword()).length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự!");
            pfPassword.requestFocus();
            return;
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
    public Role   getSelectedRole() { return (Role) cbRole.getSelectedItem(); }
    public String getPassword()     { return pfPassword != null ? new String(pfPassword.getPassword()) : ""; }

    public List<Integer> getSelectedTeamIds() {
        return listTeams.getSelectedValuesList().stream()
            .map(TeamDTO::getTeamId)
            .toList();
    }

    public LocalDate getBirthDate() {
        try {
            String txt = tfBirthDate.getText().trim();
            return txt.isBlank() ? null : LocalDate.parse(txt, DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void updateTeamSelectionState() {
        Role role = (Role) cbRole.getSelectedItem();
        boolean isMember = role != null && role.getPermissionLevel() != null && role.getPermissionLevel() < 2;
        listTeams.setEnabled(isMember);
    }
}
