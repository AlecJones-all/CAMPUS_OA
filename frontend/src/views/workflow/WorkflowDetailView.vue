<template>
  <div v-if="detail" class="app-page">
    <PageHero eyebrow="流程详情" :title="detail.title" :description="`${detail.typeName} / 单号 ${detail.applicationNo}`">
      <template #actions>
        <el-button
          v-if="primaryAction"
          type="primary"
          :loading="submitting"
          @click="runAction(primaryAction.key)"
        >
          {{ primaryAction.label }}
        </el-button>
        <ActionMenu v-if="moreActions.length > 0" :items="moreActions" @select="runAction" />
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <div class="split-layout">
      <div class="app-page">
        <SectionCard title="申请内容" description="展示申请内容和基本信息。">
          <div class="field-grid">
            <div class="field-card">
              <span>申请人</span>
              <strong>{{ detail.applicantName }}</strong>
            </div>
            <div class="field-card">
              <span>当前审批人</span>
              <strong>{{ detail.currentApproverName || '未分配' }}</strong>
            </div>
            <div class="field-card">
              <span>提交时间</span>
              <strong>{{ detail.submittedAt || '-' }}</strong>
            </div>
            <div class="field-card">
              <span>完成时间</span>
              <strong>{{ detail.finishedAt || '-' }}</strong>
            </div>
          </div>
          <div class="form-help">{{ detail.content }}</div>
        </SectionCard>

        <AttachmentPanel business-type="WORKFLOW_APPLICATION" :business-id="detail.id" title="申请附件" />
      </div>

      <SectionCard title="审批记录" description="按时间顺序查看审批记录。">
        <div v-if="detail.records.length > 0" class="timeline-list">
          <article v-for="record in detail.records" :key="record.id" class="timeline-item">
            <div class="timeline-item__row">
              <strong>{{ actionLabel[record.actionType] ?? record.actionType }}</strong>
              <span>{{ record.createdAt }}</span>
            </div>
            <p class="muted-text">{{ record.actorName }}</p>
            <p class="muted-text">{{ record.comment || '无审批意见' }}</p>
          </article>
        </div>
        <EmptyStateBlock
          v-else
          title="暂无审批记录"
          description="当前申请还没有审批动作。"
          badge="记录"
        />
      </SectionCard>
    </div>
  </div>

  <SectionCard v-else title="申请详情" description="正在加载申请详情。">
    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    <p v-else class="muted-text">正在加载申请详情...</p>
  </SectionCard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AttachmentPanel from '../../components/AttachmentPanel.vue';
import ActionMenu from '../../components/ActionMenu.vue';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import { getWorkflowApplicationDetail, submitWorkflowApplication, withdrawWorkflowApplication, type WorkflowApplicationDetail } from '../../api/http';

const route = useRoute();
const router = useRouter();
const detail = ref<WorkflowApplicationDetail | null>(null);
const errorMessage = ref('');
const submitting = ref(false);

const statusLabel: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '待审批',
  IN_PROGRESS: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回'
};

const actionLabel: Record<string, string> = {
  SUBMIT: '提交申请',
  APPROVE: '审批通过',
  REJECT: '审批驳回',
  WITHDRAW: '申请撤回'
};

const stats = computed(() => {
  if (!detail.value) {
    return [];
  }
  return [
    { label: '当前状态', value: statusLabel[detail.value.status] ?? detail.value.status, hint: '流程状态' },
    { label: '当前审批人', value: detail.value.currentApproverName || '未分配', hint: '当前处理节点' },
    { label: '记录条数', value: detail.value.records.length, hint: '审批动作数' }
  ];
});

const actionItems = computed(() => {
  if (!detail.value) {
    return [];
  }
  const items: Array<{ key: string; label: string }> = [];
  if (detail.value.canSubmit) {
    items.push({ key: 'submit', label: '提交申请' });
  }
  if (detail.value.canApprove || detail.value.canReject) {
    items.push({ key: 'approve', label: '审批处理' });
  }
  if (detail.value.canWithdraw) {
    items.push({ key: 'withdraw', label: '撤回申请' });
  }
  return items;
});

const primaryAction = computed(() => actionItems.value[0] ?? null);
const moreActions = computed(() => actionItems.value.slice(1));

function currentId() {
  return Number(route.params.id);
}

async function loadDetail() {
  errorMessage.value = '';
  try {
    const response = await getWorkflowApplicationDetail(currentId());
    detail.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '详情加载失败';
  }
}

async function runAction(key: string) {
  submitting.value = true;
  try {
    if (key === 'submit') {
      await submitWorkflowApplication(currentId());
      await loadDetail();
      return;
    }

    if (key === 'withdraw') {
      await withdrawWorkflowApplication(currentId());
      await loadDetail();
      return;
    }

    if (key === 'approve') {
      await router.push({ name: 'workflow-process', params: { id: currentId() } });
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程操作失败';
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  if (!Number.isFinite(currentId())) {
    void router.push({ name: 'workflow-applications' });
    return;
  }
  void loadDetail();
});
</script>
