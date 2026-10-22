import {
  appCopyrightPreferences,
  defineOverridesPreferences,
  definePreferencesExtension,
} from '@vben/preferences';

import logo from '#/assets/logo.svg';

interface WebAntdPreferencesExtension {
  defaultTableSize: number;
  enableFormFullscreen: boolean;
  reportTitle: string;
  tenantMode: 'multi' | 'single';
}

export const mallLayoutPreferences = defineOverridesPreferences({
  app: {
    preferencesButtonPosition: 'header',
  },
  header: {
    height: 60,
  },
  logo: {
    enable: true,
    showText: true,
    source: logo,
    sourceDark: logo,
  },
  sidebar: {
    width: 224,
  },
  tabbar: {
    height: 40,
    showMaximize: false,
    showRefresh: false,
    styleType: 'card',
  },
  theme: {
    semiDarkSidebar: true,
    semiDarkSidebarSub: true,
  },
  widget: {
    fullscreen: true,
    fullscreenButtonPosition: 'user-dropdown',
    globalSearch: true,
    languageToggle: false,
    lockScreen: true,
    lockScreenButtonPosition: 'user-dropdown',
    logoutButtonPosition: 'user-dropdown',
    notification: false,
    refresh: true,
    sidebarToggle: true,
    themeToggle: true,
    timezone: false,
  },
});

export const overridesPreferences = defineOverridesPreferences({
  ...mallLayoutPreferences,
  app: {
    authPageLayout: 'panel-right',
    contentCompact: 'wide',
    defaultHomePath: '/buyer/workspace',
    enableRefreshToken: true,
    locale: 'zh-CN',
    name: import.meta.env.VITE_APP_TITLE || '微服务商城运营台',
    preferencesButtonPosition: 'header',
  },
  copyright: {
    ...appCopyrightPreferences,
    companyName: '微服务商城',
    companySiteLink: '',
    date: '2026',
    enable: true,
    icp: '',
    icpLink: '',
    settingShow: false,
  },
  theme: {
    builtinType: 'default',
    colorPrimary: '#0E665D',
    fontSize: 14,
    mode: 'light',
    radius: '0.625',
    semiDarkHeader: false,
    semiDarkSidebar: true,
    semiDarkSidebarSub: true,
  },
});

export const preferencesExtension =
  definePreferencesExtension<WebAntdPreferencesExtension>({
    tabLabel: 'preferences.antd.tabLabel',
    title: 'preferences.antd.title',
    fields: [
      {
        component: 'switch',
        defaultValue: true,
        key: 'enableFormFullscreen',
        label: 'preferences.antd.fields.enableFormFullscreen.label',
        tip: 'preferences.antd.fields.enableFormFullscreen.tip',
      },
      {
        component: 'select',
        defaultValue: 'single',
        key: 'tenantMode',
        label: 'preferences.antd.fields.tenantMode.label',
        options: [
          {
            label: 'preferences.antd.fields.tenantMode.options.single.label',
            value: 'single',
          },
          {
            label: 'preferences.antd.fields.tenantMode.options.multi.label',
            value: 'multi',
          },
        ],
      },
      {
        component: 'number',
        componentProps: {
          max: 200,
          min: 10,
          step: 10,
        },
        defaultValue: 20,
        key: 'defaultTableSize',
        label: 'preferences.antd.fields.defaultTableSize.label',
      },
      {
        component: 'input',
        defaultValue: '',
        key: 'reportTitle',
        label: 'preferences.antd.fields.reportTitle.label',
        placeholder: 'preferences.antd.fields.reportTitle.placeholder',
      },
    ],
  });
