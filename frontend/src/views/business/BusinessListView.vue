<template>
  <div class="app-page">
    <PageHero
      eyebrow="业务列表"
      :title="moduleDef?.name ?? '业务模块'"
      :description="moduleDef?.description ?? '查看业务记录和审批状态。'"
    >
      <template #actions>
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选" style="width: 180px" @change="loadRecords">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <RouterLink v-if="canCreate" :to="{ name: 'business-new', params: { businessKey } }">
          <el-button type="primary">新建业务</el-button>
        </RouterLink>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <SectionCard title="业务记录" description="查看记录、筛选状态并进入详情。">
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      <p v-else-if="loading" class="muted-text">正在加载业务列表...</p>

      <div v-else-if="records.length > 0" class="list-shell">
        <article v-for="item in records" :key="item.id" class="record-card">
          <div class="record-card__main">
            <div class="record-card__title">
              <h3>{{ item.title }}</h3>
              <StatusTag :status="item.status" :label="statusLabel[item.status] ?? item.status" />
            </div>
            <p>申请人：{{ item.applicantName }}</p>
            <p>当前审批人：{{ item.currentApproverName || '未分配' }}</p>
            <p>提交时间：{{ item.submittedAt || '-' }}</p>
          </div>
          <div class="record-card__actions">
            <RouterLink :to="{ name: 'business-detail', params: { businessKey, id: item.id } }">
              <el-button type="primary" plain>查看</el-button>
            </RouterLink>
          </div>
        </article>
      </div>

      <EmptyStateBlock
        v-else
        title="暂无业务记录"
        description="可先新建一条业务。"
        badge="业务"
      >
        <template #actions>
          <RouterLink v-if="canCreate" :to="{ name: 'business-new', params: { businessKey } }">
            <el-button type="primary">新建第一条业务</el-button>
          </RouterLink>
        </template>
      </EmptyStateBlock>
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { canCreateBusinessModule, getBusinessModule } from '../../business/modules';
import { listBusinessRecords, type BusinessRecordSummary } from '../../api/http';
import { useAppStore } from '../../stores/app';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import StatusTag from '../../components/StatusTag.vue';
import SummaryStats from '../../components/SummaryStats.vue';

const route = useRoute();
const appStore = useAppStore();
const loading = ref(false);
const errorMessage = ref('');
const records = ref<BusinessRecordSummary[]>([]);
const statusFilter = ref('');

const businessKey = computed(() => String(route.params.businessKey ?? ''));
const moduleDef = computed(() => getBusinessModule(businessKey.value));
const canCreate = computed(() => (moduleDef.value ? canCreateBusinessModule(moduleDef.value, appStore.roles) : false));

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待审批', value: 'PENDING' },
  { label: '审批中', value: 'IN_PROGRESS' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已撤回', value: 'WITHDRAWN' }
];

const statusLabel: Record<string, string> = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]));

const stats = computed(() => [
  { label: '记录总数', value: records.value.length, hint: '当前筛选结果' },
  { label: '可提交', value: records.value.filter((item) => item.status === 'DRAFT').length, hint: '草稿待提交' },
  { label: '审批中', value: records.value.filter((item) => item.status === 'PENDING' || item.status === 'IN_PROGRESS').length, hint: '待处理流程' }
]);

async function loadRecords() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await listBusinessRecords(businessKey.value, statusFilter.value || undefined);
    records.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '业务列表加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadRecords();
});
</script>
