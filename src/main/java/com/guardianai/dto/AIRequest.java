package com.guardianai.dto;

import jakarta.validation.constraints.NotBlank;

public class AIRequest {

    @NotBlank(message = "Situation description is required")
    private String situation;

    public AIRequest() {
    }

    public AIRequest(String situation) {
        this.situation = situation;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }
}
