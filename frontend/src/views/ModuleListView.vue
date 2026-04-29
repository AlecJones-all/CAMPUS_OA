<template>
  <div class="app-page">
    <PageHero :eyebrow="heroLabel" :title="title" :description="description">
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <SectionCard title="功能入口" description="选择需要进入的业务或管理功能。">
      <div v-if="items.length > 0" class="module-grid">
        <article v-for="item in items" :key="item.name" class="module-card">
          <div>
            <span class="module-card__badge">{{ item.badge }}</span>
            <h3>{{ item.name }}</h3>
            <p>{{ item.desc }}</p>
          </div>
          <div class="module-card__footer">
            <span class="muted-text">{{ item.footer }}</span>
            <div class="module-card__actions">
              <RouterLink v-if="item.routeName" :to="{ name: item.routeName }">
                <el-button type="primary">{{ item.actionLabel || '进入管理' }}</el-button>
              </RouterLink>
              <template v-else-if="item.businessKey">
                <RouterLink :to="{ name: 'business-list', params: { businessKey: item.businessKey } }">
                  <el-button plain>业务列表</el-button>
                </RouterLink>
                <RouterLink v-if="item.canCreate" :to="{ name: 'business-new', params: { businessKey: item.businessKey } }">
                  <el-button type="primary">新建申请</el-button>
                </RouterLink>
              </template>
            </div>
          </div>
        </article>
      </div>

      <EmptyStateBlock
        v-else
        title="当前模块暂无可见业务"
        description="当前角色暂无可访问的业务入口。"
        badge="模块"
      />
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { canCreateBusinessModule, canViewBusinessModule, getBusinessModulesByDomain } from '../business/modules';
import EmptyStateBlock from '../components/EmptyStateBlock.vue';
import PageHero from '../components/PageHero.vue';
import SectionCard from '../components/SectionCard.vue';
import SummaryStats from '../components/SummaryStats.vue';
import { useAppStore } from '../stores/app';

interface ModuleCardItem {
  name: string;
  desc: string;
  badge: string;
  footer: string;
  businessKey?: string;
  canCreate?: boolean;
  routeName?: string;
  actionLabel?: string;
}

const route = useRoute();
const appStore = useAppStore();

const systemItems: ModuleCardItem[] = [
  { name: '用户管理', desc: '维护用户账号、组织归属和角色分配。', routeName: 'system-users', actionLabel: '进入用户管理', badge: '基础', footer: '账号管理' },
  { name: '组织管理', desc: '维护学校、学院、部门和班级。', routeName: 'system-orgs', actionLabel: '进入组织管理', badge: '组织', footer: '组织维护' },
  { name: '角色权限', desc: '维护角色和权限。', routeName: 'system-roles', actionLabel: '进入角色权限', badge: '权限', footer: '权限配置' },
  { name: '附件中心', desc: '查询和管理附件。', routeName: 'system-files', actionLabel: '进入附件中心', badge: '附件', footer: '附件管理' },
  { name: '流程模板', desc: '维护流程模板和审批节点。', routeName: 'system-workflows', actionLabel: '进入流程模板', badge: '流程', footer: '流程配置' }
];

const modules: Record<string, { title: string; description: string; eyebrow: string }> = {
  'student-affairs': { title: '学生事务', description: '查看学生相关业务入口。', eyebrow: '业务域' },
  academic: { title: '教学事务', description: '查看教学相关业务入口。', eyebrow: '业务域' },
  research: { title: '科研事务', description: '查看科研相关业务入口。', eyebrow: '业务域' },
  logistics: { title: '后勤事务', description: '查看后勤相关业务入口。', eyebrow: '业务域' },
  system: { title: '系统管理', description: '查看平台管理功能入口。', eyebrow: '平台管理' }
};

const domain = computed(() => String(route.params.domain ?? ''));
const title = computed(() => modules[domain.value]?.title ?? '模块');
const description = computed(() => modules[domain.value]?.description ?? '查看当前业务域下的功能入口。');
const heroLabel = computed(() => modules[domain.value]?.eyebrow ?? '模块');

const items = computed<ModuleCardItem[]>(() => {
  if (domain.value === 'system') {
    return systemItems;
  }

  return getBusinessModulesByDomain(domain.value)
    .filter((item) => canViewBusinessModule(item, appStore.roles))
    .map((item) => ({
      name: item.name,
      desc: item.description,
      businessKey: item.key,
      canCreate: canCreateBusinessModule(item, appStore.roles),
      routeName: undefined,
      actionLabel: undefined,
      badge:
        item.domain === 'student-affairs'
          ? '学生'
          : item.domain === 'academic'
            ? '教学'
            : item.domain === 'research'
              ? '科研'
              : '后勤',
      footer: item.createRoles.length > 0 ? '可发起' : '仅查看'
    }));
});

const stats = computed(() => [
  { label: '入口数量', value: items.value.length, hint: '当前角色可见入口' },
  { label: '可新建业务', value: items.value.filter((item) => item.canCreate).length, hint: '当前角色可发起数量' },
  { label: '当前模块', value: title.value, hint: '正在查看的业务分组' }
]);
</script>
