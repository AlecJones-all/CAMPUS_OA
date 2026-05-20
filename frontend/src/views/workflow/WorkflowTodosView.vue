<template>
  <div class="app-page">
    <PageHero eyebrow="流程中心" title="我的待办" description="查看待处理审批任务。">
      <template #actions>
        <RouterLink :to="{ name: 'workflow-applications' }">
          <el-button plain>我的申请</el-button>
        </RouterLink>
        <el-button :loading="loading" @click="loadTodos">刷新待办</el-button>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <SectionCard title="待办任务" description="仅展示待办任务和处理入口。">
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      <p v-else-if="loading" class="muted-text">正在加载待办列表...</p>

      <div v-else-if="todos.length > 0" class="list-shell">
        <article v-for="item in todos" :key="item.id" class="record-card">
          <div class="record-card__main">
            <div class="record-card__title">
              <h3>{{ item.title }}</h3>
              <StatusTag :status="item.status" :label="statusLabel[item.status] ?? item.status" />
            </div>
            <p>{{ item.typeName }} / 申请人 {{ item.applicantName }}</p>
            <p>当前审批角色：{{ approverLabel(item) }}</p>
            <p>提交时间：{{ item.submittedAt || '-' }}</p>
          </div>
          <div class="record-card__actions">
            <RouterLink :to="{ name: 'workflow-process', params: { id: item.id } }">
              <el-button type="primary">审批处理</el-button>
            </RouterLink>
          </div>
        </article>
      </div>

      <EmptyStateBlock
        v-else
        title="暂无待办任务"
        description="当前没有需要处理的审批。"
        badge="待办"
      />
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import StatusTag from '../../components/StatusTag.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import { listWorkflowTodos, type WorkflowApplicationSummary } from '../../api/http';

const loading = ref(false);
const errorMessage = ref('');
const todos = ref<WorkflowApplicationSummary[]>([]);

const statusLabel: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '待审批',
  IN_PROGRESS: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回'
};

const stats = computed(() => [
  { label: '待办总数', value: todos.value.length, hint: '当前需处理任务' },
  { label: '待审批', value: todos.value.filter((item) => item.status === 'PENDING').length, hint: '等待审批处理' },
  { label: '审批中', value: todos.value.filter((item) => item.status === 'IN_PROGRESS').length, hint: '正在流转的申请' }
]);

function approverLabel(item: WorkflowApplicationSummary) {
  if (item.currentApproverRoleCode) {
    return `${item.currentApproverRoleCode} / ${item.currentApproverName || '未指定人员'}`;
  }
  return item.currentApproverName || '未分配';
}

async function loadTodos() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await listWorkflowTodos();
    todos.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '待办列表加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadTodos();
});
</script>
