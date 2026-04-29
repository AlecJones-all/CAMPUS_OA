<template>
  <SectionCard :title="title" description="统一管理当前申请的附件。">
    <template #actions>
      <div v-if="hasTarget" class="app-actions">
        <input ref="fileInputRef" type="file" @change="handleFileChange" />
        <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="handleUpload">
          {{ uploading ? '上传中...' : '上传附件' }}
        </el-button>
      </div>
    </template>

    <SummaryStats :items="stats" />

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

    <EmptyStateBlock
      v-if="!hasTarget"
      title="请先创建业务记录"
      description="记录创建后即可上传附件。"
      badge="附件"
    />

    <template v-else>
      <div v-if="attachments.length > 0" class="list-shell">
        <article v-for="item in attachments" :key="item.id" class="record-card">
          <div class="record-card__main">
            <div class="record-card__title">
              <h3>{{ item.fileName }}</h3>
              <el-tag type="info" effect="light" round>{{ formatSize(item.fileSize) }}</el-tag>
            </div>
            <div class="record-meta">
              <span>上传人：<strong>{{ item.uploadedByName }}</strong></span>
              <span>上传时间：<strong>{{ item.createdAt }}</strong></span>
              <span>业务类型：<strong>{{ item.businessType }}</strong></span>
            </div>
          </div>
          <div class="record-card__actions">
            <el-button @click="handleDownload(item.id, item.fileName)">下载</el-button>
            <ActionMenu
              :items="[
                { key: `delete:${item.id}`, label: '删除附件', danger: true }
              ]"
              @select="handleMenuAction"
            />
          </div>
        </article>
      </div>

      <EmptyStateBlock
        v-else-if="!loading"
        title="暂无附件"
        description="可上传申请材料、证明文件和补充材料。"
        badge="文件"
      >
        <template #actions>
          <el-button type="primary" :disabled="!selectedFile" :loading="uploading" @click="handleUpload">
            {{ uploading ? '上传中...' : '上传第一份附件' }}
          </el-button>
        </template>
      </EmptyStateBlock>

      <p v-else class="muted-text">正在加载附件列表...</p>
    </template>
  </SectionCard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import {
  deleteAttachment,
  downloadAttachment,
  listAttachments,
  uploadAttachment,
  type AttachmentRecord
} from '../api/http';
import ActionMenu from './ActionMenu.vue';
import EmptyStateBlock from './EmptyStateBlock.vue';
import SectionCard from './SectionCard.vue';
import SummaryStats from './SummaryStats.vue';

const props = withDefaults(
  defineProps<{
    businessType: string;
    businessId?: number | null;
    title?: string;
  }>(),
  {
    title: '附件材料'
  }
);

const attachments = ref<AttachmentRecord[]>([]);
const loading = ref(false);
const uploading = ref(false);
const selectedFile = ref<File | null>(null);
const errorMessage = ref('');
const fileInputRef = ref<HTMLInputElement | null>(null);

const hasTarget = computed(() => typeof props.businessId === 'number' && Number.isFinite(props.businessId));

const stats = computed(() => {
  const totalSize = attachments.value.reduce((sum, item) => sum + item.fileSize, 0);
  return [
    { label: '附件数量', value: attachments.value.length, hint: '当前申请已上传文件数' },
    { label: '总大小', value: formatSize(totalSize), hint: '用于控制材料体量' },
    { label: '绑定对象', value: hasTarget.value ? props.businessId ?? '-' : '未创建', hint: '关联业务或流程记录' }
  ];
});

async function loadAttachments() {
  if (!hasTarget.value) {
    attachments.value = [];
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await listAttachments(props.businessType, props.businessId ?? undefined);
    attachments.value = response.data;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '附件加载失败';
  } finally {
    loading.value = false;
  }
}

function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  selectedFile.value = input.files?.[0] ?? null;
}

async function handleUpload() {
  if (!selectedFile.value || !hasTarget.value) {
    return;
  }
  uploading.value = true;
  errorMessage.value = '';
  try {
    await uploadAttachment(props.businessType, props.businessId as number, selectedFile.value);
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

async function handleDelete(id: number) {
  errorMessage.value = '';
  try {
    await deleteAttachment(id);
    await loadAttachments();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '附件删除失败';
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

function handleMenuAction(command: string) {
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

watch(
  () => [props.businessType, props.businessId],
  () => {
    void loadAttachments();
  }
);

onMounted(() => {
  void loadAttachments();
});
</script>
