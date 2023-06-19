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

 Date: 18/08/2022 16:12:10
*/

CREATE SEQUENCE label_project_base_info_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_project_base_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_project_base_info";
CREATE TABLE "public"."label_project_base_info" (
  "id" int4 NOT NULL DEFAULT nextval('label_project_base_info_id_seq'::regclass),
  "label_project_id" int4,
  "base" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."label_project_base_info"."label_project_id" IS '标注项目id';
COMMENT ON COLUMN "public"."label_project_base_info"."base" IS '基地名';

-- ----------------------------
-- Primary Key structure for table label_project_base_info
-- ----------------------------
ALTER TABLE "public"."label_project_base_info" ADD CONSTRAINT "label_project_base_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table label_project_base_info
-- ----------------------------
ALTER TABLE "public"."label_project_base_info" ADD CONSTRAINT "label_project_id" FOREIGN KEY ("label_project_id") REFERENCES "public"."label_project" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
