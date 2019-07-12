#!/bin/bash
API_ROOT="http://localhost:9200/"

for DIR in */
do
  EVENT=${DIR:0:-1}
  cd $DIR

  for FILE in *.json
  do
    UPPERCASE="kypo2-cz.muni.csirt.kypo.events.trainings.${EVENT}"
    LOWERCASE=${UPPERCASE,,}
    curl -X POST -d @"$FILE" "${API_ROOT}${LOWERCASE}/$UPPERCASE"
  done

  cd ..
done
