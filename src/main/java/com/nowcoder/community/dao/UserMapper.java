package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByName(String username);

    User selectByEmail(String email);

    int updateStatus(@Param("id") Integer id,@Param("status") Integer status);

    int updateHeader(@Param("id") Integer id,@Param("headerUrl") String headerUrl);
    int updatePassword(@Param("id") Integer id,@Param("password") String password);
}