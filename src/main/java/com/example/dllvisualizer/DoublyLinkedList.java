package com.example.dllvisualizer;

import java.util.ArrayList;
import java.util.List;

public class DoublyLinkedList {
    private Node head;
    private Node tail;
    private int size;
    private int nextNodeId = 1;

    public List<NodeStateDto> getSnapshotNodes(Node extraNode) {
        List<NodeStateDto> snapshot = new ArrayList<>();
        Node temp = head;
        while (temp != null) {
            Integer prevId = (temp.getPrev() != null) ? temp.getPrev().getId() : null;
            Integer nextId = (temp.getNext() != null) ? temp.getNext().getId() : null;
            snapshot.add(new NodeStateDto(temp.getId(), temp.getData(), prevId, nextId));
            temp = temp.getNext();
        }

        if (extraNode != null) {
            Integer prevId = (extraNode.getPrev() != null) ? extraNode.getPrev().getId() : null;
            Integer nextId = (extraNode.getNext() != null) ? extraNode.getNext().getId() : null;
            snapshot.add(new NodeStateDto(extraNode.getId(), extraNode.getData(), prevId, nextId));
        }
        return snapshot;
    }

    private void recordStep(
            List<OperationStepDto> steps,
            String actionType,
            String description,
            Node current,
            Node highlighted,
            Node extraNode
    ) {
        Integer headId = (head != null) ? head.getId() : null;
        Integer tailId = (tail != null) ? tail.getId() : null;
        Integer currentId = (current != null) ? current.getId() : null;
        Integer highlightedId = (highlighted != null) ? highlighted.getId() : null;

        steps.add(new OperationStepDto(
                steps.size() + 1,
                actionType,
                description,
                getSnapshotNodes(extraNode),
                headId,
                tailId,
                currentId,
                highlightedId
        ));
    }

    public List<OperationStepDto> insertAtHead(int data) {
        List<OperationStepDto> steps = new ArrayList<>();
        Node newNode = new Node(nextNodeId++, data);

        recordStep(steps, "CREATE_NODE", "Created new node with value " + data, newNode, newNode, newNode);

        if (head == null) {
            head = tail = newNode;
            size++;
            recordStep(steps, "SET_HEAD_TAIL", "List was empty; set both head and tail to node " + data, newNode, newNode, null);
        } else {
            newNode.setNext(head);
            recordStep(steps, "LINK_NEXT", "Pointed new node's next to current head (" + head.getData() + ")", head, newNode, newNode);

            head.setPrev(newNode);
            recordStep(steps, "LINK_PREV", "Pointed current head's prev to new node (" + data + ")", head, newNode, newNode);

            head = newNode;
            size++;
            recordStep(steps, "MOVE_HEAD", "Moved head pointer to new node (" + data + ")", head, newNode, null);
        }
        return steps;
    }

    public List<OperationStepDto> insertAtTail(int data) {
        List<OperationStepDto> steps = new ArrayList<>();
        Node newNode = new Node(nextNodeId++, data);

        recordStep(steps, "CREATE_NODE", "Created new node with value " + data, newNode, newNode, newNode);

        if (head == null) {
            head = tail = newNode;
            size++;
            recordStep(steps, "SET_HEAD_TAIL", "List was empty; set both head and tail to node " + data, newNode, newNode, null);
        } else {
            tail.setNext(newNode);
            recordStep(steps, "LINK_NEXT", "Pointed current tail's next to new node (" + data + ")", tail, newNode, newNode);

            newNode.setPrev(tail);
            recordStep(steps, "LINK_PREV", "Pointed new node's prev to current tail (" + tail.getData() + ")", tail, newNode, newNode);

            tail = newNode;
            size++;
            recordStep(steps, "MOVE_TAIL", "Moved tail pointer to new node (" + data + ")", tail, newNode, null);
        }
        return steps;
    }

    // 1-based position (1 = head, size + 1 = tail)
    public List<OperationStepDto> insertAtPosition(int data, int position) {
        if (position < 1 || position > size + 1) {
            return null;
        }
        if (position == 1) {
            return insertAtHead(data);
        }
        if (position == size + 1) {
            return insertAtTail(data);
        }

        List<OperationStepDto> steps = new ArrayList<>();
        Node newNode = new Node(nextNodeId++, data);
        recordStep(steps, "CREATE_NODE", "Created new node with value " + data + " for position " + position, head, newNode, newNode);

        Node temp = head;
        recordStep(steps, "TRAVERSE_START", "Started traversal at head (" + temp.getData() + ")", temp, temp, newNode);

        for (int i = 1; i < position - 1; i++) {
            temp = temp.getNext();
            recordStep(steps, "TRAVERSE_STEP", "Moved pointer to node (" + temp.getData() + ") at index " + (i + 1), temp, temp, newNode);
        }

        Node nextNode = temp.getNext();
        newNode.setNext(nextNode);
        newNode.setPrev(temp);
        recordStep(steps, "PREPARE_LINKS", "Linked new node (" + data + ") between (" + temp.getData() + ") and (" + nextNode.getData() + ")", temp, newNode, newNode);

        temp.setNext(newNode);
        nextNode.setPrev(newNode);
        size++;
        recordStep(steps, "INSERT_COMPLETE", "Updated pointers and inserted node (" + data + ") at position " + position, temp, newNode, null);

        return steps;
    }

    public List<OperationStepDto> deleteNode(int data) {
        if (head == null) {
            return null;
        }

        List<OperationStepDto> steps = new ArrayList<>();
        Node temp = head;
        recordStep(steps, "SEARCH_START", "Searching for value " + data + " starting from head", temp, temp, null);

        while (temp != null && temp.getData() != data) {
            temp = temp.getNext();
            if (temp != null) {
                recordStep(steps, "SEARCH_STEP", "Checking node with value " + temp.getData(), temp, temp, null);
            }
        }

        if (temp == null) {
            return null;
        }

        recordStep(steps, "NODE_FOUND", "Found target node with value " + data, temp, temp, null);

        if (temp == head && temp == tail) {
            head = tail = null;
        } else if (temp == head) {
            head = head.getNext();
            head.setPrev(null);
        } else if (temp == tail) {
            tail = tail.getPrev();
            tail.setNext(null);
        } else {
            temp.getPrev().setNext(temp.getNext());
            temp.getNext().setPrev(temp.getPrev());
        }

        size--;
        recordStep(steps, "DELETE_COMPLETE", "Removed node with value " + data + " and reconnected pointers", head, null, null);
        return steps;
    }

    public List<OperationStepDto> clear() {
        List<OperationStepDto> steps = new ArrayList<>();
        head = null;
        tail = null;
        size = 0;
        recordStep(steps, "CLEAR_LIST", "Cleared all nodes and reset head and tail to null", null, null, null);
        return steps;
    }

    public int size() {
        return size;
    }

    public List<Integer> getForwardList() {
        List<Integer> list = new ArrayList<>();
        Node temp = head;
        while (temp != null) {
            list.add(temp.getData());
            temp = temp.getNext();
        }
        return list;
    }

    public List<Integer> getBackwardList() {
        List<Integer> list = new ArrayList<>();
        Node temp = tail;
        while (temp != null) {
            list.add(temp.getData());
            temp = temp.getPrev();
        }
        return list;
    }
}