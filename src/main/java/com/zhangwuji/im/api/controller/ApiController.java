package com.zhangwuji.im.api.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.result.userinfoVo;
import com.zhangwuji.im.api.service.IOnImuserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.zhangwuji.im.api.common.JavaBeanUtil;
import com.zhangwuji.im.api.entity.OnImuser;
import com.zhangwuji.im.api.entity.ServerInfoEntity;
import com.zhangwuji.im.api.result.returnResult;
import com.zhangwuji.im.config.RedisCacheHelper;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-04
 */
@RestController
@RequestMapping(value = "/api")
public class ApiController {

	@Value("${cloudtalk.bqmmplugin.appid}")
	public String bqmmplugin_appid;
	@Value("${cloudtalk.bqmmplugin.appsecret}")
	public String bqmmplugin_appsecret;
	@Value("${cloudtalk.files.msfsprior}")
	public String files_msfsprior;
	@Value("${cloudtalk.files.msfspriorbackup}")
	public String files_msfspriorbackup;

	@Resource
	RedisCacheHelper redisHelper;
	
	@Resource
	JavaBeanUtil javaBeanUtil;

	@Resource
	ControllerUtil controllerUtil;

    @Resource
    @Qualifier(value = "imUserService")
    private IOnImuserService iOnImuserService;
	
    @RequestMapping(value = "test", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    public String test(HttpServletRequest req,HttpServletResponse rsp) {
    	rsp.addHeader("Access-Control-Allow-Origin", "*");
       return "helloworld!";
    }
    
    @RequestMapping(value = "checkLogin", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public returnResult checkLogin(HttpServletRequest req,HttpServletResponse rsp) {
    	rsp.addHeader("Access-Control-Allow-Origin", "*");
    	returnResult returnResult=new returnResult();
    	Map<String, Object> returnData=new HashMap<>();
		ServerInfoEntity serverinfo=new ServerInfoEntity();
		Map<String, Object> bmqq_plugin=new HashMap<>();

		String appid=req.getParameter("appId");
		String username=req.getParameter("username");
		String password=req.getParameter("password");

		OnImuser users=iOnImuserService.getOne(new QueryWrapper<OnImuser>().eq("appId",appid).eq("username",username));
		if(users==null || users.getId()==0)
		{
			returnResult.setCode(returnResult.ERROR);
			returnResult.setData(returnData);
			returnResult.setMessage("账号不存在!");
			return returnResult;
		}

		password=DigestUtils.md5Hex(password+users.getSalt()).toLowerCase();
		if(!users.getPassword().equals(password))
		{
			returnResult.setCode(returnResult.ERROR);
			returnResult.setData(returnData);
			returnResult.setMessage("密码错误!");
			return returnResult;
		}

		//*********从redis中获取 负载量小的 聊天服务器************
    	//****************************************************
    	Map<Object, Object> serverlistmap=new HashMap<>();
        String selectServerInfo="";
    	serverlistmap=redisHelper.hmget("msg_srv_list");
    	if(serverlistmap!=null && serverlistmap.size()>0)
    	{
    		serverlistmap= javaBeanUtil.sortMapByValue(serverlistmap);
    		selectServerInfo= javaBeanUtil.getFirstKeyFromMap(serverlistmap).toString();
    		serverinfo.setServer_ip(selectServerInfo.split("\\|")[0]);
    		serverinfo.setServer_ip2(selectServerInfo.split("\\|")[1]);
    		serverinfo.setServer_port(Integer.parseInt(selectServerInfo.split("\\|")[2]));
    	}

    	bmqq_plugin.put("appid",bqmmplugin_appid);
		bmqq_plugin.put("appsecret",bqmmplugin_appsecret);


		Map<String, Object> returnUsers=JavaBeanUtil.convertBeanToMap(users);
		returnUsers.remove("password");

    	returnData.put("token", users.getApiToken());
       	returnData.put("userinfo", returnUsers);
    	returnData.put("serverinfo", serverinfo);
     	returnData.put("bqmmplugin", bmqq_plugin);


		iOnImuserService.updateById(users);

     	returnResult.setCode(returnResult.SUCCESS);
     	returnResult.setData(returnData);
     	returnResult.setMessage("登录成功!");
    	
        return returnResult;
    }

	@RequestMapping(value = "getUserInfo", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public returnResult getUserInfo(HttpServletRequest req,HttpServletResponse rsp) {
		rsp.addHeader("Access-Control-Allow-Origin", "*");
		returnResult returnResult=new returnResult();
		Map<String, Object> returnData=new HashMap<>();

		OnImuser myinfo=controllerUtil.checkToken(req);
		if(myinfo==null)
		{
			returnResult.setCode(returnResult.ERROR);
			returnResult.setData(returnData);
			returnResult.setMessage("token验证失败!");
			return returnResult;
		}


		String friuid=req.getParameter("friuids");
		List<Map<String, Object>> userslist=iOnImuserService.getUsersInfo(friuid);

		if(userslist.size()>0)
		{
			returnData.put("userinfo",userslist);

			returnResult.setCode(returnResult.SUCCESS);
			returnResult.setData(returnData);
		}
		else
		{
			returnResult.setCode(returnResult.ERROR);
		}
		returnResult.setMessage("查询成功!");
		return returnResult;
	}

    @RequestMapping(value = "getSrvInfo", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    public returnResult getSrvInfo(HttpServletRequest req,HttpServletResponse rsp) {
    	rsp.addHeader("Access-Control-Allow-Origin", "*");
    	returnResult returnResult=new returnResult();
    	Map<String, Object> returnData=new HashMap<>();
		ServerInfoEntity serverinfo=new ServerInfoEntity();
		
    	//*********从redis中获取 负载量小的 聊天服务器************
    	//***************************************************
    	Map<Object, Object> serverlistmap=new HashMap<>();
        String selectServerInfo="";
    	serverlistmap=redisHelper.hmget("msg_srv_list");
    	if(serverlistmap!=null && serverlistmap.size()>0)
    	{
    		serverlistmap= javaBeanUtil.sortMapByValue(serverlistmap);
    		selectServerInfo= javaBeanUtil.getFirstKeyFromMap(serverlistmap).toString();
    		serverinfo.setServer_ip(selectServerInfo.split("\\|")[0]);
    		serverinfo.setServer_ip2(selectServerInfo.split("\\|")[1]);
    		serverinfo.setServer_port(Integer.parseInt(selectServerInfo.split("\\|")[2]));
    	}

    	serverinfo.setMsfsPrior(files_msfsprior);
    	serverinfo.setMsfsBackup(files_msfspriorbackup);
     	returnResult.setCode(returnResult.SUCCESS);
     	returnResult.setData(serverinfo);
     	returnResult.setMessage("登录成功!");

        return returnResult;
    }

	@RequestMapping("/users/{page}/{size}")
	public Map<String, Object> users(@PathVariable Integer page, @PathVariable Integer size) {
		Map<String, Object> map = new HashMap<>();
		Page<OnImuser> questionStudent = iOnImuserService.getAllUserBypage(new Page<>(page, size));

		OnImuser users=iOnImuserService.getById(1);

		List<Map<String, Object>> list=iOnImuserService.selectUser2();

		if (questionStudent.getRecords().size() == 0) {
			map.put("code", 400);
		} else {
			map.put("code", 200);
			map.put("data", questionStudent);
		}
		return map;
	}
    
}
