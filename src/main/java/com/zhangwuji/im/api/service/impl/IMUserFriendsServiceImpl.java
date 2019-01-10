package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMUserFriends;
import com.zhangwuji.im.api.mapper.IMUserFriendsMapper;
import com.zhangwuji.im.api.service.IIMUserFriendsService;
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
public class IMUserFriendsServiceImpl extends ServiceImpl<IMUserFriendsMapper, IMUserFriends> implements IIMUserFriendsService {

}
