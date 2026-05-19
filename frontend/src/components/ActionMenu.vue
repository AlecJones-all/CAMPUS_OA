<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <el-button>
      更多
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="item in items"
          :key="item.key"
          :command="item.key"
          :disabled="item.disabled"
          :class="{ 'is-danger': item.danger }"
        >
          {{ item.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue';

defineProps<{
  items: Array<{ key: string; label: string; danger?: boolean; disabled?: boolean }>;
}>();

const emit = defineEmits<{
  (e: 'select', key: string): void;
}>();

function handleCommand(key: string) {
  emit('select', key);
}
</script>

<style scoped>
:deep(.is-danger) {
  color: var(--app-danger);
}
</style>
