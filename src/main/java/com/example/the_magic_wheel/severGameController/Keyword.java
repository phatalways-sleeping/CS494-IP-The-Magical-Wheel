package com.example.the_magic_wheel.severGameController;

import java.util.Random;

public class Keyword {
    private final String keyword;
    private final String hint;
    private int priorityNo = 0;

    public Keyword(String keyword, String hint) {
        this.keyword = keyword;
        this.hint = hint;
        this.priorityNo = new Random().nextInt(100);
    }

    public String getKeyword() {
        return keyword;
    }

    public String getHint() {
        return hint;
    }

    public int getPriorityNo() {
        return priorityNo;
    }
}
