package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiscussPostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DiscussPost record);

    int insertSelective(DiscussPost record);

    DiscussPost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DiscussPost record);

    int updateByPrimaryKey(DiscussPost record);

    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId,
                                         @Param("offset") Integer offset,
                                         @Param("limit")Integer limit);

    Integer selectDiscussPostRows(@Param("userId") Integer userId);

    /**
     * 更新帖子评论数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(@Param("id")int id,@Param("commentCount") int commentCount);
}