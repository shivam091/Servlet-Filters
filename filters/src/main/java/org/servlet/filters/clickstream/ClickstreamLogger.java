package org.servlet.filters.clickstream;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.servlet.filters.clickstream.Clickstream;

/**
 * <code>Class that captures session and context events to glue everything together.</code>
 * 
 * @author shivam
 *
 */
public class ClickstreamLogger implements ServletContextListener,
		HttpSessionListener {
	Map<String, Clickstream> clickstreams = new HashMap<String, Clickstream>();

	public ClickstreamLogger() {
		// System.out.println("ClickstreamLogger constructed");
	}

	public void contextInitialized(ServletContextEvent sce) {
		// System.out.println("ServletContext Initialised");

		sce.getServletContext().setAttribute("clickstreams", clickstreams);
	}

	public void contextDestroyed(ServletContextEvent sce) {
		sce.getServletContext().setAttribute("clickstreams", null);
		// System.out.println("ServletContext Destroyed");
	}

	public void sessionCreated(HttpSessionEvent hse) {
		// System.out.println("Session Created");
		HttpSession session = hse.getSession();

		Clickstream clickstream = new Clickstream();
		session.setAttribute("clickstream", clickstream);

		clickstreams.put(session.getId(), clickstream);
	}

	public void sessionDestroyed(HttpSessionEvent hse) {
		// System.out.println("Session Destroyed");

		HttpSession session = hse.getSession();

		Clickstream stream = (Clickstream) session.getAttribute("clickstream");

		clickstreams.remove(session.getId());

		System.out.println("Final session clickstream:\n" + stream);
	}
}