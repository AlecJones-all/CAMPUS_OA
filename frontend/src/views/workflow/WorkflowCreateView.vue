<template>
  <div class="app-page">
    <PageHero eyebrow="流程发起" title="新建申请" description="填写申请类型、标题和申请内容后提交。">
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <form class="app-page" @submit.prevent="saveDraft">
      <SectionCard title="申请基础信息" description="填写当前申请的基础内容。">
        <div class="page-form-grid">
          <label>
            <span>申请类型</span>
            <select v-model="form.typeId" required>
              <option value="" disabled>请选择申请类型</option>
              <option v-for="item in types" :key="item.id" :value="String(item.id)">{{ item.typeName }}</option>
            </select>
          </label>

          <label>
            <span>申请标题</span>
            <input v-model.trim="form.title" type="text" maxlength="200" required />
          </label>

          <label class="span-two">
            <span>申请内容</span>
            <textarea v-model.trim="form.content" rows="8" required />
          </label>
        </div>
      </SectionCard>

      <div class="sticky-action-bar">
        <div class="app-actions app-actions--end">
          <RouterLink :to="{ name: 'workflow-applications' }">
            <el-button>返回</el-button>
          </RouterLink>
          <el-button type="primary" plain :loading="submitting" native-type="submit">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click.prevent="saveAndSubmit">提交申请</el-button>
        </div>
        <p v-if="errorMessage" class="error-text" style="margin-top: 12px">{{ errorMessage }}</p>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import SummaryStats from '../../components/SummaryStats.vue';
import { createWorkflowApplication, getWorkflowTypes, submitWorkflowApplication, type ApplicationType } from '../../api/http';

const router = useRouter();
const submitting = ref(false);
const errorMessage = ref('');
const types = ref<ApplicationType[]>([]);

const form = reactive({
  typeId: '',
  title: '',
  content: ''
});

const stats = computed(() => [
  { label: '类型数量', value: types.value.length, hint: '可选流程类型' },
  { label: '默认状态', value: '草稿', hint: '保存后先生成申请单' },
  { label: '主操作', value: '提交申请', hint: '提交后进入审批流程' }
]);

async function loadTypes() {
  const response = await getWorkflowTypes();
  types.value = response.data;
}

async function createDraftOnly() {
  if (!form.typeId) {
    throw new Error('请选择申请类型');
  }
  const response = await createWorkflowApplication({
    typeId: Number(form.typeId),
    title: form.title,
    content: form.content
  });
  return response.data.id;
}

async function saveDraft() {
  submitting.value = true;
  errorMessage.value = '';
  try {
    const id = await createDraftOnly();
    await router.push({ name: 'workflow-detail', params: { id } });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '保存草稿失败';
  } finally {
    submitting.value = false;
  }
}

async function saveAndSubmit() {
  submitting.value = true;
  errorMessage.value = '';
  try {
    const id = await createDraftOnly();
    await submitWorkflowApplication(id);
    await router.push({ name: 'workflow-detail', params: { id } });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '提交申请失败';
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  void loadTypes().catch((error) => {
    errorMessage.value = error instanceof Error ? error.message : '申请类型加载失败';
  });
});
</script>

<style scoped>
label {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

input,
select,
textarea {
  border: 1px solid var(--app-border);
  border-radius: 14px;
  padding: 12px 14px;
  background: #fff;
}
</style>
