package com.retail.cart.feign;

import com.retail.common.dto.ApiResponse;
import com.retail.cart.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/api/v1/products/sku/{sku}")
    ApiResponse<ProductDto> getProductBySku(@PathVariable("sku") String sku);
}

