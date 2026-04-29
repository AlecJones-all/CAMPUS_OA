<template>
  <section class="page-hero">
    <div class="page-hero__main">
      <div class="page-hero__copy">
        <span v-if="eyebrow" class="page-hero__eyebrow">{{ eyebrow }}</span>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="page-hero__actions">
        <slot name="actions" />
      </div>
    </div>

    <div v-if="$slots.meta" class="page-hero__meta">
      <slot name="meta" />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
  eyebrow?: string;
}>();
</script>
