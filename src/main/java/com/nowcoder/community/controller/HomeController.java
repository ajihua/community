package com.nowcoder.community.controller;

import com.nowcoder.community.component.CommunityConstant;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;



    @GetMapping("/index")
    public String getIndexPage(Model model, Page page) {
        page.setRows(discussPostService.selectDiscussPostRows(null));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.selectDiscussPosts(null, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
                //TODO第四章
                long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }


    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }


}
