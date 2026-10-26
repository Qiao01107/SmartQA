package com.example.springboot.service;

import com.example.springboot.dao.QuestionManageMapper;
import com.example.springboot.dao.TemplateManageMapper;
import com.example.springboot.entity.Question;
import com.example.springboot.entity.QuestionTemplate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateManageService {

  @Autowired
  private TemplateManageMapper templateManageMapper;

  @Autowired
  private QuestionManageMapper questionManageMapper;

  // 问题模板表相关接口
  public boolean addTemplate(String questionTemplate, String preciseQuestion) {
    List<QuestionTemplate> questionTemplates = templateManageMapper.findAllTemplate();
    for (QuestionTemplate i : questionTemplates) {
      if (questionTemplate.equals(i.getQuestionTemplate())) {
        return false;
      }
    }
    List<Question> Questions = questionManageMapper.findAllQuestion();
    int questionId = -1;
    for (Question i : Questions) {
      if (i.getQuestion().equals(preciseQuestion)) {
        questionId = i.getId();
        break;
      }
    }
    if (questionId == -1) {
      questionManageMapper.addQuestion(preciseQuestion, null, "0");
      Questions = questionManageMapper.findAllQuestion();
      for (Question i : Questions) {
        if (i.getQuestion().equals(preciseQuestion)) {
          questionId = i.getId();
        }
      }
    }
    templateManageMapper.addQuestionTemplate(questionId, questionTemplate, preciseQuestion);
    return true;
  }

  public boolean removeTemplate(String template) {
    templateManageMapper.removeQuestionTemplate(template);
    return true;
  }

  public boolean changeTemplate(int questionId, String oldTemplate, String newTemplate) {
    List<QuestionTemplate> questionTemplates = templateManageMapper.findAllTemplate();
    for (QuestionTemplate i : questionTemplates) {
      if (i.getQuestionTemplate().equals(newTemplate)) {
        return false;
      }
    }
    templateManageMapper.changeQuestionTemplate(questionId, oldTemplate, newTemplate);
    return true;
  }

  public List<QuestionTemplate> searchTemplate(int field, String inquire) {
    List<QuestionTemplate> questionTemplates = new ArrayList<>();
    List<QuestionTemplate> allTemplates = templateManageMapper.findAllTemplate();
    if (field == 1) {
      for (QuestionTemplate i : allTemplates) {
        String template = i.getQuestionTemplate();
        if (template.contains(inquire)) {
          questionTemplates.add(i);
        }
      }
    } else {
      for (QuestionTemplate i : allTemplates) {
        String question = i.getPreciseQuestion();
        if (question.contains(inquire)) {
          questionTemplates.add(i);
        }
      }
    }
    return questionTemplates;
  }

  public int countTemplate() {
    return templateManageMapper.countTemplate();
  }

  public List<QuestionTemplate> getTemplate(int start, int number) {
    return templateManageMapper.getTemplate(start - 1, number);
  }

}
