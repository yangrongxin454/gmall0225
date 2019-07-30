package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.PmsProductSaleAttr;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.beans.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map){

        // 查询一个sku对象
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        map.put("skuInfo",pmsSkuInfo);
        // 查询当前sku的销售属性列表
        List<PmsProductSaleAttr> pmsProductSaleAttrs = skuService.spuSaleAttrListCheckdBySkuId(pmsSkuInfo.getProductId(),skuId);
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);

        // 创建隐藏Hash表
        List<PmsSkuInfo> pmsSkuInfos = skuService.checkSkuBySpuId(pmsSkuInfo.getProductId());
        HashMap<String, String> skuSaleAttrMap = new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String skuIdForHashMap = skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

            String valueIds = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                valueIds = valueIds + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();

            }
            skuSaleAttrMap.put(valueIds,skuIdForHashMap);

        }
        map.put("skuSaleAttrMap", JSON.toJSONString(skuSaleAttrMap) );
        map.put("currentSkuId",skuId);
        return "item";
    }

}
