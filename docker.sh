#!/bin/bash

function deploy
{
        echo "Try to deploy to docker: " $1
        docker build -t lww336/$1:$2 target
        docker push lww336/$1:$2
}

docker login -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
deploy $1 $2

exit