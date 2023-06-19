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

 Date: 18/08/2022 16:12:15
*/

CREATE SEQUENCE label_task_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_task";
CREATE TABLE "public"."label_task" (
  "id" int4 NOT NULL DEFAULT nextval('label_task_id_seq'::regclass),
  "user_id" int4,
  "label_project_id" int4,
  "total_count" int4,
  "finish_count" int4,
  "publisher_id" int4,
  "task_type" int4,
  "label_dataset_id" int4 DEFAULT 0,
  "processing_list" text COLLATE "pg_catalog"."default",
  "default_apply_count" int4,
  "keywords" text COLLATE "pg_catalog"."default",
  "make_sample_set_num" int4
)
;
COMMENT ON COLUMN "public"."label_task"."id" IS 'id';
COMMENT ON COLUMN "public"."label_task"."user_id" IS '标注员id';
COMMENT ON COLUMN "public"."label_task"."label_project_id" IS '标注项目id';
COMMENT ON COLUMN "public"."label_task"."total_count" IS '标注总数';
COMMENT ON COLUMN "public"."label_task"."finish_count" IS '已完成数量';
COMMENT ON COLUMN "public"."label_task"."publisher_id" IS '发布员id';
COMMENT ON COLUMN "public"."label_task"."task_type" IS '任务类型：审核、标注';
COMMENT ON COLUMN "public"."label_task"."label_dataset_id" IS '关联的数据集id';
COMMENT ON COLUMN "public"."label_task"."processing_list" IS '正在处理的影像idlist表';
COMMENT ON COLUMN "public"."label_task"."default_apply_count" IS '默认申请数量';
COMMENT ON COLUMN "public"."label_task"."keywords" IS '关键字';
COMMENT ON COLUMN "public"."label_task"."make_sample_set_num" IS '样本产出量';

-- ----------------------------
-- Primary Key structure for table label_task
-- ----------------------------
ALTER TABLE "public"."label_task" ADD CONSTRAINT "label_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table label_task
-- ----------------------------
ALTER TABLE "public"."label_task" ADD CONSTRAINT "fk_label_user_task_1" FOREIGN KEY ("label_project_id") REFERENCES "public"."label_project" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
