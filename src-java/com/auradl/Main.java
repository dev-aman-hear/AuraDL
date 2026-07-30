package com.auradl;

import com.auradl.api.AppleMusicApiClient;
import com.auradl.config.ConfigManager;
import com.auradl.download.DownloadManager;
import com.auradl.ui.GlassUIWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Insets;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("    AuraDL v1.0 - Apple Music Desktop    ");
        System.out.println("=========================================");

        // 1. Initialize Configuration Manager
        ConfigManager configManager = new ConfigManager();
        System.out.println("[Main] Config loaded from: " + System.getProperty("user.home") + "/.auradl/config.yml");

        // 2. Initialize Apple Music API Client
        AppleMusicApiClient apiClient = new AppleMusicApiClient(configManager.getConfig());
        apiClient.initialize().thenAccept(success -> {
            if (success) {
                System.out.println("[Main] Apple Music API initialized successfully!");
            } else {
                System.out.println("[Main] Apple Music API initialized in offline mode.");
            }
        });

        // 3. Initialize Download Manager
        DownloadManager downloadManager = new DownloadManager(configManager.getConfig());

        // 4. Launch Desktop GUI Window on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply 100% True Dark UI defaults across all Swing components
                UIManager.put("Panel.background", new Color(5, 6, 9));
                UIManager.put("TabbedPane.background", new Color(5, 6, 9));
                UIManager.put("TabbedPane.foreground", new Color(245, 247, 255));
                UIManager.put("TabbedPane.contentAreaColor", new Color(5, 6, 9));
                UIManager.put("TabbedPane.tabAreaBackground", new Color(5, 6, 9));
                UIManager.put("TabbedPane.shadow", new Color(0, 0, 0, 0));
                UIManager.put("TabbedPane.darkShadow", new Color(0, 0, 0, 0));
                UIManager.put("TabbedPane.light", new Color(212, 160, 23, 80));
                UIManager.put("TabbedPane.highlight", new Color(212, 160, 23, 80));
                UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
                UIManager.put("TabbedPane.borderHighlightColor", new Color(0, 0, 0, 0));
                UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
                UIManager.put("ScrollPane.background", new Color(5, 6, 9));
                UIManager.put("Viewport.background", new Color(5, 6, 9));
                UIManager.put("List.background", new Color(5, 6, 9));
                UIManager.put("List.foreground", Color.WHITE);
                UIManager.put("OptionPane.background", new Color(11, 13, 20));
                UIManager.put("OptionPane.messageForeground", Color.WHITE);
                UIManager.put("PopupMenu.background", new Color(11, 13, 20));
                UIManager.put("Menu.background", new Color(11, 13, 20));
                UIManager.put("MenuItem.background", new Color(11, 13, 20));
                UIManager.put("MenuItem.foreground", Color.WHITE);
                UIManager.put("ComboBox.background", new Color(6, 8, 14));
                UIManager.put("ComboBox.foreground", Color.WHITE);
                UIManager.put("TextField.background", new Color(6, 8, 14));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("TextField.caretForeground", new Color(255, 215, 0));
            } catch (Exception ignored) {}

            GlassUIWindow window = new GlassUIWindow(configManager, apiClient, downloadManager);
            window.setVisible(true);

            System.out.println("[Main] Java Glassmorphism Dark GUI window displayed successfully!");
        });

        // Shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Main] Shutting down AuraDL Engine...");
            downloadManager.shutdown();
        }));
    }
}
