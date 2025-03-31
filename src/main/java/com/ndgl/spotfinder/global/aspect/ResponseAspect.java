package com.ndgl.spotfinder.global.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ResponseAspect {
	private final HttpServletResponse response;

	@Around("""
		(
		    within
		    (
		        @org.springframework.web.bind.annotation.RestController *
		    )
		    &&
		    (
		        @annotation(org.springframework.web.bind.annotation.GetMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.PostMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.PutMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.DeleteMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.RequestMapping)
		    )
		)
		||
		@annotation(org.springframework.web.bind.annotation.ResponseBody)
		||
		within(@org.springframework.web.bind.annotation.RestControllerAdvice *)
		""")
	public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
		Object proceed = joinPoint.proceed();

		if (proceed instanceof RsData<?> rsData) {
			response.setStatus(rsData.getCode());
		}

		return proceed;
	}
}