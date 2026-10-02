# NullClient (Minecraft 26.2)

## Запуск в Windows

Для Gradle нужен установленный JDK, чтобы запустить Gradle Wrapper. Достаточно JDK 21 или новее. Для компиляции и запуска клиента проект использует JDK 25: Gradle автоматически скачает его через Foojay при первой сборке. Для этого нужен доступ к интернету.

Чтобы запустить клиент двойным щелчком, откройте `start.bat` в папке проекта.

Если `JAVA_HOME` уже указывает на JDK 21 или новее, откройте PowerShell в папке проекта и выполните:

```powershell
.\gradlew.bat runClient
```

Если Java не находится, укажите путь к установленному JDK (замените путь, если он у вас отличается), затем запустите игру:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runClient
```

Сборка JAR-файла:

```powershell
.\gradlew.bat build
```

Готовый мод появится в `build\libs`.

## IntelliJ IDEA

Откройте проект как Gradle-проект, выберите в **Settings → Build, Execution, Deployment → Build Tools → Gradle** установленный JDK 21 или новее для Gradle JVM, затем обновите Gradle-проект. Для конфигураций **Minecraft Client** и **Minecraft Server** используется JDK 25 из Gradle toolchain: Minecraft 26.2 и проект собраны под Java 25. Если JDK 25 ещё не скачан, сначала выполните Gradle-задачу `build`, чтобы Gradle установил toolchain.
