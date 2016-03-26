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
java mp3retag testMp3/

# detail option
java mp3retag -d testMp3/

# write option
java mp3retag --write testMp3/

# detail option re
java mp3retag -d testMp3/

