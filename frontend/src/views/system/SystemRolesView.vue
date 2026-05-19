<template>
  <div class="app-page">
    <PageHero
      eyebrow="系统管理"
      title="角色与权限管理"
      description="配置角色、菜单和按钮权限。"
    >
      <template #actions>
        <el-button type="primary" @click="createRole">新建角色</el-button>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <div class="split-layout">
      <SectionCard title="角色列表" description="选择角色后配置对应权限。">
        <div v-if="roles.length > 0" class="selection-list">
          <article
            v-for="item in roles"
            :key="item.id"
            class="selection-card"
            :class="{ 'is-active': form.id === item.id }"
            @click="selectRole(item)"
          >
            <div class="selection-card__title">
              <h3>{{ item.roleName }}</h3>
              <el-tag :type="item.status === 1 ? 'success' : 'info'" effect="light" round>
                {{ item.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="selection-card__meta">
              <span>编码：{{ item.roleCode }}</span>
              <span>菜单：{{ item.assignedMenuIds.length }}</span>
              <span>权限：{{ item.assignedPermissionIds.length }}</span>
            </div>
            <div class="selection-card__footer">
              <p class="muted-text">点击右侧继续配置基础信息、菜单权限和按钮权限。</p>
              <el-button @click.stop="selectRole(item)">配置</el-button>
            </div>
          </article>
        </div>
        <EmptyStateBlock
          v-else
          title="暂无角色"
          description="先创建学生、教师或管理员角色。"
          badge="角色"
        >
          <template #actions>
            <el-button type="primary" @click="createRole">创建首个角色</el-button>
          </template>
        </EmptyStateBlock>
        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      </SectionCard>

      <SectionCard title="角色配置" description="按基础信息、菜单权限和按钮权限分组配置。">
        <template #actions>
          <div class="app-actions">
            <el-button v-if="activeTab === 'basic'" type="primary" :loading="saving" @click="saveRole">
              {{ saving ? '保存中...' : form.id ? '保存角色' : '创建角色' }}
            </el-button>
            <el-button
              v-if="activeTab === 'menus'"
              type="primary"
              :loading="saving"
              :disabled="!form.id"
              @click="saveMenus"
            >
              保存菜单权限
            </el-button>
            <el-button
              v-if="activeTab === 'permissions'"
              type="primary"
              :loading="saving"
              :disabled="!form.id"
              @click="savePermissions"
            >
              保存按钮权限
            </el-button>
          </div>
        </template>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础信息" name="basic">
            <div class="form-stack">
              <div class="compact-form-grid">
                <el-input v-model.trim="form.roleCode" placeholder="角色编码" />
                <el-input v-model.trim="form.roleName" placeholder="角色名称" />
                <el-select v-model="form.status" placeholder="状态">
                  <el-option :value="1" label="启用" />
                  <el-option :value="0" label="停用" />
                </el-select>
              </div>
              <div class="side-note">
                先保存角色基础信息，再配置菜单和按钮权限。
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="菜单权限" name="menus">
            <EmptyStateBlock
              v-if="!form.id"
              title="请先创建角色"
              description="保存角色后再分配菜单权限。"
              badge="菜单"
            />
            <div v-else class="form-stack">
              <SectionCard
                v-for="group in menuGroups"
                :key="group.label"
                :title="group.label"
                :description="`该组下共有 ${group.items.length} 个可配置菜单。`"
              >
                <el-checkbox-group v-model="form.assignedMenuIds" class="check-columns">
                  <label v-for="menu in group.items" :key="menu.id" class="check-item-card">
                    <el-checkbox :label="menu.id" />
                    <div>
                      <strong>{{ menu.menuName }}</strong>
                      <span>{{ menu.routePath || menu.permissionCode || '系统入口' }}</span>
                    </div>
                  </label>
                </el-checkbox-group>
              </SectionCard>
            </div>
          </el-tab-pane>

          <el-tab-pane label="按钮权限" name="permissions">
            <EmptyStateBlock
              v-if="!form.id"
              title="请先创建角色"
              description="保存角色后再分配按钮或接口权限。"
              badge="权限"
            />
            <div v-else class="form-stack">
              <SectionCard
                v-for="group in permissionGroups"
                :key="group.label"
                :title="group.label"
                :description="`当前分组包含 ${group.items.length} 个权限点。`"
              >
                <el-checkbox-group v-model="form.assignedPermissionIds" class="check-columns">
                  <label v-for="permission in group.items" :key="permission.id" class="check-item-card">
                    <el-checkbox :label="permission.id" />
                    <div>
                      <strong>{{ permission.permissionName }}</strong>
                      <span>{{ permission.permissionCode }}</span>
                    </div>
                  </label>
                </el-checkbox-group>
              </SectionCard>
            </div>
          </el-tab-pane>
        </el-tabs>
      </SectionCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import {
  assignSystemRoleMenus,
  assignSystemRolePermissions,
  createSystemRole,
  listSystemMenus,
  listSystemPermissions,
  listSystemRoles,
  updateSystemRole,
  type SystemMenuRecord,
  type SystemPermissionRecord,
  type SystemRoleRecord
} from '../../api/http';

const roles = ref<SystemRoleRecord[]>([]);
const menus = ref<SystemMenuRecord[]>([]);
const permissions = ref<SystemPermissionRecord[]>([]);
const saving = ref(false);
const activeTab = ref('basic');
const errorMessage = ref('');

const form = reactive({
  id: 0,
  roleCode: '',
  roleName: '',
  status: 1,
  assignedMenuIds: [] as number[],
  assignedPermissionIds: [] as number[]
});

const menuMap = computed(() => {
  const map = new Map<number, SystemMenuRecord>();
  menus.value.forEach((item) => map.set(item.id, item));
  return map;
});

const stats = computed(() => [
  { label: '角色数量', value: roles.value.length, hint: '当前可维护角色数' },
  { label: '菜单数量', value: menus.value.length, hint: '可分配菜单入口' },
  { label: '权限点', value: permissions.value.length, hint: '可分配按钮/接口权限' }
]);

const menuGroups = computed(() => {
  const groups = new Map<string, SystemMenuRecord[]>();
  for (const menu of menus.value) {
    const parentName = menu.parentId ? menuMap.value.get(menu.parentId)?.menuName : '';
    const label = parentName || menu.menuName;
    if (!groups.has(label)) {
      groups.set(label, []);
    }
    groups.get(label)?.push(menu);
  }
  return Array.from(groups.entries()).map(([label, items]) => ({
    label,
    items: items.sort((a, b) => a.sortNo - b.sortNo)
  }));
});

const permissionGroups = computed(() => {
  const groups = new Map<string, SystemPermissionRecord[]>();
  for (const permission of permissions.value) {
    const label = permission.permissionGroup || '未分组权限';
    if (!groups.has(label)) {
      groups.set(label, []);
    }
    groups.get(label)?.push(permission);
  }
  return Array.from(groups.entries()).map(([label, items]) => ({ label, items }));
});

async function loadData() {
  errorMessage.value = '';
  try {
    const [roleRes, menuRes, permissionRes] = await Promise.all([
      listSystemRoles(),
      listSystemMenus(),
      listSystemPermissions()
    ]);
    roles.value = roleRes.data;
    menus.value = menuRes.data;
    permissions.value = permissionRes.data;
    if (!form.id && roles.value.length > 0) {
      selectRole(roles.value[0]);
    } else if (form.id) {
      const matched = roles.value.find((item) => item.id === form.id);
      if (matched) {
        selectRole(matched, false);
      }
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '角色权限数据加载失败';
  }
}

function fillForm(role: SystemRoleRecord) {
  form.id = role.id;
  form.roleCode = role.roleCode;
  form.roleName = role.roleName;
  form.status = role.status;
  form.assignedMenuIds = [...role.assignedMenuIds];
  form.assignedPermissionIds = [...role.assignedPermissionIds];
}

function selectRole(role: SystemRoleRecord, switchTab = true) {
  fillForm(role);
  errorMessage.value = '';
  if (switchTab) {
    activeTab.value = 'basic';
  }
}

function createRole() {
  form.id = 0;
  form.roleCode = '';
  form.roleName = '';
  form.status = 1;
  form.assignedMenuIds = [];
  form.assignedPermissionIds = [];
  errorMessage.value = '';
  activeTab.value = 'basic';
}

async function saveRole() {
  if (!form.roleCode.trim() || !form.roleName.trim()) {
    errorMessage.value = '请先填写角色编码和角色名称';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  const payload = {
    roleCode: form.roleCode,
    roleName: form.roleName,
    status: form.status
  };

  try {
    let roleId = form.id;
    if (roleId) {
      await updateSystemRole(roleId, payload);
    } else {
      const response = await createSystemRole(payload);
      roleId = response.data.id;
      form.id = roleId;
    }
    await loadData();
    const matched = roles.value.find((item) => item.id === roleId);
    if (matched) {
      selectRole(matched, false);
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '角色保存失败';
  } finally {
    saving.value = false;
  }
}

async function saveMenus() {
  if (!form.id) {
    errorMessage.value = '请先创建角色';
    return;
  }
  if (form.assignedMenuIds.length === 0) {
    errorMessage.value = '请至少选择一个菜单';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  try {
    await assignSystemRoleMenus(form.id, form.assignedMenuIds);
    await loadData();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜单权限保存失败';
  } finally {
    saving.value = false;
  }
}

async function savePermissions() {
  if (!form.id) {
    errorMessage.value = '请先创建角色';
    return;
  }
  if (form.assignedPermissionIds.length === 0) {
    errorMessage.value = '请至少选择一个权限点';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  try {
    await assignSystemRolePermissions(form.id, form.assignedPermissionIds);
    await loadData();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '按钮权限保存失败';
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadData();
});
</script>
