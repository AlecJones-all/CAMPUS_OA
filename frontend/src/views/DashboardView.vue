<template>
  <div class="app-page">
    <PageHero
      eyebrow="工作台"
      title="校园 OA 工作台"
      description="查看待办审批、我的申请和常用业务入口。"
    >
      <template #meta>
        <SummaryStats :items="heroStats" />
      </template>
    </PageHero>

    <div class="split-layout">
      <div class="app-page">
        <SectionCard title="常用入口" description="进入常用审批和业务办理。">
          <div class="module-grid">
            <article v-for="entry in quickEntries" :key="entry.title" class="module-card">
              <div>
                <span class="module-card__badge">{{ entry.badge }}</span>
                <h3>{{ entry.title }}</h3>
                <p>{{ entry.description }}</p>
              </div>
              <div class="module-card__footer">
                <span class="muted-text">{{ entry.hint }}</span>
                <div class="module-card__actions">
                  <RouterLink :to="entry.to">
                    <el-button type="primary">{{ entry.action }}</el-button>
                  </RouterLink>
                </div>
              </div>
            </article>
          </div>
        </SectionCard>

        <SectionCard title="业务概览" description="按事务类别查看可办理业务。">
          <div class="three-column-grid">
            <article v-for="domain in businessDomains" :key="domain.label" class="field-card">
              <span>{{ domain.label }}</span>
              <strong>{{ domain.title }}</strong>
              <p class="muted-text">{{ domain.description }}</p>
            </article>
          </div>
        </SectionCard>
      </div>

      <div class="app-page">
        <SectionCard title="近期申请" description="查看最近提交的申请状态。">
          <template #actions>
            <RouterLink :to="{ name: 'workflow-applications' }">
              <el-button plain>查看全部</el-button>
            </RouterLink>
          </template>

          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
          <p v-else-if="loading" class="muted-text">正在加载申请记录...</p>

          <div v-else-if="recentApplications.length > 0" class="list-shell">
            <article v-for="item in recentApplications" :key="item.id" class="record-card">
              <div class="record-card__main">
                <div class="record-card__title">
                  <h3>{{ item.title }}</h3>
                  <StatusTag :status="item.status" :label="statusLabel[item.status] ?? item.status" />
                </div>
                <p>{{ item.typeName }} / 单号 {{ item.applicationNo }}</p>
                <p>提交时间：{{ item.submittedAt || '-' }}</p>
              </div>
              <div class="record-card__actions">
                <RouterLink :to="{ name: 'workflow-detail', params: { id: item.id } }">
                  <el-button type="primary" plain>查看</el-button>
                </RouterLink>
              </div>
            </article>
          </div>

          <EmptyStateBlock
            v-else
            title="暂无申请记录"
            description="可从常用入口发起业务申请。"
            badge="申请"
          />
        </SectionCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { listMyWorkflowApplications, listWorkflowTodos, type WorkflowApplicationSummary } from '../api/http';
import { useAppStore } from '../stores/app';
import EmptyStateBlock from '../components/EmptyStateBlock.vue';
import PageHero from '../components/PageHero.vue';
import SectionCard from '../components/SectionCard.vue';
import StatusTag from '../components/StatusTag.vue';
import SummaryStats from '../components/SummaryStats.vue';

const appStore = useAppStore();
const loading = ref(false);
const errorMessage = ref('');
const todos = ref<WorkflowApplicationSummary[]>([]);
const applications = ref<WorkflowApplicationSummary[]>([]);

const statusLabel: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '待审批',
  IN_PROGRESS: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回'
};

const heroStats = computed(() => [
  { label: '我的待办', value: todos.value.length, hint: '需要处理的审批' },
  { label: '我的申请', value: applications.value.length, hint: '已发起申请' },
  { label: '已通过', value: applications.value.filter((item) => item.status === 'APPROVED').length, hint: '审批通过申请' }
]);

const recentApplications = computed(() => applications.value.slice(0, 5));

const quickEntries = computed(() => {
  const entries = [
    {
      title: '我的待办',
      description: '查看并处理当前分配给我的审批任务。',
      action: '进入待办',
      to: { name: 'workflow-todos' },
      badge: '审批',
      hint: '待处理审批',
      requiredMenu: 'workflow'
    },
    {
      title: '我的申请',
      description: '查看我发起的申请和流转状态。',
      action: '查看申请',
      to: { name: 'workflow-applications' },
      badge: '流程',
      hint: '申请进度',
      requiredMenu: 'workflow'
    },
    {
      title: '学生事务',
      description: '请假、销假、实习和异常学生。',
      action: '进入模块',
      to: { name: 'module-domain', params: { domain: 'student-affairs' } },
      badge: '学生',
      hint: '学生日常业务',
      requiredMenu: 'student-affairs'
    },
    {
      title: '系统管理',
      description: '用户、组织、角色、附件和流程模板。',
      action: '进入管理',
      to: { name: 'module-domain', params: { domain: 'system' } },
      badge: '管理',
      hint: '管理员可见',
      requiredMenu: 'system'
    }
  ];
  return entries.filter((entry) => appStore.menus.includes(entry.requiredMenu));
});

const businessDomains = [
  { label: '学生事务', title: '请假 / 销假 / 实习 / 奖助 / 证明 / 离返校', description: '学生日常业务入口。' },
  { label: '教学事务', title: '调课 / 停补课 / 课程标准 / 教材 / 考试 / 教室', description: '教学管理入口。' },
  { label: '科研事务', title: '课题申报 / 中期 / 结题 / 成果 / 讲座', description: '科研管理入口。' },
  { label: '后勤事务', title: '会议室 / 维修 / 办公用品 / 用章 / 车辆 / 公告', description: '后勤保障入口。' }
];

async function loadDashboardData() {
  if (!appStore.menus.includes('workflow')) {
    todos.value = [];
    applications.value = [];
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  try {
    const [todoResponse, applicationResponse] = await Promise.all([
      listWorkflowTodos(),
      listMyWorkflowApplications()
    ]);
    todos.value = todoResponse.data;
    applications.value = applicationResponse.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '工作台数据加载失败';
    todos.value = [];
    applications.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadDashboardData();
});
</script>
