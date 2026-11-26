package com.mandiconnect.ui;

import com.mandiconnect.dsa.BidLinkedList;
import com.mandiconnect.dsa.BidQueue;
import com.mandiconnect.dsa.BinarySearchTree;
import com.mandiconnect.dsa.HashTable;
import com.mandiconnect.dsa.MaxHeap;
import com.mandiconnect.dsa.QuickSort;
import com.mandiconnect.dsa.benchmark.BenchmarkResult;
import com.mandiconnect.dsa.benchmark.BenchmarkRunner;
import com.mandiconnect.model.Bid;
import com.mandiconnect.model.Lot;
import com.mandiconnect.service.BidService;
import com.mandiconnect.service.LotService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Algorithm Console window: demonstrates all 6 custom ADS structures live
 * and compares their speed against Java's built-in collections.
 */
public class AlgorithmConsoleFrame extends JFrame {

    private final LotService lots;
    private final BidService bids;

    public AlgorithmConsoleFrame(LotService lots, BidService bids) {
        super("MandiConnect — Algorithm Console");
        this.lots = lots;
        this.bids = bids;
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // One tab for each data structure + one Benchmarks tab
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Hash Table", createReportTab(() -> hashReport()));
        tabs.addTab("BST (Price)", createReportTab(() -> bstReport()));
        tabs.addTab("QuickSort", createReportTab(() -> quickReport()));
        tabs.addTab("MaxHeap (Bids)", createReportTab(() -> heapReport()));
        tabs.addTab("Bid Queue", createReportTab(() -> queueReport()));
        tabs.addTab("Bid LinkedList", createReportTab(() -> listReport()));
        tabs.addTab("Benchmarks", createBenchmarkTab());
        add(tabs);
    }

    /** Functional helper so each tab can regenerate its text report on Refresh. */
    private interface ReportSupplier {
        String get();
    }

    /** Builds a tab with a Refresh button at the top and monospaced text area below. */
    private JPanel createReportTab(ReportSupplier supplier) {
        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);

        JButton refreshBtn = UiText.button("Refresh", e -> area.setText(supplier.get()));
        area.setText(supplier.get());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(refreshBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    /** Tab 1: Shows HashTable size, load factor, sample lookups, and probe count. */
    private String hashReport() {
        HashTable<Lot> table = lots.getHash();
        long before = table.getTotalProbes();

        List<Lot> sample = new ArrayList<>();
        for (Lot lot : lots.getBST().inOrder()) {
            if (sample.size() == 10) {
                break;
            }
            sample.add(lot);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hash table — linear probing, polynomial hash h = (h*31 + c) mod capacity\n");
        sb.append("capacity     : ").append(table.capacity()).append('\n');
        sb.append("size         : ").append(table.size()).append('\n');
        sb.append("load factor  : ").append(String.format("%.4f", table.loadFactor())).append('\n');
        sb.append("probes before: ").append(before).append('\n');
        sb.append("\nget(lotCode) on ").append(sample.size()).append(" sample lots\n");
        if (sample.isEmpty()) {
            sb.append("(no lots indexed yet — add a lot as a farmer)\n");
        }
        for (Lot lot : sample) {
            Lot found = table.get(lot.getLotCode()).orElse(null);
            sb.append("  get(").append(lot.getLotCode()).append(") -> ");
            sb.append(found == null ? "MISS" : found.getCropName() + " @" + UiText.money(found.getExpectedPrice()));
            sb.append('\n');
        }
        long after = table.getTotalProbes();
        sb.append("\nprobes after : ").append(after).append('\n');
        sb.append("probes used  : ").append(after - before).append('\n');
        if (!sample.isEmpty()) {
            sb.append(String.format("avg probes/lookup : %.2f%n", (after - before) / (double) sample.size()));
        }
        sb.append("\nA small average probe count is the practical form of O(1) search.\n");
        return sb.toString();
    }

    /** Tab 2: Shows BST size, height, in-order list, priceRange(2000, 5000), and ASCII tree. */
    private String bstReport() {
        BinarySearchTree<Lot> tree = lots.getBST();
        StringBuilder sb = new StringBuilder();
        sb.append("Binary search tree keyed by expected price (duplicates go right)\n");
        sb.append("size                : ").append(tree.size()).append('\n');
        sb.append("height              : ").append(tree.height()).append('\n');
        sb.append("insert comparisons  : ").append(tree.getInsertComparisons()).append('\n');

        List<Lot> ordered = tree.inOrder();
        sb.append("\nIn-order (first 15, ascending price)\n");
        int shown = Math.min(15, ordered.size());
        if (shown == 0) {
            sb.append("(empty tree)\n");
        }
        for (int i = 0; i < shown; i++) {
            Lot lot = ordered.get(i);
            sb.append(String.format("  %2d. ₹%s  %s  %s%n", i + 1, UiText.money(lot.getExpectedPrice()), lot.getLotCode(), lot.getCropName()));
        }

        List<Lot> range = tree.priceRange(2000, 5000);
        sb.append("\npriceRange(2000, 5000) count: ").append(range.size()).append('\n');
        for (int i = 0; i < Math.min(15, range.size()); i++) {
            Lot lot = range.get(i);
            sb.append("  ₹").append(UiText.money(lot.getExpectedPrice())).append("  ").append(lot.getLotCode()).append('\n');
        }
        sb.append("\nASCII tree (right child above, left child below)\n");
        sb.append(tree.visualize());
        return sb.toString();
    }

    /** Tab 3: Runs QuickSort on available lots and compares comparisons vs n*log2(n). */
    private String quickReport() {
        StringBuilder sb = new StringBuilder();
        try {
            List<Lot> data = new ArrayList<>(lots.available());
            sb.append("QuickSort — Lomuto partition, median-of-three pivot\n");
            sb.append("Source: available lots\n");
            long comparisons = QuickSort.sortAndCount(data, (a, b) -> Double.compare(a.getExpectedPrice(), b.getExpectedPrice()));
            int n = data.size();
            double bound = (n <= 1) ? 0 : n * (Math.log(n) / Math.log(2));
            sb.append("N                  : ").append(n).append('\n');
            sb.append("comparison count   : ").append(comparisons).append('\n');
            sb.append(String.format("theoretical n·log2 n: %.1f%n", bound));
            if (bound > 0) {
                sb.append(String.format("comparisons / bound : %.2f%n", comparisons / bound));
            }
            sb.append("\nFirst 20 lots after sort (price ascending)\n");
            if (data.isEmpty()) {
                sb.append("(no available lots)\n");
            }
            for (int i = 0; i < Math.min(20, data.size()); i++) {
                Lot lot = data.get(i);
                sb.append(String.format("  ₹%s  %s  %s%n", UiText.money(lot.getExpectedPrice()), lot.getLotCode(), lot.getCropName()));
            }
        } catch (Exception ex) {
            sb.append("QuickSort failed: ").append(ex.getMessage());
        }
        return sb.toString();
    }

    /** Tab 4: Shows each lot's MaxHeap array, root maximum, and sift swap counts. */
    private String heapReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Max-heap per lot — index 0 is the highest bid\n");
        sb.append("parent (i-1)/2    left 2i+1    right 2i+2\n\n");
        int shown = 0;
        try {
            for (Lot lot : lots.all()) {
                MaxHeap<Bid> heap = bids.getHeap(lot.getId());
                if (heap == null || heap.isEmpty()) {
                    continue;
                }
                shown++;
                MaxHeap.Item<Bid> top = heap.peekMax();
                sb.append(lot.getLotCode()).append("  ").append(lot.getCropName()).append('\n');
                sb.append("  heap size     : ").append(heap.size()).append('\n');
                sb.append("  peek max      : ₹").append(UiText.money(top.amount))
                        .append("  trader #").append(top.data.getTraderId()).append('\n');
                sb.append("  sift-up swaps : ").append(heap.getSiftUpSwaps()).append('\n');
                sb.append("  sift-down swaps: ").append(heap.getSiftDownSwaps()).append('\n');
                sb.append("  heap array    : ").append(formatHeap(heap.asArray())).append("\n\n");
            }
        } catch (Exception ex) {
            sb.append("Heap report failed: ").append(ex.getMessage()).append('\n');
        }
        if (shown == 0) {
            sb.append("(no bids indexed — place a bid as a trader, then refresh)\n");
        }
        return sb.toString();
    }

    /** Tab 5: Shows the FIFO BidQueue snapshot and session dequeue log. */
    private String queueReport() {
        BidQueue<Bid> queue = bids.getQueue();
        StringBuilder sb = new StringBuilder();
        sb.append("Bid queue — FIFO intake\n");
        sb.append("queue size: ").append(queue.size()).append('\n');
        sb.append("snapshot of pending bids:\n");
        List<Bid> pending = queue.snapshot();
        if (pending.isEmpty()) {
            sb.append("  (empty)\n");
        }
        for (Bid bid : pending) {
            sb.append("  lot #").append(bid.getLotId())
                    .append("  ₹").append(UiText.money(bid.getAmount()))
                    .append("  trader #").append(bid.getTraderId()).append('\n');
        }
        sb.append("\nFIFO fairness: placeBid enqueues the offer, then dequeues the head\n");
        sb.append("before writing it to MySQL. Arrival order is the persist order.\n");
        sb.append("The pending snapshot is empty whenever the UI is idle, because\n");
        sb.append("each bid is drained before placeBid returns.\n\n");
        sb.append("Bids dequeued this session, head to tail:\n");
        List<Bid> done = bids.getProcessedFifo().toList();
        if (done.isEmpty()) {
            sb.append("  (none yet in this dashboard session)\n");
        }
        for (Bid bid : done) {
            sb.append("  [₹").append(UiText.money(bid.getAmount())).append("] lot #")
                    .append(bid.getLotId()).append(" trader #").append(bid.getTraderId()).append('\n');
        }
        return sb.toString();
    }

    /** Tab 6: Prints each lot's BidLinkedList node chain from head to tail. */
    private String listReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Singly linked bid history — head is the earliest bid, tail is the latest\n\n");
        int shown = 0;
        try {
            for (Lot lot : lots.all()) {
                BidLinkedList<Bid> list = bids.getList(lot.getId());
                if (list == null || list.size() == 0) {
                    continue;
                }
                shown++;
                sb.append(lot.getLotCode()).append("  ").append(lot.getCropName()).append("  (").append(list.size()).append(" nodes)\n  ");
                for (Bid bid : list.toList()) {
                    sb.append("[₹").append(UiText.money(bid.getAmount())).append("] → ");
                }
                sb.append("null\n\n");
            }
        } catch (Exception ex) {
            sb.append("Linked list report failed: ").append(ex.getMessage()).append('\n');
        }
        if (shown == 0) {
            sb.append("(no bid lists yet)\n");
        }
        return sb.toString();
    }

    /** Tab 7: Table comparing custom structures against Java built-in equivalents. */
    private JPanel createBenchmarkTab() {
        DefaultTableModel model = UiText.readOnlyModel(
                "Algorithm", "N", "Custom (ms)", "Built-in (ms)", "Correct", "Complexity");
        JTable table = new JTable(model);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JButton runBtn = new JButton("Run Benchmarks");
        runBtn.addActionListener(e -> runBenchmarks(runBtn, model));

        JPanel north = new JPanel(new BorderLayout());
        north.add(runBtn, BorderLayout.WEST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /** Runs BenchmarkRunner in a background thread so the Swing UI stays responsive. */
    private void runBenchmarks(JButton runBtn, DefaultTableModel model) {
        runBtn.setEnabled(false);
        model.setRowCount(0);

        new Thread(() -> {
            try {
                List<BenchmarkResult> results = new BenchmarkRunner().runAll();
                // Update Swing table back on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    for (BenchmarkResult r : results) {
                        model.addRow(new Object[]{
                                r.algorithm(),
                                r.n(),
                                String.format("%.3f", r.customNanos() / 1_000_000.0),
                                String.format("%.3f", r.builtinNanos() / 1_000_000.0),
                                r.correct() ? "✔" : "✘",
                                r.complexity()
                        });
                    }
                    runBtn.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    runBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Benchmark", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private static String formatHeap(double[] amounts) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < amounts.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(UiText.money(amounts[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}
