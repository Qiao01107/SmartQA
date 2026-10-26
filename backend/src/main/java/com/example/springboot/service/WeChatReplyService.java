package com.example.springboot.service;

import static org.neo4j.driver.Values.parameters;

import com.example.springboot.dao.WeChatReplyMapper;
import com.example.springboot.entity.Question;
import com.example.springboot.entity.QuestionTemplate;
import com.example.springboot.entity.SearchTime;
import com.example.springboot.tools.HotQuestion;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeChatReplyService {

  private final WeChatReplyMapper weChatReplyMapper;
  private final Driver driver;
  private final String fuzzyWordsPath;

  @Autowired
  public WeChatReplyService(WeChatReplyMapper weChatReplyMapper, Driver driver,
      @Value("${HANLP_FUZZY_WORDS:hanlp-data/fuzzyWords.txt}") String fuzzyWordsPath) {
    this.weChatReplyMapper = weChatReplyMapper;
    this.driver = driver;
    this.fuzzyWordsPath = fuzzyWordsPath;
  }

  public void addSearch(String question, String answer) {
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String str = df.format(date);
    if (!answer.equals("抱歉，暂无解答")) {
      weChatReplyMapper.addSearch(question, str, answer);
    }
  }

  public String[] getApart(String str) {
    List<Term> termList;
    try {
      // 使用 HanLP 标准分词（portable 版内置词典，不依赖感知机大模型）。
      termList = HanLP.segment(str);
    } catch (Throwable e) {
      // HanLP 词典/模型缺失时给出明确提示，而不是让调用方看到晦涩的初始化错误。
      throw new IllegalStateException(
          "HanLP 分词不可用：请检查 backend/src/main/resources/hanlp.properties 的 root "
              + "以及 hanlp-data 目录是否完整（可用 HANLP_ROOT 覆盖）。", e);
    }
    Object[] o = termList.toArray(new Term[0]);
    String[] array = new String[o.length];
    for (int i = 0; i < o.length; i++) {
      array[i] = o[i].toString();
      array[i] = array[i].substring(0, array[i].indexOf("/"));
    }
    array = fuzzyWords(array, array.length);
    return array;
  }

  // 问答
  public String apart(String text) {
    int z = 0;
    String[] array = getApart(text);
    for (String s : array) {
      if (s.equals("是不是") || s.equals("吗")) {
        z = 1;
        break;
      }
    }
    List<Integer> list = countNum(array, array.length);
    if (que(array, array.length) == null && queOne(array, array.length) == null &&
        interrogativeQue(array, array.length) == null) {
      if (queTwo(text) == null) {
        if (queThree(array) == null) {
          return "抱歉，暂无解答";
        }
        return queThree(array);
      }
      return queTwo(text);
    }
    if (z == 1) {
      return interrogativeQue(array, array.length);
    }
    if (list.get(1) == 1) {
      return queOne(array, array.length);
    } else if (list.get(1) > 1) {
      return que(array, array.length);
    }
    return null;
  }

  public List<String> getFuzzyWords() {
    List<String> list = new LinkedList<>();
    File file = new File(fuzzyWordsPath);
    if (!file.isAbsolute()) {
      file = file.getAbsoluteFile();
    }
    if (!file.isFile()) {
      System.err.println("[HanLP] 未找到模糊词文件：" + file.getAbsolutePath()
          + "；本次问答将跳过模糊词替换。请参考 backend/hanlp-data/README.md 准备数据，"
          + "或通过 HANLP_FUZZY_WORDS 指定路径。");
      return list;
    }
    try (BufferedReader in = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line;
      while ((line = in.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          list.add(line);
        }
      }
    } catch (IOException e) {
      System.err.println("[HanLP] 读取模糊词文件失败：" + file.getAbsolutePath());
      e.printStackTrace();
    }
    return list;
  }

  public String[] fuzzyWords(String[] str, int num) {
    List<String> list = getFuzzyWords();
    for (String s : list) {
      String[] strings = s.split(" ");
      for (int j = 0; j < strings.length - 1; j++) {
        for (int k = 0; k < num; k++) {
          if (str[k].equals(strings[j])) {
            str[k] = strings[strings.length - 1];
            break;
          }
        }
      }
    }
    return str;
  }

  public List<Integer> countNum(String[] str, int num) {
    try (Session session = driver.session()) {
      int entity = 0;
      int textRelation = 0;
      int neo4jRelation = 0;
      for (int i = 0; i < num; i++) {
        Result result = session.run(
            "MATCH (a) WHERE a.name = $name " + " RETURN a.name AS name",
            parameters("name", str[i]));
        if (result.hasNext()) {
          entity++;
          for (int j = i + 1; j < num; j++) {
            Result result1 = session.run(
                "match(n)-[r]->(b) where n.name = $name and type(r)=$as " + " return b.name as name",
                parameters("as", str[j], "name", str[i]));
            if (result1.hasNext()) {
              textRelation++;
            }
            while (result1.hasNext()) {
              Record record = result1.next();
              neo4jRelation++;
            }
          }
        }
      }
      List<Integer> list = new LinkedList<>();
      list.add(entity);
      list.add(textRelation);
      list.add(neo4jRelation);
      return list;
    }
  }

  public String interrogativeQue(String[] str, int num) {
    try (Session session = driver.session()) {
      for (int i = 0; i < num; i++) {
        Result result = session.run(
            "MATCH (a) WHERE a.name = $name " + " RETURN a.name AS name",
            parameters("name", str[i]));
        if (result.hasNext()) {
          for (int j = i + 1; j < num; j++) {
            Result result1 = session.run(
                "match(n)-[r]->(b) where n.name = $name and type(r)=$as " + " return b.name as name",
                parameters("as", str[j], "name", str[i]));
            int z = 0;
            while (result1.hasNext()) {
              z = 1;
              Record record = result1.next();
              for (String s : str) {
                if (s.equals(record.get("name").asString())) {
                  return "是的";
                }
              }
            }
            if (z == 1) {
              return "不是";
            }
          }
        }
      }
      return null;
    }
  }

  public String queOne(String[] str, int num) {
    try (Session session = driver.session()) {
      String string;
      int z = 0;
      for (int i = 0; i < num; i++) {
        Result result = session.run(
            "MATCH (a) WHERE a.name = $name " + " RETURN a.name AS name",
            parameters("name", str[i]));
        if (result.hasNext()) {
          for (int j = i + 1; j < num; j++) {
            result = session.run(
                "match(n)-[r]->(b) where n.name = $name and type(r)=$as " + " return b.name as name",
                parameters("as", str[j], "name", str[i]));
            String[] array = new String[3];
            while (result.hasNext()) {
              Record record = result.next();
              if (z < 3) {
                array[z] = record.get("name").asString();
              }
              z++;
            }
            if (z == 1) {
              string = str[i] + "的" + str[j] + "是" + array[0];
              return string;
            } else if (z == 2) {
              string = str[i] + "的" + str[j] + "是" + array[0] + "、" + array[1] + "等";
              return string;
            } else if (z == 3) {
              string = str[i] + "的" + str[j] + "是" + array[0] + "、" + array[1] + "、" + array[2] + "等";
              return string;
            } else if (z > 3) {
              List<Integer> list = countNum(str, num);
              string = str[i] + "的" + str[j] + "有" + array[0] + "、" + array[1] + "、" + array[2] + "等"
                  + list.get(2) + "个" + str[j];
              return string;
            }
          }
        }
      }
      return null;
    }
  }

  public String que(String[] str, int num) {
    try (Session session = driver.session()) {
      StringBuilder string;
      for (int i = 0; i < num; i++) {
        Result result = session.run(
            "MATCH (a) WHERE a.name = $name " + " RETURN a.name AS name",
            parameters("name", str[i]));
        String entityOne = str[i];
        string = new StringBuilder(entityOne);//记录语句第一个实体
        int k = i;//记录位置
        int getNext = 0;//记录是否继续循环
        if (result.hasNext()) {
          while (getNext == 0) {
            if (k == num - 1) {
              break;//特判语句最后一位是关系的情况
            }
            for (int j = k + 1; j < num; j++) {
              result = session.run(
                  "match(n)-[r]->(b) where n.name = $name and type(r)=$as " + " return b.name as name",
                  parameters("as", str[j], "name", entityOne));
              if (result.hasNext()) {
                Record record = result.next();
                string.append("的").append(str[j]);
                entityOne = record.get("name").asString();
                k = j;
                getNext = 0;
                break;
              }
              getNext = 1;
            }
          }
        }
        if (!string.toString().equals(str[i])) {
          string.append("是").append(entityOne);
          return string.toString();
        }
      }
      return null;
    }
  }

  public String queTwo(String str) {
    List<Question> Questions = weChatReplyMapper.findQue();
    for (Question i : Questions) {
      if (i.getQuestion().equals(str) && i.getQuestionStatus().equals("1")) {
        return i.getAnswer();
      }
    }
    return null;
  }

  public String queThree(String[] str) {
    double weight;
    double max = 0;
    int id = 0;
    List<QuestionTemplate> QuestionTemplates = weChatReplyMapper.findQueTemp();
    for (QuestionTemplate i : QuestionTemplates) {
      weight = 0;
      String[] array = i.getQuestionTemplate().split("#");
      for (String s : array) {
        String[] split = s.split("_");
        for (String s1 : str) {
          double d = Double.parseDouble(split[1]);
          if (s1.equals(split[0])) {
            weight += d;
          }
        }
      }
      if (weight > max) {
        max = weight;
        id = i.getId();
      }
    }
    if (max >= 0.6) {
      List<Question> questions = weChatReplyMapper.findQue();
      for (Question j : questions) {
        if (j.getId() == id) {
          return j.getAnswer();
        }
      }
    }
    return null;
  }

  public boolean isLatestWeek(Date addTime, Date now) {
    Calendar calendar = Calendar.getInstance();  //得到日历
    calendar.setTime(now);//把当前时间赋给日历
    calendar.add(Calendar.DAY_OF_MONTH, -7);  //设置为7天前
    Date before7days = calendar.getTime();   //得到7天前的时间
    return before7days.getTime() < addTime.getTime();
  }

  public boolean similar(String str1, String str2) {
    int z = 0;
    for (String s1 : getApart(str1)) {
      for (String s2 : getApart(str2)) {
        if (s1.equals(s2)) {
          z++;
        }
      }
    }
    return z == getApart(str1).length || z == getApart(str2).length;
  }

  public List<HotQuestion> hotSearch() {
    List<SearchTime> searchTimes = weChatReplyMapper.findSearchTime();
    List<HotQuestion> hotQuestions = new ArrayList<>();
    Date date = new Date();
    for (SearchTime i : searchTimes) {
      if (isLatestWeek(i.getSearchTime(), date)) {
        if (hotQuestions.isEmpty()) {
          HotQuestion hotQuestion = new HotQuestion(1, i.getReply(), i.getQuestion());
          hotQuestions.add(hotQuestion);
        } else {
          int z = 0;
          for (HotQuestion j : hotQuestions) {
            if (j.getReply().equals(i.getReply()) && similar(i.getQuestion(), j.getQuestion())) {
              j.setSum(j.getSum() + 1);
              z = 1;
              break;
            }
          }
          if (z == 0) {
            HotQuestion hotQuestion = new HotQuestion(1, i.getReply(), i.getQuestion());
            hotQuestions.add(hotQuestion);
          }
        }
      }
    }
    Set<HotQuestion> Hashset = new HashSet<>(hotQuestions);
    List<HotQuestion> list = new ArrayList<>(Hashset);
    list.sort((o1, o2) -> Integer.compare(o2.getSum(), o1.getSum()));
    return list.subList(0, Math.min(4, list.size()));
  }
}
