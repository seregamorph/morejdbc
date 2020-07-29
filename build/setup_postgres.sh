#!/usr/bin/env bash

psql -U postgres -f sql/postgres/create-test-user.sql
psql -U test -f sql/postgres/schema-postgres-1.sql
