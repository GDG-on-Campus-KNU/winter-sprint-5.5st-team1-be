package com.gdg.sprint.team1.security;

import com.gdg.sprint.team1.entity.User.UserRole;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserContext context) {
        HOLDER.set(context);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static Integer getCurrentUserId() {
        UserContext ctx = HOLDER.get();
        return ctx != null ? ctx.userId() : null;
    }

    public static UserRole getCurrentRole() {
        UserContext ctx = HOLDER.get();
        return ctx != null ? ctx.role() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record UserContext(Integer userId, UserRole role) {}
}
