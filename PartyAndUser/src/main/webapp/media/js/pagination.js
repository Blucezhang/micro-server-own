/**
 * 分页标签
 */
var module=angular.module("myPagination",[]);
module.directive('paginationTag',function($timeout){
	return{
		
		/*restrict的取值范围:
         *E - 表示创建的是一个HTML标签: <custom-tag></custom-tag>
         *A - 为HTML标签创建属性: <div my-directive="exp"> </div>
         *C - 为HTML标签创建类: <div class="my-directive: exp;"></div>
         *M - 创建HTML注释: <!-- directive: my-directive exp -->
        */
	    restrict:"EA",
	    
	    /*replace为true时模块则覆盖标签，比如模块是:<div></div>，
         *则<customTag></customTag>最终解释为<div></div>
        */
        replace:true,
        
        /*模块，即把<paginationTag>映射成最终什么样的HTML嗲吗*/
        template:'<ul class="pagination" id="currentPageUlId">'+
	        	   '<li title="首页" ng-click="firstPage();"><a href="#">&laquo;&laquo;</a></li>'+
	        	   '<li title="上一页" ng-class="{disabled: conf.currentPage == 1}" ng-click="lastPage();"><a href="#">&laquo;</a></li>'+
	        	   '<li class="disabled" ng-show="conf.isFirst"><a href="">...</a></li>'+
	        	   '<li ng-repeat="item in pageList" ng-click="currentPage(item)" ng-class="{active:item == conf.currentPage}"><a href="">{{item}}</a></li>'+
	        	   '<li class="disabled" ng-show="conf.isEnd"><a href="">...</a></li>'+
	        	   '<li title="下一页" ng-class="{disabled: conf.currentPage == conf.pages}" ng-click="nextPage();"><a href="#">&raquo;</a></li>'+
	        	   '<li title="尾页" ng-click="endPage();"><a href="#">&raquo;&raquo;</a></li>'+
	        	 '</ul>',
	      
	        	 
	        	 /*   template:
	              	  '<select>'+
	              	      '<option value="">10</option>'+
		              	  '<option value="">15</option>'+
		              	  '<option value="">10</option>'+
		              	  '<option value="">10</option>'+
		              	  '<option value="">10</option>'+
	              	  '</select>',*/
	    
	    /*配置参数*/
    	scope: {
            conf: '='
        },
        
	    link: function(scope, element, attrs){
	    	var tempTotalItems='-1';
	    	/*监听数据总数变化*/
	    	scope.$watch(function(){
	    		if(scope.conf.totalItems!=tempTotalItems){
	    			tempTotalItems=scope.conf.totalItems;
	    			initFun();
	    		}
            });
	    	/*初始化*/
	    	function initFun(){
	    		scope.conf.queryList(scope.conf.currentPage);
	    		scope.conf.isFirst=false;
	    		scope.conf.isEnd=true;
	    		/*如果没有设置分页标签数量显示，那么默认为10*/
	    		if(!scope.conf.pageNum){
	    			scope.conf.pageNum=10;
	    		}
    			countPageList(scope.conf.currentPage);
	    	};
	    	
	    	/*计算页数数组*/
	    	function countPageList(parm){
	    		if(!scope.conf.isEnd){
	    			return;
	    		}
	    		/*页数数组*/
	    		scope.pageList = [];
	    		/*计算页数*/
	    		scope.conf.pages = Math.ceil(scope.conf.totalItems/scope.conf.pageRecord);
	    		if(scope.conf.pages>scope.conf.pageNum && (scope.conf.pages-scope.conf.currentPage)<scope.conf.pageNum){
	    			scope.conf.isFirst=true;
	    			scope.conf.isEnd=false;
	    			for(var i=1;i<=scope.conf.pageNum;i++){
	    				scope.pageList.push(scope.conf.pages-scope.conf.pageNum+i);
	    			};
	    		}else{
	    			var k=1;
		    		for(var i=parm;i<=scope.conf.pages;i++){
		    			if(scope.pageList[0]!=1 && scope.pageList.length>0){
		    				scope.conf.isFirst=true;
		    			}
		    			if(k++<=scope.conf.pageNum){
		    				if(i==scope.conf.pages){
		    					scope.conf.isEnd=false;
		    				}
	    					scope.pageList.push(i);
	    				}else{
	    					break;
	    				}
		    		}
	    		}
	    	};
	    	
	    	/*点击页数*/
	    	scope.currentPage = function(parm){
	    		scope.conf.currentPage=parm;
	    		if(parm>scope.conf.pageNum && scope.conf.pageNum<(scope.conf.pages-parm)){
	    			countPageList(parm);
	    		}
	    		scope.conf.queryList(parm,scope.conf.pageNum);
	    	};
	    	/*上一页*/
	    	scope.lastPage = function(){
	    		if(scope.conf.currentPage > 1){
                    scope.conf.currentPage -= 1;
                    if((scope.conf.pages-scope.conf.currentPage)>=scope.conf.pageNum){
                    	scope.conf.isEnd=true;
                    }
                    countPageList(scope.conf.currentPage);
                }
	    		if(scope.conf.currentPage == 1){
	    			scope.conf.isFirst=false;
                }
	    		scope.conf.queryList(scope.conf.currentPage,scope.conf.pageNum);
	    	};
	    	/*下一页*/
	    	scope.nextPage = function(){
	    		if(scope.conf.currentPage < scope.conf.pages){
                    scope.conf.currentPage += 1;
                    if(scope.conf.currentPage>scope.conf.pageNum){
                    	countPageList(scope.conf.currentPage);
    	    		}
                }
	    		scope.conf.queryList(scope.conf.currentPage,scope.conf.pageNum);
	    	};
	    	/*首页*/
	    	scope.firstPage = function(){
	    		scope.conf.isFirst=false;
	    		if(scope.conf.pages>scope.conf.pageNum){
	    			scope.conf.isEnd=true;
	    		}
	    		scope.conf.currentPage = 1;
	    		countPageList(scope.conf.currentPage);
	    		scope.conf.queryList(1,scope.conf.pageNum);
	    	};
	    	/*尾页*/
	    	scope.endPage = function(){
	    		scope.countEndPageList();
	    	};
	    	
	    	/*计算尾页页数数组*/
	    	scope.countEndPageList = function(){
	    		if(scope.conf.pages>scope.conf.pageNum){
	    			scope.conf.isFirst=true;
	    		}
				scope.conf.isEnd=false;
	    		scope.conf.currentPage=scope.conf.pages;
	    		if(scope.conf.pages>scope.conf.pageNum){
	    			scope.pageList=[];
	    			for(var i=1;i<=scope.conf.pageNum;i++){
	    				scope.pageList.push(scope.conf.pages-scope.conf.pageNum+i);
	    			};
	    		}
	    		scope.conf.queryList(scope.conf.pages,scope.conf.pageNum);
	    	};
	    	
	    }
	};
});