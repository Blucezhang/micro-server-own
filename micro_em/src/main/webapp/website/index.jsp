<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action="/info/sms" method="post">
		<input type="hidden" name="_method" value="put"/> 
		<input type="text" value="18600262193" />
		<input type="text" value="这是短信内容" />
		<input type="submit" value="发送" />
	</form>
</body>
</html>