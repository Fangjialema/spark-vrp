package org.fangjialema.RTree;

import java.util.ArrayList;
import java.util.List;

public class RTree {
    private final RTreeNode root;

    private final int maxChildren;

    public RTree(int maxChildren, Rectangle initRange) {
        this.maxChildren = maxChildren;
        root = new RTreeNode(initRange); // 初始根节点范围
    }

    public void insert(Rectangle rect) {
        insertRecursive(root, rect);
    }

    public List<Rectangle> query(Rectangle queryRect) {
        List<Rectangle> result = new ArrayList<>();
        queryRecursive(root, queryRect, result);
        return result;
    }

    public void delete(Rectangle rect) {
        deleteRecursive(root, rect);
    }

    private void insertRecursive(RTreeNode node, Rectangle rect) {
        if (node.isLeaf()) {
            if (node.children.size() < maxChildren) {
                node.children.add(new RTreeNode(rect));
            } else {
                node.children.add(new RTreeNode(rect));
                splitNode(node);
            }
        } else {
            RTreeNode bestChild = chooseBestChild(node, rect);
            insertRecursive(bestChild, rect);
        }
    }

    /*
     * 选择最佳的子节点，以最小化新节点的面积增加
     */
    private RTreeNode chooseBestChild(RTreeNode node, Rectangle rect) {
        double bestAreaIncrease = Double.POSITIVE_INFINITY;
        RTreeNode bestChild = null;
        for (RTreeNode child : node.children) {
            double areaIncrease = calculateAreaIncrease(child.boundary, rect);
            if (areaIncrease < bestAreaIncrease) {
                bestAreaIncrease = areaIncrease;
                bestChild = child;
            }
        }
        return bestChild;
    }


    private void queryRecursive(RTreeNode node, Rectangle queryRect, List<Rectangle> result) {
        if (node.boundary.intersects(queryRect)) {
            for (RTreeNode child : node.children) {
                if (child.boundary.intersects(queryRect)) {
                    if (child.isLeaf()) {
                        result.add(child.boundary);
                    } else {
                        queryRecursive(child, queryRect, result);
                    }
                }
            }
        }
    }

    private boolean deleteRecursive(RTreeNode node, Rectangle rect) {
        if (node.isLeaf()) {
            // 找到匹配的叶子节点并删除
            boolean deleted = node.children.removeIf(child -> child.boundary.equals(rect));
            if (deleted) {
                // 如果删除成功，更新当前节点的边界
                updateBoundary(node);
            }
            return deleted;
        } else {
            // 递归向下查找匹配的叶子节点
            boolean deleted = false;
            for (RTreeNode child : new ArrayList<>(node.children)) {
                if (child.boundary.intersects(rect) && deleteRecursive(child, rect)) {
                    // 如果子节点删除成功，更新当前节点的边界
                    updateBoundary(node);
                    deleted = true;
                }
            }
            // 合并可能需要的节点
            mergeNode(node);
            return deleted;
        }
    }

    /*
     * 如果子节点数量小于节点最小允许的数量，考虑节点的合并
     */
    private void mergeNode(RTreeNode node) {
        if (node.children.size() < maxChildren / 2) {
            RTreeNode parent = findParent(root, node);
            if (parent != null) {
                List<RTreeNode> pChildren = parent.children;
                if (pChildren.size() > 1) {
                    // 找到与当前节点有重叠的兄弟节点
                    RTreeNode nearestSibling = findNearest(node, pChildren);
                    if (nearestSibling != null) {
                        // 合并当前节点和最近的兄弟节点
                        node.children.addAll(nearestSibling.children);
                        pChildren.remove(nearestSibling);
                        updateBoundary(node);
                    }
                }
            }
        }
    }

    /*
     * 找到与当前节点有重叠且重叠最少的节点
     */
    private RTreeNode findNearest(RTreeNode node, List<RTreeNode> pChildren) {
        double minOverlap = Double.MAX_VALUE;
        RTreeNode nearest = null;
        for (RTreeNode child : pChildren) {
            if (child != node && child.boundary.intersects(node.boundary)) {
                double overlap = calculateOverlap(child.boundary, node.boundary);
                if (overlap < minOverlap) {
                    minOverlap = overlap;
                    nearest = child;
                }
            }
        }
        return nearest;
    }

    /*
     * 计算两个矩形的重叠面积
     */
    private double calculateOverlap(Rectangle rect1, Rectangle rect2) {
        double overlapX = Math.max(0, Math.min(rect1.maxX, rect2.maxX) - Math.max(rect1.minX, rect2.minX));
        double overlapY = Math.max(0, Math.min(rect1.maxY, rect2.maxY) - Math.max(rect1.minY, rect2.minY));
        return overlapX * overlapY;
    }

    /*
     * 更新节点的边界，以包含所有子节点的边界
     */
    private void updateBoundary(RTreeNode node) {
        if (!node.isLeaf()) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;
            for (RTreeNode child : node.children) {
                minX = Math.min(minX, child.boundary.minX);
                minY = Math.min(minY, child.boundary.minY);
                maxX = Math.max(maxX, child.boundary.maxX);
                maxY = Math.max(maxY, child.boundary.maxY);
            }
            node.boundary = new Rectangle(minX, minY, maxX, maxY);
        }
    }

    private RTreeNode findParent(RTreeNode root, RTreeNode child) {
        if (root.isLeaf() || root.children.contains(child)) {
            return root;
        } else {
            for (RTreeNode node : root.children) {
                RTreeNode parent = findParent(node, child);
                if (parent != null) {
                    return parent;
                }
            }
            return null;
        }
    }

    /*
     * 试图选择最佳的两个子节点组合到一个新节点中，以最小化拆分后的节点的总面积。它考虑了所有可能的组合，然后选择最佳组合
     */
    private void splitNode(RTreeNode node) {
        List<RTreeNode> children = node.children;
        // 初始化最佳拆分参数
        double bestOverlap = Double.POSITIVE_INFINITY;
        double bestAreaIncrease = Double.POSITIVE_INFINITY;
        RTreeNode bestChild1 = null;
        RTreeNode bestChild2 = null;
        // 尝试所有可能的子节点组合
        for (int i = 0, n = children.size(); i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                RTreeNode child1 = children.get(i);
                RTreeNode child2 = children.get(j);
                // 合并两个子节点的边界
                Rectangle combinedBoundary = Rectangle.combineRectangles(child1.boundary, child2.boundary);
                // 计算拆分后的重叠和面积增加
                double overlap = calculateOverlap(child1.boundary, child2.boundary);
                double areaIncrease = calculateAreaIncrease(combinedBoundary, node.boundary);
                // 如果找到更好的拆分方式，更新最佳参数
                if (overlap < bestOverlap || (overlap == bestOverlap && areaIncrease < bestAreaIncrease)) {
                    bestOverlap = overlap;
                    bestAreaIncrease = areaIncrease;
                    bestChild1 = child1;
                    bestChild2 = child2;
                }
            }
        }
        // 创建新节点并重新组织子节点
        if (bestChild1 != null) {
            children.remove(bestChild1);
            children.remove(bestChild2);
            RTreeNode newNode = new RTreeNode(Rectangle.combineRectangles(bestChild1.boundary, bestChild2.boundary));
            newNode.children.add(bestChild1);
            newNode.children.add(bestChild2);
            children.add(newNode);
            // 更新当前节点的边界
            updateBoundary(node);
        }
    }

    /*
     * 计算拆分后的面积增加
     */
    private double calculateAreaIncrease(Rectangle newBoundary, Rectangle oldBoundary) {
        double newArea = calculateArea(newBoundary);
        double oldArea = calculateArea(oldBoundary);
        return newArea - oldArea;
    }

    /*
     * 计算矩形的面积
     */
    private double calculateArea(Rectangle rect) {
        return (rect.maxX - rect.minX) * (rect.maxY - rect.minY);
    }

}
