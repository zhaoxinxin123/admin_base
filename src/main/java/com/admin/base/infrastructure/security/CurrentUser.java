package com.admin.base.infrastructure.security;

import java.util.List;

public record CurrentUser(String username, Long adminId, List<String> authorities) {
}
