package com.admin.base.infrastructure.aop;

import com.admin.base.infrastructure.aop.annotation.Log;
import com.admin.base.infrastructure.security.UserDetailsImpl;
import com.admin.base.shared.constant.ResponseCode;
import com.admin.base.system.log.domain.OperationLog;
import com.admin.base.infrastructure.async.AsyncManager;
import com.admin.base.infrastructure.async.factory.AsyncFactory;
import com.admin.base.shared.util.RequestUtils;
import com.admin.base.shared.util.SecurityUtils;
import com.admin.base.shared.util.ServletUtils;
import com.admin.base.shared.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/22 9:54 下午
 * @desc 操作日志记录 — 已替换 Gson 为 Jackson
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "oldPassword",
            "newPassword",
            "token",
            "authorization",
            "code",
            "uuid",
            "file"
    );

    String sanitizeLogPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return payload;
        }
        String sanitized = payload;
        for (String key : SENSITIVE_KEYS) {
            sanitized = sanitized.replaceAll("(?i)(\"" + key + "\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
        }
        return sanitized;
    }

    // 配置织入点
    @Pointcut("@annotation(com.admin.base.infrastructure.aop.annotation.Log)")
    public void logPointCut() {
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        try {
            // 获得注解
            Log controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }

            // 获取当前的用户
            UserDetailsImpl loginUser = SecurityUtils.getLoginUser();

            // *========数据库日志=========*//
            OperationLog operationLog = new OperationLog();
            operationLog.setStatus(ResponseCode.CODE_OK);
            // 请求的地址

            String ip = RequestUtils.getCurrentRequest().getRemoteAddr();
            operationLog.setOperationIp(ip);
            // 返回参数 — 使用 Jackson 序列化
            try {
                operationLog.setJsonResult(objectMapper.writeValueAsString(jsonResult));
            } catch (JsonProcessingException ex) {
                operationLog.setJsonResult(String.valueOf(jsonResult));
            }

            operationLog.setOperationUrl(ServletUtils.getRequest().getRequestURI());
            if (loginUser != null) {
                operationLog.setOperationName(loginUser.getUsername());
            }

            if (e != null) {
                operationLog.setStatus(ResponseCode.CODE_SYS_ERROR);
                operationLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operationLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operationLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            operationLog.setOperationTime(LocalDateTime.now());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operationLog);
            // 保存数据库
            AsyncManager.me().execute(AsyncFactory.recordOperation(operationLog));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log          日志
     * @param operationLog 操作日志
     * @throws Exception 异常
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, OperationLog operationLog) throws Exception {
        // 设置action动作
        operationLog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        operationLog.setTitle(log.title());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operationLog);
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operationLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, OperationLog operationLog) throws Exception {
        String requestMethod = operationLog.getRequestMethod();
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs());
            operationLog.setOperationParam(sanitizeLogPayload(StringUtils.substring(params, 0, 2000)));
        } else {
            Map<?, ?> paramsMap = (Map<?, ?>) ServletUtils.getRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            operationLog.setOperationParam(sanitizeLogPayload(StringUtils.substring(paramsMap.toString(), 0, 2000)));
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private Log getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(Log.class);
        }
        return null;
    }

    /**
     * 参数拼装 — 使用 Jackson 替代 Gson
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        params.append(objectMapper.writeValueAsString(o)).append(" ");
                    } catch (JsonProcessingException e) {
                        params.append(o.toString()).append(" ");
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}