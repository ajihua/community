package com.nowcoder.community.service;

import com.nowcoder.community.component.CommunityConstant;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;

import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

//    @Resource
//    private LoginTicketMapper loginTicketMapper;
    @Resource
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(Integer id) {
//        return userMapper.selectByPrimaryKey(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }


        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(CommunityConstant.USER_TYPE);
        user.setStatus(CommunityConstant.NOT_ACTIVATION_STATUS);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertSelective(user);

        // // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public User selectByEmail(String email){
        return userMapper.selectByEmail(email);
    }

    public Integer activation(Integer userId, String code) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user.getStatus() == 1) {
            return CommunityConstant.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, CommunityConstant.ACTIVATION_STATUS);
            clearCache(userId);
            return CommunityConstant.ACTIVATION_SUCCESS;
        } else {
            return CommunityConstant.ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user==null || !user.getPassword().equals(CommunityUtil.md5(password+user.getSalt()))){
            map.put("usernameMsg", "账号或者密码错误!");
            return map;
        }

        if(user.getStatus() == CommunityConstant.NOT_ACTIVATION_STATUS){
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }


        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(CommunityConstant.YES_LOGINTICKET_STATUS);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000L));
//        loginTicketMapper.insertSelective(loginTicket);
        // TODO 登录凭证存redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put(CommunityConstant.TICKET, loginTicket.getTicket());
        map.put("loginUser", user);
        return map;
    }

    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public void logout(String ticket) {
        //loginTicketMapper.updateStatus(ticket,CommunityConstant.NOT_LOGINTICKET_STATUS);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public int updateHeader(Integer id,String headerUrl){
//        return userMapper.updateHeader(id,headerUrl);
        int rows = userMapper.updateHeader(id, headerUrl);
        clearCache(id);
        return rows;
    }

    public int updatePassword(Integer id,String srcPassword,String destPassword){
        User user = userMapper.selectByPrimaryKey(id);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }

        if(!CommunityUtil.md5(srcPassword+user.getSalt()).equals(user.getPassword())){
            return -1;
        }

        return userMapper.updatePassword(id,CommunityUtil.md5(destPassword+user.getSalt()));

    }

    public int updatePassword(Integer id,String destPassword){
        User user = userMapper.selectByPrimaryKey(id);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        return userMapper.updatePassword(id,CommunityUtil.md5(destPassword+user.getSalt()));
    }

    public User findUserByName(String toName) {
        return userMapper.selectByName(toName);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        //TODO 缓存user findById
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
