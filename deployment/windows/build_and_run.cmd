if exist deployment\windows\Gossip-1.0-SNAPSHOT-all.jar del deployment\windows\Gossip-1.0-SNAPSHOT-all.jar
if exist deployment\windows\service.ini del deployment\windows\service.ini
if exist deployment\windows\test_inis rmdir /f /s deployment\windows\test_inis
gradlew clean build shadowJar && ^
copy build\libs\Gossip-1.0-SNAPSHOT-all.jar deployment\windows\ && ^
copy build\resources\main\service.ini deployment\windows\ && ^
Xcopy build\resources\main\test_inis deployment\windows\test_inis\ /Y && ^
java -jar deployment\windows\Gossip-1.0-SNAPSHOT-all.jar -c deployment\windows\service.ini