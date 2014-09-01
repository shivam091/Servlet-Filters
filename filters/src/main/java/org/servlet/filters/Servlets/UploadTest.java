package org.servlet.filters.Servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.servlet.filters.multipart.MultipartWrapper;

public class UploadTest extends HttpServlet {

	private static final long serialVersionUID = 6709604202006782774L;

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();

		out.println("<HTML>");
		out.println("<HEAD><TITLE>UploadTest</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<H1>UploadTest</H1>");

		// Parameters can now be read the same way for both
		// application/x-www-form-urlencoded and multipart/form-data requests!

		out.println("<H3>Request Parameters:</H3><PRE>");
		Enumeration<?> enume = req.getParameterNames();
		while (enume.hasMoreElements()) {
			String name = (String) enume.nextElement();
			String values[] = req.getParameterValues(name);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					out.println(name + " (" + i + "): " + values[i]);
				}
			}
		}
		out.println("</PRE>");

		// Files can be read if the request class is MultipartWrapper
		// Init params to MultipartWrapper control the upload handling

		if (req instanceof MultipartWrapper) {
			try {
				// Cast the request to a MultipartWrapper
				MultipartWrapper multi = (MultipartWrapper) req;

				// Show which files we received
				out.println("<H3>Files:</H3>");
				out.println("<PRE>");
				Enumeration<?> files = multi.getFileNames();
				while (files.hasMoreElements()) {
					String name = (String) files.nextElement();
					String filename = multi.getFilesystemName(name);
					String type = multi.getContentType(name);
					File f = multi.getFile(name);
					out.println("name: " + name);
					out.println("filename: " + filename);
					out.println("type: " + type);
					if (f != null) {
						out.println("length: " + f.length());
					}
					out.println();
				}
				out.println("</PRE>");
			} catch (Exception e) {
				out.println("<PRE>");
				e.printStackTrace(out);
				out.println("</PRE>");
			}
		}

		out.println("</BODY></HTML>");
	}
}
