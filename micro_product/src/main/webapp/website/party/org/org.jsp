<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
<title>员工信息维护</title>
<style>
.container {width: 1500px !important;}  

.form-horizontal .control-label {
padding-top: 7px;
margin-bottom: 0;
text-align: right;
min-width : 0px;
}

.abc {
	position: absolute;
	top: 100%;
	left: 0;
	z-index: 1000;
	float: left;
	min-width: 160px;
	padding: 5px 0;
	margin: 2px 0 0;
	font-size: 14px;
	text-align: left;
	list-style: none;
	background-color: #fff;
	-webkit-background-clip: padding-box;
	background-clip: padding-box;
	border: 1px solid #ccc;
	border: 1px solid rgba(0, 0, 0, .15);
	border-radius: 4px;
	-webkit-box-shadow: 0 6px 12px rgba(0, 0, 0, .175);
	box-shadow: 0 6px 12px rgba(0, 0, 0, .175)
}

::-ms-clear, ::-ms-reveal{display: none;}

.page-list .pagination {float:left;}
.page-list .pagination span {cursor: pointer;}
.page-list .pagination .separate span{cursor: default; border-top:none;border-bottom:none;}
.page-list .pagination .separate span:hover {background: none;}
.page-list .page-total {float:left; margin: 25px 20px;}
.page-list .page-total input, .page-list .page-total select{height: 26px; border: 1px solid #ddd;}
.page-list .page-total input {width: 40px; padding-left:3px;}
.page-list .page-total select {width: 50px;}

</style>

</head>
<body class="container" >
	<ol class="breadcrumb">
		<li style="font-size: 13px">您的位置：后台管理</li>
		<li style="font-size: 13px">系统管理</li>
		<li style="font-size: 13px">组织机构维护</li>
	</ol>
	<form action="" style="width: 95%">
		<div class="form-horizontal" style="margin-top: 10px;">
				<div class="form-group">
					<label contenteditable="false" class="col-xs-2 control-label">所属项目：</label>
					
					<div class="col-xs-4">
						<input type="hidden" ng-model="parentOrgPage">
				        <div class="form-group" >
				            <div class="col-xs-2">
					        	<select class="form-control select-hover" ng-model="employeeEntity.proId"  style="margin-top:15px;margin-left:-10px;width:200%;margin-top:-2px;position: absolute;z-index:3;"  >
				                    <option value="">全部</option>
				                    <option value="{{p.currOrgId}}" ng-repeat="p in proListCopy">{{p.name}}</option>
								</select>
							</div> 
					   </div>
					   <div class="col-xs-3" style="margin-left: 225px;margin-top:-18px;">
							<input type="button" id="searchBtnId" class="btn btn-primary" value="查询" />
					   </div>
				    </div> 
			</div>
			<ol class="breadcrumb" style="margin-left:28px;margin-bottom:-5px;background-color: #FFF;">
				组织机构位置：
				<li style="font-size: 13px" ng-repeat="org in orgChild"><a href="javascript:void(0)">{{org.name}}</a></li>
			</ol>
			<table class="table table-hover" style="margin-left: 28px;z-index:3" ng-init="queryOrg();">
				<tbody>
					<tr class="success">
						<th style="white-space: nowrap; text-align: center;"><span style="margin-left:-15x;">序号</span></th>
						<th style="white-space: nowrap; text-align: center;">机构名称</th>
						<th style="white-space: nowrap; text-align: center;">机构等级</th>
						<th style="white-space: nowrap; text-align: center;">操作</th>
					</tr>
				</tbody>
				<tbody>
					<tr ng-repeat="entity in entityList" style="text-align: center;" ng-click="check(entity,$index+1);">
						<td>
							<input style="margin-left:-15px;margin-top:12px;" type="radio" name="onlyOne" ng-click="chooseEmployee(entity.partyId,entity.state)" ng-checked="radioTrIndex==$index+1">
							<p style="margin-left:13px;margin-top:-21px;">{{$index+1}}</p>
						</td>
					<!-- 	<td style="white-space: nowrap; text-align: center;"><span style="margin-left:-75px;">{{$index+1+(paginationConf.currentPage-1)*paginationConf.itemsPerPage}}</span></td> -->
						<td><a href="javascript:void(0);" ng-click="queryChildOrg(entity);">{{entity.name}}</a></td>
						<td>{{entity.level}}</td>
						<td>
							<button type="button" class="btn btn-primary" ng-click="updModal(entity);">修改</button>
							<button type="button" class="btn btn-primary" ng-click="deleteData(entity)">删除</button>
						</td>
					</tr>
				</tbody>
			</table>
	 		<div style="margin-left: 45px;">
				<div  style="text-align: right; float: right;height: 1px" ng-show="btnShow_">
					<tm-pagination conf="paginationConf" style="margin-left:500px;" ></tm-pagination>
				</div>
				<label></label>
				<div>
					<button type="button" class="btn btn-primary" ng-click="deleteOrgLi()" ng-if="orgChild.length>0">返回上级</button>
					<button type="button" class="btn btn-primary" ng-click="addModal()">添加</button>
				</div>
			</div> 
		</div>
	</form>
	
	<div ng-include src="'/website/party/org/addorg.jsp'"></div>
	<div ng-include src="'/website/party/org/updorg.jsp'"></div> 
	
</body>