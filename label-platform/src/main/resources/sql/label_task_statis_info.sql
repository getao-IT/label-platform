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

 Date: 18/08/2022 16:12:21
*/

CREATE SEQUENCE label_task_statis_info_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_task_statis_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_task_statis_info";
CREATE TABLE "public"."label_task_statis_info" (
  "id" int4 NOT NULL DEFAULT nextval('label_task_statis_info_id_seq'::regclass),
  "label_project_id" int4 DEFAULT 0,
  "user_id" int4,
  "user_role" int4,
  "user_name" varchar(255) COLLATE "pg_catalog"."default",
  "apply_file_count" int4 DEFAULT 0,
  "commit_count" int4 DEFAULT 0,
  "commit_object_count" int4 DEFAULT 0,
  "finish_count" int4 DEFAULT 0,
  "time_consume" float4 DEFAULT 0,
  "refuse_count" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."label_task_statis_info"."id" IS 'id';
COMMENT ON COLUMN "public"."label_task_statis_info"."label_project_id" IS '标注项目id';
COMMENT ON COLUMN "public"."label_task_statis_info"."user_id" IS '标注人员id';
COMMENT ON COLUMN "public"."label_task_statis_info"."user_role" IS '标注人员类型（0：标注员 1：审核员）';
COMMENT ON COLUMN "public"."label_task_statis_info"."user_name" IS '标注人员姓名';
COMMENT ON COLUMN "public"."label_task_statis_info"."apply_file_count" IS '已申领标注任务总数';
COMMENT ON COLUMN "public"."label_task_statis_info"."commit_count" IS '已提交审核次数，提交一次，累加';
COMMENT ON COLUMN "public"."label_task_statis_info"."commit_object_count" IS '提交审核目标数量';
COMMENT ON COLUMN "public"."label_task_statis_info"."finish_count" IS '已完成标注任务个数，审核通过时，更新其值';
COMMENT ON COLUMN "public"."label_task_statis_info"."time_consume" IS '完成标注任务耗时，审核通过时，更新其值';
COMMENT ON COLUMN "public"."label_task_statis_info"."refuse_count" IS '审核不通过次数，审核拒绝时，累加';

-- ----------------------------
-- Primary Key structure for table label_task_statis_info
-- ----------------------------
ALTER TABLE "public"."label_task_statis_info" ADD CONSTRAINT "label_task_statis_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table label_task_statis_info
-- ----------------------------
ALTER TABLE "public"."label_task_statis_info" ADD CONSTRAINT "fk_label_user_task_1" FOREIGN KEY ("label_project_id") REFERENCES "public"."label_project" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
