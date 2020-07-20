package com.google.sps.data;

public class CommentClass {
    String comment;
    float sentiment;

    public CommentClass(String newComment, float score) {
        comment = newComment;
        sentiment = score;
    }
    public String getComment(){
        return comment;
    }
    
    public float getSentiment(){
        return sentiment;
    }
}
