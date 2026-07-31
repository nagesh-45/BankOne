package com.bankone.audit.service;

import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.dto.AuditEventResponse;
import com.bankone.audit.entity.AuditEventEntity;
import com.bankone.audit.repository.AuditEventRepository;
import com.bankone.auth.security.BankUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Best-effort write: never fails the calling business transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            AuditCategory category,
            String action,
            String targetType,
            String targetId,
            String summary,
            String details,
            boolean success
    ) {
        try {
            Actor actor = currentActor();
            AuditEventEntity event = new AuditEventEntity();
            event.setCategory(category);
            event.setAction(action);
            event.setActorUsername(actor.username());
            event.setActorUserId(actor.userId());
            event.setTargetType(targetType);
            event.setTargetId(targetId);
            event.setSummary(trim(summary, 500));
            event.setDetails(trim(details, 2000));
            event.setSuccess(success);
            auditEventRepository.save(event);
        } catch (Exception ex) {
            log.warn("Failed to write audit event {} / {}: {}", category, action, ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordForUser(
            AuditCategory category,
            String action,
            String actorUsername,
            Long actorUserId,
            String targetType,
            String targetId,
            String summary,
            String details,
            boolean success
    ) {
        try {
            AuditEventEntity event = new AuditEventEntity();
            event.setCategory(category);
            event.setAction(action);
            event.setActorUsername(actorUsername);
            event.setActorUserId(actorUserId);
            event.setTargetType(targetType);
            event.setTargetId(targetId);
            event.setSummary(trim(summary, 500));
            event.setDetails(trim(details, 2000));
            event.setSuccess(success);
            auditEventRepository.save(event);
        } catch (Exception ex) {
            log.warn("Failed to write audit event {} / {}: {}", category, action, ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> search(
            AuditCategory category,
            String action,
            String actor,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return auditEventRepository.search(
                category,
                blankToNull(action),
                blankToNull(actor),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(this::toResponse);
    }

    private AuditEventResponse toResponse(AuditEventEntity e) {
        AuditEventResponse r = new AuditEventResponse();
        r.setId(e.getId());
        r.setCategory(e.getCategory());
        r.setAction(e.getAction());
        r.setActorUsername(e.getActorUsername());
        r.setActorUserId(e.getActorUserId());
        r.setTargetType(e.getTargetType());
        r.setTargetId(e.getTargetId());
        r.setSummary(e.getSummary());
        r.setDetails(e.getDetails());
        r.setSuccess(e.isSuccess());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }

    private Actor currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return new Actor(null, null);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof BankUserDetails details) {
            return new Actor(details.getUsername(), details.getId());
        }
        String name = auth.getName();
        if (name == null || "anonymousUser".equals(name)) {
            return new Actor(null, null);
        }
        return new Actor(name, null);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private record Actor(String username, Long userId) {
    }
}
