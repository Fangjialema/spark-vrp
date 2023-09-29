package org.fangjialema.RTree;

import java.util.ArrayList;
import java.util.List;

public class  RTreeNode {
    List<RTreeNode> children;
    Rectangle boundary;

    public RTreeNode(Rectangle boundary) {
        this.boundary = boundary;
        this.children = new ArrayList<>();
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}
