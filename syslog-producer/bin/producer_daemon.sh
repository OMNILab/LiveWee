#!/bin/bash
#  Trap non-normal exit signals: 1/HUP, 2/INT, 3/QUIT, 15/TERM, ERR
trap founderror 1 2 3 15 ERR

founderror()
{
    exit 1
}

echo "daemon: $$"
pid=""
dname=$(dirname $0)

function producer_running()
{
    stat=1
    if [ "$pid" != "" ]; then
        ps cax | grep $pid > /dev/null
        stat=$?
    fi
    echo $stat
}

while true; do
    if [[ "$(producer_running)" == "1" ]]; then
        echo "Producer is stopped. Restarting ..."
        $dname/syslog_producer.sh -p 30515 &
        pid=$!
        echo "producer: $pid"
    fi
    sleep 5
done