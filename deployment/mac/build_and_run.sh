./gradlew clean
rm -f deployment/mac/Gossip-1.0-SNAPSHOT-all.jar
rm -f deployment/mac/service.ini
rm -f -R deployment/mac/test_inis
./gradlew build
./gradlew shadowJar
cp build/libs/Gossip-1.0-SNAPSHOT-all.jar deployment/mac/
cp -R build/resources/main/test_inis deployment/mac/
cp build/resources/main/service.ini deployment/mac/
java -jar deployment/mac/Gossip-1.0-SNAPSHOT-all.jar -c deployment/mac/service.ini