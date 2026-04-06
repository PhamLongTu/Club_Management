-- ============================================================
-- DATABASE: CLUB MANAGEMENT SYSTEM
-- Hệ thống Quản lý Câu lạc bộ Trường học
-- Version: 1.0
-- Author: Windows Programming Course - Final Project
-- ============================================================

DROP DATABASE IF EXISTS club_management;
CREATE DATABASE club_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE club_management;

-- ============================================================
-- TABLE: roles
-- Vai trò trong CLB (Admin, Leader, Member, ...)
-- ============================================================
CREATE TABLE roles (
    role_id     INT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Tên vai trò',
    description VARCHAR(255) COMMENT 'Mô tả vai trò',
    permission_level INT DEFAULT 1 COMMENT '1=Member, 2=Leader, 3=Admin'
) ENGINE=InnoDB COMMENT='Bảng vai trò';

-- ============================================================
-- TABLE: members
-- Thông tin thành viên CLB
-- ============================================================
CREATE TABLE members (
    member_id   INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL COMMENT 'Họ và tên',
    student_id  VARCHAR(20)  NOT NULL UNIQUE COMMENT 'Mã sinh viên',
    email       VARCHAR(100) NOT NULL UNIQUE COMMENT 'Email',
    phone       VARCHAR(15)  COMMENT 'Số điện thoại',
    gender      VARCHAR(10)  DEFAULT 'Other' COMMENT 'Giới tính: Male/Female/Other',
    birth_date  DATE         COMMENT 'Ngày sinh',
    join_date   DATE         DEFAULT (CURRENT_DATE) COMMENT 'Ngày gia nhập',
    status      VARCHAR(20)  DEFAULT 'Active' COMMENT 'Trạng thái: Active/Inactive/Suspended',
    avatar_url  VARCHAR(500) COMMENT 'Ảnh đại diện',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã mã hóa',
    role_id     INT NOT NULL COMMENT 'FK -> roles',
    CONSTRAINT fk_member_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB COMMENT='Bảng thành viên';

-- ============================================================
-- TABLE: teams
-- Các nhóm/ban chuyên môn trong CLB
-- ============================================================
CREATE TABLE teams (
    team_id      INT AUTO_INCREMENT PRIMARY KEY,
    team_name    VARCHAR(100) NOT NULL UNIQUE COMMENT 'Tên nhóm/ban',
    description  TEXT COMMENT 'Mô tả',
    created_date DATE DEFAULT (CURRENT_DATE) COMMENT 'Ngày thành lập',
    leader_id    INT COMMENT 'FK -> members (trưởng nhóm)',
    CONSTRAINT fk_team_leader FOREIGN KEY (leader_id) REFERENCES members(member_id)
) ENGINE=InnoDB COMMENT='Bảng nhóm/ban';

-- ============================================================
-- TABLE: member_team (N-N: members <-> teams)
-- Một thành viên có thể thuộc nhiều nhóm
-- ============================================================
CREATE TABLE member_team (
    member_id   INT NOT NULL,
    team_id     INT NOT NULL,
    joined_at   DATE DEFAULT (CURRENT_DATE) COMMENT 'Ngày tham gia nhóm',
    position    VARCHAR(50) COMMENT 'Chức vụ trong nhóm',
    PRIMARY KEY (member_id, team_id),
    CONSTRAINT fk_mt_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    CONSTRAINT fk_mt_team   FOREIGN KEY (team_id)   REFERENCES teams(team_id)   ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Bảng liên kết thành viên - nhóm';

-- ============================================================
-- TABLE: events
-- Sự kiện, hoạt động của CLB
-- ============================================================
CREATE TABLE events (
    event_id         INT AUTO_INCREMENT PRIMARY KEY,
    event_name       VARCHAR(200) NOT NULL COMMENT 'Tên sự kiện',
    description      TEXT         COMMENT 'Mô tả chi tiết',
    start_date       DATETIME     NOT NULL COMMENT 'Thời gian bắt đầu',
    end_date         DATETIME     NOT NULL COMMENT 'Thời gian kết thúc',
    registration_deadline DATETIME COMMENT 'Hạn đăng ký',
    location         VARCHAR(255) COMMENT 'Địa điểm',
    status           VARCHAR(20)  DEFAULT 'Upcoming' COMMENT 'Upcoming/Ongoing/Completed/Cancelled',
    budget           DECIMAL(15,2) DEFAULT 0 COMMENT 'Ngân sách (VNĐ)',
    max_participants INT DEFAULT 100 COMMENT 'Số lượng tối đa',
    created_by       INT COMMENT 'FK -> members (người tạo)',
    CONSTRAINT fk_event_creator FOREIGN KEY (created_by) REFERENCES members(member_id)
) ENGINE=InnoDB COMMENT='Bảng sự kiện';

-- ============================================================
-- TABLE: participations (N-N: members <-> events)
-- Đăng ký tham gia sự kiện
-- ============================================================
CREATE TABLE participations (
    participation_id  INT AUTO_INCREMENT PRIMARY KEY,
    member_id         INT NOT NULL COMMENT 'FK -> members',
    event_id          INT NOT NULL COMMENT 'FK -> events',
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày đăng ký',
    status            VARCHAR(20)  DEFAULT 'Registered' COMMENT 'Registered/Attended/Absent/Cancelled',
    role_in_event     VARCHAR(100) COMMENT 'Vai trò cụ thể trong sự kiện',
    UNIQUE KEY uq_participation (member_id, event_id),
    CONSTRAINT fk_part_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    CONSTRAINT fk_part_event  FOREIGN KEY (event_id)  REFERENCES events(event_id)  ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Bảng đăng ký tham gia sự kiện';

-- ============================================================
-- TABLE: tasks
-- Nhiệm vụ giao cho thành viên
-- ============================================================
CREATE TABLE tasks (
    task_id      INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL COMMENT 'Tiêu đề nhiệm vụ',
    description  TEXT         COMMENT 'Mô tả chi tiết',
    deadline     DATETIME     COMMENT 'Hạn hoàn thành',
    priority     VARCHAR(10)  DEFAULT 'Medium' COMMENT 'Low/Medium/High/Critical',
    status       VARCHAR(15)  DEFAULT 'Todo'   COMMENT 'Todo/InProgress/Done/Overdue',
    visibility   VARCHAR(10)  DEFAULT 'Public' COMMENT 'Public/Private',
    max_assignees INT DEFAULT 1 COMMENT 'Số người tối đa',
    created_date DATETIME     DEFAULT CURRENT_TIMESTAMP,
    assigner_id  INT COMMENT 'FK -> members (người giao)',
    event_id     INT COMMENT 'FK -> events (nhiệm vụ thuộc sự kiện nào, nullable)',
    CONSTRAINT fk_task_assigner FOREIGN KEY (assigner_id) REFERENCES members(member_id),
    CONSTRAINT fk_task_event    FOREIGN KEY (event_id)    REFERENCES events(event_id)
) ENGINE=InnoDB COMMENT='Bảng nhiệm vụ';

-- ============================================================
-- TABLE: task_members (N-N: tasks <-> members)
-- Thành viên tham gia nhiệm vụ
-- ============================================================
CREATE TABLE task_members (
    task_id    INT NOT NULL,
    member_id  INT NOT NULL,
    joined_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id, member_id),
    CONSTRAINT fk_tm_task   FOREIGN KEY (task_id)   REFERENCES tasks(task_id)   ON DELETE CASCADE,
    CONSTRAINT fk_tm_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Bảng liên kết nhiệm vụ - thành viên';

-- ============================================================
-- TABLE: attendances
-- Điểm danh thành viên tại sự kiện
-- ============================================================
CREATE TABLE attendances (
    attendance_id  INT AUTO_INCREMENT PRIMARY KEY,
    member_id      INT NOT NULL COMMENT 'FK -> members',
    event_id       INT NOT NULL COMMENT 'FK -> events',
    check_in_time  DATETIME COMMENT 'Giờ đến',
    check_out_time DATETIME COMMENT 'Giờ về',
    status         VARCHAR(15)  DEFAULT 'Present' COMMENT 'Present/Late/Absent/Excused',
    note           VARCHAR(500) COMMENT 'Ghi chú',
    UNIQUE KEY uq_attendance (member_id, event_id),
    CONSTRAINT fk_att_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_event  FOREIGN KEY (event_id)  REFERENCES events(event_id)  ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Bảng điểm danh';

-- ============================================================
-- TABLE: announcements
-- Thông báo nội bộ CLB
-- ============================================================
CREATE TABLE announcements (
    announcement_id INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL COMMENT 'Tiêu đề thông báo',
    content         TEXT         NOT NULL COMMENT 'Nội dung',
    created_date    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    is_pinned       BOOLEAN      DEFAULT FALSE COMMENT 'Ghim thông báo',
    target_audience VARCHAR(20)  DEFAULT 'All' COMMENT 'Đối tượng: All/Leaders/Members',
    target_team_id  INT COMMENT 'FK -> teams (thông báo theo ban)',
    author_id       INT COMMENT 'FK -> members (người đăng)',
    CONSTRAINT fk_ann_author FOREIGN KEY (author_id) REFERENCES members(member_id),
    CONSTRAINT fk_ann_team   FOREIGN KEY (target_team_id) REFERENCES teams(team_id)
) ENGINE=InnoDB COMMENT='Bảng thông báo';

-- ============================================================
-- TABLE: meetings
-- Cuộc họp nội bộ
-- ============================================================
CREATE TABLE meetings (
    meeting_id   INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL COMMENT 'Tieu de cuoc hop',
    content      TEXT COMMENT 'Noi dung',
    start_time   DATETIME NOT NULL COMMENT 'Thoi gian bat dau',
    end_time     DATETIME NOT NULL COMMENT 'Thoi gian ket thuc',
    location     VARCHAR(255) COMMENT 'Dia diem',
    meet_link    VARCHAR(500) COMMENT 'Link Google Meet (neu online)',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    host_id      INT NOT NULL COMMENT 'FK -> members (chu tri)',
    CONSTRAINT fk_meeting_host FOREIGN KEY (host_id) REFERENCES members(member_id)
) ENGINE=InnoDB COMMENT='Bảng cuộc họp';

-- ============================================================
-- TABLE: projects
-- Dự án của CLB
-- ============================================================
CREATE TABLE projects (
    project_id    INT AUTO_INCREMENT PRIMARY KEY,
    project_name  VARCHAR(200) NOT NULL COMMENT 'Tên dự án',
    description   TEXT         COMMENT 'Mô tả',
    objective     TEXT         COMMENT 'Mục tiêu',
    start_date    DATE         COMMENT 'Ngày bắt đầu',
    end_date      DATE         COMMENT 'Ngày kết thúc dự kiến',
    budget        DECIMAL(15,2) DEFAULT 0 COMMENT 'Ngân sách',
    status        VARCHAR(20)  DEFAULT 'Planning' COMMENT 'Planning/Active/OnHold/Completed/Cancelled',
    visibility    VARCHAR(10)  DEFAULT 'Public' COMMENT 'Public/Private',
    max_members   INT DEFAULT 0 COMMENT '0=Khong gioi han',
    manager_id    INT COMMENT 'FK -> members (quản lý dự án)',
    CONSTRAINT fk_proj_manager FOREIGN KEY (manager_id) REFERENCES members(member_id)
) ENGINE=InnoDB COMMENT='Bảng dự án';

-- ============================================================
-- TABLE: project_members (N-N: projects <-> members)
-- Thành viên tham gia dự án
-- ============================================================
CREATE TABLE project_members (
    project_id  INT NOT NULL,
    member_id   INT NOT NULL,
    joined_date DATE DEFAULT (CURRENT_DATE),
    role_in_project VARCHAR(100) COMMENT 'Vai trò trong dự án',
    PRIMARY KEY (project_id, member_id),
    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_member  FOREIGN KEY (member_id)  REFERENCES members(member_id)  ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Bảng thành viên dự án';

-- ============================================================
-- TABLE: documents
-- Tài liệu của CLB
-- ============================================================
CREATE TABLE documents (
    document_id  INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL COMMENT 'Tiêu đề tài liệu',
    file_path    VARCHAR(500) COMMENT 'Đường dẫn file',
    file_type    VARCHAR(50)  COMMENT 'Loại file (PDF, DOCX, ...)',
    upload_date  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    description  TEXT         COMMENT 'Mô tả nội dung',
    is_public    BOOLEAN      DEFAULT TRUE COMMENT 'Công khai hay không',
    uploader_id  INT COMMENT 'FK -> members',
    event_id     INT COMMENT 'FK -> events (nullable)',
    project_id   INT COMMENT 'FK -> projects (nullable)',
    CONSTRAINT fk_doc_uploader FOREIGN KEY (uploader_id) REFERENCES members(member_id),
    CONSTRAINT fk_doc_event    FOREIGN KEY (event_id)    REFERENCES events(event_id),
    CONSTRAINT fk_doc_project  FOREIGN KEY (project_id)  REFERENCES projects(project_id)
) ENGINE=InnoDB COMMENT='Bảng tài liệu';

-- ============================================================
-- SAMPLE DATA (Dữ liệu mẫu)
-- ============================================================

-- Roles
INSERT INTO roles (role_name, description, permission_level) VALUES
('Admin',  'Quản trị viên hệ thống, toàn quyền', 3),
('Leader', 'Trưởng/Phó CLB, quản lý hoạt động', 2),
('Member', 'Thành viên thường', 1);

-- Members (password: 'admin123' -> BCrypt hash đã xác minh)
-- Hash: $2b$10$RKtAfxS8ATaDMDSqMfBTYuGxrHITnVmqdYSwMZxZ2t38HCPljrQ5e
-- Đây là BCrypt hash thật của "admin123" (cost=10), tương thích Spring Security BCryptPasswordEncoder
INSERT INTO members (full_name, student_id, email, phone, gender, birth_date, join_date, status, password_hash, role_id) VALUES
('Nguyễn Văn E',    'SV001', 'member@gmail.com',    '0901234567', 'Male',   '2002-05-15', '2023-09-01', 'Active', '$2b$10$RKtAfxS8ATaDMDSqMfBTYuGxrHITnVmqdYSwMZxZ2t38HCPljrQ5e', 3),
('Nguyễn Văn D',    'SV002', 'leader@gmail.com',    '0912345678', 'Female', '2002-08-20', '2023-09-01', 'Active', '$2b$10$RKtAfxS8ATaDMDSqMfBTYuGxrHITnVmqdYSwMZxZ2t38HCPljrQ5e', 2),
('Nguyễn Văn C',     'SV003', 'leader1@gmail.com',     '0923456789', 'Male',   '2001-12-10', '2022-10-01', 'Active', '$2b$10$RKtAfxS8ATaDMDSqMfBTYuGxrHITnVmqdYSwMZxZ2t38HCPljrQ5e', 2),
('Nguyễn Văn B',    'SV004', 'leader2@gmail.com',    '0934567890', 'Female', '2002-03-25', '2022-10-01', 'Active', '$2b$10$RKtAfxS8ATaDMDSqMfBTYuGxrHITnVmqdYSwMZxZ2t38HCPljrQ5e', 2),
('Nguyễn Văn A',     'SV005', 'admin@gmail.com',     '0945678901', 'Male',   '2001-07-08', '2021-09-01', 'Active', '$2b$10$RKtAfxS8ATaDMDSqMfBTYuGxrHITnVmqdYSwMZxZ2t38HCPljrQ5e', 1);

-- Update admin (member_id=5 has Admin role)
UPDATE members SET role_id = 1 WHERE member_id = 5;

-- Teams
INSERT INTO teams (team_name, description, created_date, leader_id) VALUES
('Ban Tổ Chức',    'Phụ trách tổ chức sự kiện',          '2022-10-01', 3),
('Ban Truyền Thông','Phụ trách truyền thông, thiết kế', '2022-10-01', 4),
('Ban Kỹ Thuật',   'Phụ trách âm thanh, ánh sáng, IT', '2022-10-01', 5);

-- Member-Team
INSERT INTO member_team (member_id, team_id, position) VALUES
(1, 1, 'Thành viên'), (2, 1, 'Phó ban'),
(3, 1, 'Trưởng ban'), (1, 2, 'Thành viên'),
(4, 2, 'Trưởng ban'), (5, 3, 'Trưởng ban'), (2, 3, 'Thành viên');

-- Events
INSERT INTO events (event_name, description, start_date, end_date, registration_deadline, location, status, budget, max_participants, created_by) VALUES
('Ngày hội Chào Tân Sinh Viên 2024',
 'Sự kiện chào đón tân sinh viên nhập học năm 2024',
 '2024-09-15 08:00:00', '2024-09-15 17:00:00', '2024-09-14 23:59:00',
 'Sân trường chính', 'Completed', 15000000, 500, 5),
('Workshop: Kỹ năng lập trình Python',
 'Workshop dạy lập trình Python cơ bản đến nâng cao',
 '2024-10-20 08:00:00', '2024-10-20 12:00:00', '2024-10-19 23:59:00',
 'Phòng máy tính A201', 'Completed', 2000000, 50, 3),
('Gala Concert Âm nhạc Mùa Xuân',
 'Đêm nhạc kỷ niệm 5 năm thành lập CLB',
 '2025-01-25 18:00:00', '2025-01-25 22:00:00', '2025-01-24 23:59:00',
 'Hội trường lớn', 'Upcoming', 30000000, 300, 4);

-- Participations
INSERT INTO participations (member_id, event_id, status, role_in_event) VALUES
(1, 1, 'Attended', 'Tình nguyện viên'),
(2, 1, 'Attended', 'MC'),
(3, 1, 'Attended', 'Trưởng ban tổ chức'),
(4, 1, 'Attended', 'Truyền thông'),
(1, 2, 'Attended', 'Học viên'),
(2, 2, 'Attended', 'Giảng viên hỗ trợ'),
(5, 2, 'Attended', 'Quản trị kỹ thuật'),
(1, 3, 'Registered', 'Tình nguyện viên'),
(2, 3, 'Registered', 'MC');

-- Tasks
INSERT INTO tasks (title, description, deadline, priority, status, visibility, max_assignees, assigner_id, event_id) VALUES
('Thiết kế poster sự kiện',  'Tạo poster cho Gala Concert', '2025-01-10 23:59:00', 'High',   'Done',       'Public', 1, 3, 3),
('Liên hệ nhà tài trợ',      'Liên hệ các doanh nghiệp',   '2025-01-05 23:59:00', 'High',   'InProgress', 'Public', 2, 5, 3),
('Chuẩn bị hệ thống âm thanh','Kiểm tra và lắp đặt hệ thống','2025-01-20 17:00:00','Critical','Todo',      'Private', 2, 3, 3),
('Thu thập phản hồi sự kiện','Gửi form khảo sát sau sự kiện','2024-09-20 23:59:00','Medium',  'Done',      'Public', 1, 5, 1);

-- Task Members
INSERT INTO task_members (task_id, member_id) VALUES
(1, 4),
(2, 3),
(3, 5),
(4, 2);

-- Attendances
INSERT INTO attendances (member_id, event_id, check_in_time, check_out_time, status) VALUES
(1, 1, '2024-09-15 07:45:00', '2024-09-15 17:15:00', 'Present'),
(2, 1, '2024-09-15 08:05:00', '2024-09-15 17:00:00', 'Late'),
(3, 1, '2024-09-15 07:30:00', '2024-09-15 17:30:00', 'Present'),
(4, 1, '2024-09-15 08:00:00', '2024-09-15 17:00:00', 'Present'),
(1, 2, '2024-10-20 07:50:00', '2024-10-20 12:00:00', 'Present'),
(2, 2, '2024-10-20 08:00:00', '2024-10-20 12:00:00', 'Present');

-- Announcements
INSERT INTO announcements (title, content, is_pinned, target_audience, target_team_id, author_id) VALUES
('Thông báo họp Ban Chấp Hành tháng 1', 'Họp BCH định kỳ vào 18h00 ngày 15/01/2025 tại phòng CLB A305. Mọi thành viên BCH vui lòng tham dự đầy đủ.', TRUE, 'Leaders', NULL, 5),
('Thông báo nội bộ Ban Tổ Chức', 'Ban Tổ Chức họp nội bộ vào 19h00 ngày 12/01/2025. Vui lòng có mặt đúng giờ.', TRUE, 'Members', 1, 3),
('Tuyển thành viên mới học kỳ 2 2024-2025', 'CLB mở đơn tuyển thành viên mới. Hạn nộp: 28/02/2025. Form đăng ký tại link: forms.gle/abc123', TRUE, 'All', NULL, 4),
('Kết quả sự kiện Chào Tân Sinh Viên', 'Sự kiện đã thành công tốt đẹp với 487/500 học sinh tham dự. Cảm ơn tất cả thành viên đã cống hiến!', FALSE, 'All', NULL, 3);

-- Meetings
INSERT INTO meetings (title, content, start_time, end_time, location, meet_link, host_id) VALUES
('Hop Ban Chap Hanh thang 2', 'Thong nhat ke hoach hoat dong thang 2.', '2025-02-10 18:00:00', '2025-02-10 19:30:00', 'Phong CLB A305', NULL, 5),
('Hop Online chuan bi su kien', 'Phan cong nhiem vu cho su kien thang 3.', '2025-01-12 20:00:00', '2025-01-12 21:00:00', NULL, 'https://meet.google.com/abc-xyz', 3),
('Hop tong ket du an', 'Tong ket tien do va rut kinh nghiem.', '2024-12-25 19:00:00', '2024-12-25 20:30:00', 'Phong hop B2', NULL, 4);

-- Projects
INSERT INTO projects (project_name, description, objective, start_date, end_date, budget, status, visibility, max_members, manager_id) VALUES
('Website CLB 2025', 'Xây dựng website chính thức cho CLB', 'Ra mắt website trước tháng 3/2025', '2024-11-01', '2025-02-28', 5000000, 'Active', 'Public', 5, 5),
('Sách kỷ yếu CLB 5 năm', 'Biên soạn kỷ yếu nhân dịp 5 năm thành lập', 'Hoàn thành sách kỷ yếu 100 trang', '2024-12-01', '2025-01-20', 8000000, 'Active', 'Public', 3, 4);

-- Project Members
INSERT INTO project_members (project_id, member_id, role_in_project) VALUES
(1, 5, 'Project Manager'), (1, 1, 'Frontend Dev'), (1, 2, 'Content Manager'),
(2, 4, 'Project Manager'), (2, 2, 'Editor'), (2, 3, 'Reviewer');

-- Documents
INSERT INTO documents (title, file_path, file_type, description, is_public, uploader_id, event_id) VALUES
('Kế hoạch tổ chức Chào Tân SV 2024', '/docs/events/plan_chao_tan_sv.pdf', 'PDF', 'Kế hoạch chi tiết sự kiện', FALSE, 3, 1),
('Báo cáo tổng kết sự kiện 2024', '/docs/events/report_2024.docx', 'DOCX', 'Báo cáo kết quả các sự kiện năm 2024', FALSE, 5, NULL),
('Quy chế hoạt động CLB', '/docs/club/quy_che.pdf', 'PDF', 'Văn bản quy chế chính thức', TRUE, 5, NULL);

-- ============================================================
-- VIEWS for easy querying
-- ============================================================

CREATE VIEW v_member_info AS
SELECT m.member_id, m.full_name, m.student_id, m.email, m.phone,
       m.gender, m.status, r.role_name, m.join_date
FROM members m JOIN roles r ON m.role_id = r.role_id;

CREATE VIEW v_event_summary AS
SELECT e.event_id, e.event_name, e.start_date, e.end_date,
       e.location, e.status, e.budget,
       COUNT(DISTINCT p.member_id) AS registered_count,
       m.full_name AS created_by_name
FROM events e
LEFT JOIN participations p ON e.event_id = p.event_id
LEFT JOIN members m ON e.created_by = m.member_id
GROUP BY e.event_id;
