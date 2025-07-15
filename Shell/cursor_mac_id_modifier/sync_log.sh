#!/bin/bash

# 定义日志文件路径
LOG_FILE="/tmp/cursor_mac_id_modifier.log"

main() {
    echo "Copying $LOG_FILE to $pwd"
    cp -r $LOG_FILE ./
    echo "Copied"
}

# 执行主函数
main