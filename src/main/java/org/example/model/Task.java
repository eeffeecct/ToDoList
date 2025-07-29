package org.example.model;

import java.time.LocalDateTime;

public class Task {
    private Integer id;
    private String title;
    private String description;
    private Boolean isCompleted;
    private LocalDateTime createdAt;

    public Task(LocalDateTime createdAt, String description, Integer id, Boolean isCompleted, String title) {
        this.createdAt = createdAt;
        this.description = description;
        this.id = id;
        this.isCompleted = isCompleted;
        this.title = title;
    }

    public Task() {
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Task{" +
                "createdAt=" + createdAt +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
