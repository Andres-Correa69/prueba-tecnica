package develope.auth.infrastructure.adapter.in.rest.dto;

import java.util.UUID;

public record RegisterResponse(UUID userId, String username) {}
