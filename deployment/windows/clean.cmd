if exist deployment\windows\Gossip-1.0-SNAPSHOT-all.jar del deployment\windows\Gossip-1.0-SNAPSHOT-all.jar
if exist deployment\windows\settings.ini del deployment\windows\settings.ini
if exist deployment\windows\test_inis\ rmdir /f /s deployment\windows\test_inis\
gradlew clean