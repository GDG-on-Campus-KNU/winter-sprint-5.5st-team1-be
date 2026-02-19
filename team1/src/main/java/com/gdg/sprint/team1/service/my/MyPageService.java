package com.gdg.sprint.team1.service.my;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdg.sprint.team1.dto.my.MyCouponResponse;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.entity.UserCoupon;
import com.gdg.sprint.team1.exception.AuthRequiredException;
import com.gdg.sprint.team1.repository.UserCouponRepository;
import com.gdg.sprint.team1.security.UserContextHolder;
import com.gdg.sprint.team1.service.order.OrderService;

@Service
public class MyPageService {

    private final OrderService orderService;
    private final UserCouponRepository userCouponRepository;

    public MyPageService(OrderService orderService, UserCouponRepository userCouponRepository) {
        this.orderService = orderService;
        this.userCouponRepository = userCouponRepository;
    }

    private Integer currentUserId() {
        Integer userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new AuthRequiredException();
        }
        return userId;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Integer page, Integer limit, String status) {
        return orderService.getOrders(page, limit, status);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getMyOrderDetail(Integer orderId) {
        return orderService.getOrderDetail(orderId);
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
