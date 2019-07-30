package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    UmsMember getOneUser(String id);



    void putToken(String token, String memberId);


    UmsMember login(UmsMember umsMember);

    List<UmsMemberReceiveAddress> getMemberAddressesById(String memberId);

    UmsMember addOauthUser(UmsMember umsMember);

    UmsMemberReceiveAddress getMemberAddressesByAddressId(String receiveAddressId);
}
