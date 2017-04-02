app.controller('categoryController', function($scope,category) {
	
	$scope.testA=function(){
		function succ(rec){
			$scope.entityList=rec.result;
		};
		function err(rec){};
		category.query({level:1,categoryId:"1,2,3,4,5"},succ,err);
	};

	$scope.orgChild=[];
	
	$scope.addOrgLi=function(parm){
		var judge=true;
		for(var i=0;i<$scope.orgChild.length;i++){
			var tmpObj=$scope.orgChild[i];
			if(tmpObj.id==parm.id){
				judge=false;
				break;
			}
		}
		if(judge){
			$scope.orgChild.push(parm)
		}
	};
	
	$scope.deleteOrgLi=function(){
		$scope.orgChild.splice($scope.orgChild.length-1,1)
		if($scope.orgChild.length>0){
			$scope.queryChildOrg($scope.orgChild[$scope.orgChild.length-1]);
		}else{
			$scope.queryOrg();
			$scope.orgChild=[];
		}
	};
	
	$scope.queryOrg=function(){
		function succ(rec){
			$scope.entityList=rec.result;
		};
		function err(rec){};
		category.query({},succ,err);
	};
	
	$scope.queryChildOrg=function(obj){
		$scope.addOrgLi(obj);
		function succ(rec){
			$scope.entityList=rec.result;
		};
		function err(rec){};
		category.query({parm:obj.id,Action:"All"},succ,err);
	};
	
	$scope.addModal=function(){
		$scope.orgData={};
		if($scope.orgChild.length){
			$scope.orgData.parOrgId=$scope.orgChild[$scope.orgChild.length-1].id
			$scope.orgData.parOrgName=$scope.orgChild[$scope.orgChild.length-1].name
		}
		$('#addCategoryId').modal('show');
	};
	
	$scope.closeAdd=function(){
		$('#addCategoryId').modal('hide');
	};
	
	$scope.add=function(){
		
		if(!$scope.orgData && !$scope.orgData.name){
			$.messager.popup("请填写机构名称.");
			return;
		}
		
		function succ(rec){
			$('#addCategoryId').modal('hide');
			$.messager.popup("添加成功.");
			$scope.queryOrg();
			$scope.orgChild=[];
		};
		function err(rec){};
		category.add($scope.orgData,succ,err);
	};
	
	$scope.updModal=function(obj){
		
		function succ(rec){
			$scope.orgData=rec.result;
			if($scope.orgChild.length){
				$scope.orgData.parOrgId=$scope.orgChild[$scope.orgChild.length-1].id
				$scope.orgData.parOrgName=$scope.orgChild[$scope.orgChild.length-1].name
			}
		};
		function err(rec){};
		category.query({parm:obj.id,Action:"Single"},succ,err);
		$('#updCategoryId').modal('show');
	};
	
	$scope.closeUpd=function(){
		$('#updCategoryId').modal('hide');
	};
	
	$scope.upd=function(){
		function succ(rec){
			$('#updCategoryId').modal('hide');
			$scope.queryOrg();
			$scope.orgChild=[];
		};
		function err(rec){};
		category.upd({parm:$scope.orgData.id},$scope.orgData,succ,err);
	};
	
	$scope.deleteData=function(obj){
		$.messager.confirm("提示", "是否删除!", function(){
			function succ(rec){
				$.messager.popup("删除成功.");
				$scope.queryOrg();
				$scope.orgChild=[];
			};
			function err(rec){};
			category.del({parm:obj.id},succ,err);
		});
	};
	
});


