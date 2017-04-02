app.controller('personController', function($scope,person,org) {

	$scope.queryAllOrg=function(){
		function succ(rec){
			$scope.allOrgList=rec.result;
		};
		function err(rec){};
		org.query({Action:"All"},succ,err);
	};
	$scope.queryAllOrg();
	
	$scope.queryOrg=function(){
		function succ(rec){
			$scope.orgList=rec.result;
		};
		function err(rec){};
		org.query({},succ,err);
	};
	
	$scope.queryChildOrg=function(obj){
		$scope.addOrgLi(obj);
		function succ(rec){
			$scope.orgList=rec.result;
		};
		function err(rec){};
		org.query({parm:obj.id,Action:"All"},succ,err);
	};
	
	$scope.chooseOrg=function(obj){
		$scope.personData.orgId=obj.id;
		$scope.personData.orgName=obj.name;
		$('#queryOrgId').modal('hide');
	};
	
	$scope.queryPerson=function(){
		function succ(rec){
			$scope.entityList=rec.result;
		};
		function err(rec){};
		person.query({},succ,err);
	};
	
	$scope.addModal=function(){
		$scope.personData={};
		$('#addPersonId').modal('show');
	};
	
	$scope.closeAdd=function(){
		$('#addPersonId').modal('hide');
	};
	
	$scope.add=function(){
		
		if(!$scope.personData && !$scope.personData.name){
			$.messager.popup("请填写员工姓名.");
			return;
		}
		
		function succ(rec){
			$.messager.popup("添加成功.");
			$('#addPersonId').modal('hide');
			$scope.queryPerson();
		};
		function err(rec){};
		person.add($scope.personData,succ,err);
	};
	
	$scope.updModal=function(obj){
		function succ(rec){
			console.log(rec);
			$scope.personData=rec.result;
		};
		function err(rec){};
		person.query({parm:obj.id},succ,err);
		$('#updPersonId').modal('show');
	};
	
	$scope.closeUpd=function(){
		$('#updPersonId').modal('hide');
	};
	
	$scope.upd=function(){
		function succ(rec){
			$('#updPersonId').modal('hide');
			$scope.queryPerson();
		};
		function err(rec){};
		person.upd({parm:$scope.personData.id},$scope.personData,succ,err);
	};
	
	$scope.deleteData=function(obj){
		$.messager.confirm("提示", "是否删除!", function(){
			function succ(rec){
				$.messager.popup("删除成功.");
				$scope.queryPerson();
			};
			function err(rec){};
			person.del({parm:obj.id},succ,err);
		});
	};
	
	$scope.openOrgModal=function(){
		$('#queryOrgId').modal('show');
	}
	
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
	
	$scope.orgChild=[];
	
	$scope.deleteOrgLi=function(){
		$scope.orgChild.splice($scope.orgChild.length-1,1)
		if($scope.orgChild.length>0){
			$scope.queryChildOrg($scope.orgChild[$scope.orgChild.length-1]);
		}else{
			$scope.queryOrg();
			$scope.orgChild=[];
		}
	};
	
	
});


