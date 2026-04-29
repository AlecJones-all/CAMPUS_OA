<template>
  <div class="app-page">
    <PageHero
      eyebrow="工作台"
      title="校园 OA 工作台"
      description="查看常用入口、业务概览和平台状态。"
    >
      <template #meta>
        <SummaryStats :items="heroStats" />
      </template>
    </PageHero>

    <div class="split-layout">
      <div class="app-page">
        <SectionCard title="常用入口" description="展示当前角色常用业务入口。">
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

        <SectionCard title="业务概览" description="按业务域查看已开放功能。">
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
        <SectionCard title="平台状态" description="查看后端服务运行状态。">
          <div class="two-column-grid">
            <div class="field-card">
              <span>服务名</span>
              <strong>{{ health?.service ?? '-' }}</strong>
            </div>
            <div class="field-card">
              <span>当前状态</span>
              <strong :style="{ color: health?.status === 'UP' ? 'var(--app-success)' : 'var(--app-danger)' }">
                {{ loading ? '检查中' : health?.status ?? '不可用' }}
              </strong>
            </div>
            <div class="field-card span-two">
              <span>最近检查时间</span>
              <strong>{{ health?.timestamp ?? '-' }}</strong>
            </div>
          </div>
          <div class="app-actions">
            <el-button :loading="loading" @click="loadHealth">重新检查</el-button>
          </div>
          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        </SectionCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { getHealth, type HealthData } from '../api/http';
import { businessModules } from '../business/modules';
import { useAppStore } from '../stores/app';
import PageHero from '../components/PageHero.vue';
import SectionCard from '../components/SectionCard.vue';
import SummaryStats from '../components/SummaryStats.vue';

const appStore = useAppStore();
const loading = ref(false);
const health = ref<HealthData | null>(null);
const errorMessage = ref('');

const heroStats = computed(() => [
  { label: '已启用菜单', value: appStore.menus.length, hint: '当前角色可见菜单数量' },
  { label: '业务模块', value: businessModules.length, hint: '前端已接入模块数量' },
  { label: '角色数量', value: appStore.roles.length, hint: '当前登录角色数量' }
]);

const quickEntries = computed(() => {
  const entries = [
    {
      title: '我的待办',
      description: '查看并处理当前分配给我的审批任务。',
      action: '进入待办',
      to: { name: 'workflow-todos' },
      badge: '审批',
      hint: '高频处理入口',
      requiredMenu: 'workflow'
    },
    {
      title: '我的申请',
      description: '查看我发起的申请和流转状态。',
      action: '查看申请',
      to: { name: 'workflow-applications' },
      badge: '流程',
      hint: '统一申请台账',
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
  { label: '学生事务', title: '请假 / 销假 / 实习 / 异常学生', description: '学生日常业务入口。' },
  { label: '教学事务', title: '调课 / 课程标准 / 教材征订', description: '教学管理入口。' },
  { label: '科研事务', title: '课题申报 / 中期检查 / 结题', description: '科研管理入口。' }
];

async function loadHealth() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await getHealth();
    health.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '后端连接失败';
    health.value = null;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadHealth();
});
</script>
