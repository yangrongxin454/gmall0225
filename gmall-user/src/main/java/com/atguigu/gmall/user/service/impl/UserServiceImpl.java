package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UmsMemberMapper umsMemberMapper;

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
}
