export interface BusinessFieldDefinition {
  key: string
  label: string
  type: 'text' | 'textarea' | 'date' | 'datetime-local' | 'number' | 'select'
  required?: boolean
  options?: Array<{ label: string; value: string }>
}

export interface BusinessModuleDefinition {
  key: string
  name: string
  domain: 'student-affairs' | 'academic' | 'research' | 'logistics'
  description: string
  createRoles: string[]
  visibleRoles: string[]
  fields: BusinessFieldDefinition[]
}

export const businessModules: BusinessModuleDefinition[] = [
  {
    key: 'internship-materials',
    name: '实习材料',
    domain: 'student-affairs',
    description: '提交实习协议和相关证明材料。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'ADMIN'],
    fields: [
      { key: 'semesterCode', label: '学期', type: 'text', required: true },
      { key: 'internshipCompany', label: '实习单位', type: 'text', required: true },
      { key: 'internshipPosition', label: '实习岗位', type: 'text', required: true },
      { key: 'tutorName', label: '指导老师', type: 'text', required: true },
      { key: 'tutorPhone', label: '指导老师电话', type: 'text', required: true },
      { key: 'startDate', label: '开始日期', type: 'date', required: true },
      { key: 'endDate', label: '结束日期', type: 'date', required: true },
      {
        key: 'materialType',
        label: '材料类型',
        type: 'select',
        required: true,
        options: [
          { label: '实习协议', value: '实习协议' },
          { label: '单位接收证明', value: '单位接收证明' },
          { label: '岗位说明', value: '岗位说明' }
        ]
      },
      { key: 'materialTitle', label: '材料标题', type: 'text', required: true },
      { key: 'materialSummary', label: '材料说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'abnormal-students',
    name: '异常学生',
    domain: 'student-affairs',
    description: '登记异常学生信息和干预情况。',
    createRoles: ['ADVISER', 'ADMIN'],
    visibleRoles: ['ADVISER', 'ADMIN'],
    fields: [
      { key: 'studentNo', label: '学号', type: 'text', required: true },
      { key: 'studentName', label: '姓名', type: 'text', required: true },
      {
        key: 'alertType',
        label: '异常类型',
        type: 'select',
        required: true,
        options: [
          { label: '学业预警', value: '学业预警' },
          { label: '心理预警', value: '心理预警' },
          { label: '纪律异常', value: '纪律异常' }
        ]
      },
      {
        key: 'alertLevel',
        label: '异常等级',
        type: 'select',
        required: true,
        options: [
          { label: '低', value: '低' },
          { label: '中', value: '中' },
          { label: '高', value: '高' }
        ]
      },
      { key: 'problemDescription', label: '问题描述', type: 'textarea', required: true },
      { key: 'interventionPlan', label: '干预措施', type: 'textarea', required: true },
      {
        key: 'followUpStatus',
        label: '跟进状态',
        type: 'select',
        required: true,
        options: [
          { label: '待跟进', value: '待跟进' },
          { label: '跟进中', value: '跟进中' },
          { label: '已闭环', value: '已闭环' }
        ]
      }
    ]
  },
  {
    key: 'research-projects',
    name: '课题申报',
    domain: 'research',
    description: '教师提交科研课题申报材料。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'RESEARCH', 'ADMIN'],
    fields: [
      { key: 'projectName', label: '课题名称', type: 'text', required: true },
      {
        key: 'projectCategory',
        label: '课题类别',
        type: 'select',
        required: true,
        options: [
          { label: '校级', value: '校级' },
          { label: '厅级', value: '厅级' },
          { label: '省级', value: '省级' }
        ]
      },
      { key: 'applicationYear', label: '申报年度', type: 'text', required: true },
      { key: 'projectLevel', label: '课题级别', type: 'text', required: true },
      { key: 'budgetAmount', label: '预算金额', type: 'number', required: true },
      { key: 'teamMembers', label: '团队成员', type: 'text', required: true },
      { key: 'projectSummary', label: '课题简介', type: 'textarea', required: true }
    ]
  },
  {
    key: 'course-standards',
    name: '课程标准',
    domain: 'academic',
    description: '提交课程标准和修订说明。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseCode', label: '课程编号', type: 'text', required: true },
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'academicYear', label: '学年', type: 'text', required: true },
      { key: 'targetMajor', label: '适用专业', type: 'text', required: true },
      { key: 'totalHours', label: '总学时', type: 'number', required: true },
      { key: 'standardVersion', label: '标准版本', type: 'text', required: true },
      { key: 'revisionNote', label: '修订说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'leave-applications',
    name: '请假',
    domain: 'student-affairs',
    description: '学生提交请假申请。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'ADMIN'],
    fields: [
      {
        key: 'leaveType',
        label: '请假类型',
        type: 'select',
        required: true,
        options: [
          { label: '事假', value: '事假' },
          { label: '病假', value: '病假' },
          { label: '外出请假', value: '外出请假' }
        ]
      },
      { key: 'startTime', label: '开始时间', type: 'datetime-local', required: true },
      { key: 'endTime', label: '结束时间', type: 'datetime-local', required: true },
      { key: 'reason', label: '请假原因', type: 'textarea', required: true },
      { key: 'emergencyContact', label: '紧急联系人', type: 'text', required: true },
      { key: 'emergencyPhone', label: '紧急联系电话', type: 'text', required: true },
      { key: 'destination', label: '去向', type: 'text', required: true }
    ]
  },
  {
    key: 'leave-cancellations',
    name: '销假',
    domain: 'student-affairs',
    description: '返校后提交销假说明。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'ADMIN'],
    fields: [
      { key: 'relatedLeaveNo', label: '关联请假单号', type: 'text', required: true },
      { key: 'returnTime', label: '返校时间', type: 'datetime-local', required: true },
      { key: 'cancelReason', label: '销假原因', type: 'textarea', required: true },
      { key: 'actualReturnNote', label: '返校说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'schedule-adjustments',
    name: '调课',
    domain: 'student-affairs',
    description: '教师提交调课申请。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'originalTime', label: '原上课时间', type: 'datetime-local', required: true },
      { key: 'adjustedTime', label: '调整后时间', type: 'datetime-local', required: true },
      { key: 'originalClassroom', label: '原教室', type: 'text', required: true },
      { key: 'adjustedClassroom', label: '调整后教室', type: 'text', required: true },
      { key: 'adjustmentReason', label: '调课原因', type: 'textarea', required: true }
    ]
  },
  {
    key: 'meeting-rooms',
    name: '会议室',
    domain: 'logistics',
    description: '提交会议室预约申请。',
    createRoles: ['TEACHER', 'ADVISER', 'OFFICE', 'RESEARCH', 'ADMIN'],
    visibleRoles: ['TEACHER', 'ADVISER', 'OFFICE', 'RESEARCH', 'ADMIN'],
    fields: [
      { key: 'meetingSubject', label: '会议主题', type: 'text', required: true },
      { key: 'roomName', label: '会议室', type: 'text', required: true },
      { key: 'startTime', label: '开始时间', type: 'datetime-local', required: true },
      { key: 'endTime', label: '结束时间', type: 'datetime-local', required: true },
      { key: 'attendeeCount', label: '参会人数', type: 'number', required: true },
      { key: 'equipmentNeeds', label: '设备需求', type: 'textarea', required: true },
      { key: 'contactName', label: '联系人', type: 'text', required: true },
      { key: 'contactPhone', label: '联系电话', type: 'text', required: true }
    ]
  },
  {
    key: 'dorm-repairs',
    name: '宿舍维修',
    domain: 'logistics',
    description: '学生提交宿舍维修申请。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADMIN'],
    fields: [
      { key: 'dormBuilding', label: '宿舍楼', type: 'text', required: true },
      { key: 'roomNo', label: '房间号', type: 'text', required: true },
      {
        key: 'repairType',
        label: '报修类型',
        type: 'select',
        required: true,
        options: [
          { label: '水电维修', value: '水电维修' },
          { label: '门窗维修', value: '门窗维修' },
          { label: '家具维修', value: '家具维修' },
          { label: '网络故障', value: '网络故障' }
        ]
      },
      { key: 'problemDescription', label: '问题描述', type: 'textarea', required: true },
      { key: 'contactPhone', label: '联系电话', type: 'text', required: true },
      {
        key: 'urgencyLevel',
        label: '紧急程度',
        type: 'select',
        required: true,
        options: [
          { label: '普通', value: '普通' },
          { label: '紧急', value: '紧急' }
        ]
      }
    ]
  },
  {
    key: 'asset-repairs',
    name: '资产报修',
    domain: 'logistics',
    description: '教师提交资产报修申请。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'ADMIN'],
    fields: [
      { key: 'assetNo', label: '资产编号', type: 'text', required: true },
      { key: 'assetName', label: '资产名称', type: 'text', required: true },
      { key: 'locationText', label: '所在位置', type: 'text', required: true },
      { key: 'faultDescription', label: '故障描述', type: 'textarea', required: true },
      {
        key: 'urgencyLevel',
        label: '紧急程度',
        type: 'select',
        required: true,
        options: [
          { label: '普通', value: '普通' },
          { label: '紧急', value: '紧急' }
        ]
      }
    ]
  },
  {
    key: 'scholarship-applications',
    name: '奖学金',
    domain: 'student-affairs',
    description: '提交奖学金申请材料。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'STUDENT_AFFAIRS', 'ADMIN'],
    fields: [
      {
        key: 'scholarshipType',
        label: '奖学金类型',
        type: 'select',
        required: true,
        options: [
          { label: '国家奖学金', value: '国家奖学金' },
          { label: '校级一等奖学金', value: '校级一等奖学金' },
          { label: '校级二等奖学金', value: '校级二等奖学金' }
        ]
      },
      { key: 'gradeRank', label: '成绩排名', type: 'text', required: true },
      { key: 'comprehensiveScore', label: '综合成绩', type: 'text', required: true },
      { key: 'awardRecords', label: '获奖记录', type: 'textarea', required: true },
      { key: 'familySituation', label: '家庭情况', type: 'textarea', required: true },
      { key: 'applicationReason', label: '申请原因', type: 'textarea', required: true }
    ]
  },
  {
    key: 'grant-applications',
    name: '助学金',
    domain: 'student-affairs',
    description: '提交助学金申请材料。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'STUDENT_AFFAIRS', 'ADMIN'],
    fields: [
      { key: 'familyIncome', label: '家庭收入', type: 'text', required: true },
      { key: 'householdSize', label: '家庭人口', type: 'number', required: true },
      {
        key: 'difficultyLevel',
        label: '困难等级',
        type: 'select',
        required: true,
        options: [
          { label: '一般困难', value: '一般困难' },
          { label: '困难', value: '困难' },
          { label: '特别困难', value: '特别困难' }
        ]
      },
      { key: 'applicationReason', label: '申请原因', type: 'textarea', required: true },
      { key: 'specialNotes', label: '补充说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'difficulty-recognitions',
    name: '困难认定',
    domain: 'student-affairs',
    description: '提交家庭经济困难认定申请。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'STUDENT_AFFAIRS', 'ADMIN'],
    fields: [
      { key: 'familyMembers', label: '家庭成员', type: 'textarea', required: true },
      { key: 'annualIncome', label: '年收入', type: 'text', required: true },
      { key: 'specialCondition', label: '特殊情况', type: 'textarea', required: true },
      {
        key: 'recognitionLevel',
        label: '认定等级',
        type: 'select',
        required: true,
        options: [
          { label: '一般困难', value: '一般困难' },
          { label: '困难', value: '困难' },
          { label: '特别困难', value: '特别困难' }
        ]
      },
      { key: 'applicationReason', label: '申请原因', type: 'textarea', required: true }
    ]
  },
  {
    key: 'enrollment-certificates',
    name: '在读证明',
    domain: 'student-affairs',
    description: '提交在读证明申请。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'ADMIN'],
    fields: [
      { key: 'certificatePurpose', label: '证明用途', type: 'text', required: true },
      { key: 'receiverOrg', label: '接收单位', type: 'text', required: true },
      {
        key: 'languageType',
        label: '语言类型',
        type: 'select',
        required: true,
        options: [
          { label: '中文', value: '中文' },
          { label: '英文', value: '英文' }
        ]
      },
      {
        key: 'deliveryMethod',
        label: '领取方式',
        type: 'select',
        required: true,
        options: [
          { label: '现场领取', value: '现场领取' },
          { label: '快递邮寄', value: '快递邮寄' }
        ]
      },
      { key: 'remarkText', label: '备注', type: 'textarea', required: true }
    ]
  },
  {
    key: 'textbook-orders',
    name: '教材征订',
    domain: 'academic',
    description: '教师提交教材征订申请。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'textbookName', label: '教材名称', type: 'text', required: true },
      { key: 'isbn', label: 'ISBN', type: 'text', required: true },
      { key: 'publisher', label: '出版社', type: 'text', required: true },
      { key: 'authorName', label: '作者', type: 'text', required: true },
      { key: 'classNames', label: '适用班级', type: 'text', required: true },
      { key: 'orderQuantity', label: '征订数量', type: 'number', required: true },
      { key: 'selectionReason', label: '选用理由', type: 'textarea', required: true }
    ]
  },
  {
    key: 'goods-borrow-applications',
    name: '物资借用',
    domain: 'logistics',
    description: '提交物资借用申请。',
    createRoles: ['STUDENT', 'TEACHER', 'ADMIN'],
    visibleRoles: ['STUDENT', 'TEACHER', 'ADMIN'],
    fields: [
      { key: 'itemName', label: '物资名称', type: 'text', required: true },
      { key: 'itemSpec', label: '物资规格', type: 'text', required: true },
      { key: 'borrowQuantity', label: '借用数量', type: 'number', required: true },
      { key: 'borrowStartTime', label: '借用开始时间', type: 'datetime-local', required: true },
      { key: 'borrowEndTime', label: '借用结束时间', type: 'datetime-local', required: true },
      { key: 'borrowPurpose', label: '借用用途', type: 'textarea', required: true },
      { key: 'returnPlan', label: '归还计划', type: 'textarea', required: true },
      { key: 'contactName', label: '联系人', type: 'text', required: true },
      { key: 'contactPhone', label: '联系电话', type: 'text', required: true },
      { key: 'remarks', label: '备注', type: 'textarea', required: true }
    ]
  },
  {
    key: 'course-suspension-applications',
    name: '停课申请',
    domain: 'academic',
    description: '教师提交停课申请。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'courseCode', label: '课程编号', type: 'text', required: true },
      { key: 'suspensionDate', label: '停课日期', type: 'date', required: true },
      { key: 'suspensionStartTime', label: '停课开始时间', type: 'datetime-local', required: true },
      { key: 'suspensionEndTime', label: '停课结束时间', type: 'datetime-local', required: true },
      { key: 'suspensionReason', label: '停课原因', type: 'textarea', required: true },
      { key: 'makeupSuggestion', label: '补课建议', type: 'textarea', required: true },
      { key: 'affectedClass', label: '受影响班级', type: 'text', required: true },
      { key: 'remarks', label: '备注', type: 'textarea', required: true }
    ]
  },
  {
    key: 'makeup-class-applications',
    name: '补课申请',
    domain: 'academic',
    description: '教师提交补课申请。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'courseCode', label: '课程编号', type: 'text', required: true },
      { key: 'makeupDate', label: '补课日期', type: 'date', required: true },
      { key: 'makeupStartTime', label: '补课开始时间', type: 'datetime-local', required: true },
      { key: 'makeupEndTime', label: '补课结束时间', type: 'datetime-local', required: true },
      { key: 'makeupLocation', label: '补课地点', type: 'text', required: true },
      { key: 'relatedSuspensionNo', label: '关联停课单号', type: 'text', required: true },
      { key: 'makeupReason', label: '补课原因', type: 'textarea', required: true },
      { key: 'affectedClass', label: '受影响班级', type: 'text', required: true },
      { key: 'noticePlan', label: '通知方案', type: 'textarea', required: true }
    ]
  },
  {
    key: 'research-midterm-checks',
    name: '课题中期检查',
    domain: 'research',
    description: '提交课题中期检查材料。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'RESEARCH', 'ADMIN'],
    fields: [
      { key: 'projectName', label: '课题名称', type: 'text', required: true },
      { key: 'projectNo', label: '课题编号', type: 'text', required: true },
      { key: 'progressRate', label: '执行进度', type: 'text', required: true },
      { key: 'stageOutcome', label: '阶段成果', type: 'textarea', required: true },
      { key: 'existingProblems', label: '存在问题', type: 'textarea', required: true },
      { key: 'correctionPlan', label: '整改计划', type: 'textarea', required: true },
      { key: 'budgetUsage', label: '经费使用说明', type: 'textarea', required: true },
      { key: 'remarks', label: '备注', type: 'textarea', required: true }
    ]
  },
  {
    key: 'research-completion-applications',
    name: '课题结题申请',
    domain: 'research',
    description: '提交课题结题申请材料。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'RESEARCH', 'ADMIN'],
    fields: [
      { key: 'projectName', label: '课题名称', type: 'text', required: true },
      { key: 'projectNo', label: '课题编号', type: 'text', required: true },
      { key: 'achievements', label: '成果列表', type: 'textarea', required: true },
      { key: 'fundingUsage', label: '经费说明', type: 'textarea', required: true },
      { key: 'completionReport', label: '结题报告', type: 'textarea', required: true },
      { key: 'expertList', label: '专家名单', type: 'textarea', required: true },
      { key: 'conclusionNote', label: '结论说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'lesson-plan-submissions',
    name: '教案提交',
    domain: 'academic',
    description: '教师提交课程教案。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'semesterCode', label: '学期', type: 'text', required: true },
      { key: 'chapterRange', label: '章节范围', type: 'text', required: true },
      { key: 'versionNo', label: '版本号', type: 'text', required: true },
      { key: 'lessonPlanTitle', label: '教案标题', type: 'text', required: true },
      { key: 'lessonPlanSummary', label: '教案说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'teaching-outline-submissions',
    name: '教学大纲提交',
    domain: 'academic',
    description: '教师提交课程大纲。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'academicYear', label: '学年', type: 'text', required: true },
      { key: 'targetMajor', label: '适用专业', type: 'text', required: true },
      { key: 'versionNo', label: '版本号', type: 'text', required: true },
      { key: 'revisionNote', label: '修订说明', type: 'textarea', required: true },
      { key: 'outlineSummary', label: '大纲说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'grade-correction-applications',
    name: '成绩更正申请',
    domain: 'academic',
    description: '教师提交成绩更正。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'courseCode', label: '课程编号', type: 'text', required: true },
      { key: 'studentNo', label: '学号', type: 'text', required: true },
      { key: 'studentName', label: '学生姓名', type: 'text', required: true },
      { key: 'originalGrade', label: '原成绩', type: 'text', required: true },
      { key: 'newGrade', label: '新成绩', type: 'text', required: true },
      { key: 'correctionReason', label: '更正原因', type: 'textarea', required: true },
      { key: 'proofMaterials', label: '证明材料', type: 'textarea', required: true }
    ]
  },
  {
    key: 'exam-schedule-applications',
    name: '考试安排申请',
    domain: 'academic',
    description: '提交考试安排需求。',
    createRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'courseName', label: '课程名称', type: 'text', required: true },
      { key: 'className', label: '班级', type: 'text', required: true },
      { key: 'examCount', label: '考试人数', type: 'number', required: true },
      { key: 'examTimeSuggestion', label: '考试时间建议', type: 'text', required: true },
      { key: 'classroomNeed', label: '教室需求', type: 'text', required: true },
      { key: 'invigilatorNeed', label: '监考需求', type: 'text', required: true },
      { key: 'remarks', label: '备注', type: 'textarea', required: true }
    ]
  },
  {
    key: 'classroom-borrow-applications',
    name: '教室借用申请',
    domain: 'academic',
    description: '提交教室借用申请。',
    createRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    visibleRoles: ['TEACHER', 'OFFICE', 'ADMIN'],
    fields: [
      { key: 'classroomName', label: '教室名称', type: 'text', required: true },
      { key: 'borrowDate', label: '借用日期', type: 'date', required: true },
      { key: 'borrowStartTime', label: '借用开始时间', type: 'datetime-local', required: true },
      { key: 'borrowEndTime', label: '借用结束时间', type: 'datetime-local', required: true },
      { key: 'borrowPurpose', label: '借用用途', type: 'textarea', required: true },
      { key: 'attendeeCount', label: '预计人数', type: 'number', required: true },
      { key: 'equipmentNeeds', label: '设备需求', type: 'textarea', required: true },
      { key: 'contactName', label: '联系人', type: 'text', required: true },
      { key: 'contactPhone', label: '联系电话', type: 'text', required: true }
    ]
  },
  {
    key: 'student-leave-applications',
    name: '学生离校申请',
    domain: 'student-affairs',
    description: '提交学生离校申请。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'STUDENT_AFFAIRS', 'ADMIN'],
    fields: [
      {
        key: 'leaveType',
        label: '离校类型',
        type: 'select',
        required: true,
        options: [
          { label: '短期离校', value: '短期离校' },
          { label: '长期离校', value: '长期离校' }
        ]
      },
      { key: 'leaveStartTime', label: '离校开始时间', type: 'datetime-local', required: true },
      { key: 'leaveEndTime', label: '离校结束时间', type: 'datetime-local', required: true },
      { key: 'leaveDestination', label: '离校去向', type: 'text', required: true },
      { key: 'leaveReason', label: '离校原因', type: 'textarea', required: true },
      { key: 'emergencyContact', label: '紧急联系人', type: 'text', required: true },
      { key: 'emergencyPhone', label: '紧急联系电话', type: 'text', required: true },
      { key: 'returnPlan', label: '返校计划', type: 'textarea', required: true }
    ]
  },
  {
    key: 'student-return-confirmations',
    name: '学生返校确认',
    domain: 'student-affairs',
    description: '提交学生返校确认。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'STUDENT_AFFAIRS', 'ADMIN'],
    fields: [
      { key: 'relatedLeaveNo', label: '关联离校单号', type: 'select', required: true, options: [] },
      { key: 'returnTime', label: '返校时间', type: 'datetime-local', required: true },
      { key: 'returnNote', label: '返校说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'graduation-project-openings',
    name: '毕业设计开题申请',
    domain: 'student-affairs',
    description: '提交毕业设计开题材料。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'TEACHER', 'ADMIN'],
    fields: [
      { key: 'projectName', label: '课题名称', type: 'text', required: true },
      { key: 'topicDirection', label: '研究方向', type: 'text', required: true },
      { key: 'advisorName', label: '指导老师', type: 'select', required: true, options: [] },
      { key: 'openingDate', label: '开题日期', type: 'date', required: true },
      { key: 'openingSummary', label: '开题说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'graduation-project-midterms',
    name: '毕业设计中期检查',
    domain: 'student-affairs',
    description: '提交毕业设计中期检查。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'TEACHER', 'ADMIN'],
    fields: [
      { key: 'projectName', label: '课题名称', type: 'select', required: true, options: [] },
      { key: 'progressRate', label: '进度比例', type: 'number', required: true },
      { key: 'midtermDate', label: '检查日期', type: 'date', required: true },
      { key: 'problemsFound', label: '发现问题', type: 'textarea', required: true },
      { key: 'rectificationPlan', label: '整改计划', type: 'textarea', required: true },
      { key: 'midtermSummary', label: '中期说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'research-achievement-registrations',
    name: '科研成果登记',
    domain: 'research',
    description: '登记科研成果信息。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'RESEARCH', 'ADMIN'],
    fields: [
      { key: 'achievementName', label: '成果名称', type: 'text', required: true },
      {
        key: 'achievementType',
        label: '成果类型',
        type: 'select',
        required: true,
        options: [
          { label: '论文', value: '论文' },
          { label: '专利', value: '专利' },
          { label: '著作', value: '著作' },
          { label: '获奖', value: '获奖' }
        ]
      },
      { key: 'publishTime', label: '登记日期', type: 'date', required: true },
      { key: 'issueUnit', label: '发布单位', type: 'text', required: true },
      { key: 'achievementLevel', label: '成果级别', type: 'text', required: true },
      { key: 'achievementSummary', label: '成果说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'academic-lecture-applications',
    name: '学术讲座申请',
    domain: 'research',
    description: '提交学术讲座申请和备案信息。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'RESEARCH', 'ADMIN'],
    fields: [
      { key: 'lectureTopic', label: '讲座主题', type: 'text', required: true },
      { key: 'speakerName', label: '主讲人', type: 'text', required: true },
      { key: 'lectureTime', label: '讲座时间', type: 'datetime-local', required: true },
      { key: 'lectureLocation', label: '讲座地点', type: 'text', required: true },
      { key: 'audienceScope', label: '听众范围', type: 'text', required: true },
      { key: 'budgetAmount', label: '预算金额', type: 'number', required: true },
      { key: 'attachmentNote', label: '附件说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'office-supply-requests',
    name: '办公用品申领',
    domain: 'logistics',
    description: '提交办公用品申领和用途说明。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'ADMIN'],
    fields: [
      { key: 'itemName', label: '用品名称', type: 'text', required: true },
      { key: 'requestQuantity', label: '申领数量', type: 'number', required: true },
      { key: 'usagePurpose', label: '用途说明', type: 'textarea', required: true },
      { key: 'departmentName', label: '使用部门', type: 'text', required: true },
      { key: 'remarks', label: '备注', type: 'textarea', required: true }
    ]
  },
  {
    key: 'lab-safety-hazard-reports',
    name: '实验室安全隐患上报',
    domain: 'logistics',
    description: '上报实验室安全隐患、风险等级和整改要求。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'ADMIN'],
    fields: [
      { key: 'labName', label: '实验室名称', type: 'text', required: true },
      {
        key: 'hazardType',
        label: '隐患类型',
        type: 'select',
        required: true,
        options: [
          { label: '设备安全', value: '设备安全' },
          { label: '化学品', value: '化学品' },
          { label: '用电安全', value: '用电安全' },
          { label: '消防安全', value: '消防安全' },
          { label: '其他', value: '其他' }
        ]
      },
      { key: 'hazardDescription', label: '隐患描述', type: 'textarea', required: true },
      {
        key: 'riskLevel',
        label: '风险等级',
        type: 'select',
        required: true,
        options: [
          { label: '低', value: '低' },
          { label: '中', value: '中' },
          { label: '高', value: '高' }
        ]
      },
      { key: 'rectificationRequirement', label: '整改要求', type: 'textarea', required: true },
      { key: 'attachmentNote', label: '附件说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'dorm-adjustment-requests',
    name: '宿舍调宿申请',
    domain: 'student-affairs',
    description: '学生提交宿舍调宿申请。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'ADMIN'],
    fields: [
      { key: 'studentInfo', label: '学生信息', type: 'text', required: true },
      { key: 'currentDormitory', label: '当前宿舍', type: 'text', required: true },
      { key: 'targetDormitory', label: '目标宿舍', type: 'text', required: true },
      { key: 'adjustmentReason', label: '调宿原因', type: 'textarea', required: true },
      { key: 'remarks', label: '备注', type: 'textarea', required: true }
    ]
  },
  {
    key: 'stamp-requests',
    name: '用章申请',
    domain: 'logistics',
    description: '提交用章事项、印章类型和文件份数。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'ADMIN'],
    fields: [
      { key: 'requestSubject', label: '用章事项', type: 'text', required: true },
      {
        key: 'sealType',
        label: '印章类型',
        type: 'select',
        required: true,
        options: [
          { label: '公章', value: '公章' },
          { label: '合同章', value: '合同章' },
          { label: '财务章', value: '财务章' },
          { label: '学院章', value: '学院章' },
          { label: '其他', value: '其他' }
        ]
      },
      { key: 'usageTime', label: '用章时间', type: 'datetime-local', required: true },
      { key: 'documentName', label: '文件名称', type: 'text', required: true },
      { key: 'documentCount', label: '文件份数', type: 'number', required: true },
      { key: 'attachmentNote', label: '附件说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'vehicle-requests',
    name: '车辆申请',
    domain: 'logistics',
    description: '提交公务用车时间、目的地和调度要求。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'ADMIN'],
    fields: [
      { key: 'useStartTime', label: '用车开始时间', type: 'datetime-local', required: true },
      { key: 'useEndTime', label: '用车结束时间', type: 'datetime-local', required: true },
      { key: 'destination', label: '目的地', type: 'text', required: true },
      { key: 'passengerCount', label: '乘车人数', type: 'number', required: true },
      { key: 'useReason', label: '用车事由', type: 'textarea', required: true },
      { key: 'contactName', label: '联系人', type: 'text', required: true },
      { key: 'contactPhone', label: '联系电话', type: 'text', required: true },
      { key: 'dispatchRequirement', label: '调度要求', type: 'textarea', required: true }
    ]
  },
  {
    key: 'class-notice-receipts',
    name: '班级通知回执',
    domain: 'student-affairs',
    description: '发布班级通知并记录回执统计。',
    createRoles: ['ADVISER', 'ADMIN'],
    visibleRoles: ['ADVISER', 'STUDENT', 'ADMIN'],
    fields: [
      { key: 'targetClass', label: '通知班级', type: 'text', required: true },
      { key: 'noticeTitle', label: '通知标题', type: 'text', required: true },
      { key: 'noticeContent', label: '通知内容', type: 'textarea', required: true },
      { key: 'deadlineTime', label: '回执截止时间', type: 'datetime-local', required: true },
      {
        key: 'receiptRequired',
        label: '是否需要回执',
        type: 'select',
        required: true,
        options: [
          { label: '是', value: '是' },
          { label: '否', value: '否' }
        ]
      },
      { key: 'expectedReceiptCount', label: '应回执人数', type: 'number', required: true },
      { key: 'receivedReceiptCount', label: '已回执人数', type: 'number', required: true },
      { key: 'attachmentNote', label: '附件说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'student-warning-processes',
    name: '学生预警处理',
    domain: 'student-affairs',
    description: '登记学生预警、处理记录和跟进计划。',
    createRoles: ['ADVISER', 'ADMIN'],
    visibleRoles: ['ADVISER', 'STUDENT_AFFAIRS', 'ADMIN'],
    fields: [
      { key: 'studentNo', label: '学号', type: 'text', required: true },
      { key: 'studentName', label: '学生姓名', type: 'text', required: true },
      {
        key: 'warningType',
        label: '预警类型',
        type: 'select',
        required: true,
        options: [
          { label: '学业预警', value: '学业预警' },
          { label: '心理预警', value: '心理预警' },
          { label: '纪律预警', value: '纪律预警' },
          { label: '就业预警', value: '就业预警' }
        ]
      },
      {
        key: 'warningLevel',
        label: '预警等级',
        type: 'select',
        required: true,
        options: [
          { label: '低', value: '低' },
          { label: '中', value: '中' },
          { label: '高', value: '高' }
        ]
      },
      { key: 'triggerReason', label: '触发原因', type: 'textarea', required: true },
      { key: 'processRecord', label: '处理记录', type: 'textarea', required: true },
      { key: 'followUpPlan', label: '跟进计划', type: 'textarea', required: true },
      { key: 'attachmentNote', label: '附件说明', type: 'textarea', required: true }
    ]
  },
  {
    key: 'material-supplements',
    name: '证明材料补交',
    domain: 'student-affairs',
    description: '补交被退回或缺失的证明材料。',
    createRoles: ['STUDENT', 'ADMIN'],
    visibleRoles: ['STUDENT', 'ADVISER', 'ADMIN'],
    fields: [
      { key: 'relatedBusinessNo', label: '关联业务单号', type: 'text', required: true },
      { key: 'returnReason', label: '退回原因', type: 'textarea', required: true },
      { key: 'supplementDescription', label: '补交说明', type: 'textarea', required: true },
      { key: 'supplementMaterialNote', label: '补交材料说明', type: 'textarea', required: true },
      { key: 'originalReviewer', label: '原审批人', type: 'text', required: true }
    ]
  },
  {
    key: 'announcement-publishes',
    name: '公告发布',
    domain: 'logistics',
    description: '提交公告标题、范围、发布时间和附件说明。',
    createRoles: ['TEACHER', 'ADMIN'],
    visibleRoles: ['TEACHER', 'ADVISER', 'OFFICE', 'RESEARCH', 'STUDENT_AFFAIRS', 'ADMIN'],
    fields: [
      { key: 'announcementTitle', label: '公告标题', type: 'text', required: true },
      { key: 'announcementContent', label: '公告内容', type: 'textarea', required: true },
      {
        key: 'publishScope',
        label: '发布范围',
        type: 'select',
        required: true,
        options: [
          { label: '全校', value: '全校' },
          { label: '学院', value: '学院' },
          { label: '班级', value: '班级' },
          { label: '部门', value: '部门' }
        ]
      },
      { key: 'plannedPublishTime', label: '计划发布时间', type: 'datetime-local', required: true },
      {
        key: 'topFlag',
        label: '是否置顶',
        type: 'select',
        required: true,
        options: [
          { label: '否', value: '否' },
          { label: '是', value: '是' }
        ]
      },
      { key: 'attachmentNote', label: '附件说明', type: 'textarea', required: true }
    ]
  }
]

export function getBusinessModuleByKey(key: string): BusinessModuleDefinition | undefined {
  return businessModules.find((item) => item.key === key)
}

export function getModulesByDomain(domain: BusinessModuleDefinition['domain']) {
  return businessModules.filter((item) => item.domain === domain)
}

export function getBusinessModule(key: string): BusinessModuleDefinition | undefined {
  return getBusinessModuleByKey(key)
}

export function getBusinessModulesByDomain(domain: string): BusinessModuleDefinition[] {
  return businessModules.filter((item) => item.domain === domain)
}

export function canCreateBusinessModule(module: BusinessModuleDefinition, roles: string[]): boolean {
  return roles.some((role) => module.createRoles.includes(role) || role === 'ADMIN')
}

export function canViewBusinessModule(module: BusinessModuleDefinition, roles: string[]): boolean {
  return roles.some((role) => module.visibleRoles.includes(role) || role === 'ADMIN')
}
