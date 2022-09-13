-- Base script: https://github.com/forana/postgres-init/blob/master/init.sql
\echo -------------------------------
\echo | Clearing out any old tables
\echo -------------------------------

drop table if exists Products;
drop table if exists Customers; --https://www.meetspaceapp.com/2016/04/12/passwords-postgresql-pgcrypto.html

-- Store hashes of passwords
CREATE EXTENSION pgcrypto;
\echo -------------------------------
\echo | Creating tables
\echo -------------------------------

-- A customer has an ID, an username and a hashed password
create table Customers (
    ID uuid DEFAULT gen_random_uuid() PRIMARY KEY, --UUID Instead of a serial number
    username text NOT NULL UNIQUE,
    password text NOT NULL
);

-- A product has an ID, a type, a color, a name and additional info
create table Products (
    ID SERIAL primary key,
    product_type text NOT NULL,
    color text NOT NULL,
    product_name text NOT NULL,
    additional text NOT NULL,
    Owner_ID uuid,  
    CONSTRAINT fk_OwnerID FOREIGN KEY(Owner_ID) REFERENCES Customers(ID)  
);

\echo -------------------------------
\echo | Inserting data
\echo -------------------------------

insert into Customers(username, password) values
    ('user1',  crypt('test1', gen_salt('bf', 8))),
    ('rpaez', crypt('test2', gen_salt('bf', 8))),
    ('eruiz', crypt('test3', gen_salt('bf', 8)))
;
-- Retrieve  select * from Customers where username='eruiz' AND password=crypt('test3', password);

insert into Products (product_type, color, product_name, additional) values
    ('Shoes', 'Green, black and white', 'Converse Future Comfort Chuck 70 AT-CX', 'Boots'), 
    ('Shoes', 'Pink and black', 'Converse Stüssy Chuck 70', 'Boots'),
    ('Electronics', 'Dark Gray', 'Realme 3', '0.5 kg'),
    ('Electronics', 'Blue', 'iPhone 12', '1.7 kg')
;

\echo -------------------------------
\echo | All done!
\echo -------------------------------

/*
To connect
psql -h <hostname> -p <port> -U <username> -d <database> (If not local, password is prompted)
psql -U distribuidos -d distribuidos (Local)
*/