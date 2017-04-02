/**
 * gusi=globally user session id （全局用户会话ID）
 */

//声明并实例化cookie工厂对象
var cf=new cookiesFactory();
//声明user_session_id的模块
var app=angular.module('sessionIdModule', ['ngResource']);
//定义产生user_session_id的服务
app.provider('createGUID',{
	$get: function() {
		function S4() {
			return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
		}
		return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
    }
});
//定义获取user_session_id的工厂
app.factory("getSessionId",function(createGUID){
	
	var sessionId=cf.getCookies("gusi");
	
/*	var userInfo=cf.getCookies("userInfo");*/
/*	console.log("sessionId is : ",sessionId,"userInfo",userInfo);*/
	
	if(sessionId==null){
		cf.createCookies("gusi",createGUID);
		return createGUID;
	}else{
		return sessionId;
	}
});

