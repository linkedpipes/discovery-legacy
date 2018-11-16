DOCKER_IMAGE_TAG=$1

green=`tput setaf 2`
reset=`tput sgr0`

echo "\n${green}Assembling fat jar using sbt assembly${reset}\n"

cd src 

sbt assembly 

mv ./target/scala-2.11/app-assembly.jar ./

cd ..

echo "\n${green}Staring docker build${reset}\n"

docker build -t $DOCKER_IMAGE_TAG .

echo "\n${green}Docker build succeeded! Performing cleanup...${reset}\n"

rm -rf ./src/app-assembly.jar

echo "\n${green}Cleanup completed! Removed fat jar!${reset}\n"
