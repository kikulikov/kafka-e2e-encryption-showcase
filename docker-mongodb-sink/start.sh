#!/usr/bin/env bash

cd "$(dirname "$0")" || exit

echo
echo "Starting the containers.."
docker-compose -f docker-compose-confluent.yml -f docker-compose-mongodb.yml up -d

echo
echo "Waiting for containers to start.."
sleep 10

echo
echo "Creating the [orders] collection in the default [test] database.."
docker exec -it mongodb mongosh -u root -p confluent --quiet --eval 'db.createCollection("orders")'
