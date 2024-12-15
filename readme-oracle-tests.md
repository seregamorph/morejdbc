
https://download.liquibase.org/download/
Set environment variable LIQUIBASE_HOME
Copy driver to $LIQUIBASE_HOME/lib

#### Run docker with Oracle XE
```shell
cd sql/oracle
rm -f init/.cache
docker run --rm --name morejdbc-oracle \
    -p 1521:1521 \
    -e ORACLE_PASSWORD="test" \
    -v $PWD/init:/container-entrypoint-initdb.d \
    gvenzl/oracle-free:23-slim

```
You can pass the tablespace volume with extra parameter `-v $HOME/oracle_data:/u01/app/oracle` 

#### Create Oracle schema as test user
```shell
# Workaround only for XE and Russian locale (ORA-12705: Cannot access NLS data files or invalid environment specified)
# export JAVA_OPTS="-Duser.country=en -Duser.language=en"
$LIQUIBASE_HOME/liquibase --url=jdbc:oracle:thin:@127.0.0.1:1521:FREE --username=test --password=test --changeLogFile=changelog.xml --logLevel=info update
```

#### Run tests
```
OracleJdbcCallTest
OracleNamedJdbcCallTest
```

#### Stop docker with Oracle (auto deletes container)
```shell
docker stop morejdbc-oracle
```

For more information:
https://github.com/wnameless/docker-oracle-xe-11g
https://hub.docker.com/r/wnameless/oracle-xe-11g-r2
