#!/bin/bash
API_ROOT="http://localhost:9200/"
TEMPLATE_INFO="_template/template_1"
TEMPLATE_PATH="template.json"

curl -X PUT -d @${TEMPLATE_PATH} ${API_ROOT}${TEMPLATE_INFO}