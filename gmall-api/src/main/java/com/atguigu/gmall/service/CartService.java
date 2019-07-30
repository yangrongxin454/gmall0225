package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem isCartExist(OmsCartItem omsCartItem);

    void updataCart(OmsCartItem omsCartItem1Exist);

    void insertCart(OmsCartItem omsCartItem);

    List<OmsCartItem> getCartCache(String memberId);

    void updataCartCheck(OmsCartItem omsCartItem);
}
