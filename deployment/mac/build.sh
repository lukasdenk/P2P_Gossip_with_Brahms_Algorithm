./gradlew shadowJar
rm deployment/mac/Gossip-1.0-SNAPSHOT-all.jar
rm deployment/mac/service.ini
rm -R deployment/mac/test_inis
cp build/libs/Gossip-1.0-SNAPSHOT-all.jar deployment/mac/
cp -R build/resources/main/test_inis deployment/mac/
cp build/resources/main/service.ini deployment/mac/