package com.example.g6_findly.Models;

import java.util.ArrayList;

public class Suggestions {
    public ArrayList<Object> movie_suggestions = new ArrayList<>();
    public Suggestions() {
    }

    public Suggestions(ArrayList movie_suggestions) {
        this.movie_suggestions = movie_suggestions;
    }

    public ArrayList getSuggestions() {
        return movie_suggestions;
    }
}
