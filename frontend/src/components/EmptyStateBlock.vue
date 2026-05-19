<template>
  <div class="empty-state-block">
    <el-empty :description="description">
      <template #image>
        <div class="empty-state-block__badge">{{ badge }}</div>
      </template>
      <template #default>
        <div class="empty-state-block__copy">
          <strong>{{ title }}</strong>
          <p>{{ description }}</p>
        </div>
        <div v-if="$slots.actions" class="empty-state-block__actions">
          <slot name="actions" />
        </div>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string;
    description: string;
    badge?: string;
  }>(),
  {
    badge: 'OA'
  }
);
</script>

<style scoped>
.empty-state-block {
  border: 1px dashed var(--app-border);
  border-radius: 24px;
  background: var(--app-surface-subtle);
}

.empty-state-block__badge {
  width: 84px;
  height: 84px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(15, 76, 129, 0.12), rgba(191, 139, 0, 0.16));
  color: var(--app-primary);
  font-size: 24px;
  font-weight: 700;
}

.empty-state-block__copy {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.empty-state-block__copy p {
  margin: 0;
  color: var(--app-text-secondary);
}

.empty-state-block__actions {
  margin-top: 16px;
}
</style>
