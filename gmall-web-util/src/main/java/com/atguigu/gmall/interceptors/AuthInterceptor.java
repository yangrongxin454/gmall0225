package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.HashMap;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);

        // 判定有没有注解
        if (loginRequired == null) {
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request,"oldToken",true);
        if (StringUtils.isNotBlank(oldToken)){
            token =oldToken;
        }

        String newToken = request.getParameter("newToken");
        if (StringUtils.isNotBlank(newToken)){
            token = newToken;
        }
        String ReturnUrl = request.getRequestURL().toString() ;
        if (StringUtils.isNotBlank(token)){
            String  success = "";
                  success  = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token="+token) ; //"http://passport.gmall.com:8082/verify";
            HashMap<String,String> hashMap = JSON.parseObject(success, new HashMap<String,String>().getClass());

            if (!hashMap.get("success").equals("success")){
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + ReturnUrl);
            }

            request.setAttribute("memberId",hashMap.get("memberId"));
            request.setAttribute("nickname",hashMap.get("nickname"));

            CookieUtil.setCookie(request,response,"oldToken",token,60*30,true);

        }else {
            if (loginRequired.isNeedeSuccess()){
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + ReturnUrl);
                return false;
            }

        }


        return true;
    }
}