package org.saipal.srms.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			if (modelAndView.getViewName() != null) {
				if (!modelAndView.getViewName().contains("redirect:")) {
					String conPath = request.getContextPath();
					int port = request.getServerPort();
					String prt = (port == 80 || port == 443) ? "" : ":" + port;
					String baseUrl = request.getScheme() + "://" + request.getServerName() + prt + conPath;
					modelAndView.addObject("baseUrl", baseUrl);
				}
			}
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		response.setCharacterEncoding("utf-8");
	}
}