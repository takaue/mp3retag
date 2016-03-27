#!/bin/bash -v

rm -rf testMP3
cp -pr testbak testMP3
# compile
javac -Xdiags:verbose mp3retag.java 
if [ "$?" -ne "0" ]; then
  exit;
fi

# do test
# no option
java mp3retag testMP3/

# detail option
java mp3retag -d testMP3/

# convert option
java mp3retag --convert testMP3/

# detail option re
java mp3retag -d testMP3/

