package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.Attributes;
import eu.tib.ontologyhistory.model.History;
import eu.tib.ontologyhistory.model.TreeNode;
import eu.tib.ontologyhistory.repository.HistoryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryServiceImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<History> findAll() {
        return historyRepository.findAll();
    }

    @Override
    public TreeNode getFullTree(String treeId) {
        List<History> history =  historyRepository.findByTreeId(treeId);
        List<TreeNode> nodes = new ArrayList<>();

        for (History node : history) {
            TreeNode treeNode = new TreeNode();
            Attributes attributes = new Attributes();
            attributes.setAttributeName(node.getAttributes());
            try {
                BeanUtils.copyProperties(node, treeNode, "id", "children");
            } catch (Exception e) {
                e.printStackTrace();
            }
            treeNode.setAttributes(attributes);
            nodes.add(treeNode);
        }

        return HistoryService.assembleTree(nodes, "-1");
    }

    @Override
    public History getSubTree(String treeId, String node) {
        return null;
    }
}
