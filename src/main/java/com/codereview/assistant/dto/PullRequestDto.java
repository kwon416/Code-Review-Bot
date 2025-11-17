package com.codereview.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestDto {

    private Long id;

    private Integer number;

    private String title;

    private String body;

    private String state;

    private UserDto user;

    private HeadDto head;

    @JsonProperty("diff_url")
    private String diffUrl;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDto {
        private String login;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeadDto {
        private String sha;
    }
}
