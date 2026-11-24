package com.example.dllvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DoublyLinkedList {
    public static final int MAX_NODES = 60;

    private Node head;
    private Node tail;
    private int size;
    private int nextNodeId = 1;

    public List<NodeStateDto> getSnapshotNodes(Node extraNode) {
        Map<Integer, NodeStateDto> uniqueNodes = new LinkedHashMap<>();
        Node temp = head;
        while (temp != null) {
            Integer prevId = (temp.getPrev() != null) ? temp.getPrev().getId() : null;
            Integer nextId = (temp.getNext() != null) ? temp.getNext().getId() : null;
            uniqueNodes.put(temp.getId(), new NodeStateDto(temp.getId(), temp.getData(), prevId, nextId));
            temp = temp.getNext();
        }

        if (extraNode != null && !uniqueNodes.containsKey(extraNode.getId())) {
            Integer prevId = (extraNode.getPrev() != null) ? extraNode.getPrev().getId() : null;
            Integer nextId = (extraNode.getNext() != null) ? extraNode.getNext().getId() : null;
            uniqueNodes.put(extraNode.getId(), new NodeStateDto(extraNode.getId(), extraNode.getData(), prevId, nextId));
        }
        return new ArrayList<>(uniqueNodes.values());
    }

    private void recordStep(
            List<OperationStepDto> steps,
            String actionType,
            String description,
            Node current,
            Node highlighted,
            Node extraNode,
            Integer algorithmLine
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
                highlightedId,
                algorithmLine
        ));
    }

    private List<OperationStepDto> recordOverflowAndExit(List<OperationStepDto> steps, int data) {
        recordStep(
                steps,
                "OVERFLOW",
                "[OVERFLOW?] AVAIL = NULL (pool capacity " + MAX_NODES + " reached). Cannot insert " + data + "; write OVERFLOW and exit.",
                head,
                null,
                null,
                1
        );
        recordStep(steps, "EXIT", "OVERFLOW occurred. Exit without inserting " + data + ".", head, null, null, 4);
        return steps;
    }

    public List<OperationStepDto> insertAtHead(int data) {
        List<OperationStepDto> steps = new ArrayList<>();

        if (size >= MAX_NODES) {
            return recordOverflowAndExit(steps, data);
        }

        // 1. [OVERFLOW?]
        recordStep(steps, "OVERFLOW_CHECK", "[OVERFLOW?] Check AVAIL != NULL. Free node available in AVAIL pool.", head, null, null, 1);

        // 2. [Remove from AVAIL & copy data]
        Node newNode = new Node(nextNodeId++, data);
        recordStep(steps, "CREATE_NODE", "[Remove from AVAIL] Set NEW := AVAIL, AVAIL := FORW[AVAIL], INFO[NEW] := " + data, newNode, newNode, newNode, 2);

        // 3. [Insert into list]
        if (head == null) {
            head = tail = newNode;
            size++;
            recordStep(steps, "SET_HEAD_TAIL", "[Insert into list] List empty; set HEAD := NEW (" + data + ") and TAIL := NEW (" + data + ")", newNode, newNode, null, 3);
        } else {
            newNode.setNext(head);
            recordStep(steps, "LINK_NEXT", "[Insert into list] Set FORW[NEW] := HEAD (#" + head.getId() + ")", head, newNode, newNode, 3);

            head.setPrev(newNode);
            recordStep(steps, "LINK_PREV", "[Insert into list] Set BACK[HEAD] := NEW (#" + newNode.getId() + ")", head, newNode, newNode, 3);

            head = newNode;
            size++;
            recordStep(steps, "MOVE_HEAD", "[Insert into list] Update HEAD := NEW (#" + newNode.getId() + ")", head, newNode, null, 3);
        }

        // 4. Exit
        recordStep(steps, "EXIT", "Insertion complete. Exit.", head, null, null, 4);
        return steps;
    }

    public List<OperationStepDto> insertAtTail(int data) {
        List<OperationStepDto> steps = new ArrayList<>();

        if (size >= MAX_NODES) {
            return recordOverflowAndExit(steps, data);
        }

        recordStep(steps, "OVERFLOW_CHECK", "[OVERFLOW?] Check AVAIL != NULL. Free node available in AVAIL pool.", tail, null, null, 1);

        Node newNode = new Node(nextNodeId++, data);
        recordStep(steps, "CREATE_NODE", "[Remove from AVAIL] Set NEW := AVAIL, AVAIL := FORW[AVAIL], INFO[NEW] := " + data, newNode, newNode, newNode, 2);

        if (head == null) {
            head = tail = newNode;
            size++;
            recordStep(steps, "SET_HEAD_TAIL", "[Insert into list] List empty; set HEAD := NEW (" + data + ") and TAIL := NEW (" + data + ")", newNode, newNode, null, 3);
        } else {
            tail.setNext(newNode);
            recordStep(steps, "LINK_NEXT", "[Insert into list] Set FORW[TAIL] := NEW (#" + newNode.getId() + ")", tail, newNode, newNode, 3);

            newNode.setPrev(tail);
            recordStep(steps, "LINK_PREV", "[Insert into list] Set BACK[NEW] := TAIL (#" + tail.getId() + ")", tail, newNode, newNode, 3);

            tail = newNode;
            size++;
            recordStep(steps, "MOVE_TAIL", "[Insert into list] Update TAIL := NEW (#" + newNode.getId() + ")", tail, newNode, null, 3);
        }

        recordStep(steps, "EXIT", "Insertion complete. Exit.", tail, null, null, 4);
        return steps;
    }

    public List<OperationStepDto> insertAtPosition(int data, int position) {
        if (position < 1 || position > size + 1) {
            return null;
        }
        if (size >= MAX_NODES) {
            List<OperationStepDto> steps = new ArrayList<>();
            return recordOverflowAndExit(steps, data);
        }
        if (position == 1) {
            return insertAtHead(data);
        }
        if (position == size + 1) {
            return insertAtTail(data);
        }

        List<OperationStepDto> steps = new ArrayList<>();

        recordStep(steps, "OVERFLOW_CHECK", "[OVERFLOW?] Check AVAIL != NULL. Free node available for position " + position, head, null, null, 1);

        Node newNode = new Node(nextNodeId++, data);
        recordStep(steps, "CREATE_NODE", "[Remove from AVAIL] Set NEW := AVAIL, AVAIL := FORW[AVAIL], INFO[NEW] := " + data, head, newNode, newNode, 2);

        Node temp = head;
        recordStep(steps, "TRAVERSE_START", "[Locate LOCA] Set CURR := HEAD (#" + temp.getId() + ")", temp, temp, newNode, 3);

        for (int i = 1; i < position - 1; i++) {
            temp = temp.getNext();
            recordStep(steps, "TRAVERSE_STEP", "[Locate LOCA] Advance CURR := FORW[CURR] -> node #" + temp.getId() + " (" + temp.getData() + ")", temp, temp, newNode, 3);
        }

        Node nextNode = temp.getNext();
        newNode.setNext(nextNode);
        recordStep(steps, "LINK_NEXT", "[Insert into list] Set FORW[NEW] := LOCB (#" + nextNode.getId() + ")", temp, newNode, newNode, 3);

        newNode.setPrev(temp);
        recordStep(steps, "LINK_PREV", "[Insert into list] Set BACK[NEW] := LOCA (#" + temp.getId() + ")", temp, newNode, newNode, 3);

        temp.setNext(newNode);
        nextNode.setPrev(newNode);
        size++;
        recordStep(steps, "INSERT_COMPLETE", "[Insert into list] Set FORW[LOCA] := NEW and BACK[LOCB] := NEW at position " + position, temp, newNode, null, 3);

        recordStep(steps, "EXIT", "Insertion complete. Exit.", newNode, null, null, 4);
        return steps;
    }

    public List<OperationStepDto> deleteNode(int data) {
        if (head == null) {
            return null;
        }

        List<OperationStepDto> steps = new ArrayList<>();
        Node temp = head;
        recordStep(steps, "SEARCH_START", "Set CURR := HEAD (#" + temp.getId() + ") to search for LOC where INFO[LOC] = " + data, temp, temp, null, 1);

        while (temp != null && temp.getData() != data) {
            temp = temp.getNext();
            if (temp != null) {
                recordStep(steps, "SEARCH_STEP", "Advance CURR := FORW[CURR] -> node #" + temp.getId() + " (INFO = " + temp.getData() + ")", temp, temp, null, 1);
            }
        }

        if (temp == null) {
            return null;
        }

        recordStep(steps, "NODE_FOUND", "Found target node LOC := #" + temp.getId() + " (INFO = " + data + ")", temp, temp, null, 1);

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
        recordStep(steps, "DELETE_COMPLETE", "[Delete node] Unlink LOC (#" + temp.getId() + "): FORW[BACK[LOC]] := FORW[LOC] and BACK[FORW[LOC]] := BACK[LOC]", head, temp, null, 1);

        recordStep(steps, "RETURN_AVAIL", "[Return node to AVAIL list] Set FORW[LOC] := AVAIL and AVAIL := LOC (#" + temp.getId() + ")", head, temp, null, 2);

        recordStep(steps, "EXIT", "Deletion complete. Exit.", head, null, null, 3);
        return steps;
    }

    public List<OperationStepDto> traverseForwardSteps() {
        List<OperationStepDto> steps = new ArrayList<>();
        recordStep(steps, "TRAVERSE_INIT", "1. Set CURR := HEAD", head, head, null, 1);

        Node curr = head;
        while (curr != null) {
            recordStep(steps, "TRAVERSE_CHECK", "2. Check CURR != NULL (CURR = #" + curr.getId() + ")", curr, curr, null, 2);
            recordStep(steps, "PROCESS_NODE", "3. Apply PROCESS to INFO[CURR] = " + curr.getData(), curr, curr, null, 3);
            Node next = curr.getNext();
            recordStep(steps, "TRAVERSE_ADVANCE", "4. Set CURR := FORW[CURR]" + (next != null ? " -> #" + next.getId() : " -> NULL"), next, curr, null, 4);
            curr = next;
        }

        recordStep(steps, "TRAVERSE_END", "2. CURR = NULL; forward traversal finished. Exit.", null, null, null, 5);
        return steps;
    }

    public List<OperationStepDto> traverseBackwardSteps() {
        List<OperationStepDto> steps = new ArrayList<>();
        recordStep(steps, "TRAVERSE_INIT", "1. Set CURR := TAIL", tail, tail, null, 1);

        Node curr = tail;
        while (curr != null) {
            recordStep(steps, "TRAVERSE_CHECK", "2. Check CURR != NULL (CURR = #" + curr.getId() + ")", curr, curr, null, 2);
            recordStep(steps, "PROCESS_NODE", "3. Apply PROCESS to INFO[CURR] = " + curr.getData(), curr, curr, null, 3);
            Node prev = curr.getPrev();
            recordStep(steps, "TRAVERSE_ADVANCE", "4. Set CURR := BACK[CURR]" + (prev != null ? " -> #" + prev.getId() : " -> NULL"), prev, curr, null, 4);
            curr = prev;
        }

        recordStep(steps, "TRAVERSE_END", "2. CURR = NULL; backward traversal finished. Exit.", null, null, null, 5);
        return steps;
    }

    public List<OperationStepDto> clear() {
        List<OperationStepDto> steps = new ArrayList<>();
        head = null;
        tail = null;
        size = 0;
        nextNodeId = 1;
        recordStep(steps, "CLEAR_LIST", "Cleared all nodes, reset HEAD and TAIL to NULL, and returned nodes to AVAIL.", null, null, null, 2);
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