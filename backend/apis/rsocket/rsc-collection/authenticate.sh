#!/bin/bash

export ACCESS_TOKEN=`curl -s --location --request POST 'http://localhost:8999/realms/eventsourcing-with-axon/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=my-backend' \
  --data-urlencode 'username=test1' \
  --data-urlencode 'password=test' \
  --data-urlencode 'grant_type=password' | jq -r .access_token`
