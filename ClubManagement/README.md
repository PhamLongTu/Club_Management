# 🏫 Club Management System

## Hệ thống Quản lý Câu lạc bộ Trường học

> **Final Project - Windows Programming Course**  
> Java Swing MVC + Hibernate/JPA + MySQL

---

## 📋 Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Cài đặt & Chạy ứng dụng](#3-cài-đặt--chạy-ứng-dụng)
3. [Cấu trúc dự án](#4-cấu-trúc-dự-án)
4. [Tài khoản mẫu](#5-tài-khoản-mẫu)
5. [Hướng dẫn sử dụng](#6-hướng-dẫn-sử-dụng)

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


## 2. Cài đặt & Chạy ứng dụng

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

## 3. Cấu trúc dự án

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

## 4. Tài khoản mẫu

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

## 5. Hướng dẫn sử dụng

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

