/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50624
Source Host           : localhost:3306
Source Database       : micro

Target Server Type    : MYSQL
Target Server Version : 50624
File Encoding         : 65001

Date: 2018-05-02 16:59:58
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `acc_accstate`
-- ----------------------------
DROP TABLE IF EXISTS `acc_accstate`;
CREATE TABLE `acc_accstate` (
  `AccStateId` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDay` date DEFAULT NULL,
  `startDay` date DEFAULT NULL,
  `stateTime` datetime DEFAULT NULL,
  `StateType` bigint(20) NOT NULL,
  `AcctId` bigint(20) NOT NULL,
  PRIMARY KEY (`AccStateId`),
  KEY `FKksevyieae4q8v6du9gy1pky8q` (`StateType`),
  CONSTRAINT `FKksevyieae4q8v6du9gy1pky8q` FOREIGN KEY (`StateType`) REFERENCES `acc_statetype` (`stateType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_accstate
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_acctmedia`
-- ----------------------------
DROP TABLE IF EXISTS `acc_acctmedia`;
CREATE TABLE `acc_acctmedia` (
  `mediaId` bigint(20) NOT NULL AUTO_INCREMENT,
  `accRoleId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`mediaId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_acctmedia
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_acctype`
-- ----------------------------
DROP TABLE IF EXISTS `acc_acctype`;
CREATE TABLE `acc_acctype` (
  `AccTypeId` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`AccTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_acctype
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_mediasate`
-- ----------------------------
DROP TABLE IF EXISTS `acc_mediasate`;
CREATE TABLE `acc_mediasate` (
  `mediaState` bigint(20) NOT NULL,
  `mediaId` bigint(20) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mediaState`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_mediasate
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_relation`
-- ----------------------------
DROP TABLE IF EXISTS `acc_relation`;
CREATE TABLE `acc_relation` (
  `relId` bigint(20) NOT NULL AUTO_INCREMENT,
  `FirstAcct` bigint(20) DEFAULT NULL,
  `RelType` varchar(255) DEFAULT NULL,
  `SecAcct` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`relId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_relation
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_relationtype`
-- ----------------------------
DROP TABLE IF EXISTS `acc_relationtype`;
CREATE TABLE `acc_relationtype` (
  `relType` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`relType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_relationtype
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_role`
-- ----------------------------
DROP TABLE IF EXISTS `acc_role`;
CREATE TABLE `acc_role` (
  `accRoleId` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  `roleType` bigint(20) NOT NULL,
  `acctId` bigint(20) NOT NULL,
  `partyId` bigint(20) NOT NULL,
  PRIMARY KEY (`accRoleId`),
  KEY `FK6jslclm1vnn8g3vxqpmoqbcis` (`roleType`),
  KEY `FKsc6r9pjvb9ayy5155n8a4abue` (`partyId`),
  CONSTRAINT `FK6jslclm1vnn8g3vxqpmoqbcis` FOREIGN KEY (`roleType`) REFERENCES `acc_roletype` (`roleType`),
  CONSTRAINT `FKsc6r9pjvb9ayy5155n8a4abue` FOREIGN KEY (`partyId`) REFERENCES `par_party` (`partyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_role
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_roletype`
-- ----------------------------
DROP TABLE IF EXISTS `acc_roletype`;
CREATE TABLE `acc_roletype` (
  `roleType` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`roleType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_roletype
-- ----------------------------

-- ----------------------------
-- Table structure for `acc_statetype`
-- ----------------------------
DROP TABLE IF EXISTS `acc_statetype`;
CREATE TABLE `acc_statetype` (
  `stateType` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`stateType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of acc_statetype
-- ----------------------------

-- ----------------------------
-- Table structure for `biz_businessflowcontext`
-- ----------------------------
DROP TABLE IF EXISTS `biz_businessflowcontext`;
CREATE TABLE `biz_businessflowcontext` (
  `businessflowId` int(11) NOT NULL AUTO_INCREMENT,
  `bizTypeId` int(11) DEFAULT NULL,
  `businessflowContext` varchar(255) DEFAULT NULL,
  `processId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`businessflowId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of biz_businessflowcontext
-- ----------------------------

-- ----------------------------
-- Table structure for `biz_operator`
-- ----------------------------
DROP TABLE IF EXISTS `biz_operator`;
CREATE TABLE `biz_operator` (
  `operatorId` bigint(20) NOT NULL AUTO_INCREMENT,
  `convertId` bigint(20) DEFAULT NULL,
  `loginUserId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`operatorId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of biz_operator
-- ----------------------------

-- ----------------------------
-- Table structure for `biz_party`
-- ----------------------------
DROP TABLE IF EXISTS `biz_party`;
CREATE TABLE `biz_party` (
  `bizPartyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `partyId` bigint(20) DEFAULT NULL,
  `processId` bigint(20) DEFAULT NULL,
  `roleType` int(11) DEFAULT NULL,
  PRIMARY KEY (`bizPartyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of biz_party
-- ----------------------------

-- ----------------------------
-- Table structure for `biz_state`
-- ----------------------------
DROP TABLE IF EXISTS `biz_state`;
CREATE TABLE `biz_state` (
  `stateId` bigint(20) NOT NULL AUTO_INCREMENT,
  `agreeNum` int(11) DEFAULT NULL,
  `currFlag` bit(1) DEFAULT NULL,
  `desentNum` int(11) DEFAULT NULL,
  `exeNum` int(11) DEFAULT NULL,
  `finishFlag` bit(1) DEFAULT NULL,
  `processId` bigint(20) DEFAULT NULL,
  `runningFlag` bit(1) DEFAULT NULL,
  `stateDesc` varchar(255) DEFAULT NULL,
  `stateTime` datetime DEFAULT NULL,
  `stateType` bit(1) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `strategyType` int(11) DEFAULT NULL,
  PRIMARY KEY (`stateId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of biz_state
-- ----------------------------

-- ----------------------------
-- Table structure for `biz_stateconvert`
-- ----------------------------
DROP TABLE IF EXISTS `biz_stateconvert`;
CREATE TABLE `biz_stateconvert` (
  `ConvertId` bigint(20) NOT NULL AUTO_INCREMENT,
  `bizTypeId` int(11) DEFAULT NULL,
  `functionId` bigint(20) DEFAULT NULL,
  `nextStatus` int(11) DEFAULT NULL,
  `partyId` bigint(20) DEFAULT NULL,
  `preState` int(11) DEFAULT NULL,
  `strategyType` int(11) DEFAULT NULL,
  `sysAutoFlag` bit(1) DEFAULT NULL,
  PRIMARY KEY (`ConvertId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of biz_stateconvert
-- ----------------------------

-- ----------------------------
-- Table structure for `biz_type`
-- ----------------------------
DROP TABLE IF EXISTS `biz_type`;
CREATE TABLE `biz_type` (
  `bizTypeId` int(11) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`bizTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of biz_type
-- ----------------------------

-- ----------------------------
-- Table structure for `commoninfo`
-- ----------------------------
DROP TABLE IF EXISTS `commoninfo`;
CREATE TABLE `commoninfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `info_id` int(11) NOT NULL,
  `info_type` varchar(255) DEFAULT NULL,
  `receive_account` varchar(255) DEFAULT NULL,
  `receive_name` varchar(255) DEFAULT NULL,
  `receive_time` datetime DEFAULT NULL,
  `send_account` varchar(255) DEFAULT NULL,
  `send_name` varchar(255) DEFAULT NULL,
  `send_time` datetime DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of commoninfo
-- ----------------------------

-- ----------------------------
-- Table structure for `com_msgdef`
-- ----------------------------
DROP TABLE IF EXISTS `com_msgdef`;
CREATE TABLE `com_msgdef` (
  `msgId` bigint(20) NOT NULL AUTO_INCREMENT,
  `aptName` varchar(255) DEFAULT NULL,
  `debugData` longtext,
  `name` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `transIn` longtext,
  `transOut` longtext,
  PRIMARY KEY (`msgId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of com_msgdef
-- ----------------------------

-- ----------------------------
-- Table structure for `email`
-- ----------------------------
DROP TABLE IF EXISTS `email`;
CREATE TABLE `email` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `draft` int(11) NOT NULL,
  `email_type` int(11) NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `file_url` varchar(255) DEFAULT NULL,
  `flag` int(11) NOT NULL,
  `img_url` varchar(255) DEFAULT NULL,
  `music_url` varchar(255) DEFAULT NULL,
  `receive_email` varchar(255) DEFAULT NULL,
  `receive_name` varchar(255) DEFAULT NULL,
  `receive_time` datetime DEFAULT NULL,
  `receivexml` varchar(255) DEFAULT NULL,
  `send_email` varchar(255) DEFAULT NULL,
  `send_name` varchar(255) DEFAULT NULL,
  `send_result` varchar(255) DEFAULT NULL,
  `send_status` int(11) NOT NULL,
  `send_time` datetime DEFAULT NULL,
  `sendxml` varchar(255) DEFAULT NULL,
  `string1` varchar(255) DEFAULT NULL,
  `string2` varchar(255) DEFAULT NULL,
  `string3` varchar(255) DEFAULT NULL,
  `string4` varchar(255) DEFAULT NULL,
  `third_id` varchar(255) DEFAULT NULL,
  `third_result` varchar(255) DEFAULT NULL,
  `third_status` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_account` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of email
-- ----------------------------

-- ----------------------------
-- Table structure for `jgpush`
-- ----------------------------
DROP TABLE IF EXISTS `jgpush`;
CREATE TABLE `jgpush` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `drapt` int(11) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `flag` int(11) DEFAULT NULL,
  `jpush_type` int(11) DEFAULT NULL,
  `platform` int(11) DEFAULT NULL,
  `receive_id` varchar(255) DEFAULT NULL,
  `receive_result` varchar(255) DEFAULT NULL,
  `receive_status` int(11) DEFAULT NULL,
  `receive_time` datetime DEFAULT NULL,
  `receivexml` varchar(255) DEFAULT NULL,
  `release_fun` varchar(255) DEFAULT NULL,
  `send_id` varchar(255) DEFAULT NULL,
  `send_result` varchar(255) DEFAULT NULL,
  `send_status` int(11) DEFAULT NULL,
  `send_time` datetime DEFAULT NULL,
  `sendxml` varchar(255) DEFAULT NULL,
  `string1` varchar(255) DEFAULT NULL,
  `string2` varchar(255) DEFAULT NULL,
  `string3` varchar(255) DEFAULT NULL,
  `string4` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_account` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of jgpush
-- ----------------------------

-- ----------------------------
-- Table structure for `par_org`
-- ----------------------------
DROP TABLE IF EXISTS `par_org`;
CREATE TABLE `par_org` (
  `accBank` varchar(255) DEFAULT NULL,
  `clertNum` int(11) DEFAULT NULL,
  `comType` int(11) DEFAULT NULL,
  `engName` varchar(255) DEFAULT NULL,
  `isGroup` int(11) DEFAULT NULL,
  `isIpo` int(11) DEFAULT NULL,
  `licEndDay` datetime DEFAULT NULL,
  `licNo` varchar(255) DEFAULT NULL,
  `licStartDay` datetime DEFAULT NULL,
  `loanCardNo` varchar(255) DEFAULT NULL,
  `localTaxNo` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `natTaxNo` varchar(255) DEFAULT NULL,
  `offAddr` varchar(255) DEFAULT NULL,
  `orgCode` varchar(255) DEFAULT NULL,
  `ower` varchar(255) DEFAULT NULL,
  `phoneNo` varchar(255) DEFAULT NULL,
  `regAddr` varchar(255) DEFAULT NULL,
  `regCapi` decimal(19,2) DEFAULT NULL,
  `regType` int(11) DEFAULT NULL,
  `shortName` varchar(255) DEFAULT NULL,
  `zip` varchar(255) DEFAULT NULL,
  `partyId` bigint(20) NOT NULL,
  PRIMARY KEY (`partyId`),
  CONSTRAINT `FKm9s22s0bulrbnahu7c5t6pmpi` FOREIGN KEY (`partyId`) REFERENCES `par_party` (`partyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_org
-- ----------------------------

-- ----------------------------
-- Table structure for `par_party`
-- ----------------------------
DROP TABLE IF EXISTS `par_party`;
CREATE TABLE `par_party` (
  `partyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `createDay` datetime DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `parTypeId` int(11) DEFAULT NULL,
  `state` smallint(6) NOT NULL,
  PRIMARY KEY (`partyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_party
-- ----------------------------

-- ----------------------------
-- Table structure for `par_partytype`
-- ----------------------------
DROP TABLE IF EXISTS `par_partytype`;
CREATE TABLE `par_partytype` (
  `parTypeId` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`parTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_partytype
-- ----------------------------

-- ----------------------------
-- Table structure for `par_party_extinfo`
-- ----------------------------
DROP TABLE IF EXISTS `par_party_extinfo`;
CREATE TABLE `par_party_extinfo` (
  `fieldId` bigint(20) NOT NULL AUTO_INCREMENT,
  `fName` varchar(255) DEFAULT NULL,
  `recordId` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fieldId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_party_extinfo
-- ----------------------------

-- ----------------------------
-- Table structure for `par_person`
-- ----------------------------
DROP TABLE IF EXISTS `par_person`;
CREATE TABLE `par_person` (
  `Gender` int(11) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `cellPhone` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `homeAddr` varchar(255) DEFAULT NULL,
  `idNo` varchar(255) DEFAULT NULL,
  `idType` varchar(255) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  `portrait` varchar(255) DEFAULT NULL,
  `postAddr` varchar(255) DEFAULT NULL,
  `qqNo` varchar(255) DEFAULT NULL,
  `weiXinNo` varchar(255) DEFAULT NULL,
  `zip` varchar(255) DEFAULT NULL,
  `partyId` bigint(20) NOT NULL,
  PRIMARY KEY (`partyId`),
  CONSTRAINT `FKjid1a1ba4jj6vccnux042gpq7` FOREIGN KEY (`partyId`) REFERENCES `par_party` (`partyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_person
-- ----------------------------

-- ----------------------------
-- Table structure for `par_role`
-- ----------------------------
DROP TABLE IF EXISTS `par_role`;
CREATE TABLE `par_role` (
  `roleId` int(11) NOT NULL AUTO_INCREMENT,
  `partyId` bigint(20) NOT NULL,
  `roleTypeId` int(11) NOT NULL,
  PRIMARY KEY (`roleId`),
  KEY `FKc4oqvgsgboq4m7n2bfvwtfbge` (`partyId`),
  KEY `FKcjt1fd6gqlp8kbocerc9vxy9a` (`roleTypeId`),
  CONSTRAINT `FKc4oqvgsgboq4m7n2bfvwtfbge` FOREIGN KEY (`partyId`) REFERENCES `par_party` (`partyId`),
  CONSTRAINT `FKcjt1fd6gqlp8kbocerc9vxy9a` FOREIGN KEY (`roleTypeId`) REFERENCES `par_role_type` (`roleTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_role
-- ----------------------------

-- ----------------------------
-- Table structure for `par_rolerelation_type`
-- ----------------------------
DROP TABLE IF EXISTS `par_rolerelation_type`;
CREATE TABLE `par_rolerelation_type` (
  `roleReType` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`roleReType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_rolerelation_type
-- ----------------------------

-- ----------------------------
-- Table structure for `par_role_relation`
-- ----------------------------
DROP TABLE IF EXISTS `par_role_relation`;
CREATE TABLE `par_role_relation` (
  `relationId` int(11) NOT NULL AUTO_INCREMENT,
  `role1Id` int(11) NOT NULL,
  `role2Id` int(11) NOT NULL,
  `roleReType` int(11) NOT NULL,
  PRIMARY KEY (`relationId`),
  KEY `FK7dypm37aq039hjk1pkisa91` (`role1Id`),
  KEY `FKoew5o5xgpcfdkukbvu3cbvj5x` (`role2Id`),
  KEY `FK6tfvlttqx47duav9kq89kuj04` (`roleReType`),
  CONSTRAINT `FK6tfvlttqx47duav9kq89kuj04` FOREIGN KEY (`roleReType`) REFERENCES `par_rolerelation_type` (`roleReType`),
  CONSTRAINT `FK7dypm37aq039hjk1pkisa91` FOREIGN KEY (`role1Id`) REFERENCES `par_role` (`roleId`),
  CONSTRAINT `FKoew5o5xgpcfdkukbvu3cbvj5x` FOREIGN KEY (`role2Id`) REFERENCES `par_role` (`roleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_role_relation
-- ----------------------------

-- ----------------------------
-- Table structure for `par_role_type`
-- ----------------------------
DROP TABLE IF EXISTS `par_role_type`;
CREATE TABLE `par_role_type` (
  `roleTypeId` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`roleTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_role_type
-- ----------------------------

-- ----------------------------
-- Table structure for `par_upload_file`
-- ----------------------------
DROP TABLE IF EXISTS `par_upload_file`;
CREATE TABLE `par_upload_file` (
  `relation_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `party_id` bigint(20) NOT NULL,
  `upload_id` bigint(20) NOT NULL,
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `UK_9btc5jdr3p5c1eoryi8pjatix` (`party_id`),
  KEY `FK2y29841baw5sd4jsfqrvmnh6r` (`upload_id`),
  CONSTRAINT `FK2y29841baw5sd4jsfqrvmnh6r` FOREIGN KEY (`upload_id`) REFERENCES `upload_file_info` (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of par_upload_file
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_archives`
-- ----------------------------
DROP TABLE IF EXISTS `prod_archives`;
CREATE TABLE `prod_archives` (
  `ArchiveId` bigint(20) NOT NULL AUTO_INCREMENT,
  `archName` varchar(255) DEFAULT NULL,
  `archType` bigint(20) DEFAULT NULL,
  `fileId` bigint(20) DEFAULT NULL,
  `prodId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ArchiveId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_archives
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_attrdef`
-- ----------------------------
DROP TABLE IF EXISTS `prod_attrdef`;
CREATE TABLE `prod_attrdef` (
  `attrId` bigint(20) NOT NULL AUTO_INCREMENT,
  `attrName` varchar(255) DEFAULT NULL,
  `attrType` bigint(20) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`attrId`),
  KEY `FK2do7la3lp1ef3na6g5w1os4xp` (`attrType`),
  CONSTRAINT `FK2do7la3lp1ef3na6g5w1os4xp` FOREIGN KEY (`attrType`) REFERENCES `prod_attrtype` (`AttrType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_attrdef
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_attrtype`
-- ----------------------------
DROP TABLE IF EXISTS `prod_attrtype`;
CREATE TABLE `prod_attrtype` (
  `AttrType` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`AttrType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_attrtype
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_calssify`
-- ----------------------------
DROP TABLE IF EXISTS `prod_calssify`;
CREATE TABLE `prod_calssify` (
  `ClassifyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `classfiyFlag` varchar(255) DEFAULT NULL,
  `endDay` date DEFAULT NULL,
  `prodId` bigint(20) NOT NULL,
  `startDay` date DEFAULT NULL,
  `templateId` bigint(20) NOT NULL,
  PRIMARY KEY (`ClassifyId`),
  KEY `FKo802wi2msgxk6ec33f5n12usb` (`templateId`),
  CONSTRAINT `FKo802wi2msgxk6ec33f5n12usb` FOREIGN KEY (`templateId`) REFERENCES `prod_template` (`TemplateId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_calssify
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_funset`
-- ----------------------------
DROP TABLE IF EXISTS `prod_funset`;
CREATE TABLE `prod_funset` (
  `FunctionId` bigint(20) NOT NULL AUTO_INCREMENT,
  `bizTypeId` int(11) DEFAULT NULL,
  `config` longtext,
  `dicConf` longtext,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`FunctionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_funset
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_index`
-- ----------------------------
DROP TABLE IF EXISTS `prod_index`;
CREATE TABLE `prod_index` (
  `amount` decimal(19,2) DEFAULT NULL,
  `balance` decimal(19,2) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `contractNo` varchar(255) DEFAULT NULL,
  `interestDay` date DEFAULT NULL,
  `minAmt` decimal(19,2) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `prodType` int(11) DEFAULT NULL,
  `progress` decimal(19,2) DEFAULT NULL,
  `rate` decimal(19,2) DEFAULT NULL,
  `risk` int(11) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `term` int(11) DEFAULT NULL,
  `prodId` bigint(20) NOT NULL,
  PRIMARY KEY (`prodId`),
  CONSTRAINT `FKdblukm5rletcbh98ubkggfc7x` FOREIGN KEY (`prodId`) REFERENCES `prod_product` (`ProdId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_index
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_planrelation`
-- ----------------------------
DROP TABLE IF EXISTS `prod_planrelation`;
CREATE TABLE `prod_planrelation` (
  `ReLationId` bigint(20) NOT NULL AUTO_INCREMENT,
  `PlanIdOne` bigint(20) DEFAULT NULL,
  `PlanIdTwo` bigint(20) DEFAULT NULL,
  `ReType` bigint(20) NOT NULL,
  PRIMARY KEY (`ReLationId`),
  KEY `FK681po7c67la2g7dp6x3q2jlkh` (`ReType`),
  CONSTRAINT `FK681po7c67la2g7dp6x3q2jlkh` FOREIGN KEY (`ReType`) REFERENCES `prod_plan_retype` (`reType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_planrelation
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_act`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_act`;
CREATE TABLE `prod_plan_act` (
  `actId` bigint(20) NOT NULL AUTO_INCREMENT,
  `actType` bigint(20) DEFAULT NULL,
  `amt` decimal(19,2) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `prodPlanId` bigint(20) DEFAULT NULL,
  `roleId` bigint(20) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`actId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_act
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_acttype`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_acttype`;
CREATE TABLE `prod_plan_acttype` (
  `actType` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`actType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_acttype
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_retype`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_retype`;
CREATE TABLE `prod_plan_retype` (
  `reType` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`reType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_retype
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_role`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_role`;
CREATE TABLE `prod_plan_role` (
  `roleId` bigint(20) NOT NULL AUTO_INCREMENT,
  `partyId` bigint(20) DEFAULT NULL,
  `planId` bigint(20) DEFAULT NULL,
  `roleType` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`roleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_role
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_roletype`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_roletype`;
CREATE TABLE `prod_plan_roletype` (
  `roleType` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`roleType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_roletype
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_state`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_state`;
CREATE TABLE `prod_plan_state` (
  `stateId` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDay` date DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `startDay` date DEFAULT NULL,
  `stateTime` date DEFAULT NULL,
  `prodPlanId` bigint(20) NOT NULL,
  `stateType` bigint(20) NOT NULL,
  PRIMARY KEY (`stateId`),
  KEY `FK1tkt9v06yyppjxepeh1qpt2dq` (`stateType`),
  CONSTRAINT `FK1tkt9v06yyppjxepeh1qpt2dq` FOREIGN KEY (`stateType`) REFERENCES `prod_plan_statetype` (`stateType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_state
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_statetype`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_statetype`;
CREATE TABLE `prod_plan_statetype` (
  `stateType` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`stateType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_statetype
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_type`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_type`;
CREATE TABLE `prod_plan_type` (
  `planId` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`planId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_type
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_plan_urge`
-- ----------------------------
DROP TABLE IF EXISTS `prod_plan_urge`;
CREATE TABLE `prod_plan_urge` (
  `urgeId` bigint(20) NOT NULL AUTO_INCREMENT,
  `recordTime` datetime DEFAULT NULL,
  `urgeDay` date DEFAULT NULL,
  `urgeDesc` varchar(255) DEFAULT NULL,
  `urgeType` int(11) DEFAULT NULL,
  `partyId` bigint(20) NOT NULL,
  `prodPlanId` bigint(20) NOT NULL,
  PRIMARY KEY (`urgeId`),
  KEY `FKarq0apjae5ipvvmsiyfmn908g` (`partyId`),
  CONSTRAINT `FKarq0apjae5ipvvmsiyfmn908g` FOREIGN KEY (`partyId`) REFERENCES `par_person` (`partyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_plan_urge
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_prodattr`
-- ----------------------------
DROP TABLE IF EXISTS `prod_prodattr`;
CREATE TABLE `prod_prodattr` (
  `ProdAttrId` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDay` datetime DEFAULT NULL,
  `startDay` datetime DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `attrId` bigint(20) NOT NULL,
  `prodId` bigint(20) NOT NULL,
  PRIMARY KEY (`ProdAttrId`),
  KEY `FK7ymv2y8hemobo90c1dvmtrv29` (`attrId`),
  KEY `FKcuic2q7a6mjg7q4hye510eydf` (`prodId`),
  CONSTRAINT `FK7ymv2y8hemobo90c1dvmtrv29` FOREIGN KEY (`attrId`) REFERENCES `prod_attrdef` (`attrId`),
  CONSTRAINT `FKcuic2q7a6mjg7q4hye510eydf` FOREIGN KEY (`prodId`) REFERENCES `prod_product` (`ProdId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_prodattr
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_prodfun`
-- ----------------------------
DROP TABLE IF EXISTS `prod_prodfun`;
CREATE TABLE `prod_prodfun` (
  `ProdFunId` bigint(20) NOT NULL AUTO_INCREMENT,
  `config` varchar(255) DEFAULT NULL,
  `endDay` date DEFAULT NULL,
  `functionId` bigint(20) NOT NULL,
  `prodAttrId` bigint(20) NOT NULL,
  `prodId` bigint(20) NOT NULL,
  `startDay` date DEFAULT NULL,
  PRIMARY KEY (`ProdFunId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_prodfun
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_prodstate`
-- ----------------------------
DROP TABLE IF EXISTS `prod_prodstate`;
CREATE TABLE `prod_prodstate` (
  `StateId` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDay` date DEFAULT NULL,
  `startDay` date DEFAULT NULL,
  `typeId` bigint(20) NOT NULL,
  `prodId` bigint(20) NOT NULL,
  PRIMARY KEY (`StateId`),
  KEY `FK6emy6ey64d5jkdpv2wrsibxg0` (`typeId`),
  KEY `FKhpc3717h2ipckh1uej5u9x5gf` (`prodId`),
  CONSTRAINT `FK6emy6ey64d5jkdpv2wrsibxg0` FOREIGN KEY (`typeId`) REFERENCES `prod_prodstatetype` (`TypeId`),
  CONSTRAINT `FKhpc3717h2ipckh1uej5u9x5gf` FOREIGN KEY (`prodId`) REFERENCES `prod_index` (`prodId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_prodstate
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_prodstatetype`
-- ----------------------------
DROP TABLE IF EXISTS `prod_prodstatetype`;
CREATE TABLE `prod_prodstatetype` (
  `TypeId` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`TypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_prodstatetype
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_product`
-- ----------------------------
DROP TABLE IF EXISTS `prod_product`;
CREATE TABLE `prod_product` (
  `ProdId` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ProdId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_product
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_tempattr`
-- ----------------------------
DROP TABLE IF EXISTS `prod_tempattr`;
CREATE TABLE `prod_tempattr` (
  `TempAttrId` bigint(20) NOT NULL AUTO_INCREMENT,
  `defValue` varchar(255) DEFAULT NULL,
  `attrId` bigint(20) NOT NULL,
  `templateId` bigint(20) NOT NULL,
  PRIMARY KEY (`TempAttrId`),
  KEY `FKtluskhby51b4lmy2i41rv6g1` (`attrId`),
  KEY `FK970vgseohfre2jl8y22kko183` (`templateId`),
  CONSTRAINT `FK970vgseohfre2jl8y22kko183` FOREIGN KEY (`templateId`) REFERENCES `prod_template` (`TemplateId`),
  CONSTRAINT `FKtluskhby51b4lmy2i41rv6g1` FOREIGN KEY (`attrId`) REFERENCES `prod_attrdef` (`attrId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_tempattr
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_tempfun`
-- ----------------------------
DROP TABLE IF EXISTS `prod_tempfun`;
CREATE TABLE `prod_tempfun` (
  `TempFunId` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDay` date DEFAULT NULL,
  `startDay` date DEFAULT NULL,
  `tempAttrId` int(11) NOT NULL,
  `functionId` bigint(20) NOT NULL,
  `templateId` bigint(20) NOT NULL,
  PRIMARY KEY (`TempFunId`),
  KEY `FKl1pjjj1sbe24en5ghauervx2e` (`functionId`),
  KEY `FKamfwp4e1emies6jb5v1i77qiv` (`templateId`),
  CONSTRAINT `FKamfwp4e1emies6jb5v1i77qiv` FOREIGN KEY (`templateId`) REFERENCES `prod_template` (`TemplateId`),
  CONSTRAINT `FKl1pjjj1sbe24en5ghauervx2e` FOREIGN KEY (`functionId`) REFERENCES `prod_funset` (`FunctionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_tempfun
-- ----------------------------

-- ----------------------------
-- Table structure for `prod_template`
-- ----------------------------
DROP TABLE IF EXISTS `prod_template`;
CREATE TABLE `prod_template` (
  `TemplateId` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`TemplateId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prod_template
-- ----------------------------

-- ----------------------------
-- Table structure for `sms`
-- ----------------------------
DROP TABLE IF EXISTS `sms`;
CREATE TABLE `sms` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `draft` int(11) NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `flag` int(11) NOT NULL,
  `msg_type` int(11) NOT NULL,
  `receive_mobile` varchar(255) DEFAULT NULL,
  `receive_name` varchar(255) DEFAULT NULL,
  `receive_time` datetime DEFAULT NULL,
  `receivexml` varchar(255) DEFAULT NULL,
  `send_mobile` varchar(255) DEFAULT NULL,
  `send_name` varchar(255) DEFAULT NULL,
  `send_result` varchar(255) DEFAULT NULL,
  `send_status` int(11) NOT NULL,
  `send_time` datetime DEFAULT NULL,
  `sendxml` varchar(255) DEFAULT NULL,
  `string1` varchar(255) DEFAULT NULL,
  `string2` varchar(255) DEFAULT NULL,
  `string3` varchar(255) DEFAULT NULL,
  `string4` varchar(255) DEFAULT NULL,
  `third_id` varchar(255) DEFAULT NULL,
  `third_result` varchar(255) DEFAULT NULL,
  `third_status` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_account` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sms
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_function`
-- ----------------------------
DROP TABLE IF EXISTS `sys_function`;
CREATE TABLE `sys_function` (
  `funId` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`funId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_function
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_fun_debugmsg`
-- ----------------------------
DROP TABLE IF EXISTS `sys_fun_debugmsg`;
CREATE TABLE `sys_fun_debugmsg` (
  `MsgId` bigint(20) NOT NULL AUTO_INCREMENT,
  `msgData` longtext,
  `msgTime` datetime DEFAULT NULL,
  `procName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`MsgId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_fun_debugmsg
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_info_file`
-- ----------------------------
DROP TABLE IF EXISTS `sys_info_file`;
CREATE TABLE `sys_info_file` (
  `upFileId` bigint(20) NOT NULL AUTO_INCREMENT,
  `clientFileName` varchar(255) DEFAULT NULL,
  `fileName` varchar(255) DEFAULT NULL,
  `fileType` varchar(255) DEFAULT NULL,
  `recordTime` datetime DEFAULT NULL,
  `srcId` bigint(20) DEFAULT NULL,
  `srcTable` bigint(20) DEFAULT NULL,
  `upPartyId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`upFileId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_info_file
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_loginlog`
-- ----------------------------
DROP TABLE IF EXISTS `sys_loginlog`;
CREATE TABLE `sys_loginlog` (
  `logId` varchar(255) NOT NULL,
  `loginChannel` smallint(6) NOT NULL,
  `loginTime` datetime DEFAULT NULL,
  `loginUserId` decimal(19,2) DEFAULT NULL,
  `userIp` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`logId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_loginlog
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_loginroletype`
-- ----------------------------
DROP TABLE IF EXISTS `sys_loginroletype`;
CREATE TABLE `sys_loginroletype` (
  `role` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_loginroletype
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_loginuser`
-- ----------------------------
DROP TABLE IF EXISTS `sys_loginuser`;
CREATE TABLE `sys_loginuser` (
  `loginUserId` bigint(20) NOT NULL AUTO_INCREMENT,
  `loginId` varchar(255) DEFAULT NULL,
  `loginName` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `trsPwd` varchar(255) DEFAULT NULL,
  `PartyId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`loginUserId`),
  KEY `FKe4ev97s59sc1wwg3693xolcqj` (`PartyId`),
  CONSTRAINT `FKe4ev97s59sc1wwg3693xolcqj` FOREIGN KEY (`PartyId`) REFERENCES `par_party` (`partyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_loginuser
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_login_role`
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_role`;
CREATE TABLE `sys_login_role` (
  `userLoginRoleId` bigint(20) NOT NULL AUTO_INCREMENT,
  `loginUserId` bigint(20) NOT NULL,
  `role` int(11) NOT NULL,
  PRIMARY KEY (`userLoginRoleId`),
  KEY `FK50rd0brpq16fismrt3ycqcviu` (`loginUserId`),
  KEY `FKfygm9u59lq2skd471p1q1m1w8` (`role`),
  CONSTRAINT `FK50rd0brpq16fismrt3ycqcviu` FOREIGN KEY (`loginUserId`) REFERENCES `sys_loginuser` (`loginUserId`),
  CONSTRAINT `FKfygm9u59lq2skd471p1q1m1w8` FOREIGN KEY (`role`) REFERENCES `sys_loginroletype` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_login_role
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_mediatype`
-- ----------------------------
DROP TABLE IF EXISTS `sys_mediatype`;
CREATE TABLE `sys_mediatype` (
  `mediaType` smallint(6) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mediaType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_mediatype
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_param`
-- ----------------------------
DROP TABLE IF EXISTS `sys_param`;
CREATE TABLE `sys_param` (
  `fieldId` varchar(255) NOT NULL,
  `recorderId` varchar(255) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fieldId`,`recorderId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_param
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_record`
-- ----------------------------
DROP TABLE IF EXISTS `sys_record`;
CREATE TABLE `sys_record` (
  `recordId` bigint(20) NOT NULL AUTO_INCREMENT,
  `classify` int(11) NOT NULL,
  `createTime` datetime DEFAULT NULL,
  `nextRecId` bigint(20) DEFAULT NULL,
  `parentId` bigint(20) NOT NULL,
  `recName` varchar(255) DEFAULT NULL,
  `state` int(11) NOT NULL,
  PRIMARY KEY (`recordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_record
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_rolefun`
-- ----------------------------
DROP TABLE IF EXISTS `sys_rolefun`;
CREATE TABLE `sys_rolefun` (
  `roleFunId` bigint(20) NOT NULL AUTO_INCREMENT,
  `funId` bigint(20) DEFAULT NULL,
  `role` int(11) NOT NULL,
  PRIMARY KEY (`roleFunId`),
  KEY `FKfv2o6ix0iot6dpjf0c3ci7mmk` (`role`),
  CONSTRAINT `FKfv2o6ix0iot6dpjf0c3ci7mmk` FOREIGN KEY (`role`) REFERENCES `sys_loginroletype` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_rolefun
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_settype`
-- ----------------------------
DROP TABLE IF EXISTS `sys_settype`;
CREATE TABLE `sys_settype` (
  `typeName` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note1` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`typeName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_settype
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_usermedia`
-- ----------------------------
DROP TABLE IF EXISTS `sys_usermedia`;
CREATE TABLE `sys_usermedia` (
  `userMediaId` bigint(20) NOT NULL,
  `loginUserId` bigint(20) NOT NULL,
  `mediaType` int(11) NOT NULL,
  PRIMARY KEY (`userMediaId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_usermedia
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_usermediainfo`
-- ----------------------------
DROP TABLE IF EXISTS `sys_usermediainfo`;
CREATE TABLE `sys_usermediainfo` (
  `infoId` varchar(255) NOT NULL,
  `recorderId` decimal(19,2) DEFAULT NULL,
  `userMediaId` decimal(19,2) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`infoId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_usermediainfo
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_usertoken`
-- ----------------------------
DROP TABLE IF EXISTS `sys_usertoken`;
CREATE TABLE `sys_usertoken` (
  `userTokenId` bigint(20) NOT NULL AUTO_INCREMENT,
  `accessToken` varchar(255) DEFAULT NULL,
  `loginId` bigint(20) DEFAULT NULL,
  `loginUserId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`userTokenId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_usertoken
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_accprod`
-- ----------------------------
DROP TABLE IF EXISTS `tran_accprod`;
CREATE TABLE `tran_accprod` (
  `AccProdId` bigint(20) NOT NULL AUTO_INCREMENT,
  `acctId` bigint(20) DEFAULT NULL,
  `endDay` date DEFAULT NULL,
  `prodId` bigint(20) DEFAULT NULL,
  `startDay` date DEFAULT NULL,
  `transId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`AccProdId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_accprod
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_relation`
-- ----------------------------
DROP TABLE IF EXISTS `tran_relation`;
CREATE TABLE `tran_relation` (
  `RelationId` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDay` date DEFAULT NULL,
  `relType` bigint(20) DEFAULT NULL,
  `startDay` date DEFAULT NULL,
  `transaction1` bigint(20) DEFAULT NULL,
  `transaction2` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`RelationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_relation
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_relationtype`
-- ----------------------------
DROP TABLE IF EXISTS `tran_relationtype`;
CREATE TABLE `tran_relationtype` (
  `RelType` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`RelType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_relationtype
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_state`
-- ----------------------------
DROP TABLE IF EXISTS `tran_state`;
CREATE TABLE `tran_state` (
  `StateId` bigint(20) NOT NULL AUTO_INCREMENT,
  `endDay` date DEFAULT NULL,
  `startDay` date DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `StateType` bigint(20) NOT NULL,
  `TransId` bigint(20) NOT NULL,
  PRIMARY KEY (`StateId`),
  KEY `FKsssdjauc9q5nloby6c2ajem83` (`StateType`),
  CONSTRAINT `FKsssdjauc9q5nloby6c2ajem83` FOREIGN KEY (`StateType`) REFERENCES `tran_statetype` (`StateType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_state
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_statetype`
-- ----------------------------
DROP TABLE IF EXISTS `tran_statetype`;
CREATE TABLE `tran_statetype` (
  `StateType` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`StateType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_statetype
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_task`
-- ----------------------------
DROP TABLE IF EXISTS `tran_task`;
CREATE TABLE `tran_task` (
  `TaskId` bigint(20) NOT NULL,
  `createDay` date DEFAULT NULL,
  `taskDay` date DEFAULT NULL,
  `taskTyp` bigint(20) NOT NULL,
  `transId` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL,
  PRIMARY KEY (`TaskId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_task
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_taskdef`
-- ----------------------------
DROP TABLE IF EXISTS `tran_taskdef`;
CREATE TABLE `tran_taskdef` (
  `Unit` varchar(255) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Unit`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_taskdef
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_tasktype`
-- ----------------------------
DROP TABLE IF EXISTS `tran_tasktype`;
CREATE TABLE `tran_tasktype` (
  `TaskType` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`TaskType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_tasktype
-- ----------------------------

-- ----------------------------
-- Table structure for `tran_type`
-- ----------------------------
DROP TABLE IF EXISTS `tran_type`;
CREATE TABLE `tran_type` (
  `TranType` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`TranType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tran_type
-- ----------------------------

-- ----------------------------
-- Table structure for `upload_file_info`
-- ----------------------------
DROP TABLE IF EXISTS `upload_file_info`;
CREATE TABLE `upload_file_info` (
  `upload_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `note` varchar(255) DEFAULT NULL,
  `pic_name` varchar(255) DEFAULT NULL,
  `pic_type` varchar(255) DEFAULT NULL,
  `save_dir` varchar(255) DEFAULT NULL,
  `save_name` varchar(255) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `upload_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of upload_file_info
-- ----------------------------

-- ----------------------------
-- Table structure for `view_procconvert`
-- ----------------------------
DROP TABLE IF EXISTS `view_procconvert`;
CREATE TABLE `view_procconvert` (
  `convertId` bigint(20) NOT NULL AUTO_INCREMENT,
  `bizName` varchar(255) DEFAULT NULL,
  `bizType` int(11) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `funName` varchar(255) DEFAULT NULL,
  `functionId` bigint(20) DEFAULT NULL,
  `nextStatus` int(11) DEFAULT NULL,
  `originOrg` bigint(20) DEFAULT NULL,
  `partyId` bigint(20) DEFAULT NULL,
  `preStatus` int(11) DEFAULT NULL,
  `processId` bigint(20) DEFAULT NULL,
  `prodId` bigint(20) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `sysAutoFlag` bit(1) DEFAULT NULL,
  PRIMARY KEY (`convertId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of view_procconvert
-- ----------------------------

-- ----------------------------
-- Table structure for `view_user_role`
-- ----------------------------
DROP TABLE IF EXISTS `view_user_role`;
CREATE TABLE `view_user_role` (
  `roleFunId` bigint(20) NOT NULL AUTO_INCREMENT,
  `LoginId` bigint(20) DEFAULT NULL,
  `LoginName` varchar(255) DEFAULT NULL,
  `LoginUserId` bigint(20) DEFAULT NULL,
  `PartyId` bigint(20) DEFAULT NULL,
  `RoleId` bigint(20) DEFAULT NULL,
  `funId` bigint(20) DEFAULT NULL,
  `funName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`roleFunId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of view_user_role
-- ----------------------------
