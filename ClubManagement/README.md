# 🏫 Club Management System
## Hệ thống Quản lý Câu lạc bộ Trường học

> **Final Project - Windows Programming Course**  
> Java Swing MVC + Hibernate/JPA + MySQL

---

## 📋 Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [ERD & Thiết kế CSDL](#2-erd--thiết-kế-csdl)
3. [Cài đặt & Chạy ứng dụng](#3-cài-đặt--chạy-ứng-dụng)
4. [Cấu trúc dự án](#4-cấu-trúc-dự-án)
5. [Tài khoản mẫu](#5-tài-khoản-mẫu)
6. [Hướng dẫn sử dụng](#6-hướng-dẫn-sử-dụng)
7. [Phân tích 3NF](#7-phân-tích-3nf)

---

## 1. Tổng quan hệ thống

### 1.1 Lý do xây dựng

Các CLB trường học quản lý thủ công bằng Excel gặp nhiều vấn đề:
- Dữ liệu rời rạc, khó tra cứu
- Không có lịch sử hoạt động
- Điểm danh tốn thời gian
- Phân công nhiệm vụ mơ hồ

### 1.2 Tech Stack

| Thành phần | Công nghệ |
|-----------|-----------|
| Ngôn ngữ  | Java 17   |
| UI        | Java Swing (MVC Pattern) |
| ORM       | Hibernate 6 / JPA |
| Database  | MySQL 8   |
| L&F       | FlatLaf (giao diện hiện đại) |
| Security  | BCrypt (mã hóa mật khẩu) |
| Build     | Maven     |
| Logging   | SLF4J + Logback |

### 1.3 Tính năng chính

- ✅ Đăng nhập với phân quyền (Admin / Leader / Member)
- ✅ Dashboard tổng quan với số liệu thống kê
- ✅ Quản lý thành viên (CRUD + tìm kiếm + lọc)
- ✅ Quản lý sự kiện (CRUD + trạng thái màu sắc)
- ✅ Quản lý dự án (CRUD + theo dõi tiến độ)
- ✅ Soft delete (không xóa vật lý)
- ✅ Background thread (SwingWorker) - UI không bị đóng băng

---

## 2. ERD & Thiết kế CSDL

### 2.1 Sơ đồ ERD (Text Format)

```
┌──────────┐       ┌──────────┐       ┌──────────────┐
│  roles   │       │ members  │       │    teams     │
├──────────┤       ├──────────┤       ├──────────────┤
│PK role_id│       │PK mem_id │       │PK team_id    │
│ role_name│ 1   N │ full_name│ N   N │  team_name   │
│ perm_lvl │───────│ role_id  │───────│  leader_id──►│─► members
└──────────┘       │ email    │       └──────────────┘
                   │ status   │
                   └────┬─────┘
                        │
              ┌─────────┼──────────┐
              │         │          │
        N     │    N    │    N     │
┌─────────────▼─┐ ┌─────▼──────┐ ┌▼───────────┐
│  participations│ │   tasks    │ │ attendances│
├───────────────┤ ├────────────┤ ├────────────┤
│PK part_id     │ │PK task_id  │ │PK att_id   │
│FK member_id   │ │FK assignee │ │FK member_id│
│FK event_id ──►│ │FK assigner │ │FK event_id │
│ status        │ │FK event_id │ │ check_in   │
└───────────────┘ │ priority   │ │ status     │
                  │ status     │ └────────────┘
                  └────────────┘
                                    
┌──────────────┐     ┌──────────────────┐
│    events    │     │   announcements  │
├──────────────┤     ├──────────────────┤
│PK event_id   │     │PK ann_id         │
│ event_name   │     │  title           │
│ start_date   │     │  content         │
│ end_date     │     │  is_pinned       │
│ status       │     │FK author_id ────►│─► members
│ budget       │     │  target_audience │
│FK created_by │     └──────────────────┘
└──────┬───────┘
       │ N-N (event_sponsors)
       │
┌──────▼───────┐     ┌──────────────┐
│   sponsors   │     │   projects   │
├──────────────┤     ├──────────────┤
│PK sponsor_id │     │PK project_id │
│ sponsor_name │     │ project_name │
│ email        │     │ start_date   │
│ sponsorship  │     │ status       │
│ total_amount │     │ budget       │
└──────────────┘     │FK manager_id │
                     └──────┬───────┘
                            │ N-N (project_members)
                            │◄─────► members
                     
┌──────────────┐     ┌──────────────┐
│  feedbacks   │     │  documents   │
├──────────────┤     ├──────────────┤
│PK fb_id      │     │PK doc_id     │
│  content     │     │  title       │
│  rating 1-5  │     │  file_path   │
│FK member_id  │     │  file_type   │
│FK event_id   │     │FK uploader   │
│FK project_id │     │FK event_id   │
└──────────────┘     │FK project_id │
                     └──────────────┘
```

### 2.2 Quan hệ chính

| Quan hệ | Loại | Bảng trung gian |
|---------|------|----------------|
| Member ↔ Role | N-1 | - |
| Member ↔ Team | N-N | member_team |
| Member ↔ Event | N-N | participations |
| Member ↔ Project | N-N | project_members |
| Event ↔ Sponsor | N-N | event_sponsors |
| Task → Member (giao) | N-1 | - |
| Task → Member (nhận) | N-1 | - |
| Attendance → Member + Event | N-1 (×2) | - |

---

## 3. Cài đặt & Chạy ứng dụng

### Yêu cầu
- Java JDK 17+
- MySQL 8.0+
- Maven 3.8+
- IntelliJ IDEA (khuyến nghị)

### Bước 1: Tạo Database

```bash
# Đăng nhập MySQL
mysql -u root -p

# Chạy file SQL
source D:/LapTrinhWindows/ClubManagement/database/club_management.sql
```

### Bước 2: Cấu hình kết nối DB

Mở file `src/main/resources/hibernate.cfg.xml`, sửa:

```xml
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">YOUR_PASSWORD</property>
```

### Bước 3: Build & Run

```bash
cd D:\LapTrinhWindows\ClubManagement

# Build project
mvn clean package

# Chạy ứng dụng
mvn exec:java -Dexec.mainClass="com.clubmanagement.MainApp"

# Hoặc chạy JAR
java -jar target/ClubManagement-1.0-SNAPSHOT.jar
```

### Chạy trong IntelliJ IDEA

1. Mở **File → Open** → chọn thư mục `ClubManagement`
2. Đợi Maven tải dependencies
3. Mở `MainApp.java`
4. Click nút ▶ Run

---

## 4. Cấu trúc dự án

```
ClubManagement/
├── pom.xml                          # Cấu hình Maven
├── database/
│   └── club_management.sql          # Script tạo DB + dữ liệu mẫu
├── docs/
│   └── analysis.md                  # Phân tích hệ thống
└── src/main/
    ├── resources/
    │   ├── hibernate.cfg.xml        # Cấu hình Hibernate + DB
    │   └── logback.xml              # Cấu hình logging
    └── java/com/clubmanagement/
        ├── MainApp.java             # ← ENTRY POINT
        ├── entity/                  # JPA Entity classes
        │   ├── Role.java
        │   ├── Member.java
        │   ├── Team.java
        │   ├── Event.java
        │   ├── Participation.java
        │   ├── Task.java
        │   ├── Attendance.java
        │   ├── Announcement.java
        │   ├── Project.java
        │   ├── Feedback.java
        │   ├── Sponsor.java
        │   └── Document.java
        ├── dto/                     # Data Transfer Objects
        │   ├── MemberDTO.java
        │   ├── EventDTO.java
        │   └── ProjectDTO.java
        ├── dao/                     # Data Access Objects (DB queries)
        │   ├── MemberDAO.java
        │   ├── EventDAO.java
        │   ├── ProjectDAO.java
        │   └── AnnouncementDAO.java
        ├── service/                 # Business Logic
        │   ├── MemberService.java
        │   ├── EventService.java
        │   ├── ProjectService.java
        │   └── AnnouncementService.java
        ├── view/                    # Swing UI (View)
        │   ├── LoginView.java
        │   ├── DashboardView.java
        │   ├── MemberView.java
        │   ├── EventView.java
        │   ├── ProjectView.java
        │   └── MemberFormDialog.java
        ├── controller/              # MVC Controllers
        │   ├── LoginController.java
        │   ├── DashboardController.java
        │   ├── MemberController.java
        │   ├── EventController.java
        │   └── ProjectController.java
        └── util/                    # Utilities
            ├── HibernateUtil.java
            └── PasswordUtil.java
```

---

mvn exec:java "-Dexec.mainClass=com.clubmanagement.MainApp" 

## 5. Tài khoản mẫu

| Email | Mật khẩu | Vai trò |
|-------|----------|---------| 
| em.hoang@email.com | admin123 | Admin |
| cuong.le@email.com | admin123 | Leader |
| binh.tran@email.com | admin123 | Member |
| an.nguyen@email.com | admin123 | Member |

> ⚠️ **Quan trọng**: Nếu bạn đã import database trước ngày **30/03/2025**, hãy chạy lại file SQL vì phiên bản cũ dùng **hash giả** (không hoạt động). Phiên bản mới dùng BCrypt hash thật của chuỗi `admin123`.
>
> ```bash
> mysql -u root -p club_management < database/club_management.sql
> ```
>
> Hoặc trong MySQL CLI:
> ```sql
> DROP DATABASE IF EXISTS club_management;
> source D:/LapTrinhWindows/ClubManagement/database/club_management.sql
> ```

---

## 6. Hướng dẫn sử dụng

### Đăng nhập
1. Nhập email và mật khẩu
2. Nhấn **ĐĂNG NHẬP** hoặc Enter

### Quản lý Thành viên
- **Tìm kiếm**: Nhập tên/email/mã SV vào ô tìm kiếm → Enter
- **Lọc**: Chọn trạng thái trong dropdown
- **Thêm**: Nhấn ➕ Thêm → điền form → Lưu
- **Sửa**: Chọn dòng → nhấn ✏ Sửa (hoặc double-click)
- **Xóa**: Chọn dòng → nhấn 🗑 Xóa (soft delete)

### Quản lý Sự kiện / Dự án
- Tương tự quản lý thành viên
- Hỗ trợ lọc theo trạng thái (màu sắc phân biệt)

---

## 7. Phân tích 3NF

### 3NF là gì?
Database đạt 3NF khi:
1. **1NF**: Mỗi ô chứa một giá trị nguyên tử (không lặp nhóm)
2. **2NF**: Mọi thuộc tính non-key phụ thuộc đầy đủ vào Primary Key
3. **3NF**: Không có phụ thuộc bắc cầu (A→B→C thì A→C phải loại bỏ bằng cách tách bảng)

### Phân tích thiết kế đạt 3NF

**Bảng `members`**:
- PK: `member_id`
- `role_name` KHÔNG lưu trực tiếp trong `members` → tách ra bảng `roles`
- Điều này loại bỏ phụ thuộc bắc cầu: `member_id → role_id → role_name`
- ✅ Đạt 3NF

**Bảng `participations`** (N-N Member × Event):
- PK: `participation_id` (hoặc composite `member_id + event_id`)
- Không có thuộc tính nào phụ thuộc vào chỉ `member_id` hoặc chỉ `event_id`
- `registration_date`, `status`, `role_in_event` → phụ thuộc vào cả hai
- ✅ Đạt 3NF

**Bảng `tasks`**:
- PK: `task_id`
- `assignee_name` KHÔNG lưu → FK `assignee_id → members`
- ✅ Đạt 3NF

**Tất cả N-N được chuyển thành bảng trung gian**:
- `member_team`, `project_members`, `event_sponsors`
- Tránh việc lưu mảng trong một ô → vi phạm 1NF
- ✅ Đạt chuẩn

---

*© 2025 Club Management System - Final Project Windows Programming*
