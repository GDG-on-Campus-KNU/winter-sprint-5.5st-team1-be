package com.gdg.sprint.team1.service.my;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.dto.auth.UserMeResponse;
import com.gdg.sprint.team1.dto.my.MyCouponResponse;
import com.gdg.sprint.team1.entity.UserCoupon;
import com.gdg.sprint.team1.exception.AuthRequiredException;
import com.gdg.sprint.team1.repository.UserCouponRepository;
import com.gdg.sprint.team1.security.UserContextHolder;
import com.gdg.sprint.team1.service.auth.AuthService;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final AuthService authService;
    private final UserCouponRepository userCouponRepository;

    private Integer currentUserId() {
        Integer userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new AuthRequiredException();
        }
        return userId;
    }

    public UserMeResponse getMyInfo() {
        Integer userId = currentUserId();
        return UserMeResponse.from(authService.getCurrentUser(userId));
    }

    /**
     * @param status "AVAILABLE" | "USED" | null(전체)
     */
    @Transactional(readOnly = true)
    public List<MyCouponResponse> getMyCoupons(String status) {
        Integer userId = currentUserId();
        List<UserCoupon> list = userCouponRepository.findByUser_IdOrderByIssuedAtDesc(userId);
        Stream<UserCoupon> stream = list.stream();
        if ("AVAILABLE".equalsIgnoreCase(status)) {
            stream = stream.filter(UserCoupon::isUsable);
        } else if ("USED".equalsIgnoreCase(status)) {
            stream = stream.filter(uc -> uc.getUsedAt() != null);
        }
        return stream.map(MyCouponResponse::from).toList();
    }
}
