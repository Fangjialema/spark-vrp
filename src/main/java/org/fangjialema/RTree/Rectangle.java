package org.fangjialema.RTree;

import java.util.Objects;

public class Rectangle {
    double minX, minY, maxX, maxY;

    private final double area;

    public Rectangle(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        area = calculateArea();
    }

    // 检查两个矩形是否相交
    public boolean intersects(Rectangle other) {
        return !(this.minX > other.maxX || this.maxX < other.minX || this.minY > other.maxY || this.maxY < other.minY);
    }

    /*
     * 合并两个矩形的边界
     */
    public static Rectangle combineRectangles(Rectangle rect1, Rectangle rect2) {
        double minX = Math.min(rect1.minX, rect2.minX);
        double minY = Math.min(rect1.minY, rect2.minY);
        double maxX = Math.max(rect1.maxX, rect2.maxX);
        double maxY = Math.max(rect1.maxY, rect2.maxY);
        return new Rectangle(minX, minY, maxX, maxY);
    }

    static double calculateDistance(Rectangle rect1, Rectangle rect2) {
        // 使用矩形中心点之间的欧几里得距离
        double centerX1 = (rect1.minX + rect1.maxX) / 2.0;
        double centerY1 = (rect1.minY + rect1.maxY) / 2.0;
        double centerX2 = (rect2.minX + rect2.maxX) / 2.0;
        double centerY2 = (rect2.minY + rect2.maxY) / 2.0;
        double dx = centerX1 - centerX2;
        double dy = centerY1 - centerY2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    double calculateAreaIncrease(Rectangle other) {
        double minX = Math.min(this.minX, other.minX);
        double minY = Math.min(this.minY, other.minY);
        double maxX = Math.max(this.maxX, other.maxX);
        double maxY = Math.max(this.maxY, other.maxY);
        return (maxX - minX) * (maxY - minY) - area;
    }


    double calculateArea() {
        return (maxX - minX) * (maxY - minY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rectangle rectangle = (Rectangle) o;
        return Double.compare(rectangle.minX, minX) == 0 && Double.compare(rectangle.minY, minY) == 0 && Double.compare(rectangle.maxX, maxX) == 0 && Double.compare(rectangle.maxY, maxY) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, maxX, maxY);
    }

    @Override
    public String toString() {
        return String.format("【 minX: %.2f, minY: %.2f, maxX: %.2f, maxY: %.2f】", minX, minY, maxX, maxY);
    }
}
