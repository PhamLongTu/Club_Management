package com.clubmanagement.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;

/**
 * UiFormUtil - Bộ tiện ích cho các thành phần form Swing dùng chung.
 */
public final class UiFormUtil {

    private UiFormUtil() {}

    /**
     * Tạo header dùng chung cho dialog.
     * @param title Tiêu đề header
     * @return JPanel header
     */
    public static JPanel buildDialogHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(30, 41, 59));
        header.add(label, BorderLayout.WEST);
        return header;
    }

    /**
     * Tạo bộ chọn ngày với giá trị mặc định (nullable).
     * @param dateTime Ngày giờ mặc định
     * @return JDateChooser
     */
    public static JDateChooser createDateChooser(LocalDateTime dateTime) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("yyyy-MM-dd");
        chooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (dateTime != null) {
            chooser.setDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        }
        return chooser;
    }

    /**
     * Tạo bộ chọn ngày với giá trị LocalDate (nullable).
     * @param date Ngày mặc định
     * @return JDateChooser
     */
    public static JDateChooser createDateChooser(LocalDate date) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("yyyy-MM-dd");
        chooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (date != null) {
            chooser.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        return chooser;
    }

    /**
     * Tạo spinner chọn giờ với giá trị mặc định (nullable).
     * @param dateTime Nguồn thời gian mặc định
     * @return JSpinner
     */
    public static JSpinner createTimeSpinner(LocalDateTime dateTime) {
        LocalTime time = dateTime != null ? dateTime.toLocalTime() : LocalTime.now().withSecond(0).withNano(0);
        Date timeValue = Date.from(time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(timeValue, null, null, Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return spinner;
    }

    /**
     * Gộp input ngày và giờ vào một panel.
     * @param dateChooser Bộ chọn ngày
     * @param timeSpinner Spinner chọn giờ
     * @return JPanel chứa cả hai input
     */
    public static JPanel buildDateTimePanel(JDateChooser dateChooser, JSpinner timeSpinner) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        timeSpinner.setPreferredSize(new Dimension(90, 28));
        panel.add(dateChooser, BorderLayout.CENTER);
        panel.add(timeSpinner, BorderLayout.EAST);
        return panel;
    }

    /**
     * Chuyển giá trị từ ngày + giờ sang LocalDateTime.
     * @param dateChooser Bộ chọn ngày
     * @param timeSpinner Spinner chọn giờ
     * @return LocalDateTime hoặc null
     */
    public static LocalDateTime toLocalDateTime(JDateChooser dateChooser, JSpinner timeSpinner) {
        Date date = dateChooser.getDate();
        if (date == null) return null;
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date time = (Date) timeSpinner.getValue();
        LocalTime localTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
        return LocalDateTime.of(localDate, localTime);
    }

    /**
     * Tạo label hiển thị thông tin chuẩn.
     * @param text Nội dung label
     * @return JLabel
     */
    public static JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(2, 0, 2, 0));
        return label;
    }
}
