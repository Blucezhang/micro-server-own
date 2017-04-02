<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<script type="text/javascript">
var app = angular.module('indexApp',['ngResource','ngRoute','unifyModule']);

app.controller('indexController',function($scope){});

app.config(['$routeProvider',function($routeProvider){
	$routeProvider.when('/person', {
		templateUrl: '/website/party/person/person.jsp',//用户设置
		controller:'personController'
	}).when('/org', {
		templateUrl: '/website/party/org/org.jsp',//组织机构设置
		controller:'orgController'
	}).when('/category', {
		templateUrl: '/website/product/category/category.jsp',//产品-类别
		controller:'categoryController'
	});
}]);  
</script>
<script type="text/javascript" src="/website/party/person/person.js"></script>
<script type="text/javascript" src="/website/party/org/org.js"></script>
<script type="text/javascript" src="/website/product/category/category.js"></script>
