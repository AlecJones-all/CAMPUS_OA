<template>
  <div v-if="detail" class="app-page">
    <PageHero eyebrow="业务详情" :title="detail.title" :description="detail.businessName">
      <template #actions>
        <el-button
          v-if="primaryAction"
          type="primary"
          :loading="submitting"
          @click="runPrimaryAction(primaryAction.key)"
        >
          {{ primaryAction.label }}
        </el-button>
        <ActionMenu v-if="moreActions.length > 0" :items="moreActions" @select="runPrimaryAction" />
      </template>
      <template #meta>
        <SummaryStats :items="heroStats" />
      </template>
    </PageHero>

    <SectionCard title="业务内容" description="展示业务字段和申请信息。">
      <div class="field-grid">
        <div v-for="field in detail.fields" :key="field.key" class="field-card">
          <span>{{ field.label }}</span>
          <strong>{{ field.value }}</strong>
        </div>
      </div>
    </SectionCard>

    <AttachmentPanel business-type="WORKFLOW_APPLICATION" :business-id="detail.workflow.id" title="业务附件" />

    <SectionCard title="审批记录" description="查看审批轨迹和处理意见。">
      <div v-if="detail.workflow.records.length > 0" class="timeline-list">
        <article v-for="record in detail.workflow.records" :key="record.id" class="timeline-item">
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

  <SectionCard v-else title="业务详情" description="正在加载业务详情。">
    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    <p v-else class="muted-text">正在加载业务详情...</p>
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
import { getBusinessRecordDetail, submitBusinessRecord, withdrawWorkflowApplication, type BusinessRecordDetail } from '../../api/http';

const route = useRoute();
const router = useRouter();
const detail = ref<BusinessRecordDetail | null>(null);
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

const heroStats = computed(() => {
  if (!detail.value) {
    return [];
  }
  return [
    { label: '当前状态', value: statusLabel[detail.value.workflow.status] ?? detail.value.workflow.status, hint: '流程状态' },
    { label: '当前审批人', value: detail.value.workflow.currentApproverName || '未分配', hint: '当前处理节点' },
    { label: '记录条数', value: detail.value.workflow.records.length, hint: '审批记录数' }
  ];
});

const actionItems = computed(() => {
  if (!detail.value) {
    return [];
  }

  const items: Array<{ key: string; label: string }> = [];
  if (detail.value.workflow.canSubmit) {
    items.push({ key: 'submit', label: '提交业务' });
  }
  if (detail.value.workflow.canApprove || detail.value.workflow.canReject) {
    items.push({ key: 'approve', label: '去审批处理' });
  }
  if (detail.value.workflow.canWithdraw) {
    items.push({ key: 'withdraw', label: '撤回业务' });
  }
  return items;
});

const primaryAction = computed(() => actionItems.value[0] ?? null);
const moreActions = computed(() => actionItems.value.slice(1));

function businessKey() {
  return String(route.params.businessKey ?? '');
}

function currentId() {
  return Number(route.params.id);
}

async function loadDetail() {
  errorMessage.value = '';
  try {
    const response = await getBusinessRecordDetail(businessKey(), currentId());
    detail.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '业务详情加载失败';
    if (errorMessage.value.includes('无权') || errorMessage.value.includes('不存在')) {
      await router.replace({ name: 'business-list', params: { businessKey: businessKey() } });
    }
  }
}

async function runPrimaryAction(key: string) {
  submitting.value = true;
  try {
    if (key === 'submit') {
      await submitBusinessRecord(businessKey(), currentId());
      await loadDetail();
      return;
    }

    if (key === 'withdraw' && detail.value) {
      await withdrawWorkflowApplication(detail.value.workflow.id);
      await loadDetail();
      return;
    }

    if (key === 'approve' && detail.value) {
      await router.push({ name: 'workflow-process', params: { id: detail.value.workflow.id } });
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '业务操作失败';
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  if (!Number.isFinite(currentId())) {
    void router.push({ name: 'dashboard' });
    return;
  }
  void loadDetail();
});
</script>
