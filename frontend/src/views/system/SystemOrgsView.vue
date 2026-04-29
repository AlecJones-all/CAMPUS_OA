<template>
  <div class="app-page">
    <PageHero eyebrow="系统管理" title="组织管理" description="维护学校、学院、部门和班级等组织结构。">
      <template #actions>
        <el-button type="primary" @click="openCreate">新建组织</el-button>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

      <SectionCard title="组织结构" description="查看组织树并维护节点。">
      <div v-if="flatRows.length > 0" class="selection-list">
        <article
          v-for="item in flatRows"
          :key="item.id"
          class="selection-card"
          :class="{ 'is-active': form.id === item.id && drawerVisible }"
          @click="editOrg(item)"
        >
          <div class="selection-card__title">
            <h3 :style="{ paddingLeft: `${item.depth * 20}px` }">{{ item.orgName }}</h3>
            <el-tag :type="item.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ item.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </div>
          <div class="selection-card__meta">
            <span>编码：{{ item.orgCode }}</span>
            <span>类型：{{ item.orgType }}</span>
            <span>排序：{{ item.sortNo }}</span>
          </div>
          <div class="selection-card__footer">
            <p class="muted-text">{{ item.parentId ? '点击查看并编辑当前节点' : '顶级组织节点' }}</p>
            <el-button @click.stop="editOrg(item)">编辑</el-button>
          </div>
        </article>
      </div>
      <EmptyStateBlock
        v-else
        title="暂无组织数据"
        description="先创建顶级组织或部门节点。"
        badge="组织"
      >
        <template #actions>
          <el-button type="primary" @click="openCreate">创建首个组织</el-button>
        </template>
      </EmptyStateBlock>
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </SectionCard>

    <el-drawer v-model="drawerVisible" size="520px" :title="form.id ? '编辑组织' : '新建组织'">
      <div class="form-stack">
        <SectionCard title="组织信息" description="填写当前组织节点的基础信息。">
          <div class="compact-form-grid">
            <el-select v-model="form.parentId" clearable placeholder="上级组织">
              <el-option label="顶级组织" value="" />
              <el-option v-for="item in orgOptions" :key="item.id" :label="item.label" :value="String(item.id)" />
            </el-select>
            <el-input v-model.trim="form.orgCode" placeholder="组织编码" />
            <el-input v-model.trim="form.orgName" placeholder="组织名称" />
            <el-input v-model.trim="form.orgType" placeholder="组织类型，如 COLLEGE / DEPARTMENT" />
            <el-input-number v-model="form.sortNo" :min="0" :controls="false" placeholder="排序号" />
            <el-select v-model="form.status" placeholder="状态">
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="停用" />
            </el-select>
          </div>
        </SectionCard>

        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

        <div class="sticky-action-bar">
          <div class="app-actions app-actions--end">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveOrg">
              {{ saving ? '保存中...' : form.id ? '保存修改' : '创建组织' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import { createSystemOrg, listSystemOrgs, updateSystemOrg, type OrgNode } from '../../api/http';

type FlatOrgNode = OrgNode & { depth: number };

const tree = ref<OrgNode[]>([]);
const flatRows = ref<FlatOrgNode[]>([]);
const orgOptions = ref<Array<{ id: number; label: string }>>([]);
const saving = ref(false);
const drawerVisible = ref(false);
const errorMessage = ref('');

const form = reactive({
  id: 0,
  parentId: '',
  orgCode: '',
  orgName: '',
  orgType: 'DEPARTMENT',
  sortNo: 0,
  status: 1
});

const rootCount = computed(() => tree.value.length);
const orgTypes = computed(() => Array.from(new Set(flatRows.value.map((item) => item.orgType))).filter(Boolean));
const stats = computed(() => [
  { label: '组织节点', value: flatRows.value.length, hint: '当前组织总节点数' },
  { label: '顶级组织', value: rootCount.value, hint: '根节点数量' },
  { label: '组织类型', value: orgTypes.value.length, hint: '当前使用的类型数' }
]);

async function loadData() {
  errorMessage.value = '';
  try {
    const response = await listSystemOrgs();
    tree.value = response.data;
    flatRows.value = flattenTree(response.data);
    orgOptions.value = flattenOptions(response.data);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '组织数据加载失败';
  }
}

function flattenTree(nodes: OrgNode[], depth = 0): FlatOrgNode[] {
  const rows: FlatOrgNode[] = [];
  for (const node of nodes) {
    rows.push({ ...node, depth });
    rows.push(...flattenTree(node.children ?? [], depth + 1));
  }
  return rows;
}

function flattenOptions(nodes: OrgNode[], depth = 0): Array<{ id: number; label: string }> {
  const rows: Array<{ id: number; label: string }> = [];
  for (const node of nodes) {
    rows.push({ id: node.id, label: `${'　'.repeat(depth)}${node.orgName}` });
    rows.push(...flattenOptions(node.children ?? [], depth + 1));
  }
  return rows;
}

function resetForm() {
  form.id = 0;
  form.parentId = '';
  form.orgCode = '';
  form.orgName = '';
  form.orgType = 'DEPARTMENT';
  form.sortNo = 0;
  form.status = 1;
  errorMessage.value = '';
}

function openCreate() {
  resetForm();
  drawerVisible.value = true;
}

function editOrg(org: FlatOrgNode) {
  form.id = org.id;
  form.parentId = org.parentId ? String(org.parentId) : '';
  form.orgCode = org.orgCode;
  form.orgName = org.orgName;
  form.orgType = org.orgType;
  form.sortNo = org.sortNo;
  form.status = org.status;
  errorMessage.value = '';
  drawerVisible.value = true;
}

async function saveOrg() {
  if (!form.orgCode.trim() || !form.orgName.trim()) {
    errorMessage.value = '请先填写组织编码和组织名称';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  const payload = {
    parentId: form.parentId ? Number(form.parentId) : null,
    orgCode: form.orgCode,
    orgName: form.orgName,
    orgType: form.orgType,
    sortNo: form.sortNo,
    status: form.status
  };

  try {
    if (form.id) {
      await updateSystemOrg(form.id, payload);
    } else {
      await createSystemOrg(payload);
    }
    await loadData();
    drawerVisible.value = false;
    resetForm();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '组织保存失败';
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadData();
});
</script>
