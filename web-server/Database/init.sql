-- Base script: https://github.com/forana/postgres-init/blob/master/init.sql
\echo -------------------------------
\echo | Clearing out any old tables
\echo -------------------------------

drop table if exists Shoes;
drop table if exists Electronics;

\echo -------------------------------
\echo | Creating tables
\echo -------------------------------

-- A pair of shoes has an ID, a color, a name and a weight
create table Shoes (
    ID SERIAL primary key,
    shoe_color text,
    shoe_name text,
    shoe_type text
);

-- An electronic has an ID, a color, a name and a weight
create table Electronics (
    ID SERIAL primary key,
    electronic_color text,
    electronic_name text,
    electronic_weight numeric --In kilograms
);

\echo -------------------------------
\echo | Inserting data
\echo -------------------------------

insert into Shoes (shoe_color, shoe_name, shoe_type) values
    ('Green, black and white', 'Converse Future Comfort Chuck 70 AT-CX', 'Boots'), 
    ('Pink and black', 'Converse Stüssy Chuck 70', 'Boots');

insert into Electronics (electronic_color, electronic_name, electronic_weight) values
    ('Dark Gray', 'Realme 3', 0.5),
    ('Blue', 'iPhone 12', 1.7);

\echo -------------------------------
\echo | All done!
\echo -------------------------------