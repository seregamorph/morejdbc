
https://download.liquibase.org/download/
Set environment variable LIQUIBASE_HOME
Copy driver to $LIQUIBASE_HOME/lib

#### Run docker with Oracle XE 11 release 2
```
cd sql/oracle
rm -f init/.cache
docker run --rm -it -p 1521:1521 -v $PWD/init:/docker-entrypoint-initdb.d --name morejdbc-oracle wnameless/oracle-xe-11g-r2
```
You can pass the tablespace volume with extra parameter `-v $HOME/oracle_data:/u01/app/oracle` 

#### Create Oracle schema as test user
```

# Workaround only for XE and Russian locale (ORA-12705: Cannot access NLS data files or invalid environment specified)
# export JAVA_OPTS="-Duser.country=en -Duser.language=en"
$LIQUIBASE_HOME/liquibase --url=jdbc:oracle:thin:@127.0.0.1:1521:XE --username=test --password=test --changeLogFile=changelog.xml --logLevel=info update
```

#### Run tests
```
OracleJdbcCallTest
OracleNamedJdbcCallTest
```

#### Stop docker with Oracle (auto deletes container)
```
docker stop oracle
```

For more information:
https://hub.docker.com/r/sath89/oracle-xe-11g/
https://github.com/MaksymBilenko/docker-oracle-xe-11g
