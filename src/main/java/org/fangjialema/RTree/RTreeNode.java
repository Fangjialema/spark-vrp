package org.fangjialema.RTree;

import java.util.ArrayList;
import java.util.List;

public class RTreeNode {
    List<RTreeNode> children;
    Rectangle boundary;

    int level;

    double area;

    boolean isLeaf;
    boolean isElement;

    public RTreeNode(Rectangle boundary) {
        this.boundary = boundary;
        area = boundary.calculateArea();
    }

    public RTreeNode() {
        this.children = new ArrayList<>();
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void regainBoundary() {
        double minX = children.stream().map(x -> x.boundary.minX).min((x, y) -> (int) (x - y)).orElse(-1.0);
        double maxX = children.stream().map(x -> x.boundary.maxX).min((x, y) -> (int) (x - y)).orElse(-1.0);
        double minY = children.stream().map(x -> x.boundary.minY).min((x, y) -> (int) (x - y)).orElse(-1.0);
        double maxY = children.stream().map(x -> x.boundary.maxY).min((x, y) -> (int) (x - y)).orElse(-1.0);
        boundary = new Rectangle(minX, minY, maxX, maxY);
    }
}
