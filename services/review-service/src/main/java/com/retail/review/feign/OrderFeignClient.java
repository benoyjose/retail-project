package com.retail.review.feign;

import com.retail.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderFeignClient {

    @GetMapping("/api/v1/orders/verify-purchase")
    ApiResponse<Boolean> verifyPurchase(
            @RequestParam("customerId") Long customerId,
            @RequestParam("productSku") String productSku);
}

