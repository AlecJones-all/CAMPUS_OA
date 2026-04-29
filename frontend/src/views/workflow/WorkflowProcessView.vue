<template>
  <div v-if="detail" class="app-page">
    <PageHero eyebrow="审批处理" title="审批处理" :description="`${detail.typeName} / ${detail.title}`">
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <div class="split-layout">
      <SectionCard title="申请摘要" description="查看申请内容后再进行审批。">
        <div class="form-help">{{ detail.content }}</div>
        <div class="field-grid">
          <div class="field-card">
            <span>申请人</span>
            <strong>{{ detail.applicantName }}</strong>
          </div>
          <div class="field-card">
            <span>当前状态</span>
            <strong>{{ detail.status }}</strong>
          </div>
        </div>
      </SectionCard>

      <SectionCard title="审批操作" description="通过和驳回在同一区域处理。">
        <label class="approval-form">
          <span>审批意见</span>
          <textarea v-model.trim="comment" rows="8" placeholder="请输入审批意见，可为空" />
        </label>
        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        <div class="app-actions">
          <el-button type="primary" :loading="submitting" :disabled="!detail.canApprove" @click="handleApprove">审批通过</el-button>
          <el-button type="danger" :loading="submitting" :disabled="!detail.canReject" @click="handleReject">审批驳回</el-button>
        </div>
      </SectionCard>
    </div>
  </div>

  <SectionCard v-else title="审批处理" description="正在加载审批单。">
    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    <p v-else class="muted-text">正在加载审批单...</p>
  </SectionCard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import {
  approveWorkflowApplication,
  getWorkflowApplicationDetail,
  rejectWorkflowApplication,
  type WorkflowApplicationDetail
} from '../../api/http';

const route = useRoute();
const router = useRouter();
const detail = ref<WorkflowApplicationDetail | null>(null);
const comment = ref('');
const submitting = ref(false);
const errorMessage = ref('');

const stats = computed(() => [
  { label: '审批动作', value: '通过 / 驳回', hint: '当前可执行动作' },
  { label: '申请摘要', value: '已展示', hint: '查看业务内容' },
  { label: '意见字数', value: comment.value.length, hint: '审批意见长度' }
]);

function currentId() {
  return Number(route.params.id);
}

async function loadDetail() {
  errorMessage.value = '';
  try {
    const response = await getWorkflowApplicationDetail(currentId());
    detail.value = response.data;
    if (!detail.value.canApprove && !detail.value.canReject) {
      await router.replace({ name: 'workflow-detail', params: { id: currentId() } });
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '审批单加载失败';
    if (errorMessage.value.includes('无权') || errorMessage.value.includes('不存在')) {
      await router.replace({ name: 'workflow-todos' });
    }
  }
}

async function handleApprove() {
  submitting.value = true;
  try {
    await approveWorkflowApplication(currentId(), comment.value);
    await router.push({ name: 'workflow-detail', params: { id: currentId() } });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '审批通过失败';
  } finally {
    submitting.value = false;
  }
}

async function handleReject() {
  submitting.value = true;
  try {
    await rejectWorkflowApplication(currentId(), comment.value);
    await router.push({ name: 'workflow-detail', params: { id: currentId() } });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '审批驳回失败';
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  if (!Number.isFinite(currentId())) {
    void router.push({ name: 'workflow-todos' });
    return;
  }
  void loadDetail();
});
</script>

<style scoped>
.approval-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

textarea {
  border: 1px solid var(--app-border);
  border-radius: 14px;
  padding: 12px 14px;
  background: #fff;
}
</style>
