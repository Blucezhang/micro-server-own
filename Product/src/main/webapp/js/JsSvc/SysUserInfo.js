/**
 * 将登录的系统信息缓存到前端内存中，目前暂时未用（将对应的登录用户的信息，缓存到页面的公共变量中了）
 */
var app = angular.module('sysUserInfoModule',['ngResource']);

app.factory('sysUserInfoFactory',function() {
	//存放系统用户信息的临时变量
	var sysUserInfoTemp={};
	return{
		//设置系统用户的信息
		setSysUserInfo:function(sysUserInfo)
		{
			sysUserInfoTemp=sysUserInfo;
			return sysUserInfoTemp;
		},
		//获取系统用户的常量
		getSysUserInfo:function()
		{
			return sysUserInfoTemp;
		}
	};
    
});