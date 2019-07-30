package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem isCartExist(OmsCartItem omsCartItem) {

        OmsCartItem omsCartItem1 = new OmsCartItem();
        omsCartItem1.setProductSkuId(omsCartItem.getProductSkuId());
        omsCartItem1.setMemberId(omsCartItem.getMemberId());
        OmsCartItem select = omsCartItemMapper.selectOne(omsCartItem1);

        return select;
    }

    @Override
    public void updataCart(OmsCartItem omsCartItem1Exist) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setQuantity(omsCartItem1Exist.getQuantity());
        omsCartItem.setTotalPrice(omsCartItem1Exist.getTotalPrice());

        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo( "id",omsCartItem1Exist.getId());

        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);

        // 同步购物车缓存
        flushCache(omsCartItem1Exist.getMemberId());
    }

    @Override
    public void insertCart(OmsCartItem omsCartItem) {
        omsCartItemMapper.insertSelective(omsCartItem);

        // 同步购物车缓存
        flushCache(omsCartItem.getMemberId());
    }

    @Override
    public List<OmsCartItem> getCartCache(String memberId) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
        if (hvals != null){
            for (String cartJson : hvals) {
                OmsCartItem omsCartItem = new OmsCartItem();
                omsCartItem = JSON.parseObject(cartJson, OmsCartItem.class);
                omsCartItem.setTotalPrice(omsCartItem.getQuantity().multiply(omsCartItem.getPrice()));
                omsCartItems.add(omsCartItem);
            }
        }
        jedis.close();
        return omsCartItems;
    }

    @Override
    public void updataCartCheck(OmsCartItem omsCartItem) {

        OmsCartItem omsCartItem1 = new OmsCartItem();
        omsCartItem1.setIsChecked(omsCartItem.getIsChecked());

        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("productSkuId",omsCartItem.getProductSkuId()).andEqualTo("memberId",omsCartItem.getMemberId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem1,example);

        flushCache(omsCartItem.getMemberId());
    }

    private void flushCache(String memberId) {
        Jedis jedis = null;

        try {
             jedis = redisUtil.getJedis();
             String cartCachekey = "user:" + memberId + ":cart";
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
            HashMap<String, String> map = new HashMap<>();
            for (OmsCartItem cartItem : omsCartItems) {
                map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }
            jedis.hmset(cartCachekey,map);
        }finally {
            jedis.close();
        }

    }
}
