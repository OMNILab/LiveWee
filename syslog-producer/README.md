# syslog-producer

Listening to local UDP port and send messages (also ELTed) to Kafka cluster.

## Compile

    $ mvn package

## Usage

    usage: bin/syslog_produer.sh [-b <arg>] [-h] [-p <arg>] [-t <arg>]
     -b,--brokers <arg>   broker servers separated by commas
     -h,--help            print this message
     -p,--port <arg>      UDP port of source message
     -t,--topic <arg>     name of Kafka topic (ETLed topic is appended with 'etled' automatically)

## Kafka Topics

Two topics the messages sent to are programmed:

* `arubasyslog`
* `arubasyslog.etled`