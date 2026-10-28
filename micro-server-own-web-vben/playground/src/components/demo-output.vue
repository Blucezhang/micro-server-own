<script setup lang="ts">
import type { HTMLAttributes } from 'vue';

import { computed } from 'vue';

import { cn } from '@vben/utils';

interface Props {
  class?: HTMLAttributes['class'];
  size?: 'inherit' | 'sm' | 'xs';
  text?: string;
}

const props = withDefaults(defineProps<Props>(), {
  class: undefined,
  size: 'sm',
  text: '',
});

const sizeClassMap = {
  inherit: '',
  sm: 'text-sm',
  xs: 'text-xs',
} as const;

const mergedClass = computed(() => {
  return cn(
    'bg-muted overflow-auto rounded-md p-4',
    sizeClassMap[props.size],
    props.class,
  );
});
</script>

<template>
  <pre :class="mergedClass" v-text="text"></pre>
</template>
