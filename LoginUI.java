package net.javaguides.pfm;

import java.awt.*;
import javax.swing.*;

public class LoginUI {

    private static final String USERNAME = "rahul";
    private static final String PASSWORD = "Admin";

    private final JFrame frame;
    private final JTextField userField;
    private final JPasswordField passField;
    private final FinanceManager manager;

    public LoginUI(FinanceManager manager) {
        this.manager = manager;
        frame = new JFrame("Login – Personal Finance Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 160);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("Username:"));
        userField = new JTextField();
        form.add(userField);
        form.add(new JLabel("Password:"));
        passField = new JPasswordField();
        form.add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> attemptLogin());
        frame.add(form, BorderLayout.CENTER);
        frame.add(loginBtn, BorderLayout.SOUTH);

        passField.addActionListener(e -> attemptLogin());

        frame.setVisible(true);
    }

    private void attemptLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (USERNAME.equals(user) && PASSWORD.equals(pass)) {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new SwingUI(manager));
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
    }
}
