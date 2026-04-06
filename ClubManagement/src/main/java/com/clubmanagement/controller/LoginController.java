package com.clubmanagement.controller;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.view.DashboardView;
import com.clubmanagement.view.LoginView;

/**
 * LoginController - Bộ điều khiển màn hình Đăng nhập.
 *
 * Trách nhiệm:
 * 1. Lắng nghe sự kiện từ LoginView (nút đăng nhập, Enter)
 * 2. Gọi MemberService để xác thực
 * 3. Điều hướng sang DashboardView nếu thành công
 * 4. Hiển thị lỗi nếu thất bại
 *
 * Tuân theo MVC: Controller biết View + Service,
 * View KHÔNG biết Service, Service KHÔNG biết View.
 */
public class LoginController {

    private final LoginView    view;
    private final MemberService memberService;

    /**
     * Khởi tạo LoginController.
     * @param view          LoginView (đã khởi tạo)
     * @param memberService Service xử lý nghiệp vụ thành viên
     */
    public LoginController(LoginView view, MemberService memberService) {
        this.view          = view;
        this.memberService = memberService;
        attachListeners();
    }

    /**
     * Đăng ký tất cả event listeners cho View.
     * Gọi một lần khi Controller được tạo.
     */
    private void attachListeners() {
        // Listener cho nút Đăng nhập
        view.addLoginListener(e -> handleLogin());

        // Nhấn Enter trong ô email → chuyển sang ô mật khẩu
        view.addEmailEnterListener(e -> view.focusPasswordField());

        // Nhấn Enter trong ô mật khẩu → đăng nhập
        view.addPasswordEnterListener(e -> handleLogin());
    }

    /**
     * Xử lý logic đăng nhập khi người dùng nhấn nút.
     *
     * Quy trình:
     * 1. Lấy email + password từ View
     * 2. Gọi Service để xác thực
     * 3. Nếu thành công: ẩn LoginView, mở DashboardView
     * 4. Nếu thất bại: hiển thị thông báo lỗi
     */
    private void handleLogin() {
        String email    = view.getEmail();
        String password = view.getPassword();

        // Validate đơn giản tại Controller (validation phức tạp hơn ở Service)
        if (email.isBlank()) {
            view.showError("Vui lòng nhập Email!");
            return;
        }
        if (password.isBlank()) {
            view.showError("Vui lòng nhập mật khẩu!");
            return;
        }

        // Hiện trạng thái loading (vô hiệu hóa nút để tránh double-click)
        view.setLoading(true);
        view.clearStatus();

        // Chạy xác thực trong background thread để không đóng băng UI
        SwingWorker<Optional<MemberDTO>, Void> worker = new SwingWorker<>() {

            @Override
            protected Optional<MemberDTO> doInBackground() {
                // doInBackground chạy trên worker thread (không phải EDT)
                return memberService.login(email, password);
            }

            @Override
            protected void done() {
                // done() chạy lại trên EDT (Event Dispatch Thread) → an toàn cập nhật UI
                view.setLoading(false);
                try {
                    Optional<MemberDTO> result = get();
                    if (result.isPresent()) {
                        // Đăng nhập thành công
                        MemberDTO user = result.get();
                        view.showSuccess("Đăng nhập thành công! Xin chào, " + user.getFullName());

                        // Delay nhỏ trước khi chuyển màn hình
                        Timer timer = new Timer(800, evt -> {
                            view.setVisible(false);
                            view.dispose();
                            openDashboard(user);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        // Đăng nhập thất bại
                        view.showError("Email hoặc mật khẩu không đúng!");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showError("Lỗi kết nối: " + ex.getMessage());
                } catch (ExecutionException ex) {
                    view.showError("Lỗi kết nối: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Mở DashboardView và khởi tạo DashboardController.
     * @param user Thông tin người dùng đã đăng nhập
     */
    private void openDashboard(MemberDTO user) {
        DashboardView dashboard = new DashboardView(user);
        DashboardController dashCtrl = new DashboardController(dashboard, user);
        dashboard.getRootPane().putClientProperty("controller", dashCtrl);
        dashboard.setVisible(true);
    }
}
