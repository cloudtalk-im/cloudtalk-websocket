package com.zhangwuji.im.api.common;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.service.IIMUserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Component
public class ControllerUtil {

    @Resource
    @Qualifier(value = "imUserService")
    private IIMUserService iOnImuserService;

    public int getIntParameter(HttpServletRequest req,String key,int def)
    {
        String value=req.getParameter(key);
        if(value!=null && value!="")
        {
            return Integer.parseInt(value);
        }
        else
        {
            return def;
        }
    }
    public double getDoubleParameter(HttpServletRequest req,String key,double def)
    {
        String value=req.getParameter(key);
        if(value!=null && value!="")
        {
            return Double.parseDouble(value);
        }
        else
        {
            return def;
        }
    }
    public String getStringParameter(HttpServletRequest req,String key,String def)
    {
        String value=req.getParameter(key);
        if(value!=null && value!="")
        {
            return value;
        }
        else
        {
            return def;
        }
    }

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

    public  Long timestamp() {
        long timeStampSec = System.currentTimeMillis()/1000;
        String timestamp = String.format("%010d", timeStampSec);
        return Long.parseLong(timestamp);
    }

}
