package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.History;

import java.util.List;

public interface NodeGraphLookupRepository {

    List<History> getSubTree(String treeId, int nodeId);
}
