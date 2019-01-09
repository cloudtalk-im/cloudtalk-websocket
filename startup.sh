#-Xverify:none -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider
#-Xrunjdwp:transport=dt_socket,address=8888,suspend=n,server=y

nohup java -Xverify:none -Xms64m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -Dtio.default.read.buffer.size=2048 -XX:HeapDumpPath=./zhangwuji_imwebsocket-pid.hprof -cp ./config:./lib/* com.zhangwuji.websocket.server.IMWebSocketStarter &

#java -Xverify:none -Xms64m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -Dtio.default.read.buffer.size=2048 -XX:HeapDumpPath=./zhangwuji_imwebsocket-pid.hprof -cp ./config:./lib/* -jar ./lib/cloudtalk-websocket-1.0-SNAPSHOT.jar
