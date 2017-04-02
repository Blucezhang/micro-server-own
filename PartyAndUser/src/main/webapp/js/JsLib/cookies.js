/**
 * 维护cookie
 * 使用方法:
 * var cf=new window.cookiesFactory();    
 * cf.createCookies(?,?,?,?);常量变量时间路径 存
 * cf.getCookies(?);取 key
 * cf.deleteCookies(?);删
 */
(function (root,doc) {
	
	root.cookiesFactory=function(){
		this.cookieDefaultPath="/";/*默认服务器根目录*/
		this.cookieRemoveTime=24*60*60*1000;/*移除cookie 需要1天的毫秒数*/ 
	};
	
	/**
	 * 创建cookie
	 * @param name cookie的key
	 * @param value cookie的value
	 * @param removeTime cookie的移除时间（天数）
	 * @param path cookie存放在服务器的路径
	 */
	cookiesFactory.prototype.createCookies=function(name,value,removeTime,path){
		if(typeof value=='object'){
			value=JSON.stringify(value);
		}
		var tmpCookie=name+"="+escape(value);
		if(removeTime!=null){
			var currentTime = new Date();
			currentTime.setTime(currentTime.getTime() + removeTime*this.cookieRemoveTime);
			tmpCookie+=";expires=" + currentTime.toGMTString();
		}/*else{
			//如果不设置时间，默认过期时间1个月(30天)
			var currentTime = new Date();
			currentTime.setTime(currentTime.getTime() + 30*this.cookieRemoveTime);
			tmpCookie+=";expires=" + currentTime.toGMTString();
		}*/
		if(path!=null){
			tmpCookie+=";path="+path;
		}else{
			tmpCookie+=";path="+this.cookieDefaultPath;
		}
		doc.cookie=tmpCookie;
	};
	
	/**
	 * 查询cookie
	 * @param name cookie的key
	 * @returns
	 */
	cookiesFactory.prototype.getCookies=function(name){
		var cookies=doc.cookie;
		/*console.info("cookies is : ",cookies,"name is : ",name);*/
		if(cookies.indexOf(name)>=0){
			var arr=cookies.split(";");
			for(var i=0;i<arr.length;i++){
				var tmpArr=arr[i].split("=");
				var nm=tmpArr[0];
				var val=tmpArr[1];
				if(nm.indexOf(name)>=0){
					var returnVal=unescape(val);
					try{
						return eval("("+returnVal+")");
					}catch(e){
						return unescape(val);
					}
				}
			}
		}else{
			return null;
		}
	};
	
	/**
	 * 移除cookie
	 * @param name cookie的key
	 * @param path cookie存放在服务器的路径
	 */
	cookiesFactory.prototype.deleteCookies=function(name,path){
		this.createCookies(name, null, -1, path);
	};
	
	
	
})(this,document);




