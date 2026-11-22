package com.example.dllvisualizer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dll")
@CrossOrigin(origins = "*")
public class DllController {

    private final DoublyLinkedList dll = new DoublyLinkedList();

    @GetMapping("/get")
    public ResponseEntity<List<Integer>> getList() {
        return ResponseEntity.ok(dll.getForwardList());
    }

    @GetMapping("/get-backward")
    public ResponseEntity<List<Integer>> getBackwardList() {
        return ResponseEntity.ok(dll.getBackwardList());
    }

    @GetMapping("/size")
    public ResponseEntity<Integer> getSize() {
        return ResponseEntity.ok(dll.size());
    }

    @GetMapping("/state")
    public ResponseEntity<List<NodeStateDto>> getCurrentState() {
        return ResponseEntity.ok(dll.getSnapshotNodes(null));
    }

    @PostMapping("/insert-head")
    public ResponseEntity<DllResponseDto> insertHead(@RequestParam int data) {
        List<OperationStepDto> steps = dll.insertAtHead(data);
        String msg = "Inserted " + data + " at Head successfully!";
        return ResponseEntity.ok(new DllResponseDto(true, msg, dll.size(), dll.getSnapshotNodes(null), steps));
    }

    @PostMapping("/insert-tail")
    public ResponseEntity<DllResponseDto> insertTail(@RequestParam int data) {
        List<OperationStepDto> steps = dll.insertAtTail(data);
        String msg = "Inserted " + data + " at Tail successfully!";
        return ResponseEntity.ok(new DllResponseDto(true, msg, dll.size(), dll.getSnapshotNodes(null), steps));
    }

    @PostMapping("/insert-position")
    public ResponseEntity<DllResponseDto> insertPosition(@RequestParam int data, @RequestParam int position) {
        List<OperationStepDto> steps = dll.insertAtPosition(data, position);
        if (steps == null) {
            String errMsg = "Invalid position: " + position + ". Valid range is 1 to " + (dll.size() + 1);
            return ResponseEntity.badRequest()
                    .body(new DllResponseDto(false, errMsg, dll.size(), dll.getSnapshotNodes(null), List.of()));
        }
        String msg = "Inserted " + data + " at position " + position + " successfully!";
        return ResponseEntity.ok(new DllResponseDto(true, msg, dll.size(), dll.getSnapshotNodes(null), steps));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DllResponseDto> deleteNode(@RequestParam int data) {
        List<OperationStepDto> steps = dll.deleteNode(data);
        if (steps == null) {
            String errMsg = "Node with value " + data + " not found!";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DllResponseDto(false, errMsg, dll.size(), dll.getSnapshotNodes(null), List.of()));
        }
        String msg = "Deleted node with value: " + data;
        return ResponseEntity.ok(new DllResponseDto(true, msg, dll.size(), dll.getSnapshotNodes(null), steps));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<DllResponseDto> clearList() {
        List<OperationStepDto> steps = dll.clear();
        return ResponseEntity.ok(
                new DllResponseDto(true, "Doubly Linked List cleared successfully!", dll.size(), dll.getSnapshotNodes(null), steps)
        );
    }
}