#!/bin/bash

BASE_DIR=$(cd "$(dirname "$0")" && pwd)
APP_NAME="$BASE_DIR/zhishilu.jar"

if [ ! -f "$APP_NAME" ]; then
  echo "未找到 jar 文件: $APP_NAME"
  exit 1
fi

LOG_DIR="$BASE_DIR/logs"
GC_LOG="$LOG_DIR/gc.log"
APP_LOG="$LOG_DIR/app.out"
PID_FILE="$BASE_DIR/app.pid"
HEAP_DUMP_PATH="$LOG_DIR"

mkdir -p "$LOG_DIR"

JAVA_OPTS="\
-Xms512m \
-Xmx512m \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=$HEAP_DUMP_PATH \
-XX:+ExitOnOutOfMemoryError \
-Dfile.encoding=UTF-8 \
-Xlog:gc*:file=$GC_LOG:time,level,tags:filecount=5,filesize=20M"

start() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
      echo "应用已在运行中，PID=$PID"
      exit 1
    else
      rm -f "$PID_FILE"
    fi
  fi

  echo "正在启动 zhishilu.jar ..."
  nohup java $JAVA_OPTS -jar "$APP_NAME" > "$APP_LOG" 2>&1 &
  PID=$!
  echo $PID > "$PID_FILE"
  echo "启动成功，PID=$PID"
  echo "应用日志: $APP_LOG"
  echo "GC日志: $GC_LOG"
}

stop() {
  if [ ! -f "$PID_FILE" ]; then
    echo "未找到 PID 文件，应用可能未运行"
    exit 1
  fi

  PID=$(cat "$PID_FILE")
  if ps -p "$PID" > /dev/null 2>&1; then
    echo "正在停止应用，PID=$PID ..."
    kill "$PID"
    sleep 3

    if ps -p "$PID" > /dev/null 2>&1; then
      echo "进程未退出，强制停止 PID=$PID"
      kill -9 "$PID"
    fi

    rm -f "$PID_FILE"
    echo "应用已停止"
  else
    echo "进程不存在，清理 PID 文件"
    rm -f "$PID_FILE"
  fi
}

status() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
      echo "应用运行中，PID=$PID"
      exit 0
    fi
  fi
  echo "应用未运行"
  exit 1
}

restart() {
  stop
  start
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    restart
    ;;
  status)
    status
    ;;
  *)
    echo "用法: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac