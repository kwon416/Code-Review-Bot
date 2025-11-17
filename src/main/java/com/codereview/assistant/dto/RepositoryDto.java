package com.codereview.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryDto {

    private Long id;

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private OwnerDto owner;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OwnerDto {
        private String login;
    }
}
