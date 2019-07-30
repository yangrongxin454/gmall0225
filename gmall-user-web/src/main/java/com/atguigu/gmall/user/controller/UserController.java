package com.atguigu.gmall.user.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    UserService userService;

    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){

        List<UmsMember>  umsMembers =  userService.getAllUser();

        return umsMembers;
    }

    @ResponseBody
    @RequestMapping("getOneUser/{id}")
    public  UmsMember getOneUser(@PathVariable("id")String id ){

        UmsMember umsMember = userService.getOneUser(id);
        return umsMember;
    }
}
