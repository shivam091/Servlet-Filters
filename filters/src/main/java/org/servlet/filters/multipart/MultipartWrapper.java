package org.servlet.filters.multipart;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.oreilly.servlet.MultipartRequest;

/**
 * <code>The wrapper constructs a com.oreilly.servlet.MultipartRequest object to handle the upload parsing 
 * and overrides the getParameter() family of methods to use the MultipartRequest rather than the 
 * raw request to read parameter values. The wrapper also defines various getFile() methods 
 * so that a servlet receiving this wrapped request can call additional methods to handle the uploaded files.</code>
 * 
 * @author shivam
 *
 */
public class MultipartWrapper extends HttpServletRequestWrapper {

	MultipartRequest mreq = null;

	public MultipartWrapper(HttpServletRequest req, String dir)
			throws IOException {
		super(req);
		mreq = new MultipartRequest(req, dir);
	}

	// Methods to replace HSR methods
	public Enumeration<?> getParameterNames() {
		return mreq.getParameterNames();
	}

	public String getParameter(String name) {
		return mreq.getParameter(name);
	}

	public String[] getParameterValues(String name) {
		return mreq.getParameterValues(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getParameterMap() {
		Map map = new HashMap();
		Enumeration enume = getParameterNames();
		while (enume.hasMoreElements()) {
			String name = (String) enume.nextElement();
			map.put(name, mreq.getParameterValues(name));
		}
		return map;
	}

	// Methods only in MultipartRequest
	public Enumeration<?> getFileNames() {
		return mreq.getFileNames();
	}

	public String getFilesystemName(String name) {
		return mreq.getFilesystemName(name);
	}

	public String getContentType(String name) {
		return mreq.getContentType(name);
	}

	public File getFile(String name) {
		return mreq.getFile(name);
	}
}
