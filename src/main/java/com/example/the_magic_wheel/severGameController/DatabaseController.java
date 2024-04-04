package com.example.the_magic_wheel.severGameController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

// public class Server implements Runnable, Component {
//     public static void main(String[] args) throws IOException {
//         DatabaseController databaseController = DatabaseController.getInstance();
//         String keywordstring = databaseController.getKeyWordString();
//         System.out.println(keywordstring);
       
//     }



public class DatabaseController {
    private static DatabaseController instance = null;
    private List<Keyword> keywordList;

    private DatabaseController() {
        keywordList = new ArrayList<>();
        readDataFromFile();
    }

    public static DatabaseController getInstance() {
        if (instance == null) {
            instance = new DatabaseController();
        }
        return instance;
    }

    public String getKeyWordString() {
        // return a string with format: keyword#hint
        if (keywordList.size() == 0) {
            return "No keyword available";
        }
        int lastIdx = keywordList.size() - 1;
        String keywordAndHint = keywordList.get(lastIdx).getKeyword() + "#" + keywordList.get(lastIdx).getHint();
        keywordList.remove(lastIdx);
        return keywordAndHint;
    }

    public Keyword getKeyWord() {
        // return a keyword object
        Keyword keyword = new Keyword("", "No keyword available");
        if (keywordList.size() == 0) {
            return keyword;
        }
        int lastIdx = keywordList.size() - 1;
        keyword = keywordList.get(lastIdx);
        keywordList.remove(lastIdx);
        return keyword;
    }

    private void readDataFromFile() {
        String workingDir = System.getProperty("user.dir");
        workingDir += "/src/main/java/com/example/the_magic_wheel/severGameController/database.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(workingDir))) {
            int numKeywords = Integer.parseInt(reader.readLine());
            for (int i = 0; i < numKeywords; i++) {
                String keyword = reader.readLine();
                String hint = reader.readLine();
                keywordList.add(new Keyword(keyword, hint));
            }
            Collections.sort(keywordList, new SortByPriorityNo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SortByPriorityNo implements Comparator<Keyword> {

        // Method
        // Sorting in ascending order of priorityNo
        public int compare(Keyword a, Keyword b) {

            return a.getPriorityNo() - b.getPriorityNo();
        }
    }

    private static class Keyword {
        private String keyword;
        private String hint;
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
}
