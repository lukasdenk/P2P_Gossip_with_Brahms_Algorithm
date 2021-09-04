Remove-Item deployment\windows\Gossip.jar
Remove-Item deployment\windows\service.ini
Copy-Item -Path out\artifacts\Gossip_jar\Gossip.jar -Destination deployment\windows\ -PassThru
Copy-Item -Path out\production\resources\service.ini -Destination deployment\windows\ -PassThru