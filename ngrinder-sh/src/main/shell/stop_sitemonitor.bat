@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java -server -cp "lib/*" org.ngrinder.NGrinderAgentStarter --mode=sitemonitor --command=stop %*