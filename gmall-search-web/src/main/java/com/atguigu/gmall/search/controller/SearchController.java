package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.beans.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){

        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.search(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfoList);

        // 平台属性去重
        HashSet<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        // 查询pmsBaseAttrInfos返回给网页
        List<PmsBaseAttrInfo> pmsBaseAttrInfos =  null;
        if (valueIdSet != null && valueIdSet.size() > 0 ){
            pmsBaseAttrInfos = attrService.getAttrValueByValueIds(valueIdSet);
        }
        modelMap.put("attrList",pmsBaseAttrInfos);

        // 面包屑的
//        ArrayList<PmsCrumb> pmsCrumbs = new ArrayList<>();
//        String[] valueIds2 = pmsSearchParam.getValueId();
//        if (valueIds2 != null && valueIds2.length > 0){
//            for (String valueIdForCrumb : valueIds2) {
//                PmsCrumb pmsCrumb = new PmsCrumb();
//                pmsCrumb.setUrlParam(getUrlParam(pmsSearchParam,valueIdForCrumb));
//                for (PmsBaseAttrInfo pmsBaseAttrInfo : pmsBaseAttrInfos) {
//                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
//                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
//                        if (valueIdForCrumb.equals(pmsBaseAttrValue.getId())){
//                            pmsCrumb.setValueName(pmsBaseAttrValue.getValueName());
//                        }
//                    }
//                }
//
//                pmsCrumbs.add(pmsCrumb);
//            }
//
//        }




        // 减去平台属性值所在属性名
        String[] valueIds = pmsSearchParam.getValueId();
        // 面包屑的集合声明
       List<PmsCrumb> pmsCrumbs = new ArrayList<>();
        if (valueIds != null && valueIds.length > 0 ){
            for (String valueId : valueIds) {
                PmsCrumb pmsCrumb = new PmsCrumb();
                pmsCrumb.setUrlParam(getUrlParam(pmsSearchParam,valueId));
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                while (iterator.hasNext()){
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String id = pmsBaseAttrValue.getId();
                        if (id.equals(valueId)){
                            pmsCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                pmsCrumbs.add(pmsCrumb);

            }
        }

        modelMap.put("attrValueSelectedList",pmsCrumbs);
        String currentUrl = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",currentUrl);
        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam,String...valueIdForCrumb ){

        String currentUrl = "";
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        if (StringUtils.isNotBlank(catalog3Id)){
            currentUrl = currentUrl + "&" ;
        }
        currentUrl = currentUrl + "currentUrl=" + catalog3Id;

        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)){
            currentUrl = currentUrl + "&";
        }
        currentUrl = currentUrl + "keyword=" +keyword;


        String[] valueIds = pmsSearchParam.getValueId();
        if (valueIds!=null&&valueIds.length>0){
            for (String valueId : valueIds) {
                if ((valueIdForCrumb == null || valueIdForCrumb.length == 0) || (valueIdForCrumb != null && valueIdForCrumb.length > 0 && !valueId.equals(valueIdForCrumb[0]))) {
                    currentUrl = currentUrl + "&valueId=" + valueId;
                }
            }
        }


        return currentUrl;
    }
}
