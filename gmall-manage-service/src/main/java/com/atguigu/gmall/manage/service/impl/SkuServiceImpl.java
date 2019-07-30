package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.*;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckdBySkuId(String spuId,String skuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrs= pmsSkuSaleAttrValueMapper.selectSpuSaleAttrListCheckdBySkuId(spuId ,skuId);
        return pmsProductSaleAttrs;
    }

    @Override
    public  List<PmsSkuInfo> checkSkuBySpuId(String spuId){
        List<PmsSkuInfo> PmsSkuInfos = pmsSkuSaleAttrValueMapper.checkSkuBySpuId(spuId);

        return PmsSkuInfos;
    }

    @Override
    public String checkSkuByValueIdsTwo(String[] ids){
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuSaleAttrValueMapper.checkSkuByValueIdsTwo(StringUtils.join(ids,","));
        HashMap<String, String> skuMap = new HashMap<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();

            String valueIds = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                valueIds = valueIds + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();

            }
            skuMap.put(valueIds,skuId);

        }
        String valueIds = "";
        for (String id : ids) {
            valueIds = valueIds + "|" + id;
        }
        String itemSkuId = skuMap.get(valueIds);

        return  itemSkuId;
    }

    @Override
    public String checkSkuByValueIds(String[] ids){
        String skuId = null;
      List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuSaleAttrValueMapper.checkSkuByValueIds(StringUtils.join(ids,","));
      List<String> skuIds = new ArrayList<>();
      if (pmsSkuSaleAttrValues!=null&&pmsSkuSaleAttrValues.size()>0){
          for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
              skuIds.add(pmsSkuSaleAttrValue.getSkuId());
          }
      }
        HashMap<String, String> skuMap = new HashMap<>();
        for (String skuIdFromList : skuIds) {
            String valueIdHashStr = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
                String skuIdFromDb = pmsSkuSaleAttrValue.getSkuId();
                if (skuIdFromDb.equals(skuIdFromList)){
                    valueIdHashStr = valueIdHashStr + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();
                }
            }
            skuMap.put(valueIdHashStr,skuIdFromList);
        }
        System.out.println("你的数据库中的所有有关的属性值和sku对应的hash表："+skuMap);
        String valueIds = "";
        for (String id : ids) {
            valueIds = valueIds + "|" + id;
        }

        String itemSkuId = skuMap.get(valueIds);
        if (itemSkuId!=null&&!itemSkuId.equals("")){
            skuId = itemSkuId;
        }

        System.out.println("你的销售属性值的组合id:"+valueIds);
        return skuId;
    }

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String id = pmsSkuInfo.getId();

        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(id);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(id);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(id);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

    }

    @Override
    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo1.setSkuImageList(pmsSkuImages);
        return pmsSkuInfo1;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId){
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        Jedis jedis = redisUtil.getJedis();

        try{
            String skuJsonStr = jedis.get("sku:" + skuId + ":info");

            if (StringUtils.isBlank(skuJsonStr)){
                System.err.println(Thread.currentThread().getName()+"缓存未命中！");
                String delCode = UUID.randomUUID().toString();
               String lock= jedis.set("sku:" +skuId+":lock",delCode,"nx","px" ,100000);
                if (StringUtils.isNotBlank(lock)&&"OK".equals(lock)){
                    System.err.println(Thread.currentThread().getName()+"获得分布式锁！");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pmsSkuInfo = getSkuByIdFromDb(skuId);
                    if (pmsSkuInfo!=null){
                        jedis.set("sku:"+skuId+":info",JSON.toJSONString(pmsSkuInfo));

                    }
                    System.out.println("请求成功归还分布式锁");
                    String v = jedis.get("sku:" +skuId+":lock");
                    if (StringUtils.isNotBlank(v)&&v.equals(delCode)){
                        jedis.del("sku:" +skuId+":lock");
                    }

                }else{
                    System.err.println(Thread.currentThread().getName()+"未获得分布式锁，开始自旋！");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return getSkuById( skuId);
                }

            }else {
                System.err.println(Thread.currentThread().getName()+"缓存已命中！！！！！！！！！！！！！！！！！！！");
                pmsSkuInfo = JSON.parseObject(skuJsonStr,PmsSkuInfo.class);
            }

        }finally {
            jedis.close();
        }
        return  pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }

}
