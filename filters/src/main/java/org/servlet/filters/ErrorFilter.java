package org.servlet.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Alternative to Spring Boot's ErrorPageFilter which was causing problems
 * with other filters, because it was trying to send errors after a response was committed.
 *
 * @author shivam
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorFilter implements Filter {

	private static final String ERROR_MESSAGE = "javax.servlet.error.message";
	private static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";

	@Override
	public void init(FilterConfig fc) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			ErrorWrapperResponse wrapped = new ErrorWrapperResponse((HttpServletResponse) response);
			try {
				chain.doFilter(request, wrapped);
				int status = wrapped.getStatus();
				if (status >= 400) {
					setErrorAttributes(request, status, wrapped.getMessage());
				}
			} catch (Throwable ex) {
				rethrow(ex);
			}
			response.flushBuffer();
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

	private void setErrorAttributes(ServletRequest request, int status, String message) {
		request.setAttribute(ERROR_STATUS_CODE, status);
		request.setAttribute(ERROR_MESSAGE, message);
	}

	private void rethrow(Throwable ex) throws IOException, ServletException {
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		if (ex instanceof Error) {
			throw (Error) ex;
		}
		if (ex instanceof IOException) {
			throw (IOException) ex;
		}
		if (ex instanceof ServletException) {
			throw (ServletException) ex;
		}
		throw new IllegalStateException(ex);
	}

	private static class ErrorWrapperResponse extends HttpServletResponseWrapper {

		private int status;
		private String message;

		public ErrorWrapperResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendError(int status) throws IOException {
			sendError(status, null);
		}

		@Override
		public void sendError(int status, String message) throws IOException {
			this.status = status;
			this.message = message;
		}

		@Override
		public int getStatus() {
			return this.status;
		}

		public String getMessage() {
			return this.message;
		}
	}
}
