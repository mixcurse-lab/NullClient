const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
  // Окно
  minimize: () => ipcRenderer.send('window:minimize'),
  close:    () => ipcRenderer.send('window:close'),
  openURL:  (url) => ipcRenderer.send('open:url', url),

  // Ассеты
  getAssetPath: (name) => ipcRenderer.sendSync('get:assetpath', name),

  // Настройки
  settingsGet:        ()       => ipcRenderer.invoke('settings:get'),
  settingsSave:       (data)   => ipcRenderer.invoke('settings:save', data),
  settingsBrowse:     (type)   => ipcRenderer.invoke('settings:browse', type),
  settingsOpenFolder: ()       => ipcRenderer.send('settings:openFolder'),

  // Запуск
  launch: () => ipcRenderer.invoke('launch:game'),

  // Авто-обновление
  checkUpdate:    ()   => ipcRenderer.invoke('update:check'),
  onUpdateStatus: (cb) => ipcRenderer.on('update:status', (_, data) => cb(data)),

  // Прогресс скачивания (старый канал — совместимость)
  onDownloadProgress: (cb) => {
    ipcRenderer.on('download:progress', (_, data) => cb(data))
  },

  // Прогресс запуска (новый канал)
  onLaunchProgress: (cb) => {
    ipcRenderer.on('launch:progress', (_, data) => cb(data))
  },
})
