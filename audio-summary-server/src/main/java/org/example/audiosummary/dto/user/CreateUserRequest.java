package org.example.audiosummary.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email has invalid format")
    private String email;

    public CreateUserRequest(){
    }

    public CreateUserRequest(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }
}
