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

 Date: 18/08/2022 16:11:48
*/

CREATE SEQUENCE label_image_object_info_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_image_object_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_image_object_info";
CREATE TABLE "public"."label_image_object_info" (
  "id" int4 NOT NULL DEFAULT nextval('label_image_object_info_id_seq'::regclass),
  "country" varchar(255) COLLATE "pg_catalog"."default",
  "sensor" varchar(255) COLLATE "pg_catalog"."default",
  "satellite" varchar(255) COLLATE "pg_catalog"."default",
  "resolution" varchar(24) COLLATE "pg_catalog"."default",
  "label1" varchar(255) COLLATE "pg_catalog"."default",
  "label2" varchar(255) COLLATE "pg_catalog"."default",
  "label3" varchar(255) COLLATE "pg_catalog"."default",
  "label4" varchar(255) COLLATE "pg_catalog"."default",
  "label5" varchar(255) COLLATE "pg_catalog"."default",
  "label6" varchar(255) COLLATE "pg_catalog"."default",
  "image_id" int4,
  "base" varchar(255) COLLATE "pg_catalog"."default",
  "plan_id" int4,
  "selected" bool DEFAULT false
)
;

-- ----------------------------
-- Primary Key structure for table label_image_object_info
-- ----------------------------
ALTER TABLE "public"."label_image_object_info" ADD CONSTRAINT "label_image_label_info_pkey" PRIMARY KEY ("id");
