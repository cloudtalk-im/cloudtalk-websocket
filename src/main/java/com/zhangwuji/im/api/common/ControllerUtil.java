package com.zhangwuji.im.api.common;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.service.IMUserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Component
public class ControllerUtil {

    @Resource
    @Qualifier(value = "imUserService")
    private IMUserService iOnImuserService;

    public IMUser checkToken(HttpServletRequest req)
    {
        String appId=req.getHeader("appid");
        String token=req.getHeader("token");
        if(appId==null ||appId=="")
        {
            appId= req.getParameter("appid");
            token= req.getParameter("token");
        }
        if(token==null|| token=="")return null;

        IMUser user=iOnImuserService.getOne(new QueryWrapper<IMUser>().eq("appId",appId).eq("api_token",token));
        return user;
    }

}
