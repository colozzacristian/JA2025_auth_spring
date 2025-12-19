#!/bin/bash
export $(cat .env | xargs)
mvn clean package jetty:run