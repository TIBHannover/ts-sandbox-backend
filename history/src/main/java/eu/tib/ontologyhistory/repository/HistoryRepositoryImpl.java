package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.History;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GraphLookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HistoryRepositoryImpl implements NodeGraphLookupRepository {

    private final MongoTemplate mongoTemplate;

    public HistoryRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<History> getSubTree(String treeId, int nodeId) {
        final Criteria byNodeId = new Criteria("node").is(nodeId);
        final Criteria byTreeId = new Criteria("treeId").is(treeId);
        final MatchOperation matchOperation = Aggregation.match(byTreeId.andOperator(byNodeId));

        GraphLookupOperation graphLookupOperation = GraphLookupOperation.builder()
                .from("history")
                .startWith("$node")
                .connectFrom("node")
                .connectTo("parent")
                .restrict(new Criteria("treeId").is(treeId))
                .as("children");

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, graphLookupOperation);
        return mongoTemplate.aggregate(aggregation, "history", History.class).getMappedResults();
    }
}
