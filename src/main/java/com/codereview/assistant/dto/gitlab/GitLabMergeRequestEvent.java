package com.codereview.assistant.dto.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitLab Merge Request Webhook Event
 * https://docs.gitlab.com/ee/user/project/integrations/webhook_events.html#merge-request-events
 */
@Data
public class GitLabMergeRequestEvent {

    @JsonProperty("object_kind")
    private String objectKind; // "merge_request"

    private String eventType;

    private GitLabUser user;

    private GitLabProject project;

    @JsonProperty("object_attributes")
    private GitLabMergeRequest objectAttributes;

    private GitLabRepository repository;

    @Data
    public static class GitLabUser {
        private Long id;
        private String name;
        private String username;
        private String email;

        @JsonProperty("avatar_url")
        private String avatarUrl;
    }

    @Data
    public static class GitLabProject {
        private Long id;
        private String name;
        private String description;

        @JsonProperty("web_url")
        private String webUrl;

        @JsonProperty("git_ssh_url")
        private String gitSshUrl;

        @JsonProperty("git_http_url")
        private String gitHttpUrl;

        private String namespace;
        private String visibility;

        @JsonProperty("path_with_namespace")
        private String pathWithNamespace;

        @JsonProperty("default_branch")
        private String defaultBranch;

        @JsonProperty("homepage")
        private String homepage;

        @JsonProperty("url")
        private String url;

        @JsonProperty("ssh_url")
        private String sshUrl;

        @JsonProperty("http_url")
        private String httpUrl;
    }

    @Data
    public static class GitLabMergeRequest {
        private Long id;
        private Long iid; // GitLab internal ID

        @JsonProperty("target_branch")
        private String targetBranch;

        @JsonProperty("source_branch")
        private String sourceBranch;

        @JsonProperty("source_project_id")
        private Long sourceProjectId;

        @JsonProperty("target_project_id")
        private Long targetProjectId;

        private String title;
        private String description;
        private String state; // opened, closed, merged, locked

        @JsonProperty("merge_status")
        private String mergeStatus; // can_be_merged, cannot_be_merged, unchecked

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("last_commit")
        private GitLabCommit lastCommit;

        @JsonProperty("work_in_progress")
        private Boolean workInProgress;

        @JsonProperty("url")
        private String url;

        @JsonProperty("action")
        private String action; // open, close, reopen, update, approved, unapproved, approval, unapproval, merge

        @JsonProperty("author_id")
        private Long authorId;

        @JsonProperty("assignee_id")
        private Long assigneeId;
    }

    @Data
    public static class GitLabCommit {
        private String id;
        private String message;
        private String timestamp;
        private String url;
        private GitLabAuthor author;
    }

    @Data
    public static class GitLabAuthor {
        private String name;
        private String email;
    }

    @Data
    public static class GitLabRepository {
        private String name;
        private String url;
        private String description;
        private String homepage;

        @JsonProperty("git_http_url")
        private String gitHttpUrl;

        @JsonProperty("git_ssh_url")
        private String gitSshUrl;
    }
}
