package com.firstone.greenjangteo.aop;

import com.firstone.greenjangteo.user.dto.request.UserIdRequestDto;
import com.firstone.greenjangteo.utility.MemoryUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import static com.firstone.greenjangteo.aop.LogConstant.PERFORMANCE_MEASUREMENT;

@Component
@Aspect
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // TokenService의 첫 번째 인수는 리프레시 토큰 값이므로 로그 대상에서 제외한다
    private static final String STRING_VALUE_POINTCUT
            = "execution(* com.firstone.greenjangteo..service.*.*(..)) && args(stringValue, ..)"
            + " && !within(com.firstone.greenjangteo.user.domain.token.service.TokenService)";

    private static final String LONG_VALUE_POINTCUT
            = "execution(* com.firstone.greenjangteo..service.*.*(..)) && args(longValue, ..)";

    private static final String START
            = "Beginning to '{}.{}' task by {}: '{}'";
    private static final String END
            = "'{}.{}' task was executed successfully by {}: '{}', ";

    private static final String NO_VALUE_POINTCUT
            = "execution(* com.firstone.greenjangteo..service.*.*())";
    private static final String NO_VALUE_START
            = "Beginning to '{}.{}' task";
    private static final String NO_VALUE_END
            = "'{}.{}' task was executed successfully, ";

    private static final String USER_ID_REQUEST_DTO_POINTCUT
            = "execution(* com.firstone.greenjangteo..service.*.*(..)) && args(userIdRequestDto)";
    private static final String USER_ID_REQUEST_DTO_START
            = "Beginning to '{}.{}' task by userId: '{}', ";
    private static final String USER_ID_REQUEST_DTO_END
            = "'{}.{}' task was executed successfully by userId: '{}', ";

    private static final String PAGEABLE_AND_ID_POINTCUT
            = "execution(* com.firstone.greenjangteo..service.*.*(..)) && args(pageable, longValue)";
    private static final String PAGEABLE_AND_ID_START
            = "Beginning to '{}.{}' task by page: '{}', {}: '{}'";
    private static final String PAGEABLE_AND_ID_END
            = "'{}.{}' task was executed successfully by page: '{}', {}: '{}', ";

    @Around(STRING_VALUE_POINTCUT)
    public Object logAroundForStringValue(ProceedingJoinPoint joinPoint, String stringValue) throws Throwable {
        String parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames()[0];

        log.info(START,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, stringValue);

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info(END + PERFORMANCE_MEASUREMENT,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, stringValue,
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;
    }

    @Around(LONG_VALUE_POINTCUT)
    public Object logAroundForLongValue(ProceedingJoinPoint joinPoint, Long longValue) throws Throwable {
        String parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames()[0];

        log.info(START,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, longValue);

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info(END + PERFORMANCE_MEASUREMENT,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, longValue,
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;
    }

    @Around(NO_VALUE_POINTCUT)
    public Object logAroundForNoValue(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info(NO_VALUE_START,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName());

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info(NO_VALUE_END + PERFORMANCE_MEASUREMENT,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;
    }

    @Around(USER_ID_REQUEST_DTO_POINTCUT)
    public Object logAroundForUserIdRequestDto(ProceedingJoinPoint joinPoint,
                                               UserIdRequestDto userIdRequestDto) throws Throwable {
        log.info(USER_ID_REQUEST_DTO_START,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), userIdRequestDto.getUserId());

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info(USER_ID_REQUEST_DTO_END + PERFORMANCE_MEASUREMENT,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), userIdRequestDto.getUserId(),
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;
    }

    @Around(PAGEABLE_AND_ID_POINTCUT)
    public Object logAroundForPageableAndId(ProceedingJoinPoint joinPoint, Pageable pageable, Long longValue)
            throws Throwable {
        String parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames()[1];

        log.info(PAGEABLE_AND_ID_START,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), pageable.getPageNumber(), parameterName, longValue);

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info(PAGEABLE_AND_ID_END + PERFORMANCE_MEASUREMENT,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), pageable.getPageNumber(), parameterName, longValue,
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;
    }
}
