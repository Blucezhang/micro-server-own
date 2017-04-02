var app = angular.module('unifyModule', ['ngResource','Config']);

/*
 * 用户
 */
app.service('person', function($resource,personUrl) {
	var actions = {
			query:{method:'GET', params: {parm:"@parm"}},
			add:{method:'PUT', params: {}},
			upd:{method:'POST', params: {urlPath:"@urlPath",parm:"@parm"}},
			del:{method:'DELETE', params: {urlPath:"@urlPath",parm:"@parm"}}
  	};
 	return $resource(personUrl+"/:urlPath"+"/:parm", {}, actions);	
});

/*
 * 组织
 */
app.service('org', function($resource,orgUrl) {
	var actions = {
			query:{method:'GET', params: {parm:"@parm"}},
			add:{method:'PUT', params: {}},
			upd:{method:'POST', params: {urlPath:"@urlPath",parm:"@parm"}},
			del:{method:'DELETE', params: {urlPath:"@urlPath",parm:"@parm"}}
  	};
 	return $resource(orgUrl+"/:urlPath"+"/:parm", {}, actions);	
});

