#!/bin/bash
API_ROOT="http://localhost:9200/"
TEMPLATE_INFO="_template/template_1"
TEMPLATE_PATH="../template.json"

curl -X PUT -d @${TEMPLATE_PATH} ${API_ROOT}${TEMPLATE_INFO} -H 'Content-Type: application/json'

for DIR in */
do
  EVENT=${DIR:0:-1}
  cd $DIR

  for FILE in *.json
  do
    UPPERCASE="kypo2-cz.muni.csirt.kypo.events.trainings.${EVENT}"
    LOWERCASE=${UPPERCASE,,}
    curl -X POST -d @"$FILE" "${API_ROOT}${LOWERCASE}/default" -H 'Content-Type: application/json'
  done

  cd ..
done
