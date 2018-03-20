#!/bin/sh
(cd result && docker build -t saiworkshop/result-app .)
(cd vote && docker build -t saiworkshop/voting-app .)
(cd worker && docker build -t saiworkshop/worker .)