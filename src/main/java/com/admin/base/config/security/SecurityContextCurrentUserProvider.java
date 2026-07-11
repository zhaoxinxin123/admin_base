package com.admin.base.config.security;

import com.admin.base.constant.ResponseCode;
import com.admin.base.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResponseCode.CODE_NO_LOGIN, "请先登录");
        }
        Long adminId = authentication.getDetails() instanceof UserDetailsImpl userDetails
                ? userDetails.getAdminId()
                : null;
        var authorities = authentication.getAuthorities().stream()
                .map(Object::toString)
                .toList();
        return new CurrentUser(authentication.getName(), adminId, authorities);
    }
}
