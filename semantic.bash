set -e
cd "$(dirname "$0")"
export CCHK="java -classpath ./lib/antlr-4.7.2-complete.jar:./bin princeYang.mxcc.MxCC"
$CCHK
