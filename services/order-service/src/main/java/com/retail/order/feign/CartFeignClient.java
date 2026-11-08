package com.retail.order.feign;

import com.retail.common.dto.ApiResponse;
import com.retail.order.dto.CartResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service")
public interface CartFeignClient {

    @GetMapping("/api/v1/cart")
    ApiResponse<CartResponseDto> getCart(@RequestHeader("X-User-Id") String userId);
}

