./gradlew clean && ./gradlew build
cp build/libs/Gossip-1.0-SNAPSHOT.jar deployment/mac
cp src/main/resources/service.ini deployment/mac
java -jar deployment/mac/Gossip-1.0-SNAPSHOT.jar -c deployment/mac/service.ini
#rm deployment/mac/Gossip-1.0-SNAPSHOT.jar
#rm deployment/mac/service.ini