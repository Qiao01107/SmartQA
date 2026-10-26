package com.example.springboot.dao;

import com.example.springboot.entity.QuestionTemplate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TemplateManageMapper {

  // 问题模板表相关接口
  void addQuestionTemplate(@Param("id") int id, @Param("template") String template,
      @Param("preciseQuestion") String preciseQuestion);

  void removeQuestionTemplate(@Param("template") String template);

  void removeTemplateByQuestion(@Param("question") String question);

  void changeQuestionTemplate(@Param("questionId") int questionId,
      @Param("oldTemplate") String oldTemplate,
      @Param("newTemplate") String newTemplate);

  void changePreciseQuestion(@Param("questionId") int questionId,
      @Param("newQuestion") String newQuestion);

  int countTemplate();

  List<QuestionTemplate> getTemplate(@Param("start") int start, @Param("number") int number);

  List<QuestionTemplate> findAllTemplate();

}
