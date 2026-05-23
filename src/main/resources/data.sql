--DELETE FROM film_genres;
--DELETE FROM film_likes;
--DELETE FROM friendships;
--DELETE FROM films;
--DELETE FROM users;
--DELETE FROM review_likes;
--DELETE FROM reviews;

--ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
--ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
--ALTER TABLE reviews ALTER COLUMN id RESTART WITH 1;

MERGE INTO friendship_statuses KEY (id) VALUES (1, 'PENDING');
MERGE INTO friendship_statuses KEY (id) VALUES (2, 'CONFIRMED');

MERGE INTO mpa_ratings KEY (id) VALUES (1, 'G');
MERGE INTO mpa_ratings KEY (id) VALUES (2, 'PG');
MERGE INTO mpa_ratings KEY (id) VALUES (3, 'PG-13');
MERGE INTO mpa_ratings KEY (id) VALUES (4, 'R');
MERGE INTO mpa_ratings KEY (id) VALUES (5, 'NC-17');

MERGE INTO genres KEY (id) VALUES (1, 'Комедия');
MERGE INTO genres KEY (id) VALUES (2, 'Драма');
MERGE INTO genres KEY (id) VALUES (3, 'Мультфильм');
MERGE INTO genres KEY (id) VALUES (4, 'Триллер');
MERGE INTO genres KEY (id) VALUES (5, 'Документальный');
MERGE INTO genres KEY (id) VALUES (6, 'Боевик');
