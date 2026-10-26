package com.example.springboot.service;

import com.example.springboot.dao.WeChatCollectibleMapper;
import com.example.springboot.entity.UserCollectible;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeChatCollectibleService {

  @Autowired
  private WeChatCollectibleMapper weChatCollectibleMapper;

  // 查看收藏
  public List<UserCollectible> collectible(String phone) {
    return weChatCollectibleMapper.collectible(phone);
  }

  // 判断收藏
  public boolean getCollectible(String phone, String question, String reply) {
    List<UserCollectible> collectibles = weChatCollectibleMapper.findCollection();
    for (UserCollectible i : collectibles) {
      if (i.getPhone().equals(phone) && i.getQuestion().equals(question) && i.getReply()
          .equals(reply)) {
        return true;
      }
    }
    return false;
  }

  // 添加收藏
  public boolean addCollectible(String phone, String question, String reply) {
    Date date = new Date();//获取当前的日期
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    String str = df.format(date);
    weChatCollectibleMapper.addCollectible(phone, question, reply, str);
    return true;
  }

  // 取消收藏
  public boolean removeCollectible(String phone, String question, String reply) {
    weChatCollectibleMapper.removeCollectible(phone, question, reply);
    return true;
  }

}
