package com.zhangwuji.im.api.controller;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.entity.GeoBean;
import com.zhangwuji.im.api.entity.IMUserGeoData;
import com.zhangwuji.im.api.service.IIMUserGeoDataService;
import com.zhangwuji.im.api.service.IIMUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.zhangwuji.im.api.common.JavaBeanUtil;
import com.zhangwuji.im.api.entity.IMUser;
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
    private IIMUserService iOnImuserService;

	@Resource
	@Qualifier(value = "imUserGeoDataService")
	private IIMUserGeoDataService imUserGeoDataService;
	
    @RequestMapping(value = "test", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    public Object test(HttpServletRequest req,HttpServletResponse rsp) {
    	rsp.addHeader("Access-Control-Allow-Origin", "*");

        return "helloworld!";
    }


	@RequestMapping(value = "getNearByUser", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public returnResult getNearByUser(HttpServletRequest req,HttpServletResponse rsp) {
		rsp.addHeader("Access-Control-Allow-Origin", "*");
		returnResult returnResult=new returnResult();
		Map<String, Object> returnData=new HashMap<>();
		Map<String, Double> geodata=new HashMap<>();


		IMUser myinfo=controllerUtil.checkToken(req);
		if(myinfo==null)
		{
			returnResult.setCode(returnResult.ERROR);
			returnResult.setData(returnData);
			returnResult.setMessage("token验证失败!");
			return returnResult;
		}

		int page=controllerUtil.getIntParameter(req,"page",1);
		int pagesize=controllerUtil.getIntParameter(req,"pagesize",20);
		double lng=controllerUtil.getDoubleParameter(req,"lng",0);
		double lat=controllerUtil.getDoubleParameter(req,"lat",0);


		List<GeoBean> geoBeanList = new LinkedList<>();
		List<GeoBean> pageGeoList = new LinkedList<>();

		redisHelper.cacheGeo("hunan22",122.172565,37.419147,"1",13600*10);
		redisHelper.cacheGeo("hunan22",122.172565,37.417147,"2",13600*10);
		redisHelper.cacheGeo("hunan22",122.172565,37.415147,"3",13600*10);
		redisHelper.cacheGeo("hunan22",122.172565,37.416147,"4",13600*10);
		String geojson="";


		//流程:先从数据库查找缓存。看有没有缓存数据，如果有的话，直接读取缓存数据进行查分页查找。没有缓存数据时，用redis geo里面进行搜索
		IMUserGeoData  imUserGeoData2=imUserGeoDataService.getOne(new QueryWrapper<IMUserGeoData>().eq("uid",myinfo.getId()));
		if(imUserGeoData2!=null && imUserGeoData2.getId()>0 && imUserGeoData2.getUpdated()>0 && ((controllerUtil.timestamp()-imUserGeoData2.getUpdated())<60*10))
		{
			geojson=imUserGeoData2.getData();
			geoBeanList=JSON.parseArray(geojson,GeoBean.class);
		}
		else
		{
			GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults=redisHelper.radiusGeo("hunan22",lng,lat,1000, Sort.Direction.ASC,100);
			List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> geoResults1= geoResults.getContent();
			for (GeoResult<RedisGeoCommands.GeoLocation<Object>> item:geoResults){
				GeoBean geoBean=new GeoBean();
				geoBean.setDis(item.getDistance().getValue());
				geoBean.setKey(item.getContent().getName().toString());
				geoBeanList.add(geoBean);
			}
			//将json存到数据库埋在去
			geojson=JSON.toJSONString(geoBeanList);
		}

		if(imUserGeoData2==null || imUserGeoData2.getUid()<=0)
		{
			IMUserGeoData imUserGeoData=new IMUserGeoData();
			imUserGeoData.setId(null);
			imUserGeoData.setUid(myinfo.getId());
			imUserGeoData.setData(geojson);
			imUserGeoData.setStatus(1);
			imUserGeoData.setLat(lat);
			imUserGeoData.setLng(lng);

			imUserGeoData.setUpdated(controllerUtil.timestamp());
			imUserGeoDataService.save(imUserGeoData);
		}

		pageGeoList=javaBeanUtil.sublist(geoBeanList,page,pagesize);
        List<String> userids = new LinkedList<>();
		for (GeoBean geoBean:pageGeoList) {
			userids.add(geoBean.getKey());
		}

		String uids = StringUtils.join(userids, ",");
		List<Map<String, Object>> userslist=iOnImuserService.getUsersInfo(uids);


		//哈哈。连环for。主要是为了排序和输出dists
		List<Map<String, Object>> returndatalist=new LinkedList<>();
		for (GeoBean geoBean:pageGeoList) {
			for (Map<String, Object> map:userslist) {
				if(geoBean.getKey().equals(map.get("id").toString()))
				{
					map.put("dists",geoBean.getDis());
					returndatalist.add(map);
					break;
				}
			}
		}

		returnResult.setCode(returnResult.SUCCESS);
		returnResult.setData(returndatalist);
		returnResult.setMessage("查询成功!");
		return returnResult;
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

		IMUser users=iOnImuserService.getOne(new QueryWrapper<IMUser>().eq("appId",appid).eq("username",username));
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

		IMUser myinfo=controllerUtil.checkToken(req);
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
		Page<IMUser> questionStudent = iOnImuserService.getAllUserBypage(new Page<>(page, size));

		IMUser users=iOnImuserService.getById(1);

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
