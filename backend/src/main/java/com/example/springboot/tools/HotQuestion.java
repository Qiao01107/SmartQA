package com.example.springboot.tools;

public class HotQuestion {
    private int sum;
    private String reply;
    private String question;

    public HotQuestion() {
    }

    public HotQuestion(int sum, String reply, String question) {
        this.sum = sum;
        this.reply = reply;
        this.question = question;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
