#!/usr/bin/env bash

mysql < sql/mysql/create-test-user.sql
mysql -u test --password=test test < sql/mysql/schema-mysql-1.sql
