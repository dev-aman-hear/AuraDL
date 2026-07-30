package com.auradl.ui;

import com.auradl.api.AppleMusicApiClient;
import com.auradl.api.MediaItem;
import com.auradl.config.ApiMethod;
import com.auradl.config.ConfigManager;
import com.auradl.download.DownloadManager;
import com.auradl.download.DownloadStatus;
import com.auradl.download.DownloadTask;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class GlassUIWindow extends JFrame {
    private final ConfigManager configManager;
    private final AppleMusicApiClient apiClient;
    private final DownloadManager downloadManager;

    private JTabbedPane mainTabbedPane;
    private JTextField searchField;
    private DefaultListModel<MediaItem> searchResultsModel;
    // Categorized search models
    private DefaultListModel<MediaItem> songsModel;
    private DefaultListModel<MediaItem> albumsModel;
    private DefaultListModel<MediaItem> videosModel;
    private DefaultListModel<DownloadTask> queueListModel;
    private JLabel statusLabel;
    private JTextArea logArea; // Live gamdl output console
    private JLabel queueStatsLabel; // Summary: N active · M done · P failed

    // Hero Dashboard Chips
    private JLabel chipOutput;
    private JLabel chipCodec;
    private JLabel chipMethod;
    private JLabel chipEngine;
    private JLabel chipAuthStatus;

    // Settings Controls
    private JComboBox<String> apiMethodCombo;
    private JTextField outputDirField;
    private JTextField tempDirField;
    private JTextField cookiesField;
    private JTextField mediaUserTokenField;
    private JTextField nm3u8dlreField;
    private JTextField ffmpegField;
    private JTextField albumFolderTemplateField;
    private JTextField songFileTemplateField;
    private JComboBox<String> songQualityCombo;
    private JComboBox<String> videoResolutionCombo;
    private JComboBox<String> videoCodecCombo;
    private JComboBox<String> videoRemuxCombo;
    private JSpinner concurrentSpinner;
    private JTextField wrapperUrlField;
    private JComboBox<String> downloadModeCombo;
    private JComboBox<String> languageCombo;
    private JComboBox<String> coverFormatCombo;
    private JComboBox<Integer> coverSizeCombo;
    private JComboBox<String> lyricsFormatCombo;
    private JCheckBox saveCoverCheckBox;
    private JCheckBox savePlaylistCheckBox;
    private JCheckBox overwriteCheckBox;

    // Live Naming Preview Control
    private JLabel livePreviewLabel;

    // Window controls & dragging
    private Point mouseClickPoint;
    private MacTrafficLightBtn closeMacBtn;
    private MacTrafficLightBtn minMacBtn;
    private MacTrafficLightBtn maxMacBtn;

    // True Dark Premium Golden Theme Palette
    private static final Color COL_BG = new Color(5, 6, 9); // True Dark AMOLED Background (#050609)
    private static final Color COL_CARD = new Color(14, 18, 30); // True Dark Glass Card
    private static final Color COL_SECTION = new Color(15, 18, 28, 252); // True Dark Section Card (#0F121C)
    private static final Color COL_GOLD = new Color(255, 215, 0); // Royal Gold (#FFD700)
    private static final Color COL_RED = new Color(250, 45, 85); // Apple Red Accent
    private static final Color COL_CYAN = new Color(0, 220, 255); // Neon Cyan Accent
    private static final Color COL_PURPLE = new Color(168, 85, 247); // Royal Purple Accent
    private static final Color COL_GREEN = new Color(0, 230, 145); // Emerald Green Accent
    private static final Color COL_AMBER = new Color(255, 215, 0); // Amber set to Royal Gold
    private static final Color COL_TEXT = new Color(245, 247, 255); // High-contrast text (#F5F7FF)
    private static final Color COL_SUBTEXT = new Color(145, 155, 180); // Subtext (#919BB4)
    private static final Color COL_BORDER = new Color(212, 160, 23, 90); // Subtle Metallic Golden Outline
    private static final Color COL_BORDER_GOLD = new Color(255, 215, 0, 180); // Glowing Gold Focus Outline

    public GlassUIWindow(ConfigManager configManager, AppleMusicApiClient apiClient, DownloadManager downloadManager) {
        this.configManager = configManager;
        this.apiClient = apiClient;
        this.downloadManager = downloadManager;

        // Ensure default output directory points to user.dir / Apple Music
        String curOut = this.configManager.getConfig().getOutputDir();
        if (curOut == null || curOut.isEmpty() || curOut.equals("D:/Apple Music")) {
            String defaultDir = new File(System.getProperty("user.dir"), "Apple Music").getAbsolutePath().replace('\\',
                    '/');
            this.configManager.getConfig().setOutputDir(defaultDir);
        }

        initUI();
        this.downloadManager.setTaskUpdateListener(task -> SwingUtilities.invokeLater(() -> {
            updateQueueItem(task);
            refreshQueueStats();
        }));
        this.downloadManager.setLogListener(line -> SwingUtilities.invokeLater(() -> appendLog(line)));
    }

    // ─────────────────────────────────────────────────────────
    // Root layout
    // ─────────────────────────────────────────────────────────
    private void initUI() {
        setUndecorated(true);
        setTitle("AuraDL v1.0 — Apple Music Desktop Downloader");
        setSize(1240, 920);
        setMinimumSize(new Dimension(960, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // ambient golden & neon glass glows
                paintGlow(g2, -100, -100, 520, new Color(255, 215, 0, 32));
                paintGlow(g2, getWidth() - 380, getHeight() - 380, 560, new Color(212, 160, 23, 26));
                paintGlow(g2, getWidth() / 2 - 240, getHeight() / 2 - 240, 480, new Color(168, 85, 247, 18));

                // Double-layer Metallic Golden Window Outline
                g2.setColor(COL_BORDER_GOLD);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(212, 160, 23, 80));
                g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                g2.dispose();
            }
        };
        root.setLayout(new BorderLayout(0, 4));
        root.setBorder(new EmptyBorder(8, 12, 12, 12));
        setContentPane(root);

        JPanel topContainer = new JPanel(new BorderLayout(0, 2));
        topContainer.setOpaque(false);
        topContainer.add(buildTitleBar(), BorderLayout.NORTH);
        topContainer.add(buildHeader(), BorderLayout.CENTER);

        root.add(topContainer, BorderLayout.NORTH);

        mainTabbedPane = buildTabs();
        root.add(mainTabbedPane, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private enum MacBtnType {
        CLOSE, MINIMIZE, MAXIMIZE
    }

    private void notifyGroupHover(boolean hover) {
        if (closeMacBtn != null)
            closeMacBtn.setGroupHover(hover);
        if (minMacBtn != null)
            minMacBtn.setGroupHover(hover);
        if (maxMacBtn != null)
            maxMacBtn.setGroupHover(hover);
    }

    private class MacTrafficLightBtn extends JButton {
        private final MacBtnType type;
        private boolean groupHover = false;
        private boolean selfHover = false;
        private boolean isMaximized = false;

        public MacTrafficLightBtn(MacBtnType type) {
            this.type = type;
            setPreferredSize(new Dimension(13, 13));
            setMinimumSize(new Dimension(13, 13));
            setMaximumSize(new Dimension(13, 13));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    selfHover = true;
                    notifyGroupHover(true);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    selfHover = false;
                    notifyGroupHover(false);
                    repaint();
                }
            });
        }

        public void setGroupHover(boolean hover) {
            this.groupHover = hover;
            repaint();
        }

        public void setMaximized(boolean max) {
            this.isMaximized = max;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();
            int diam = Math.min(w, h);
            int x = (w - diam) / 2;
            int y = (h - diam) / 2;

            Color baseColor;
            Color borderColor;
            Color iconColor;

            switch (type) {
                case CLOSE:
                    baseColor = selfHover ? new Color(255, 95, 86) : new Color(255, 95, 86, 230);
                    borderColor = new Color(224, 68, 62);
                    iconColor = new Color(77, 0, 0, 220);
                    break;
                case MINIMIZE:
                    baseColor = selfHover ? new Color(255, 189, 46) : new Color(255, 189, 46, 230);
                    borderColor = new Color(222, 161, 35);
                    iconColor = new Color(153, 87, 0, 220);
                    break;
                case MAXIMIZE:
                default:
                    baseColor = selfHover ? new Color(39, 201, 63) : new Color(39, 201, 63, 230);
                    borderColor = new Color(26, 171, 41);
                    iconColor = new Color(0, 102, 0, 220);
                    break;
            }

            g2.setColor(baseColor);
            g2.fillOval(x, y, diam, diam);

            g2.setColor(borderColor);
            g2.drawOval(x, y, diam - 1, diam - 1);

            if (groupHover || selfHover) {
                g2.setColor(iconColor);
                int cx = x + diam / 2;
                int cy = y + diam / 2;

                switch (type) {
                    case CLOSE:
                        g2.setStroke(new BasicStroke(1.4f));
                        g2.drawLine(cx - 3, cy - 3, cx + 3, cy + 3);
                        g2.drawLine(cx + 3, cy - 3, cx - 3, cy + 3);
                        break;
                    case MINIMIZE:
                        g2.setStroke(new BasicStroke(1.4f));
                        g2.drawLine(cx - 3, cy, cx + 3, cy);
                        break;
                    case MAXIMIZE:
                        g2.setStroke(new BasicStroke(1.2f));
                        if (isMaximized) {
                            g2.drawLine(cx - 3, cy + 1, cx + 1, cy - 3);
                            g2.drawLine(cx + 3, cy - 1, cx - 1, cy + 3);
                        } else {
                            g2.drawLine(cx - 3, cy - 3, cx + 3, cy + 3);
                            g2.drawLine(cx - 3, cy - 1, cx - 3, cy - 3);
                            g2.drawLine(cx - 3, cy - 3, cx - 1, cy - 3);
                            g2.drawLine(cx + 3, cy + 1, cx + 3, cy + 3);
                            g2.drawLine(cx + 3, cy + 3, cx + 1, cy + 3);
                        }
                        break;
                }
            }

            g2.dispose();
        }
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(100, 34));
        bar.setBorder(new EmptyBorder(6, 12, 4, 12));

        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH)
                    return;
                Point curr = e.getLocationOnScreen();
                setLocation(curr.x - mouseClickPoint.x, curr.y - mouseClickPoint.y);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    toggleMaximize();
            }
        };
        bar.addMouseListener(dragAdapter);
        bar.addMouseMotionListener(dragAdapter);

        JLabel titleLbl = new JLabel("  AuraDL v1.0 — Apple Music Desktop");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLbl.setForeground(new Color(150, 165, 195));
        titleLbl.addMouseListener(dragAdapter);
        titleLbl.addMouseMotionListener(dragAdapter);
        bar.add(titleLbl, BorderLayout.WEST);

        JPanel trafficLights = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        trafficLights.setOpaque(false);

        minMacBtn = new MacTrafficLightBtn(MacBtnType.MINIMIZE);
        minMacBtn.setToolTipText("Minimize");
        minMacBtn.addActionListener(e -> setState(JFrame.ICONIFIED));

        maxMacBtn = new MacTrafficLightBtn(MacBtnType.MAXIMIZE);
        maxMacBtn.setToolTipText("Maximize / Fullscreen");
        maxMacBtn.addActionListener(e -> toggleMaximize());

        closeMacBtn = new MacTrafficLightBtn(MacBtnType.CLOSE);
        closeMacBtn.setToolTipText("Close Application");
        closeMacBtn.addActionListener(e -> System.exit(0));

        trafficLights.add(minMacBtn);
        trafficLights.add(maxMacBtn);
        trafficLights.add(closeMacBtn);

        bar.add(trafficLights, BorderLayout.EAST);

        return bar;
    }

    private void toggleMaximize() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
            if (maxMacBtn != null)
                maxMacBtn.setMaximized(false);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            if (maxMacBtn != null)
                maxMacBtn.setMaximized(true);
        }
    }

    private void paintGlow(Graphics2D g2, int x, int y, int r, Color c) {
        g2.setColor(c);
        g2.fillOval(x, y, r, r);
    }

    // ─────────────────────────────────────────────────────────
    // Header
    // ─────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = glassCard(0);
        h.setLayout(new BorderLayout(14, 0));
        h.setBorder(new EmptyBorder(13, 22, 13, 20));

        JLabel logo = new JLabel(" AuraDL");
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        logo.setForeground(COL_RED);
        try {
            java.net.URL iconUrl = GlassUIWindow.class.getResource("/assets/AuraLogo.png");
            if (iconUrl == null) iconUrl = GlassUIWindow.class.getResource("/assets/logo.png");
            Image iconImg = null;
            if (iconUrl != null) {
                iconImg = new ImageIcon(iconUrl).getImage();
            } else {
                File imgFile = new File("src-java/assets/AuraLogo.png");
                if (!imgFile.exists()) imgFile = new File("src/assets/AuraLogo.png");
                if (!imgFile.exists()) imgFile = new File("src-java/assets/logo.png");
                if (!imgFile.exists()) imgFile = new File("src/assets/logo.png");
                if (imgFile.exists()) {
                    iconImg = new ImageIcon(imgFile.getAbsolutePath()).getImage();
                }
            }
            if (iconImg != null) {
                Image scaled = iconImg.getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                logo.setIcon(new ImageIcon(scaled));
                setIconImage(iconImg);
                try {
                    if (Taskbar.isTaskbarSupported()) {
                        Taskbar taskbar = Taskbar.getTaskbar();
                        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                            taskbar.setIconImage(iconImg);
                        }
                    }
                } catch (Throwable ignored) {}
            } else {
                logo.setText("✨ AuraDL");
            }
        } catch (Exception ignored) {
            logo.setText("✨ AuraDL");
        }
        h.add(logo, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(10, 0));
        center.setOpaque(false);
        searchField = darkTextField("Paste Apple Music URL or search song / artist / album…");
        center.add(searchField, BorderLayout.CENTER);

        JButton go = glowButton("⚡  Add / Search", COL_RED, Color.WHITE);
        go.setFont(new Font("SansSerif", Font.BOLD, 13));
        go.addActionListener(e -> handleSearch());
        center.add(go, BorderLayout.EAST);

        MouseAdapter headerDrag = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH)
                    return;
                Point curr = e.getLocationOnScreen();
                setLocation(curr.x - mouseClickPoint.x, curr.y - mouseClickPoint.y);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    toggleMaximize();
            }
        };
        h.addMouseListener(headerDrag);
        h.addMouseMotionListener(headerDrag);

        h.add(center, BorderLayout.CENTER);

        JLabel badge = new JLabel("  v1.0");
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(COL_SUBTEXT);
        h.add(badge, BorderLayout.EAST);
        return h;
    }

    // ─────────────────────────────────────────────────────────
    // Tabs
    // ─────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane t = new JTabbedPane(JTabbedPane.TOP);
        t.setFont(new Font("SansSerif", Font.BOLD, 13));
        t.setForeground(COL_TEXT);
        t.setBackground(COL_BG);
        t.setBorder(null);
        t.setOpaque(false);
        t.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = new Color(0, 0, 0, 0);
                lightHighlight = new Color(0, 0, 0, 0);
                shadow = new Color(0, 0, 0, 0);
                darkShadow = new Color(0, 0, 0, 0);
                focus = new Color(0, 0, 0, 0);
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Eliminate Swing's default white border line!
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? COL_BORDER_GOLD : new Color(212, 160, 23, 50));
                g2.drawRoundRect(x, y + (isSelected ? 2 : 4), w - 2, h - (isSelected ? 2 : 4), 10, 10);
                g2.dispose();
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(new Color(15, 18, 28, 250));
                    g2.fillRoundRect(x, y + 2, w - 2, h - 2, 10, 10);
                    g2.setColor(COL_GOLD);
                    g2.fillRect(x + 6, y + h - 3, w - 14, 3); // Royal Golden active tab indicator
                } else {
                    g2.setColor(new Color(8, 10, 16, 200));
                    g2.fillRoundRect(x, y + 4, w - 2, h - 4, 8, 8);
                }
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                    Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            }
        });
        t.addTab("🔍  Search", buildSearchTab());
        t.addTab("📥  Queue", buildQueueTab());
        t.addTab("⚙️  Settings", buildSettingsTab());
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // Search Tab
    // ─────────────────────────────────────────────────────────
    private JLabel searchStatusLabel;
    private JList<MediaItem> searchList;
    // Categorized section lists & header labels
    private JList<MediaItem> songsList;
    private JList<MediaItem> albumsList;
    private JList<MediaItem> videosList;

    // Tab pane ref so handleSearch can update tab titles with counts
    private JTabbedPane searchTabPane;

    private JPanel buildSearchTab() {
        JPanel p = glassCard(16);
        p.setLayout(new BorderLayout(0, 12));
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        // ── Top header
        // ──────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(sectionTitle("🔍  Apple Music Search"), BorderLayout.WEST);

        searchStatusLabel = new JLabel("Paste a URL above to download, or type a song name to search");
        searchStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchStatusLabel.setForeground(COL_SUBTEXT);
        top.add(searchStatusLabel, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // ── Shared model (URL resolve)
        // ───────────────────────────────
        searchResultsModel = new DefaultListModel<>();
        searchList = new JList<>(searchResultsModel);
        searchList.setCellRenderer(new MediaItemCellRenderer());
        searchList.setOpaque(false);
        searchList.setBackground(new Color(0, 0, 0, 0));
        searchList.setSelectionBackground(new Color(250, 45, 85, 90));
        searchList.setFixedCellHeight(-1);
        searchList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MediaItem sel = searchList.getSelectedValue();
                    if (sel != null)
                        queueDownload(sel);
                }
            }
        });

        // ── Categorized models + lists
        // ───────────────────────────────
        songsModel = new DefaultListModel<>();
        albumsModel = new DefaultListModel<>();
        videosModel = new DefaultListModel<>();

        songsList = makeCategoryList(songsModel);
        albumsList = makeCategoryList(albumsModel);
        videosList = makeCategoryList(videosModel);

        // ── Build the dark-styled tab pane
        // ───────────────────────────
        searchTabPane = buildSearchTabPane();

        p.add(searchTabPane, BorderLayout.CENTER);

        // ── Bottom tip
        // ───────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel tip = new JLabel(
                "💡  Double-click a result or click 📥 Download to add to queue  ·  Supports: songs, albums, playlists, music videos");
        tip.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tip.setForeground(new Color(80, 92, 118));
        bottom.add(tip, BorderLayout.WEST);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    /**
     * Builds the premium dark JTabbedPane with Songs / Albums / Music Videos tabs.
     */
    private JTabbedPane buildSearchTabPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(5, 6, 9));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tabs.setOpaque(false);
        tabs.setBackground(new Color(5, 6, 9));
        tabs.setForeground(COL_SUBTEXT);

        // Custom tab UI — dark background, gold selected indicator
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            private final int ARC = 10;

            @Override
            protected void installDefaults() {
                super.installDefaults();
                lightHighlight = new Color(0, 0, 0, 0);
                shadow = new Color(0, 0, 0, 0);
                darkShadow = new Color(0, 0, 0, 0);
                highlight = new Color(0, 0, 0, 0);
                focus = new Color(0, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                    int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(new Color(18, 20, 32));
                    g2.fillRoundRect(x, y, w, h, ARC, ARC);
                    // Gold bottom accent line
                    g2.setColor(COL_GOLD);
                    g2.fillRoundRect(x + 6, y + h - 3, w - 12, 3, 2, 2);
                } else {
                    g2.setColor(new Color(10, 11, 18));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, ARC, ARC);
                }
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement,
                    int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                if (!isSelected)
                    return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 215, 0, 100));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y, w - 1, h - 1, ARC, ARC);
                g2.dispose();
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Draw a subtle gold hairline below the tab bar
                Graphics2D g2 = (Graphics2D) g.create();
                int y = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) + tabAreaInsets.top;
                g2.setColor(new Color(255, 215, 0, 45));
                g2.fillRect(0, y, tabPane.getWidth(), 1);
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement,
                    Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // suppress default focus rectangle
            }
        });

        // Shared scroll pane factory
        tabs.addTab("🎵  Songs", makeTabScrollPane(songsList));
        tabs.addTab("💽  Albums", makeTabScrollPane(albumsList));
        tabs.addTab("🎬  Music Videos", makeTabScrollPane(videosList));

        // Style tab labels
        for (int i = 0; i < tabs.getTabCount(); i++) {
            JLabel lbl = new JLabel(tabs.getTitleAt(i));
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(i == 0 ? COL_GREEN : i == 1 ? COL_CYAN : COL_AMBER);
            lbl.setBorder(new EmptyBorder(8, 14, 8, 14));
            tabs.setTabComponentAt(i, lbl);
        }

        // Tab selection change listener — dynamic category search trigger
        tabs.addChangeListener(e -> {
            int selIdx = tabs.getSelectedIndex();
            for (int i = 0; i < tabs.getTabCount(); i++) {
                Component c = tabs.getTabComponentAt(i);
                if (c instanceof JLabel lbl) {
                    boolean sel = (i == selIdx);
                    Color base = i == 0 ? COL_GREEN : i == 1 ? COL_CYAN : COL_AMBER;
                    lbl.setForeground(
                            sel ? Color.WHITE : new Color(base.getRed(), base.getGreen(), base.getBlue(), 140));
                }
            }
            onSearchTabChanged(selIdx);
        });
        // Trigger initial state
        tabs.setSelectedIndex(0);

        return tabs;
    }

    /** Triggers on-demand targeted search when user switches search tabs. */
    private void onSearchTabChanged(int tabIdx) {
        String q = searchField != null ? searchField.getText().trim() : "";
        if (q.isEmpty() || AppleMusicApiClient.isAppleMusicUrl(q))
            return;

        String categoryName = tabIdx == 0 ? "Songs" : tabIdx == 1 ? "Albums" : "Music Videos";
        String entity = tabIdx == 0 ? "song" : tabIdx == 1 ? "album" : "musicVideo";
        DefaultListModel<MediaItem> model = tabIdx == 0 ? songsModel : tabIdx == 1 ? albumsModel : videosModel;

        // If category model has loading state or is empty, fire explicit category
        // search
        if (model.isEmpty() || (model.size() == 1 && model.get(0).getId().startsWith("loading-"))) {
            searchStatusLabel.setText("🔍  Searching only " + categoryName + " for '" + q + "'...");
            model.clear();
            model.addElement(new MediaItem("loading-" + tabIdx, "⏳  Searching " + categoryName.toLowerCase() + "…",
                    "Please wait", "", entity, 0, ""));

            apiClient.searchByType(q, entity).thenAccept(items -> SwingUtilities.invokeLater(() -> {
                model.clear();
                items.forEach(model::addElement);
                updateTabCount(tabIdx, tabIdx == 0 ? "🎵  Songs" : tabIdx == 1 ? "💽  Albums" : "🎬  Music Videos",
                        items.size());
                searchStatusLabel.setText("✅  Found " + items.size() + " " + categoryName + " for '" + q + "'");
            }));
        } else {
            searchStatusLabel.setText("Showing " + model.size() + " " + categoryName + " for '" + q + "'");
        }
    }

    /** Dark scroll pane wrapping a category list. */
    private JScrollPane makeTabScrollPane(JList<MediaItem> list) {
        JScrollPane sp = new JScrollPane(list);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBackground(new Color(5, 6, 9));
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(36);
        sp.getVerticalScrollBar().setBlockIncrement(140);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(255, 215, 0, 60);
                trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(null);
                return b;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(null);
                return b;
            }
        });
        return sp;
    }

    /** Updates a tab's label with a count badge. */
    private void updateTabCount(int tabIndex, String baseTitle, int count) {
        if (searchTabPane == null)
            return;
        Component c = searchTabPane.getTabComponentAt(tabIndex);
        if (c instanceof JLabel lbl) {
            lbl.setText(count > 0 ? baseTitle + " (" + count + ")" : baseTitle);
        }
    }

    /**
     * Creates a styled JList wired for double-click download and right-click menu.
     */
    private JList<MediaItem> makeCategoryList(DefaultListModel<MediaItem> model) {
        JList<MediaItem> list = new JList<>(model);
        list.setCellRenderer(new MediaItemCellRenderer());
        list.setOpaque(false);
        list.setBackground(new Color(0, 0, 0, 0));
        list.setSelectionBackground(new Color(250, 45, 85, 90));
        list.setFixedCellHeight(-1);

        // Context menu for search items
        JPopupMenu searchMenu = new JPopupMenu();
        searchMenu.setBackground(new Color(12, 15, 24));
        searchMenu.setBorder(new LineBorder(new Color(212, 160, 23, 80), 1));

        JMenuItem menuAddQueue = new JMenuItem("📥  Add to Download Queue");
        styleMenuItem(menuAddQueue, COL_GOLD);
        menuAddQueue.addActionListener(e -> {
            MediaItem sel = list.getSelectedValue();
            if (sel != null && !sel.getId().startsWith("loading-")) {
                queueDownload(sel);
            }
        });

        JMenuItem menuCopyUrl = new JMenuItem("🔗  Copy Apple Music Link");
        styleMenuItem(menuCopyUrl, COL_TEXT);
        menuCopyUrl.addActionListener(e -> {
            MediaItem sel = list.getSelectedValue();
            if (sel != null && sel.getUrl() != null) {
                java.awt.datatransfer.StringSelection ss = new java.awt.datatransfer.StringSelection(sel.getUrl());
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
                statusLabel.setText("Apple Music URL copied to clipboard.");
            }
        });

        JMenuItem menuSearchArtist = new JMenuItem("🔍  Search This Artist");
        styleMenuItem(menuSearchArtist, COL_CYAN);
        menuSearchArtist.addActionListener(e -> {
            MediaItem sel = list.getSelectedValue();
            if (sel != null && sel.getArtist() != null && !sel.getArtist().isEmpty()) {
                searchField.setText(sel.getArtist());
                handleSearch();
            }
        });

        JMenuItem menuViewAlbum = new JMenuItem("📖  View Album Tracks & Details");
        styleMenuItem(menuViewAlbum, COL_CYAN);
        menuViewAlbum.addActionListener(e -> {
            MediaItem sel = list.getSelectedValue();
            if (sel != null && sel.getType().equalsIgnoreCase("album")) {
                openAlbumDetailsDialog(sel);
            }
        });

        searchMenu.add(menuViewAlbum);
        searchMenu.add(menuAddQueue);
        searchMenu.add(menuCopyUrl);
        searchMenu.addSeparator();
        searchMenu.add(menuSearchArtist);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MediaItem sel = list.getSelectedValue();
                    if (sel != null && !sel.getId().startsWith("loading-")) {
                        if (sel.getType().equalsIgnoreCase("album")) {
                            openAlbumDetailsDialog(sel);
                        } else {
                            queueDownload(sel);
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    maybeShowMenu(e);
            }

            private void maybeShowMenu(MouseEvent e) {
                int idx = list.locationToIndex(e.getPoint());
                if (idx >= 0)
                    list.setSelectedIndex(idx);
                MediaItem sel = list.getSelectedValue();
                if (sel != null && !sel.getId().startsWith("loading-")) {
                    menuViewAlbum.setVisible(sel.getType().equalsIgnoreCase("album"));
                    searchMenu.show(list, e.getX(), e.getY());
                }
            }
        });
        return list;
    }

    private boolean isHostReachable(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty())
            return false;
        try {
            String cleanUrl = urlStr.trim();
            if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                cleanUrl = "http://" + cleanUrl;
            }
            java.net.URL url = java.net.URI.create(cleanUrl).toURL();
            String host = url.getHost();
            int port = url.getPort() != -1 ? url.getPort() : (cleanUrl.startsWith("https") ? 443 : 80);
            if (host == null || host.isEmpty())
                return false;

            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 350);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateAuthOrPrompt() {
        String method = configManager.getConfig().getApiMethod().getValue();
        String warningMsg = null;
        String title = null;

        if ("browser-cookies".equalsIgnoreCase(method)) {
            String bName = configManager.getConfig().getBrowserName();
            if (bName == null || bName.trim().isEmpty()) {
                warningMsg = "Your active authentication method is set to Automatic Browser Login, but no browser was selected.\n\nPlease select your browser (Chrome, Edge, Firefox, Brave, etc.) in Settings.";
                title = "AuraDL — Browser Selection Missing";
            }
        } else if ("cookies".equalsIgnoreCase(method) || "cookies-file".equalsIgnoreCase(method)) {
            String path = configManager.getConfig().getCookies();
            if (path == null || path.trim().isEmpty()) {
                warningMsg = "Your active authentication method is set to 'cookies-file', but no cookies.txt file path has been configured.\n\nPlease check your Cookies File setting.";
                title = "AuraDL — Missing Cookies Configuration";
            } else {
                File f = new File(path.trim());
                if (!f.exists()) {
                    warningMsg = "Your active authentication method is set to 'cookies-file', but the specified cookies.txt file was NOT found:\n\nPath: "
                            + path + "\n\nPlease check your cookies file location in Settings.";
                    title = "AuraDL — Cookies File Missing";
                } else if (f.length() == 0) {
                    warningMsg = "Your active authentication method is set to 'cookies-file', but the specified cookies.txt file is EMPTY (0 bytes):\n\nPath: "
                            + path + "\n\nPlease export a valid netscape cookies.txt from your browser session.";
                    title = "AuraDL — Cookies File Empty";
                }
            }
        } else if ("media-user-token".equalsIgnoreCase(method)) {
            String tok = configManager.getConfig().getMediaUserToken();
            if (tok == null || tok.trim().isEmpty()) {
                warningMsg = "Your active authentication method is set to 'media-user-token', but no Media User Token string has been provided.\n\nPlease enter your token in Settings.";
                title = "AuraDL — Missing Media User Token";
            }
        } else if ("wrapper".equalsIgnoreCase(method)) {
            String url = configManager.getConfig().getWrapperBaseUrl();
            if (url == null || url.trim().isEmpty()) {
                warningMsg = "Your active authentication method is set to 'wrapper', but no Wrapper URL has been configured.\n\nPlease check your Wrapper Base Proxy URL setting in Settings.";
                title = "AuraDL — Missing Wrapper Base URL";
            } else if (!isHostReachable(url)) {
                warningMsg = "Your active authentication method is set to 'wrapper', but AuraDL could NOT connect to the local wrapper server at:\n\n"
                        + url
                        + "\n\nPlease make sure your local wrapper service is running on localhost, or switch API Method to Automatic Browser Login in Settings.";
                title = "AuraDL — Wrapper Server Offline";
            }
        }

        if (warningMsg != null) {
            int option = JOptionPane.showOptionDialog(this,
                    warningMsg + "\n\nWould you like to open the Settings tab now to check and fix your configuration?",
                    title,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new Object[] { "⚙️ Open Settings", "Cancel" },
                    "⚙️ Open Settings");

            if (option == JOptionPane.YES_OPTION || option == 0) {
                if (mainTabbedPane != null) {
                    mainTabbedPane.setSelectedIndex(2); // Jump directly to Settings tab!
                }
            }
            return false;
        }
        return true;
    }

    private void queueDownload(MediaItem item) {
        if (!validateAuthOrPrompt())
            return;
        downloadManager.addToQueue(item.getUrl(), item);
        statusLabel.setText("📥  Queued: " + item.getTitle() + " — " + item.getArtist());
        searchStatusLabel.setText("Queued: " + item.getTitle());
    }

    // ─────────────────────────────────────────────────────────
    // Queue Tab
    // ─────────────────────────────────────────────────────────
    private JPanel buildQueueTab() {
        JPanel p = glassCard(16);
        p.setLayout(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(18, 18, 14, 18));

        // ── Top header row
        // ─────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(12, 0));
        titleRow.setOpaque(false);
        titleRow.add(sectionTitle("📥  Download Queue"), BorderLayout.WEST);

        // Stats pill: "N active · M done · P failed"
        queueStatsLabel = new JLabel("No downloads yet");
        queueStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        queueStatsLabel.setForeground(new Color(120, 135, 165));
        queueStatsLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        titleRow.add(queueStatsLabel, BorderLayout.CENTER);
        top.add(titleRow, BorderLayout.WEST);

        // Action buttons row
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);

        JButton openOutput = glowButton("📁 Open Folder", new Color(20, 28, 48), COL_GOLD);
        openOutput.addActionListener(e -> openDir(configManager.getConfig().getOutputDir()));

        JButton clrDone = glowButton("🧹 Clear Done", new Color(0, 80, 90), COL_CYAN);
        clrDone.addActionListener(e -> {
            downloadManager.clearCompleted();
            refreshQueueList();
            refreshQueueStats();
            statusLabel.setText("Cleared completed downloads.");
        });

        JButton retry = glowButton("🔄 Retry Failed", new Color(60, 20, 110), COL_PURPLE);
        retry.addActionListener(e -> {
            downloadManager.getTasks().stream().filter(t -> t.getStatus() == DownloadStatus.FAILED)
                    .forEach(t -> downloadManager.retryTask(t.getId()));
            statusLabel.setText("Retrying failed…");
        });

        JButton clrAll = glowButton("✕ Clear All", new Color(80, 12, 25), COL_RED);
        clrAll.addActionListener(e -> {
            downloadManager.clearQueue();
            queueListModel.clear();
            refreshQueueStats();
            if (logArea != null)
                logArea.setText("");
            statusLabel.setText("Queue cleared.");
        });

        btns.add(openOutput);
        btns.add(clrDone);
        btns.add(retry);
        btns.add(clrAll);
        top.add(btns, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // ── Queue list
        // ────────────────────────────────────────────────
        queueListModel = new DefaultListModel<>();
        JList<DownloadTask> qList = new JList<>(queueListModel);
        qList.setCellRenderer(new QueueTaskCellRenderer());
        qList.setOpaque(false);
        qList.setBackground(new Color(0, 0, 0, 0));
        qList.setFixedCellHeight(-1);
        qList.setSelectionBackground(new Color(255, 215, 0, 30));

        // Right-click context menu
        JPopupMenu qMenu = new JPopupMenu();
        qMenu.setBackground(new Color(12, 15, 24));
        qMenu.setBorder(new LineBorder(new Color(212, 160, 23, 80), 1));

        JMenuItem menuOpenFolder = new JMenuItem("📁  Open Output Folder");
        styleMenuItem(menuOpenFolder, COL_TEXT);
        menuOpenFolder.addActionListener(e -> {
            DownloadTask sel = qList.getSelectedValue();
            if (sel != null && sel.getFinalPath() != null) {
                File fp = new File(sel.getFinalPath());
                openDir(fp.isDirectory() ? fp.getAbsolutePath() : fp.getParent());
            } else {
                openDir(configManager.getConfig().getOutputDir());
            }
        });

        JMenuItem menuRetry = new JMenuItem("🔄  Retry Task");
        styleMenuItem(menuRetry, new Color(168, 85, 247));
        menuRetry.addActionListener(e -> {
            DownloadTask sel = qList.getSelectedValue();
            if (sel != null && sel.getStatus() == DownloadStatus.FAILED)
                downloadManager.retryTask(sel.getId());
        });

        JMenuItem menuCancel = new JMenuItem("✕  Cancel Task");
        styleMenuItem(menuCancel, new Color(250, 100, 120));
        menuCancel.addActionListener(e -> {
            DownloadTask sel = qList.getSelectedValue();
            if (sel != null)
                downloadManager.cancelTask(sel.getId());
        });

        JMenuItem menuCopyUrl = new JMenuItem("🔗  Copy URL");
        styleMenuItem(menuCopyUrl, COL_SUBTEXT);
        menuCopyUrl.addActionListener(e -> {
            DownloadTask sel = qList.getSelectedValue();
            if (sel != null) {
                java.awt.datatransfer.StringSelection ss = new java.awt.datatransfer.StringSelection(sel.getUrl());
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
                statusLabel.setText("URL copied to clipboard.");
            }
        });

        qMenu.add(menuOpenFolder);
        qMenu.add(menuRetry);
        qMenu.add(menuCancel);
        qMenu.addSeparator();
        qMenu.add(menuCopyUrl);

        qList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    maybeShowMenu(e);
            }

            private void maybeShowMenu(MouseEvent e) {
                int idx = qList.locationToIndex(e.getPoint());
                if (idx >= 0)
                    qList.setSelectedIndex(idx);
                qMenu.show(qList, e.getX(), e.getY());
            }
        });

        JScrollPane qScrollPane = darkScrollPane(qList);

        // ── Live log panel
        // ────────────────────────────────────────────
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setOpaque(true);
        logArea.setBackground(new Color(4, 5, 8));
        logArea.setForeground(new Color(0, 210, 120));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBorder(new EmptyBorder(8, 12, 8, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(false);
        logArea.setText("[AuraDL Engine] Ready. Waiting for downloads...\n");

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setOpaque(false);
        logScroll.getViewport().setOpaque(false);
        logScroll.setBorder(null);
        logScroll.getVerticalScrollBar().setUnitIncrement(16);
        logScroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0, 180, 100, 55);
                trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return zeroSizeBtn();
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return zeroSizeBtn();
            }

            private JButton zeroSizeBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(null);
                return b;
            }
        });

        // Log container with header bar
        JPanel logContainer = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(4, 5, 8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0, 160, 90, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        logContainer.setOpaque(false);

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        logHeader.setBorder(new EmptyBorder(7, 12, 6, 12));

        JLabel logTitle = new JLabel("⚡  Live Engine Output");
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        logTitle.setForeground(new Color(0, 200, 115));
        logHeader.add(logTitle, BorderLayout.WEST);

        JButton clearLog = new JButton("Clear log");
        clearLog.setFont(new Font("SansSerif", Font.PLAIN, 10));
        clearLog.setForeground(new Color(70, 90, 120));
        clearLog.setBackground(new Color(8, 10, 18));
        clearLog.setBorder(new CompoundBorder(
                new LineBorder(new Color(40, 55, 80), 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        clearLog.setFocusPainted(false);
        clearLog.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearLog.addActionListener(e -> logArea.setText(""));
        logHeader.add(clearLog, BorderLayout.EAST);

        logContainer.add(logHeader, BorderLayout.NORTH);
        logContainer.add(logScroll, BorderLayout.CENTER);

        // ── JSplitPane: queue on top / log at bottom
        // ─────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, qScrollPane, logContainer);
        split.setOpaque(false);
        split.setResizeWeight(0.68);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setContinuousLayout(true);
        split.setBackground(new Color(0, 0, 0, 0));
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(new Color(255, 215, 0, 30));
                        g.fillRect(0, getHeight() / 2 - 1, getWidth(), 2);
                    }
                };
            }
        });

        p.add(split, BorderLayout.CENTER);
        return p;
    }

    /** Style a JMenuItem for the dark queue context menu. */
    private void styleMenuItem(JMenuItem item, Color fg) {
        item.setBackground(new Color(12, 15, 24));
        item.setForeground(fg);
        item.setFont(new Font("SansSerif", Font.PLAIN, 12));
        item.setBorder(new EmptyBorder(6, 14, 6, 14));
    }

    // ─────────────────────────────────────────────────────────
    // Modern Animated Toggle Switch (Custom Swing Component)
    // ─────────────────────────────────────────────────────────
    private class ModernToggleSwitch extends JCheckBox {
        private final Color activeColor;

        public ModernToggleSwitch(String text, boolean selected, Color activeColor) {
            super(text, selected);
            this.activeColor = activeColor;
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setBorder(null);
            setFocusable(false);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setForeground(COL_TEXT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean sel = isSelected();
            boolean hover = getModel().isRollover();

            int h = getHeight();
            int switchW = 34;
            int switchH = 18;
            int switchY = (h - switchH) / 2;
            int switchX = 0;

            Color trackBg = sel
                    ? (hover ? activeColor.brighter() : activeColor)
                    : (hover ? new Color(45, 52, 75) : new Color(25, 30, 48));

            g2.setColor(trackBg);
            g2.fillRoundRect(switchX, switchY, switchW, switchH, switchH, switchH);

            g2.setColor(sel ? new Color(255, 255, 255, 90) : new Color(255, 255, 255, 30));
            g2.drawRoundRect(switchX, switchY, switchW - 1, switchH - 1, switchH, switchH);

            int thumbDiam = switchH - 4;
            int thumbY = switchY + 2;
            int thumbX = sel ? (switchX + switchW - thumbDiam - 2) : (switchX + 2);

            g2.setColor(Color.WHITE);
            g2.fillOval(thumbX, thumbY, thumbDiam, thumbDiam);

            g2.setFont(getFont());
            g2.setColor(hover ? Color.WHITE : COL_TEXT);
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), switchX + switchW + 12, textY);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Font font = getFont();
            if (font == null)
                font = new Font("SansSerif", Font.PLAIN, 12);
            FontMetrics fm = getFontMetrics(font);
            int textW = (fm != null) ? fm.stringWidth(getText()) : getText().length() * 9;
            int w = 34 + 12 + textW + 30;
            return new Dimension(w, 24);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    // Live Naming Preview Update Helper
    private void updateLiveNamingPreview() {
        if (livePreviewLabel == null)
            return;
        String defaultDir = new File(System.getProperty("user.dir"), "Apple Music").getAbsolutePath().replace('\\',
                '/');
        String outDir = outputDirField != null && !outputDirField.getText().trim().isEmpty()
                ? outputDirField.getText().trim()
                : defaultDir;
        String albumTpl = albumFolderTemplateField != null ? albumFolderTemplateField.getText().trim()
                : "{album_artist}/{album}";
        String songTpl = songFileTemplateField != null ? songFileTemplateField.getText().trim()
                : "{track_number} {title}";

        if (albumTpl.isEmpty())
            albumTpl = "{album_artist}/{album}";
        if (songTpl.isEmpty())
            songTpl = "{track_number} {title}";

        String folder = albumTpl
                .replace("{album_artist}", "The Weeknd")
                .replace("{album}", "After Hours")
                .replace("{year}", "2020")
                .replace("{genre}", "R&B/Soul");

        String file = songTpl
                .replace("{track_number}", "01")
                .replace("{title}", "Blinding Lights")
                .replace("{artist}", "The Weeknd")
                .replace("{album}", "After Hours");

        if (!outDir.endsWith("/") && !outDir.endsWith("\\"))
            outDir += "/";
        String fullPath = outDir + folder + "/" + file + ".m4a";
        fullPath = fullPath.replace("//", "/").replace("\\\\", "\\");

        livePreviewLabel.setText("<html><body style='font-family:sans-serif;font-size:12px;color:#00dcf0;'>"
                + "<span style='color:#8291b0;'>Live File Naming Preview: </span><b style='color:#a855f7;'>"
                + fullPath + "</b></body></html>");
    }

    private String checkAuthStatus(String method) {
        if ("cookies".equalsIgnoreCase(method) || "cookies-file".equalsIgnoreCase(method)
                || "browser-cookies".equalsIgnoreCase(method)) {
            String path = cookiesField != null ? cookiesField.getText().trim() : configManager.getConfig().getCookies();
            if (path.isEmpty())
                return "🔑 Auth Status: ⚠️ No Cookies Path";
            File f = new File(path);
            if (!f.exists())
                return "🔑 Auth Status: ❌ Cookies File Missing";
            if (f.length() == 0)
                return "🔑 Auth Status: ⚠️ Cookies File Empty";
            return "🔑 Auth Status: ✅ Cookies Ready";
        } else if ("media-user-token".equalsIgnoreCase(method)) {
            String tok = mediaUserTokenField != null ? mediaUserTokenField.getText().trim()
                    : configManager.getConfig().getMediaUserToken();
            if (tok.isEmpty())
                return "🔑 Auth Status: ⚠️ Token Empty";
            return "🔑 Auth Status: ✅ Token Ready";
        } else if ("wrapper".equalsIgnoreCase(method)) {
            String url = wrapperUrlField != null ? wrapperUrlField.getText().trim()
                    : configManager.getConfig().getWrapperBaseUrl();
            if (url.isEmpty())
                return "🔑 Auth Status: ⚠️ Wrapper URL Empty";
            boolean connected = isHostReachable(url);
            if (!connected)
                return "🔑 Auth Status: ❌ Wrapper Offline";
            return "🔑 Auth Status: ✅ Wrapper Connected";
        }
        return "🔑 Auth Status: ❓ Unknown Method";
    }

    private void updateHeroChips() {
        if (chipOutput == null)
            return;
        String out = outputDirField != null ? outputDirField.getText().trim()
                : configManager.getConfig().getOutputDir();
        String codec = songQualityCombo != null ? (String) songQualityCombo.getSelectedItem()
                : configManager.getConfig().getSongCodecPriority();
        String method = apiMethodCombo != null ? (String) apiMethodCombo.getSelectedItem()
                : configManager.getConfig().getApiMethod().getValue();
        String engine = downloadModeCombo != null ? (String) downloadModeCombo.getSelectedItem()
                : configManager.getConfig().getDownloadMode();

        chipOutput.setText("📁 Output: " + out);
        chipCodec.setText("🎧 Codec: " + codec);
        chipMethod.setText("🔑 Method: " + method);
        chipEngine.setText("⚡ Engine: " + engine);

        String authStatusText = checkAuthStatus(method);
        chipAuthStatus.setText(authStatusText);
    }

    // ─────────────────────────────────────────────────────────
    // SETTINGS TAB — redesigned with unconstrained right column
    // ─────────────────────────────────────────────────────────
    private JPanel buildSettingsTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── Top Hero Header Card
        // ───────────────────────────────────────
        JPanel hero = glassCard(16);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(18, 24, 18, 24));
        hero.setAlignmentX(LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(LEFT_ALIGNMENT);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel hTitle = new JLabel("⚙️  Settings & Configuration Dashboard");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        hTitle.setForeground(Color.WHITE);

        JLabel hSub = new JLabel(
                "Configure storage directories, audio quality priority, authentication credentials, file naming templates, and performance options.");
        hSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hSub.setForeground(COL_SUBTEXT);

        titlePanel.add(hTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(hSub);

        topRow.add(titlePanel, BorderLayout.CENTER);

        JPanel heroChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        heroChips.setOpaque(false);
        heroChips.setAlignmentX(LEFT_ALIGNMENT);

        chipOutput = qualityBadge("📁 Output: " + configManager.getConfig().getOutputDir(), COL_CYAN);
        chipCodec = qualityBadge("🎧 Codec: " + configManager.getConfig().getSongCodecPriority(), COL_RED);
        chipMethod = qualityBadge("🔑 Method: " + configManager.getConfig().getApiMethod().getValue(), COL_AMBER);
        chipEngine = qualityBadge("⚡ Engine: " + configManager.getConfig().getDownloadMode(), COL_PURPLE);
        chipAuthStatus = qualityBadge(checkAuthStatus(configManager.getConfig().getApiMethod().getValue()), COL_GREEN);

        heroChips.add(chipOutput);
        heroChips.add(chipCodec);
        heroChips.add(chipMethod);
        heroChips.add(chipEngine);
        heroChips.add(chipAuthStatus);

        JPanel qTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        qTools.setOpaque(false);
        qTools.setAlignmentX(LEFT_ALIGNMENT);
        JButton openOut = glowButton("📂 Open Storage Directory", COL_CYAN, Color.BLACK);
        openOut.addActionListener(e -> openDir(outputDirField.getText().trim()));
        JButton purgeT = glowButton("🧹 Clear Temp Cache", COL_PURPLE, Color.WHITE);
        purgeT.addActionListener(e -> purgeTempCache());
        JButton diag = glowButton("🧪 System Diagnostics", COL_AMBER, Color.BLACK);
        diag.addActionListener(e -> runDiagnostics());
        qTools.add(openOut);
        qTools.add(purgeT);
        qTools.add(diag);

        hero.add(topRow);
        hero.add(Box.createVerticalStrut(10));
        hero.add(heroChips);
        hero.add(Box.createVerticalStrut(10));
        hero.add(qTools);

        content.add(hero);
        content.add(Box.createVerticalStrut(14));

        // ─────────────────────────────────────────
        // Section 1 — Paths & Storage Locations
        // ─────────────────────────────────────────
        content.add(settingsSection("📁  Paths & Storage Locations", COL_CYAN, new JPanel[] {
                settingsRow(
                        "Output Directory",
                        "Where downloaded songs, albums and lyrics are stored (Default: /Apple Music/ in install folder)",
                        COL_CYAN,
                        () -> {
                            outputDirField = darkTextField("");
                            String defaultOut = configManager.getConfig().getOutputDir();
                            if (defaultOut == null || defaultOut.isEmpty() || defaultOut.equals("D:/Apple Music")) {
                                defaultOut = new File(System.getProperty("user.dir"), "Apple Music").getAbsolutePath()
                                        .replace('\\', '/');
                                configManager.getConfig().setOutputDir(defaultOut);
                            }
                            outputDirField.setText(defaultOut);
                            outputDirField.getDocument().addDocumentListener(new DocumentListener() {
                                public void insertUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void removeUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void changedUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }
                            });
                            JButton b = browseBtn();
                            b.addActionListener(e -> chooseDir("Output Directory", outputDirField));
                            return rowWithBrowse(outputDirField, b);
                        }),
                settingsRow(
                        "Temp Scratch Directory",
                        "Temporary workspace for decryption and remuxing (automatically cleared after completion)",
                        COL_CYAN,
                        () -> {
                            tempDirField = darkTextField("");
                            tempDirField.setText(configManager.getConfig().getTempDir());
                            JButton b = browseBtn();
                            b.addActionListener(e -> chooseDir("Temp Directory", tempDirField));
                            return rowWithBrowse(tempDirField, b);
                        }),
                settingsRow(
                        "FFmpeg Binary Executable",
                        "Required for audio remuxing, M4A container assembly, and embedded tag writing",
                        COL_CYAN,
                        () -> {
                            ffmpegField = darkTextField("path\\to\\ffmpeg.exe");
                            ffmpegField.setText(configManager.getConfig().getFfmpegPath());
                            JButton b = browseBtn();
                            b.addActionListener(e -> chooseFile("FFmpeg Executable", ffmpegField));
                            return rowWithBrowse(ffmpegField, b);
                        }),
                settingsRow(
                        "N_m3u8DL-RE Executable",
                        "Required for nm3u8dlre download mode — leave blank to use default yt-dlp engine",
                        COL_CYAN,
                        () -> {
                            nm3u8dlreField = darkTextField("path\\to\\N_m3u8DL-RE.exe");
                            nm3u8dlreField.setText(configManager.getConfig().getNm3u8dlrePath());
                            JButton b = browseBtn();
                            b.addActionListener(e -> chooseFile("N_m3u8DL-RE Executable", nm3u8dlreField));
                            return rowWithBrowse(nm3u8dlreField, b);
                        }),
        }));

        content.add(Box.createVerticalStrut(14));

        // ─────────────────────────────────────────
        // Section 2 — Audio Quality & Engine
        // ─────────────────────────────────────────
        content.add(settingsSection("🔊  Audio Quality & Engine Priority", COL_RED, new JPanel[] {
                settingsRow(
                        "Codec Priority Order",
                        "gamdl attempts codecs from left to right — ALAC delivers Apple Lossless quality",
                        COL_RED,
                        () -> {
                            songQualityCombo = darkCombo(new String[] {
                                    "alac,atmos,aac",
                                    "alac",
                                    "atmos",
                                    "aac",
                                    "aac-he"
                            });
                            songQualityCombo.setSelectedItem(configManager.getConfig().getSongCodecPriority());
                            songQualityCombo.addActionListener(e -> updateHeroChips());

                            JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                            badges.setOpaque(false);
                            String[] descs = { "ALAC = Apple Lossless", "Atmos = Spatial Audio", "AAC = Standard Lossy",
                                    "AAC-HE = Mobile Low-Bitrate" };
                            Color[] cols = { COL_CYAN, COL_PURPLE, COL_GREEN, COL_AMBER };
                            for (int i = 0; i < descs.length; i++) {
                                badges.add(qualityBadge(descs[i], cols[i]));
                            }

                            JPanel col = new JPanel();
                            col.setOpaque(false);
                            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
                            col.add(songQualityCombo);
                            col.add(Box.createVerticalStrut(8));
                            col.add(badges);
                            col.setPreferredSize(new Dimension(440, 68));
                            return col;
                        }),
                settingsRow(
                        "Download Engine Backend",
                        "yt-dlp engine is recommended for standard downloads; nm3u8dlre for raw HLS manifests",
                        COL_RED,
                        () -> {
                            downloadModeCombo = darkCombo(new String[] { "ytdlp", "nm3u8dlre" });
                            downloadModeCombo.setSelectedItem(configManager.getConfig().getDownloadMode());
                            downloadModeCombo.setPreferredSize(new Dimension(260, 38));
                            downloadModeCombo.addActionListener(e -> updateHeroChips());
                            return downloadModeCombo;
                        }),
                settingsRow(
                        "Music Video Max Resolution",
                        "Maximum video resolution cap for Apple Music Video downloads (2160p = 4K, 1080p = Full HD)",
                        COL_RED,
                        () -> {
                            videoResolutionCombo = darkCombo(new String[] { "2160p", "1080p", "720p", "480p", "360p" });
                            videoResolutionCombo.setSelectedItem(configManager.getConfig().getMusicVideoResolution());
                            videoResolutionCombo.setPreferredSize(new Dimension(260, 38));
                            videoResolutionCombo.addActionListener(e -> updateHeroChips());
                            return videoResolutionCombo;
                        }),
                settingsRow(
                        "Music Video Codec Priority",
                        "Codec priority order for Apple Music Videos (h264 for maximum compatibility, h265 for efficiency)",
                        COL_RED,
                        () -> {
                            videoCodecCombo = darkCombo(new String[] { "h264,h265", "h265,h264", "h264", "h265" });
                            videoCodecCombo.setSelectedItem(configManager.getConfig().getMusicVideoCodecPriority());
                            videoCodecCombo.setPreferredSize(new Dimension(260, 38));
                            videoCodecCombo.addActionListener(e -> updateHeroChips());
                            return videoCodecCombo;
                        }),
                settingsRow(
                        "Music Video Remux Format",
                        "Remux output container format for downloaded videos (MP4 or MKV)",
                        COL_RED,
                        () -> {
                            videoRemuxCombo = darkCombo(new String[] { "mp4", "mkv" });
                            videoRemuxCombo.setSelectedItem(configManager.getConfig().getMusicVideoRemuxFormat());
                            videoRemuxCombo.setPreferredSize(new Dimension(260, 38));
                            return videoRemuxCombo;
                        }),
        }));

        content.add(Box.createVerticalStrut(14));

        // ─────────────────────────────────────────
        // Section 3 — File Naming & Synced Lyrics
        // ─────────────────────────────────────────
        content.add(settingsSection("📂  File Naming & Synced Lyrics", COL_PURPLE, new JPanel[] {
                settingsRow(
                        "Live Destination Path Preview",
                        "Real-time preview showing how destination file paths are formatted on disk",
                        COL_PURPLE,
                        () -> {
                            JPanel previewCard = new JPanel(new BorderLayout());
                            previewCard.setOpaque(false);
                            previewCard.setBorder(new CompoundBorder(
                                    new LineBorder(new Color(168, 85, 247, 90), 1, true),
                                    new EmptyBorder(8, 12, 8, 12)));
                            previewCard.setBackground(new Color(10, 12, 24, 200));

                            livePreviewLabel = new JLabel();
                            updateLiveNamingPreview();
                            previewCard.add(livePreviewLabel, BorderLayout.CENTER);
                            previewCard.setPreferredSize(new Dimension(440, 44));
                            return previewCard;
                        }),
                settingsRow(
                        "Album Folder Template",
                        "Tokens define folder structure. Click chips below to append tokens:",
                        COL_PURPLE,
                        () -> {
                            albumFolderTemplateField = darkTextField("{album_artist}/{album}");
                            albumFolderTemplateField.setText(configManager.getConfig().getAlbumFolderTemplate());

                            JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                            chips.setOpaque(false);
                            chips.add(tokenBadge("{album_artist}", albumFolderTemplateField, COL_PURPLE));
                            chips.add(tokenBadge("{album}", albumFolderTemplateField, COL_CYAN));
                            chips.add(tokenBadge("{year}", albumFolderTemplateField, COL_AMBER));
                            chips.add(tokenBadge("{genre}", albumFolderTemplateField, COL_GREEN));

                            JPanel col = new JPanel();
                            col.setOpaque(false);
                            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
                            col.add(albumFolderTemplateField);
                            col.add(Box.createVerticalStrut(6));
                            col.add(chips);
                            col.setPreferredSize(new Dimension(440, 72));
                            return col;
                        }),
                settingsRow(
                        "Song File Template",
                        "Filename format for tracks. Click chips below to append tokens:",
                        COL_PURPLE,
                        () -> {
                            songFileTemplateField = darkTextField("{track_number} {title}");
                            songFileTemplateField.setText(configManager.getConfig().getSongFileTemplate());

                            JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                            chips.setOpaque(false);
                            chips.add(tokenBadge("{track_number}", songFileTemplateField, COL_PURPLE));
                            chips.add(tokenBadge("{title}", songFileTemplateField, COL_CYAN));
                            chips.add(tokenBadge("{artist}", songFileTemplateField, COL_AMBER));
                            chips.add(tokenBadge("{album}", songFileTemplateField, COL_GREEN));

                            JPanel col = new JPanel();
                            col.setOpaque(false);
                            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
                            col.add(songFileTemplateField);
                            col.add(Box.createVerticalStrut(6));
                            col.add(chips);
                            col.setPreferredSize(new Dimension(440, 72));
                            return col;
                        }),
                settingsRow(
                        "Synced Lyrics Format",
                        "LRC = timestamped karaoke format file. TXT = plain lyric text lines",
                        COL_PURPLE,
                        () -> {
                            lyricsFormatCombo = darkCombo(new String[] { "lrc", "txt" });
                            lyricsFormatCombo.setSelectedItem(configManager.getConfig().getSyncedLyricsFormat());
                            lyricsFormatCombo.setPreferredSize(new Dimension(260, 38));
                            return lyricsFormatCombo;
                        }),
                settingsRow(
                        "Metadata Storefront / Language",
                        "Language storefront code for track titles and artist names returned from Apple Music API",
                        COL_PURPLE,
                        () -> {
                            languageCombo = darkCombo(new String[] { "en-US", "es-ES", "ja-JP", "de-DE", "fr-FR",
                                    "pt-BR", "zh-CN", "ko-KR" });
                            languageCombo.setSelectedItem(configManager.getConfig().getLanguage());
                            languageCombo.setPreferredSize(new Dimension(260, 38));
                            return languageCombo;
                        }),
        }));

        content.add(Box.createVerticalStrut(14));

        // ─────────────────────────────────────────
        // Section 4 — Authentication Credentials Manager
        // ─────────────────────────────────────────
        content.add(settingsSection("🔑  Authentication Credentials Manager", COL_AMBER, new JPanel[] {
                settingsRow(
                        "Authentication Method",
                        "cookies-file = Netscape format cookies.txt file. media-user-token = Token string. wrapper = Proxy API",
                        COL_AMBER,
                        () -> {
                            apiMethodCombo = darkCombo(new String[] { "cookies-file", "media-user-token", "wrapper" });
                            apiMethodCombo.setSelectedItem(configManager.getConfig().getApiMethod().getValue());
                            apiMethodCombo.setPreferredSize(new Dimension(260, 38));
                            apiMethodCombo.addActionListener(e -> updateHeroChips());
                            return apiMethodCombo;
                        }),
                settingsRow(
                        "Manual Cookies File (.txt)",
                        "Netscape format cookies.txt file exported from browser",
                        COL_AMBER,
                        () -> {
                            cookiesField = darkTextField("path\\to\\cookies.txt");
                            cookiesField.setText(configManager.getConfig().getCookies());
                            cookiesField.getDocument().addDocumentListener(new DocumentListener() {
                                public void insertUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void removeUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void changedUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }
                            });
                            JButton b = browseBtn();
                            b.addActionListener(e -> chooseFile("Cookies File (.txt)", cookiesField));
                            return rowWithBrowse(cookiesField, b);
                        }),
                settingsRow(
                        "Media User Token",
                        "Paste token string directly or click Browse to select a file containing your token",
                        COL_AMBER,
                        () -> {
                            mediaUserTokenField = darkTextField(
                                    "Paste media user token string or select token file…");
                            mediaUserTokenField.setText(configManager.getConfig().getMediaUserToken());
                            mediaUserTokenField.getDocument().addDocumentListener(new DocumentListener() {
                                public void insertUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void removeUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void changedUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }
                            });
                            JButton b = browseBtn();
                            b.addActionListener(e -> chooseTokenFile("Media User Token File", mediaUserTokenField));
                            return rowWithBrowse(mediaUserTokenField, b);
                        }),
                settingsRow(
                        "Wrapper Base Proxy URL",
                        "Local API proxy endpoint URL (only used when Auth Method = wrapper)",
                        COL_AMBER,
                        () -> {
                            wrapperUrlField = darkTextField("http://localhost:10020");
                            wrapperUrlField.setText(configManager.getConfig().getWrapperBaseUrl());
                            wrapperUrlField.setPreferredSize(new Dimension(360, 38));
                            wrapperUrlField.getDocument().addDocumentListener(new DocumentListener() {
                                public void insertUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void removeUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }

                                public void changedUpdate(DocumentEvent e) {
                                    updateHeroChips();
                                }
                            });
                            return wrapperUrlField;
                        }),
        }));

        content.add(Box.createVerticalStrut(14));

        // ─────────────────────────────────────────
        // Section 5 — Artwork & Performance
        // ─────────────────────────────────────────
        content.add(settingsSection("🖼️  Artwork & Download Toggles", COL_GREEN, new JPanel[] {
                settingsRow(
                        "Cover Art Resolution",
                        "Maximum dimensions (px) of the embedded album artwork image (3000px max)",
                        COL_GREEN,
                        () -> {
                            coverSizeCombo = darkCombo(new Integer[] { 3000, 1200, 600, 300 });
                            coverSizeCombo.setSelectedItem(configManager.getConfig().getCoverSize());
                            coverSizeCombo.setPreferredSize(new Dimension(260, 38));
                            return coverSizeCombo;
                        }),
                settingsRow(
                        "Cover Art Format",
                        "JPEG provides small file size; PNG retains uncompressed original image data",
                        COL_GREEN,
                        () -> {
                            coverFormatCombo = darkCombo(new String[] { "jpg", "png" });
                            coverFormatCombo.setSelectedItem(configManager.getConfig().getCoverFormat());
                            coverFormatCombo.setPreferredSize(new Dimension(260, 38));
                            return coverFormatCombo;
                        }),
                settingsRow(
                        "Max Concurrent Downloads",
                        "Number of tracks downloaded simultaneously in parallel worker threads (1–10)",
                        COL_GREEN,
                        () -> {
                            concurrentSpinner = darkSpinner(new SpinnerNumberModel(
                                    configManager.getConfig().getMaxConcurrentDownloads(), 1, 10, 1));
                            concurrentSpinner.setPreferredSize(new Dimension(160, 38));
                            return concurrentSpinner;
                        }),
                settingsRow(
                        "Download Toggle Flags",
                        "Fine-grained flags passed directly to the underlying download engine",
                        COL_GREEN,
                        () -> {
                            saveCoverCheckBox = new ModernToggleSwitch("Save standalone cover art",
                                    configManager.getConfig().isSaveCover(), COL_GREEN);
                            savePlaylistCheckBox = new ModernToggleSwitch("Save playlist M3U8 file",
                                    configManager.getConfig().isSavePlaylist(), COL_CYAN);
                            overwriteCheckBox = new ModernToggleSwitch("Overwrite existing files",
                                    configManager.getConfig().isOverwrite(), COL_RED);

                            JPanel col = new JPanel();
                            col.setOpaque(false);
                            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
                            saveCoverCheckBox.setAlignmentX(LEFT_ALIGNMENT);
                            savePlaylistCheckBox.setAlignmentX(LEFT_ALIGNMENT);
                            overwriteCheckBox.setAlignmentX(LEFT_ALIGNMENT);
                            col.add(saveCoverCheckBox);
                            col.add(Box.createVerticalStrut(6));
                            col.add(savePlaylistCheckBox);
                            col.add(Box.createVerticalStrut(6));
                            col.add(overwriteCheckBox);
                            col.setPreferredSize(new Dimension(260, 84));
                            return col;
                        }),
        }));

        content.add(Box.createVerticalStrut(16));

        // ─────────────────────────────────────────
        // Footer — Save / Reset
        // ─────────────────────────────────────────
        JPanel footer = glassCard(12);
        footer.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 12));
        footer.setAlignmentX(LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JButton save = glowButton("💾  Save Configuration", COL_RED, Color.WHITE);
        save.setFont(new Font("SansSerif", Font.BOLD, 14));
        save.setPreferredSize(new Dimension(220, 42));
        save.addActionListener(e -> saveSettings());

        JButton reset = glowButton("🔄  Reset Defaults", new Color(40, 45, 60), COL_SUBTEXT);
        reset.setFont(new Font("SansSerif", Font.PLAIN, 13));
        reset.setPreferredSize(new Dimension(170, 42));
        reset.addActionListener(e -> resetDefaults());

        footer.add(save);
        footer.add(reset);
        content.add(footer);
        content.add(Box.createVerticalStrut(12));

        JScrollPane sp = new JScrollPane(content);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(36);
        sp.getVerticalScrollBar().setBlockIncrement(140);
        sp.addMouseWheelListener(e -> {
            JScrollBar sb = sp.getVerticalScrollBar();
            sb.setValue(sb.getValue() + e.getUnitsToScroll() * 10);
        });
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(255, 255, 255, 50);
                trackColor = new Color(255, 255, 255, 10);
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return zeroBtn();
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return zeroBtn();
            }

            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(null);
                return b;
            }
        });

        wrapper.add(sp, BorderLayout.CENTER);

        // Initial update for chips status
        updateHeroChips();

        return wrapper;
    }

    // ─────────────────────────────────────────────────────────
    // Settings helpers — BorderLayout ensures EAST keeps 100% preferred width
    // ─────────────────────────────────────────────────────────

    /** A labelled section card */
    private JPanel settingsSection(String title, Color accent, JPanel[] rows) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark glass card background
                g2.setColor(COL_SECTION);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Left vertical accent pillar
                g2.setPaint(new GradientPaint(0, 0, accent, 0, getHeight(),
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70)));
                g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);

                // Glass border
                g2.setColor(COL_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setBorder(new EmptyBorder(18, 24, 16, 24));

        // Section header
        JLabel hdr = new JLabel(title);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 15));
        hdr.setForeground(accent);
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        hdr.setBorder(new EmptyBorder(0, 4, 10, 0));
        card.add(hdr);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55));
        sep.setBackground(new Color(0, 0, 0, 0));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(10));

        for (int i = 0; i < rows.length; i++) {
            rows[i].setAlignmentX(LEFT_ALIGNMENT);
            card.add(rows[i]);
            if (i < rows.length - 1) {
                JSeparator rowSep = new JSeparator();
                rowSep.setForeground(new Color(255, 255, 255, 18));
                rowSep.setBackground(new Color(0, 0, 0, 0));
                rowSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                rowSep.setAlignmentX(LEFT_ALIGNMENT);
                card.add(Box.createVerticalStrut(4));
                card.add(rowSep);
                card.add(Box.createVerticalStrut(4));
            }
        }
        return card;
    }

    private JPanel settingsRow(String label, String desc, Color accent, ControlSupplier cs) {
        JPanel row = new JPanel(new BorderLayout(24, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 6, 12, 6));

        // ── Left column: label + description ──
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(COL_TEXT);
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><body style='color:#94a3b8;font-size:11px;line-height:1.3;'>"
                + desc + "</body></html>");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(COL_SUBTEXT);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        left.add(lbl);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        // ── Right column: control in EAST keeps 100% full unconstrained preferred
        // width ─────
        JComponent ctrl = cs.build();
        if (ctrl instanceof JPanel) {
            ctrl.setOpaque(false);
        }

        JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightWrapper.setOpaque(false);
        rightWrapper.add(ctrl);

        row.add(left, BorderLayout.CENTER);
        row.add(rightWrapper, BorderLayout.EAST);

        return row;
    }

    @FunctionalInterface
    interface ControlSupplier {
        JComponent build();
    }

    private JPanel rowWithBrowse(JTextField field, JButton browseBtn) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        p.add(browseBtn, BorderLayout.EAST);
        p.setPreferredSize(new Dimension(440, 38));
        p.setMinimumSize(new Dimension(320, 38));
        return p;
    }

    private JLabel qualityBadge(String text, Color c) {
        JLabel lbl = new JLabel("  " + text + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(c);
        lbl.setBorder(new EmptyBorder(3, 0, 3, 0));
        lbl.setOpaque(false);
        return lbl;
    }

    private JLabel tokenBadge(String token, JTextField targetField, Color c) {
        JLabel lbl = new JLabel(" " + token + " ") {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (targetField != null) {
                            String cur = targetField.getText();
                            targetField.setText(cur + (cur.isEmpty() || cur.endsWith("/") ? "" : " ") + token);
                            targetField.requestFocus();
                            updateLiveNamingPreview();
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hover ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 80)
                        : new Color(c.getRed(), c.getGreen(), c.getBlue(), 35);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), hover ? 220 : 140));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(c);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbl.setToolTipText("Click to add " + token + " to template");
        lbl.setBorder(new EmptyBorder(4, 6, 4, 6));
        lbl.setOpaque(false);
        return lbl;
    }

    private JButton browseBtn() {
        JButton b = glowButton("Browse…", new Color(25, 32, 52), COL_CYAN);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(100, 38));
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        return b;
    }

    // ─────────────────────────────────────────────────────────
    // Status bar
    // ─────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = glassCard(0);
        bar.setLayout(new BorderLayout());
        bar.setBorder(new EmptyBorder(7, 16, 7, 16));

        statusLabel = new JLabel("Ready  ·  Output: " + configManager.getConfig().getOutputDir());
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(COL_SUBTEXT);
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel ver = new JLabel("AuraDL v1.0  ·  Java Edition");
        ver.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ver.setForeground(new Color(80, 90, 115));
        bar.add(ver, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    // Business logic
    // ─────────────────────────────────────────────────────────
    private void handleSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty())
            return;

        if (!validateAuthOrPrompt())
            return;

        // ── URL resolution
        // ───────────────────────────────────────────────
        if (AppleMusicApiClient.isAppleMusicUrl(q)) {
            searchResultsModel.clear();
            songsModel.clear();
            albumsModel.clear();
            videosModel.clear();
            updateTabCount(0, "🎵  Songs", 0);
            updateTabCount(1, "💽  Albums", 0);
            updateTabCount(2, "🎬  Music Videos", 0);
            searchStatusLabel.setText("⏳  Resolving Apple Music URL…");
            statusLabel.setText("Resolving: " + q);

            apiClient.resolveUrl(q).thenAccept(item -> SwingUtilities.invokeLater(() -> {
                searchField.setText("");
                if (item != null) {
                    searchResultsModel.clear();
                    searchResultsModel.addElement(item);
                    String t = item.getType() != null ? item.getType().toLowerCase() : "song";
                    if (t.contains("album")) {
                        albumsModel.addElement(item);
                        updateTabCount(1, "💽  Albums", 1);
                        if (searchTabPane != null)
                            searchTabPane.setSelectedIndex(1);
                    } else if (t.contains("video")) {
                        videosModel.addElement(item);
                        updateTabCount(2, "🎬  Music Videos", 1);
                        if (searchTabPane != null)
                            searchTabPane.setSelectedIndex(2);
                    } else {
                        songsModel.addElement(item);
                        updateTabCount(0, "🎵  Songs", 1);
                        if (searchTabPane != null)
                            searchTabPane.setSelectedIndex(0);
                    }
                    searchStatusLabel.setText(
                            "✅  Resolved: " + item.getTitle()
                                    + "  ·  Click 📥 Download or double-click to add to queue");
                    statusLabel.setText("Resolved: " + item.getTitle() + " — " + item.getArtist());
                } else {
                    searchStatusLabel.setText("❌  Could not resolve URL — check your internet connection");
                }
            }));
            return;
        }

        // ── Explicit typed search: 3 parallel API calls
        // ─────────────────
        searchResultsModel.clear();
        songsModel.clear();
        albumsModel.clear();
        videosModel.clear();

        // Loading placeholders in each tab list
        songsModel.addElement(new MediaItem("loading-s", "⏳  Searching songs…", "Please wait", "", "song", 0, ""));
        albumsModel
                .addElement(new MediaItem("loading-a", "⏳  Searching albums…", "Please wait", "", "album", 0, ""));
        videosModel.addElement(
                new MediaItem("loading-v", "⏳  Searching music videos…", "Please wait", "", "video", 0, ""));

        updateTabCount(0, "🎵  Songs", 0);
        updateTabCount(1, "💽  Albums", 0);
        updateTabCount(2, "🎬  Music Videos", 0);

        searchStatusLabel.setText("🔍  Searching songs, albums & videos for '" + q + "'...");
        statusLabel.setText("Searching…");

        // ── Songs
        // ────────────────────────────────────────────────────────
        apiClient.searchByType(q, "song").thenAccept(items -> SwingUtilities.invokeLater(() -> {
            songsModel.clear();
            items.forEach(songsModel::addElement);
            updateTabCount(0, "🎵  Songs", items.size());
            updateSearchStatusSummary(q);
        }));

        // ── Albums
        // ───────────────────────────────────────────────────────
        apiClient.searchByType(q, "album").thenAccept(items -> SwingUtilities.invokeLater(() -> {
            albumsModel.clear();
            items.forEach(albumsModel::addElement);
            updateTabCount(1, "💽  Albums", items.size());
            updateSearchStatusSummary(q);
        }));

        // ── Music Videos
        // ─────────────────────────────────────────────────
        apiClient.searchByType(q, "musicVideo").thenAccept(items -> SwingUtilities.invokeLater(() -> {
            videosModel.clear();
            items.forEach(videosModel::addElement);
            updateTabCount(2, "🎬  Music Videos", items.size());
            updateSearchStatusSummary(q);
        }));
    }

    /**
     * Refreshes the search status bar after any of the three parallel searches
     * complete.
     */
    private void updateSearchStatusSummary(String q) {
        // Only update if all loading placeholders are gone
        boolean songsLoading = songsModel.size() == 1 && "loading-s".equals(songsModel.get(0).getId());
        boolean albumsLoading = albumsModel.size() == 1 && "loading-a".equals(albumsModel.get(0).getId());
        boolean videosLoading = videosModel.size() == 1 && "loading-v".equals(videosModel.get(0).getId());
        if (songsLoading || albumsLoading || videosLoading)
            return; // still waiting

        int songs = songsModel.size();
        int albums = albumsModel.size();
        int videos = videosModel.size();
        int total = songs + albums + videos;

        if (total == 0) {
            searchStatusLabel.setText("No results found for '" + q + "'");
            statusLabel.setText("No results.");
        } else {
            searchStatusLabel.setText("Found " + total + " results  ·  "
                    + songs + " songs  ·  " + albums + " albums  ·  " + videos + " music videos");
            statusLabel.setText("Found " + total + " results for '" + q + "'");
        }
    }

    private void saveSettings() {
        configManager.getConfig().setOutputDir(outputDirField.getText().trim());
        configManager.getConfig().setTempDir(tempDirField.getText().trim());
        configManager.getConfig().setSongCodecPriority((String) songQualityCombo.getSelectedItem());
        configManager.getConfig().setAlbumFolderTemplate(albumFolderTemplateField.getText().trim());
        configManager.getConfig().setSongFileTemplate(songFileTemplateField.getText().trim());
        configManager.getConfig().setNm3u8dlrePath(nm3u8dlreField.getText().trim());
        configManager.getConfig().setFfmpegPath(ffmpegField.getText().trim());
        configManager.getConfig().setCookies(cookiesField.getText().trim());
        if (mediaUserTokenField != null) {
            configManager.getConfig().setMediaUserToken(mediaUserTokenField.getText().trim());
        }
        if (videoResolutionCombo != null) {
            configManager.getConfig().setMusicVideoResolution((String) videoResolutionCombo.getSelectedItem());
        }
        if (videoCodecCombo != null) {
            configManager.getConfig().setMusicVideoCodecPriority((String) videoCodecCombo.getSelectedItem());
        }
        if (videoRemuxCombo != null) {
            configManager.getConfig().setMusicVideoRemuxFormat((String) videoRemuxCombo.getSelectedItem());
        }
        configManager.getConfig().setApiMethod(ApiMethod.fromString((String) apiMethodCombo.getSelectedItem()));
        configManager.getConfig().setDownloadMode((String) downloadModeCombo.getSelectedItem());
        configManager.getConfig().setLanguage((String) languageCombo.getSelectedItem());
        configManager.getConfig().setMaxConcurrentDownloads((Integer) concurrentSpinner.getValue());
        configManager.getConfig().setCoverSize((Integer) coverSizeCombo.getSelectedItem());
        configManager.getConfig().setCoverFormat((String) coverFormatCombo.getSelectedItem());
        configManager.getConfig().setSyncedLyricsFormat((String) lyricsFormatCombo.getSelectedItem());
        configManager.getConfig().setSaveCover(saveCoverCheckBox.isSelected());
        configManager.getConfig().setSavePlaylist(savePlaylistCheckBox.isSelected());
        configManager.getConfig().setOverwrite(overwriteCheckBox.isSelected());
        configManager.getConfig().setWrapperBaseUrl(wrapperUrlField.getText().trim());
        configManager.saveConfig();

        updateHeroChips();

        statusLabel.setText("✓  Settings saved  ·  Output: " + configManager.getConfig().getOutputDir());
        JOptionPane.showMessageDialog(this,
                "Configuration saved successfully!\n\nOutput: " + configManager.getConfig().getOutputDir() +
                        "\nCodec: " + configManager.getConfig().getSongCodecPriority() +
                        "\nVideo Quality: " + configManager.getConfig().getMusicVideoResolution() + " ("
                        + configManager.getConfig().getMusicVideoCodecPriority() + ")" +
                        "\nAuth Status: " + checkAuthStatus(configManager.getConfig().getApiMethod().getValue()),
                "AuraDL — Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetDefaults() {
        int c = JOptionPane.showConfirmDialog(this, "Reset all settings to defaults?", "Reset Settings",
                JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION)
            return;
        String defaultDir = new File(System.getProperty("user.dir"), "Apple Music").getAbsolutePath().replace('\\',
                '/');
        outputDirField.setText(defaultDir);
        tempDirField.setText(".");
        songQualityCombo.setSelectedItem("aac-web,aac-he-web,aac,alac,atmos");
        if (videoResolutionCombo != null)
            videoResolutionCombo.setSelectedItem("1080p");
        if (videoCodecCombo != null)
            videoCodecCombo.setSelectedItem("h264,h265");
        if (videoRemuxCombo != null)
            videoRemuxCombo.setSelectedItem("mp4");
        albumFolderTemplateField.setText("{album_artist}/{album}");
        songFileTemplateField.setText("{track_number} {title}");
        nm3u8dlreField.setText("N_m3u8DL-RE.exe");
        ffmpegField.setText("C:/Added To Path/ffmpeg-8.1.1-essentials_build/bin/ffmpeg.exe");
        cookiesField.setText("./cookies.txt");
        if (mediaUserTokenField != null) {
            mediaUserTokenField.setText("");
        }
        apiMethodCombo.setSelectedItem("cookies-file");
        downloadModeCombo.setSelectedItem("ytdlp");
        languageCombo.setSelectedItem("en-US");
        concurrentSpinner.setValue(3);
        coverSizeCombo.setSelectedItem(1200);
        coverFormatCombo.setSelectedItem("jpg");
        lyricsFormatCombo.setSelectedItem("lrc");
        saveCoverCheckBox.setSelected(true);
        savePlaylistCheckBox.setSelected(false);
        overwriteCheckBox.setSelected(false);
        wrapperUrlField.setText("http://localhost");
        saveSettings();
    }

    private void openDir(String path) {
        try {
            File d = new File(path);
            if (!d.exists())
                d.mkdirs();
            Desktop.getDesktop().open(d);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot open: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void purgeTempCache() {
        File d = new File(tempDirField.getText().trim());
        if (!d.exists() || !d.isDirectory()) {
            statusLabel.setText("Temp dir not found.");
            return;
        }
        File[] files = d.listFiles();
        int del = 0;
        if (files != null)
            for (File f : files)
                if (f.isFile() && f.delete())
                    del++;
        statusLabel.setText("Purged " + del + " temp files.");
        JOptionPane.showMessageDialog(this, "Purged " + del + " temporary files.", "Purge Temp",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void runDiagnostics() {
        String py = "C:\\Users\\AMAN\\AppData\\Local\\Programs\\Python\\Python314\\python.exe";
        String sb = "🧪  AuraDL — System Diagnostics\n" +
                "─────────────────────────────────────────\n"
                +
                "Python 3.14    : " + check(py) + "\n" +
                "FFmpeg         : " + check(ffmpegField.getText().trim()) + "\n" +
                "N_m3u8DL-RE    : " + check(nm3u8dlreField.getText().trim()) + "  (optional)\n" +
                "yt-dlp         : "
                + check("C:\\Users\\AMAN\\AppData\\Local\\Programs\\Python\\Python314\\Scripts\\yt-dlp.exe") + "\n" +
                "Cookies file   : " + check(cookiesField.getText().trim()) + "\n" +
                "Output dir     : "
                + (new File(outputDirField.getText().trim()).exists() ? "✅ exists" : "⚠ will be created") + "\n" +
                "─────────────────────────────────────────\n"
                +
                "Codec priority : " + configManager.getConfig().getSongCodecPriority() + "\n" +
                "Download mode  : " + configManager.getConfig().getDownloadMode() + "\n" +
                "Auth Status    : " + checkAuthStatus(configManager.getConfig().getApiMethod().getValue());
        JOptionPane.showMessageDialog(this, sb, "AuraDL — Diagnostics", JOptionPane.INFORMATION_MESSAGE);
    }

    private String check(String p) {
        return new File(p).exists() ? "✅  " + p : "❌  NOT FOUND";
    }

    private void chooseDir(String title, JTextField field) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (!field.getText().isEmpty())
            fc.setCurrentDirectory(new File(field.getText()));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(fc.getSelectedFile().getAbsolutePath().replace('\\', '/'));
            updateLiveNamingPreview();
            updateHeroChips();
        }
    }

    private void chooseFile(String title, JTextField field) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (!field.getText().isEmpty()) {
            File f = new File(field.getText()).getParentFile();
            if (f != null)
                fc.setCurrentDirectory(f);
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(fc.getSelectedFile().getAbsolutePath().replace('\\', '/'));
            updateLiveNamingPreview();
            updateHeroChips();
        }
    }

    private void chooseTokenFile(String title, JTextField field) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (!field.getText().isEmpty()) {
            File f = new File(field.getText()).getParentFile();
            if (f != null && f.exists())
                fc.setCurrentDirectory(f);
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            try {
                String tokenStr = java.nio.file.Files.readString(selectedFile.toPath()).trim();
                field.setText(tokenStr);
            } catch (Exception ex) {
                field.setText(selectedFile.getAbsolutePath().replace('\\', '/'));
            }
            updateHeroChips();
        }
    }

    private void refreshQueueList() {
        queueListModel.clear();
        downloadManager.getTasks().forEach(queueListModel::addElement);
    }

    private void updateQueueItem(DownloadTask t) {
        for (int i = 0; i < queueListModel.getSize(); i++) {
            if (queueListModel.getElementAt(i).getId().equals(t.getId())) {
                queueListModel.set(i, t);
                return;
            }
        }
        queueListModel.addElement(t);
    }

    private void refreshQueueStats() {
        if (queueStatsLabel == null)
            return;
        java.util.List<com.auradl.download.DownloadTask> tasks = downloadManager.getTasks();
        long active = tasks.stream().filter(t -> t.getStatus() == DownloadStatus.DOWNLOADING
                || t.getStatus() == DownloadStatus.FETCHING
                || t.getStatus() == DownloadStatus.DECRYPTING
                || t.getStatus() == DownloadStatus.EXTRACTING
                || t.getStatus() == DownloadStatus.SAVING_TAGS
                || t.getStatus() == DownloadStatus.PENDING_FETCHING).count();
        long done = tasks.stream().filter(t -> t.getStatus() == DownloadStatus.COMPLETED).count();
        long failed = tasks.stream().filter(t -> t.getStatus() == DownloadStatus.FAILED).count();
        if (tasks.isEmpty()) {
            queueStatsLabel.setText("No downloads yet");
            queueStatsLabel.setForeground(new Color(80, 95, 125));
        } else {
            String txt = active + " active  ·  " + done + " done  ·  " + failed + " failed";
            queueStatsLabel.setText(txt);
            queueStatsLabel.setForeground(
                    active > 0 ? new Color(0, 210, 120)
                            : failed > 0 ? new Color(250, 90, 110)
                                    : new Color(100, 200, 140));
        }
    }

    /**
     * Append a line to the live log textarea, auto-scroll, and cap at 500 lines.
     */
    private void appendLog(String line) {
        if (logArea == null)
            return;
        logArea.append(line + "\n");
        // Trim to 500 lines to prevent memory bloat
        int lineCount = logArea.getLineCount();
        if (lineCount > 500) {
            try {
                int end = logArea.getLineEndOffset(lineCount - 500);
                logArea.replaceRange("", 0, end);
            } catch (Exception ignored) {
            }
        }
        // Auto-scroll to bottom
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // ─────────────────────────────────────────────────────────
    // Design primitives
    // ─────────────────────────────────────────────────────────
    private JPanel glassCard(int arc) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc > 0 ? arc : 18, arc > 0 ? arc : 18);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc > 0 ? arc : 18, arc > 0 ? arc : 18);
                g2.setColor(new Color(255, 255, 255, 24));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc > 0 ? arc : 18, arc > 0 ? arc : 18);
                g2.dispose();
            }
        };
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JTextField darkTextField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBackground(new Color(6, 8, 14, 250));
        f.setForeground(COL_TEXT);
        f.setCaretColor(COL_GOLD);
        f.setPreferredSize(new Dimension(340, 38));
        f.setBorder(new CompoundBorder(
                new LineBorder(COL_BORDER_GOLD, 1, true),
                new EmptyBorder(7, 12, 7, 12)));
        f.setToolTipText(placeholder);
        f.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateLiveNamingPreview();
            }

            public void removeUpdate(DocumentEvent e) {
                updateLiveNamingPreview();
            }

            public void changedUpdate(DocumentEvent e) {
                updateLiveNamingPreview();
            }
        });
        return f;
    }

    private <T> JComboBox<T> darkCombo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(new Color(6, 8, 14));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(COL_GOLD);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                        g2.drawString("▼", getWidth() / 2 - 4, getHeight() / 2 + 4);
                        g2.dispose();
                    }
                };
                b.setBorder(null);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                return b;
            }
        });
        c.setBackground(new Color(6, 8, 14));
        c.setForeground(COL_TEXT);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setPreferredSize(new Dimension(260, 38));
        c.setBorder(new CompoundBorder(
                new LineBorder(COL_BORDER_GOLD, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v, int i, boolean sel, boolean foc) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, v, i, sel, foc);
                lbl.setBorder(new EmptyBorder(8, 12, 8, 12));
                lbl.setBackground(sel ? new Color(212, 160, 23, 220) : new Color(6, 8, 14));
                lbl.setForeground(sel ? Color.BLACK : COL_TEXT);
                return lbl;
            }
        });
        return c;
    }

    private JSpinner darkSpinner(SpinnerModel model) {
        JSpinner s = new JSpinner(model);
        s.setFont(new Font("SansSerif", Font.PLAIN, 13));
        s.setBackground(new Color(6, 8, 14));
        s.setForeground(COL_TEXT);
        s.setPreferredSize(new Dimension(160, 38));
        s.setBorder(
                new CompoundBorder(new LineBorder(COL_BORDER_GOLD, 1, true), new EmptyBorder(4, 8, 4, 8)));
        JComponent ed = s.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
            tf.setBackground(new Color(6, 8, 14));
            tf.setForeground(COL_TEXT);
            tf.setCaretColor(COL_GOLD);
            tf.setBorder(null);
        }
        return s;
    }

    private JButton glowButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean pressed = getModel().isPressed();
                boolean hover = getModel().isRollover();

                int w = getWidth();
                int h = getHeight();
                int yOffset = pressed ? 2 : 0;

                if (hover && !pressed) {
                    g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 60));
                    g2.fillRoundRect(0, 0, w, h, 14, 14);
                }

                Color topCol = hover ? bg.brighter() : bg;
                Color botCol = hover ? bg : bg.darker();
                if (pressed) {
                    topCol = bg.darker();
                    botCol = bg.darker().darker();
                }

                g2.setPaint(new GradientPaint(0, yOffset, topCol, 0, h, botCol));
                g2.fillRoundRect(0, yOffset, w, h - yOffset, 12, 12);

                g2.setColor(new Color(255, 255, 255, hover ? 70 : 35));
                g2.drawRoundRect(1, 1 + yOffset, w - 3, h - 3 - yOffset, 10, 10);

                Color borderCol = hover ? new Color(255, 255, 255, 120) : new Color(255, 255, 255, 40);
                g2.setColor(borderCol);
                g2.drawRoundRect(0, yOffset, w - 1, h - 1 - yOffset, 12, 12);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(6, 16, 6, 16));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JScrollPane darkScrollPane(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(36);
        sp.getVerticalScrollBar().setBlockIncrement(140);
        sp.addMouseWheelListener(e -> {
            JScrollBar sb = sp.getVerticalScrollBar();
            sb.setValue(sb.getValue() + e.getUnitsToScroll() * 10);
        });
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(255, 255, 255, 45);
                trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return nb();
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return nb();
            }

            private JButton nb() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(null);
                return b;
            }
        });
        return sp;
    }

    // ─────────────────────────────────────────────────────────
    // Async Artwork Cache
    // ─────────────────────────────────────────────────────────
    private static final java.util.concurrent.ConcurrentHashMap<String, ImageIcon> ARTWORK_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final ImageIcon ARTWORK_PLACEHOLDER = buildPlaceholderIcon();

    private static ImageIcon buildPlaceholderIcon() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(20, 24, 40));
        g2.fillRoundRect(0, 0, 64, 64, 12, 12);
        g2.setColor(new Color(60, 70, 100));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g2.drawString("♪", 18, 44);
        g2.dispose();
        return new ImageIcon(img);
    }

    private static void loadArtworkAsync(String url, JList<?> list, int index) {
        if (url == null || url.isEmpty() || ARTWORK_CACHE.containsKey(url))
            return;
        ARTWORK_CACHE.put(url, ARTWORK_PLACEHOLDER);
        new javax.swing.SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    java.net.URL u = java.net.URI.create(url).toURL();
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(8000);
                    conn.setRequestProperty("User-Agent", "AuraDL/1.0");
                    java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(conn.getInputStream());
                    if (raw == null)
                        return ARTWORK_PLACEHOLDER;
                    java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(64, 64,
                            java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = scaled.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, 64, 64, 12, 12));
                    g.drawImage(raw, 0, 0, 64, 64, null);
                    g.dispose();
                    return new ImageIcon(scaled);
                } catch (Exception e) {
                    return ARTWORK_PLACEHOLDER;
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    ARTWORK_CACHE.put(url, icon);
                    SwingUtilities.invokeLater(() -> {
                        if (list != null && list.getModel().getSize() > index) {
                            list.repaint(list.getCellBounds(index, index));
                        }
                    });
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    // ─────────────────────────────────────────────────────────
    // MediaItem Cell Renderer
    // ─────────────────────────────────────────────────────────
    private class MediaItemCellRenderer extends JPanel implements ListCellRenderer<MediaItem> {
        private final JLabel artworkLabel = new JLabel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel artistLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();
        private final JLabel qualityLabel = new JLabel();
        private final JLabel typeBadge = new JLabel();
        private final JLabel explicitBadge = new JLabel(" E ");
        private final JButton dlBtn = glowButton("📥", COL_RED, Color.WHITE);
        private final JButton openAlbumBtn = glowButton("📖 Tracks", new Color(18, 32, 55), COL_CYAN);
        private final JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        private boolean sel;
        private MediaItem currentItem;

        MediaItemCellRenderer() {
            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 14, 10, 14));

            artworkLabel.setPreferredSize(new Dimension(64, 64));
            artworkLabel.setMinimumSize(new Dimension(64, 64));
            artworkLabel.setIcon(ARTWORK_PLACEHOLDER);
            artworkLabel.setHorizontalAlignment(SwingConstants.CENTER);

            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setForeground(Color.WHITE);

            artistLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            artistLabel.setForeground(new Color(170, 185, 215));

            metaLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            metaLabel.setForeground(new Color(100, 115, 150));

            qualityLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
            qualityLabel.setForeground(new Color(0, 220, 140));
            qualityLabel.setOpaque(true);
            qualityLabel.setBackground(new Color(0, 220, 140, 30));
            qualityLabel.setBorder(new EmptyBorder(2, 8, 2, 8));

            typeBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
            typeBadge.setOpaque(true);
            typeBadge.setBorder(new EmptyBorder(2, 8, 2, 8));

            explicitBadge.setFont(new Font("SansSerif", Font.BOLD, 9));
            explicitBadge.setForeground(new Color(255, 100, 100));
            explicitBadge.setOpaque(true);
            explicitBadge.setBackground(new Color(255, 80, 80, 40));
            explicitBadge.setBorder(new EmptyBorder(1, 5, 1, 5));
            explicitBadge.setVisible(false);

            dlBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
            dlBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
            dlBtn.addActionListener(e -> {
                if (currentItem != null)
                    queueDownload(currentItem);
            });

            openAlbumBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
            openAlbumBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
            openAlbumBtn.addActionListener(e -> {
                if (currentItem != null && currentItem.getType().equalsIgnoreCase("album")) {
                    openAlbumDetailsDialog(currentItem);
                }
            });

            JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            badges.setOpaque(false);
            badges.add(typeBadge);
            badges.add(qualityLabel);
            badges.add(explicitBadge);

            JPanel textCol = new JPanel();
            textCol.setOpaque(false);
            textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);
            artistLabel.setAlignmentX(LEFT_ALIGNMENT);
            badges.setAlignmentX(LEFT_ALIGNMENT);
            textCol.add(titleLabel);
            textCol.add(Box.createVerticalStrut(3));
            textCol.add(artistLabel);
            textCol.add(Box.createVerticalStrut(5));
            textCol.add(badges);

            rightPanel.setOpaque(false);
            rightPanel.add(openAlbumBtn);
            rightPanel.add(dlBtn);

            add(artworkLabel, BorderLayout.WEST);
            add(textCol, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends MediaItem> list, MediaItem v, int index, boolean isSelected, boolean hasFocus) {
            sel = isSelected;
            currentItem = v;

            titleLabel.setText(v.getTitle());

            StringBuilder meta = new StringBuilder();
            if (!v.getArtist().isEmpty()) {
                meta.append(v.getArtist());
            }
            if (!v.getCollectionName().isEmpty() && !v.getCollectionName().equalsIgnoreCase(v.getTitle())
                    && !v.getCollectionName().equalsIgnoreCase(v.getArtist())) {
                if (meta.length() > 0)
                    meta.append("  ·  ");
                meta.append(v.getCollectionName());
            }
            if (!v.getGenre().isEmpty()) {
                if (meta.length() > 0)
                    meta.append("  ·  ");
                meta.append(v.getGenre());
            }
            String dur = v.getFormattedDuration();
            if (!dur.isEmpty()) {
                if (meta.length() > 0)
                    meta.append("  ·  ");
                meta.append(dur);
            }
            if (v.getTrackNumber() > 0) {
                if (meta.length() > 0)
                    meta.append("  ·  ");
                meta.append("Track ").append(v.getTrackNumber());
            }
            artistLabel.setText(meta.toString());

            String q = v.getQuality();
            String t = v.getType().toLowerCase();
            Color ac;
            if (t.contains("video")) {
                ac = new Color(255, 185, 0);
                typeBadge.setText("  🎬 MUSIC VIDEO  ");
                typeBadge.setForeground(ac);
                typeBadge.setBackground(new Color(255, 185, 0, 55));

                qualityLabel.setForeground(new Color(255, 215, 0));
                qualityLabel.setBackground(new Color(255, 185, 0, 40));
                qualityLabel.setText("  " + (q.isEmpty() ? "1080p HD Video · AAC Audio" : q) + "  ");
            } else if (t.contains("album")) {
                ac = new Color(0, 220, 255);
                typeBadge.setText("  💽 ALBUM  ");
                typeBadge.setForeground(ac);
                typeBadge.setBackground(new Color(0, 220, 255, 45));

                qualityLabel.setForeground(new Color(0, 220, 255));
                qualityLabel.setBackground(new Color(0, 220, 255, 30));
                qualityLabel.setText("  " + (q.isEmpty() ? "ALAC Lossless Album" : q) + "  ");
            } else {
                ac = new Color(250, 45, 85);
                typeBadge.setText("  🎵 SONG  ");
                typeBadge.setForeground(ac);
                typeBadge.setBackground(new Color(250, 45, 85, 45));

                qualityLabel.setForeground(new Color(0, 230, 145));
                qualityLabel.setBackground(new Color(0, 230, 145, 30));
                qualityLabel.setText("  " + (q.isEmpty() ? "ALAC Lossless" : q) + "  ");
            }
            qualityLabel.setVisible(true);
            explicitBadge.setVisible(v.isExplicit());
            openAlbumBtn.setVisible(t.contains("album"));

            String artUrl = v.getArtworkUrl();
            if (artUrl != null && !artUrl.isEmpty()) {
                ImageIcon cached = ARTWORK_CACHE.get(artUrl);
                if (cached != null) {
                    artworkLabel.setIcon(cached);
                } else {
                    artworkLabel.setIcon(ARTWORK_PLACEHOLDER);
                    loadArtworkAsync(artUrl, list, index);
                }
            } else {
                artworkLabel.setIcon(ARTWORK_PLACEHOLDER);
            }

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (sel) {
                g2.setPaint(new GradientPaint(0, 0, new Color(250, 45, 85, 90),
                        getWidth(), getHeight(), new Color(168, 85, 247, 90)));
            } else {
                g2.setColor(new Color(10, 12, 24, 170));
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 16, 16);
            g2.setColor(new Color(255, 255, 255, sel ? 30 : 16));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Queue Task Cell Renderer — Rich Premium Card
    // ─────────────────────────────────────────────────────────────────────────
    private static class QueueTaskCellRenderer extends JPanel implements ListCellRenderer<DownloadTask> {
        // Status accent colors
        private static final Color COL_RED = new Color(250, 45, 85);
        private static final Color COL_CYAN = new Color(0, 220, 255);
        private static final Color COL_AMBER = new Color(255, 185, 0);
        private static final Color COL_PURPLE = new Color(168, 85, 247);
        private static final Color COL_GREEN = new Color(0, 220, 140);

        // Subcomponents
        private final JLabel artLabel = new JLabel();
        private final JLabel titleLbl = new JLabel();
        private final JLabel subtitleLbl = new JLabel();
        private final JLabel metaLbl = new JLabel();
        private final JLabel statusPill = new JLabel();
        private final JLabel pctLbl = new JLabel();
        private final JLabel errLbl = new JLabel();

        // State for custom paint
        private double taskProgress = 0.0;
        private Color barColor1 = COL_CYAN;
        private Color barColor2 = new Color(0, 120, 200);
        private boolean isSelected = false;

        QueueTaskCellRenderer() {
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 14, 10, 14));

            // Album art thumbnail
            artLabel.setPreferredSize(new Dimension(52, 52));
            artLabel.setMinimumSize(new Dimension(52, 52));
            artLabel.setHorizontalAlignment(SwingConstants.CENTER);
            artLabel.setIcon(buildQueuePlaceholderIcon());

            // Text labels
            titleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            titleLbl.setForeground(Color.WHITE);

            subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            subtitleLbl.setForeground(new Color(140, 158, 200));

            metaLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            metaLbl.setForeground(new Color(90, 105, 140));

            errLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            errLbl.setForeground(new Color(255, 90, 110));
            errLbl.setVisible(false);

            // Status pill badge
            statusPill.setFont(new Font("SansSerif", Font.BOLD, 10));
            statusPill.setOpaque(true);
            statusPill.setBorder(new EmptyBorder(2, 8, 2, 8));

            // Percentage label
            pctLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            pctLbl.setForeground(new Color(200, 215, 255));

            // Text column
            JPanel textCol = new JPanel();
            textCol.setOpaque(false);
            textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
            titleLbl.setAlignmentX(LEFT_ALIGNMENT);
            subtitleLbl.setAlignmentX(LEFT_ALIGNMENT);
            metaLbl.setAlignmentX(LEFT_ALIGNMENT);
            errLbl.setAlignmentX(LEFT_ALIGNMENT);
            textCol.add(titleLbl);
            textCol.add(Box.createVerticalStrut(2));
            textCol.add(subtitleLbl);
            textCol.add(Box.createVerticalStrut(2));
            textCol.add(metaLbl);
            textCol.add(Box.createVerticalStrut(2));
            textCol.add(errLbl);

            // Right column: pill + percentage
            JPanel right = new JPanel(new BorderLayout(0, 4));
            right.setOpaque(false);
            right.setPreferredSize(new Dimension(130, 52));
            JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pillRow.setOpaque(false);
            pillRow.add(statusPill);
            right.add(pillRow, BorderLayout.NORTH);
            right.add(pctLbl, BorderLayout.SOUTH);

            add(artLabel, BorderLayout.WEST);
            add(textCol, BorderLayout.CENTER);
            add(right, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends DownloadTask> list, DownloadTask t, int index, boolean sel, boolean focus) {
            isSelected = sel;
            taskProgress = t.getProgress();

            // Title & subtitle
            if (t.getMediaItem() != null) {
                titleLbl.setText(t.getMediaItem().getTitle());
                String artist = t.getMediaItem().getArtist();
                String album = t.getMediaItem().getCollectionName();
                subtitleLbl.setText((!artist.isEmpty() ? artist : "")
                        + (!artist.isEmpty() && !album.isEmpty() && !album.equals(t.getMediaItem().getTitle())
                                ? "  ·  " + album
                                : ""));
            } else {
                titleLbl.setText(t.getUrl());
                subtitleLbl.setText("Apple Music");
            }

            // Meta: speed + ETA
            String eta = (t.getEta() != null && !t.getEta().equals("--:--")) ? t.getEta() : "";
            String speed = t.getSpeed() != null ? t.getSpeed() : "";
            String metaTxt = speed;
            if (!eta.isEmpty() && t.getStatus() != DownloadStatus.COMPLETED)
                metaTxt += (metaTxt.isEmpty() ? "" : "  ·  ") + "ETA " + eta;
            metaLbl.setText(metaTxt);

            // Inline error message
            if (t.getStatus() == DownloadStatus.FAILED
                    && t.getErrorMessage() != null && !t.getErrorMessage().isEmpty()) {
                errLbl.setText("⚠ " + t.getErrorMessage());
                errLbl.setVisible(true);
            } else {
                errLbl.setVisible(false);
            }

            // Status colors, pill text, gradient colors
            Color ac;
            String pillIcon;
            String pillText;
            switch (t.getStatus()) {
                case PENDING_FETCHING:
                    ac = new Color(100, 130, 180);
                    pillIcon = "⏳";
                    pillText = "PENDING";
                    barColor1 = new Color(60, 80, 130);
                    barColor2 = new Color(40, 60, 110);
                    break;
                case FETCHING:
                    ac = COL_CYAN;
                    pillIcon = "🔍";
                    pillText = "FETCHING";
                    barColor1 = COL_CYAN;
                    barColor2 = new Color(0, 130, 200);
                    break;
                case DOWNLOADING:
                    ac = new Color(60, 160, 255);
                    pillIcon = "⬇";
                    pillText = "DOWNLOADING";
                    barColor1 = new Color(60, 160, 255);
                    barColor2 = COL_CYAN;
                    break;
                case DECRYPTING:
                    ac = COL_AMBER;
                    pillIcon = "🔓";
                    pillText = "DECRYPTING";
                    barColor1 = COL_AMBER;
                    barColor2 = new Color(200, 140, 0);
                    break;
                case EXTRACTING:
                    ac = COL_PURPLE;
                    pillIcon = "🔧";
                    pillText = "EXTRACTING";
                    barColor1 = COL_PURPLE;
                    barColor2 = new Color(110, 40, 180);
                    break;
                case SAVING_TAGS:
                    ac = new Color(200, 100, 255);
                    pillIcon = "🏷";
                    pillText = "TAGGING";
                    barColor1 = new Color(200, 100, 255);
                    barColor2 = COL_PURPLE;
                    break;
                case COMPLETED:
                    ac = COL_GREEN;
                    pillIcon = "✅";
                    pillText = "DONE";
                    barColor1 = COL_GREEN;
                    barColor2 = new Color(0, 160, 100);
                    break;
                case FAILED:
                    ac = COL_RED;
                    pillIcon = "❌";
                    pillText = "FAILED";
                    barColor1 = COL_RED;
                    barColor2 = new Color(180, 20, 55);
                    break;
                default:
                    ac = new Color(100, 130, 180);
                    pillIcon = "⏳";
                    pillText = "WAITING";
                    barColor1 = new Color(60, 80, 130);
                    barColor2 = new Color(40, 60, 110);
            }

            statusPill.setText("  " + pillIcon + " " + pillText + "  ");
            statusPill.setForeground(ac);
            statusPill.setBackground(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 45));

            int pct = (int) (taskProgress * 100);
            pctLbl.setText(pct > 0 ? pct + "%" : "");
            pctLbl.setForeground(ac);

            // Album art (reuse shared artwork cache)
            String artUrl = (t.getMediaItem() != null) ? t.getMediaItem().getArtworkUrl() : null;
            if (artUrl != null && !artUrl.isEmpty()) {
                ImageIcon cached = ARTWORK_CACHE.get(artUrl);
                if (cached != null) {
                    artLabel.setIcon(scaleToQueue(cached));
                } else {
                    artLabel.setIcon(buildQueuePlaceholderIcon());
                    loadArtworkAsync(artUrl, list, index);
                }
            } else {
                artLabel.setIcon(buildQueuePlaceholderIcon());
            }

            return this;
        }

        /** Scale the shared 64×64 artwork icon down to 48×48 with rounded clip. */
        private static ImageIcon scaleToQueue(ImageIcon icon) {
            if (icon == null || icon == ARTWORK_PLACEHOLDER)
                return buildQueuePlaceholderIcon();
            java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(48, 48,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, 48, 48, 10, 10));
            g.drawImage(icon.getImage(), 0, 0, 48, 48, null);
            g.dispose();
            return new ImageIcon(out);
        }

        private static ImageIcon buildQueuePlaceholderIcon() {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(48, 48,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(18, 22, 38));
            g2.fillRoundRect(0, 0, 48, 48, 10, 10);
            g2.setColor(new Color(50, 60, 90));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
            g2.drawString("♪", 13, 34);
            g2.dispose();
            return new ImageIcon(img);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Card background
            if (isSelected) {
                g2.setPaint(new GradientPaint(0, 0, new Color(30, 38, 65, 230),
                        w, h, new Color(20, 28, 50, 230)));
            } else {
                g2.setColor(new Color(10, 13, 22, 210));
            }
            g2.fillRoundRect(0, 0, w, h - 3, 16, 16);

            // Card border
            g2.setColor(new Color(255, 255, 255, isSelected ? 28 : 14));
            g2.drawRoundRect(0, 0, w - 1, h - 4, 16, 16);

            // Gradient progress bar at the very bottom of each card
            int barH = 4;
            int barY = h - barH - 2;
            int barW = (int) (w * taskProgress);
            if (taskProgress == 0.0) {
                // Empty track
                g2.setColor(new Color(25, 30, 48));
                g2.fillRoundRect(4, barY, w - 8, barH, barH, barH);
            } else {
                // Track background (dim)
                g2.setColor(new Color(30, 36, 55));
                g2.fillRoundRect(4, barY, w - 8, barH, barH, barH);
                // Filled gradient
                int fillW = Math.min(Math.max(barW - 4, 4), w - 8);
                g2.setPaint(new GradientPaint(4, barY, barColor1, Math.max(fillW, 12) + 4, barY, barColor2));
                g2.fillRoundRect(4, barY, fillW, barH, barH, barH);
                // Glow dot at leading edge
                if (taskProgress < 0.99 && barW > 8) {
                    g2.setColor(new Color(barColor1.getRed(), barColor1.getGreen(), barColor1.getBlue(), 110));
                    g2.fillOval(fillW, barY - 1, 6, barH + 2);
                }
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Album Details Modal & Tracklist Viewer
    // ─────────────────────────────────────────────────────────────────────────
    private void openAlbumDetailsDialog(MediaItem albumItem) {
        JDialog dlg = new JDialog(this, "Album Details — " + albumItem.getTitle(), true);
        dlg.setSize(920, 680);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(6, 8, 14));

        JPanel mainPanel = glassCard(16);
        mainPanel.setLayout(new BorderLayout(0, 16));
        mainPanel.setBorder(new EmptyBorder(22, 26, 22, 26));

        // ── Header Card ─────────────────────────────────────────────
        JPanel headerCard = glassCard(14);
        headerCard.setLayout(new BorderLayout(14, 0));
        headerCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel dlgArtLabel = new JLabel(ARTWORK_PLACEHOLDER);
        dlgArtLabel.setPreferredSize(new Dimension(110, 110));
        dlgArtLabel.setMinimumSize(new Dimension(110, 110));
        dlgArtLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dlgArtLabel.setVerticalAlignment(SwingConstants.TOP);

        // Fetch high-res artwork async
        String artUrl = albumItem.getArtworkUrl();
        if (artUrl != null && !artUrl.isEmpty()) {
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() {
                    try {
                        java.net.URL u = java.net.URI.create(artUrl).toURL();
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setRequestProperty("User-Agent", "AuraDL/1.0");
                        java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(conn.getInputStream());
                        if (raw == null)
                            return null;
                        java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(110, 110,
                                java.awt.image.BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = scaled.createGraphics();
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, 110, 110, 14, 14));
                        g.drawImage(raw, 0, 0, 110, 110, null);
                        g.dispose();
                        return new ImageIcon(scaled);
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null)
                            dlgArtLabel.setIcon(icon);
                    } catch (Exception ignored) {
                    }
                }
            }.execute();
        }

        JPanel headerInfo = new JPanel();
        headerInfo.setOpaque(false);
        headerInfo.setLayout(new BoxLayout(headerInfo, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("<html><span style='color: #FFFFFF; font-size: 14pt; font-weight: bold;'>"
                + escapeHtml(albumItem.getTitle()) + "</span></html>");
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel artistLbl = new JLabel(albumItem.getArtist());
        artistLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        artistLbl.setForeground(COL_GOLD);
        artistLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel metaLbl = new JLabel("Fetching tracklist and audio quality specifications…");
        metaLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        metaLbl.setForeground(COL_SUBTEXT);
        metaLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel qualityBadge = new JLabel("  💽 ALAC Lossless · 24-bit / 96kHz · Apple Music  ");
        qualityBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        qualityBadge.setForeground(COL_CYAN);
        qualityBadge.setOpaque(true);
        qualityBadge.setBackground(new Color(0, 220, 255, 35));
        qualityBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(LEFT_ALIGNMENT);
        badgeRow.add(qualityBadge);

        headerInfo.add(titleLbl);
        headerInfo.add(Box.createVerticalStrut(4));
        headerInfo.add(artistLbl);
        headerInfo.add(Box.createVerticalStrut(4));
        headerInfo.add(metaLbl);
        headerInfo.add(Box.createVerticalStrut(8));
        headerInfo.add(badgeRow);

        JPanel headerActions = new JPanel();
        headerActions.setOpaque(false);
        headerActions.setLayout(new BoxLayout(headerActions, BoxLayout.Y_AXIS));

        JButton dlAlbumBtn = glowButton("📥 Queue Entire Album", COL_RED, Color.WHITE);
        dlAlbumBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        dlAlbumBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        dlAlbumBtn.setAlignmentX(RIGHT_ALIGNMENT);
        dlAlbumBtn.addActionListener(e -> {
            queueDownload(albumItem);
            statusLabel.setText("Queued Album: " + albumItem.getTitle());
        });

        JButton closeBtn = glowButton("✕ Close", new Color(40, 50, 70), Color.WHITE);
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        closeBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        closeBtn.setAlignmentX(RIGHT_ALIGNMENT);
        closeBtn.addActionListener(e -> dlg.dispose());

        headerActions.add(dlAlbumBtn);
        headerActions.add(Box.createVerticalStrut(8));
        headerActions.add(closeBtn);

        headerCard.add(dlgArtLabel, BorderLayout.WEST);
        headerCard.add(headerInfo, BorderLayout.CENTER);
        headerCard.add(headerActions, BorderLayout.EAST);
        mainPanel.add(headerCard, BorderLayout.NORTH);

        // ── Tracklist Panel ─────────────────────────────────────────
        DefaultListModel<MediaItem> tracksModel = new DefaultListModel<>();
        tracksModel.addElement(new MediaItem("loading", "⏳  Loading album tracks and song quality specs...",
                "Please wait", "", "song", 0, ""));

        JList<MediaItem> trackList = new JList<>(tracksModel);
        trackList.setCellRenderer(new AlbumTrackCellRenderer());
        trackList.setOpaque(true);
        trackList.setBackground(new Color(10, 12, 20));
        trackList.setSelectionBackground(new Color(0, 220, 255, 45));

        trackList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = trackList.locationToIndex(e.getPoint());
                if (idx >= 0) {
                    Rectangle bounds = trackList.getCellBounds(idx, idx);
                    if (bounds != null && bounds.contains(e.getPoint())) {
                        MediaItem clickedTrack = tracksModel.getElementAt(idx);
                        if (clickedTrack != null && !clickedTrack.getId().startsWith("loading")
                                && !clickedTrack.getId().equals("empty")) {
                            queueDownload(clickedTrack);
                            statusLabel.setText("Queued Track: " + clickedTrack.getTitle());
                        }
                    }
                }
            }
        });

        JScrollPane trackScroll = darkScrollPane(trackList);
        trackScroll.getViewport().setBackground(new Color(10, 12, 20));

        JPanel trackContainer = glassCard(12);
        trackContainer.setLayout(new BorderLayout(0, 8));
        trackContainer.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Table Header Line
        JPanel trackHeaderBar = new JPanel(new BorderLayout(12, 0));
        trackHeaderBar.setOpaque(false);
        trackHeaderBar.setBorder(new EmptyBorder(0, 8, 6, 8));

        JLabel hNum = new JLabel("#");
        hNum.setFont(new Font("Monospaced", Font.BOLD, 12));
        hNum.setForeground(COL_GOLD);
        hNum.setPreferredSize(new Dimension(32, 20));
        hNum.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel hTitle = new JLabel("TRACK TITLE & AUDIO SPECS");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        hTitle.setForeground(COL_GOLD);

        JLabel hAction = new JLabel("DURATION  ·  ACTION");
        hAction.setFont(new Font("SansSerif", Font.BOLD, 11));
        hAction.setForeground(COL_GOLD);

        trackHeaderBar.add(hNum, BorderLayout.WEST);
        trackHeaderBar.add(hTitle, BorderLayout.CENTER);
        trackHeaderBar.add(hAction, BorderLayout.EAST);

        trackContainer.add(trackHeaderBar, BorderLayout.NORTH);
        trackContainer.add(trackScroll, BorderLayout.CENTER);
        mainPanel.add(trackContainer, BorderLayout.CENTER);

        dlg.getContentPane().add(mainPanel);

        String lookupQuery = (albumItem.getUrl() != null && !albumItem.getUrl().isEmpty()) ? albumItem.getUrl()
                : albumItem.getId();
        apiClient.fetchAlbumDetails(lookupQuery)
                .thenAccept(details -> SwingUtilities.invokeLater(() -> {
                    if (details != null && !details.getTracks().isEmpty()) {
                        tracksModel.clear();
                        details.getTracks().forEach(tracksModel::addElement);
                        MediaItem realAlbum = details.getAlbum() != null ? details.getAlbum() : albumItem;

                        String genre = !realAlbum.getGenre().isEmpty() ? realAlbum.getGenre() : "Apple Music";
                        int tCount = details.getTracks().size();
                        long totalDurMs = details.getTracks().stream().mapToLong(MediaItem::getDurationMs).sum();
                        long s = totalDurMs / 1000;
                        long m = s / 60;
                        long h = m / 60;
                        m %= 60;
                        String totalDurStr = h > 0 ? String.format("%d hr %d min", h, m) : String.format("%d min", m);

                        metaLbl.setText(genre + "  ·  " + tCount + " Songs  ·  Total: " + totalDurStr);
                        qualityBadge.setText("  💽 ALAC Lossless · 24-bit / 96kHz · " + tCount + " Tracks Available  ");
                        dlAlbumBtn.setText("📥 Queue All " + tCount + " Tracks");
                    } else {
                        tracksModel.clear();
                        tracksModel.addElement(new MediaItem("empty", "❌ Could not load album tracklist",
                                "Check internet connection", "", "song", 0, ""));
                    }
                }));

        dlg.setVisible(true);
    }

    private static String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private class AlbumTrackCellRenderer extends JPanel implements ListCellRenderer<MediaItem> {
        private final JLabel trackNumLbl = new JLabel();
        private final JLabel titleLbl = new JLabel();
        private final JLabel qualityLbl = new JLabel();
        private final JLabel durationLbl = new JLabel();
        private final JLabel explicitLbl = new JLabel(" E ");
        private final JButton dlBtn = glowButton("📥 Queue Song", COL_RED, Color.WHITE);
        private boolean sel;

        AlbumTrackCellRenderer() {
            setLayout(new BorderLayout(12, 0));
            setOpaque(true);
            setBackground(new Color(10, 12, 20));
            setBorder(new EmptyBorder(6, 10, 6, 10));

            trackNumLbl.setFont(new Font("Monospaced", Font.BOLD, 12));
            trackNumLbl.setForeground(COL_GOLD);
            trackNumLbl.setPreferredSize(new Dimension(32, 24));
            trackNumLbl.setHorizontalAlignment(SwingConstants.CENTER);

            titleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            titleLbl.setForeground(Color.WHITE);

            qualityLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
            qualityLbl.setForeground(new Color(0, 230, 145));
            qualityLbl.setOpaque(true);
            qualityLbl.setBackground(new Color(0, 230, 145, 30));
            qualityLbl.setBorder(new EmptyBorder(2, 8, 2, 8));

            explicitLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
            explicitLbl.setForeground(new Color(255, 100, 100));
            explicitLbl.setOpaque(true);
            explicitLbl.setBackground(new Color(255, 80, 80, 40));
            explicitLbl.setBorder(new EmptyBorder(1, 5, 1, 5));

            durationLbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            durationLbl.setForeground(COL_SUBTEXT);

            dlBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
            dlBtn.setBorder(new EmptyBorder(4, 10, 4, 10));

            JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            center.setOpaque(false);
            center.add(titleLbl);
            center.add(explicitLbl);
            center.add(qualityLbl);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            right.setOpaque(false);
            right.add(durationLbl);
            right.add(dlBtn);

            add(trackNumLbl, BorderLayout.WEST);
            add(center, BorderLayout.CENTER);
            add(right, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MediaItem> list, MediaItem v, int index,
                boolean isSelected, boolean focus) {
            sel = isSelected;

            if (v.getId().startsWith("loading") || v.getId().equals("empty")) {
                trackNumLbl.setText("");
                titleLbl.setText(v.getTitle());
                qualityLbl.setVisible(false);
                explicitLbl.setVisible(false);
                durationLbl.setText("");
                dlBtn.setVisible(false);
                return this;
            }

            dlBtn.setVisible(true);
            int tNum = v.getTrackNumber() > 0 ? v.getTrackNumber() : index + 1;
            trackNumLbl.setText(String.format("%02d", tNum));
            titleLbl.setText(v.getTitle());
            explicitLbl.setVisible(v.isExplicit());
            qualityLbl.setText("  🎧 ALAC · Lossless · 24-bit/96kHz  ");
            qualityLbl.setVisible(true);
            durationLbl.setText(v.getFormattedDuration());

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Solid dark background to prevent ANY ghost text bleed
            g2.setColor(new Color(10, 12, 20));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Rounded card row background
            if (sel) {
                g2.setColor(new Color(0, 220, 255, 40));
            } else {
                g2.setColor(new Color(16, 20, 32));
            }
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);

            // Subtle border
            g2.setColor(new Color(255, 255, 255, sel ? 30 : 12));
            g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
