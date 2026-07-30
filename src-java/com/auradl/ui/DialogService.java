package com.auradl.ui;

import javax.swing.*;
import java.awt.*;

public class DialogService {
    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static String showInput(Component parent, String title, String label) {
        return JOptionPane.showInputDialog(parent, label, title, JOptionPane.QUESTION_MESSAGE);
    }
}
