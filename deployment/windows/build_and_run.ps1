.\gradlew clean
if (Test-Path deployment\windows\Gossip-1.0-SNAPSHOT-all.jar)
{
Remove-Item deployment\windows\Gossip-1.0-SNAPSHOT-all.jar
}
if (Test-Path deployment\windows\service.ini)
{
Remove-Item deployment\windows\service.ini
}
if (Test-Path deployment\windows\test_inis)
{
Remove-Item deployment\windows\test_inis -Recurse
}
.\gradlew build
.\gradlew shadowJar
Copy-Item -Path build\libs\Gossip-1.0-SNAPSHOT-all.jar -Destination deployment\windows\ -PassThru
Copy-Item -Path build\resources\main\service.ini -Destination deployment\windows\ -PassThru
Copy-Item -Path build\resources\main\test_inis -Destination deployment\windows\ -Recurse
java -jar deployment/windows/Gossip-1.0-SNAPSHOT-all.jar -c deployment/windows/service.ini