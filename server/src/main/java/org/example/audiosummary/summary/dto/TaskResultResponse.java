package org.example.audiosummary.summary.dto;

public class TaskResultResponse {
    private Long taskId;
    private String status;
    private String summary;

    public TaskResultResponse() {
    }

    public TaskResultResponse(Long taskId, String status, String summary) {
        this.taskId = taskId;
        this.status = status;
        this.summary = summary;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}