package eu.tib.ontologyhistory.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TreeNode implements Serializable {

    @Id
    private String name;

    private String treeId;

    private String commitSha1;

    private Attributes attributes;

    private List<String> parent;

    private List<TreeNode> children;

    @JsonIgnore
    private List<TreeNode> parents;

    public TreeNode() {
        this.parent = new ArrayList<>();
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();
    }

    public void addChild(TreeNode child) {
        if (this.children != null && !this.children.contains(child) && child != null)
            this.children.add(child);
    }

    public void addParent(TreeNode parent) {
        if (this.parents != null && !this.parents.contains(parent) && parent != null)
            this.parents.add(parent);
    }

}
