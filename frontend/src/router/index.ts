import { createRouter, createWebHistory } from 'vue-router';
import { useAppStore } from '../stores/app';
import MainLayout from '../layouts/MainLayout.vue';
import DashboardView from '../views/DashboardView.vue';
import ModuleListView from '../views/ModuleListView.vue';
import LoginView from '../views/LoginView.vue';
import WorkflowApplicationsView from '../views/workflow/WorkflowApplicationsView.vue';
import WorkflowCreateView from '../views/workflow/WorkflowCreateView.vue';
import WorkflowDetailView from '../views/workflow/WorkflowDetailView.vue';
import WorkflowTodosView from '../views/workflow/WorkflowTodosView.vue';
import WorkflowProcessView from '../views/workflow/WorkflowProcessView.vue';
import BusinessListView from '../views/business/BusinessListView.vue';
import BusinessCreateView from '../views/business/BusinessCreateView.vue';
import BusinessDetailView from '../views/business/BusinessDetailView.vue';
import SystemUsersView from '../views/system/SystemUsersView.vue';
import SystemOrgsView from '../views/system/SystemOrgsView.vue';
import SystemRolesView from '../views/system/SystemRolesView.vue';
import SystemFilesView from '../views/system/SystemFilesView.vue';
import SystemWorkflowDefinitionsView from '../views/system/SystemWorkflowDefinitionsView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardView
        },
        {
          path: 'workflow/applications',
          name: 'workflow-applications',
          component: WorkflowApplicationsView
        },
        {
          path: 'workflow/new',
          name: 'workflow-new',
          component: WorkflowCreateView
        },
        {
          path: 'workflow/applications/:id',
          name: 'workflow-detail',
          component: WorkflowDetailView
        },
        {
          path: 'workflow/todos',
          name: 'workflow-todos',
          component: WorkflowTodosView
        },
        {
          path: 'workflow/todos/:id/process',
          name: 'workflow-process',
          component: WorkflowProcessView
        },
        {
          path: 'business/:businessKey/list',
          name: 'business-list',
          component: BusinessListView
        },
        {
          path: 'business/:businessKey/new',
          name: 'business-new',
          component: BusinessCreateView
        },
        {
          path: 'business/:businessKey/:id',
          name: 'business-detail',
          component: BusinessDetailView
        },
        {
          path: 'modules/:domain',
          name: 'module-domain',
          component: ModuleListView
        },
        {
          path: 'system/users',
          name: 'system-users',
          component: SystemUsersView,
          meta: { requiredMenu: 'system' }
        },
        {
          path: 'system/orgs',
          name: 'system-orgs',
          component: SystemOrgsView,
          meta: { requiredMenu: 'system' }
        },
        {
          path: 'system/roles',
          name: 'system-roles',
          component: SystemRolesView,
          meta: { requiredMenu: 'system' }
        },
        {
          path: 'system/files',
          name: 'system-files',
          component: SystemFilesView,
          meta: { requiredMenu: 'system' }
        },
        {
          path: 'system/workflows',
          name: 'system-workflows',
          component: SystemWorkflowDefinitionsView,
          meta: { requiredMenu: 'system' }
        }
      ]
    }
  ]
});

router.beforeEach(async (to) => {
  const appStore = useAppStore();
  await appStore.ensureAuth();
  if (to.meta.requiresAuth && !appStore.isLoggedIn) {
    return { name: 'login' };
  }
  if (to.name === 'login' && appStore.isLoggedIn) {
    return { name: 'dashboard' };
  }
  const requiredMenu = typeof to.meta.requiredMenu === 'string' ? to.meta.requiredMenu : '';
  if (requiredMenu && !appStore.menus.includes(requiredMenu)) {
    return { name: 'dashboard' };
  }
  return true;
});

export default router;
