@echo off
if exist "C:\Users\Amrutha\apache-maven-3.9.9\bin\mvn.cmd" (
    "C:\Users\Amrutha\apache-maven-3.9.9\bin\mvn.cmd" %*
) else (
    mvn %*
)
