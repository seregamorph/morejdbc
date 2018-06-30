
https://download.liquibase.org/download/
Install liquibase 3.5.5 (don't install the latest, it's broken)
Set environment variable LIQUIBASE_HOME

#### Run docker with Oracle XE 11 release 2
```
docker run --rm -it -p 8088:8080 -p 1521:1521 --name oracle11.2 sath89/oracle-xe-11g
```
You can pass the volume with extra parameter '-v $HOME/oracle_data:/u01/app/oracle' 

####Run liquibase to create test user
```
# Workaround only for XE and Russian locale
# export JAVA_OPTS="-Duser.country=en -Duser.language=en"
$LIQUIBASE_HOME/liquibase --url=jdbc:oracle:thin:@127.0.0.1:1521:XE --username=system --password=oracle --changeLogFile=changelog-init.xml --logLevel=info update
```

####Create schema as test user
```
$LIQUIBASE_HOME/liquibase --url=jdbc:oracle:thin:@127.0.0.1:1521:XE --username=test --password=test  --changeLogFile=changelog-main.xml --logLevel=info update
```

####Run tests
```
OracleJdbcCallTest
OracleNamedJdbcCallTest
```
