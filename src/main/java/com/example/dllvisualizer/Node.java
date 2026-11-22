package com.example.dllvisualizer;

public class Node {
    private final int id;
    private int data;
    private Node next;
    private Node prev;

    public Node(int id, int data) {
        this.id = id;
        this.data = data;
        this.next = null;
        this.prev = null;
    }

    public int getId() {
        return id;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }
}