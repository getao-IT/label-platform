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

 Date: 18/08/2022 16:11:52
*/

CREATE SEQUENCE label_plan_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_plan
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_plan";
CREATE TABLE "public"."label_plan" (
  "id" int4 NOT NULL DEFAULT nextval('label_plan_id_seq'::regclass),
  "dataset_id" int4,
  "plan_name" varchar(255) COLLATE "pg_catalog"."default",
  "first_project_user_id" int4,
  "second_project_user_id" int4,
  "third_project_user_id" int4,
  "total_count" int4,
  "dataset_type" varchar(255) COLLATE "pg_catalog"."default",
  "plan_publisher_id" int4 DEFAULT nextval('label_plan_id_seq'::regclass),
  "category" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamptz(0) DEFAULT NULL::timestamp with time zone,
  "finish_count" int4 DEFAULT 0,
  "label_user_id" int4,
  "selected_count" int4,
  "reject_count" int4,
  "selected" bool,
  "batch_no" int4 DEFAULT 0,
  "selected_batch_no" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."label_plan"."dataset_id" IS '数据集id';
COMMENT ON COLUMN "public"."label_plan"."plan_name" IS '计划名称';
COMMENT ON COLUMN "public"."label_plan"."first_project_user_id" IS '第一阶段标注项目分配员';
COMMENT ON COLUMN "public"."label_plan"."second_project_user_id" IS '第二阶段标注项目分配员';
COMMENT ON COLUMN "public"."label_plan"."third_project_user_id" IS '第三阶段标注项目分配员';
COMMENT ON COLUMN "public"."label_plan"."total_count" IS '数据集总数';
COMMENT ON COLUMN "public"."label_plan"."dataset_type" IS '数据类型';
COMMENT ON COLUMN "public"."label_plan"."plan_publisher_id" IS '计划发布员id';
COMMENT ON COLUMN "public"."label_plan"."category" IS '标注类别';
COMMENT ON COLUMN "public"."label_plan"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."label_plan"."finish_count" IS '完成数';
COMMENT ON COLUMN "public"."label_plan"."label_user_id" IS '标注员id';
COMMENT ON COLUMN "public"."label_plan"."selected_count" IS '抽取数量';
COMMENT ON COLUMN "public"."label_plan"."reject_count" IS '审核不通过数量';
COMMENT ON COLUMN "public"."label_plan"."batch_no" IS '上传批次';

-- ----------------------------
-- Primary Key structure for table label_plan
-- ----------------------------
ALTER TABLE "public"."label_plan" ADD CONSTRAINT "label_plan_pkey" PRIMARY KEY ("id");
