package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.OnImuserfriends;
import com.zhangwuji.im.api.mapper.OnImuserfriendsMapper;
import com.zhangwuji.im.api.service.IOnImuserfriendsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-04
 */
@Service
@Qualifier(value = "iOnImuserfriendsService")
public class OnImuserfriendsServiceImpl extends ServiceImpl<OnImuserfriendsMapper, OnImuserfriends> implements IOnImuserfriendsService {

}
