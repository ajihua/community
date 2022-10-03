package com.nowcoder.community;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.apache.commons.lang3.CharUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DemoMain {
    public static void main(String[] args) {
//        DemoMain demoMain = new DemoMain();
//        demoMain.init();
//        String s = "嫖.娼+frea嫖d";
//        System.out.println(s.substring(10));
//        System.out.println(demoMain.filter(s));
//        String s="123167f9";
//        System.out.println(CommunityUtil.md5(s));
        DiscussPost post  = new DiscussPost();
        post.setTitle("test1");
        post.setContent("aabbcc");
        System.out.println(JSONObject.toJSONString(post));
    }

    private String filter(String text) {
        StringBuilder sb = new StringBuilder();
        TreeNode tempNode = root;
        int begin = 0;int position = 0;
        while (position<text.length()){
            char c = text.charAt(position);
            if(isSymbol(c)){
                if(tempNode==root){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if(tempNode==null){
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = root;
            }else if(tempNode.isEndWord){
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode = root;
            }else {
                position++;
            }
        }
        System.out.println("begin="+begin);
        System.out.println("position="+position);
        System.out.println("length="+text.length());
        sb.append(text.substring(begin));
        return sb.toString();
    }

    TreeNode root = new TreeNode();
    private static final String REPLACEMENT = "***";

    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    public void init(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(DemoMain.class.getClassLoader().getResourceAsStream("sensitive-words.txt")));
            String line ;
            while ((line=reader.readLine())!=null){
                add(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void add(String keyword) {
        TreeNode tempNode = root;
        for (int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TreeNode node = tempNode.getSubNode(c);
            if(node==null){
                node = new TreeNode();
                tempNode.addNode(c,node);
            }
            tempNode = node;
            if(i==keyword.length()-1){
                tempNode.setEndWord(true);
            }
        }
    }

    private class TreeNode{
        private boolean isEndWord = false;
        Map<Character,TreeNode> subNode = new HashMap<>();

        public boolean isEndWord() {
            return isEndWord;
        }

        public void setEndWord(boolean endWord) {
            isEndWord = endWord;
        }

        public void addNode(Character c,TreeNode node){
            subNode.put(c,node);
        }

        public TreeNode getSubNode(Character c){
            return subNode.get(c);
        }
    }
}
