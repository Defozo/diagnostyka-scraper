#!/bin/bash

# java -jar /usr/src/app/diagnostyka-scraper.jar &
chromium-browser --headless --use-gl=swiftshader --disable-gpu --disable-software-rasterizer --disable-dev-shm-usage

# Wait for any process to exit
wait -n

# Exit with status of process that exited first
exit $?