package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.component.CommunityConstant;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    String[] suffixNames = {"png", "jpg", "jpeg", "gif"};
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private MailClient mailClient;
    @Resource
    private LikeService likeService;
    @Resource
    private FollowService followService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
//        if (hostHolder.getUser() == null) {
//            return "redirect:/login";
//        }
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
//        if (hostHolder.getUser() == null) {
//            return "redirect:/login";
//        }
        if (headerImage == null) {
            model.addAttribute("error", "????????????????????????!");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!Arrays.asList(suffixNames).contains(suffix)) {
            model.addAttribute("error", "?????????????????????");
            return "/site/setting";
        }

        fileName = CommunityUtil.generateUUID() + "." + suffix;
        File path = new File(uploadPath);
        File dest = new File(uploadPath, fileName);
        if (!path.exists()) {
            dest.mkdir();
        }
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
            throw new RuntimeException("??????????????????,?????????????????????!", e);
        }

        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @LoginRequired
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ?????????????????????
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        response.setContentType("image/" + suffix);
        FileInputStream fis = null;
        try {
            OutputStream os = response.getOutputStream();
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @LoginRequired
    @PostMapping("/update/p")
    public String updatePassword(String srcPassword,String destPassword1,String destPassword2, Model model) {
        if(!destPassword1.equals(destPassword2)){
            model.addAttribute("error","2??????????????????");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        userService.updatePassword(user.getId(),srcPassword,destPassword1);
        model.addAttribute("msg", "?????????????????????");
        model.addAttribute("target", "/index");
        return "/site/operate-result";
    }

    @PostMapping("/forget/password")
    public String updatePassword(String email,Model model) {
        if(StringUtils.isBlank(email)){
            model.addAttribute("error","????????????");
            return "/site/password";
        }
        User user = userService.selectByEmail(email);
        if(user==null || !email.equals(user.getEmail())){
            model.addAttribute("error","????????????");
            return "/site/password";
        }
        String newPassword = CommunityUtil.generateUUID().substring(0,6);
        userService.updatePassword(user.getId(), newPassword);
        mailClient.sendMail(email,"?????????","?????????????????? "+newPassword);
        model.addAttribute("msg", "????????????????????????????????????????????????????????????");
        model.addAttribute("target", "/index");
        return "/site/operate-result";
    }

    @GetMapping("/forget")
    public String updatePassword() {
        return "/site/password";
    }

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        //TODO ?????????
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????!");
        }

        // ??????
        model.addAttribute("user", user);
        // ????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // ????????????
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ???????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

}
