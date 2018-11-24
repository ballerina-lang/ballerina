/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the ''License''); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * ''AS IS'' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import * as React from "react";
import ToolBar from './ToolBar';
import TraceList from './TraceList';

export interface TraceLogsState {
    filteredTraces: Array<any>;
    selected?: string;
}

export interface TraceLogsProps {
    filters: any;
    clearLogs: Function;
    traces: Array<any>;
    onSelected: Function;
}

export default class TraceLogs extends React.Component<TraceLogsProps, TraceLogsState> {
    static defaultProps = {
        traces : [],
        onSelected: ()=>{},
    }
    constructor(props: any, context: any) {
        super(props);
        this.onFilteredTraces = this.onFilteredTraces.bind(this);
        this.onSelected = this.onSelected.bind(this);
        this.state = {
            filteredTraces: props.traces,
        };
    }

    onSelected(selected: any) {
        if (selected.id === this.state.selected) {
            this.setState({
                selected: undefined
            });
            this.props.onSelected(undefined);
        } else {
            this.setState({
                selected: selected.id
            });
            this.props.onSelected(selected);
        }
    }

    componentWillReceiveProps(nextProps: any) {
        this.setState({
            filteredTraces: nextProps.traces,
        })
    }

    onFilteredTraces(filtered: any) {
        this.setState({
            filteredTraces: filtered,
        });
    }
    render() {
        const { traces } = this.props;
        
        return (
            <div>
                <div>
                    <ToolBar
                        traces={traces} 
                        filters={this.props.filters} 
                        clearLogs={this.props.clearLogs}
                        onFilteredTraces={this.onFilteredTraces} 
                    />
                </div>
                <div>
                    <TraceList traces={this.state.filteredTraces} selected={this.state.selected} onSelected={this.onSelected} />
                </div>
            </div>
        );
    }
}
