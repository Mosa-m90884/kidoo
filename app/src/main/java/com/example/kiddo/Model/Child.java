package com.example.kiddo.Model;
public class Child {
    private String id;

    private String username;
    private String email;
    private String parentId; // معرف الأب

    public Child() {
        // مطلوب لمكتبة Firebase
    }

    public Child(String username, String email, String parentId) {
        this.username = username;
        this.email = email;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
// getters and setters
}