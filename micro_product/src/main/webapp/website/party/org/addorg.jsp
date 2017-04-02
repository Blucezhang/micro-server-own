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
<div class="modal fade" id="addOrgId" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="width: 950px;">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">组织机构维护</h4>
			</div>
			<button type="button"  style="margin-left: 880px;margin-top:-75px;outline: none;" class="btn btn-link  "  data-dismiss="modal"><span  class="glyphicon glyphicon-remove " ></span></button>
			<div class="modal-body" style="text-align: center;">
				<form name="emForm" action="" method="post" id="form" enctype="application/x-www-form-urlencoded">
					<div class="form-horizontal" style="margin-top: 10px;">
						<ul>
						
							<div class="form-group">
								<label contenteditable="false" class="col-xs-2 control-label">上级机构名称：</label>
								<div class="col-xs-4" style="margin-top:5px;">
									{{orgData.parOrgName}}
								</div>
							</div>
							
							<div class="form-group">
								<label contenteditable="false" class="col-xs-2 control-label">机构名称：</label>
								<div class="col-xs-4">
									<input type="text" class="form-control"  ng-model="orgData.name" >
								</div>
							</div>
							
							<div class="form-group">
								<label contenteditable="false" class="col-xs-2 control-label">机构级别：</label>
								<div class="col-xs-4">
									<input type="text" class="form-control"  ng-model="orgData.level" >
								</div>
							</div>
							
						</ul>
					</div>
					<input class="btn btn-primary" style="margin-left: 20px;" type="button" value="提交" ng-click="add();">
					<input class="btn btn-primary" style="margin-left: 20px;" type="button" value="返回" ng-click="closeAdd();">
			 </form>
		   </div>			
		</div>
	</div>
</div>
