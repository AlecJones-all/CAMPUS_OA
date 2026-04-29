<template>
  <div class="app-page">
    <PageHero eyebrow="流程中心" title="我的申请" description="查看我发起的审批申请。">
      <template #actions>
        <RouterLink :to="{ name: 'workflow-todos' }">
          <el-button plain>我的待办</el-button>
        </RouterLink>
        <RouterLink :to="{ name: 'workflow-new' }">
          <el-button type="primary">新建申请</el-button>
        </RouterLink>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <SectionCard title="申请列表" description="仅展示查看入口。">
      <div class="table-summary">
        <p>当前共 {{ applications.length }} 条申请记录</p>
        <el-button :loading="loading" @click="loadApplications">刷新列表</el-button>
      </div>

      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      <p v-else-if="loading" class="muted-text">正在加载申请列表...</p>

      <div v-else-if="applications.length > 0" class="list-shell">
        <article v-for="item in applications" :key="item.id" class="record-card">
          <div class="record-card__main">
            <div class="record-card__title">
              <h3>{{ item.title }}</h3>
              <StatusTag :status="item.status" :label="statusLabel[item.status] ?? item.status" />
            </div>
            <p>{{ item.typeName }} / 单号 {{ item.applicationNo }}</p>
            <p>当前审批人：{{ item.currentApproverName || '未分配' }}</p>
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
        description="可先发起一条新的申请。"
        badge="申请"
      >
        <template #actions>
          <RouterLink :to="{ name: 'workflow-new' }">
            <el-button type="primary">新建申请</el-button>
          </RouterLink>
        </template>
      </EmptyStateBlock>
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import StatusTag from '../../components/StatusTag.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import { listMyWorkflowApplications, type WorkflowApplicationSummary } from '../../api/http';

const loading = ref(false);
const errorMessage = ref('');
const applications = ref<WorkflowApplicationSummary[]>([]);

const statusLabel: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '待审批',
  IN_PROGRESS: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回'
};

const stats = computed(() => [
  { label: '申请总数', value: applications.value.length, hint: '当前账号申请数' },
  { label: '审批中', value: applications.value.filter((item) => item.status === 'PENDING' || item.status === 'IN_PROGRESS').length, hint: '进行中的流程' },
  { label: '已完成', value: applications.value.filter((item) => item.status === 'APPROVED').length, hint: '已通过申请' }
]);

async function loadApplications() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await listMyWorkflowApplications();
    applications.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '申请列表加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadApplications();
});
</script>
