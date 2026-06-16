#!/usr/bin/bash
date=$(date '+%d\\%m\\%Y')
GREEN='\033[0;32m'
NC='\033[0m'
sed -i "4s/.*/Date: $date/" app/src/main/java/org/AS91907/*.java
if sudo ./gradlew shadowJar | grep 'BUILD SUCCESSFUL'; then
    printf -v prompt "%bCONTINUE:%b " "$GREEN" "$NC"
    read -r -p "$prompt"
    clear
    echo -e "${GREEN}BUILD SUCCESSFUL${NC} \n----------------"
    java -jar app/build/libs/app-all.jar
fi
