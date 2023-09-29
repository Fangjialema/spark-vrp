package org.fangjialema.RTree;

import java.util.Objects;

public class Rectangle {
    double minX, minY, maxX, maxY;

    public Rectangle(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
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
}
