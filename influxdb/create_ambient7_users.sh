#!/bin/bash
# Usage: ./create_ambient7_users.sh [HOST] [USER:PASS]

HOST="$1"
AUTH="$2"
DB='ambient7'

RO_USER='ambient7_ro'
RW_USER='ambient7_rw'

if [ -z "$HOST" ];then
    HOST='localhost:8086'
fi
AUTH_PARAM=''
if [ -n "$AUTH" ];then
    AUTH_PARAM="-u$AUTH"
fi

function create_user() {
    user=$1
    perms=$2
    echo "Password for user $user "
    read pass
    curl -v -f --get "http://${HOST}/query" \
        --data-urlencode 'q=CREATE USER "'$user'" WITH PASSWORD '"'"$pass"'" \
        "$AUTH_PARAM"
    curl -v -f --get "http://${HOST}/query" \
        --data-urlencode 'q=GRANT '$perms' ON '$DB' TO "'$user'"' \
        "$AUTH_PARAM"
    echo -e "\n\n"
}

create_user $RO_USER "READ"
create_user $RW_USER "WRITE"

echo -e "\nDone"
