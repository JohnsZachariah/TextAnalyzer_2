#!/usr/bin/env bash

chmod +x build.sh run-web.sh run-console.sh verify-db.sh 2>/dev/null || true

echo "=========================================================="
echo "    WORD COUNTER & TEXT ANALYZER - SELECT LAUNCH MODE     "
echo "=========================================================="
echo " 1. Web Application (Browser UI at http://localhost:8080) [Default]"
echo " 2. Menu-Driven Console Application (Terminal CLI)"
echo " 3. Compile Project Only"
echo " 4. SQL Database & JDBC Verification Suite (Staff Demo)"
echo " 5. Exit"
echo "=========================================================="
read -p "Select mode [1/2/3/4/5, default: 1]: " choice

case "$choice" in
    2)
        ./run-console.sh
        ;;
    3)
        ./build.sh
        ;;
    4)
        ./verify-db.sh
        ;;
    5)
        echo "Exited."
        exit 0
        ;;
    *)
        ./run-web.sh
        ;;
esac
