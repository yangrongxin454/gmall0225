package com.atguigu.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.OmsCartItem;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    // 修改选中状态
    @LoginRequired
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap map,OmsCartItem omsCartItem) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        if (StringUtils.isBlank(memberId)){
            // 没有登录
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
                for (OmsCartItem cartItem : omsCartItems) {
                    if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                        cartItem.setIsChecked(omsCartItem.getIsChecked());
                    }
                }
            }
            String jsonString = JSON.toJSONString(omsCartItems);
            System.out.println(jsonString);
            CookieUtil.setCookie(request,response,"cartListCookie",jsonString,1000*60*60*24,true);
        }else {
            // 已登录，查询缓存或db
            omsCartItem.setMemberId(memberId);
            cartService.updataCartCheck(omsCartItem);
            omsCartItems = cartService.getCartCache(memberId);

        }
        map.put("cartList",omsCartItems);
        map.put("cartSumPirce",getCartSumPrice(omsCartItems));
        return "cartListInner";
    }

    // 从缓存或cookie中获取数据跳转到购物车界面
    @LoginRequired
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap map) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        // 用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        if (StringUtils.isBlank(memberId)){
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }else {
            // 用户已登录，取缓存数据

            omsCartItems = cartService.getCartCache(memberId);
        }

        map.put("cartList",omsCartItems);
        map.put("cartSumPirce",getCartSumPrice(omsCartItems));
        return "cartList";
    }

    // 获取总价
    private Object getCartSumPrice(List<OmsCartItem> omsCartItems) {

        BigDecimal sum = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            String isChecked = omsCartItem.getIsChecked();
            if (isChecked.equals("1")){
                BigDecimal totalPrice = omsCartItem.getTotalPrice();
                sum = sum.add(totalPrice);
            }
        }
        return sum;
    }

    // 添加数据到cookie，db，缓存，的购物车
    @LoginRequired
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, HttpSession session, OmsCartItem omsCartItem){

        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(omsCartItem.getProductSkuId());
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        omsCartItem.setCreateDate(new Date());
        omsCartItem.setIsChecked("1");
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuId(pmsSkuInfo.getId());
        omsCartItem.setTotalPrice(pmsSkuInfo.getPrice().multiply(omsCartItem.getQuantity()));

        String memberId =(String) request.getAttribute("memberId");
        String nickname =(String) request.getAttribute("nickname");
        // 判断用户是否登录
        if (StringUtils.isBlank(memberId)){
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            // 判断有没有Cookie
            if (StringUtils.isBlank(cartListCookie)){
                omsCartItems.add(omsCartItem);

            }else {
                // 判断cookie中的购物车数据是否有重复
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);

                // 更新或者添加购物车
                boolean b = if_new_cart(omsCartItems,omsCartItem);
                if (b){
                    // 新车添加
                    omsCartItems.add(omsCartItem);
                }else {
                    // 老车更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductId().equals(omsCartItem.getProductId())){
                            BigDecimal newQuantity = cartItem.getQuantity().add(omsCartItem.getQuantity());
                            cartItem.setQuantity(newQuantity);
                            BigDecimal newPrice = cartItem.getPrice().multiply(newQuantity);
                            cartItem.setTotalPrice(newPrice);
                            break;
                        }
                    }
                }
            }
            // 添加cookie到浏览器

            String JSONcookie = JSON.toJSONString(omsCartItems);
            System.out.println(JSONcookie);
            CookieUtil.setCookie(request,response,"cartListCookie",JSONcookie,1000*60*60*24,true);

        }else {
            omsCartItem.setMemberId(memberId);
            omsCartItem.setMemberNickname("windir");
            // 已经登录，查询db
           OmsCartItem omsCartItem1Exist = cartService.isCartExist(omsCartItem);

           if (omsCartItem1Exist != null){
               // 更新数据库
               System.out.println("更新");
               omsCartItem1Exist.setQuantity(omsCartItem1Exist.getQuantity().add(omsCartItem.getQuantity()));
               omsCartItem1Exist.setTotalPrice(omsCartItem1Exist.getQuantity().multiply(omsCartItem1Exist.getPrice()));
               cartService.updataCart(omsCartItem1Exist);

           }else {
               // 添加数据库
               System.out.println("添加");
               cartService.insertCart(omsCartItem);
           }
        }


        return "redirect:/success.html";
    }

    // 判断
    private boolean if_new_cart(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = true ;

        for (OmsCartItem cartItem : omsCartItems) {
            if (cartItem.getProductId().equals(omsCartItem.getProductId())){
                b = false;
                break;
            }
        }

        return b;
    }
}
