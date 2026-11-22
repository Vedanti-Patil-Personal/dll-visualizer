package com.example.dllvisualizer;

public class NodeStateDto {
    private final int id;
    private final int value;
    private final Integer prevId;
    private final Integer nextId;

    public NodeStateDto(int id, int value, Integer prevId, Integer nextId) {
        this.id = id;
        this.value = value;
        this.prevId = prevId;
        this.nextId = nextId;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public Integer getPrevId() {
        return prevId;
    }

    public Integer getNextId() {
        return nextId;
    }
}