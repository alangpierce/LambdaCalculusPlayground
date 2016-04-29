#!/usr/bin/env sh

# Wrapper .command file that runs the React Native server with the proper
# version of node.
THIS_DIR=`dirname $0`
cd "${THIS_DIR}"

# Note that this nvm stuff might fail if nvm isn't installed. This isn't a big
# deal, since that generally means that the default node/npm is fine anyway.
NVM_ROOT=`brew --prefix nvm`
source "${NVM_ROOT}/nvm.sh"
nvm use

npm start
read
