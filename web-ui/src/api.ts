/*
 * This file is part of 12pit.
 *
 * Copyright (C) 2026 The 12pit Authors and contributors <https://github.com/12src/12pit>
 *
 * 12pit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 12pit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 12pit. If not, see <https://www.gnu.org/licenses/>.
 */
import type { SourceText } from './languages'

export interface Option {
  id: string
  name: string
  description: string
  kind:
    | 'BOOLEAN'
    | 'NUMBER'
    | 'CHOICE'
    | 'COLOR'
    | 'COLOR_PICKER'
    | 'KEYBIND'
    | 'TEXT'
  value: boolean | number | string
  min?: number
  max?: number
  step?: number
  keyName?: string
  choices?: {
    value: number
    name: string
    locale?: string
    displayName?: SourceText
  }[]
}

export interface Feature {
  id: string
  name: string
  description: string
  categoryId: string
  category: string
  toggleable: boolean
  enabled: boolean
  sections: {
    id?: string
    name?: string
    subcategory?: { id: string; name: string }
    options: Option[]
  }[]
}

export type RelationType = 'FRIEND' | 'ENEMY'

export interface RelationEntry {
  uuid: string | null
  name: string
  relation: RelationType
}

export interface RelationInput {
  uuid?: string | null
  name: string
}

export interface RelationResult {
  name: string
  message: string
}

export interface ConfigurationCatalog {
  language: string
  version: string
  features: Feature[]
  settings: Feature
}

export interface WebUiPreferences {
  languageHint: boolean
  favorites: string[]
  ready: boolean
  problem: SourceText | null
}

export interface LanguageHint {
  message: string
  gotIt: string
  hide: string
  switch: string
}

export interface WebUiLink {
  id: string
  name: string
  url: string
}

export interface ProfilesState {
  loadState: 'LOADING' | 'READY' | 'DEGRADED'
  activeId: string | null
  entries: { id: string; name: string }[]
  problems: string[]
  unpersisted: boolean
}

export interface RelationsState {
  problem: string | null
  entries: RelationEntry[]
}

export interface TransferState {
  swapBindings: { problem: string | null }
}

export interface SettingValue {
  featureId: string
  settingId: string
  value: boolean | number | string
  keyName?: string
}

export interface RelationConflict {
  id: string
  type: RelationType
  kind: 'group' | 'identity' | 'name'
  local: RelationEntry[]
  incoming: RelationEntry
}

export interface TransferPreview {
  profiles: { id: string; name: string }[]
  relations: Partial<Record<RelationType, number>>
  conflicts: RelationConflict[]
  fingerprint: string
  swapBindings: boolean
}

async function request<T>(
  path: string,
  method = 'GET',
  body?: object,
): Promise<T> {
  const response = await fetch(path, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
    cache: 'no-store',
  })
  const payload = await response.json()
  if (!response.ok) throw new Error(payload.error || 'Request failed')
  return payload as T
}

export const loadCatalog = () => request<ConfigurationCatalog>('/api/settings')
export const loadLinks = () => request<WebUiLink[]>('/api/links')
export const loadLanguageHint = (locale: string) =>
  request<LanguageHint>(`/api/languages/${encodeURIComponent(locale)}/hint`)
export const loadSettingValues = (featureId?: string) =>
  request<SettingValue[]>(
    `/api/settings/${encodeURIComponent(featureId ?? 'values')}`,
  )
export const resetSettings = () =>
  request<SettingValue[]>('/api/settings', 'DELETE')
export const loadPreferences = () =>
  request<WebUiPreferences>('/api/preferences')
export const loadProfiles = () => request<ProfilesState>('/api/profiles')
export const loadRelations = () => request<RelationsState>('/api/relations')
export const loadTransfer = () => request<TransferState>('/api/transfer')

export const changeFavorite = (featureId: string, favorite: boolean) =>
  request<WebUiPreferences>('/api/preferences', 'PATCH', {
    favorites: { [featureId]: favorite },
  })
export const changeSetting = (
  featureId: string,
  settingId: string,
  value: boolean | number | string,
) =>
  request<SettingValue[]>(
    `/api/settings/${encodeURIComponent(featureId)}/${encodeURIComponent(settingId)}`,
    'PUT',
    { value },
  )
export const dismissLanguageHint = () =>
  request<WebUiPreferences>('/api/preferences', 'PATCH', {
    languageHint: false,
  })
export const createProfile = (name: string) =>
  request<ProfilesState>('/api/profiles', 'POST', { name })
export const renameProfile = (id: string, name: string) =>
  request<ProfilesState>(`/api/profiles/${encodeURIComponent(id)}`, 'PATCH', {
    name,
  })
export const deleteProfile = (id: string) =>
  request<ProfilesState>(`/api/profiles/${encodeURIComponent(id)}`, 'DELETE')
export const selectProfile = (id: string) =>
  request<ProfilesState>('/api/profiles/active', 'PUT', { id })
export const changeRelations = (
  action: 'add' | 'remove',
  relation: RelationType,
  entries: RelationInput[],
) =>
  request<{ relations: RelationsState; results: RelationResult[] }>(
    '/api/relations',
    'PATCH',
    { relation, [action]: entries },
  )

export const exportData = (
  profiles: string[],
  relations: RelationType[],
  swapBindings: boolean,
) => {
  const query = new URLSearchParams({
    profiles: profiles.join(','),
    relations: relations.join(','),
    swapBindings: String(swapBindings),
  })
  return request<Record<string, unknown>>(`/api/transfer?${query}`)
}
export const previewData = (data: object) =>
  request<TransferPreview>('/api/transfer', 'POST', { data })
export const applyData = (
  data: object,
  profiles: string[],
  relations: RelationType[],
  mode: 'merge' | 'replace',
  resolutions: Record<string, 'local' | 'imported'>,
  fingerprint: string,
  swapBindings: boolean,
) =>
  request<void>('/api/transfer', 'PUT', {
    data,
    profiles,
    relations,
    mode,
    resolutions,
    fingerprint,
    swapBindings,
  })

export function subscribe(
  receive: (resources: string[]) => void,
  opened: () => void,
  failed: (cause: unknown) => void,
) {
  const stream = new EventSource('/api/events')
  stream.addEventListener('change', (event) => {
    try {
      receive(JSON.parse((event as MessageEvent<string>).data) as string[])
    } catch (cause) {
      failed(cause)
    }
  })
  stream.onopen = opened
  stream.onerror = () => failed(new Error('Request failed'))
  return stream
}
