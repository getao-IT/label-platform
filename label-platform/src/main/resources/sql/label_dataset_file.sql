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

 Date: 18/08/2022 16:11:37
*/

CREATE SEQUENCE label_dataset_file_id_seq INCREMENT BY 1 START WITH 1 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for label_dataset_file
-- ----------------------------
DROP TABLE IF EXISTS "public"."label_dataset_file";
CREATE TABLE "public"."label_dataset_file" (
  "id" int4 NOT NULL DEFAULT nextval('label_dataset_file_id_seq'::regclass),
  "dataset_id" int4,
  "file_id" int4,
  "status" int4,
  "label" text COLLATE "pg_catalog"."default",
  "ai_label" text COLLATE "pg_catalog"."default",
  "feedback" varchar(255) COLLATE "pg_catalog"."default",
  "label_user_id" int4,
  "check_user_id" int4,
  "screenshot" text COLLATE "pg_catalog"."default",
  "assign_label_time" timestamp(6),
  "assign_check_time" timestamp(6),
  "finish_label_time" timestamp(6),
  "finish_check_time" timestamp(6),
  "commit_count" int4,
  "related_file_id" int4
)
;
COMMENT ON COLUMN "public"."label_dataset_file"."id" IS 'id';
COMMENT ON COLUMN "public"."label_dataset_file"."dataset_id" IS '数据集id';
COMMENT ON COLUMN "public"."label_dataset_file"."file_id" IS '影像id';
COMMENT ON COLUMN "public"."label_dataset_file"."status" IS '标注状态';
COMMENT ON COLUMN "public"."label_dataset_file"."label" IS '标注信息';
COMMENT ON COLUMN "public"."label_dataset_file"."ai_label" IS 'ai标注信息';
COMMENT ON COLUMN "public"."label_dataset_file"."feedback" IS '审核反馈信息';
COMMENT ON COLUMN "public"."label_dataset_file"."label_user_id" IS '标注员id';
COMMENT ON COLUMN "public"."label_dataset_file"."check_user_id" IS '审核员id';
COMMENT ON COLUMN "public"."label_dataset_file"."screenshot" IS '反馈截图信息';
COMMENT ON COLUMN "public"."label_dataset_file"."assign_label_time" IS '申领标注任务时间';
COMMENT ON COLUMN "public"."label_dataset_file"."assign_check_time" IS '申领审核任务时间';
COMMENT ON COLUMN "public"."label_dataset_file"."commit_count" IS '提交次数';
COMMENT ON COLUMN "public"."label_dataset_file"."related_file_id" IS '变化检测对比文件id';
