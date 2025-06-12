CREATE DATABASE comment_db;
CREATE DATABASE event_db;
CREATE DATABASE request_db;
CREATE DATABASE user_db;

CREATE USER comment_user WITH PASSWORD 'password1';
CREATE USER event_user WITH PASSWORD 'password2';
CREATE USER request_user WITH PASSWORD 'password3';
CREATE USER user_user WITH PASSWORD 'password4';

ALTER DATABASE comment_db OWNER TO comment_user;
ALTER DATABASE event_db OWNER TO event_user;
ALTER DATABASE request_db OWNER TO request_user;
ALTER DATABASE user_db OWNER TO user_user;