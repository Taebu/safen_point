# nohup /home/point/safen_point/startw.sh 로 호출해야 백그라운드로 구동됨
# nohup /home/point/safen_point/startw.sh
# 이것을 작업관리자에 등록하면 윈도우 서비스처럼 구동이 가능함.<단, 경로를 적절히 수정>
# /home/point/safen_point/stop.sh >>"/home/point/safen_point/killme.txt"
ping 127.0.0.1 -c 4
ps -ef | grep safen_point/bin | grep java | grep -v grep | awk '{print $2}' | xargs kill -9
/home/java/bin/java -Dfile.encoding=UTF-8 -classpath ".:/home/point/safen_point/bin:/home/point/safen_point/libs/mysql-connector-java-5.1.35.jar:/home/point/safen_point/libs/json-simple-1.1.1.jar:/home/point/safen_point/libs/log4j-1.2.17.jar" kr.co.cashq.safen_point.SAFEN_POINT /home/point/safen_point 1>/dev/null 2>&1 &
