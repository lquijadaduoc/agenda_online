#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
#
# Optional ENV vars
# -----------------
#   JAVA_HOME - location of a JDK home dir, required when download maven via java source
#   MVNW_REPOURL - repo url base for downloading maven distribution
#   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
#   MVNW_VERBOSE - true: enable verbose log; debug: trace the mvnw script; others: silence the output
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != debug ] || set -x

die() {
  printf '%s\n' "$@" >&2
  exit 1
}

# OS specific support.
native_path() { printf %s\\n "$1"; }
case "$(uname)" in
CYGWIN* | MINGW*)
  [ -z "${JAVA_HOME-}" ] || JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  native_path() { cygpath --path --windows "$1"; }
  ;;
esac

quiet() { "${MVNW_QUIET:-false}" = false || "$@" >/dev/null 2>&1; }

usage() {
  cat << EOF
Usage: $0 [options] [MAVEN_ARGS]
Options:
  -Dname=value - set Java system property
  -Pprofile    - activate profile
  -q           - quiet output
  -X           - debug output
EOF
}

while [ $# -gt 0 ]; do
  case "$1" in
  -h | --help)
    usage
    exit 0
    ;;
  -v | --version)
    MAVEN_ARGS="$MAVEN_ARGS --version"
    ;;
  *) MAVEN_ARGS="$MAVEN_ARGS \"$1\"" ;;
  esac
  shift
done

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_VERSION="3.2.0"
MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
JAR_DIR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
JAR_FILE="$JAR_DIR/maven-wrapper.jar"
PROPERTIES_FILE="$JAR_DIR/maven-wrapper.properties"
MAVEN_WRAPPER_REPOURL="${MVNW_REPOURL:-}"

log() { [ "${MVNW_VERBOSE-}" != true ] || printf %s\\n "$1"; }

if [ ! -f "$PROPERTIES_FILE" ]; then
  die "Cannot find $PROPERTIES_FILE"
fi

wrapperUrlPropertyValue() {
  while IFS="=" read -r key value; do
    if [ "$key" = "wrapperUrl" ]; then
      echo "$value"
      return
    fi
  done < "$PROPERTIES_FILE"
}

WRAPPER_URL="$(wrapperUrlPropertyValue)"
if [ -n "$MAVEN_WRAPPER_REPOURL" ]; then
  WRAPPER_URL="$MAVEN_WRAPPER_REPOURL/org/apache/maven/wrapper/maven-wrapper/$MAVEN_WRAPPER_VERSION/maven-wrapper-$MAVEN_WRAPPER_VERSION.jar"
fi

if [ ! -f "$JAR_FILE" ]; then
  log "Downloading Maven Wrapper jar from $WRAPPER_URL"
  mkdir -p "$JAR_DIR"
  if command -v curl >/dev/null 2>&1; then
    quiet curl -o "$JAR_FILE" "$WRAPPER_URL" || die "Failed to download $WRAPPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    quiet wget -O "$JAR_FILE" "$WRAPPER_URL" || die "Failed to download $WRAPPER_URL"
  else
    die "curl or wget are required to download Maven Wrapper"
  fi
fi

JAVA_EXE="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if [ ! -x "$JAVA_EXE" ]; then
  JAVA_EXE="java"
fi

exec "$JAVA_EXE" -classpath "$(native_path "$JAR_FILE")" "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
