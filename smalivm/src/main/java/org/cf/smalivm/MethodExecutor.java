package org.cf.smalivm;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayDeque;
import java.util.Deque;

import org.cf.smalivm.context.ExecutionGraph;
import org.cf.smalivm.context.ExecutionNode;
import org.cf.smalivm.exception.MaxCallDepthExceeded;
import org.cf.smalivm.exception.MaxNodeVisitsExceeded;
import org.cf.smalivm.opcode.Op;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodExecutor {

    private static Logger log = LoggerFactory.getLogger(MethodExecutor.class.getSimpleName());

    private final VirtualMachine vm;
    private final TIntIntMap addressToNodeVisitCounts;

    MethodExecutor(VirtualMachine vm) {
        this.vm = vm;
        addressToNodeVisitCounts = new TIntIntHashMap();
    }

    ExecutionGraph execute(ExecutionGraph graph) throws MaxNodeVisitsExceeded, MaxCallDepthExceeded {
        String methodDescriptor = graph.getMethodDescriptor();
        ExecutionNode currentNode = graph.getRoot();
        if (log.isDebugEnabled()) {
            log.info("Executing " + methodDescriptor + ", depth=" + currentNode.getCallDepth());
        }
        if (currentNode.getCallDepth() > vm.getMaxCallDepth()) {
            throw new MaxCallDepthExceeded(methodDescriptor);
        }

        Deque<ExecutionNode> executeStack = new ArrayDeque<ExecutionNode>();
        executeStack.push(currentNode);
        while ((currentNode = executeStack.poll()) != null) {
            checkMaxVisits(currentNode, methodDescriptor);

            int[] childAddresses = currentNode.execute();
            for (int address : childAddresses) {
                // Each visit adds a new ExecutionNode in the pile. Piles can be inspected later for register / field
                // consensus for optimization.
                Op childOp = graph.getTemplateNode(address).getOp();
                ExecutionNode childNode = currentNode.getChild(childOp);
                graph.addNode(childNode);
            }

            executeStack.addAll(currentNode.getChildren());
        }

        return graph;
    }

    private void checkMaxVisits(ExecutionNode node, String methodDescriptor) throws MaxNodeVisitsExceeded {
        int address = node.getAddress();
        int visitCount = addressToNodeVisitCounts.get(address);
        if (visitCount > vm.getMaxNodeVisits()) {
            throw new MaxNodeVisitsExceeded(node, methodDescriptor);
        }
        boolean adjusted = addressToNodeVisitCounts.adjustValue(address, 1);
        if (!adjusted) {
            addressToNodeVisitCounts.put(address, 1);
        }
    }

}