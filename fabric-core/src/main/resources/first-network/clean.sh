#!/usr/bin/env bash


echo "清除容器..."

if [ "$(docker ps -aq)" != "" ];then
    echo ">>>>>>>>>>>>>>>"
    docker ps -aq|xargs docker rm -f
else
    echo
    echo ">>没有发现需要清除的容器！"
    echo
fi
echo "清除镜像..."

if [ "$(docker images |grep dev-peer|awk '{print $3}')" != "" ];then
    echo ">>>>>>>>>>>>>>>"
    docker images |grep dev-peer|awk '{print $3}' |xargs docker rmi -f
else
    echo
    echo ">>没有发现需要清除的镜像！"
    echo
fi
