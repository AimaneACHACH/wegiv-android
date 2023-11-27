package com.ziyad.wegiv;

import android.graphics.Bitmap;
import android.net.Uri;

public class Article {
    String Id, Title, Category, Description, Points, PublishingDate, ExpirationDate, Location, Publisher;
    Boolean doesExpire;

    public Article(String id, String title, String category, String description, String points, String publishingDate, String expirationDate, String location, String publisher, Boolean doesExpire) {
        Id = id;
        Title = title;
        Category = category;
        Description = description;
        Points = points;
        PublishingDate = publishingDate;
        ExpirationDate = expirationDate;
        Location = location;
        Publisher = publisher;
        this.doesExpire = doesExpire;
    }

    public Article() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPoints() {
        return Points;
    }

    public void setPoints(String points) {
        Points = points;
    }

    public String getPublishingDate() {
        return PublishingDate;
    }

    public void setPublishingDate(String publishingDate) {
        PublishingDate = publishingDate;
    }

    public String getExpirationDate() {
        return ExpirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        ExpirationDate = expirationDate;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public Boolean getDoesExpire() {
        return doesExpire;
    }

    public void setDoesExpire(Boolean doesExpire) {
        this.doesExpire = doesExpire;
    }

    @Override
    public String toString() {
        return "Article{" +
                "Id='" + Id + '\'' +
                ", Title='" + Title + '\'' +
                ", Category='" + Category + '\'' +
                ", Description='" + Description + '\'' +
                ", Points='" + Points + '\'' +
                ", PublishingDate='" + PublishingDate + '\'' +
                ", ExpirationDate='" + ExpirationDate + '\'' +
                ", Location='" + Location + '\'' +
                ", Publisher='" + Publisher + '\'' +
                ", doesExpire=" + doesExpire +
                '}';
    }
}
