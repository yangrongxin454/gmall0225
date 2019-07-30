package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

	@Reference
	SkuService skuService;

	@Autowired
	JestClient jestClient;

	public  void  setJest() throws IOException {

		List<PmsSkuInfo> pmsSkuInfos = skuService.getAllSku();

		List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();

		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {

			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
			String id = pmsSkuInfo.getId();
			long l = Long.parseLong(id);
			pmsSearchSkuInfo.setId(l);
			pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
		}
		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
		    Index index =	new Index.Builder(pmsSearchSkuInfo).index("gmall0225").type("PmsSearchSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();

			JestResult execute = jestClient.execute(index);
		}
	}

	public void searchTest() throws IOException {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(20);
		searchSourceBuilder.from(0);

		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","小米");
		boolQueryBuilder.must(matchQueryBuilder);

		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","41");
		boolQueryBuilder.filter(termQueryBuilder);

		searchSourceBuilder.query(boolQueryBuilder);

		System.err.println(searchSourceBuilder);
		Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("gmall0225").addType("PmsSearchSkuInfo").build();

		SearchResult searchResult = jestClient.execute(search);

		List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
		List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
		for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
			PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
			pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
		}
		System.out.println(pmsSearchSkuInfoList.size());

	}

	@Test
	public void contextLoads() throws IOException {
		setJest();
	}

}
