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