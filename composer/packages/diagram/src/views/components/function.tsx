import { Function as FunctionNode } from "@ballerina/ast-model";
import * as React from "react";
import { FunctionViewState } from "../../view-model/index";
import { AddWorkerOrEndpointMenu } from "./add-worker-or-endpoint-menu";
import { Block } from "./block";
import { LifeLine } from "./life-line";
import { Panel } from "./panel";

export const Function = (props: {model: FunctionNode}) => {
    const { model } = props;
    const viewState: FunctionViewState = model.viewState;

    return (
        <Panel model={viewState} title={model.name.value} icon="function">
            <LifeLine title="Client" icon="client" model={viewState.client}/>
            <LifeLine title="Default" icon="worker" model={viewState.defaultWorker}/>
            { model.body && <Block model={model.body} />}
            <AddWorkerOrEndpointMenu
                triggerPosition={viewState.menuTrigger}
                onAddEndpoint={(epDef: any) => {
                    // todo
                }}
                onAddWorker={() => {
                    // todo
                }}
            />
        </Panel>);
};
