app.controller('orgController', function($scope,org) {

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
		org.query({},succ,err);
	};
	
	$scope.queryChildOrg=function(obj){
		$scope.addOrgLi(obj);
		function succ(rec){
			$scope.entityList=rec.result;
		};
		function err(rec){};
		org.query({parm:obj.id,Action:"All"},succ,err);
	};
	
	$scope.addModal=function(){
		$scope.orgData={};
		if($scope.orgChild.length){
			$scope.orgData.parOrgId=$scope.orgChild[$scope.orgChild.length-1].id
			$scope.orgData.parOrgName=$scope.orgChild[$scope.orgChild.length-1].name
		}
		$('#addOrgId').modal('show');
	};
	
	$scope.closeAdd=function(){
		$('#addOrgId').modal('hide');
	};
	
	$scope.add=function(){
		
		if(!$scope.orgData && !$scope.orgData.name){
			$.messager.popup("请填写机构名称.");
			return;
		}
		
		function succ(rec){
			$('#addOrgId').modal('hide');
			$.messager.popup("添加成功.");
			$scope.queryOrg();
			$scope.orgChild=[];
		};
		function err(rec){};
		org.add($scope.orgData,succ,err);
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
		org.query({parm:obj.id,Action:"Single"},succ,err);
		$('#updOrgId').modal('show');
	};
	
	$scope.closeUpd=function(){
		$('#updOrgId').modal('hide');
	};
	
	$scope.upd=function(){
		function succ(rec){
			$('#updOrgId').modal('hide');
			$scope.queryOrg();
			$scope.orgChild=[];
		};
		function err(rec){};
		org.upd({parm:$scope.orgData.id},$scope.orgData,succ,err);
	};
	
	$scope.deleteData=function(obj){
		$.messager.confirm("提示", "是否删除!", function(){
			function succ(rec){
				$.messager.popup("删除成功.");
				$scope.queryOrg();
				$scope.orgChild=[];
			};
			function err(rec){};
			org.del({parm:obj.id},succ,err);
		});
	};
	
});


