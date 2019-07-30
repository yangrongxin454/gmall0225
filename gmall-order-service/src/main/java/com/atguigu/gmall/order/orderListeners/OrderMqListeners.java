package com.atguigu.gmall.order.orderListeners;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Controller;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.math.BigDecimal;
import java.util.Date;

@Controller
public class OrderMqListeners {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_SUCCESS_QUEUE")
    public void  comsumePaymentSuccess(MapMessage mapMessage) throws JMSException {

        System.out.println("订单系统消费支付信息");
        // 获得支付信息
        String out_trade_no = mapMessage.getString("out_trade_no");
        BigDecimal pay_amount = new BigDecimal(mapMessage.getDouble("pay_amount"));

        // 更新订单数据
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        omsOrder.setStatus("1");
        omsOrder.setPayAmount(pay_amount);
        omsOrder.setPaymentTime(new Date());

        orderService.updateOrder(omsOrder);

        // 通知库存，锁定商品 ORDER_PAY_QUEUE
        orderService.sentOrderPayQueue(out_trade_no);

    }
}
