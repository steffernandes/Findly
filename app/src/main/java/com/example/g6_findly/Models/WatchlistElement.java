package com.example.g6_findly.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class WatchlistElement {
    private String movie_id;
    private String series_id;
    private String poster_path;

    public WatchlistElement() {
    }

    public WatchlistElement( String movie_id, String poster_path, String series_id) {
        this.movie_id = movie_id;
        this.series_id = series_id;
        this.poster_path = poster_path;
    }

    public String getSeries_id() {
        return series_id;
    }

    public void setSeries_id(String series_id) {
        this.series_id = series_id;
    }

    public String getMovie_id() {
        return movie_id;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setMovie_id(String movie_id) {
        this.movie_id = movie_id;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

}
