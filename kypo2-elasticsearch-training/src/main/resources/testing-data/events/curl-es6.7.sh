#!/bin/bash
API_ROOT="http://localhost:9200/"
TEMPLATE_INFO="_template/template_1"
TEMPLATE_PATH="../template.json"

for DIR in */
do
  cd $DIR
  for FILE in *.json
  do
	FILE_NAME="$FILE"
    TD_ID=$(jq '.training_definition_id' $FILE_NAME)
    TI_ID=$(jq '.training_instance_id' $FILE_NAME)
	EVENT=$(echo $FILE_NAME | grep -o "^[a-zA-Z]*")
    UPPERCASE="kypo3.cz.muni.csirt.kypo.events.trainings.${EVENT}_evt.definition=${TD_ID}.instance=${TI_ID}"
	LOWERCASE=${UPPERCASE,,}
	curl -X POST -d @$FILE_NAME "${API_ROOT}${LOWERCASE}/default" -H 'Content-Type: application/json'
  done

  cd ..
done
