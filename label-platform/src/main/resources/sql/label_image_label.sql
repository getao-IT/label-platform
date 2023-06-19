/*
 Navicat Premium Data Transfer

 Source Server         : geodl_iecas
 Source Server Type    : PostgreSQL
 Source Server Version : 120003
 Source Host           : 192.168.9.64:32189
 Source Catalog        : geodl_iecas
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 120003
 File Encoding         : 65001

 Date: 18/08/2022 16:11:43
*/

CREATE SEQUENCE label_image_label_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_image_label
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_image_label";
CREATE TABLE "public"."label_image_label" (
  "id" int4 NOT NULL,
  "label_name" varchar(255) COLLATE "pg_catalog"."default",
  "parent_id" int4 DEFAULT nextval('label_image_label_id_seq'::regclass)
)
;

-- ----------------------------
-- Primary Key structure for table label_image_label
-- ----------------------------
ALTER TABLE "public"."label_image_label" ADD CONSTRAINT "label_image_label_pkey" PRIMARY KEY ("id");
