package com.example.dllvisualizer;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dll")
@CrossOrigin(origins = "*") 
public class DllController {

    private final DoublyLinkedList dll = new DoublyLinkedList();

    
    @GetMapping("/get")
    public List<Integer> getList() {
        return dll.getForwardList();
    }

    
    @GetMapping("/get-backward")
    public List<Integer> getBackwardList() {
        return dll.getBackwardList();
    }

    
    @PostMapping("/insert-head")
    public String insertHead(@RequestParam int data) {
        dll.insertAtHead(data);
        return "Inserted " + data + " at Head successfully!";
    }

   
    @PostMapping("/insert-tail")
    public String insertTail(@RequestParam int data) {
        dll.insertAtTail(data);
        return "Inserted " + data + " at Tail successfully!";
    }

   
    @PostMapping("/insert-position")
    public String insertPosition(@RequestParam int data, @RequestParam int position) {
        dll.insertAtPosition(data, position);
        return "Inserted " + data + " at position " + position + " successfully!";
    }

    
    @DeleteMapping("/delete")
    public String deleteNode(@RequestParam int data) {
        boolean deleted = dll.deleteNode(data);
        return deleted ? "Deleted node with value: " + data : "Node not found!";
    }
}