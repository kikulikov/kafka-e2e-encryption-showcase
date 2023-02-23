#!/usr/bin/env bash

cd "$(dirname "$0")" || exit

echo "Submit Classifications.."

curl localhost:8081/subjects/field-classifications -XDELETE # delete the subject if incompatible

echo "{ \"schema\": \"$(sed 's/\"/\\\"/g' field-classifications.json)\" }" > field-classifications.schema

echo
curl localhost:8081/subjects/field-classifications/versions -XPOST \
-H "Content-Type: application/json" -d @field-classifications.schema

rm -f field-classifications.schema

echo
echo "Submit Metadata.."

curl localhost:8081/subjects/field-metadata -XDELETE # delete the subject if incompatible

echo "{ \"schema\": \"$(sed 's/\"/\\\"/g' field-metadata.json)\" }" > field-metadata.schema

echo
curl localhost:8081/subjects/field-metadata/versions -XPOST \
-H "Content-Type: application/json" -d @field-metadata.schema

rm -f field-metadata.schema