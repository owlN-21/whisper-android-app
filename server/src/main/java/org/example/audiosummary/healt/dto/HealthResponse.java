package org.example.audiosummary.healt.dto;

public class HealthResponse {

  private String status;
  private String service;

  public HealthResponse(String status, String service) {
    this.status = status;
    this.service = service;
  }

  public String getStatus() {
    return status;
  }

  public String getService() {
    return service;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setService(String service) {
    this.service = service;
  }
}
