
package com.example.g6_findly.Models;

public class SearchResults {
    private String title;
    private String id;
    private String image;
    private String movie_id;




    private String poster_path;
    public SearchResults() {
    }

    public SearchResults(String title,String id, String image, String poster_path, String movie_id) {
        this.title = title;
        this.id = id;
        this.image = image;
        this.poster_path = poster_path;
        this.movie_id = movie_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getMovie_id() {
        return movie_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
}

