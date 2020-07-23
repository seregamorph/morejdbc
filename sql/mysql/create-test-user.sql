create database test;
create user 'test'@'%' identified by 'test';
grant all privileges on *.* to 'test'@'%';
flush privileges;