<template>
  <div class="app-page">
    <PageHero
      eyebrow="系统管理"
      title="流程模板管理"
      description="维护流程模板和审批节点。"
    >
      <template #actions>
        <el-button type="primary" @click="createDefinition">新建模板</el-button>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <div class="split-layout">
      <SectionCard title="模板列表" description="选择模板后配置对应节点。">
        <div v-if="definitions.length > 0" class="selection-list">
          <article
            v-for="item in definitions"
            :key="item.id"
            class="selection-card"
            :class="{ 'is-active': definitionForm.id === item.id }"
            @click="selectDefinition(item.id)"
          >
            <div class="selection-card__title">
              <h3>{{ item.definitionName }}</h3>
              <el-tag :type="item.status === 1 ? 'success' : 'info'" effect="light" round>
                {{ item.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="selection-card__meta">
              <span>业务类型：{{ item.businessType }}</span>
              <span>版本：{{ item.versionNo }}</span>
              <span>节点数：{{ item.nodeCount }}</span>
            </div>
            <div class="selection-card__footer">
              <p class="muted-text">点击右侧继续配置模板信息和审批节点。</p>
              <el-button @click.stop="selectDefinition(item.id)">配置</el-button>
            </div>
          </article>
        </div>
        <EmptyStateBlock
          v-else
          title="暂无流程模板"
          description="先补齐常用业务的流程模板。"
          badge="流程"
        >
          <template #actions>
            <el-button type="primary" @click="createDefinition">创建首个模板</el-button>
          </template>
        </EmptyStateBlock>
        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      </SectionCard>

      <SectionCard title="模板配置" description="分开维护模板信息和审批节点。">
        <template #actions>
          <div class="app-actions">
            <el-button v-if="activeTab === 'basic'" type="primary" :loading="saving" @click="saveDefinition">
              {{ saving ? '保存中...' : definitionForm.id ? '保存模板' : '创建模板' }}
            </el-button>
            <template v-else>
              <el-button :disabled="!definitionForm.id" @click="addNode">新增节点</el-button>
              <el-button type="primary" :loading="saving" :disabled="!definitionForm.id" @click="saveNodes">
                保存节点
              </el-button>
            </template>
          </div>
        </template>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础信息" name="basic">
            <div class="form-stack">
              <div class="compact-form-grid">
                <el-select v-model="definitionForm.businessType" placeholder="业务类型">
                  <el-option v-for="item in workflowTypes" :key="item.typeCode" :label="item.typeName" :value="item.typeCode" />
                </el-select>
                <el-input v-model.trim="definitionForm.definitionCode" placeholder="流程编码" />
                <el-input v-model.trim="definitionForm.definitionName" placeholder="流程名称" />
                <el-input-number v-model="definitionForm.versionNo" :min="1" :controls="false" />
                <el-select v-model="definitionForm.status" placeholder="状态">
                  <el-option :value="1" label="启用" />
                  <el-option :value="0" label="停用" />
                </el-select>
              </div>
              <div class="side-note">
                保存基础信息后，可继续配置审批节点。当前仅支持角色节点。
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="节点配置" name="nodes">
            <EmptyStateBlock
              v-if="!definitionForm.id"
              title="请先创建模板"
              description="保存模板后再配置节点。"
              badge="节点"
            />
            <div v-else-if="nodes.length > 0" class="selection-list">
              <article v-for="(node, index) in nodes" :key="`${node.nodeCode}-${index}`" class="selection-card node-card">
                <div class="compact-form-grid">
                  <el-input v-model.trim="node.nodeCode" placeholder="节点编码" />
                  <el-input v-model.trim="node.nodeName" placeholder="节点名称" />
                  <el-select v-model="node.approverRoleCode" placeholder="审批角色">
                    <el-option v-for="role in roles" :key="role.id" :label="`${role.roleName} (${role.roleCode})`" :value="role.roleCode" />
                  </el-select>
                  <el-input-number v-model="node.sortNo" :min="1" :controls="false" />
                  <el-select v-model="node.status" placeholder="状态">
                    <el-option :value="1" label="启用" />
                    <el-option :value="0" label="停用" />
                  </el-select>
                </div>
                <div class="selection-card__footer">
                  <p class="muted-text">按排序号串联审批顺序。</p>
                  <el-button type="danger" plain @click="removeNode(index)">删除节点</el-button>
                </div>
              </article>
            </div>
            <EmptyStateBlock
              v-else
              title="当前模板还没有节点"
              description="请先添加至少一个审批节点。"
              badge="审批"
            >
              <template #actions>
                <el-button type="primary" @click="addNode">新增第一个节点</el-button>
              </template>
            </EmptyStateBlock>
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
  createWorkflowDefinition,
  getWorkflowTypes,
  listSystemRoles,
  listWorkflowDefinitions,
  listWorkflowNodes,
  saveWorkflowNodes,
  updateWorkflowDefinition,
  type ApplicationType,
  type SystemRoleRecord,
  type WorkflowDefinitionRecord,
  type WorkflowNodeRecord
} from '../../api/http';

type EditableNode = WorkflowNodeRecord;

const workflowTypes = ref<ApplicationType[]>([]);
const roles = ref<SystemRoleRecord[]>([]);
const definitions = ref<WorkflowDefinitionRecord[]>([]);
const nodes = ref<EditableNode[]>([]);
const saving = ref(false);
const activeTab = ref('basic');
const errorMessage = ref('');

const definitionForm = reactive({
  id: 0,
  businessType: '',
  definitionCode: '',
  definitionName: '',
  versionNo: 1,
  status: 1
});

const stats = computed(() => [
  { label: '模板数量', value: definitions.value.length, hint: '当前流程模板数' },
  { label: '业务类型', value: workflowTypes.value.length, hint: '可绑定业务类型数' },
  { label: '角色数量', value: roles.value.length, hint: '可分配审批角色数' }
]);

async function loadData() {
  errorMessage.value = '';
  try {
    const [typeRes, roleRes, definitionRes] = await Promise.all([
      getWorkflowTypes(),
      listSystemRoles(),
      listWorkflowDefinitions()
    ]);
    workflowTypes.value = typeRes.data;
    roles.value = roleRes.data;
    definitions.value = definitionRes.data;

    if (!definitionForm.id && definitions.value.length > 0) {
      await selectDefinition(definitions.value[0].id);
    } else if (definitionForm.id) {
      const matched = definitions.value.find((item) => item.id === definitionForm.id);
      if (matched) {
        fillDefinition(matched);
      }
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程模板数据加载失败';
  }
}

function fillDefinition(item: WorkflowDefinitionRecord) {
  definitionForm.id = item.id;
  definitionForm.businessType = item.businessType;
  definitionForm.definitionCode = item.definitionCode;
  definitionForm.definitionName = item.definitionName;
  definitionForm.versionNo = item.versionNo;
  definitionForm.status = item.status;
}

function resetDefinition() {
  definitionForm.id = 0;
  definitionForm.businessType = '';
  definitionForm.definitionCode = '';
  definitionForm.definitionName = '';
  definitionForm.versionNo = 1;
  definitionForm.status = 1;
  nodes.value = [];
  errorMessage.value = '';
}

function createDefinition() {
  resetDefinition();
  activeTab.value = 'basic';
}

async function selectDefinition(id: number) {
  const item = definitions.value.find((definition) => definition.id === id);
  if (!item) {
    return;
  }
  fillDefinition(item);
  activeTab.value = 'basic';
  await loadNodes(id);
}

async function loadNodes(definitionId: number) {
  errorMessage.value = '';
  try {
    const response = await listWorkflowNodes(definitionId);
    nodes.value = response.data.map((item) => ({ ...item }));
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程节点加载失败';
  }
}

function addNode() {
  nodes.value.push({
    id: Date.now(),
    nodeCode: `APPROVE_${nodes.value.length + 1}`,
    nodeName: `审批节点${nodes.value.length + 1}`,
    nodeType: 'APPROVAL',
    approverType: 'ROLE',
    approverRoleCode: '',
    sortNo: nodes.value.length + 1,
    status: 1
  });
}

function removeNode(index: number) {
  nodes.value.splice(index, 1);
}

async function saveDefinition() {
  if (!definitionForm.businessType || !definitionForm.definitionCode.trim() || !definitionForm.definitionName.trim()) {
    errorMessage.value = '请先填写业务类型、流程编码和流程名称';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  const payload = {
    businessType: definitionForm.businessType,
    definitionCode: definitionForm.definitionCode,
    definitionName: definitionForm.definitionName,
    versionNo: definitionForm.versionNo,
    status: definitionForm.status
  };

  try {
    if (definitionForm.id) {
      await updateWorkflowDefinition(definitionForm.id, payload);
    } else {
      const response = await createWorkflowDefinition(payload);
      definitionForm.id = response.data.id;
    }
    await loadData();
    if (definitionForm.id) {
      await loadNodes(definitionForm.id);
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程模板保存失败';
  } finally {
    saving.value = false;
  }
}

async function saveNodes() {
  if (!definitionForm.id) {
    errorMessage.value = '请先创建模板';
    return;
  }
  if (nodes.value.length === 0) {
    errorMessage.value = '请至少保留一个审批节点';
    return;
  }
  if (nodes.value.some((node) => !node.nodeCode.trim() || !node.nodeName.trim() || !node.approverRoleCode)) {
    errorMessage.value = '请完善节点编码、名称和审批角色';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  try {
    await saveWorkflowNodes(
      definitionForm.id,
      nodes.value.map((node) => ({
        nodeCode: node.nodeCode,
        nodeName: node.nodeName,
        nodeType: 'APPROVAL',
        approverType: 'ROLE',
        approverRoleCode: node.approverRoleCode,
        sortNo: node.sortNo,
        status: node.status
      }))
    );
    await loadData();
    await loadNodes(definitionForm.id);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程节点保存失败';
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadData();
});
</script>

<style scoped>
.node-card {
  cursor: default;
}
</style>
