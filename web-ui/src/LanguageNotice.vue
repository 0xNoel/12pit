<!--
This file is part of 12pit.

Copyright (C) 2026 The 12pit Authors and contributors <https://github.com/12src/12pit>

12pit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

12pit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with 12pit. If not, see <https://www.gnu.org/licenses/>.
-->
<script setup lang="ts">
import { TriangleAlert } from '@lucide/vue'
import { computed, onUnmounted, ref, watch } from 'vue'
import type { WebUiState } from './useWebUiState'
import { loadLanguage, systemLanguage, translate } from './languages'
import UiNotice from './UiNotice.vue'

const props = defineProps<{
  state: WebUiState
  busy: boolean
  disable: () => Promise<boolean>
}>()
const emit = defineEmits<{
  switch: [value: number]
  error: [cause: unknown]
}>()
const option = computed(() =>
  props.state.webui.settings.sections
    .flatMap((section) => section.options)
    .find((option) => option.id === 'language')!,
)
const notice = ref<{
  locale: string
  value: number
  message: string
  gotIt: string
  hide: string
  switch: string
} | null>(null)
let checked = false
let stopped = false
let timer: ReturnType<typeof setTimeout> | undefined

function pause() {
  clearTimeout(timer)
}

function dismiss() {
  pause()
  notice.value = null
}

function resume(event?: Event) {
  if (event) {
    const element = event.currentTarget as HTMLElement
    const focused =
      event instanceof FocusEvent ? event.relatedTarget : document.activeElement
    if (
      element.matches(':hover') ||
      (focused instanceof Node && element.contains(focused))
    )
      return
  }
  pause()
  timer = setTimeout(dismiss, 5000)
}

async function hide() {
  pause()
  if (await props.disable()) dismiss()
  else resume()
}

function switchLanguage() {
  const value = notice.value!.value
  dismiss()
  emit('switch', value)
}

watch(
  () => [
    props.state.webui.ready,
    props.state.webui.languageHint,
    option.value.value,
  ],
  async () => {
    if (
      !props.state.webui.languageHint ||
      notice.value?.value === option.value.value
    )
      dismiss()
    if (!props.state.webui.ready || checked) return
    checked = true
    if (!props.state.webui.languageHint) return
    const available = systemLanguage(
      option.value,
      props.state.webui.systemLanguage,
    )
    if (!available || available.value === option.value.value) return
    try {
      const catalog = await loadLanguage(available.locale!)
      if (
        stopped ||
        !props.state.webui.languageHint ||
        option.value.value === available.value
      )
        return
      const t = translate.bind(null, catalog)
      notice.value = {
        locale: props.state.webui.systemLanguage,
        value: available.value,
        message: t(
          '{0} is available. Open Settings to change your language.',
          available.name,
        ),
        gotIt: t('Got it'),
        hide: t("Don't show again"),
        switch: t('Switch language'),
      }
      resume()
    } catch (cause) {
      if (!stopped) emit('error', cause)
    }
  },
  { immediate: true },
)

onUnmounted(() => {
  stopped = true
  pause()
})
</script>

<template>
  <Transition name="toast" appear>
    <UiNotice
      v-if="notice"
      class="language-notice"
      :lang="notice.locale"
      @pointerenter="pause"
      @pointerleave="resume"
      @focusin="pause"
      @focusout="resume"
    >
      <div class="language-notice-message">
        <TriangleAlert :size="19" aria-hidden="true" />
        <p>{{ notice.message }}</p>
      </div>
      <div class="language-notice-actions">
        <button type="button" class="secondary" @click="dismiss">
          {{ notice.gotIt }}
        </button>
        <button type="button" class="secondary" :disabled="busy" @click="hide">
          {{ notice.hide }}
        </button>
        <button
          type="button"
          class="primary"
          :disabled="busy"
          @click="switchLanguage"
        >
          {{ notice.switch }}
        </button>
      </div>
    </UiNotice>
  </Transition>
</template>

<style scoped>
.language-notice {
  padding: 16px;
  border-color: #795d22;
  background: #352b15;
  color: #f7d986;
}

.language-notice-message {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.language-notice-message svg {
  flex: none;
}

.language-notice p {
  margin: 0;
}

.language-notice-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 14px;
}

.language-notice .secondary {
  border-color: #795d22;
  background: transparent;
  color: inherit;
}

.language-notice .secondary:hover {
  background: #ffffff0a;
}

.language-notice .primary {
  border-color: #f7d986;
  background: #f7d986;
  color: #352b15;
}
</style>
