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
public class QuestionManageService {

  @Autowired
  private QuestionManageMapper questionManageMapper;

  @Autowired
  private TemplateManageMapper templateManageMapper;

  // 问题表相关接口
  public boolean addQuestion(String question, String answer) {
    List<Question> Questions = questionManageMapper.findAllQuestion();
    for (Question i : Questions) {
      if (question.equals(i.getQuestion())) {
        return false;
      }
    }
    String status;
    if (answer == null || answer.equals("")) {
      status = "0";
    } else {
      status = "1";
    }
    questionManageMapper.addQuestion(question, answer, status);
    return true;
  }

  public boolean removeQuestion(String question) {
    List<QuestionTemplate> questionTemplates = templateManageMapper.findAllTemplate();
    for (QuestionTemplate i : questionTemplates) {
      if (i.getPreciseQuestion().equals(question)) {
        return false;
      }
    }
    questionManageMapper.removeQuestion(question);
    return true;
  }

  public boolean insistRemoveQuestion(String question) {
    templateManageMapper.removeTemplateByQuestion(question);
    questionManageMapper.removeQuestion(question);
    return true;
  }

  public String changeQuestion(int questionId, String newQuestion) {
    List<Question> Questions = questionManageMapper.findAllQuestion();
    String oldQuestion = "";
    for (Question i : Questions) {
      if (i.getId() == questionId) {
        oldQuestion = i.getQuestion();
      }
      if (i.getQuestion().equals(newQuestion)) {
        return "数据库中已有该问题!";
      }
    }
    List<QuestionTemplate> questionTemplates = templateManageMapper.findAllTemplate();
    for (QuestionTemplate i : questionTemplates) {
      if (i.getPreciseQuestion().equals(oldQuestion)) {
        return "由于问题模板表产生的外键约束，修改失败!";
      }
    }
    questionManageMapper.changeQuestion(questionId, newQuestion);
    return "修改成功!";
  }

  public boolean insistChangeQuestion(int questionId, String newQuestion) {
    templateManageMapper.changePreciseQuestion(questionId, newQuestion);
    questionManageMapper.changeQuestion(questionId, newQuestion);
    return true;
  }

  public boolean changeAnswer(int questionId, String newAnswer) {
    String status;
    if (newAnswer == null || newAnswer.equals("")) {
      status = "0";
    } else {
      status = "1";
    }
    questionManageMapper.changeAnswer(questionId, newAnswer, status);
    return true;
  }

  public List<Question> searchQuestion(String inquire) {
    List<Question> Questions = new ArrayList<>();
    List<Question> allQuestion = questionManageMapper.findAllQuestion();
    for (Question i : allQuestion) {
      String question = i.getQuestion();
      if (question.contains(inquire)) {
        Questions.add(i);
      }
    }
    return Questions;
  }

  public List<Question> screenQuestion(String status) {
    return questionManageMapper.screenQuestion(status);
  }

  public int countQuestion() {
    return questionManageMapper.countQuestion();
  }

  public List<Question> getQuestion(int start, int number) {
    return questionManageMapper.getQuestion(start - 1, number);
  }

}
