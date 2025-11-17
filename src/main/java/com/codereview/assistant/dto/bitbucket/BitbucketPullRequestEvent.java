package com.codereview.assistant.dto.bitbucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Bitbucket Pull Request Webhook Event
 * https://support.atlassian.com/bitbucket-cloud/docs/event-payloads/
 */
@Data
public class BitbucketPullRequestEvent {

    @JsonProperty("pullrequest")
    private BitbucketPullRequest pullRequest;

    private BitbucketRepository repository;
    private BitbucketActor actor;

    @Data
    public static class BitbucketPullRequest {
        private Long id;
        private String title;
        private String description;
        private String state; // OPEN, MERGED, DECLINED, SUPERSEDED

        @JsonProperty("author")
        private BitbucketActor author;

        @JsonProperty("source")
        private BitbucketBranch source;

        @JsonProperty("destination")
        private BitbucketBranch destination;

        @JsonProperty("created_on")
        private String createdOn;

        @JsonProperty("updated_on")
        private String updatedOn;

        @JsonProperty("merge_commit")
        private BitbucketCommit mergeCommit;

        private BitbucketLinks links;

        @JsonProperty("comment_count")
        private Integer commentCount;

        @JsonProperty("task_count")
        private Integer taskCount;

        @JsonProperty("close_source_branch")
        private Boolean closeSourceBranch;

        @JsonProperty("closed_by")
        private BitbucketActor closedBy;

        private String reason; // Reason for state change
    }

    @Data
    public static class BitbucketRepository {
        private String uuid;
        private String name;

        @JsonProperty("full_name")
        private String fullName;

        private String type;
        private Boolean isPrivate;
        private BitbucketActor owner;
        private String website;
        private BitbucketLinks links;

        @JsonProperty("created_on")
        private String createdOn;

        @JsonProperty("updated_on")
        private String updatedOn;

        @JsonProperty("size")
        private Long size;

        @JsonProperty("language")
        private String language;

        @JsonProperty("has_issues")
        private Boolean hasIssues;

        @JsonProperty("has_wiki")
        private Boolean hasWiki;

        @JsonProperty("fork_policy")
        private String forkPolicy;

        private BitbucketProject project;

        @JsonProperty("mainbranch")
        private BitbucketMainBranch mainBranch;
    }

    @Data
    public static class BitbucketActor {
        private String uuid;

        @JsonProperty("display_name")
        private String displayName;

        @JsonProperty("account_id")
        private String accountId;

        private String nickname;
        private String type;
        private BitbucketLinks links;
    }

    @Data
    public static class BitbucketBranch {
        private BitbucketRepository repository;
        private BitbucketCommit commit;

        @JsonProperty("branch")
        private BitbucketBranchInfo branch;
    }

    @Data
    public static class BitbucketBranchInfo {
        private String name;
    }

    @Data
    public static class BitbucketCommit {
        private String hash;
        private String type;
        private BitbucketLinks links;

        @JsonProperty("author")
        private BitbucketCommitAuthor author;

        private String message;
        private String date;

        @JsonProperty("parents")
        private List<BitbucketCommit> parents;
    }

    @Data
    public static class BitbucketCommitAuthor {
        private String raw;
        private BitbucketActor user;
    }

    @Data
    public static class BitbucketLinks {
        private BitbucketLink self;
        private BitbucketLink html;
        private BitbucketLink diff;
        private BitbucketLink commits;
        private BitbucketLink comments;
        private BitbucketLink activity;
        private BitbucketLink approve;
        private BitbucketLink statuses;
    }

    @Data
    public static class BitbucketLink {
        private String href;
    }

    @Data
    public static class BitbucketProject {
        private String uuid;
        private String key;
        private String name;
        private String type;
        private BitbucketLinks links;
    }

    @Data
    public static class BitbucketMainBranch {
        private String name;
        private String type;
    }
}
