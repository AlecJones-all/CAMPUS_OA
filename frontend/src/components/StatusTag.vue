<template>
  <el-tag :type="tagType" effect="light" round>{{ text }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  status: string;
  label?: string;
}>();

const statusTypeMap: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
  DRAFT: 'info',
  PENDING: 'warning',
  IN_PROGRESS: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  WITHDRAWN: 'info'
};

const text = computed(() => props.label ?? props.status);
const tagType = computed(() => statusTypeMap[props.status] ?? 'info');
</script>
