package com.codereview.assistant.controller;

import com.codereview.assistant.dto.ReviewRuleDto;
import com.codereview.assistant.service.ReviewRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review Rules", description = "커스텀 리뷰 규칙 관리 API")
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Slf4j
public class ReviewRuleController {

    private final ReviewRuleService reviewRuleService;

    @Operation(
        summary = "규칙 목록 조회",
        description = "모든 리뷰 규칙을 조회합니다. Repository ID로 필터링할 수 있습니다."
    )
    @GetMapping
    public ResponseEntity<List<ReviewRuleDto>> getAllRules(
            @Parameter(description = "Repository ID (선택사항)", example = "1")
            @RequestParam(required = false) Long repositoryId
    ) {
        log.info("GET /api/rules?repositoryId={}", repositoryId);
        List<ReviewRuleDto> rules = reviewRuleService.getAllRules(repositoryId);
        return ResponseEntity.ok(rules);
    }

    @Operation(
        summary = "새 규칙 생성",
        description = "새로운 리뷰 규칙을 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ReviewRuleDto> createRule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "생성할 규칙 정보"
            )
            @RequestBody ReviewRuleDto ruleDto
    ) {
        log.info("POST /api/rules - Creating rule: {}", ruleDto.getName());
        ReviewRuleDto created = reviewRuleService.createRule(ruleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "규칙 수정",
        description = "기존 리뷰 규칙을 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ReviewRuleDto> updateRule(
            @Parameter(description = "규칙 ID", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "수정할 규칙 정보"
            )
            @RequestBody ReviewRuleDto ruleDto
    ) {
        log.info("PUT /api/rules/{} - Updating rule", id);
        ReviewRuleDto updated = reviewRuleService.updateRule(id, ruleDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "규칙 삭제",
        description = "기존 리뷰 규칙을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "규칙 ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /api/rules/{}", id);
        reviewRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
