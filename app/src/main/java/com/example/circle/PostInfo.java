package com.example.circle;

public class PostInfo {
    String key,creator,circleName,postText,postImage,postTime,postDate;


    public PostInfo() {

    }
    public PostInfo(String key,String creator, String circleName, String postText, String postImage, String postTime, String postDate) {
        this.key = key;
        this.creator = creator;
        this.circleName = circleName;
        this.postText = postText;
        this.postImage = postImage;
        this.postTime = postTime;
        this.postDate = postDate;
    }
    public PostInfo(String key,String creator, String circleName, String postText,  String postTime, String postDate) {
        this.key = key;
        this.creator = creator;
        this.circleName = circleName;
        this.postText = postText;
        this.postTime = postTime;
        this.postDate = postDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCircleName() {
        return circleName;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }
}
