package com.marketplace.domain.service;

import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class OrderDomainService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.21"); // 21% Tax

    public void calculateOrderTotals(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            order.setTotalAmount(BigDecimal.ZERO);
            order.setTaxAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            item.calculateSubtotal();
            subtotal = subtotal.add(item.getSubtotal());
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        order.setSubtotalAmount(subtotal);
        if (order.getDiscountAmount() == null) {
            order.setDiscountAmount(BigDecimal.ZERO);
        }
        order.setTaxAmount(tax);
        order.setTotalAmount(total);
    }
}
