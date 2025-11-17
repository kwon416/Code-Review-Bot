package com.codereview.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWebhookPayload {

    private String action;

    @JsonProperty("pull_request")
    private PullRequestDto pullRequest;

    private RepositoryDto repository;

    private InstallationDto installation;
}
