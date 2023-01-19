#!/usr/bin/env bash

echo "Submit Classifications"

curl localhost:8081/subjects/field-classifications -XDELETE # delete the subject if incompatible

echo "{ \"schema\": \"$(cat field-classifications.json | sed 's/\"/\\\"/g')\" }" > field-classifications.schema

curl localhost:8081/subjects/field-classifications/versions -XPOST \
-H "Content-Type: application/json" -d @field-classifications.schema

rm -f field-classifications.schema

echo "Submit Metadata"

curl localhost:8081/subjects/field-metadata -XDELETE # delete the subject if incompatible

echo "{ \"schema\": \"$(cat field-metadata.json | sed 's/\"/\\\"/g')\" }" > field-metadata.schema

curl localhost:8081/subjects/field-metadata/versions -XPOST \
-H "Content-Type: application/json" -d @field-metadata.schema

rm -f field-metadata.schema