<template>
  <div class="layout-shell">
    <aside class="layout-sidebar">
      <div class="layout-brand">
        <div class="layout-brand__mark">OA</div>
        <div>
          <strong>Campus OA</strong>
          <span>校园协同办公平台</span>
        </div>
      </div>

      <div class="layout-sidebar__controls">
        <div>
          <strong>菜单浏览</strong>
          <span>查看更多业务入口</span>
        </div>
        <div class="layout-sidebar__buttons">
          <el-button size="small" plain circle title="向上滚动菜单" @click="scrollSidebar(-1)">↑</el-button>
          <el-button size="small" plain circle title="向下滚动菜单" @click="scrollSidebar(1)">↓</el-button>
          <el-button size="small" plain circle title="回到菜单顶部" @click="scrollSidebarToTop">↥</el-button>
        </div>
      </div>

      <div ref="sidebarScrollRef" class="layout-sidebar__groups">
        <section v-for="group in visibleMenuGroups" :key="group.key" class="layout-menu-group">
          <h2>{{ group.label }}</h2>
          <RouterLink
            v-for="item in group.items"
            :key="item.key"
            :to="item.to"
            class="layout-menu-link"
          >
            <div>
              <strong>{{ item.label }}</strong>
              <span>{{ item.description }}</span>
            </div>
          </RouterLink>
        </section>
      </div>
    </aside>

    <main class="layout-main">
      <header class="layout-topbar">
        <div class="layout-topbar__copy">
          <span class="layout-topbar__eyebrow">待办、申请、业务入口</span>
          <h1>{{ currentPageTitle }}</h1>
          <p>{{ currentPageDescription }}</p>
        </div>
        <div class="layout-user">
          <div class="layout-user__card">
            <strong>{{ appStore.currentUserName || '未登录' }}</strong>
            <div class="layout-user__roles">
              <span v-for="role in roleTags" :key="role">{{ role }}</span>
            </div>
            <el-button type="primary" plain @click="logout">退出登录</el-button>
          </div>
        </div>
      </header>

      <section class="layout-content">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAppStore } from '../stores/app';

interface MenuItem {
  key: string;
  label: string;
  description: string;
  to: { name: string; params?: Record<string, string> };
  requiredMenu?: string;
}

const router = useRouter();
const route = useRoute();
const appStore = useAppStore();
const sidebarScrollRef = ref<HTMLElement | null>(null);

const roleLabelMap: Record<string, string> = {
  ADMIN: '系统管理员',
  STUDENT: '学生',
  TEACHER: '教师',
  ADVISER: '班主任/辅导员',
  RESEARCH: '科技处',
  OFFICE: '教务处',
  REVIEWER: '评审专家'
};

const menuGroups: Array<{ key: string; label: string; items: MenuItem[] }> = [
  {
    key: 'workspace',
    label: '工作中心',
    items: [
      { key: 'dashboard', label: '工作台', description: '查看概览、常用入口和平台状态。', to: { name: 'dashboard' }, requiredMenu: 'dashboard' },
      { key: 'workflow-applications', label: '我的申请', description: '查看我发起的申请。', to: { name: 'workflow-applications' }, requiredMenu: 'workflow' },
      { key: 'workflow-todos', label: '我的待办', description: '处理待办审批任务。', to: { name: 'workflow-todos' }, requiredMenu: 'workflow' }
    ]
  },
  {
    key: 'business',
    label: '业务入口',
    items: [
      { key: 'student-affairs', label: '学生事务', description: '请假、销假、实习、奖助学金。', to: { name: 'module-domain', params: { domain: 'student-affairs' } }, requiredMenu: 'student-affairs' },
      { key: 'academic', label: '教学事务', description: '调课、课程标准、教材征订。', to: { name: 'module-domain', params: { domain: 'academic' } }, requiredMenu: 'academic' },
      { key: 'research', label: '科研事务', description: '课题申报、中期检查、结题。', to: { name: 'module-domain', params: { domain: 'research' } }, requiredMenu: 'research' },
      { key: 'logistics', label: '后勤事务', description: '会议室、维修、借用。', to: { name: 'module-domain', params: { domain: 'logistics' } }, requiredMenu: 'logistics' }
    ]
  },
  {
    key: 'platform',
    label: '平台管理',
    items: [
      { key: 'system', label: '系统管理', description: '用户、组织、角色、附件和流程模板。', to: { name: 'module-domain', params: { domain: 'system' } }, requiredMenu: 'system' }
    ]
  }
];

const pageTextMap: Record<string, { title: string; description: string }> = {
  dashboard: { title: '工作台', description: '查看工作概览、常用入口和平台状态。' },
  'workflow-applications': { title: '我的申请', description: '查看我发起的申请。' },
  'workflow-todos': { title: '我的待办', description: '处理当前待办任务。' },
  'workflow-new': { title: '新建申请', description: '填写申请信息并提交。' },
  'workflow-detail': { title: '申请详情', description: '查看申请内容、附件和审批记录。' },
  'workflow-process': { title: '审批处理', description: '处理当前审批任务。' },
  'business-list': { title: '业务列表', description: '查看记录并筛选状态。' },
  'business-new': { title: '新建业务', description: '填写业务信息并提交。' },
  'business-detail': { title: '业务详情', description: '查看业务内容、附件和审批记录。' },
  'module-domain': { title: '模块入口', description: '查看当前业务域下的功能入口。' },
  'system-users': { title: '用户管理', description: '管理平台用户、角色分配和账号状态。' },
  'system-orgs': { title: '组织管理', description: '维护学校、学院、部门和班级。' },
  'system-roles': { title: '角色权限', description: '配置角色和权限。' },
  'system-files': { title: '附件中心', description: '查询和管理附件。' },
  'system-workflows': { title: '流程模板', description: '维护业务类型对应的流程模板和审批节点。' }
};

const visibleMenuGroups = computed(() =>
  menuGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => !item.requiredMenu || appStore.menus.includes(item.requiredMenu))
    }))
    .filter((group) => group.items.length > 0)
);

const currentPageTitle = computed(() => pageTextMap[String(route.name)]?.title ?? '校园 OA 系统');
const currentPageDescription = computed(() => pageTextMap[String(route.name)]?.description ?? '查看当前页面的业务内容。');
const roleTags = computed(() => appStore.roles.map((role) => roleLabelMap[role] ?? role));

function scrollSidebar(step: number) {
  sidebarScrollRef.value?.scrollBy({ top: step * 240, behavior: 'smooth' });
}

function scrollSidebarToTop() {
  sidebarScrollRef.value?.scrollTo({ top: 0, behavior: 'smooth' });
}

async function logout() {
  await appStore.logout();
  await router.push({ name: 'login' });
}
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: 310px minmax(0, 1fr);
  height: 100vh;
  overflow: hidden;
}

.layout-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  overflow: hidden;
  padding: 24px 20px 20px;
  background:
    linear-gradient(180deg, rgba(11, 59, 100, 0.96) 0%, rgba(15, 76, 129, 0.96) 58%, rgba(12, 67, 99, 0.98) 100%);
  color: #fff;
}

.layout-brand {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
}

.layout-brand__mark {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #ffd36a, #f2aa1b);
  color: #102a43;
  font-size: 18px;
  font-weight: 700;
}

.layout-brand strong,
.layout-brand span {
  display: block;
}

.layout-brand span {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.layout-sidebar__controls {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
}

.layout-sidebar__controls strong,
.layout-sidebar__controls span {
  display: block;
}

.layout-sidebar__controls strong {
  font-size: 14px;
}

.layout-sidebar__controls span {
  margin-top: 3px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.layout-sidebar__buttons {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.layout-sidebar__buttons :deep(.el-button) {
  width: 30px;
  height: 30px;
  border-color: rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.layout-sidebar__groups {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  padding-right: 4px;
  overscroll-behavior: contain;
  scrollbar-width: none;
  -ms-overflow-style: none;
  scrollbar-gutter: stable;
}

.layout-sidebar__groups::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

.layout-menu-group {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.layout-menu-group h2 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: rgba(255, 255, 255, 0.68);
  text-transform: uppercase;
}

.layout-menu-link {
  display: block;
  padding: 14px 16px;
  border-radius: 18px;
  text-decoration: none;
  color: inherit;
  background: rgba(255, 255, 255, 0.08);
  transition:
    transform 0.18s ease,
    background 0.18s ease;
}

.layout-menu-link:hover {
  transform: translateX(2px);
  background: rgba(255, 255, 255, 0.14);
}

.layout-menu-link strong,
.layout-menu-link span {
  display: block;
}

.layout-menu-link strong {
  font-size: 15px;
  margin-bottom: 4px;
}

.layout-menu-link span {
  font-size: 12px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.74);
}

.layout-main {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: var(--app-bg);
}

.layout-topbar {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  padding: 28px 32px 18px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(248, 250, 252, 0.98) 100%);
}

.layout-topbar__copy {
  max-width: 720px;
}

.layout-topbar__eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(23, 95, 161, 0.08);
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.layout-topbar h1 {
  margin: 14px 0 8px;
  font-size: 30px;
  line-height: 1.2;
  color: var(--app-text);
}

.layout-topbar p {
  margin: 0;
  color: var(--app-text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.layout-user {
  display: flex;
  justify-content: flex-end;
}

.layout-user__card {
  min-width: 220px;
  padding: 16px 18px;
  border-radius: 20px;
  background: #fff;
  box-shadow: var(--app-shadow-sm);
}

.layout-user__card strong {
  display: block;
  font-size: 16px;
}

.layout-user__roles {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0 14px;
}

.layout-user__roles span {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(23, 95, 161, 0.1);
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 600;
}

.layout-content {
  height: 100%;
  min-height: 0;
  overflow: auto;
  padding: 24px 32px 32px;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}

@media (max-width: 1200px) {
  .layout-shell {
    grid-template-columns: 280px minmax(0, 1fr);
  }
}

@media (max-width: 960px) {
  .layout-shell {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 100vh;
  }

  .layout-sidebar {
    position: sticky;
    top: 0;
    z-index: 10;
  }

  .layout-content {
    padding: 20px;
  }

  .layout-topbar {
    flex-direction: column;
    padding: 22px 20px 18px;
  }
}
</style>
