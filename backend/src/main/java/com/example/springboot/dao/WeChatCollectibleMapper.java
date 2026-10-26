package com.example.springboot.dao;

import com.example.springboot.entity.UserCollectible;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeChatCollectibleMapper {

  List<UserCollectible> findCollection();

  List<UserCollectible> collectible(@Param("phone") String phone);

  void addCollectible(@Param("phone") String phone, @Param("question") String question,
      @Param("reply") String reply, @Param("time") String time);

  void removeCollectible(@Param("phone") String phone, @Param("question") String question,
      @Param("reply") String reply);

}
