# Mục lục báo cáo (bản cập nhật)

Ghi chú: Tag [GIU] = giữ nguyên, [SUA] = chỉnh lại nội dung, [BO] = bỏ khỏi bản mới. Khi nộp có thể xóa tag.

CHƯƠNG 1. GIỚI THIỆU DỰ ÁN [GIU]
1.1. Lý do chọn đề tài [GIU] - Gợi ý: nêu nhu cầu quản lý CLB, khó khăn khi quản lý thủ công.
1.2. Mục tiêu và yêu cầu của hệ thống [SUA] - Gợi ý: bám theo yêu cầu thầy: CRUD, tìm kiếm/thống kê, JPQL, dữ liệu mẫu, chú thích code.
1.3. Phạm vi hệ thống và đối tượng sử dụng [SUA] - Gợi ý: vai trò Admin/Leader/Member, phạm vi chức năng chính.
1.4. Công nghệ sử dụng [SUA]
1.4.1. Java (JDK) và Maven [SUA] - Gợi ý: lý do chọn Java, quản lý phụ thuộc bằng Maven.
1.4.2. Swing + FlatLaf [GIU] - Gợi ý: mô tả UI desktop, MainForm/SubForm.
1.4.3. Hibernate (JPA) [GIU] - Gợi ý: ORM, giảm SQL thuần.
1.4.4. MySQL [GIU] - Gợi ý: lưu trữ dữ liệu và dùng script mẫu.
1.4.5. Mô hình MVC [GIU] - Gợi ý: phân tầng Controller/Service/DAO/View.

CHƯƠNG 2. KHẢO SÁT VÀ PHÂN TÍCH HỆ THỐNG [GIU]
2.1. Khảo sát hiện trạng quản lý CLB [SUA]
2.1.1. Quản lý thành viên và phân quyền [SUA] - Gợi ý: đăng ký, cập nhật hồ sơ, vai trò.
2.1.2. Quản lý nhóm/ban (Team) [SUA] - Gợi ý: phân công, trưởng ban.
2.1.3. Quản lý sự kiện và tham gia [SUA] - Gợi ý: tạo sự kiện, đăng ký, điểm danh.
2.1.4. Quản lý dự án [SUA] - Gợi ý: tiến độ, thành viên dự án.
2.1.5. Quản lý nhiệm vụ [SUA] - Gợi ý: giao việc, theo dõi trạng thái.
2.1.6. Thông báo, tài liệu, cuộc họp [SUA] - Gợi ý: chia sẻ thông tin nội bộ.
2.2. Xác định các thực thể [SUA]
2.2.1. Danh sách thực thể [SUA] - Gợi ý: Member, Role, Team, MemberTeam, Event, Participation, Attendance, Task, TaskMember, Project, ProjectMember, Announcement, Document, Meeting.
2.2.2. Thuộc tính khóa - danh hiệu [SUA] - Gợi ý: liệt kê PK/FK từng thực thể.
2.2.3. Thuộc tính mô tả [SUA] - Gợi ý: các trường còn lại.
2.3. Xác định mối liên kết [SUA]
2.3.1. Quan hệ 1-n [SUA] - Gợi ý: Role-Member, Member-Event (created_by), Project-Task (nếu có).
2.3.2. Quan hệ n-n [SUA] - Gợi ý: Member-Team, Member-Project, Member-Task, Member-Event (Participation).
2.4. Sơ đồ ERD [GIU] - Gợi ý: vẽ đầy đủ bảng chính + bảng trung gian.
2.5. Chuẩn hóa 1NF/2NF/3NF [GIU] - Gợi ý: giải thích ngắn, kết quả 3NF.

CHƯƠNG 3. THIẾT KẾ CƠ SỞ DỮ LIỆU VÀ ENTITY [SUA]
3.1. Chuyển ERD sang lược đồ quan hệ [SUA]
3.1.1. Bảng roles [SUA]
3.1.2. Bảng members [SUA]
3.1.3. Bảng teams [SUA]
3.1.4. Bảng member_team [SUA]
3.1.5. Bảng events [SUA]
3.1.6. Bảng participations [SUA]
3.1.7. Bảng attendances [SUA]
3.1.8. Bảng tasks [SUA]
3.1.9. Bảng task_members [SUA]
3.1.10. Bảng meetings [SUA]
3.1.11. Bảng projects [SUA]
3.1.12. Bảng project_members [SUA]
3.1.13. Bảng announcements [SUA]
3.1.14. Bảng documents [SUA]
3.1.15. View dữ liệu (v_member_info, v_event_summary) [SUA] - Gợi ý: mục hỗ trợ truy vấn.
3.2. Script tạo CSDL và dữ liệu mẫu [SUA] - Gợi ý: mô tả file SQL, thứ tự chạy.
3.3. Ánh xạ đối tượng - quan hệ với JPA [GIU]
3.3.1. Annotation cơ bản [GIU] - Gợi ý: @Entity, @Id, @GeneratedValue, @Column.
3.3.2. Ánh xạ quan hệ [GIU] - Gợi ý: @ManyToOne, @OneToMany, @ManyToMany, @JoinTable.
3.4. Các lớp Entity trong hệ thống [SUA]
3.4.1. Member, Role, Team [SUA]
3.4.2. Event, Participation, Attendance [SUA]
3.4.3. Task [SUA]
3.4.4. Project [SUA]
3.4.5. Announcement, Document, Meeting [SUA]
3.5. DTO trong hệ thống [SUA]
3.5.1. MemberDTO [SUA]
3.5.2. EventDTO [SUA]
3.5.3. ProjectDTO [SUA]
3.5.4. TeamDTO, TaskDTO, AttendanceDTO, DocumentDTO, MeetingDTO [SUA]

CHƯƠNG 4. XÂY DỰNG ỨNG DỤNG THEO MVC + HIBERNATE [SUA]
4.1. Tổng quan kiến trúc và package [GIU] - Gợi ý: sơ đồ package com.clubmanagement.
4.2. Tiện ích chung (util) [SUA]
4.2.1. HibernateUtil [GIU] - Gợi ý: quản lý SessionFactory.
4.2.2. PasswordUtil [SUA] - Gợi ý: mã hóa/kiểm tra mật khẩu.
4.2.3. ImageUtil, UiFormUtil [SUA] - Gợi ý: hỗ trợ ảnh và form.
4.3. Tầng DAO [SUA]
4.3.1. MemberDAO, Role/Team/Project DAO [SUA] - Gợi ý: CRUD, query cơ bản.
4.3.2. Event/Participation/Attendance DAO [SUA] - Gợi ý: thống kê, lọc.
4.3.3. Task/Meeting/Announcement/Document DAO [SUA] - Gợi ý: truy vấn theo trạng thái.
4.4. Tầng Service [SUA]
4.4.1. MemberService [SUA] - Gợi ý: đăng nhập, validate, DTO mapping.
4.4.2. EventService, AttendanceService [SUA] - Gợi ý: nghiệp vụ sự kiện/điểm danh.
4.4.3. ProjectService, TaskService [SUA] - Gợi ý: nghiệp vụ dự án/nhiệm vụ.
4.4.4. AnnouncementService, DocumentService, MeetingService, TeamService [SUA]
4.5. Tầng Controller [SUA]
4.5.1. LoginController [GIU] - Gợi ý: xác thực, điều hướng.
4.5.2. DashboardController [GIU] - Gợi ý: quản lý sidebar, CardLayout.
4.5.3. MemberController, MyInfoController [SUA] - Gợi ý: CRUD + profile.
4.5.4. Event/Attendance/Meeting Controller [SUA]
4.5.5. Project/Task/Team Controller [SUA]
4.5.6. Announcement/Document Controller [SUA]
4.6. Tầng View (Swing) [SUA]
4.6.1. LoginView [GIU] - Gợi ý: form đăng nhập.
4.6.2. DashboardView (MainForm) [GIU] - Gợi ý: khung chính, sidebar.
4.6.3. Các SubForm CRUD [SUA] - Gợi ý: MemberView, EventView, ProjectView, TaskView, TeamView, AttendanceView.
4.6.4. Form đặc thù [SUA] - Gợi ý: MemberFormDialog, MyInfoView.
4.6.5. Các view còn lại [SUA] - Gợi ý: AnnouncementView, DocumentView, MeetingView.
4.7. MainApp (điểm vào ứng dụng) [GIU] - Gợi ý: init Look&Feel, mở LoginView.
4.8. Quy ước chú thích mã nguồn [SUA] - Gợi ý: mô tả mỗi hàm, input/output, xử lý chính.

CHƯƠNG 5. DỮ LIỆU THỬ, JPQL VÀ HƯỚNG DẪN SỬ DỤNG [SUA]
5.1. Cài đặt và cấu hình hệ thống [SUA]
5.1.1. Yêu cầu phần mềm [SUA] - Gợi ý: JDK, MySQL, Maven.
5.1.2. Tạo CSDL và cấu hình Hibernate [SUA] - Gợi ý: hibernate.cfg.xml.
5.1.3. Chạy script dữ liệu mẫu [SUA] - Gợi ý: club_management.sql.
5.2. Dữ liệu thử nghiệm [SUA] - Gợi ý: mô tả các nhóm dữ liệu mẫu chính.
5.3. Các câu JPQL tiêu biểu [SUA]
5.3.1. Tìm kiếm thành viên theo từ khóa [GIU]
5.3.2. Thống kê sự kiện theo trạng thái [SUA]
5.3.3. Danh sách dự án theo tiến độ [SUA]
5.3.4. Lọc nhiệm vụ theo deadline/trạng thái [SUA]
5.3.5. Thống kê thành viên theo vai trò/ban [SUA]
5.4. Hướng dẫn sử dụng hệ thống [SUA]
5.4.1. Đăng nhập/đăng xuất [GIU]
5.4.2. Dashboard tổng quan [GIU]
5.4.3. CRUD và tìm kiếm cho từng module [SUA] - Gợi ý: Member/Event/Project/Task/Team/Announcement/Document/Meeting.

CHƯƠNG 6. ĐÁNH GIÁ VÀ KẾT LUẬN [GIU]
6.1. Kết quả đạt được [GIU] - Gợi ý: bám yêu cầu thầy.
6.2. Ưu điểm [GIU] - Gợi ý: MVC rõ ràng, ORM.
6.3. Hạn chế [SUA] - Gợi ý: tính năng còn thiếu, hiệu năng.
6.4. Hướng phát triển [GIU] - Gợi ý: nâng cấp UI/Report/Notification.

PHỤ LỤC. MỤC ĐÃ BỎ SO VỚI BẢN CŨ [BO]

- Feedback, Sponsor [BO] - Gợi ý: không còn trong code/SQL hiện tại.
- Bảng feedbacks, sponsors [BO] - Gợi ý: bỏ khỏi ERD và lược đồ.
