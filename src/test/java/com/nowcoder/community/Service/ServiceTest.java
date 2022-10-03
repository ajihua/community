//package com.nowcoder.community.Service;
//
//import com.nowcoder.community.CommunityApplicationTests;
//import com.nowcoder.community.dao.DiscussPostMapper;
//import com.nowcoder.community.entity.User;
//import com.nowcoder.community.service.UserService;
//import com.nowcoder.community.util.MailClient;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//public class ServiceTest extends CommunityApplicationTests {
//
//    private Logger log = LoggerFactory.getLogger(this.getClass());
//    @Resource
//    private UserService userService;
//
//    @Resource
//    private MailClient mailClient;
//
//    @Resource
//    DiscussPostMapper discussPostMapper;
//
//    @Resource
//    private KafkaProducer kafkaProducer;
//
//    @Test
//    public void test_1() {
////        mailClient.sendMail("lizuoyuanqaq@qq.com","title","hello");
//        kafkaProducer.sendMessage("test", "你好");
//        kafkaProducer.sendMessage("test", "在吗");
//
//        try {
//            Thread.sleep(1000 * 10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//}
//
//@Component
//class KafkaConsumer {
//
//    @KafkaListener(topics = {"test"})
//    public void handleMessage(ConsumerRecord record) {
//        System.out.println(record.value());
//    }
//
//
//}
//
//@Component
//class KafkaProducer {
//
//    @Resource
//    private KafkaTemplate kafkaTemplate;
//
//    public void sendMessage(String topic, String content) {
//        kafkaTemplate.send(topic, content);
//    }
//
//}
