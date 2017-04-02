<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="js" tagdir="/WEB-INF/tags"%>
<link href="../../../media/css/emailstyle.css" rel="stylesheet"> 
<script type="text/javascript" src="/media/js/emailstylejs.js"></script>


<script type="text/javascript">

	$('#element_id  a').lightBox({maxWidth:500,maxHeight:500}); 
	
	$('#element_id2  a').lightBox({maxWidth:500,maxHeight:500}); 
    
</script>
<style type="text/css">
label {
	width: 100%;
}

#div2 {
	margin-left: -130px;
}

#div3 {
	margin-left: -280px;
}

</style>
<div class="modal fade" id="queryOrgId" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="width: 950px;">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">员工信息维护</h4>
			</div>
			<button type="button"  style="margin-left: 880px;margin-top:-75px;outline: none;" class="btn btn-link  "  data-dismiss="modal"><span  class="glyphicon glyphicon-remove " ></span></button>
			<div class="modal-body" style="text-align:left;">
				<ol class="breadcrumb" style="margin-left:28px;margin-bottom:-5px;background-color: #FFF;">
					组织机构位置：
					<li style="font-size: 13px" ng-repeat="org in orgChild"><a href="javascript:void(0)">{{org.name}}</a></li>
				</ol>
				<table class="table table-hover" style="margin-left: 28px;z-index:3;width:880px;" ng-init="queryOrg();">
					<tbody>
						<tr class="success">
							<th style="white-space: nowrap; text-align: center;"><span style="margin-left:-15x;">序号</span></th>
							<th style="white-space: nowrap; text-align: center;">机构名称</th>
							<th style="white-space: nowrap; text-align: center;">机构等级</th>
							<th style="white-space: nowrap; text-align: center;">操作</th>
						</tr>
					</tbody>
					<tbody>
						<tr ng-repeat="o in orgList" style="text-align:center;">
							<td>
								<p style="margin-top:5px;">{{$index+1}}</p>
							</td>
						<!-- 	<td style="white-space: nowrap; text-align: center;"><span style="margin-left:-75px;">{{$index+1+(paginationConf.currentPage-1)*paginationConf.itemsPerPage}}</span></td> -->
							<td><a href="javascript:void(0);" ng-click="queryChildOrg(o);">{{o.name}}</a></td>
							<td>{{o.level}}</td>
							<td>
								<button type="button" class="btn btn-primary" ng-click="chooseOrg(o);">选择</button>
							</td>
						</tr>
					</tbody>
				</table>
				<div style="margin-left: 45px;">
					<div>
						<button type="button" class="btn btn-primary" ng-click="deleteOrgLi()" ng-if="orgChild.length>0">返回上级</button>
					</div>
				</div> 
		   </div>			
		</div>
	</div>
</div>
