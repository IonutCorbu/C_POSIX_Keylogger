package org.example.DTO;

import java.util.List;

public class PasswordPredictionResponse {

    private double threshold;
    private int total_words;
    private int password_candidates;
    private List<ResultItem> results;

    public PasswordPredictionResponse() {}

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public int getTotal_words() { return total_words; }
    public void setTotal_words(int total_words) { this.total_words = total_words; }

    public int getPassword_candidates() { return password_candidates; }
    public void setPassword_candidates(int password_candidates) { this.password_candidates = password_candidates; }

    public List<ResultItem> getResults() { return results; }
    public void setResults(List<ResultItem> results) { this.results = results; }

    public static class ResultItem {
        private String word;
        private double prob_password;

        public ResultItem() {}

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public double getProb_password() { return prob_password; }
        public void setProb_password(double prob_password) { this.prob_password = prob_password; }
    }
}