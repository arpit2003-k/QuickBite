package com.quickbite.cart.client;

import com.quickbite.cart.dto.MenuItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "menu-service", url = "http://localhost:8083")
public interface MenuClient {

    @GetMapping("/api/menu/items/{itemId}")
    MenuItemDTO getMenuItem(@PathVariable("itemId") Long itemId);
}
