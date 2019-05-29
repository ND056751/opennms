#!/bin/sh -e
echo "#### Executing tests"
cd ~/project/smoke-test-v2
# Iterate through the tests and stop after the first failure
pyenv local 3.5.2
for TEST_CLASS in $(python3 ../.circleci/scripts/find-tests.py --use-class-names . | circleci tests split)
do
  echo "###### Testing: ${TEST_CLASS}"
  mvn -N -DskipTests=false -DskipITs=false -DfailIfNoTests=false -Dit.test=$TEST_CLASS install verify || true
  # TODO: Remove || true from above so we don't run all the testsssss
done
