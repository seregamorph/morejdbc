
https://download.liquibase.org/download/
Install liquibase 3.5.5 (don't install the latest, it's broken)
Set environment variable LIQUIBASE_HOME

#### Run docker with Oracle XE 11 release 2
```
docker run --rm -it -p 1521:1521 -v $PWD/sql/oracle/init:/docker-entrypoint-initdb.d sath89/oracle-xe-11g
```
You can pass the tablespace volume with extra parameter '-v $HOME/oracle_data:/u01/app/oracle' 

#### Create Oracle schema as test user
```
pushd sql/oracle
# Workaround only for XE and Russian locale
# export JAVA_OPTS="-Duser.country=en -Duser.language=en"
$LIQUIBASE_HOME/liquibase --url=jdbc:oracle:thin:@127.0.0.1:1521:XE --username=test --password=test --changeLogFile=changelog.xml --logLevel=info update
popd
```

#### Run tests
```
OracleJdbcCallTest
OracleNamedJdbcCallTest
```

For more information:
https://hub.docker.com/r/sath89/oracle-xe-11g/
https://github.com/MaksymBilenko/docker-oracle-xe-11g
