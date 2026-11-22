package com.example.dllvisualizer;

import java.util.List;

public class DllResponseDto {
    private final boolean success;
    private final String message;
    private final int size;
    private final List<NodeStateDto> nodes;
    private final List<OperationStepDto> steps;

    public DllResponseDto(
            boolean success,
            String message,
            int size,
            List<NodeStateDto> nodes,
            List<OperationStepDto> steps
    ) {
        this.success = success;
        this.message = message;
        this.size = size;
        this.nodes = nodes;
        this.steps = steps;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getSize() {
        return size;
    }

    public List<NodeStateDto> getNodes() {
        return nodes;
    }

    public List<OperationStepDto> getSteps() {
        return steps;
    }
}
