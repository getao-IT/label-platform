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

 Date: 18/08/2022 16:11:20
*/


-- ----------------------------
-- Table structure for label_category
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_category";
CREATE TABLE "public"."label_category" (
  "id" int4 NOT NULL,
  "category_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."label_category"."id" IS 'id';
COMMENT ON COLUMN "public"."label_category"."category_name" IS '标注类型名称';

-- ----------------------------
-- Primary Key structure for table label_category
-- ----------------------------
ALTER TABLE "public"."label_category" ADD CONSTRAINT "label_category_pkey" PRIMARY KEY ("id");
