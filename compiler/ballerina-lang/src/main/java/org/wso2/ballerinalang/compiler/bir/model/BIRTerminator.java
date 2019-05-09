/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.model;

import org.ballerinalang.model.Name;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

/**
 * Terminators connects basic blocks together.
 * <p>
 * Each terminating instruction terminates a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRTerminator extends BIRNode implements BIRInstruction {

    public InstructionKind kind;

    public BIRTerminator(DiagnosticPos pos, InstructionKind kind) {
        super(pos);
        this.kind = kind;
    }

    /**
     * A goto instruction.
     * <p>
     * e.g., goto BB2
     *
     * @since 0.980.0
     */
    public static class GOTO extends BIRTerminator {

        public BIRBasicBlock targetBB;

        public GOTO(DiagnosticPos pos, BIRBasicBlock targetBB) {
            super(pos, InstructionKind.GOTO);
            this.targetBB = targetBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A function call instruction.
     * <p>
     * e.g., _4 = call doSomething _1 _2 _3
     *
     * @since 0.980.0
     */
    public static class Call extends BIRTerminator implements BIRAssignInstruction {
        public BIROperand lhsOp;
        public boolean isVirtual;
        public List<BIROperand> args;
        public BIRBasicBlock thenBB;
        public Name name;
        public PackageID calleePkg;

        public Call(DiagnosticPos pos,
                    InstructionKind kind,
                    boolean isVirtual,
                    PackageID calleePkg,
                    Name name,
                    List<BIROperand> args,
                    BIROperand lhsOp,
                    BIRBasicBlock thenBB) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.isVirtual = isVirtual;
            this.args = args;
            this.thenBB = thenBB;
            this.name = name;
            this.calleePkg = calleePkg;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A async function call instruction.
     * <p>
     * e.g., _4 = callAsync doSomething _1 _2 _3
     *
     * @since 0.995.0
     */
    public static class AsyncCall extends Call {

        public AsyncCall(DiagnosticPos pos,
                         InstructionKind kind,
                         boolean isVirtual,
                         PackageID calleePkg,
                         Name name,
                         List<BIROperand> args,
                         BIROperand lhsOp,
                         BIRBasicBlock thenBB) {
            super(pos, kind, isVirtual, calleePkg, name, args, lhsOp, thenBB);
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A function pointer invocation instruction.
     * <p>
     * e.g., _4 = fp.call();
     *
     * @since 0.995.0
     */
    public static class FPCall extends BIRTerminator {
        public BIROperand fp;
        public BIROperand lhsOp;
        public List<BIROperand> args;
        public BIRBasicBlock thenBB;
        public boolean isAsync;

        public FPCall(DiagnosticPos pos,
                      InstructionKind kind,
                      BIROperand fp,
                      List<BIROperand> args,
                      BIROperand lhsOp,
                      boolean isAsync,
                      BIRBasicBlock thenBB) {
            super(pos, kind);
            this.fp = fp;
            this.lhsOp = lhsOp;
            this.args = args;
            this.isAsync = isAsync;
            this.thenBB = thenBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A return instruction.
     * <p>
     * e.g., return _4
     *
     * @since 0.980.0
     */
    public static class Return extends BIRTerminator {

        public Return(DiagnosticPos pos) {
            super(pos, InstructionKind.RETURN);
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A branch instruction.
     * <p>
     * e.g., branch %4 [true:bb4, false:bb6]
     *
     * @since 0.980.0
     */
    public static class Branch extends BIRTerminator {
        public BIROperand op;
        public BIRBasicBlock trueBB;
        public BIRBasicBlock falseBB;

        public Branch(DiagnosticPos pos, BIROperand op, BIRBasicBlock trueBB, BIRBasicBlock falseBB) {
            super(pos, InstructionKind.BRANCH);
            this.op = op;
            this.trueBB = trueBB;
            this.falseBB = falseBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A panic statement.
     * <p>
     * panic error
     *
     * @since 0.995.0
     */
    public static class Panic extends BIRTerminator {

        public BIROperand errorOp;

        public Panic(DiagnosticPos pos, BIROperand errorOp) {
            super(pos, InstructionKind.PANIC);
            this.errorOp = errorOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A wait instruction.
     * <p>
     * e.g., wait w1|w2;
     *
     * @since 0.995.0
     */
    public static class Wait extends BIRTerminator {
        public List<BIROperand> exprList;
        public BIROperand lhsOp;

        public Wait(DiagnosticPos pos, List<BIROperand> exprList, BIROperand lhsOp) {
            super(pos, InstructionKind.WAIT);
            this.exprList = exprList;
            this.lhsOp = lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }
}
