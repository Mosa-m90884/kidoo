package com.example.kiddo.Model;
public class TaskInfo {
    private String taskName;
    private String date;
    private String imageUrl; // رابط الصورة
    private int points; // النقاط
    private boolean isCompleted; // حالة المهمة

    public TaskInfo(String taskName, String date, String imageUrl, int points, boolean isCompleted) {
        this.taskName = taskName;
        this.date = date;
        this.imageUrl = imageUrl;
        this.points = points;
        this.isCompleted = isCompleted;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // getters
    public String getTaskName() { return taskName; }
    public String getDate() { return date; }
    public String getImageUrl() { return imageUrl; }
    public int getPoints() { return points; }
    public boolean isCompleted() { return isCompleted; }
}