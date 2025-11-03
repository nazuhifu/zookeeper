#!/bin/bash
set -e
BASE=$(pwd)
CONF_DIR="$BASE/conf"
BIN="$BASE/bin"

ZK1_CFG="zoo1.cfg"
ZK2_CFG="zoo2.cfg"
ZK3_CFG="zoo3.cfg"

ZK1_LOG="$BASE/zk1.log"
ZK2_LOG="$BASE/zk2.log"
ZK3_LOG="$BASE/zk3.log"

start_cluster() {
  echo "=== Starting ZooKeeper cluster ==="
  "$BIN/zkServer.sh" start "$ZK1_CFG" >"$ZK1_LOG" 2>&1 &
  echo "started zk1 (port 2181) → $ZK1_LOG"
  "$BIN/zkServer.sh" start "$ZK2_CFG" >"$ZK2_LOG" 2>&1 &
  echo "started zk2 (port 2182) → $ZK2_LOG"
  "$BIN/zkServer.sh" start "$ZK3_CFG" >"$ZK3_LOG" 2>&1 &
  echo "started zk3 (port 2183) → $ZK3_LOG"

  sleep 2
  echo "cluster starting; wait a few seconds for leader election..."
}

status_cluster() {
  echo "=== ZooKeeper cluster status ==="
  "$BIN/zkServer.sh" status "$ZK1_CFG" || echo "zk1 not running"
  echo "--------------------------------"
  "$BIN/zkServer.sh" status "$ZK2_CFG" || echo "zk2 not running"
  echo "--------------------------------"
  "$BIN/zkServer.sh" status "$ZK3_CFG" || echo "zk3 not running"
}

stop_cluster() {
  echo "=== Stopping ZooKeeper cluster ==="
  "$BIN/zkServer.sh" stop "$ZK1_CFG" || true
  "$BIN/zkServer.sh" stop "$ZK2_CFG" || true
  "$BIN/zkServer.sh" stop "$ZK3_CFG" || true
  echo "All nodes stopped."
}

case "$1" in
  start)
    start_cluster
    ;;
  status)
    status_cluster
    ;;
  stop)
    stop_cluster
    ;;
  *)
    echo "Usage: $0 {start|status|stop}"
    exit 1
    ;;
esac