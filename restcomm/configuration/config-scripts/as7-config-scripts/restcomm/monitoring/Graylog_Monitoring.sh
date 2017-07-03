#!/bin/bash

BASEDIR=$(cd $(dirname "${BASH_SOURCE[0]}") && pwd)
SERVERLABEL=""
GRAYLOG_SERVER=""

HDmonitor(){
    #Collect HD Data from Host
    hdusage=`df -hP $PWD | awk '/[0-9]%/{print $(NF-1)}'`
    #Send data to graylog
    message={"\"host\"":"\"${SERVERLABEL}\"","\"message\"":"\"${hdusage}\""}
    curl  --connect-timeout 10 --max-time 15  -XPOST http://$GRAYLOG_SERVER:5555/gelf -p0 -d ${message}
}


RCJVMonitor(){
    #FInd RMS process number
    rcprocess=$(jps | grep jboss-modules.jar | cut -d " " -f 1)
    #Run JVMTOP
    jvmvars=` $BASEDIR/../jvmtop.sh --once  | grep ${rcprocess}  | sed -e "s/  */ /g" | sed -e "s/%//g" | sed -e "s/m//g"  | cut -f3,4,5,6,7,8 -d ' ' `
    #Send data to graylog
    IFS=" " read HPCUR HPMAX NHCUR NHMAX CPU GC <<< $jvmvars
    message={"\"host\"":"\"${SERVERLABEL}\"","\"message\"":"\"RC_JVM_STATS\"","\"_HPCUR\"":"${HPCUR}","\"_HPMAX\"":"${HPMAX}","\"_NHCUR\"":"${NHCUR}","\"_NHMAX\"":"${NHMAX}","\"_CPU\"":"${CPU}","\"_GC\"":"${GC}"}
    curl  --connect-timeout 10 --max-time 15   -XPOST http://$GRAYLOG_SERVER:7777/gelf -p0 -d ${message}
}


RMSJVMonitor(){
    #FInd RMS process number
    while read -r line
    do
        if  ps -ef | grep $line | grep -q  mediaserver
        then
            msprocess=$line
        fi
   done < <(jps | grep Main | cut -d " " -f 1)

    #Run JVMTOP
    jvmvars=` $BASEDIR/../jvmtop.sh --once  | grep ${msprocess} | sed -e "s/  */ /g" | sed -e "s/%//g" | sed -e "s/m//g" | cut -f3,4,5,6,7,8 -d ' ' `
    #Send data to graylog
    IFS=" " read HPCUR HPMAX NHCUR NHMAX CPU GC <<< $jvmvars
    message={"\"host\"":"\"${SERVERLABEL}\"","\"message\"":"\"MS_JVM_STATS\"","\"_HPCUR\"":"${HPCUR}","\"_HPMAX\"":"${HPMAX}","\"_NHCUR\"":"${NHCUR}","\"_NHMAX\"":"${NHMAX}","\"_CPU\"":"${CPU}","\"_GC\"":"${GC}"}
    curl  --connect-timeout 10 --max-time 15   -XPOST http://$GRAYLOG_SERVER:7777/gelf -p0 -d ${message}
}

SERVERAMonitor(){
    #Collect RAM from host data
    MemTotal=`awk '( $1 == "MemTotal:" ) { print $2/1048576 }' /proc/meminfo`
    MemFree=`awk '( $1 == "MemFree:" ) { print $2/1048576 }' /proc/meminfo`
    Buffers=`awk '( $1 == "Buffers:" ) { print $2/1048576 }' /proc/meminfo`
    Cache=`awk '( $1 == "Cached:" ) { print $2/1048576 }' /proc/meminfo`
    SwapTotal=`awk '( $1 == "SwapTotal:" ) { print $2/1048576 }' /proc/meminfo`
    SwapFree=`awk '( $1 == "SwapFree:" ) { print $2/1048576 }' /proc/meminfo`

    #Send data to graylog
    message={"\"host\"":"\"${SERVERLABEL}\"","\"message\"":"\"Host_Heap\"","\"_MemTotal\"":"${MemTotal}","\"_MemFree\"":"${MemFree}","\"_Buffers\"":"${Buffers}","\"_Cache\"":"${Cache}","\"_SwapTotal\"":"${SwapTotal}","\"_SwapFree\"":"${SwapFree}"}
    curl --connect-timeout 10 --max-time 15  -XPOST http://$GRAYLOG_SERVER:6666/gelf -p0 -d ${message}
}

echo $1
$1
