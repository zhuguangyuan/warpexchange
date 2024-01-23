package com.itranswarp.exchange.match;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.itranswarp.exchange.model.trade.OrderEntity;

/**
 * 撮合结果
 * 一个takerOrder 可以匹配多个makerOrder, 每一个匹配是一个 MatchDetailRecord
 */
public class MatchResult {

    public final OrderEntity takerOrder;
    public final List<MatchDetailRecord> matchDetails = new ArrayList<>();

    public MatchResult(OrderEntity takerOrder) {
        this.takerOrder = takerOrder;
    }

    public void add(BigDecimal price, BigDecimal matchedQuantity, OrderEntity makerOrder) {
        matchDetails.add(new MatchDetailRecord(price, matchedQuantity, this.takerOrder, makerOrder));
    }

    @Override
    public String toString() {
        if (matchDetails.isEmpty()) {
            return "no matched.";
        }
        return matchDetails.size() + " matched: "
                + String.join(", ", this.matchDetails.stream().map(MatchDetailRecord::toString).toArray(String[]::new));
    }
}
