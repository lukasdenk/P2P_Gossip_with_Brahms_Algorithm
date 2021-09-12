.\gradlew clean
if (Test-Path deployment\windows\Gossip-1.0-SNAPSHOT-all.jar)
{
Remove-Item deployment\windows\Gossip-1.0-SNAPSHOT-all.jar
}
if (Test-Path deployment\windows\settings.ini)
{
Remove-Item deployment\windows\settings.ini
}
if (Test-Path deployment\windows\test_inis)
{
Remove-Item deployment\windows\test_inis -Recurse
}
.\gradlew build
.\gradlew shadowJar
Copy-Item -Path build\libs\Gossip-1.0-SNAPSHOT-all.jar -Destination deployment\windows\ -PassThru
Copy-Item -Path build\resources\main\settings.ini -Destination deployment\windows\ -PassThru
Copy-Item -Path build\resources\main\test_inis -Destination deployment\windows\ -Recurse
