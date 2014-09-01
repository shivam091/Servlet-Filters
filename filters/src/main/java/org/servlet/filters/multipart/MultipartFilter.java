package org.servlet.filters.multipart;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * <code>This filter handles multipart/form-data POST requests, the type of
 * request that can contain file uploads.</code>
 * </p>
 * 
 * <p>
 * <code>Each multipart/form-data POST request contains any number of parameters and files, 
 * using a special format not natively understood by servlets.
 *  Servlet developers have historically used third-party classes to handle the uploads, 
 *  such as the MultipartRequest and MultipartParser classes found in my own com.oreilly.servlet package. 
 *  Here we see a new approach using a MultipartFilter to make the handling of such requests easier. 
 *  The filter builds on the parsers in the com.oreilly.servlet package and has been integrated into the package.</code>
 * </p>
 * 
 * <p>
 * <code>
 * The MultipartFilter works by watching incoming requests and when it detects a file upload request (with the content type multipart/form-data), 
 * the filter wraps the request object with a special request wrapper that knows how to parse the special content type format. 
 * A servlet receiving the special request wrapper has seamless access to the multipart parameters through the standard getParameter() methods, 
 * because the wrapper has redefined the behavior of those methods. 
 * The servlet can also handle uploaded files by casting the request to the wrapper type and using the additional getFile() methods on the wrapper.</code>
 * </p>
 * 
 * @author shivam
 *
 */
public class MultipartFilter implements Filter {

	@SuppressWarnings("unused")
	private FilterConfig config = null;
	private String dir = null;

	/**
	 * <code>
	 * The init() method determines the file upload directory. 
	 * This is the location where the multipart parser places files, 
	 * so that the entire request doesn't have to reside in memory. 
	 * It looks first for an uploadDir filter init parameter, and failing to find that, 
	 * defaults to the tempdir directory.</code>
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;

		// Determine the upload directory. First look for an uploadDir filter
		// init parameter. Then look for the context tempdir.
		dir = config.getInitParameter("uploadDir");
		if (dir == null) {
			File tempdir = (File) config.getServletContext().getAttribute(
					"javax.servlet.context.tempdir");
			if (tempdir != null) {
				dir = tempdir.toString();
			} else {
				throw new ServletException(
						"MultipartFilter: No upload directory found: set an uploadDir "
								+ "init parameter or ensure the javax.servlet.context.tempdir "
								+ "directory is valid");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		config = null;
	}

	/**
	 * <code>The doFilter() method examines the request content type, 
	 * and should it be a multipart/form-data request, wraps the request with a MultipartWrapper.</code>
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String type = req.getHeader("Content-Type");

		// If this is not a multipart/form-data request continue
		if (type == null || !type.startsWith("multipart/form-data")) {
			chain.doFilter(request, response);
		} else {
			MultipartWrapper multi = new MultipartWrapper(req, dir);
			chain.doFilter(multi, response);
		}
	}
}