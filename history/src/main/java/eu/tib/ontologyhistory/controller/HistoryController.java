package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.model.History;
import eu.tib.ontologyhistory.model.TreeNode;
import eu.tib.ontologyhistory.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

public class HistoryController {

    private final HistoryService historyService;

    private HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    private List<History> getDiffs() {
        return historyService.findAll();
    }

    @GetMapping("/history/{treeId}")
    private ResponseEntity<TreeNode> getFullTree(@PathVariable("treeId") String treeId) {
        return ResponseEntity.ok(historyService.getFullTree(treeId));
    }
}
