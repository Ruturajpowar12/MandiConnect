package com.mandiconnect.dsa;

import java.util.ArrayList;
import java.util.List;

/**
 * Unbalanced binary search tree keyed by a {@code double} price. Each node stores
 * an application payload (a lot or a bid). Equal prices are inserted on the
 * <strong>right</strong> child, so in-order traversal is non-decreasing and duplicate
 * offers stay in the tree.
 *
 * <p>MandiConnect uses the tree to answer "which lots are expected between ₹min and
 * ₹max?" without scanning every lot. {@link #priceRange(double, double)} prunes any
 * subtree that cannot intersect the window.</p>
 *
 * <p>The tree is intentionally not AVL or red-black: the project demonstrates the
 * textbook BST, and sorted inserts degrade to a chain of height n. Random mandi
 * prices keep the height near log n. Nodes are a private raw class, not
 * {@code java.util.TreeMap}.</p>
 *
 * <pre>
 * Operation        Best        Average     Worst      Output space
 * insert           O(1)        O(log n)    O(n)       O(1)
 * inOrder          O(n)        O(n)        O(n)       O(n)
 * priceRange       O(h + k)    O(h + k)    O(n)       O(k)
 * height           O(n)        O(n)        O(n)       O(h) stack
 * visualize        O(n)        O(n)        O(n)       O(n)
 * Structure                                           O(n)
 * </pre>
 *
 * Height is the number of nodes on the longest path. An empty tree has height 0
 * and a single node has height 1, so any indexed lot makes the console report
 * a height above zero. {@code h} is that height and {@code k} is the number of hits.
 *
 * @param <T> payload type
 */
public class BinarySearchTree<T> {

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

    private Node<T> root;
    private int size;
    private long insertComparisons;

    /**
     * Inserts {@code data} under {@code price}. Duplicates go to the right subtree.
     * Best O(1) when the tree is empty, average O(log n), worst O(n) on a chain.
     * Counts one comparison per existing node visited.
     */
    public void insert(double price, T data) {
        root = insert(root, price, data);
        size++;
    }

    private Node<T> insert(Node<T> node, double price, T data) {
        if (node == null) {
            return new Node<>(price, data);
        }
        insertComparisons++;
        if (price < node.price) {
            node.left = insert(node.left, price, data);
        } else {
            node.right = insert(node.right, price, data);
        }
        return node;
    }

    /**
     * In-order payload list, prices non-decreasing. Time O(n), space O(n).
     */
    public List<T> inOrder() {
        List<T> out = new ArrayList<>();
        inOrder(root, out);
        return out;
    }

    private void inOrder(Node<T> node, List<T> out) {
        if (node == null) {
            return;
        }
        inOrder(node.left, out);
        out.add(node.data);
        inOrder(node.right, out);
    }

    /**
     * Payloads whose price lies in {@code [min, max]}, in ascending order.
     * Subtrees entirely below {@code min} or entirely above {@code max} are not visited.
     * Time O(h + k), space O(k), where k is the number of matches.
     */
    public List<T> priceRange(double min, double max) {
        List<T> out = new ArrayList<>();
        if (min <= max) {
            priceRange(root, min, max, out);
        }
        return out;
    }

    private void priceRange(Node<T> node, double min, double max, List<T> out) {
        if (node == null) {
            return;
        }
        if (node.price > min) {
            priceRange(node.left, min, max, out);
        }
        if (node.price >= min && node.price <= max) {
            out.add(node.data);
        }
        if (node.price <= max) {
            priceRange(node.right, min, max, out);
        }
    }

    /**
     * Number of inserted nodes. Time O(1), space O(1).
     */
    public int size() {
        return size;
    }

    /**
     * Nodes on the longest root-to-leaf path, or 0 when the tree is empty.
     * Time O(n), space O(h) for the recursion stack.
     */
    public int height() {
        return height(root);
    }

    private int height(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(height(node.left), height(node.right));
    }

    /**
     * How many price comparisons {@link #insert(double, Object)} has performed.
     * Time O(1), space O(1).
     */
    public long getInsertComparisons() {
        return insertComparisons;
    }

    /**
     * Sideways ASCII tree. The right subtree is drawn above the node and the left
     * subtree below it, using {@code ├──} and {@code └──}. Time O(n), space O(n).
     */
    public String visualize() {
        if (root == null) {
            return "(empty tree)";
        }
        StringBuilder sb = new StringBuilder();
        visualize(root, sb, "", "");
        return sb.toString();
    }

    private void visualize(Node<T> node, StringBuilder sb, String prefix, String childrenPrefix) {
        sb.append(prefix);
        sb.append(formatPrice(node.price));
        sb.append('\n');

        boolean hasLeft = node.left != null;
        boolean hasRight = node.right != null;

        if (hasRight && hasLeft) {
            visualize(node.right, sb, childrenPrefix + "├── ", childrenPrefix + "│   ");
            visualize(node.left, sb, childrenPrefix + "└── ", childrenPrefix + "    ");
        } else if (hasRight) {
            visualize(node.right, sb, childrenPrefix + "└── ", childrenPrefix + "    ");
        } else if (hasLeft) {
            visualize(node.left, sb, childrenPrefix + "└── ", childrenPrefix + "    ");
        }
    }

    private static String formatPrice(double price) {
        if (price == Math.rint(price) && !Double.isInfinite(price)) {
            return String.format("%.0f", price);
        }
        return String.format("%.2f", price);
    }
}
