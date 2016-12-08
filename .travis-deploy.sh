#!/bin/sh -e

deploy_doc() {
  ./gradlew :${1}:javadoc
  tar cvzf ${1}.tar.gz -C ${1}/build/docs/ javadoc
  curl -H "Content-Type: application/octet-stream" --data-binary @${1}.tar.gz \
       ${DOCUMENT_SERVER}/${TRAVIS_REPO_SLUG}/${TRAVIS_BUILD_NUMBER}/${1}
}

deploy_doc arducopter
