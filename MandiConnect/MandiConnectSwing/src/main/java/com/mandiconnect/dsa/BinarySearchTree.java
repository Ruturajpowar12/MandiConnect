package com.mandiconnect.dsa;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Binary Search Tree (BST) ordered by price.
 * Smaller prices go to the left child; equal or larger prices go to the right child.
 * Used for price-range filtering without scanning every lot.
 */
public class BinarySearchTree<T> {

    /** Single node storing price key, payload data, and left/right links. */
    private static final class Node<T> {
        final double price;
        final T data;
        Node<T> left;
        Node<T> right;

        Node(double price, T data) {
            this.price = price;
            this.data = data;
        }
    }

    private Node<T> root;           // Top node of the tree
    private int size;               // Total nodes in the tree
    private long insertComparisons; // Counts price comparisons during insert

    /** Inserts a value under the given price. Time: O(log n) average, O(n) worst. */
    public void insert(double price, T data) {
        root = insertNode(root, price, data);
        size++;
    }

    private Node<T> insertNode(Node<T> node, double price, T data) {
        // Base case: empty spot reached, create a new node here
        if (node == null) {
            return new Node<>(price, data);
        }
        insertComparisons++;
        if (price < node.price) {
            node.left = insertNode(node.left, price, data);
        } else {
            // Equal or greater prices go right so duplicates stay in the tree
            node.right = insertNode(node.right, price, data);
        }
        return node;
    }

    /** Returns all items in ascending order of price (Left -> Root -> Right). */
    public List<T> inOrder() {
        List<T> result = new ArrayList<>();
        traverseInOrder(root, result);
        return result;
    }

    private void traverseInOrder(Node<T> node, List<T> result) {
        if (node == null) {
            return;
        }
        traverseInOrder(node.left, result);
        result.add(node.data);
        traverseInOrder(node.right, result);
    }

    /** Returns items whose price is between min and max, skipping branches outside the range. */
    public List<T> priceRange(double min, double max) {
        List<T> result = new ArrayList<>();
        if (min <= max) {
            collectInRange(root, min, max, result);
        }
        return result;
    }

    private void collectInRange(Node<T> node, double min, double max, List<T> result) {
        if (node == null) {
            return;
        }
        // Only explore left subtree if current price is above min
        if (node.price > min) {
            collectInRange(node.left, min, max, result);
        }
        // Add current node if its price falls inside [min, max]
        if (node.price >= min && node.price <= max) {
            result.add(node.data);
        }
        // Only explore right subtree if current price is <= max
        if (node.price <= max) {
            collectInRange(node.right, min, max, result);
        }
    }

    public int size() {
        return size;
    }

    /** Returns the height of the tree (0 if empty, 1 for root only). */
    public int height() {
        return nodeHeight(root);
    }

    private int nodeHeight(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(nodeHeight(node.left), nodeHeight(node.right));
    }

    public long getInsertComparisons() {
        return insertComparisons;
    }

    /** Builds a sideways ASCII representation of the tree for the Algorithm Console. */
    public String visualize() {
        if (root == null) {
            return "(empty tree)";
        }
        StringBuilder sb = new StringBuilder();
        buildAscii(root, sb, "", "");
        return sb.toString();
    }

    private void buildAscii(Node<T> node, StringBuilder sb, String prefix, String childPrefix) {
        sb.append(prefix).append(formatPrice(node.price)).append('\n');

        boolean hasLeft = node.left != null;
        boolean hasRight = node.right != null;

        // Draw right child on top and left child below
        if (hasRight && hasLeft) {
            buildAscii(node.right, sb, childPrefix + "├── ", childPrefix + "│   ");
            buildAscii(node.left, sb, childPrefix + "└── ", childPrefix + "    ");
        } else if (hasRight) {
            buildAscii(node.right, sb, childPrefix + "└── ", childPrefix + "    ");
        } else if (hasLeft) {
            buildAscii(node.left, sb, childPrefix + "└── ", childPrefix + "    ");
        }
    }

    private static String formatPrice(double price) {
        if (price == Math.rint(price) && !Double.isInfinite(price)) {
            return String.format("%.0f", price);
        }
        return String.format("%.2f", price);
    }
}
