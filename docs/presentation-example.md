---
marp: true
theme: default
paginate: true
class: invert
style: |
  section {
    font-size: 20px;
    padding: 24px 36px;
    background: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
    color: #e2e8f0;
  }
  h1 {
    font-size: 38px;
    color: #63b3ed;
    text-shadow: 0 2px 4px rgba(0,0,0,0.5);
  }
  h2 {
    font-size: 28px;
    color: #90cdf4;
    margin-bottom: 12px;
    text-shadow: 0 1px 2px rgba(0,0,0,0.5);
  }
  h3 { font-size: 22px; color: #a3bffa; }
  img { 
    max-height: 55vh; 
    display: block; 
    margin: 6px auto;
    border-radius: 6px;
    padding: 3px;
    background: #1a202c;
    box-shadow: 
      inset 0 2px 4px rgba(0,0,0,0.6),
      inset 0 -1px 3px rgba(0,0,0,0.3),
      0 0 0 1px rgba(255,255,255,0.04),
      0 4px 10px rgba(0,0,0,0.5);
  }
  code {
    font-size: 15px;
    background: #2d3748;
    color: #e2e8f0;
  }
  pre {
    font-size: 14px;
    background: #2d3748;
    border: 1px solid #4a5568;
  }
  table {
    font-size: 16px;
    background: #2d3748;
    border-collapse: collapse;
    width: 100%;
  }
  table th, table td {
    border: 1px solid #4a5568;
    padding: 10px 12px;
    background: #2d3748;
    color: #e2e8f0;
  }
  table th { background: #4a5568; font-weight: bold; }
  blockquote {
    font-size: 16px;
    border-left: 4px solid #fc8181;
    padding: 12px 16px;
    background: #2d3748;
    color: #e2e8f0;
  }
  .columns { display: flex; gap: 20px; }
  .columns > * { flex: 1; }
  footer { font-size: 12px; color: #a0aec0; }
---

<!-- СЛАЙД 1 -->
<!-- _class: lead -->
<!-- _paginate: false -->

# statflux

## Агрегатор статистики видео

**Java 21 · PostgreSQL · Telegram Bot**

Команда **rmrf** · МИФИ · Апрель 2026

---

<!-- СЛАЙД 2 – Зачем и для кого -->

## Зачем нужен этот бот

<style scoped>
table { font-size: 15px !important; }
table th, table td { padding: 9px 10px !important; }
</style>

| Аудитория | Боль | Что даёт statflux |
|---|---|---|
| **Владелец YouTube/VK Video-канала** | Несколько вкладок, ручное копирование цифр | Одна команда `/stats` – сводка по всем платформам |
| **Контент-менеджер** | Ручной мониторинг раз в день, легко пропустить спад | Авто-обновление каждые 15 мин, Telegram всегда под рукой |
| **Небольшая студия (1–5 чел.)** | Нет бюджета на BI-системы вроде Tableau или Power BI | Бесплатно, разворачивается за `docker compose up` |
| **Блогер / преподаватель** | Платформы дают разные единицы – сложно суммировать | `/list` – единый список с суммой просмотров |

---

<!-- СЛАЙД 3 – UI: как это выглядит -->

## Интерфейс бота

<div class="columns">
<div>

> Пользователь отправляет ссылку:
> `https://youtu.be/dQw4w9WgXcQ`

> Бот отвечает:
> [YouTube] Rick Astley – Never Gonna Give You Up
> 1 547 823 просмотров – добавлено

</div>
<div>

> `/list` – полный список:
>
> Ваши видео (3):
>
> 1. [YouTube] Rick Astley – Never...
>    1 547 823 · 2 мин назад · OK
>
> 2. [RuTube] Хакатон МИФИ 2026
>    8 902 · сейчас · OK
>
> 3. [YouTube] Старый ролик
>    5 000 (последние данные) · UNAVAILABLE
>
> [Обновить]   [<]   [>]

</div>
</div>

---

<!-- СЛАЙД 4 – Диаграмма: обновление статистики -->

## Сценарий: обновление статистики (кнопка / `/refresh`)
![](diagrams/update.png)

---

<!-- СЛАЙД 5 – Код: Chain of Responsibility -->

## Архитектура бота – Chain of Responsibility

```java
// Chain.java – Generic цепочка обработчиков
public class Chain<T> implements Consumer<T> {

    public interface Node<T> {
        void handle(T ctx, Consumer<T> next);
    }

    @Override
    public void accept(T ctx) {
        run(ctx, 0);
    }

    private void run(T ctx, int index) {
        if (index >= nodes.size()) {
            return;
        }

        nodes.get(index).handle(ctx, next -> {
            run(next, index + 1);
        });
    }
}
```

```java
// Main.java – ручная сборка, без Spring
TelegramBotRootConsumer bot = TelegramBotRootConsumer.builder()
    .withClient(new OkHttpTelegramClient(botConfig.getToken()))
    .use(new WhiteListMiddleware(botConfig.getWhiteList()))
    .use(new EchoHandler())
    .build();
```

> Добавить команду = `implements Chain.Node<TelegramBotContext>` + `.use(new MyHandler())`.

---

<!-- СЛАЙД 6 – Код: Repository / upsert -->

## Слой данных – upsert без ORM

```java
// JdbcLinkRepository.java
@Override
public boolean save(@NonNull LinkDto linkDto) {
    // сначала UPDATE – идемпотентность, дубли исключены
    int updated = queryExecutor.update(
        LinkSql.UPDATE,
        linkDto.rawLink(), linkDto.title(), linkDto.views(),
        linkDto.updatedAt(), linkDto.hostingName(), linkDto.hostingId()
    );

    // если строки не было – INSERT
    return updated == 1 || queryExecutor.update(
        LinkSql.INSERT,
        linkDto.hostingName(), linkDto.rawLink(), linkDto.hostingId(),
        linkDto.title(), linkDto.views(), linkDto.updatedAt()
    ) > 0;
}

@Override
public long getTotalViewSum() {
    return queryExecutor
        .query(LinkSql.GET_TOTAL_VIEW_SUM, rs -> rs.getLong(1))
        .getFirst();
}
```

Чистый JDBC + `QueryExecutor` поверх `PreparedStatement`. Никакого ORM.
