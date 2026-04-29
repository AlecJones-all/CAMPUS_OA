<template>
  <div class="summary-stats">
    <article v-for="item in items" :key="item.label" class="summary-stats__item">
      <span>{{ item.label }}</span>
      <strong>{{ item.value }}</strong>
      <small v-if="item.hint">{{ item.hint }}</small>
    </article>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  items: Array<{ label: string; value: string | number; hint?: string }>;
}>();
</script>
