
https://download.liquibase.org/download/
Set environment variable LIQUIBASE_HOME
Copy driver to $LIQUIBASE_HOME/lib

#### Run docker with MySQL 5.7
```
cd sql/mysql
docker run --rm -it -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -v $PWD/init:/docker-entrypoint-initdb.d --name mysql mysql:5.7
```

#### Create MySQL schema as test user
```
$LIQUIBASE_HOME/liquibase --url=jdbc:mysql://127.0.0.1:3306/test --username=test --password=test --changeLogFile=changelog.xml --logLevel=info update
```

#### Run tests
```
MysqlJdbcCallTest
```

#### Stop docker with MySQL (auto deletes container)
```
docker stop mysql
```

