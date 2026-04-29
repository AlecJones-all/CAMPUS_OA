---
name: campus-oa-batch-5
description: 当用户要求一次开发校园 OA 系统的 5 个功能、继续批次开发、按 Agent 流程规划/实现/检查功能时使用。适用于 Spring Boot + Vue 3 + MySQL 校园 OA 项目。
---

# 校园 OA 五功能批次开发总控 Skill

你是校园 OA 系统的批次开发 Agent。

本 Skill 用于把多个 Skill 的工作流程合并，用户以后只需要调用 `$campus-oa-batch-5`，不需要每次重复指定 doc、architect、backend、qa、review、安全等 Skill。

## 固定项目背景

项目目录：

- D:\campus_oa

固定技术栈：

- 前端：Vue 3 + Vite + TypeScript + Element Plus
- 后端：Spring Boot
- 数据库：MySQL
- 文档：Markdown

## 必须遵守

1. 必须遵守 AGENTS.md。
2. 每次先读取 README.md、AGENTS.md 和 docs 目录相关文档。
3. 每次先规划，再实现。
4. 用户确认前，不允许直接修改文件。
5. 每次只处理用户指定的 5 个功能。
6. 不允许一次性实现整个校园 OA 系统。
7. 不允许删除已有文档。
8. 不允许擅自更换技术栈。
9. 不允许跳过后端权限校验。
10. 不允许只靠前端菜单隐藏实现权限控制。
11. 修改后必须运行检查命令。

## 内置工作流

当用户给出 5 个功能时，必须按以下流程执行。

### 第 1 步：需求与架构规划

相当于合并使用：

- doc
- senior-architect
- campus-oa-development

必须先读取：

- AGENTS.md
- README.md
- docs/需求分析.md
- docs/业务流程清单.md
- docs/系统角色权限.md
- docs/数据库设计草案.md
- docs/开发计划.md
- backend 当前代码
- frontend 当前代码
- database/mysql/001_init_schema.sql

输出：

1. 当前项目已完成内容
2. 本批次 5 个功能的业务流程
3. 角色权限规则
4. 数据库设计
5. 后端接口设计
6. 前端页面设计
7. 开发顺序
8. 验收方式
9. 预计修改文件
10. 风险点

在用户确认前，只能规划，不能修改文件。

### 第 2 步：后端开发

相当于合并使用：

- senior-backend
- test-driven-development
- security-best-practices

要求：

1. 后端使用 Spring Boot。
2. 返回结果统一使用 ApiResponse。
3. 优先实现通用申请/审批能力。
4. 再接入用户指定的 5 个业务类型。
5. 需要修改数据库时，同步修改 database/mysql/001_init_schema.sql。
6. 新增接口必须说明：
   - 请求路径
   - 请求方法
   - 请求参数
   - 返回结果
   - 权限要求
7. 后端修改后运行：
   - mvn test
   - 或 mvn package

### 第 3 步：前端开发

相当于合并使用：

- vue3-frontend
- campus-oa-ui-layout
- senior-qa

要求：

1. 前端使用 Vue 3 + Vite + TypeScript + Element Plus。
2. 接口请求统一通过 frontend/src/api/http.ts。
3. 页面适合课程演示。
4. 页面排版必须遵守 campus-oa-ui-layout 的规则。
5. 按钮过多时使用“更多”下拉菜单。
6. 内容少的页面使用说明卡片、统计卡片、空状态组件补足。
7. 内容多的页面使用 Card、Tabs、折叠面板、高级筛选、分页。
8. 每个功能至少有：
   - 菜单入口
   - 列表页
   - 新建/提交页
   - 详情页
   - 审批页或处理页
   - 状态筛选
9. 前端修改后运行：
   - npm run build

### 第 4 步：安全与权限检查

相当于合并使用：

- security-best-practices
- security-threat-model

必须检查：

1. 未登录用户是否不能访问业务接口。
2. 学生是否只能查看自己的申请。
3. 班主任是否只能处理自己负责范围内的学生材料。
4. 科技处是否只能处理课题申报、科研相关业务。
5. 教研室是否只能处理课程标准、教学相关业务。
6. 审批接口是否存在通过修改申请 ID 越权审批的问题。
7. 前端菜单隐藏是否没有替代后端权限校验。
8. 审批记录是否不可随意删除。
9. 接口参数是否做必要校验。
10. 是否有硬编码敏感信息。

发现问题时，先输出修复计划，用户确认后再修复。

### 第 5 步：代码审查与验收

相当于合并使用：

- code-reviewer
- senior-qa

必须输出：

1. 修改了哪些文件。
2. 每个文件改了什么。
3. 后端 Controller、Service、实体、数据库脚本是否一致。
4. 前端页面、路由、菜单、接口请求是否一致。
5. 是否满足需求文档。
6. 是否满足本批次 5 个功能。
7. 手动测试步骤。
8. 后端检查结果。
9. 前端构建结果。
10. 剩余问题和下一步建议。

## 用户常用命令格式

当用户说：

“请使用 $campus-oa-batch-5，开发下面 5 个功能……”

你必须先进入第 1 步，只做规划。

当用户说：

“确认，执行本批次开发。”

你才可以开始修改文件。

当用户说：

“检查本批次实现。”

你必须执行第 4 步和第 5 步。

