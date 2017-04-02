<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
<jsp:include page="./include/Head.jsp" />
<jsp:include page="./common/common.jsp" />

<style type="text/css">

.container {width: 1500px !important;}

a:focus { 
outline: none; /* 去掉超链接外的虚线边框 */
} 
</style>
</head>
<body style="background-color: #fff">
<jsp:include page="./common/TopMenu.jsp" />
	<div class="container"  ng-app="indexApp" ng-controller="indexController"> <!-- style="background-color:#fff;width: 1580px;" -->
		<div class="row"> 
			<div style="background-color:#FFF;padding-left: 10px;padding-right: 10px;min-height: 600px;">
				<ng-view></ng-view>
			</div>
		</div>
	</div>
</body>
</html>