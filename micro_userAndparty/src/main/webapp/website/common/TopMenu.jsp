<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<style type="text/css">
	
    .dropdown-submenu {
        position: relative;
    }
    .dropdown-submenu > .dropdown-menu {
        top: 0;
        left: 100%;
        margin-top: -6px;
        margin-left: -1px;
        -webkit-border-radius: 0 6px 6px 6px;
        -moz-border-radius: 0 6px 6px;
        border-radius: 0 6px 6px 6px;
    }
    .dropdown-submenu:hover > .dropdown-menu {
        display: block;
    }
    .dropdown-submenu > a:after {
        display: block;
        content: " ";
        float: right;
        width: 0;
        height: 0;
        border-color: transparent;
        border-style: solid;
        border-width: 5px 0 5px 5px;
        border-left-color: #ccc;
        margin-top: 5px;
        margin-right: -10px;
    }
    .dropdown-submenu:hover > a:after {
        border-left-color: #fff;
    }
    .dropdown-submenu.pull-left {
        float: none;
    }
    .dropdown-submenu.pull-left > .dropdown-menu {
        left: -100%;
        margin-left: 10px;
        -webkit-border-radius: 6px 0 6px 6px;
        -moz-border-radius: 6px 0 6px 6px;
        border-radius: 6px 0 6px 6px;
    }
    .dropdown-submenu1 {
        position: relative;
        float: left;
    }
	.dropdown-submenu1:hover > .dropdown-menu {
        display: block; /* 鼠标移到下拉标题自动展开 */
    }
    
    .line-hr{
    	color:"#999999";
    	width:150px;
    	margin:0px;
    }
    
    @media ( min-width :0px) {
	.navbar-nav {
		float: left;
		margin: 0
	}
	.navbar-nav>li {
		float: left
	}
	.navbar-nav>li>a {
		padding-top: 15px;
		padding-bottom: 15px
	}
	.navbar-nav.navbar-right:last-child {
		margin-right: -15px
	}
}
@media ( min-width :768px) {
	.navbar>.container .navbar-brand,.navbar>.container-fluid .navbar-brand
		{
		margin-left: 0px;
	}
}
 
</style>
<script type="text/javascript">
	$(function () { $("[data-toggle='tooltip']").tooltip();});
</script>


<nav  class="navbar navbar-default " role="navigation">
	<!-- 当鼠标浮动到菜单上时，让所有控件失去焦点，临时隐藏域 -->
	<input type="text" id="blurInputId" style="width:0px;height:0px;border:0px;">
    <div class="navbar-header container" >              <!-- data-toggle="tooltip" title的样式 -->
      <a class="navbar-brand " href="../../../WebSite/Front/Main/HomePage.jsp" data-toggle="tooltip" data-placement="bottom" title="返回首页">
      	<span class="glyphicon glyphicon-circle-arrow-left"></span>
      	后台管理&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      </a>
      <ul id="ROOT_MENU" class="nav navbar-nav "><!-- 整个导航栏 -->
      
		<li class="dropdown-submenu1 ">
			<a href="javascript:void();" class="dropdown-toggle" data-toggle="dropdown">系统管理<b class="caret"></b></a>
			<ul class="dropdown-toggle nav navbar-nav dropdown-menu">
			 	<li><a  href="/website/index.jsp#org">组织机构设置</a></li>
			 	<li><hr class="line-hr"></li>
			 	<li><a  href="/website/index.jsp#person">用户设置</a></li>
			</ul>
		</li>
		
		<li class="dropdown-submenu1 ">
			<a href="javascript:void();" class="dropdown-toggle" data-toggle="dropdown">测试<b class="caret"></b></a>
			<ul class="dropdown-toggle nav navbar-nav dropdown-menu">
			 	<li><a  href="/website/index.jsp#person">测试</a></li>
			</ul>                                                         
		</li>
      </ul>
      <p  class=" navbar-right" style="margin-right:50px;margin-top:15px;">您好，<span id="spanId">${sessionScope.userInfo.loginId}</span>&nbsp;&nbsp;|&nbsp;&nbsp;<button id="logoutBtnId" onClick="logoutFun();" class="btn btn-link" style="color:blue">退出</button></p> 
     </div>
</nav>