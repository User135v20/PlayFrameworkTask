Данная программа написана на scala(2.13.8) с помощью Play framework, использующая библиотеки:
Slick (v3.3.3)
circe (0.14.1)
postgresql (9.4-1204-jdbc42)

Есть два сценария использования:
1. Создание двух таблиц в Postgresql и заполнение данными из дампа Википедии, расположенного в папке resources.
   Таблица-справочник с категориями статей, вторая с полями (create_timestamp, timestamp, language, wiki, category, title, auxiliary_text)
2. Вывод полей найденной статьи из второй таблицы


Подключение к базе происходит через локальный хост со следующими параметрами:  
portNumber = "5432"  
databaseName = "postgres"  
user = "postgres"  
password = "admin"


Первый сценарий использования.
вызов сценария: localhost:9000/create  
Названия создаваемых таблиц: "ArticleTable", "CategoryTable".  
В первой содержатся все статьи из дампа с полями create_timestamp, timestamp, language, wiki, category, title, auxiliary_text.
вторая таблица -- справочник категорий.
При каждом запуске проверяется существование таблиц, если они существуют, то удаляются. Далее идет создание и заполнение.

Второй сценарий использования.
вызов сценария: localhost:9000/search/<название статьи>
в url можно указывать как через пробелы, так и через нижние подчеркивания.
Если статья в таблице существует, будут выведены в консоль поля из первой таблицы, если нет -- not found.

Для работы программы необходимо добавить разархивированный дамп из Вкикипедии в папку resources.
Дамп можно скачать по ссылке https://dumps.wikimedia.org/other/cirrussearch/current/
с ruwikiquote-20211220-cirrussearch-general.json.gz работает.


