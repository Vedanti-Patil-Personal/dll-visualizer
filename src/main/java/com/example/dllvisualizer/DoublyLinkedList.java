package com.example.dllvisualizer;

import java.util.ArrayList;
import java.util.List;

public class DoublyLinkedList {
    private Node head;
    private Node tail;

    public void insertAtHead(int data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
    }

    public void insertAtTail(int data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    public void insertAtPosition(int data, int position) {
        if (position <= 1) {
            insertAtHead(data);
            return;
        }
        Node temp = head;
        for (int i = 1; temp != null && i < position - 1; i++) {
            temp = temp.next;
        }
        if (temp == null || temp.next == null) {
            insertAtTail(data);
        } else {
            Node newNode = new Node(data);
            newNode.next = temp.next;
            newNode.prev = temp;
            temp.next.prev = newNode;
            temp.next = newNode;
        }
    }

    public boolean deleteNode(int data) {
        if (head == null) return false;

        Node temp = head;
        while (temp != null && temp.data != data) {
            temp = temp.next;
        }

        if (temp == null) return false; 

        if (temp == head) {
            head = head.next;
            if (head != null) head.prev = null;
            else tail = null;
        } else if (temp == tail) {
            tail = tail.prev;
            if (tail != null) tail.next = null;
            else head = null;
        } else {
            temp.prev.next = temp.next;
            temp.next.prev = temp.prev;
        }
        return true;
    }

    public List<Integer> getForwardList() {
        List<Integer> list = new ArrayList<>();
        Node temp = head;
        while (temp != null) {
            list.add(temp.data);
            temp = temp.next;
        }
        return list;
    }

    public List<Integer> getBackwardList() {
        List<Integer> list = new ArrayList<>();
        Node temp = tail;
        while (temp != null) {
            list.add(temp.data);
            temp = temp.prev;
        }
        return list;
    }
}