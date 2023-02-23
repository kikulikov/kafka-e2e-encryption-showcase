#!/usr/bin/env bash

cd "$(dirname "$0")" || exit

docker-compose -f docker-compose-confluent.yml -f docker-compose-mongodb.yml down
