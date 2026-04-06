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

| Thành phần | Công nghệ                    |
| ---------- | ---------------------------- |
| Ngôn ngữ   | Java 17                      |
| UI         | Java Swing (MVC Pattern)     |
| ORM        | Hibernate 6 / JPA            |
| Database   | MySQL 8                      |
| L&F        | FlatLaf (giao diện hiện đại) |
| Security   | BCrypt (mã hóa mật khẩu)     |
| Build      | Maven                        |
| Logging    | SLF4J + Logback              |

### 1.3 Tính năng chính

- ✅ Đăng nhập với phân quyền (Admin / Leader / Member)
- ✅ Dashboard tổng quan với số liệu thống kê
- ✅ Quản lý thành viên (CRUD + tìm kiếm + lọc)
- ✅ Quản lý sự kiện (CRUD + trạng thái màu sắc)
- ✅ Quản lý dự án (CRUD + theo dõi tiến độ)
- ✅ Quản lý cuộc họp (CRUD + lịch họp)
- ✅ Quản lý phân công nhiệm vụ (Giao việc, theo dõi deadline)
- ✅ Quản lý thông báo nội bộ (CRUD bảng tin)
- ✅ Điểm danh tham gia sự kiện của thành viên
- ✅ Quản lý tài liệu (Lưu trữ và chia sẻ file)
- ✅ Quản lý ban/nhóm chuyên môn
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

┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│    events    │     │   announcements  │     │   meetings   │
├──────────────┤     ├──────────────────┤     ├──────────────┤
│PK event_id   │     │PK ann_id         │     │PK meeting_id │
│ event_name   │     │  title           │     │ title        │
│ start_date   │     │  content         │     │ start_time   │
│ end_date     │     │  is_pinned       │     │ end_time     │
│ status       │     │FK author_id ────►│─► members         │
│ budget       │     │  target_audience │     │FK host_id ───►│─► members
│FK created_by │     └──────────────────┘     └──────────────┘
└──────────────┘

┌──────────────┐
│   projects   │
├──────────────┤
│PK project_id │
│ project_name │
│ start_date   │
│ status       │
│ budget       │
│FK manager_id │
└──────┬───────┘
    │ N-N (project_members)
    │◄─────► members

┌──────────────┐
│  documents   │
├──────────────┤
│PK doc_id     │
│  title       │
│  file_path   │
│  file_type   │
│FK uploader   │
│FK event_id   │
│FK project_id │
└──────────────┘
```

### 2.2 Quan hệ chính

| Quan hệ                     | Loại     | Bảng trung gian |
| --------------------------- | -------- | --------------- |
| Member ↔ Role               | N-1      | -               |
| Member ↔ Team               | N-N      | member_team     |
| Member ↔ Event              | N-N      | participations  |
| Member ↔ Project            | N-N      | project_members |
| Meeting → Member (chủ trì)  | N-1      | -               |
| Task → Member (giao)        | N-1      | -               |
| Task → Member (nhận)        | N-1      | -               |
| Attendance → Member + Event | N-1 (×2) | -               |

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

```text
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
        │   ├── Announcement.java, Attendance.java, Document.java, Event.java,
        │   ├── Meeting.java, Member.java, Participation.java, Project.java,
        │   └── Role.java, Task.java, Team.java
        ├── dto/                     # Data Transfer Objects
        │   └── (Tương ứng với các Entity)
        ├── dao/                     # Data Access Objects (DB queries)
        │   └── (Tương ứng với các chức năng)
        ├── service/                 # Business Logic
        │   └── (Tương ứng với các chức năng)
        ├── view/                    # Swing UI (View)
        │   ├── LoginView.java
        │   ├── DashboardView.java
        │   ├── MemberView.java, MemberFormDialog.java
        │   ├── TeamView.java
        │   ├── EventView.java
        │   ├── TaskView.java
        │   ├── AttendanceView.java
        │   ├── AnnouncementView.java
        │   ├── MeetingView.java
        │   ├── ProjectView.java
        │   ├── DocumentView.java
        │   └── MyInfoView.java
        ├── controller/              # MVC Controllers
        │   └── (Tương ứng với các chức năng quản lý)
        └── util/                    # Utilities
            ├── HibernateUtil.java
            ├── ImageUtil.java
            ├── PasswordUtil.java
            └── UiFormUtil.java
```

---

## 5. Tài khoản mẫu

| Email             | Mật khẩu | Vai trò |
| ----------------- | -------- | ------- |
| admin@gmail.com   | admin123 | Admin   |
| leader@gmail.com  | admin123 | Leader  |
| leader1@gmail.com | admin123 | Leader  |
| leader2@gmail.com | admin123 | Leader  |
| member@gmail.com  | admin123 | Member  |

> ⚠️ **Quan trọng**: Nếu bạn đã import database trước ngày **30/03/2025**, hãy chạy lại file SQL vì phiên bản cũ dùng **hash giả** (không hoạt động). Phiên bản mới dùng BCrypt hash thật của chuỗi `admin123`.
>
> ```bash
> mysql -u root -p club_management < database/club_management.sql
> ```
>
> Hoặc trong MySQL CLI:
>
> ```sql
> DROP DATABASE IF EXISTS club_management;
> source D:/LapTrinhWindows/ClubManagement/database/club_management.sql
> ```

---

## 6. Hướng dẫn sử dụng

### Đăng nhập

1. Nhập email và mật khẩu
2. Enter ở ô email → chuyển sang ô mật khẩu
3. Enter ở ô mật khẩu → đăng nhập

### Quản lý Thành viên

- **Tìm kiếm**: Nhập tên/email/mã SV vào ô tìm kiếm → Enter
- **Lọc**: Chọn trạng thái trong dropdown
- **Thêm**: Nhấn ➕ Thêm → điền form → Lưu
- **Sửa**: Chọn dòng → nhấn ✏ Sửa (hoặc double-click)
- **Xóa**: Chọn dòng → nhấn 🗑 Xóa (soft delete)

### Quản lý Sự kiện / Dự án

- Tương tự quản lý thành viên
- Hỗ trợ lọc theo trạng thái (màu sắc phân biệt)

### Quản lý Cuộc họp

- CRUD cuộc họp, đặt thời gian và người chủ trì
- Cuộc họp quá thời gian sẽ hiển thị màu xám

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

- `member_team`, `project_members`
- Tránh việc lưu mảng trong một ô → vi phạm 1NF
- ✅ Đạt chuẩn

---

_© 2025 Club Management System - Final Project Windows Programming_
