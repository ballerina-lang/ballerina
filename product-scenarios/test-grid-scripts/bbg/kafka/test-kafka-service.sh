#!/bin/bash
# Copyright (c) 2019, WSO2 Inc. (http://wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

readonly test_kafka_parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
readonly test_kafka_grand_parent_path=$(dirname ${test_kafka_parent_path})
readonly test_kafka_great_grand_parent_path=$(dirname ${test_kafka_grand_parent_path})
readonly test_kafka_great_great_grant_parent_path=$(dirname ${test_kafka_great_grand_parent_path})

. ${test_kafka_great_grand_parent_path}/util/usage.sh
. ${test_kafka_great_grand_parent_path}/util/setup-test-env.sh ${INPUT_DIR} ${OUTPUT_DIR}

function print_debug_info() {
    echo "Host And Port: ${external_ip}:${node_port}"
}

function run_tests() {
    local external_ip=${deployment_config["ExternalIP"]}
    local node_port=${deployment_config["NodePort"]}

    local is_debug_enabled=${deployment_config["isDebugEnabled"]}
    if [ "${is_debug_enabled}" = "true" ]; then
        print_debug_info
    fi

    declare -A sys_prop_array
    sys_prop_array["messaging.with.kafka.service.host"]=${external_ip}
    sys_prop_array["messaging.with.kafka.service.port"]=${node_port}

    # Builds and run tests of the given BBG section and copies resulting surefire reports to output directory
    run_bbg_section_tests bbg-kafka kafka sys_prop_array ${INPUT_DIR} ${OUTPUT_DIR}
}

run_tests
