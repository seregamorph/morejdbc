
https://download.liquibase.org/download/
Set environment variable LIQUIBASE_HOME
Copy driver to $LIQUIBASE_HOME/lib

#### Run docker with Oracle XE 18c (you need to build it first)
```
cd sql/oracle
docker run --name morejdbc-oracle -d -p 1521:1521 oracle/database:18.4.0-xe
```
You can pass the tablespace volume with extra parameter `-v $HOME/oracle_data:/u01/app/oracle` 

#### Create Oracle schema as test user
```
# Workaround only for XE and Russian locale (ORA-12705: Cannot access NLS data files or invalid environment specified)
# export JAVA_OPTS="-Duser.country=en -Duser.language=en"
$LIQUIBASE_HOME/liquibase --url=jdbc:oracle:thin:@127.0.0.1:1521:XEPDB1 --username=test --password=test --changeLogFile=changelog.xml --logLevel=info update
```

#### Run tests
```
OracleJdbcCallTest
OracleNamedJdbcCallTest
```

#### Stop docker with Oracle (auto deletes container)
```
docker stop morejdbc-oracle
```

For more information:
https://blogs.oracle.com/oraclemagazine/deliver-oracle-database-18c-express-edition-in-containers
