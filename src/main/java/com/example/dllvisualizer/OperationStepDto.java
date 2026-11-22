package com.example.dllvisualizer;

import java.util.List;

public class OperationStepDto {
    private final int stepNumber;
    private final String actionType;
    private final String description;
    private final List<NodeStateDto> nodes;
    private final Integer headId;
    private final Integer tailId;
    private final Integer currentId;
    private final Integer highlightedNodeId;

    public OperationStepDto(
            int stepNumber,
            String actionType,
            String description,
            List<NodeStateDto> nodes,
            Integer headId,
            Integer tailId,
            Integer currentId,
            Integer highlightedNodeId
    ) {
        this.stepNumber = stepNumber;
        this.actionType = actionType;
        this.description = description;
        this.nodes = nodes;
        this.headId = headId;
        this.tailId = tailId;
        this.currentId = currentId;
        this.highlightedNodeId = highlightedNodeId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getActionType() {
        return actionType;
    }

    public String getDescription() {
        return description;
    }

    public List<NodeStateDto> getNodes() {
        return nodes;
    }

    public Integer getHeadId() {
        return headId;
    }

    public Integer getTailId() {
        return tailId;
    }

    public Integer getCurrentId() {
        return currentId;
    }

    public Integer getHighlightedNodeId() {
        return highlightedNodeId;
    }
}
