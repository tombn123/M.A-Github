<%@ page import="game.constants.Constants" %>
<%@ page import="game.utils.SessionUtils" %><%--
  Created by IntelliJ IDEA.
  User: noam
  Date: 25/09/2018
  Time: 18:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Logout</title>
</head>
<body>

<div class="container">
    <% Object usernameLogout = request.getAttribute(Constants.USER_NAME_LOGOUT);%>
    <% if (usernameLogout != null) {%>
    <h3><%=usernameLogout%> logged out</h3>
    <a href="../index.html">Back to home page</a>
    <br/>
    <% } %>
</div>
</body>
</html>
