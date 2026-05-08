-- Пример правильного наполнения data.sql
MERGE INTO FriendshipStatus KEY(id) VALUES (1, 'Неподтвержденная');
MERGE INTO FriendshipStatus KEY(id) VALUES (2, 'Подтвержденная');

MERGE INTO MpaRating KEY(id) VALUES (1, 'G');
MERGE INTO MpaRating KEY(id) VALUES (2, 'PG');
MERGE INTO MpaRating KEY(id) VALUES (3, 'PG-13');
MERGE INTO MpaRating KEY(id) VALUES (4, 'R');
MERGE INTO MpaRating KEY(id) VALUES (5, 'NC-17');

MERGE INTO Genre KEY(id) VALUES (1, 'Комедия');
MERGE INTO Genre KEY(id) VALUES (2, 'Драма');
MERGE INTO Genre KEY(id) VALUES (3, 'Мультфильм');
MERGE INTO Genre KEY(id) VALUES (4, 'Триллер');
MERGE INTO Genre KEY(id) VALUES (5, 'Документальный');
MERGE INTO Genre KEY(id) VALUES (6, 'Боевик');
