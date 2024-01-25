package com.itranswarp.exchange.redis;

public interface RedisCache {
    /**
     * pub-sub的主题
     * 交易结果、orderBook通知
     */
    public interface Topic {
        // API监听此主题，然后将结果返回给客户端
        String TRADING_API_RESULT = "trading_api_result";
        // 通知主题，有 order_match, orderbook 两个通知发布， 在PUSH服务处监听，给客户端推送
        String NOTIFICATION = "notification";
    }

    public interface Key {

        String ORDER_BOOK = "_orderbook_";

        String RECENT_TICKS = "_ticks_";

        String DAY_BARS = "_day_bars_";

        String HOUR_BARS = "_hour_bars_";

        String MIN_BARS = "_min_bars_";

        String SEC_BARS = "_sec_bars_";
    }
}
