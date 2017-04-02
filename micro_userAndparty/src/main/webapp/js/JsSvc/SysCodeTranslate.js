/**
 * 1：根据系统代码的ID和类型将对应的代码翻译为对应的名称，用于在页面详情以及列表中使用——不需要改变
 */
var app=angular.module("sysCodeTranslateModule", ['ngResource','sysCodeConfigModule']);

app.factory('sysCodeTranslateFactory',function(SYS_CODE_CON) {
	return{
		//系统代码翻译
		codeTranslate:function(code,type){
			if(type)
			{
				var sysCodeCon=SYS_CODE_CON,typeList=sysCodeCon[type],i=0;
				if(typeList)
				{
					for(;i<typeList.length;i++)
					{
						if(code==typeList[i].id_){return typeList[i].name_ }
					}
				}
			}
			return code;
		}
	};
    
});