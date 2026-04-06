package com.clubmanagement.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

/**
 * UiAsyncUtil - Helper chạy tác vụ nền và cập nhật UI an toàn.
 * Giảm lặp code SwingWorker trong các controller.
 */
public final class UiAsyncUtil {

    private UiAsyncUtil() {}

    /**
     * Chạy tác vụ nền và cập nhật UI với message trạng thái.
     * @param loadingMessage Thông báo khi bắt đầu
     * @param statusSetter Setter cho status (nullable)
     * @param work Tác vụ chạy nền
     * @param onSuccess Xử lý khi thành công
     */
    public static <T> void runWithStatus(String loadingMessage,
                                         Consumer<String> statusSetter,
                                         Supplier<T> work,
                                         Consumer<T> onSuccess) {
        runWithStatus(loadingMessage, statusSetter, work, onSuccess, null);
    }

    /**
     * Chạy tác vụ nền và cập nhật UI với handler lỗi tùy chỉnh.
     * @param loadingMessage Thông báo khi bắt đầu
     * @param statusSetter Setter cho status (nullable)
     * @param work Tác vụ chạy nền
     * @param onSuccess Xử lý khi thành công
     * @param onError Xử lý lỗi (nullable)
     */
    public static <T> void runWithStatus(String loadingMessage,
                                         Consumer<String> statusSetter,
                                         Supplier<T> work,
                                         Consumer<T> onSuccess,
                                         Consumer<Exception> onError) {
        if (statusSetter != null && loadingMessage != null) {
            statusSetter.accept(loadingMessage);
        }

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() {
                return work.get();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    handleError(ex, statusSetter, onError);
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    Exception err = cause instanceof Exception
                        ? (Exception) cause
                        : new RuntimeException(cause);
                    handleError(err, statusSetter, onError);
                }
            }
        };
        worker.execute();
    }

    private static void handleError(Exception ex,
                                    Consumer<String> statusSetter,
                                    Consumer<Exception> onError) {
        if (onError != null) {
            onError.accept(ex);
            return;
        }
        if (statusSetter != null) {
            statusSetter.accept("Lỗi: " + ex.getMessage());
        }
    }
}
