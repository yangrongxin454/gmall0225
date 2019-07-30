package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    UmsMemberMapper umsMemberMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getAllUser() {

        final List<UmsMember> allUser = umsMemberMapper.selectAll(); //.getAllUser();

        return allUser;
    }

    @Override
    public UmsMember getOneUser(String id) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(id);

       UmsMember umsMember1  = umsMemberMapper.selectOne(umsMember);
        return umsMember1;
    }

    @Override
    public void putToken(String gmallToken,String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user" + memberId + "info",60*60, gmallToken);
        jedis.close();
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        UmsMember umsMember1 = new UmsMember();
        umsMember1.setUsername(umsMember.getUsername());
        umsMember1.setPassword(umsMember.getPassword());
        UmsMember umsMemberForDb = umsMemberMapper.selectOne(umsMember1);

        if (umsMemberForDb != null){
            Jedis jedis = redisUtil.getJedis();
            jedis.setex("user" + umsMemberForDb.getId() + "info",60*60, JSON.toJSONString(umsMemberForDb));
            jedis.close();
            return umsMemberForDb;
        }else {
            return  null;
        }


    }

    @Override
    public List<UmsMemberReceiveAddress> getMemberAddressesById(String memberId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> select = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return select;
    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
        UmsMember umsMemberReturn = new UmsMember();
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setSourceUid(umsMember.getSourceUid());
        List<UmsMember> select = umsMemberMapper.select(umsMember1);
        if (select == null || select.size()==0) {
            umsMemberMapper.insertSelective(umsMember);
            umsMemberReturn = umsMember;
        }else {
            umsMemberReturn = select.get(0);
        }
        return  umsMemberReturn;
    }

    @Override
    public UmsMemberReceiveAddress getMemberAddressesByAddressId(String receiveAddressId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }
}
