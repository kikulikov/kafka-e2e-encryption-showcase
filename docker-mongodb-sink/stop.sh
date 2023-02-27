#!/usr/bin/env bash

cd "$(dirname "$0")" || exit

docker-compose --env-file docker-compose-mongodb.env \
--file docker-compose-confluent.yml --file docker-compose-mongodb.yml down
