package com.codereview.assistant.controller;

import com.codereview.assistant.dto.ReviewRuleDto;
import com.codereview.assistant.service.ReviewRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Slf4j
public class ReviewRuleController {

    private final ReviewRuleService reviewRuleService;

    /**
     * 모든 규칙 조회
     */
    @GetMapping
    public ResponseEntity<List<ReviewRuleDto>> getAllRules(
            @RequestParam(required = false) Long repositoryId
    ) {
        log.info("GET /api/rules?repositoryId={}", repositoryId);
        List<ReviewRuleDto> rules = reviewRuleService.getAllRules(repositoryId);
        return ResponseEntity.ok(rules);
    }

    /**
     * 규칙 생성
     */
    @PostMapping
    public ResponseEntity<ReviewRuleDto> createRule(@RequestBody ReviewRuleDto ruleDto) {
        log.info("POST /api/rules - Creating rule: {}", ruleDto.getName());
        ReviewRuleDto created = reviewRuleService.createRule(ruleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 규칙 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewRuleDto> updateRule(
            @PathVariable Long id,
            @RequestBody ReviewRuleDto ruleDto
    ) {
        log.info("PUT /api/rules/{} - Updating rule", id);
        ReviewRuleDto updated = reviewRuleService.updateRule(id, ruleDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 규칙 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        log.info("DELETE /api/rules/{}", id);
        reviewRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
