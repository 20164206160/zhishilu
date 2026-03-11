/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'canvas-nest.js' {
  interface CanvasNestOptions {
    color?: string
    pointColor?: string
    opacity?: number
    count?: number
    zIndex?: number
  }
  export default class CanvasNest {
    constructor(el: HTMLElement, options?: CanvasNestOptions)
    destroy(): void
  }
}

declare module '@wangeditor/editor-for-vue' {
  export { Editor, Toolbar } from '@wangeditor/editor-for-vue/dist/src/index'
}
