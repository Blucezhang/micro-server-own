var app = angular.module('unifyModuleAudit', ['ngResource','Config','sessionIdModule','filterModuleAudit']);


//审核信息
app.service('busAuditSvc',function($resource,BusAuditUrl,getSessionId){
	var actions = {
			unifydo:{method:'GET', params: {urlPath:"@urlPath",parm:"@parm"}},
			unifydoAll:{method:'POST', params: {urlPath:"@urlPath",parm:"@parm"}},
			put:{method:'PUT', params: {urlPath:"@urlPath",parm:"@parm"}},
			post:{method:'POST', params: {urlPath:"@urlPath",parm:"@parm"}},
			del:{method:'DELETE', params: {urlPath:"@urlPath",parm:"@parm"}}
	};
	return $resource(BusAuditUrl+"/:urlPath", {gusi:getSessionId}, actions);	
});

/*已发布信息*/
app.service('published', function($resource,PublishedUrl,getSessionId) {
	var actions = {
			unifydo:{method:'GET', params: {urlPath:"@urlPath",parm:"@parm"}},
			getEquNames:{method:'GET', params: {urlPath:"@urlPath",parm:"@parm"},isArray:true},
			unifydoHM:{method:'POST', params: {urlPath:"@urlPath",parm:"@parm"}},
			addViewCount:{method:'GET', params: {urlPath:"@urlPath",parm:"@parm"}}
  	};
 	return $resource(PublishedUrl+"/:urlPath"+"/:parm", {gusi:getSessionId}, actions);	
});

//系统用户
app.service('sysUserSvc',function($resource,sysUserUrl,getSessionId){
	var actions = {
			loginUser:{method:'GET',params:{}},//用户登录
			loginOut:{method:'GET',params:{}}//用户登出
	};
	return $resource(sysUserUrl, {gusi:getSessionId}, actions);	
});