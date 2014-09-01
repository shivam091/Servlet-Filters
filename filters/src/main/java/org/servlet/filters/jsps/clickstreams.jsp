<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.servlet.filters.clickstream.Clickstream"%>
<%@ page info="This JSP provides a visitor summary page." %>
<%
	Map<?, ?> clickstreams = (Map<?, ?>) application.getAttribute("clickstreams");
	String showbots = "false";

	if (request.getParameter("showbots") != null) {
		if (request.getParameter("showbots").equals("true"))
			showbots = "true";
		else if (request.getParameter("showbots").equals("both"))
			showbots = "both";
	}
%>

<font face="Verdana" size="-1">
	<h1>All Clickstreams</h1> 
	<a href="clickstreams.jsp?showbots=false">No
		Bots</a> | <a href="clickstreams.jsp?showbots=true">All Bots</a> | <a
	href="clickstreams.jsp?showbots=both">Both</a>
	<p>
		<%
			if (clickstreams.keySet().size() == 0) {
		%>
		No clickstreams in progress
		<%
			}
		%>
		<%
			Iterator<?> it = clickstreams.keySet().iterator();
			int count = 0;
			while (it.hasNext()) {
				String key = (String) it.next();
				Clickstream stream = (Clickstream) clickstreams.get(key);

				if (showbots.equals("false") && stream.isBot()) {
					continue;
				} else if (showbots.equals("true") && !stream.isBot()) {
					continue;
				}

				count++;
				try {
		%>
		<%=count%>. <a href="viewstream.jsp?sid=<%=key%>"><b><%=(stream.getHostname() != null
							&& !stream.getHostname().equals("") ? stream
							.getHostname() : "Stream")%></b></a>
		<font size="-1">[<%=stream.getStream().size()%> reqs]
		</font><br>

		<%
			} catch (Exception e) {
		%>
		An error occurred -
		<%=e%><br>
		<%
	}
}
%>
</p>
</font>