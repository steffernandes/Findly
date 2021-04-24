
package com.example.g6_findly.Models;

public class ListElementsModel {
    private String name;
    private String overview;
    private String poster_path;
    private String movie_id;
    private String release_data;
    private  String id;

    public ListElementsModel() {
    }

    public ListElementsModel(String name, String overview, String poster_path, String movie_id, String release_data, String id) {
        this.name = name;
        this.id = id;
        this.overview= overview;
        this.poster_path=poster_path;
        this.movie_id= movie_id;
        this.release_data= release_data;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(String movie_id) {
        this.movie_id = movie_id;
    }

    public String getRelease_data() {
        return release_data;
    }

    public void setRelease_data(String release_data) {
        this.release_data = release_data;
    }
}

