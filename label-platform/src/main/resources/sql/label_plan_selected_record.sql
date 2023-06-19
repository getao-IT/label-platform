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

 Date: 18/08/2022 16:11:57
*/

CREATE SEQUENCE label_plan_selected_record_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_plan_selected_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_plan_selected_record";
CREATE TABLE "public"."label_plan_selected_record" (
  "id" int4 NOT NULL DEFAULT nextval('label_plan_selected_record_id_seq'::regclass),
  "label_plan_id" int4,
  "select_batch_no" int4,
  "country" varchar(255) COLLATE "pg_catalog"."default",
  "satellite" varchar(255) COLLATE "pg_catalog"."default",
  "resolution" varchar(255) COLLATE "pg_catalog"."default",
  "payload" varchar(255) COLLATE "pg_catalog"."default",
  "label_object" varchar(255) COLLATE "pg_catalog"."default",
  "object_count" int4,
  "file_count" int4,
  "file_id_list" varchar(255) COLLATE "pg_catalog"."default",
  "base" varchar(255) COLLATE "pg_catalog"."default",
  "selected_percent" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Primary Key structure for table label_plan_selected_record
-- ----------------------------
ALTER TABLE "public"."label_plan_selected_record" ADD CONSTRAINT "label_plan_selected_record_pkey" PRIMARY KEY ("id");
