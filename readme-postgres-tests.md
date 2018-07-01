
https://download.liquibase.org/download/
Install liquibase 3.5.5 (don't install the latest, it's broken)
Set environment variable LIQUIBASE_HOME
Copy driver to $LIQUIBASE_HOME/lib

#### Run docker with Postgres 10.4
```
cd sql/mysql
docker run --rm -t -p 5432:5432 -e POSTGRES_PASSWORD=postgres -v $PWD/init:/docker-entrypoint-initdb.d --name postgres postgres:10
```

#### Create Postgres schema as test user
```
$LIQUIBASE_HOME/liquibase --url=jdbc:postgresql://127.0.0.1:5432/test --username=test --password=test --changeLogFile=changelog.xml --logLevel=info update
```

#### Run tests
```
PostgresJdbcCallTest
```

#### Stop docker with Oracle (auto deletes container)
```
docker stop postgres
```

