package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.History;
import eu.tib.ontologyhistory.model.TreeNode;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface HistoryService {

    List<History> findAll();
    TreeNode getFullTree(String treeId);

    History getSubTree(String treeId, String node);

    static TreeNode assembleTree(final List<TreeNode> nodes, final String rootId) {
        final Map<String, TreeNode> map = new LinkedHashMap<>();
        for (final TreeNode node : nodes) {
            map.put(node.getName(), node);
        }

        for (final TreeNode node : nodes) {
            final List<String> parents = node.getParent();

            if (!CollectionUtils.isEmpty(parents)) {
                for (final String parentId : parents) {
                    final TreeNode parent = map.get(parentId);
                    if (parent != null) {
                        parent.addChild(node);
                        node.addParent(parent);
                    }
                }
            }
        }

        return map.get(rootId);
    }
}
