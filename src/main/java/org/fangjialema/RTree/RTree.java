package org.fangjialema.RTree;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RTree {
    private RTreeNode root;

    private final int maxChildren;

    public RTree(int maxChildren, Rectangle initRange) {
        this.maxChildren = maxChildren;
        root = new RTreeNode(initRange); // 初始根节点范围
    }

    public RTree(int maxChildren) {
        this.maxChildren = maxChildren;
        root = new RTreeNode(); // 初始根节点范围
    }

    public void insert(Rectangle rect) {
        RTreeNode parentTemp = new RTreeNode();
        insertRecursive(root, rect, parentTemp);
        if (parentTemp.children.size() != 0) {
            parentTemp.children.add(root);
            root = parentTemp;
        }
    }

    public List<Rectangle> query(Rectangle queryRect) {
        List<Rectangle> result = new ArrayList<>();
        queryRecursive(root, queryRect, result);
        return result;
    }

    public void delete(Rectangle rect) {
        deleteRecursive(root, rect);
    }

    private boolean insertRecursive(RTreeNode curNode, RTreeNode newNode, Rectangle rect) {
        if (curNode.level == 0) {
            RTreeNode node = new RTreeNode(rect);
            curNode.children.add(node);
            if (curNode.children.size() < maxChildren) {
                curNode.regainBoundary();
                return false;
            } else {
                splitNode(curNode, newNode);
                return true;
            }
        } else {
            RTreeNode bestChild = chooseBestChild(curNode, rect);
            RTreeNode newNodeNxtLevel = new RTreeNode();
            boolean split = insertRecursive(bestChild, newNodeNxtLevel, rect);
            if (split) {

            }

        }
    }

    /*
     * 选择最佳的子节点，以最小化新节点的面积增加
     */
    private RTreeNode chooseBestChild(RTreeNode node, Rectangle rect) {
        double bestAreaIncrease = Double.POSITIVE_INFINITY;
        RTreeNode bestChild = null;
        for (RTreeNode child : node.children) {
            double areaIncrease = child.boundary.calculateAreaIncrease(rect);
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
     *
     */
    private void splitNode(RTreeNode node, RTreeNode newNode) {
        int n = node.children.size();
        double maxDis = Double.NEGATIVE_INFINITY;
        RTreeNode item1 = null;
        RTreeNode item2 = null;
        for (int i = 0; i < n; ++i) {
            RTreeNode child1 = node.children.get(i);
            for (int j = i + 1; j < n; ++j) {
                RTreeNode child2 = node.children.get(j);
                double distance = Rectangle.calculateDistance(child1.boundary, child2.boundary);
                if (distance > maxDis) {
                    item1 = child1;
                    item2 = child2;
                    maxDis = distance;
                }
            }
        }
        List<RTreeNode> list1 = new ArrayList<>();
        list1.add(item1);
        List<RTreeNode> list2 = new ArrayList<>();
        list2.add(item2);
        for (RTreeNode child : node.children) {
            if (child == item1 || child == item2) {
                continue;
            }
            double increase1 = item1.boundary.calculateAreaIncrease(child.boundary);
            double increase2 = item2.boundary.calculateAreaIncrease(child.boundary);
            if (increase1 < increase2) {
                list1.add(child);
            } else {
                list2.add(child);
            }
        }
        node.children = list1;
        newNode.children = list2;
        node.regainBoundary();
        newNode.regainBoundary();
    }


    public void printTree() {
        if (root == null) {
            System.out.println("empty tree");
            return;
        }
        LinkedList<RTreeNode> nodeStk = new LinkedList<>();
        LinkedList<String> prefixStk = new LinkedList<>();
        nodeStk.addLast(root);
        prefixStk.add("");
        while (!nodeStk.isEmpty()) {
            RTreeNode curNode = nodeStk.pollLast();
            String curPrefix = prefixStk.pollLast();
            System.out.println(curPrefix + curNode.boundary.toString());
            if (CollectionUtils.isEmpty(curNode.children)) {
                continue;
            }
            String nxtPrefix = curPrefix + "├── ";
            for (int i = curNode.children.size() - 1; i >= 1; --i) {
                nodeStk.add(curNode.children.get(i));
                prefixStk.add(nxtPrefix);
            }
            nodeStk.add(curNode.children.get(0));
            prefixStk.add(curPrefix + "└── ");
        }
    }


}
