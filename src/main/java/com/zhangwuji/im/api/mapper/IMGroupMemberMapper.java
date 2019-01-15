package com.zhangwuji.im.api.mapper;

import com.zhangwuji.im.api.entity.IMGroupMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户和群的关系表 Mapper 接口
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-15
 */
public interface IMGroupMemberMapper extends BaseMapper<IMGroupMember> {
    List<Map<String, Object>> getGroupMemberList(String[] array);
}
