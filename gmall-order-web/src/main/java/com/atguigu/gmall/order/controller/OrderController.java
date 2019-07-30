package com.atguigu.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.*;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    SkuService skuService;

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @LoginRequired(isNeedeSuccess = true)
    @RequestMapping("submitOrder")
    public String submit0rder(String receiveAddressId, HttpServletRequest request,String tradeCode, ModelMap modelMap){

        // 获取用户Id，根据用户Id查询购物车列表
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        boolean b = orderService.checkTradeCode( memberId,tradeCode);
        if (b == true) {

            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getMemberAddressesByAddressId(receiveAddressId);
            // 根据购物车列表生成订单
            List<OmsCartItem> cartItems = cartService.getCartCache(memberId);

            OmsOrder omsOrder = new OmsOrder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(new Date());
            long l = System.currentTimeMillis();
            long l1 = System.nanoTime();
            String orderSn = "gmall" +format + "";
            omsOrder.setOrderSn(orderSn);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setOrderType(0);
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            omsOrder.setStatus("0");
            omsOrder.setSourceType(0);
            omsOrder.setTotalAmount(getCartSumPrice(cartItems));


            // 将订单保存数据库
            // 验库存
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            for (OmsCartItem cartItem : cartItems) {
                if (cartItem.getIsChecked().equals("1")) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    BigDecimal currentPrice = cartItem.getPrice();
                    String productSkuId = cartItem.getProductSkuId();
                    PmsSkuInfo skuByIdFromDb = skuService.getSkuByIdFromDb(productSkuId);
                    int i = currentPrice.compareTo(skuByIdFromDb.getPrice());
                    if (i == 0) {
                        omsOrderItem.setProductPrice(cartItem.getPrice());
                    } else {

                        modelMap.put("err", "价格发生变化");
                        return "tradeFail";
                    }
                    omsOrderItem.setProductQuantity(cartItem.getQuantity());
                    omsOrderItem.setRealAmount(omsOrderItem.getProductPrice().multiply(omsOrderItem.getProductQuantity()));
                    omsOrderItem.setProductSkuId(cartItem.getProductSkuId());
                    omsOrderItem.setProductPic(cartItem.getProductPic());
                    omsOrderItem.setProductName(cartItem.getProductName());
                    omsOrderItem.setProductId(cartItem.getProductId());
                    omsOrderItem.setProductCategoryId(cartItem.getProductCategoryId());

                    omsOrderItem.setOrderSn(orderSn);//外部订单号

                    omsOrderItems.add(omsOrderItem);
                }

            }

           omsOrder.setOmsOrderItems(omsOrderItems);
            orderService.addOrder(omsOrder);

            return "redirect:http://payment.gmall.com:8090/index?orderSn="+orderSn+ "&totalAmount="+getCartSumPrice(cartItems); // 重定向到支付页面

        }else {
            modelMap.put("err","重复提交");
            return "tradeFail";
        }
    }

    @LoginRequired(isNeedeSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap modelMap){

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        List<OmsCartItem> omsCartItems = cartService.getCartCache(memberId);

        // 商品转换成临时订单对象
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")){
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                omsOrderItem.setProductId(omsCartItem.getProductId());
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductPrice(omsCartItem.getPrice());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                omsOrderItem.setRealAmount(omsOrderItem.getProductPrice().multiply(omsOrderItem.getProductQuantity()));

                omsOrderItems.add(omsOrderItem);

            }


        }


        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getMemberAddressesById(memberId);

        modelMap.put("userAddressList",umsMemberReceiveAddresses);
        modelMap.put("orderDetailList",omsOrderItems);
        modelMap.put("totalAmount",getCartSumPrice(omsCartItems));

        String tradeCode  = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }

    private BigDecimal getCartSumPrice(List<OmsCartItem> omsCartItems) {

        BigDecimal sum = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            String isChecked = omsCartItem.getIsChecked();
            if(isChecked.equals("1")){
                sum = sum.add(omsCartItem.getTotalPrice());
            }
        }

        return sum;
    }
}
