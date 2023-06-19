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

 Date: 18/08/2022 16:11:30
*/

CREATE SEQUENCE label_dataset_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_dataset
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_dataset";
CREATE TABLE "public"."label_dataset" (
  "id" int4 NOT NULL DEFAULT nextval('label_dataset_id_seq'::regclass),
  "dataset_name" varchar(255) COLLATE "pg_catalog"."default",
  "dataset_path" varchar(255) COLLATE "pg_catalog"."default",
  "count" int4,
  "create_time" timestamp(0),
  "status" varchar(255) COLLATE "pg_catalog"."default",
  "manifest_path" varchar(255) COLLATE "pg_catalog"."default",
  "user_id" int4,
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "keywords" varchar(255) COLLATE "pg_catalog"."default",
  "project_id" int4,
  "category" varchar(255) COLLATE "pg_catalog"."default",
  "visibility" bool,
  "is_public" bool,
  "dataset_type" varchar(255) COLLATE "pg_catalog"."default",
  "project_category" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."label_dataset"."id" IS 'id';
COMMENT ON COLUMN "public"."label_dataset"."dataset_name" IS '数据集名称';
COMMENT ON COLUMN "public"."label_dataset"."dataset_path" IS '数据集目录';
COMMENT ON COLUMN "public"."label_dataset"."count" IS '数据集数量';
COMMENT ON COLUMN "public"."label_dataset"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."label_dataset"."status" IS '数据集状态';
COMMENT ON COLUMN "public"."label_dataset"."manifest_path" IS '数据集标注信息路径';
COMMENT ON COLUMN "public"."label_dataset"."user_id" IS '数据集创建用户id';
COMMENT ON COLUMN "public"."label_dataset"."description" IS '数据集描述';
COMMENT ON COLUMN "public"."label_dataset"."keywords" IS '数据集关键词';
COMMENT ON COLUMN "public"."label_dataset"."project_id" IS '结果数据集关联的项目id';
COMMENT ON COLUMN "public"."label_dataset"."category" IS '数据集类型';
COMMENT ON COLUMN "public"."label_dataset"."visibility" IS '是否可见';
COMMENT ON COLUMN "public"."label_dataset"."is_public" IS '是否公开';
COMMENT ON COLUMN "public"."label_dataset"."dataset_type" IS '数据集类型';
COMMENT ON COLUMN "public"."label_dataset"."project_category" IS '任务生成的数据集标注类型';

-- ----------------------------
-- Primary Key structure for table label_dataset
-- ----------------------------
ALTER TABLE "public"."label_dataset" ADD CONSTRAINT "datasets_pkey" PRIMARY KEY ("id");
