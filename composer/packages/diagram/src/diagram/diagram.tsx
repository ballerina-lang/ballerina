import { CompilationUnit, traversNode } from "@ballerina/ast-model";
import React from "react";
import { DefaultConfig } from "../config/default";
import { CompilationUnitViewState } from "../view-model/index";
import { SvgCanvas } from "../views";
import { visitor as initVisitor } from "../visitors/init-visitor";
import { visitor as positioningVisitor } from "../visitors/positioning-visitor";
import { visitor as sizingVisitor } from "../visitors/sizing-visitor";
import { ControllerPanel } from "./controllers/controller-panel";
import { DiagramContext, DiagramMode, IDiagramContext } from "./diagram-context";
import { DiagramErrorBoundary } from "./diagram-error-boundary";
import { DiagramUtils } from "./diagram-utils";

export interface CommonDiagramProps {
    height?: number;
    width?: number;
    zoom: number;
    mode: DiagramMode;
}
export interface DiagramProps extends CommonDiagramProps {
    ast?: CompilationUnit;
}

export interface DiagramState {
    currentMode: DiagramMode;
}

export class Diagram extends React.Component<DiagramProps, DiagramState> {

    // get default context or provided context from a parent (if any)
    public static contextType = DiagramContext;

    public state = {
        currentMode: DiagramMode.ACTION,
    };

    private containerRef = React.createRef<HTMLDivElement>();

    public render() {
        const { ast, width, height } = this.props;
        const children: React.ReactNode[] = [];
        const cuViewState: CompilationUnitViewState = new CompilationUnitViewState();
        cuViewState.container.w = width ? width : DefaultConfig.canvas.width;
        cuViewState.container.h = height ? height : DefaultConfig.canvas.height;

        if (ast) {
            // Initialize AST node view state
            traversNode(ast, initVisitor);
            // Set width and height to toplevel node.
            ast.viewState = cuViewState;
            // Calculate dimention of AST Nodes.
            traversNode(ast, sizingVisitor);
            // Calculate positions of the AST Nodes.
            traversNode(ast, positioningVisitor);
            // Get React components for AST Nodes.
            children.push(DiagramUtils.getComponents(ast.topLevelNodes));
        }

        return <DiagramContext.Provider value={this.createContext()}>
            <DiagramErrorBoundary>
                <div className="diagram-container" ref={this.containerRef}>
                    <ControllerPanel stickTo={this.containerRef} />
                    <SvgCanvas model={cuViewState}>
                        {children}
                    </SvgCanvas>
                </div>
            </DiagramErrorBoundary>
        </DiagramContext.Provider>;
    }

    private createContext(): IDiagramContext {
        const { ast } = this.props;
        const { currentMode } = this.state;
        // create context contributions
        const contextContributions = {
            ast,
            changeMode: (newMode: DiagramMode) => {
                this.setState({
                    currentMode: newMode,
                });
            },
            mode: currentMode,
        };

        // merge with parent (if any) or with default context
        return { ...this.context, ...contextContributions };
    }
}
