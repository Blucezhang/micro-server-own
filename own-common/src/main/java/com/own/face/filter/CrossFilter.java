package com.own.face.filter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class CrossFilter implements Filter {

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.info("初始化拦截器.....");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        httpRes.setHeader("Access-Control-Allow-Origin", "*");
        httpRes.setHeader("Access-Control-Allow-Methods", "DELETE, GET, POST, PUT");
        httpRes.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept,X-Requested-With");
        httpRes.setHeader("Access-Control-Max-Age", "43200");
        chain.doFilter(req, res);
	}
	@Override
	public void destroy() {
		log.info("摧毁拦截器.....");
	}

}
