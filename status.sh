ps -ef|grep safen_point/bin|grep java|grep safen_point|awk 'BEGIN {FS=" "} {print $2}'

