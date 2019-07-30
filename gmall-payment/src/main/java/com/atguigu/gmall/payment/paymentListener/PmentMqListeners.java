package com.atguigu.gmall.payment.paymentListener;

import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.beans.PaymentInfo;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Controller;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Controller
public class PmentMqListeners {




    @Autowired
    PaymentService paymentService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_CHECK_QUEUE")
    public void  comsumePaymentCheck(MapMessage mapMessage) throws JMSException {



        String out_trade_no = mapMessage.getString("out_trade_no");
        int count = mapMessage.getInt("count");
        System.out.println("开始检查订单"+ out_trade_no + "的支付情况");
        Map<String, Object> stringObjectMap = paymentService.checkPayment(out_trade_no);
        count --;
        String trade_status = (String) stringObjectMap.get("trade_status");
        if (StringUtils.isNotBlank(trade_status)){
            if (trade_status.equals("WAIT_BUYBR_PAY")){
                if (count>0){
                    System.out.println("还剩检查次数"+count+ "继续发生请求");
                    PaymentInfo paymentInfo = new PaymentInfo();
                    paymentInfo.setOrderSn(out_trade_no);
                    paymentService.sendPaymentStatusCheckQueue(paymentInfo,count);
                }else {
                    System.out.println("检查次数耗尽，结束检查");
                }
            }




            //进行幂等性检查
            String payStatus = paymentService.checkDbPayStatus(out_trade_no);
            if (!payStatus.equals("success")){

                // 更新支付信息
                PaymentInfo paymentInfo = new PaymentInfo();
                if (trade_status.equals("TRADB_SUCCESS") || trade_status.equals("TRADB_FINSHED")){
                    paymentInfo.setPaymentStatus("已支付");
                }else {
                    paymentInfo.setPaymentStatus("支付存在问题");
                }
                String trade_no = (String) stringObjectMap.get("trade_no");
                String callbackContent = (String) stringObjectMap.get("callbackContent");
                paymentInfo.setAlipayTradeNo(trade_no);
                paymentInfo.setCallbackContent(callbackContent);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setOrderSn(out_trade_no);


                paymentService.updatePaymentByOrderSn(paymentInfo);
                paymentService.sentPaymentResult(paymentInfo);
            }

        }else {
            System.out.println("还剩检查次数"+count+ "继续发生请求");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentService.sendPaymentStatusCheckQueue(paymentInfo,count);
        }




    }
}
