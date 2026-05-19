<template>
  <div class="app-page">
    <PageHero eyebrow="系统管理" title="附件中心" description="按业务记录查询和管理附件。">
      <template #actions>
        <el-button :loading="loading" @click="loadAttachments">刷新列表</el-button>
      </template>
      <template #meta>
        <SummaryStats :items="stats" />
      </template>
    </PageHero>

    <SectionCard title="筛选与上传" description="选择业务类型和记录后上传附件。">
      <div class="compact-form-grid">
        <el-input v-model.trim="filters.businessType" placeholder="业务类型，如 WORKFLOW_APPLICATION" />
        <el-input v-model.trim="filters.businessId" placeholder="业务 ID，可为空表示查询全部" />
        <input ref="fileInputRef" type="file" @change="handleFileChange" />
      </div>
      <div class="app-actions">
        <el-button :loading="loading" @click="loadAttachments">查询附件</el-button>
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="!selectedFile || !filters.businessType.trim() || parsedBusinessId === null"
          @click="handleUpload"
        >
          {{ uploading ? '上传中...' : '上传附件' }}
        </el-button>
      </div>
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </SectionCard>

    <SectionCard title="附件列表" description="查看当前条件下的附件记录。">
      <div v-if="attachments.length > 0" class="list-shell">
        <article v-for="item in attachments" :key="item.id" class="record-card">
          <div class="record-card__main">
            <div class="record-card__title">
              <h3>{{ item.fileName }}</h3>
              <el-tag type="info" effect="light" round>{{ formatSize(item.fileSize) }}</el-tag>
            </div>
            <div class="record-meta">
              <span>业务类型：<strong>{{ item.businessType }}</strong></span>
              <span>业务 ID：<strong>{{ item.businessId }}</strong></span>
              <span>上传人：<strong>{{ item.uploadedByName }}</strong></span>
            </div>
            <div class="record-meta">
              <span>上传时间：<strong>{{ item.createdAt }}</strong></span>
              <span>内容类型：<strong>{{ item.contentType || '未知' }}</strong></span>
            </div>
          </div>
          <div class="record-card__actions">
            <el-button @click="handleDownload(item.id, item.fileName)">下载</el-button>
            <ActionMenu
              :items="[
                { key: `delete:${item.id}`, label: '删除附件', danger: true }
              ]"
              @select="handleAttachmentAction"
            />
          </div>
        </article>
      </div>
      <EmptyStateBlock
        v-else
        title="暂无符合条件的附件"
        description="调整业务类型或业务 ID 后重新查询。"
        badge="附件"
      />
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import {
  deleteAttachment,
  downloadAttachment,
  listAttachments,
  uploadAttachment,
  type AttachmentRecord
} from '../../api/http';
import ActionMenu from '../../components/ActionMenu.vue';
import EmptyStateBlock from '../../components/EmptyStateBlock.vue';
import PageHero from '../../components/PageHero.vue';
import SectionCard from '../../components/SectionCard.vue';
import SummaryStats from '../../components/SummaryStats.vue';

const attachments = ref<AttachmentRecord[]>([]);
const selectedFile = ref<File | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const loading = ref(false);
const uploading = ref(false);
const errorMessage = ref('');

const filters = reactive({
  businessType: 'WORKFLOW_APPLICATION',
  businessId: ''
});

const parsedBusinessId = computed(() => {
  if (!filters.businessId.trim()) {
    return null;
  }
  const value = Number(filters.businessId);
  return Number.isFinite(value) ? value : null;
});

const stats = computed(() => {
  const totalSize = attachments.value.reduce((sum, item) => sum + item.fileSize, 0);
  return [
    { label: '附件数量', value: attachments.value.length, hint: '当前筛选结果条数' },
    { label: '总大小', value: formatSize(totalSize), hint: '当前列表总容量' },
    { label: '目标记录', value: parsedBusinessId.value === null ? '未指定' : parsedBusinessId.value, hint: '上传需要绑定记录' }
  ];
});

function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  selectedFile.value = input.files?.[0] ?? null;
}

async function loadAttachments() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await listAttachments(filters.businessType.trim() || undefined, parsedBusinessId.value ?? undefined);
    attachments.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '附件列表加载失败';
  } finally {
    loading.value = false;
  }
}

async function handleUpload() {
  if (!selectedFile.value || !filters.businessType.trim() || parsedBusinessId.value === null) {
    return;
  }
  uploading.value = true;
  errorMessage.value = '';
  try {
    await uploadAttachment(filters.businessType.trim(), parsedBusinessId.value, selectedFile.value);
    selectedFile.value = null;
    if (fileInputRef.value) {
      fileInputRef.value.value = '';
    }
    await loadAttachments();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '附件上传失败';
  } finally {
    uploading.value = false;
  }
}

async function handleDownload(id: number, fileName: string) {
  errorMessage.value = '';
  try {
    await downloadAttachment(id, fileName);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '附件下载失败';
  }
}

async function handleDelete(id: number) {
  errorMessage.value = '';
  try {
    await deleteAttachment(id);
    await loadAttachments();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '附件删除失败';
  }
}

function handleAttachmentAction(command: string) {
  const [action, rawId] = command.split(':');
  if (action === 'delete') {
    void handleDelete(Number(rawId));
  }
}

function formatSize(size: number) {
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

onMounted(() => {
  void loadAttachments();
});
</script>
