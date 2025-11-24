@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM
@REM Optional ENV vars
@REM   MVNW_REPOURL - repo url base for downloading maven distribution
@REM   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM   MVNW_VERBOSE - true: enable verbose log; debug: trace the mvnw script; others: silence the output
@REM ----------------------------------------------------------------------------

@if "%__MVNW_ARG0_NAME__%"=="" (set __MVNW_ARG0_NAME__=%~nx0)
@set __MVNW_CMD__=
@set __MVNW_ERROR__=
@set __MVNW_PSMODULEP_SAVE=%PSModulePath%
@set PSModulePath=
@for /F "usebackq tokens=1* delims==" %%A in (`powershell -noprofile "& {$exe = Get-Command '${env:COMSPEC:-cmd.exe}' -ErrorAction SilentlyContinue; if ($exe -ne $null) { $FullName = $exe.Definition; if ($FullName -eq $null) { $FullName = $exe.Source }; echo ('COMSPEC_FQCN=' + $FullName) } }"`) do @set %%A
@set PSModulePath=%__MVNW_PSMODULEP_SAVE%
@if "%COMSPEC_FQCN%"=="" (set COMSPEC_FQCN=%COMSPEC%)
@if "%COMSPEC_FQCN%"=="" (set COMSPEC_FQCN=cmd.exe)
@set MAVEN_PROJECTBASEDIR=%~dp0
@if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

@set MAVEN_WRAPPER_VERSION=3.2.0
@set MAVEN_USER_HOME=%MAVEN_USER_HOME%
@if "%MAVEN_USER_HOME%"=="" set MAVEN_USER_HOME=%USERPROFILE%/.m2
@set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
@set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/%MAVEN_WRAPPER_VERSION%/maven-wrapper-%MAVEN_WRAPPER_VERSION%.jar

@REM Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
@if exist "%WRAPPER_JAR%" (
    for /F "usebackq tokens=1,2 delims==" %%A in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") do (
        if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
    )
) else (
    if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" 2>nul
    @set DOWNLOAD_URL=%WRAPPER_URL%
    @if not "%MVNW_REPOURL%"=="" (
        SET DOWNLOAD_URL=%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/%MAVEN_WRAPPER_VERSION%/maven-wrapper-%MAVEN_WRAPPER_VERSION%.jar
    )
    @echo Downloading Maven Wrapper from: %DOWNLOAD_URL%
    powershell -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri '%DOWNLOAD_URL%' -OutFile '%WRAPPER_JAR%' } catch { Write-Error $_.Exception.Message; exit 1 }" || (
        @echo.
        @echo Error: Could not download Maven Wrapper Jar from %DOWNLOAD_URL%
        set __MVNW_ERROR__=1
    )
    if not exist "%WRAPPER_JAR%" (
        echo.
        echo Error: Failed to download Maven Wrapper.
        set __MVNW_ERROR__=1
    )
)
@if not "%__MVNW_ERROR__%"=="" (
    @if exist "%WRAPPER_JAR%" del /f /q "%WRAPPER_JAR%" 2>nul
    @exit /B 1
)

@REM Provide a "standardized" way to retrieve the CLI args that will
@REM work with both Windows and non-Windows executions.
set MAVEN_CMD_LINE_ARGS=%*

@set JAVACMD=java
@if not "%JAVA_HOME%"=="" (
    @set JAVACMD=%JAVA_HOME%\bin\java
)

@REM Begin escape routine to accommodate MavenWrapperMain's parsing of windows-style paths
@set BAT_SLASH=\
@set POM_SLASH=/
@set WRAPPER_JAR_ESCAPED=%WRAPPER_JAR:\=/%
@set MAVEN_PROJECTBASEDIR_ESCAPED=%MAVEN_PROJECTBASEDIR:\=/%
@set MAVEN_USER_HOME_ESCAPED=%MAVEN_USER_HOME:\=/%

@REM Execute a Java class with the found JVM
"%JAVACMD%" ^
  -classpath "%WRAPPER_JAR_ESCAPED%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR_ESCAPED%" ^
  "-Dmaven.home=%MAVEN_USER_HOME_ESCAPED%" ^
  %MAVEN_OPTS% ^
  %__MVNW_JAVA_OPTS% ^
  %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%

@set __MVNW_ARG0_NAME__=
@set __MVNW_CMD__=
@set __MVNW_ERROR__=
@set WRAPPER_JAR=
@set WRAPPER_LAUNCHER=
