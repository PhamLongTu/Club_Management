package com.clubmanagement.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil - Tiện ích mã hóa và kiểm tra mật khẩu bằng BCrypt.
 *
 * BCrypt là thuật toán hash một chiều, không thể giải mã ngược.
 * Để xác thực, so sánh plain text với hash đã lưu.
 *
 * Lưu ý về prefix:
 *  - $2a$ : prefix của jBCrypt (Java mindrot) và Spring Security
 *  - $2b$ : prefix của Python bcrypt / PHP password_hash
 *  - Hai prefix này dùng CÙNG thuật toán, chỉ khác version identifier
 *  - jBCrypt chỉ nhận $2a$ nên ta normalize $2b$ → $2a$ trước khi check
 */
public class PasswordUtil {

    /**
     * Mã hóa mật khẩu plaintext thành BCrypt hash ($2a$ prefix).
     *
     * @param plainPassword Mật khẩu chưa mã hóa
     * @return Chuỗi hash BCrypt (~60 ký tự, prefix $2a$)
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Kiểm tra mật khẩu plaintext có khớp hash đã lưu không.
     * Tự động normalize prefix $2b$/$2y$ → $2a$ để tương thích jBCrypt.
     *
     * @param plainPassword  Mật khẩu người dùng nhập
     * @param hashedPassword Hash đã lưu trong database
     * @return true nếu mật khẩu đúng, false nếu sai
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isBlank()) return false;

        // Normalize: $2b$ và $2y$ là cùng thuật toán với $2a$
        // jBCrypt (mindrot) chỉ hỗ trợ $2a$ nên cần convert
        String normalizedHash = hashedPassword;
        if (hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$")) {
            normalizedHash = "$2a$" + hashedPassword.substring(4);
        }

        return BCrypt.checkpw(plainPassword, normalizedHash);
    }
}
