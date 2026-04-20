# Инструкция: сборка презентации и работа с диаграммами

---

## Сборка PPTX из presentation.md

### Нужно: Node.js

Проверить, установлен ли Node:
```bash
node -v
```

Если команда не найдена – скачать с [nodejs.org](https://nodejs.org) (LTS-версия). `npx` входит в состав Node.js, отдельно ничего не нужно.

### Команда сборки (из корня репо)

```bash
npx @marp-team/marp-cli@latest docs/presentation-example.md --pptx -o docs/presentation-example.pptx --allow-local-files
```

Флаг `--allow-local-files` обязателен – без него marp не загружает локальные PNG-файлы (диаграммы из `docs/diagrams/`).

При первом запуске npx автоматически скачает `@marp-team/marp-cli` (~100 МБ). Последующие запуски быстрее.

### Если Node.js нет и устанавливать не хочется – Docker

```bash
docker run --rm -v "$(pwd)":/home/marp/app \
  marpteam/marp-cli \
  docs/presentation-example.md --pptx -o docs/presentation.pptx --allow-local-files
```
---

## Экспорт PlantUML-диаграмм в PNG

Диаграммы хранятся в `src/main/resources/*.puml`. Экспортированные PNG кладём в `docs/diagrams/` и коммитим.

### Шаг 1: установить плагин в IntelliJ IDEA

```
Settings → Plugins → Marketplace → найти "PlantUML Integration" (автор: Eugene Steinberg) → Install → перезапустить IDE
```

### Шаг 2: открыть .puml файл

Открыть любой файл из `src/main/resources/`, например `update.puml`. В правой панели появится предпросмотр диаграммы.

### Шаг 3: экспортировать в PNG

В панели предпросмотра нажать иконку **"Save Diagram"** (или правая кнопка мыши на диаграмме → Save Diagram As) → выбрать формат PNG → сохранить в `docs/diagrams/`.

Имена файлов:
```
docs/diagrams/add.png      ← из add.puml
docs/diagrams/update.png   ← из update.puml
docs/diagrams/stats.png    ← из stats.puml
```

## Если обновил диаграмму – пересобери PNG и PPTX
