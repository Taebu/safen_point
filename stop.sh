ps -ef | grep safen_point/bin | grep java | grep -v grep | awk '{print $2}' | xargs kill -9
