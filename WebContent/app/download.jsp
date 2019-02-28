<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

	<c:if test="${requestScope.files != null }">
		<c:forEach items="${requestScope.files }" var="file">
			FileName: ${file.fileName } <br>
			Desc: ${file.fileDesc } <br>
			<a href="${pageContext.request.contextPath }/downloadServlet?id=${file.id }">下载</a>
			<hr>
		</c:forEach>
	</c:if>

</body>
</html>