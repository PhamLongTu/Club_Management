# PHÂN TÍCH HỆ THỐNG - QUẢN LÝ CÂU LẠC BỘ TRƯỜNG HỌC
## (Club Management System Analysis)

---

## 1. KHẢO SÁT HIỆN TRẠNG (Current System Analysis)

### 1.1 Bối cảnh
Các câu lạc bộ (CLB) trong trường học hiện nay đang phát triển mạnh mẽ với nhiều 
hoạt động như tổ chức sự kiện, quản lý thành viên, thực hiện dự án, và kết nối 
với nhà tài trợ. Tuy nhiên, phần lớn các CLB vẫn đang sử dụng phương thức quản lý 
thủ công hoặc dùng các công cụ văn phòng cơ bản như Excel, Google Sheets.

### 1.2 Vấn đề hiện tại (Problems)
1. **Quản lý thành viên thủ công**: Danh sách thành viên được lưu trong file Excel,
   dễ mất dữ liệu, khó tìm kiếm và cập nhật.
2. **Theo dõi sự kiện rời rạc**: Thông tin sự kiện nằm rải rác ở nhiều file,
   khó theo dõi lịch sử và đánh giá hiệu quả.
3. **Điểm danh thủ công**: Điểm danh bằng giấy tờ, tốn thời gian xử lý và dễ sai sót.
4. **Phân công nhiệm vụ không rõ ràng**: Không có hệ thống theo dõi ai làm gì,
   dẫn đến chồng chéo hoặc bỏ sót công việc.
5. **Thông báo kém hiệu quả**: Thông báo qua nhóm chat tản mạn, thành viên dễ bỏ lỡ.
6. **Báo cáo tốn thời gian**: Phải tổng hợp dữ liệu thủ công từ nhiều nguồn.
7. **Quản lý tài liệu phân tán**: Tài liệu lưu ở nhiều nơi, khó tra cứu.
8. **Thiếu minh bạch về nhà tài trợ**: Không có hệ thống theo dõi thông tin tài trợ.

### 1.3 Mục tiêu hệ thống (System Objectives)
- Xây dựng hệ thống quản lý CLB tập trung, đồng bộ
- Tự động hóa các quy trình điểm danh, phân công nhiệm vụ
- Cung cấp dashboard tổng quan cho Ban quản lý CLB
- Lưu trữ lịch sử hoạt động dài hạn, đáng tin cậy
- Hỗ trợ nhiều vai trò người dùng (Admin, Leader, Member)

---

## 2. PHÂN TÍCH THỰC THỂ (Entity Analysis)

### Member (Thành viên)
- **PK**: member_id
- **Attributes**: full_name, student_id, email, phone, gender, birth_date,
  join_date, status, avatar_url
- **Ý nghĩa**: Lưu thông tin cá nhân của từng thành viên CLB

### Role (Vai trò)
- **PK**: role_id
- **Attributes**: role_name, description, permission_level
- **Ý nghĩa**: Định nghĩa các vai trò trong CLB (Admin, Leader, Member, ...)

### Team (Nhóm/Ban)
- **PK**: team_id
- **Attributes**: team_name, description, created_date
- **Ý nghĩa**: Các nhóm/ban chuyên môn trong CLB

### Event (Sự kiện)
- **PK**: event_id
- **Attributes**: event_name, description, start_date, end_date, location,
  status, budget, max_participants
- **Ý nghĩa**: Các hoạt động/sự kiện do CLB tổ chức

### Participation (Tham gia sự kiện)
- **PK**: participation_id
- **Attributes**: registration_date, status, role_in_event
- **Ý nghĩa**: Bảng liên kết Member và Event (N-N)

### Task (Nhiệm vụ)
- **PK**: task_id
- **Attributes**: title, description, deadline, priority, status, created_date
- **Ý nghĩa**: Các nhiệm vụ được giao cho thành viên

### Attendance (Điểm danh)
- **PK**: attendance_id
- **Attributes**: check_in_time, check_out_time, status, note
- **Ý nghĩa**: Ghi nhận điểm danh của thành viên tại sự kiện

### Announcement (Thông báo)
- **PK**: announcement_id
- **Attributes**: title, content, created_date, is_pinned, target_audience
- **Ý nghĩa**: Thông báo nội bộ CLB

### Project (Dự án)
- **PK**: project_id
- **Attributes**: project_name, description, start_date, end_date, 
  budget, status, objective
- **Ý nghĩa**: Các dự án dài hạn của CLB

### Feedback (Phản hồi)
- **PK**: feedback_id
- **Attributes**: content, rating, created_date, feedback_type
- **Ý nghĩa**: Phản hồi của thành viên về sự kiện/dự án

### Sponsor (Nhà tài trợ)
- **PK**: sponsor_id
- **Attributes**: sponsor_name, contact_person, email, phone, address,
  sponsorship_type, amount
- **Ý nghĩa**: Thông tin nhà tài trợ cho CLB/sự kiện

### Document (Tài liệu)
- **PK**: document_id
- **Attributes**: title, file_path, file_type, upload_date, description, is_public
- **Ý nghĩa**: Tài liệu, hồ sơ của CLB
