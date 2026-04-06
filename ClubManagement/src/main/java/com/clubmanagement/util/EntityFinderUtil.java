package com.clubmanagement.util;

import org.hibernate.Session;
import org.slf4j.Logger;

/**
 * EntityFinderUtil - Helper tìm entity theo ID bằng Hibernate session.
 * Dùng để giảm lặp code mở session trong Service.
 */
public final class EntityFinderUtil {

    private EntityFinderUtil() {}

    /**
     * Tìm entity theo ID, có thể log lỗi nếu cần.
     * @param entityClass Class entity
     * @param id ID cần tìm
     * @param logger Logger (nullable)
     * @param errorMessage Message log khi lỗi (nullable)
     * @return entity hoặc null nếu không tìm thấy/lỗi
     */
    public static <T> T findById(Class<T> entityClass, Integer id, Logger logger, String errorMessage) {
        if (id == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(entityClass, id);
        } catch (Exception e) {
            if (logger != null) {
                if (errorMessage == null || errorMessage.trim().isEmpty()) {
                    logger.error("Lỗi khi tìm entity: {}", e.getMessage());
                } else {
                    logger.error("{}: {}", errorMessage, e.getMessage());
                }
            }
            return null;
        }
    }
}
