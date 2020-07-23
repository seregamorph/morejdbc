#!/usr/bin/env bash

CONTAINER_NAME="oracle18"

# pull from private repo
echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin
docker pull morejdbc/morejdbc-oracle-18:latest

# run
echo "Running $CONTAINER_NAME container"
docker run -d --name "$CONTAINER_NAME" -p 1521:1521 --expose=1521 morejdbc/morejdbc-oracle-18:latest

# wait database to be ready
DB_IS_READY="0"
while test "$DB_IS_READY" = "0";
do
  sleep 5;

  if docker logs "$CONTAINER_NAME" | grep -q 'DATABASE IS READY TO USE';
  then
    DB_IS_READY="1"
  fi

  if docker logs "$CONTAINER_NAME" | grep -q 'DATABASE SETUP WAS NOT SUCCESSFUL';
  then
    exit 1
  fi
  echo "It's Oracle. Please wait..."
done;
###

docker exec -i "$CONTAINER_NAME" sqlplus / as sysdba << EOF
$(cat sql/oracle/create-test-user.sql)
exit;
EOF

docker exec -i "$CONTAINER_NAME" sqlplus test/test << EOF
$(cat sql/oracle/schema-oracle-1.sql)
exit;
EOF
