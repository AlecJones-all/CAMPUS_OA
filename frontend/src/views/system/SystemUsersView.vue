<template>
  <div class="app-page">
    <PageHero eyebrow="系统管理" title="用户管理" description="维护用户账号、组织归属和角色分配。">
      <template #actions>
        <el-button type="primary" @click="openCreate">新建用户</el-button>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

      <SectionCard title="用户列表" description="查看用户并维护账号状态。">
      <div v-if="users.length > 0" class="list-shell">
        <article v-for="item in users" :key="item.id" class="record-card">
          <div class="record-card__main">
            <div class="record-card__title">
              <h3>{{ item.realName }}</h3>
              <el-tag :type="item.status === 1 ? 'success' : 'info'" effect="light" round>
                {{ item.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="record-meta">
              <span>账号：<strong>{{ item.username }}</strong></span>
              <span>类型：<strong>{{ item.userType }}</strong></span>
              <span>组织：<strong>{{ item.orgName || '未分配' }}</strong></span>
            </div>
            <div class="record-meta">
              <span>角色：<strong>{{ item.roleNames.join(' / ') || '未分配' }}</strong></span>
              <span>更新时间：<strong>{{ item.updatedAt }}</strong></span>
            </div>
          </div>
          <div class="record-card__actions">
            <el-button @click="editUser(item)">编辑</el-button>
            <ActionMenu :items="buildUserActions(item)" @select="handleUserAction" />
          </div>
        </article>
      </div>
      <EmptyStateBlock
        v-else
        title="暂无用户"
        description="先创建平台账号。"
        badge="用户"
      >
        <template #actions>
          <el-button type="primary" @click="openCreate">创建首个用户</el-button>
        </template>
      </EmptyStateBlock>
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </SectionCard>

    <el-drawer v-model="drawerVisible" size="520px" :title="form.id ? '编辑用户' : '新建用户'">
      <div class="form-stack">
        <SectionCard title="基础信息" description="填写账号基础信息。">
          <div class="compact-form-grid">
            <el-input v-model.trim="form.username" placeholder="用户名" />
            <el-input v-if="!form.id" v-model.trim="form.password" type="password" placeholder="初始密码" show-password />
            <el-input v-model.trim="form.realName" placeholder="姓名" />
            <el-input v-model.trim="form.userType" placeholder="用户类型，如 STUDENT / TEACHER" />
            <el-select v-model="form.orgId" placeholder="所属组织" clearable>
              <el-option v-for="item in orgOptions" :key="item.id" :label="item.label" :value="String(item.id)" />
            </el-select>
            <el-select v-model="form.status" placeholder="状态">
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="停用" />
            </el-select>
            <el-input v-model.trim="form.phone" placeholder="手机号" />
            <el-input v-model.trim="form.email" placeholder="邮箱" />
          </div>
        </SectionCard>

        <SectionCard title="角色配置" description="选择当前用户的角色。">
          <el-select v-model="form.roleIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="`${role.roleName} (${role.roleCode})`" :value="role.id" />
          </el-select>
        </SectionCard>

        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

        <div class="sticky-action-bar">
          <div class="app-actions app-actions--end">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveUser">
              {{ saving ? '保存中...' : form.id ? '保存修改' : '创建用户' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import ActionMenu from '../../components/ActionMenu.vue';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import {
  assignSystemUserRoles,
  createSystemUser,
  listSystemOrgs,
  listSystemRoles,
  listSystemUsers,
  updateSystemUser,
  updateSystemUserStatus,
  type OrgNode,
  type SystemRoleRecord,
  type SystemUserRecord
} from '../../api/http';

const users = ref<SystemUserRecord[]>([]);
const roles = ref<SystemRoleRecord[]>([]);
const orgTree = ref<OrgNode[]>([]);
const orgOptions = ref<Array<{ id: number; label: string }>>([]);
const saving = ref(false);
const drawerVisible = ref(false);
const errorMessage = ref('');

const form = reactive({
  id: 0,
  username: '',
  password: '123456',
  realName: '',
  userType: 'STUDENT',
  orgId: '',
  phone: '',
  email: '',
  status: 1,
  roleIds: [] as number[]
});

const stats = computed(() => [
  { label: '用户总数', value: users.value.length, hint: '当前系统账号数量' },
  { label: '启用账号', value: users.value.filter((item) => item.status === 1).length, hint: '可登录账号' },
  { label: '角色数量', value: roles.value.length, hint: '可分配角色数' }
]);

async function loadData() {
  errorMessage.value = '';
  try {
    const [userRes, roleRes, orgRes] = await Promise.all([listSystemUsers(), listSystemRoles(), listSystemOrgs()]);
    users.value = userRes.data;
    roles.value = roleRes.data;
    orgTree.value = orgRes.data;
    orgOptions.value = flattenOrgOptions(orgTree.value);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户数据加载失败';
  }
}

function flattenOrgOptions(nodes: OrgNode[], depth = 0): Array<{ id: number; label: string }> {
  const rows: Array<{ id: number; label: string }> = [];
  for (const node of nodes) {
    rows.push({ id: node.id, label: `${'　'.repeat(depth)}${node.orgName}` });
    rows.push(...flattenOrgOptions(node.children ?? [], depth + 1));
  }
  return rows;
}

function resetForm() {
  form.id = 0;
  form.username = '';
  form.password = '123456';
  form.realName = '';
  form.userType = 'STUDENT';
  form.orgId = '';
  form.phone = '';
  form.email = '';
  form.status = 1;
  form.roleIds = [];
  errorMessage.value = '';
}

function openCreate() {
  resetForm();
  drawerVisible.value = true;
}

function editUser(user: SystemUserRecord) {
  form.id = user.id;
  form.username = user.username;
  form.password = '123456';
  form.realName = user.realName;
  form.userType = user.userType;
  form.orgId = user.orgId ? String(user.orgId) : '';
  form.phone = user.phone ?? '';
  form.email = user.email ?? '';
  form.status = user.status;
  form.roleIds = [...user.roleIds];
  errorMessage.value = '';
  drawerVisible.value = true;
}

function buildUserActions(user: SystemUserRecord) {
  return [
    {
      key: `status:${user.id}:${user.status === 1 ? 0 : 1}`,
      label: user.status === 1 ? '停用账号' : '启用账号',
      danger: user.status === 1
    }
  ];
}

async function toggleStatus(userId: number, status: number) {
  errorMessage.value = '';
  try {
    await updateSystemUserStatus(userId, status);
    await loadData();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户状态更新失败';
  }
}

function handleUserAction(command: string) {
  const [action, rawId, rawStatus] = command.split(':');
  if (action === 'status') {
    void toggleStatus(Number(rawId), Number(rawStatus));
  }
}

async function saveUser() {
  if (!form.username.trim() || !form.realName.trim()) {
    errorMessage.value = '请先填写用户名和姓名';
    return;
  }
  if (!form.id && !form.password.trim()) {
    errorMessage.value = '新建用户时必须填写初始密码';
    return;
  }
  if (form.roleIds.length === 0) {
    errorMessage.value = '请至少选择一个角色';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  const payload = {
    username: form.username,
    password: form.password,
    realName: form.realName,
    userType: form.userType,
    orgId: form.orgId ? Number(form.orgId) : null,
    phone: form.phone,
    email: form.email,
    status: form.status,
    roleIds: form.roleIds
  };

  try {
    if (form.id) {
      await updateSystemUser(form.id, payload);
      await assignSystemUserRoles(form.id, form.roleIds);
    } else {
      const response = await createSystemUser(payload);
      await assignSystemUserRoles(response.data.id, form.roleIds);
    }
    await loadData();
    drawerVisible.value = false;
    resetForm();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户保存失败';
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadData();
});
</script>
