<template>
  <div v-if="moduleUnavailable" class="app-page">
    <PageHero
      eyebrow="新建业务"
      :title="moduleDef?.name ?? '新建业务'"
      description="该业务暂未开放，不能继续新建申请。"
    />

    <SectionCard title="业务暂未开放" description="请选择当前开放的业务入口。">
      <p class="muted-text">
        该业务暂未开放，请返回工作台或业务入口。
      </p>
      <div class="app-actions">
        <RouterLink :to="backTarget">
          <el-button type="primary">返回业务入口</el-button>
        </RouterLink>
      </div>
    </SectionCard>
  </div>

  <div v-else class="app-page">
    <PageHero
      eyebrow="新建业务"
      :title="moduleDef?.name ?? '新建业务'"
      :description="moduleDef?.description ?? '填写业务信息并提交审批。'"
    />

    <form class="app-page" @submit.prevent="saveDraft">
      <SectionCard title="基础信息" description="请填写申请所需信息。">
        <div class="page-form-grid">
          <label v-for="field in primaryFields" :key="field.key">
            <span>{{ field.label }}</span>
            <input
              v-if="field.type === 'text' || field.type === 'date' || field.type === 'datetime-local' || field.type === 'number'"
              v-model.trim="formData[field.key]"
              :type="field.type"
              :required="field.required"
            />
            <textarea
              v-else-if="field.type === 'textarea'"
              v-model.trim="formData[field.key]"
              rows="5"
              :required="field.required"
            />
            <select v-else v-model="formData[field.key]" :required="field.required">
              <option value="" disabled>请选择{{ field.label }}</option>
              <option
                v-for="option in resolveFieldOptions(field)"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </label>
        </div>
        <div v-if="selectionNotice" class="form-help" style="margin-top: 16px">
          {{ selectionNotice }}
        </div>
      </SectionCard>

      <SectionCard v-if="secondaryFields.length > 0" title="补充说明" description="请补充本次申请相关信息。">
        <div class="page-form-grid">
          <label
            v-for="field in secondaryFields"
            :key="field.key"
            :class="{ 'span-two': field.type === 'textarea' }"
          >
            <span>{{ field.label }}</span>
            <input
              v-if="field.type === 'text' || field.type === 'date' || field.type === 'datetime-local' || field.type === 'number'"
              v-model.trim="formData[field.key]"
              :type="field.type"
              :required="field.required"
            />
            <textarea
              v-else-if="field.type === 'textarea'"
              v-model.trim="formData[field.key]"
              rows="6"
              :required="field.required"
            />
            <select v-else v-model="formData[field.key]" :required="field.required">
              <option value="" disabled>请选择{{ field.label }}</option>
              <option
                v-for="option in resolveFieldOptions(field)"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </label>
        </div>
      </SectionCard>

      <SectionCard title="附件材料" description="保存后可在详情页补充附件。">
        <div class="form-help">
          保存或提交后，可进入详情页上传申请材料、证明文件和补充材料。
        </div>
      </SectionCard>

      <div class="sticky-action-bar">
        <div class="app-actions app-actions--end">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" plain :loading="submitting" :disabled="selectionBlocked" native-type="submit">
            保存草稿
          </el-button>
          <el-button type="primary" :loading="submitting" :disabled="selectionBlocked" @click.prevent="saveAndSubmit">
            提交申请
          </el-button>
        </div>
        <p v-if="errorMessage" class="error-text" style="margin-top: 12px">{{ errorMessage }}</p>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { BusinessFieldDefinition } from '../../business/modules';
import { canCreateBusinessModule, getBusinessModule, isBusinessModuleActive } from '../../business/modules';
import { createBusinessRecord, getBusinessFieldOptions, submitBusinessRecord } from '../../api/http';
import { useAppStore } from '../../stores/app';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const submitting = ref(false);
const errorMessage = ref('');
const optionLoadFailed = ref(false);
const optionLoadErrorMessage = ref('');
const formData = reactive<Record<string, string>>({});
const dynamicFieldOptions = reactive<Record<string, Array<{ label: string; value: string }>>>({});

const businessKey = computed(() => String(route.params.businessKey ?? ''));
const moduleDef = computed(() => getBusinessModule(businessKey.value));
const moduleUnavailable = computed(() => !isBusinessModuleActive(moduleDef.value));
const backTarget = computed(() =>
  moduleDef.value?.domain
    ? { name: 'module-domain', params: { domain: moduleDef.value.domain } }
    : { name: 'dashboard' }
);
const canCreate = computed(() => (moduleDef.value ? canCreateBusinessModule(moduleDef.value, appStore.roles) : false));
const allFields = computed(() => moduleDef.value?.fields ?? []);
const primaryFields = computed(() => allFields.value.slice(0, 4));
const secondaryFields = computed(() => allFields.value.slice(4));
const dynamicFieldKeys = computed(() => DYNAMIC_FIELD_KEYS[businessKey.value] ?? []);
const selectionBlocked = computed(() =>
  optionLoadFailed.value || dynamicFieldKeys.value.some((fieldKey) => resolveFieldOptionsByKey(fieldKey).length === 0)
);
const selectionNotice = computed(() => {
  if (optionLoadFailed.value) {
    return optionLoadErrorMessage.value || OPTION_LOAD_FAILURE_NOTICE[businessKey.value] || '关联选项加载失败，请稍后重试。';
  }
  if (!selectionBlocked.value) {
    return '';
  }
  return SELECTION_NOTICE[businessKey.value] ?? '当前缺少可选数据，请先补齐关联业务后再发起申请。';
});
const DYNAMIC_FIELD_KEYS: Record<string, string[]> = {
  'student-return-confirmations': ['relatedLeaveNo'],
  'graduation-project-openings': ['advisorName'],
  'graduation-project-midterms': ['projectName']
};

const SELECTION_NOTICE: Record<string, string> = {
  'student-return-confirmations': '当前没有可选择的已审批离校申请，请先完成离校申请并等待审批通过。',
  'graduation-project-openings': '当前没有可选择的指导老师，请检查教师账号与角色配置。',
  'graduation-project-midterms': '当前没有可选择的已通过开题记录，请先完成开题申请并等待审批通过。'
};

const OPTION_LOAD_FAILURE_NOTICE: Record<string, string> = {
  'student-return-confirmations': '返校关联离校单加载失败，请稍后重试或联系管理员。',
  'graduation-project-openings': '指导老师选项加载失败，请稍后重试或联系管理员。',
  'graduation-project-midterms': '开题记录选项加载失败，请稍后重试或联系管理员。'
};

function resetForm() {
  for (const field of allFields.value) {
    formData[field.key] = '';
  }
}

function clearDynamicFieldOptions() {
  for (const key of Object.keys(dynamicFieldOptions)) {
    delete dynamicFieldOptions[key];
  }
}

function resolveFieldOptions(field: BusinessFieldDefinition) {
  return field.options?.length ? field.options : resolveFieldOptionsByKey(field.key);
}

function resolveFieldOptionsByKey(fieldKey: string) {
  return dynamicFieldOptions[fieldKey] ?? [];
}

async function loadDynamicFieldOptions() {
  clearDynamicFieldOptions();
  optionLoadFailed.value = false;
  optionLoadErrorMessage.value = '';
  for (const fieldKey of dynamicFieldKeys.value) {
    try {
      const response = await getBusinessFieldOptions(businessKey.value, fieldKey);
      dynamicFieldOptions[fieldKey] = response.data;
    } catch (error) {
      optionLoadFailed.value = true;
      optionLoadErrorMessage.value = OPTION_LOAD_FAILURE_NOTICE[businessKey.value]
        ?? (error instanceof Error ? error.message : '关联选项加载失败');
      throw error;
    }
  }
}

function buildPayload() {
  return Object.fromEntries(
    Object.entries(formData).map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
  );
}

function goBack() {
  void router.push({ name: 'business-list', params: { businessKey: businessKey.value } });
}

async function createDraftOnly() {
  const response = await createBusinessRecord(businessKey.value, buildPayload());
  return response.data.id;
}

async function saveDraft() {
  if (selectionBlocked.value) {
    errorMessage.value = selectionNotice.value;
    return;
  }
  submitting.value = true;
  errorMessage.value = '';
  try {
    const id = await createDraftOnly();
    await router.push({ name: 'business-detail', params: { businessKey: businessKey.value, id } });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '保存草稿失败';
  } finally {
    submitting.value = false;
  }
}

async function saveAndSubmit() {
  if (selectionBlocked.value) {
    errorMessage.value = selectionNotice.value;
    return;
  }
  submitting.value = true;
  errorMessage.value = '';
  try {
    const id = await createDraftOnly();
    await submitBusinessRecord(businessKey.value, id);
    await router.push({ name: 'business-detail', params: { businessKey: businessKey.value, id } });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '提交业务失败';
  } finally {
    submitting.value = false;
  }
}

async function initializeForm() {
  if (moduleUnavailable.value) {
    errorMessage.value = '';
    return;
  }
  if (!canCreate.value) {
    errorMessage.value = '当前角色无权新建该业务';
    void router.replace({ name: 'business-list', params: { businessKey: businessKey.value } });
    return;
  }
  errorMessage.value = '';
  resetForm();
  try {
    await loadDynamicFieldOptions();
  } catch {
    errorMessage.value = '';
  }
}

watch(businessKey, () => {
  void initializeForm();
}, { immediate: true });
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
