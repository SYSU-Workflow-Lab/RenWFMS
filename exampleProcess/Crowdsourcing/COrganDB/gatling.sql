/*
Source Server         : localhost_3306
Source Server Version : 50720
Source Host           : localhost:3306
Source Database       : rencorgan

Target Server Type    : MYSQL
Target Server Version : 50720
File Encoding         : 65001

Date: 2018-03-16 00:37:14
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `ren_agent`
-- ----------------------------
DROP TABLE IF EXISTS `ren_agent`;
CREATE TABLE `ren_agent` (
  `id` varchar(60) NOT NULL,
  `name` text,
  `location` text,
  `type` int(11) DEFAULT NULL,
  `note` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_agent
-- ----------------------------
INSERT INTO `ren_agent` VALUES ('Agent_39e499e1-25a3-11e8-91b1-2c4d54f01cf2', 'AutomicQueryAgent', 'http://localhost:10300/', '0', '');
INSERT INTO `ren_agent` VALUES ('Agent_ba525af0-24da-11e8-ae89-2c4d54f01cf2', 'AutoMergeAgent', 'http://localhost:10300/', '0', '');

-- ----------------------------
-- Table structure for `ren_capability`
-- ----------------------------
DROP TABLE IF EXISTS `ren_capability`;
CREATE TABLE `ren_capability` (
  `id` varchar(64) NOT NULL,
  `name` text,
  `description` text,
  `note` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_capability
-- ----------------------------
INSERT INTO `ren_capability` VALUES ('Capa_1f54376e-25a3-11e8-8267-2c4d54f01cf2', 'CanQuery', '', '');
INSERT INTO `ren_capability` VALUES ('Capa_cb8b61e1-24da-11e8-a3d8-2c4d54f01cf2', 'CanMerge', '', '');
INSERT INTO `ren_capability` VALUES ('Capa_cedf6e11-250a-11e8-b05a-2c4d54f01cf2', 'CanPublish', '', '');
INSERT INTO `ren_capability` VALUES ('Capa_ceecb54f-24da-11e8-84a6-2c4d54f01cf2', 'CanSolve', '', '');
INSERT INTO `ren_capability` VALUES ('Capa_d3e7e29e-24da-11e8-8487-2c4d54f01cf2', 'CanSolveVote', '', '');
INSERT INTO `ren_capability` VALUES ('Capa_d72c864f-24da-11e8-b535-2c4d54f01cf2', 'CanDecompose', '', '');
INSERT INTO `ren_capability` VALUES ('Capa_db79994f-24da-11e8-abc0-2c4d54f01cf2', 'CanDecomposeVote', '', '');
INSERT INTO `ren_capability` VALUES ('Capa_e986668f-24da-11e8-972c-2c4d54f01cf2', 'CanJudge', '', '');

-- ----------------------------
-- Table structure for `ren_cconfig`
-- ----------------------------
DROP TABLE IF EXISTS `ren_cconfig`;
CREATE TABLE `ren_cconfig` (
  `rkey` varchar(64) NOT NULL,
  `rvalue` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`rkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_cconfig
-- ----------------------------
INSERT INTO `ren_cconfig` VALUES ('organizationId', 'COrg_571d200f-0f35-11e8-9072-5404a6a99e5d');

-- ----------------------------
-- Table structure for `ren_connect`
-- ----------------------------
DROP TABLE IF EXISTS `ren_connect`;
CREATE TABLE `ren_connect` (
  `conId` int(11) NOT NULL AUTO_INCREMENT,
  `workerId` varchar(64) DEFAULT NULL,
  `belongToOrganizableId` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`conId`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_connect
-- ----------------------------
INSERT INTO `ren_connect` VALUES ('32', 'Agent_39e499e1-25a3-11e8-91b1-2c4d54f01cf2', 'Capa_1f54376e-25a3-11e8-8267-2c4d54f01cf2');
INSERT INTO `ren_connect` VALUES ('33', 'Agent_ba525af0-24da-11e8-ae89-2c4d54f01cf2', 'Capa_cb8b61e1-24da-11e8-a3d8-2c4d54f01cf2');
INSERT INTO `ren_connect` VALUES ('34', 'Human_868c2791-24db-11e8-af0e-2c4d54f01cf2', 'Capa_cedf6e11-250a-11e8-b05a-2c4d54f01cf2');
INSERT INTO `ren_connect` VALUES ('35', 'Human_868c2791-24db-11e8-af0e-2c4d54f01cf2', 'Capa_ceecb54f-24da-11e8-84a6-2c4d54f01cf2');
INSERT INTO `ren_connect` VALUES ('36', 'Human_868c2791-24db-11e8-af0e-2c4d54f01cf2', 'Capa_d3e7e29e-24da-11e8-8487-2c4d54f01cf2');
INSERT INTO `ren_connect` VALUES ('37', 'Human_868c2791-24db-11e8-af0e-2c4d54f01cf2', 'Capa_d72c864f-24da-11e8-b535-2c4d54f01cf2');
INSERT INTO `ren_connect` VALUES ('38', 'Human_868c2791-24db-11e8-af0e-2c4d54f01cf2', 'Capa_db79994f-24da-11e8-abc0-2c4d54f01cf2');
INSERT INTO `ren_connect` VALUES ('39', 'Human_868c2791-24db-11e8-af0e-2c4d54f01cf2', 'Capa_e986668f-24da-11e8-972c-2c4d54f01cf2');

-- ----------------------------
-- Table structure for `ren_group`
-- ----------------------------
DROP TABLE IF EXISTS `ren_group`;
CREATE TABLE `ren_group` (
  `id` varchar(64) NOT NULL,
  `name` text,
  `description` text,
  `note` text,
  `belongToId` varchar(64) DEFAULT NULL,
  `groupType` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_group
-- ----------------------------

-- ----------------------------
-- Table structure for `ren_human`
-- ----------------------------
DROP TABLE IF EXISTS `ren_human`;
CREATE TABLE `ren_human` (
  `id` varchar(64) NOT NULL,
  `person_id` text,
  `firstname` text,
  `lastname` text,
  `note` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_human
-- ----------------------------
INSERT INTO `ren_human` VALUES ('Human_868c2791-24db-11e8-af0e-2c4d54f01cf2', 'User', '', '', '');

-- ----------------------------
-- Table structure for `ren_log`
-- ----------------------------
DROP TABLE IF EXISTS `ren_log`;
CREATE TABLE `ren_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(64) DEFAULT NULL,
  `level` varchar(16) DEFAULT NULL,
  `message` text,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_log
-- ----------------------------

-- ----------------------------
-- Table structure for `ren_position`
-- ----------------------------
DROP TABLE IF EXISTS `ren_position`;
CREATE TABLE `ren_position` (
  `id` varchar(64) NOT NULL,
  `name` text,
  `description` text,
  `note` text,
  `belongToId` varchar(64) DEFAULT NULL,
  `reportToId` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_position
-- ----------------------------

-- ----------------------------
-- Table structure for `ren_user`
-- ----------------------------
DROP TABLE IF EXISTS `ren_user`;
CREATE TABLE `ren_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` varchar(128) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `createtimestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ren_user
-- ----------------------------
INSERT INTO `ren_user` VALUES ('1', 'admin', '57a64f6a47aa58e84035df91c798ac8e89c732b0363dfc01da54b78b7ac015bf', '1', '0', '2018-02-11 22:12:00');
