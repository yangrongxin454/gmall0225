package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void addPayment(PaymentInfo paymentInfo);

    void updatePaymentByOrderSn(PaymentInfo paymentInfo);

    void sentPaymentResult(PaymentInfo paymentInfo);

    void sendPaymentStatusCheckQueue(PaymentInfo paymentInfo,int count);

    Map<String , Object> checkPayment(String out_trade_no);


    String checkDbPayStatus(String out_trade_no);
}
