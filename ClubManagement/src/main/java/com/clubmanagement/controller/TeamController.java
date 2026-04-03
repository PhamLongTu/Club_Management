package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
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
import com.clubmanagement.dto.TeamDTO;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.TeamService;
import com.clubmanagement.view.TeamView;

public class TeamController {

    private final TeamView view;
    private final MemberDTO currentUser;
    private final TeamService teamService = new TeamService();
    private final MemberService memberService = new MemberService();

    public TeamController(TeamView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllTeamsInternal();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllTeams());
        view.getBtnViewMembers().addActionListener(e -> handleViewMembers());

        if (currentUser.isAdmin()) {
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

    private void loadAllTeamsInternal() {
        view.setStatusMessage("Đang tải danh sách Ban/Nhóm...");
        SwingWorker<List<TeamDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TeamDTO> doInBackground() {
                return teamService.getAllTeams();
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

    public final void loadAllTeams() {
        loadAllTeamsInternal();
    }

    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleEdit() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Ban cần sửa!");
            return;
        }

        List<TeamDTO> all = teamService.getAllTeams();
        Optional<TeamDTO> opt = all.stream().filter(t -> t.getTeamId().equals(id)).findFirst();
        if (opt.isEmpty()) return;

        showFormDialog(opt.get());
    }

    private void showFormDialog(TeamDTO team) {
        JDialog dialog = new JDialog((Frame) null, team == null ? "Tạo Ban mới" : "Sửa thông tin Ban", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField txtName = new JTextField();
        txtName.setFont(f);
        if (team != null) txtName.setText(team.getTeamName());

        JTextField txtDesc = new JTextField();
        txtDesc.setFont(f);
        if (team != null) txtDesc.setText(team.getDescription());

        List<MemberDTO> allMembers = memberService.getAllMembers();
        JComboBox<MemberDTO> cbLeader = new JComboBox<>(allMembers.toArray(MemberDTO[]::new));
        cbLeader.setFont(f);
        
        // Chọn sẵn leader nếu là edit
        if (team != null && team.getLeaderId() != null) {
            for (int i = 0; i < cbLeader.getItemCount(); i++) {
                if (cbLeader.getItemAt(i).getMemberId().equals(team.getLeaderId())) {
                    cbLeader.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cbLeader.setSelectedItem(null);
        }

        panel.add(new JLabel("Tên Ban / Nhóm *:")); panel.add(txtName);
        panel.add(new JLabel("Trưởng ban:"));       panel.add(cbLeader);
        panel.add(new JLabel("Mô tả:"));            panel.add(txtDesc);

        JButton btnSave = new JButton("Lưu lại");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText();
                String desc = txtDesc.getText();
                MemberDTO leader = (MemberDTO) cbLeader.getSelectedItem();
                Integer leaderId = leader != null ? leader.getMemberId() : null;

                // Để ngăn chặn Swing freeze, call service trực tiếp ở đây cũng được vì createTeam chạy nhanh.
                if (team == null) {
                    teamService.createTeam(name, desc, leaderId);
                    JOptionPane.showMessageDialog(dialog, "Đã tạo Ban thành công!");
                } else {
                    teamService.updateTeam(team.getTeamId(), name, desc, leaderId);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật thông tin Ban!");
                }
                
                dialog.dispose();
                loadAllTeams();
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
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Ban cần xóa!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc muốn xóa Ban này?\nLưu ý: Các thành viên trong ban sẽ bị gỡ khỏi ban nhưng không bị xóa khỏi CLB.",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                teamService.deleteTeam(id);
                JOptionPane.showMessageDialog(null, "Đã xóa Xong!");
                loadAllTeams();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewMembers() {
        Integer teamId = view.getSelectedId();
        String teamName = view.getSelectedName();
        if (teamId == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn một Ban để xem thành viên!");
            return;
        }

        // TODO: Mở popup chứa danh sách thành viên trong Ban (Bảng member_team theo hibernate)
        // Hiện tại Member_Team chưa thiết lập đầy đủ trong MemberService/DAO,
        // Nhưng tạm hiển thị thông báo.
        JOptionPane.showMessageDialog(null, "Tính năng quản lý thành viên chuyên sâu của Ban " + teamName + " đang được phát triển!", "Tính năng mở rộng", JOptionPane.INFORMATION_MESSAGE);
    }
}
