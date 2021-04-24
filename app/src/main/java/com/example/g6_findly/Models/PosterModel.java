package com.example.g6_findly.Models;

public class PosterModel {
    public String image;
    public String name;
    public String date;
    public String rating;
    public String overview;
    public String status;
    public String id;


    public PosterModel() {
    }

    public PosterModel(String image, String name, String date , String rating, String overview, String status, String id ) {
        this.image = image;
        this.name = name;
        this.date = date;
        this.rating = rating;
        this.overview = overview;
        this.status = status;
        this.id= id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
