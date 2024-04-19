package com.itranswarp.exchange.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.exchange.assets.AssetService;
import com.itranswarp.exchange.assets.Asset;
import com.itranswarp.exchange.enums.AssetEnum;
import com.itranswarp.exchange.model.trade.OrderEntity;
import com.itranswarp.exchange.order.OrderService;
import com.itranswarp.exchange.support.LoggerSupport;

/**
 * 获取活跃状态的订单
 * 获取用户资产等
 *
 * 下单、取消，没有直接API，而是通过定序服务传过来(API服务接收订单后传到消息队列)
 */
@RestController
@RequestMapping("/internal")
public class InternalTradingEngineApiController extends LoggerSupport {

    @Autowired
    OrderService orderService;

    @Autowired
    AssetService assetService;

    @GetMapping("/{userId}/assets")
    public Map<AssetEnum, Asset> getAssets(@PathVariable("userId") Long userId) {
        return assetService.getAssets(userId);
    }

    @GetMapping("/{userId}/orders")
    public List<OrderEntity> getOrders(@PathVariable("userId") Long userId) {
        ConcurrentMap<Long, OrderEntity> orders = orderService.getUserOrders(userId);
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        List<OrderEntity> list = new ArrayList<>(orders.size());
        for (OrderEntity order : orders.values()) {
            OrderEntity copy = null;
            while (copy == null) {
                copy = order.copy();
            }
            list.add(copy);
        }
        return list;
    }

    @GetMapping("/{userId}/orders/{orderId}")
    public OrderEntity getOrders(@PathVariable("userId") Long userId, @PathVariable("orderId") Long orderId) {
        OrderEntity order = orderService.getActivateOrder(orderId);
        if (order == null || order.userId.longValue() != userId.longValue()) {
            return null;
        }
        return order.copy();
    }
}
