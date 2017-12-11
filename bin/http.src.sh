#!/bin/bash -u

jre="java"
sws="$0"
opt=("-XX:+UseG1GC" "-XX:+UseStringDeduplication" "-XX:+DisableExplicitGC" "-XX:+UseCompressedOops" "-XX:+OptimizeStringConcat")

cmd=("${jre}" "${opt[@]}" "-jar" "${sws}" "$@")
"${cmd[@]}"

exit $?
