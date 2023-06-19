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

 Date: 18/08/2022 16:12:05
*/

CREATE SEQUENCE label_project_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_project
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_project";
CREATE TABLE "public"."label_project" (
  "id" int4 NOT NULL DEFAULT nextval('label_project_id_seq'::regclass),
  "project_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "project_description" varchar(255) COLLATE "pg_catalog"."default",
  "dataset_id" int4,
  "status" int4,
  "category" varchar(255) COLLATE "pg_catalog"."default",
  "label" varchar(255) COLLATE "pg_catalog"."default" DEFAULT 0,
  "user_id" int4,
  "label_user_ids" varchar(255) COLLATE "pg_catalog"."default",
  "check_user_ids" varchar(255) COLLATE "pg_catalog"."default",
  "default_label_count" varchar(255) COLLATE "pg_catalog"."default",
  "default_check_count" varchar(255) COLLATE "pg_catalog"."default",
  "related_dataset_id" varchar(255) COLLATE "pg_catalog"."default",
  "publisher_name" varchar(255) COLLATE "pg_catalog"."default",
  "total_count" int4,
  "finish_count" int4 DEFAULT 0,
  "keywords" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "service_id" varchar(255) COLLATE "pg_catalog"."default",
  "is_ai_label" bool,
  "use_label" bool,
  "dataset_type" varchar(255) COLLATE "pg_catalog"."default",
  "make_sample_set_num" int4,
  "refuse_count" int4,
  "commit_count" int4,
  "finish_time" timestamp(6),
  "consume_time" varchar(255) COLLATE "pg_catalog"."default",
  "refuse_rate" numeric(32,2)
)
;
COMMENT ON COLUMN "public"."label_project"."id" IS 'id';
COMMENT ON COLUMN "public"."label_project"."project_name" IS '项目名称';
COMMENT ON COLUMN "public"."label_project"."project_description" IS '项目描述';
COMMENT ON COLUMN "public"."label_project"."dataset_id" IS '关联数据集id';
COMMENT ON COLUMN "public"."label_project"."status" IS '状态';
COMMENT ON COLUMN "public"."label_project"."category" IS '分类';
COMMENT ON COLUMN "public"."label_project"."label" IS '标签';
COMMENT ON COLUMN "public"."label_project"."user_id" IS '发布人';
COMMENT ON COLUMN "public"."label_project"."label_user_ids" IS '标注员列表';
COMMENT ON COLUMN "public"."label_project"."check_user_ids" IS '审核员列表';
COMMENT ON COLUMN "public"."label_project"."default_label_count" IS '每次最多标注申请数量';
COMMENT ON COLUMN "public"."label_project"."default_check_count" IS '每次最多审核申请数量';
COMMENT ON COLUMN "public"."label_project"."related_dataset_id" IS '关联基础数据集';
COMMENT ON COLUMN "public"."label_project"."publisher_name" IS '发布者姓名';
COMMENT ON COLUMN "public"."label_project"."total_count" IS '关联影像总数';
COMMENT ON COLUMN "public"."label_project"."finish_count" IS '已完成标注数量';
COMMENT ON COLUMN "public"."label_project"."keywords" IS '关键字';
COMMENT ON COLUMN "public"."label_project"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."label_project"."service_id" IS '算法服务id';
COMMENT ON COLUMN "public"."label_project"."is_ai_label" IS '是否需要AI标注';
COMMENT ON COLUMN "public"."label_project"."use_label" IS '是否使用数据集已有的标注信息';
COMMENT ON COLUMN "public"."label_project"."dataset_type" IS '标注项目类型';
COMMENT ON COLUMN "public"."label_project"."make_sample_set_num" IS '样本产出量';
COMMENT ON COLUMN "public"."label_project"."refuse_count" IS '审核驳回次数';
COMMENT ON COLUMN "public"."label_project"."commit_count" IS '提交审核次数';
COMMENT ON COLUMN "public"."label_project"."finish_time" IS '标注任务完成时间';
COMMENT ON COLUMN "public"."label_project"."consume_time" IS '标注任务完成耗时';
COMMENT ON COLUMN "public"."label_project"."refuse_rate" IS '审核驳回率';

-- ----------------------------
-- Primary Key structure for table label_project
-- ----------------------------
ALTER TABLE "public"."label_project" ADD CONSTRAINT "label_project_pkey" PRIMARY KEY ("id");
