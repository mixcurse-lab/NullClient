'use strict'

const { app, BrowserWindow, ipcMain, shell, dialog } = require('electron')
const { execFile, spawn } = require('child_process')
const path  = require('path')
const fs    = require('fs')
const https = require('https')
const http  = require('http')
const zlib  = require('zlib')

let mainWindow

// ═══════════════════════════════════════════════════════════════════
//  LOGGER
// ═══════════════════════════════════════════════════════════════════
const LOG_DIR  = path.join(app.getPath('userData'), 'NullClient')
const LOG_FILE = path.join(LOG_DIR, 'launcher.log')

function logInit() {
  try {
    if (!fs.existsSync(LOG_DIR)) fs.mkdirSync(LOG_DIR, { recursive: true })
    if (fs.existsSync(LOG_FILE) && fs.statSync(LOG_FILE).size > 300 * 1024) {
      const lines = fs.readFileSync(LOG_FILE, 'utf8').split('\n')
      fs.writeFileSync(LOG_FILE, lines.slice(-500).join('\n'), 'utf8')
    }
  } catch {}
}

function logWrite(level, ...args) {
  const ts  = new Date().toISOString().replace('T', ' ').slice(0, 19)
  const msg = args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' ')
  const line = `[${ts}] [${level}] ${msg}`
  console.log(line)
  try { fs.appendFileSync(LOG_FILE, line + '\n', 'utf8') } catch {}
}

const log = {
  info:  (...a) => logWrite('INFO ', ...a),
  warn:  (...a) => logWrite('WARN ', ...a),
  error: (...a) => logWrite('ERROR', ...a),
  debug: (...a) => logWrite('DEBUG', ...a),
}

logInit()

// ═══════════════════════════════════════════════════════════════════
//  CONFIG
// ═══════════════════════════════════════════════════════════════════
const SETTINGS_FILE = path.join(app.getPath('userData'), 'settings.json')

function cfgDefaults() {
  const appData = app.getPath('appData')
  const tl = path.join(appData, '.tlauncher', 'legacy', 'Minecraft', 'game')
  return {
    gameDir:         fs.existsSync(tl) ? tl : path.join(appData, '.minecraft'),
    javaPath:        '',
    mcVersion:       '',
    minRam:          512,
    maxRam:          2048,
    extraJvmArgs:    '',
    extraClientArgs: '',
    modsPath:        '',
  }
}

function cfgLoad() {
  try {
    if (fs.existsSync(SETTINGS_FILE))
      return Object.assign(cfgDefaults(), JSON.parse(fs.readFileSync(SETTINGS_FILE, 'utf8')))
  } catch {}
  return cfgDefaults()
}

function cfgSave(data) {
  const merged = Object.assign(cfgLoad(), data)
  fs.writeFileSync(SETTINGS_FILE, JSON.stringify(merged, null, 2), 'utf8')
  return merged
}

function cfgModsDir(c) {
  if (c.modsPath && fs.existsSync(c.modsPath)) return c.modsPath
  return path.join(c.gameDir, 'mods')
}

// ═══════════════════════════════════════════════════════════════════
//  DOWNLOADER
// ═══════════════════════════════════════════════════════════════════
// ═══════════════════════════════════════════════════════════════════
//  AUTO-UPDATE  (GitHub Releases)
// ═══════════════════════════════════════════════════════════════════

// ── GitHub репо для авто-обновлений ───────────────────────────────
const GITHUB_REPO = 'mixcurse-lab/NullClient'
// ──────────────────────────────────────────────────────────────────

const UPDATE_STATE_FILE = path.join(app.getPath('userData'), 'NullClient', 'update_state.json')

function updateStateLoad() {
  try {
    if (fs.existsSync(UPDATE_STATE_FILE))
      return JSON.parse(fs.readFileSync(UPDATE_STATE_FILE, 'utf8'))
  } catch {}
  return { installedTag: null, installedFile: null }
}

function updateStateSave(data) {
  try {
    fs.mkdirSync(path.dirname(UPDATE_STATE_FILE), { recursive: true })
    fs.writeFileSync(UPDATE_STATE_FILE, JSON.stringify(data, null, 2), 'utf8')
  } catch {}
}

function sendUpdateStatus(status, detail) {
  log.info(`[update] ${status}: ${detail}`)
  mainWindow?.webContents.send('update:status', { status, detail })
}

/**
 * Запрашивает GitHub API для последнего релиза.
 * Возвращает { tag, jarName, downloadUrl } или null при ошибке.
 */
function fetchLatestRelease() {
  return new Promise((resolve) => {
    const url = `https://api.github.com/repos/${GITHUB_REPO}/releases/latest`
    const opts = {
      headers: {
        'User-Agent': 'NullClient-Launcher/1.0',
        'Accept':     'application/vnd.github+json',
      },
    }
    https.get(url, opts, (res) => {
      let body = ''
      if (res.statusCode === 301 || res.statusCode === 302) {
        res.resume()
        resolve(null)
        return
      }
      if (res.statusCode !== 200) {
        log.warn('[update] GitHub API status:', res.statusCode)
        res.resume()
        resolve(null)
        return
      }
      res.on('data', d => body += d)
      res.on('end', () => {
        try {
          const json = JSON.parse(body)
          const tag  = json.tag_name
          // Ищем JAR ассет
          const asset = (json.assets || []).find(a =>
            a.name.startsWith('nullclient-') && a.name.endsWith('.jar')
          )
          if (!tag || !asset) { resolve(null); return }
          resolve({
            tag,
            jarName:     asset.name,
            downloadUrl: asset.browser_download_url,
            size:        asset.size || 0,
          })
        } catch (e) {
          log.error('[update] parse error:', e.message)
          resolve(null)
        }
      })
      res.on('error', () => resolve(null))
    }).on('error', () => resolve(null))
  })
}

/**
 * Проверяет и при необходимости скачивает новую версию nullclient JAR.
 * Возвращает { updated: bool, jarName: string|null, skipped: bool }
 */
async function checkAndUpdate(modsDir) {
  if (GITHUB_REPO === 'OWNER/NullClient') {
    log.warn('[update] GITHUB_REPO not configured — skipping auto-update')
    sendUpdateStatus('skip', 'Авто-обновление не настроено (OWNER/NullClient)')
    return { updated: false, jarName: null, skipped: true }
  }

  sendUpdateStatus('check', 'Проверка обновлений...')
  const release = await fetchLatestRelease()

  if (!release) {
    sendUpdateStatus('skip', 'Сервер недоступен — используется локальная версия')
    return { updated: false, jarName: null, skipped: true }
  }

  const state = updateStateLoad()
  log.info('[update] latest release:', release.tag, '| local tag:', state.installedTag)

  // Проверяем по тегу
  if (state.installedTag === release.tag) {
    const dest = path.join(modsDir, release.jarName)
    if (fs.existsSync(dest)) {
      sendUpdateStatus('ok', `✓ Актуальная версия: ${release.tag}`)
      return { updated: false, jarName: release.jarName, skipped: false }
    }
  }

  // Нужно скачать
  sendUpdateStatus('download', `Обновление до ${release.tag}...`)
  log.info('[update] downloading:', release.jarName, 'from', release.downloadUrl)

  // Удаляем старый JAR клиента если есть (любой nullclient-*.jar)
  try {
    for (const f of fs.readdirSync(modsDir)) {
      if (f.startsWith('nullclient-') && f.endsWith('.jar') && f !== release.jarName) {
        fs.unlinkSync(path.join(modsDir, f))
        log.info('[update] removed old JAR:', f)
      }
    }
  } catch {}

  const dest = path.join(modsDir, release.jarName)
  const ok = await downloadFile(
    { src: 'direct', url: release.downloadUrl },
    dest,
    (pct) => sendUpdateStatus('download', `Обновление ${release.tag} — ${pct}%`)
  )

  if (!ok) {
    sendUpdateStatus('error', `Не удалось скачать ${release.tag} — используется старая версия`)
    log.error('[update] download failed for', release.jarName)
    return { updated: false, jarName: null, skipped: true }
  }

  updateStateSave({ installedTag: release.tag, installedFile: release.jarName })
  sendUpdateStatus('done', `✓ Обновлено до ${release.tag}`)
  log.info('[update] updated to', release.tag)
  return { updated: true, jarName: release.jarName, skipped: false }
}

// ═══════════════════════════════════════════════════════════════════
//  MODS LIST  (статические моды кроме nullclient JAR)
// ═══════════════════════════════════════════════════════════════════
const STATIC_MODS = [
  { name: 'baritone-fabric-1.15.0-10.jar', src: 'gdrive', id: '16bQJatWySZVTVL-QQsPY5Exwk7uIevrm' },
  { name: 'fabric-api-0.161.0+26.2.jar',   src: 'direct', url: 'https://cdn.modrinth.com/data/P7dR8mSH/versions/ewUK83HI/fabric-api-0.161.0%2B26.2.jar' },
]

// Оставляем MODS для обратной совместимости — заполняем динамически в launchGame
let MODS = [...STATIC_MODS]

function driveUrl(id) {
  return `https://drive.usercontent.google.com/download?id=${id}&export=download&confirm=t`
}

function downloadFile(mod, dest, onProgress) {
  const startUrl = mod.src === 'direct' ? mod.url : driveUrl(mod.id)
  return new Promise((resolve) => {
    function doGet(url, hops) {
      if (hops > 10) { resolve(false); return }
      const m = url.startsWith('https') ? https : http
      const req = m.get(url, { headers: { 'User-Agent': 'Mozilla/5.0' } }, (res) => {
        if ([301,302,303,307,308].includes(res.statusCode) && res.headers.location) {
          res.resume(); doGet(res.headers.location, hops + 1); return
        }
        if (res.statusCode !== 200) {
          log.warn('Download HTTP', res.statusCode, url)
          res.resume(); resolve(false); return
        }
        const total = parseInt(res.headers['content-length'] || '0', 10)
        let got = 0
        const tmp  = dest + '.tmp'
        const file = fs.createWriteStream(tmp)
        res.on('data', chunk => { got += chunk.length; file.write(chunk); if (total > 0) onProgress(Math.round((got/total)*100)) })
        res.on('end', () => file.end(() => {
          try { if (fs.existsSync(dest)) fs.unlinkSync(dest); fs.renameSync(tmp, dest); onProgress(100); resolve(true) }
          catch (e) { log.error('rename:', e.message); resolve(false) }
        }))
        res.on('error', () => { file.destroy(); try { if (fs.existsSync(tmp)) fs.unlinkSync(tmp) } catch {}; resolve(false) })
      })
      req.on('error', () => resolve(false))
      req.setTimeout(60000, () => { req.destroy(); resolve(false) })
    }
    doGet(startUrl, 0)
  })
}

// ═══════════════════════════════════════════════════════════════════
//  JAVA FINDER
// ═══════════════════════════════════════════════════════════════════
function findJavawInDir(dir, depth) {
  if (!dir || depth <= 0 || !fs.existsSync(dir)) return null
  try {
    for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, e.name)
      if (e.isDirectory()) { const f = findJavawInDir(full, depth-1); if (f) return f }
      else if (e.name.toLowerCase() === 'javaw.exe') return full
    }
  } catch {}
  return null
}

function resolveJavaw(p) {
  if (!p) return null
  for (const c of [p, path.join(p,'javaw.exe'), path.join(p,'bin','javaw.exe')])
    if (fs.existsSync(c) && c.toLowerCase().endsWith('.exe')) return c
  return null
}

async function findJava(config) {
  if (config.javaPath) { const j = resolveJavaw(config.javaPath); if (j) return j }
  const appData = app.getPath('appData')
  const candidates = [
    findJavawInDir(path.join(appData, '.tlauncher','legacy','Minecraft','jre'), 4),
    findJavawInDir(path.join(appData, '.tlauncher','mojang_jre'), 5),
    findJavawInDir(path.join(appData, '.minecraft','runtime'), 6),
    findJavawInDir(path.join(appData, 'VoidMC','.minecraft','runtime'), 6),
    process.env.JAVA_HOME ? resolveJavaw(path.join(process.env.JAVA_HOME,'bin','javaw.exe')) : null,
  ]
  for (const c of candidates) if (c) return c
  return new Promise(resolve => {
    execFile('where', ['javaw'], (err, out) => resolve(err ? null : out.trim().split('\n')[0].trim() || null))
  })
}

// ═══════════════════════════════════════════════════════════════════
//  VERSION FINDER
// ═══════════════════════════════════════════════════════════════════
function loadVersionJson(versionsDir, id) {
  const p = path.join(versionsDir, id, id + '.json')
  if (!fs.existsSync(p)) return null
  try { const j = JSON.parse(fs.readFileSync(p,'utf8')); if (!j.mainClass) return null; return { id, jsonPath: p, ...j } }
  catch { return null }
}

async function findVersion(config) {
  const vdir = path.join(config.gameDir, 'versions')
  if (!fs.existsSync(vdir)) return null
  let dirs; try { dirs = fs.readdirSync(vdir) } catch { return null }
  if (config.mcVersion) { const v = loadVersionJson(vdir, config.mcVersion); if (v) return v }
  for (const d of dirs.filter(d => d.toLowerCase().includes('fabric')).sort().reverse()) {
    const v = loadVersionJson(vdir, d); if (v) return v
  }
  for (const d of dirs.sort().reverse()) { const v = loadVersionJson(vdir, d); if (v) return v }
  return null
}

// ═══════════════════════════════════════════════════════════════════
//  CLASSPATH BUILDER
// ═══════════════════════════════════════════════════════════════════
function shouldInclude(lib) {
  if (!lib.rules) return true
  let allow = false
  for (const r of lib.rules) {
    const match = !r.os || r.os.name === 'windows'
    if (r.action === 'allow'    && match) allow = true
    if (r.action === 'disallow' && match) allow = false
  }
  return allow
}

/**
 * Строит путь к jar из Maven-имени формата "group:artifact:version" или
 * "group:artifact:version:classifier"
 * Пример: org.lwjgl:lwjgl:3.4.1:natives-windows
 *   → libraries/org/lwjgl/lwjgl/3.4.1/lwjgl-3.4.1-natives-windows.jar
 */
function mavenToPath(libsDir, name) {
  try {
    const parts = name.split(':')
    if (parts.length < 3) return null
    const [group, artifact, version, classifier] = parts
    const groupPath = group.replace(/\./g, path.sep)
    const fileName  = classifier
      ? `${artifact}-${version}-${classifier}.jar`
      : `${artifact}-${version}.jar`
    return path.join(libsDir, groupPath, artifact, version, fileName)
  } catch { return null }
}

async function buildClasspath(config, ver) {
  const libsDir   = path.join(config.gameDir, 'libraries')
  const entries   = []
  const missing   = []
  const nativeJars = []

  // A: определяем версию lwjgl из libraries чтобы гарантировать base jar
  let lwjglVersion = null

  for (const lib of (ver.libraries || [])) {
    if (!shouldInclude(lib)) continue

    const nameParts = (lib.name || '').split(':')
    const isNative  = nameParts.length === 4

    // A: запоминаем версию lwjgl из любого lwjgl-модуля
    if (!lwjglVersion && nameParts[0] === 'org.lwjgl' && nameParts.length >= 3) {
      lwjglVersion = nameParts[2]
    }

    if (isNative) {
      const classifier = nameParts[3]
      const isWindows  = classifier === 'natives-windows' || classifier === 'natives-windows-64'
      if (!isWindows) continue

      let p = lib.downloads?.artifact?.path
        ? path.join(libsDir, lib.downloads.artifact.path)
        : mavenToPath(libsDir, lib.name)

      if (p && fs.existsSync(p)) {
        nativeJars.push(p)
      } else {
        // A: логируем имя, а не просто счётчик
        log.warn(`[classpath] Missing lib: ${lib.name} — ${p}`)
      }
    } else {
      let p = lib.downloads?.artifact?.path
        ? path.join(libsDir, lib.downloads.artifact.path)
        : mavenToPath(libsDir, lib.name)

      if (!p) continue
      if (fs.existsSync(p)) {
        entries.push(p)
      } else {
        // A: логируем имя, а не просто счётчик
        log.warn(`[classpath] Missing lib: ${lib.name} — ${p}`)
        missing.push({ name: lib.name, p })
      }
    }
  }

  // A: гарантируем наличие базового lwjgl jar (org.lwjgl:lwjgl:<version>)
  if (lwjglVersion) {
    const lwjglBasePath = mavenToPath(libsDir, `org.lwjgl:lwjgl:${lwjglVersion}`)
    if (lwjglBasePath && !entries.includes(lwjglBasePath)) {
      if (fs.existsSync(lwjglBasePath)) {
        log.info('[classpath] Adding missing lwjgl base jar:', path.basename(lwjglBasePath))
        entries.push(lwjglBasePath)
      } else {
        log.warn(`[classpath] lwjgl base jar missing, will download: ${lwjglBasePath}`)
        // Скачиваем с libraries.minecraft.net
        const url = `https://libraries.minecraft.net/org/lwjgl/lwjgl/${lwjglVersion}/lwjgl-${lwjglVersion}.jar`
        fs.mkdirSync(path.dirname(lwjglBasePath), { recursive: true })
        const ok = await downloadFile({ src: 'direct', url }, lwjglBasePath, () => {})
        if (ok) {
          log.info('[classpath] Downloaded lwjgl base jar')
          entries.push(lwjglBasePath)
        } else {
          log.error('[classpath] Failed to download lwjgl base jar')
        }
      }
    }
  }

  // version jar
  const vjar = path.join(config.gameDir, 'versions', ver.id, ver.id + '.jar')
  if (fs.existsSync(vjar)) entries.push(vjar)
  else log.warn(`[classpath] Missing lib: ${ver.id}.jar — ${vjar}`)

  // A: дедупликация
  const unique = [...new Set(entries)]

  log.info(`[classpath] entries: ${unique.length} | native jars: ${nativeJars.length} | missing: ${missing.length}`)
  return { entries: unique, nativeJars, missing }
}

// ═══════════════════════════════════════════════════════════════════
//  NATIVES EXTRACTOR
// ═══════════════════════════════════════════════════════════════════

/**
 * Определяет подпапку для нативки по имени библиотеки.
 * TLauncher 26.2 использует:
 *   -Djava.library.path=${natives_directory}/java
 *   -Dorg.lwjgl.system.SharedLibraryExtractPath=${natives_directory}/lwjgl
 *   -Djna.tmpdir=${natives_directory}/jna
 *   -Dio.netty.native.workdir=${natives_directory}/netty
 */
function getNativeSubdir(libName) {
  const n = libName.toLowerCase()
  if (n.includes('lwjgl'))  return 'lwjgl'
  if (n.includes('netty'))  return 'netty'
  if (n.includes('jna'))    return 'jna'
  return 'java' // дефолт — java подпапка
}

/**
 * Распаковывает .dll файлы из ZIP/JAR в указанную папку.
 * Использует встроенный Node.js zlib (DEFLATE) — без внешних зависимостей.
 */
function extractDllsFromJar(jarPath, destDir) {
  try {
    if (!fs.existsSync(jarPath)) return 0
    if (!fs.existsSync(destDir)) fs.mkdirSync(destDir, { recursive: true })

    const buf = fs.readFileSync(jarPath)
    let extracted = 0

    // Найдём End of Central Directory signature (0x06054b50)
    let eocd = -1
    for (let i = buf.length - 22; i >= Math.max(0, buf.length - 65536); i--) {
      if (buf[i] === 0x50 && buf[i+1] === 0x4b && buf[i+2] === 0x05 && buf[i+3] === 0x06) {
        eocd = i; break
      }
    }
    if (eocd < 0) { log.warn('[zip] No EOCD in', path.basename(jarPath)); return 0 }

    const cdCount  = buf.readUInt16LE(eocd + 10)
    const cdOffset = buf.readUInt32LE(eocd + 16)

    let pos = cdOffset
    for (let i = 0; i < cdCount; i++) {
      // Central directory entry signature: 0x02014b50
      if (buf[pos] !== 0x50 || buf[pos+1] !== 0x4b || buf[pos+2] !== 0x01 || buf[pos+3] !== 0x02) break

      const compression  = buf.readUInt16LE(pos + 10)
      const compSize     = buf.readUInt32LE(pos + 20)
      const uncompSize   = buf.readUInt32LE(pos + 24)
      const nameLen      = buf.readUInt16LE(pos + 28)
      const extraLen     = buf.readUInt16LE(pos + 30)
      const commentLen   = buf.readUInt16LE(pos + 32)
      const localOffset  = buf.readUInt32LE(pos + 42)
      const fileName     = buf.toString('utf8', pos + 46, pos + 46 + nameLen)

      pos += 46 + nameLen + extraLen + commentLen

      // Только .dll/.so файлы (могут быть глубоко в подпапках: windows/x64/org/lwjgl/lwjgl.dll)
      if (!fileName.endsWith('.dll') && !fileName.endsWith('.so') && !fileName.endsWith('.dylib')) continue
      // Пропускаем записи папок
      if (fileName.endsWith('/') || fileName.endsWith('\\')) continue

      // Local file header: пропускаем до данных
      const lhNameLen  = buf.readUInt16LE(localOffset + 26)
      const lhExtraLen = buf.readUInt16LE(localOffset + 28)
      const dataStart  = localOffset + 30 + lhNameLen + lhExtraLen
      const compData   = buf.slice(dataStart, dataStart + compSize)

      const outPath = path.join(destDir, path.basename(fileName)) // C: кладём в плоскую папку
      if (fs.existsSync(outPath)) { extracted++; continue }

      try {
        let data
        if (compression === 0) {
          data = compData
        } else if (compression === 8) {
          data = zlib.inflateRawSync(compData)
        } else {
          log.warn('[zip] Unknown compression', compression, 'for', fileName)
          continue
        }
        // B: создаём родительские директории перед записью
        fs.mkdirSync(path.dirname(outPath), { recursive: true })
        fs.writeFileSync(outPath, data)
        extracted++
        log.debug('[natives] extracted:', path.basename(fileName), '->', path.basename(destDir))
      } catch (e) {
        log.error('[natives] failed to extract', fileName, e.message)
      }
    }

    return extracted
  } catch (e) {
    log.error('[natives] extractDlls error:', path.basename(jarPath), e.message)
    return 0
  }
}

async function extractNatives(config, ver, nativeJars) {
  // C: одна плоская папка natives/ — java.library.path указывает только на неё
  const natBase = path.join(config.gameDir, 'versions', ver.id, 'natives')

  // Проверяем нужно ли распаковывать — считаем dll прямо в натBase
  let existingDlls = 0
  if (fs.existsSync(natBase))
    existingDlls = fs.readdirSync(natBase).filter(f => f.endsWith('.dll')).length

  if (existingDlls > 5) {
    log.info('[natives] Already extracted, dlls found:', existingDlls)
    return natBase
  }

  if (!fs.existsSync(natBase)) fs.mkdirSync(natBase, { recursive: true })
  log.info('[natives] Extracting', nativeJars.length, 'native jars to:', natBase)

  let total = 0
  for (const jarPath of nativeJars) {
    const jarName = path.basename(jarPath)

    const count = extractDllsFromJar(jarPath, natBase) // C: всё в одну плоскую папку
    total += count
    if (count === 0) {
      log.warn('[natives] No files extracted from:', jarName)
    } else {
      log.info('[natives]', jarName, count, 'files')
    }
  }

  log.info('[natives] Total extracted:', total, 'files')
  return natBase
}

// ═══════════════════════════════════════════════════════════════════
//  ARG BUILDERS
// ═══════════════════════════════════════════════════════════════════

function resolveVar(s, ver, config) {
  // C: одна плоская папка natives/ — все ${natives_directory} указывают на неё
  const natBase = path.join(config.gameDir, 'versions', ver.id, 'natives')
  return s
    .replace(/\$\{natives_directory\}/g,     natBase)
    .replace(/\$\{version_name\}/g,          ver.id)
    .replace(/\$\{game_directory\}/g,        config.gameDir)
    .replace(/\$\{assets_root\}/g,           path.join(config.gameDir, 'assets'))
    .replace(/\$\{assets_index_name\}/g,     ver.assetIndex?.id || ver.assets || '')
    .replace(/\$\{auth_player_name\}/g,      'Player')
    .replace(/\$\{auth_uuid\}/g,             '00000000-0000-0000-0000-000000000000')
    .replace(/\$\{auth_access_token\}/g,     '0')
    .replace(/\$\{user_type\}/g,             'legacy')
    .replace(/\$\{version_type\}/g,          ver.type || 'release')
    .replace(/\$\{launcher_name\}/g,         'NullClient')
    .replace(/\$\{launcher_version\}/g,      '1.0.3')
    .replace(/\$\{clientid\}/g,              '')
    .replace(/\$\{auth_xuid\}/g,             '')
    .replace(/\$\{classpath\}/g,             '')
    .replace(/\$\{resolution_width\}/g,      '854')
    .replace(/\$\{resolution_height\}/g,     '480')
}

/**
 * Строит JVM аргументы.
 * Стратегия: берём ВСЕ jvm args из version JSON как основу,
 * добавляем только то чего там нет (RAM, GC).
 * НЕ добавляем дубли и не трогаем то что уже прописано TLauncher.
 */
function buildJvmArgs(config, ver, cpEntries) {
  const sep = ';'
  const cp  = cpEntries.join(sep)
  const args = []

  // RAM
  args.push(`-Xms${config.minRam}m`)
  args.push(`-Xmx${config.maxRam}m`)

  // GC — без ParallelRefProcEnabled (deprecated в Java 26)
  args.push('-XX:+UseG1GC', '-XX:MaxGCPauseMillis=200',
            '-XX:+UnlockExperimentalVMOptions', '-XX:G1HeapRegionSize=32M')

  // Безопасность
  args.push('-Dlog4j2.formatMsgNoLookups=true')
  args.push('-Dfile.encoding=UTF-8')

  // Бренд лаунчера
  args.push('-Dminecraft.launcher.brand=NullClient')
  args.push('-Dminecraft.launcher.version=1.0.3')

  // JVM args из version JSON
  if (Array.isArray(ver.arguments?.jvm)) {
    for (const a of ver.arguments.jvm) {
      const values = typeof a === 'string'
        ? [a]
        : (a?.value && shouldInclude({ rules: a.rules })) ? [].concat(a.value) : []

      for (const v of values) {
        const r = resolveVar(String(v), ver, config).trim()
        if (!r) continue

        if (r === '-XstartOnFirstThread') continue
        if (r.startsWith('-Xmx') || r.startsWith('-Xms')) continue
        if (r === '-cp' || r === '-classpath') continue
        if (r === '${classpath}' || r === '') continue
        if (r.startsWith('-Dminecraft.launcher.brand') ||
            r.startsWith('-Dminecraft.launcher.version')) continue
        if (r.startsWith('-DFabricMcEmu')) {
          args.push('-DFabricMcEmu=net.minecraft.client.main.Main')
          continue
        }
        // Пропускаем голые имена классов
        if (!r.startsWith('-') && (r.includes('.') || r.includes('/'))) continue

        // Пропускаем native-path аргументы — перезапишем ниже точными значениями
        if (r.startsWith('-Djava.library.path')) continue
        if (r.startsWith('-Dorg.lwjgl.system.SharedLibraryExtractPath')) continue
        if (r.startsWith('-Djna.tmpdir')) continue
        if (r.startsWith('-Dio.netty.native.workdir')) continue

        args.push(r)
      }
    }
  }

  // Явно прописываем native-path аргументы — все указывают на natBase
  // (DLL распакованы туда, mkdirSync выполнен ДО этого момента в launchGame)
  const natBase = path.join(config.gameDir, 'versions', ver.id, 'natives')
  args.push(`-Djava.library.path=${natBase}`)
  args.push(`-Dorg.lwjgl.system.SharedLibraryExtractPath=${natBase}`)
  args.push(`-Djna.tmpdir=${natBase}`)
  args.push(`-Dio.netty.native.workdir=${natBase}`)

  // Classpath в конце
  args.push('-cp', cp)

  // Дополнительные JVM args из настроек
  if (config.extraJvmArgs?.trim())
    args.push(...config.extraJvmArgs.trim().split(/\s+/))

  return args
}

function buildClientArgs(config, ver) {
  const args = []

  if (Array.isArray(ver.arguments?.game)) {
    for (const a of ver.arguments.game)
      if (typeof a === 'string') {
        const r = resolveVar(a, ver, config)
        if (r) args.push(r)
      }
  } else if (ver.minecraftArguments) {
    args.push(...ver.minecraftArguments.split(' ').map(a => resolveVar(a, ver, config)).filter(Boolean))
  }

  if (!args.includes('--gameDir'))  args.push('--gameDir',  config.gameDir)
  if (!args.includes('--username')) {
    args.push('--username', 'Player', '--accessToken', '0',
              '--version', ver.id, '--userType', 'legacy')
  }
  if (!args.includes('--assetIndex') && ver.assetIndex?.id)
    args.push('--assetIndex', ver.assetIndex.id)

  if (config.extraClientArgs?.trim())
    args.push(...config.extraClientArgs.trim().split(/\s+/))

  return args
}

// ═══════════════════════════════════════════════════════════════════
//  LAUNCH SERVICE
// ═══════════════════════════════════════════════════════════════════
let mcProcess = null

function sendProgress(stage, percent, detail) {
  log.info(`[${percent}%] ${stage}: ${detail}`)
  mainWindow?.webContents.send('launch:progress', { stage, percent, detail })
}

async function launchGame() {
  const config = cfgLoad()
  log.info('=== Launch started ===')
  log.info('gameDir:', config.gameDir)

  try {
    // 1. Проверка дубля
    sendProgress('check', 2, 'Проверка запущенных процессов...')
    if (mcProcess && !mcProcess.killed) {
      try { process.kill(mcProcess.pid, 0); return { success: false, error: 'Minecraft уже запущен.' } }
      catch {}
    }

    // 2. Java
    sendProgress('java', 8, 'Поиск Java...')
    const javaw = await findJava(config)
    if (!javaw) {
      return { success: false, error: 'Java не найдена.\nЗапусти TLauncher хотя бы раз или укажи путь к Java в настройках.' }
    }
    log.info('Java:', javaw)
    sendProgress('java', 14, 'Java: ' + path.basename(path.dirname(path.dirname(javaw))))

    // 3. Версия Minecraft
    sendProgress('version', 20, 'Поиск версии Minecraft...')
    const ver = await findVersion(config)
    if (!ver) {
      return { success: false, error: 'Версия Minecraft с Fabric не найдена.\nЗапусти TLauncher, установи Fabric 26.2 и запусти игру хотя бы раз.' }
    }
    log.info('Version:', ver.id, '| mainClass:', ver.mainClass)
    sendProgress('version', 28, 'Версия: ' + ver.id)

    // 4. Classpath
    sendProgress('libs', 34, 'Проверка библиотек...')
    const cp = await buildClasspath(config, ver)
    sendProgress('libs', 44, `Библиотек: ${cp.entries.length} | Natives: ${cp.nativeJars.length}`)

    // 5. Распаковка нативных библиотек
    // Создаём папку ДО формирования аргументов — java.library.path должен существовать
    const natDir = path.join(config.gameDir, 'versions', ver.id, 'natives')
    if (!fs.existsSync(natDir)) fs.mkdirSync(natDir, { recursive: true })

    sendProgress('libs', 47, 'Распаковка нативных библиотек...')
    await extractNatives(config, ver, cp.nativeJars)
    sendProgress('libs', 52, 'Нативные библиотеки готовы')

    // 6. Моды
    sendProgress('mods', 54, 'Проверка модов...')
    const modsDir = cfgModsDir(config)
    if (!fs.existsSync(modsDir)) fs.mkdirSync(modsDir, { recursive: true })

    // 6a. Авто-обновление nullclient JAR с GitHub Releases
    const updateResult = await checkAndUpdate(modsDir)
    // Строим актуальный список модов: nullclient JAR (из обновления или fallback) + статические
    const dynamicMods = []
    if (updateResult.jarName) {
      // Скачали или уже актуален — добавляем под известным именем
      dynamicMods.push({ name: updateResult.jarName, _exists: true })
    } else {
      // Fallback: ищем любой nullclient-*.jar уже в папке модов
      try {
        const found = fs.readdirSync(modsDir).find(f => f.startsWith('nullclient-') && f.endsWith('.jar'))
        if (found) {
          dynamicMods.push({ name: found, _exists: true })
          log.info('[mods] fallback to existing JAR:', found)
        } else {
          // Совсем нет — тянем с Google Drive как раньше
          dynamicMods.push({ name: 'nullclient-1.0.3.jar', src: 'gdrive', id: '1KKtm1p1736iTAyDl3ZpaBMwmdDRF7wFd' })
        }
      } catch {
        dynamicMods.push({ name: 'nullclient-1.0.3.jar', src: 'gdrive', id: '1KKtm1p1736iTAyDl3ZpaBMwmdDRF7wFd' })
      }
    }
    MODS = [...dynamicMods, ...STATIC_MODS]

    // 6b. Скачиваем остальные моды которых нет локально
    for (let i = 0; i < MODS.length; i++) {
      const mod  = MODS[i]
      const dest = path.join(modsDir, mod.name)
      const base = 54 + Math.round((i / MODS.length) * 16)

      if (mod._exists || fs.existsSync(dest)) {
        sendProgress('mods', base + 5, `✓ ${mod.name}`)
        log.info('[mods] already exists:', mod.name)
        continue
      }

      sendProgress('mods', base, `Скачивание ${mod.name}...`)
      log.info('[mods] downloading:', mod.name)
      const ok = await downloadFile(mod, dest, pct =>
        sendProgress('mods', base + Math.round((pct/100)*5), `${mod.name} — ${pct}%`)
      )
      if (!ok) return { success: false, error: `Не удалось скачать ${mod.name}.\nПроверь интернет.` }
      log.info('[mods] downloaded:', mod.name)
    }

    // 7. Аргументы
    sendProgress('launch', 72, 'Подготовка аргументов...')
    const jvmArgs    = buildJvmArgs(config, ver, cp.entries)
    const clientArgs = buildClientArgs(config, ver)
    const allArgs    = [...jvmArgs, ver.mainClass, ...clientArgs]

    // Используем java.exe для видимости ошибок
    const javaExe = javaw.replace(/javaw\.exe$/i, 'java.exe')

    log.info('[MC] Java:',      javaExe)
    log.info('[MC] CWD:',       config.gameDir)
    log.info('[MC] MainClass:', ver.mainClass)
    log.info('[MC] FullArgs:',  allArgs.join(' '))

    // 8. Запуск
    sendProgress('launch', 88, 'Запуск Minecraft...')
    mcProcess = spawn(javaExe, allArgs, {
      cwd:         config.gameDir,
      windowsHide: false,
      stdio:       ['ignore', 'pipe', 'pipe'],
    })

    mcProcess.stdout?.on('data', d => log.info('[MC]',     d.toString().trimEnd()))
    mcProcess.stderr?.on('data', d => log.error('[MC-ERR]', d.toString().trimEnd()))
    mcProcess.on('spawn', ()  => log.info('[MC] Process started, PID:', mcProcess.pid))
    mcProcess.on('error', e   => log.error('[MC] Spawn error:', e.message))
    mcProcess.on('exit',  (c,s) => {
      log.info(`[MC] Exited. code=${c} signal=${s}`)
      if (c !== 0 && c !== null) log.error('[MC] Crashed with code:', c)
    })

    sendProgress('done', 100, 'Minecraft запущен!')
    log.info('=== Launch success ===')
    return { success: true }

  } catch (e) {
    log.error('Launch exception:', e.stack || e.message)
    return { success: false, error: 'Внутренняя ошибка: ' + e.message }
  }
}

// ═══════════════════════════════════════════════════════════════════
//  ELECTRON WINDOW
// ═══════════════════════════════════════════════════════════════════
function appFile(...parts) { return path.join(app.getAppPath(), ...parts) }

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 780, height: 500, resizable: false,
    frame: false, transparent: false, backgroundColor: '#0d0f14',
    icon: path.join(process.resourcesPath || __dirname, 'assets', 'app.ico'),
    webPreferences: { preload: appFile('preload.js'), contextIsolation: true, nodeIntegration: false },
    show: false,
  })
  mainWindow.loadFile(appFile('launcher.html'))
  mainWindow.once('ready-to-show', () => mainWindow.show())
  mainWindow.webContents.setWindowOpenHandler(({ url }) => { shell.openExternal(url); return { action: 'deny' } })
}

app.whenReady().then(createWindow)
app.on('window-all-closed', () => app.quit())

// ═══════════════════════════════════════════════════════════════════
//  IPC HANDLERS
// ═══════════════════════════════════════════════════════════════════
ipcMain.on('window:minimize',     () => mainWindow?.minimize())
ipcMain.on('window:close',        () => mainWindow?.close())
ipcMain.on('open:url',            (_, url) => shell.openExternal(url))
ipcMain.on('settings:openFolder', () => { const p = cfgModsDir(cfgLoad()); if (!fs.existsSync(p)) fs.mkdirSync(p,{recursive:true}); shell.openPath(p) })
ipcMain.on('get:assetpath',       (e, name) => { e.returnValue = path.join(process.resourcesPath || __dirname, 'assets', name) })

ipcMain.handle('settings:get', () => {
  const s = cfgLoad()
  return { gameDir: s.gameDir, javaPath: s.javaPath||'', modsPath: s.modsPath||cfgModsDir(s),
           minRam: s.minRam||512, maxRam: s.maxRam||2048,
           extraJvmArgs: s.extraJvmArgs||'', extraClientArgs: s.extraClientArgs||'', mcVersion: s.mcVersion||'' }
})

ipcMain.handle('settings:save', (_, data) => { cfgSave(data); log.info('Settings saved'); return { ok: true } })

ipcMain.handle('settings:browse', async (_, type) => {
  const config = cfgLoad()
  if (type === 'java') {
    const r = await dialog.showOpenDialog(mainWindow, { title: 'Выбери javaw.exe', defaultPath: config.javaPath||'C:\\', filters: [{name:'Executable',extensions:['exe']}], properties: ['openFile'] })
    return r.canceled ? null : r.filePaths[0]
  }
  const r = await dialog.showOpenDialog(mainWindow, {
    title: type === 'gameDir' ? 'Выбери папку игры' : 'Выбери папку mods',
    defaultPath: type === 'gameDir' ? config.gameDir : cfgModsDir(config),
    properties: ['openDirectory'],
  })
  return r.canceled ? null : r.filePaths[0]
})

ipcMain.handle('launch:game', async () => {
  log.info('launch:game IPC received')
  return await launchGame()
})

ipcMain.handle('update:check', async () => {
  log.info('update:check IPC received')
  const config  = cfgLoad()
  const modsDir = cfgModsDir(config)
  if (!fs.existsSync(modsDir)) fs.mkdirSync(modsDir, { recursive: true })
  return await checkAndUpdate(modsDir)
})
