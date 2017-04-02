/**
 * 1：模块声明部分——不需要改变
 */
var app = angular.module('sysCodeConfigModule',[]);
var sysCodeCon={},sysCode;
/**
 * 2：配置自己的代码的部分，需要自己配置对应的内容以及存入对应的变量中，常量仅用于翻译信息，请勿改变，另外，命名采用全部大写多个单词使用下划线分割的形式，请自行注释一下
 */


//动力种类 (资源管理-阴悦)
sysCode =[];
sysCode=[{"id_":"1","name_":"汽油发动机"},{"id_":"2","name_":"柴油发动机"},{"id_":"3","name_":"交流电动机"}];
sysCodeCon.POWER_TYPE = sysCode;

//技术状况(资源管理-阴悦)
sysCode =[];
sysCode=[{"id_":"1","name_":"一类"},{"id_":"2","name_":"二类"},{"id_":"3","name_":"三类"}];
sysCodeCon.TECHNOLOGY_STATE = sysCode;

//设备来源分类(资源管理-阴悦)
sysCode =[];
sysCode=[{"id_":"1","name_":"自有"},{"id_":"2","name_":"外租"},{"id_":"3","name_":"外协"}];
sysCodeCon.SOURCE_TYPE = sysCode;   

//设备状态(资源管理-阴悦)
sysCode =[];
sysCode=[{"id_":"1","name_":"闲置"},{"id_":"2","name_":"使用中"},{"id_":"3","name_":"已出售"},{"id_":"4","name_":"已报废"}];
sysCodeCon.EQU_STATE= sysCode; 

//业务状态(资源管理-阴悦)
sysCode =[];
sysCode=[{"id_":"1","name_":"自用"},{"id_":"2","name_":"调拨"},{"id_":"3","name_":"局内租"},{"id_":"4","name_":"外局租"},{"id_":"5","name_":"外租"}];
sysCodeCon.WORK_STATE= sysCode; 

//发布状态(资源管理-阴悦)
sysCode =[];
sysCode=[{"id_":"1","name_":"未发布"},{"id_":"2","name_":"出租发布中"},{"id_":"3","name_":"出售发布中"}];
sysCodeCon.RELEASE_STATE= sysCode;  

//租赁方式(资源管理-阴悦）
sysCode =[];
sysCode=[{"id_":"1","name_":"月租"},{"id_":"2","name_":"日租"},{"id_":"3","name_":"工作量"}];
sysCodeCon.RENT_TYPE= sysCode;

//结算方式(资源管理-阴悦）
sysCode =[];
sysCode=[{"id_":"1","name_":"转账"},{"id_":"2","name_":"验工计价"},{"id_":"3","name_":"台班签认"},{"id_":"4","name_":"按月结算"}];
sysCodeCon.COLSE_TYPE= sysCode;

//组织级别-所有(企业管理-阴悦）
sysCode =[];
sysCode=[{"id_":"1","name_":"总公司"},{"id_":"2","name_":"局级"},{"id_":"3","name_":"处级"}];
sysCodeCon.LEVEL_ALL= sysCode;

//状态(项目设置-阴悦）
sysCode =[];
sysCode=[{"id_":"0","name_":"正常"},{"id_":"2","name_":"停用"}];
sysCodeCon.PRO_STATE= sysCode;

//组织级别-总公司(企业管理-阴悦）
sysCode =[];
sysCode=[{"id_":"2","name_":"局级"}];
sysCodeCon.ENTERPRISE_LEVEL_ALLCompany= sysCode;

//组织级别-局级(企业管理-阴悦）
sysCode =[];
sysCode=[{"id_":"3","name_":"处级"}];
sysCodeCon.ENTERPRISE_LEVEL_COMPANY= sysCode;

//组织级别-展示用(企业管理-阴悦）
sysCode =[];
sysCode=[{"id_":"2","name_":"局级"},{"id_":"3","name_":"处级"}];
sysCodeCon.ENTERPRISE_LEVEL_ALL= sysCode;

//状态(员工信息维护-阴悦）
sysCode =[];
sysCode=[{"id_":"0","name_":"正常"},{"id_":"1","name_":"停用"}];
sysCodeCon.PERSON_STATE= sysCode;

//信息类型(审核查询-阴悦）
sysCode =[];
sysCode=[{"id_":"1","name_":"出租"},{"id_":"2","name_":"出售"},{"id_":"3","name_":"求租"},{"id_":"4","name_":"求购"}];
sysCodeCon.MESSAGE_TYPE= sysCode;

//审核状态(审核查询-阴悦）
sysCode =[];
sysCode=[{"id_":"1","name_":"待审核"},{"id_":"3","name_":"审核通过"},{"id_":"4","name_":"审核不通过"}];
sysCodeCon.AUDIT_STATE= sysCode;

//租期单位(租赁费-阴悦）
sysCode =[];
sysCode=[{"id_":"1","name_":"月"},{"id_":"2","name_":"天"},{"id_":"3","name_":"台班"},{"id_":"4","name_":"小时"},];
sysCodeCon.TENANCY_UNIT= sysCode;

//单位范围
sysCode =[];
sysCode=[{"id_":"1","name_":"处内"},{"id_":"2","name_":"局内"},{"id_":"3","name_":"外局"},{"id_":"4","name_":"外单位"},];
sysCodeCon.UNIT_SCOPE = sysCode;

//设备来源  
sysCode =[];
sysCode=[{"id_":"1","name_":"自有"},{"id_":"2","name_":"外租"},{"id_":"3","name_":"外协"}];
sysCodeCon.EQUIPMENT_SOURCE = sysCode;

//设备来源  
sysCode =[];
sysCode=[{"id_":"1","name_":"自用"},{"id_":"5","name_":"外租"}];
sysCodeCon.EQUIPMENT_SOURCE_COPY = sysCode;

//设备状态（巩国才）  
sysCode =[];
sysCode=[{"id_":"1","name_":"已成交"},{"id_":"2","name_":"未成交"},{"id_":"","name_":"未成交"}];
sysCodeCon.VIEWINFO_EQU_STATE = sysCode;

//计量单位（阴悦）  
sysCode =[];
sysCode=[{"id_":"1","name_":"台"},{"id_":"2","name_":"辆"},{"id_":"3","name_":"套"}];
sysCodeCon.UNIT_NAME = sysCode;

//租金单价单位（阴悦）  
sysCode =[];
sysCode=[{"id_":"1","name_":"元/月"},{"id_":"2","name_":"元/天"},{"id_":"3","name_":"元/小时"}];
sysCodeCon.UNIT_RENTMONEY = sysCode;

//租期单位（阴悦）  
sysCode =[];
sysCode=[{"id_":"1","name_":"个月"},{"id_":"2","name_":"天"},{"id_":"3","name_":"年"}];
sysCodeCon.UNIT_RENTTIME = sysCode;

/**
 * 3：将配置的常量配置为模块的常量，以便在对应的模块中使用——不需改变
 */
app.constant('SYS_CODE_CON',sysCodeCon);