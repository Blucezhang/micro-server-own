/**
 * 定义拦截器模块，拦截前端的请求，然后获取cookie看其中是否有用户的信息，如果没有则认为此用户没有登录，跳转到登录页面
 */
//声明并实例化cookie工厂对象
var cf=new cookiesFactory();
var app=angular.module("filterModule", ['ngResource']);
//定义拦截器工厂
app.factory('userInterceptor',function($q) {
    var userInterceptor = {
    		'request':function(config){
    			/*console.log("request...",config);*/
/*    			var url=config.url;
    			var notFilterUrl=["/Issue","/Category","/Main/Top.jsp","/Main/Bottom.jsp","/DemandRent","/DemandSale","/Rent","/Login/Login.jsp","/Sys/User","/Sale","/Equipment","/Picture","/Publish/ViewInfo.jsp","/Party/Region"];
    			var userInfo=cf.getCookies("userInfo");
    			if(userInfo==null || userInfo=="")
    			{
    				var beFilter=true;
        			for(var i=0;i<notFilterUrl.length;i++)
        			{
        				if(url.lastIndexOf(notFilterUrl[i])!=-1)
        				{
        					beFilter=false;
        					break;
        				}
        			}
        			if(beFilter)
        			{
        				 $.messager.popup("用户会话过期，请重新登录！");
        				alert("用户未登录或会话过期，请重新登录！");
        				console.info("$.messager is : ",$.messager);
        				
        				$.messager.alert("用户未登录或会话过期，请重新登录！");
        			    alert("用户未登录或会话过期，请重新登录！");
        				window.location.href="/WebSite/Front/Login/Login.jsp";
        			}
    			}*/
    			return config;
    		},
    		'requestError':function(config){
    			/*console.log("requestErr...",config);*/
    			return config;
    		},
    		'response':function(response){
    			/*console.log("response...",response,response.status );*/
    			return response;
    		},
    		'responseError':function(responseErr){
    			console.log("responseErr...",responseErr,responseErr.status );
     		   if(responseErr.status=='500' && responseErr.data!=null && responseErr.data.error=='530' )
	   			{
	   			    /*$.messager.alert("用户未登录或会话过期，请重新登录！");*/
	   			    alert("用户未登录或会话过期，请重新登录！");
	   				window.location.href="/WebSite/Front/Login/Login.jsp";
	   			}
     		   
/*    			if(responseErr.status=='0')
    			{
    				$.messager.popup("后端服务未开启，请联系系统管理员！"); //消失的速度过快
    				alert("后端服务未开启，请联系系统管理员！");//不好看
    				$.messager.alert("后端服务未开启，请联系系统管理员！");
    				window.location.href="/WebSite/Front/Login/Login.jsp";
    			}
    		   
    		   if(responseErr.status=='500' && responseErr.data!=null && responseErr.data.error=='501' )
	   			{
	   			    $.messager.alert("用户未登录或会话过期，请重新登录！");
	   			    alert("你无权操作此功能，请申请对应的功能权限，谢谢配合！");
	   				window.location.href="/WebFront/WebSite/Front/Login/Login.jsp";
	   			}  */  		   
    			return $q.reject(responseErr);
    		}
    };
    return userInterceptor;
});
//将自定义拦截器放入拦截器栈
app.config(function($httpProvider) {
    $httpProvider.interceptors.push('userInterceptor');
});
