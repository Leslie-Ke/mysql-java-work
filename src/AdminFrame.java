import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminFrame extends JFrame {
    private JTextField titleField;
    private JTextField authorField;
    private JTextField searchField;
    private JTable bookTable;
    private DefaultTableModel bookTableModel;

    // 构造方法，设置界面布局和初始化组件
    public AdminFrame() {
        setTitle("管理员面板");
        setLayout(new BorderLayout());

        // 输入面板，包含图书标题和作者的输入框，以及添加和删除按钮
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("标题:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("作者:"));
        authorField = new JTextField();
        inputPanel.add(authorField);

        // 添加图书按钮
        JButton addButton = new JButton("添加图书");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });
        inputPanel.add(addButton);

        // 删除图书按钮
        JButton deleteButton = new JButton("删除图书");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteBook();
            }
        });
        inputPanel.add(deleteButton);

        add(inputPanel, BorderLayout.NORTH);

        // 图书表，用于显示所有图书的信息
        bookTableModel = new DefaultTableModel(new String[]{"ID", "标题", "作者", "可借阅"}, 0);
        bookTable = new JTable(bookTableModel);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        // 查询图书面板，包含查询输入框和查询按钮
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchPanel.add(new JLabel("查询图书:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("查询");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchBooks();
            }
        });
        searchPanel.add(searchButton, BorderLayout.EAST);

        // 查看借阅情况按钮
        JButton viewBorrowsButton = new JButton("查看借阅情况");
        viewBorrowsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewBorrowedBooks();
            }
        });
        searchPanel.add(viewBorrowsButton, BorderLayout.SOUTH);

        add(searchPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // 增加窗口大小
        setVisible(true);

        // 加载所有图书
        loadBooks();
    }

    // 添加图书的方法
    private void addBook() {
        String title = titleField.getText();
        String author = authorField.getText();

        try (Connection conn = Database.getConnection()) {
            String query = "INSERT INTO Books (title, author) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.executeUpdate();
            loadBooks(); // 重新加载图书
            JOptionPane.showMessageDialog(this, "图书添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "添加图书时出错", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 删除图书的方法
    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            int bookId = (int) bookTableModel.getValueAt(selectedRow, 0);

            try (Connection conn = Database.getConnection()) {
                String query = "DELETE FROM Books WHERE book_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
                loadBooks(); // 重新加载图书
                JOptionPane.showMessageDialog(this, "图书删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "删除图书时出错", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要删除的图书", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 加载所有图书的方法
    private void loadBooks() {
        bookTableModel.setRowCount(0); // 清空表格数据

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM Books";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookTableModel.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBoolean("available")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 查询图书的方法
    private void searchBooks() {
        String keyword = searchField.getText();
        bookTableModel.setRowCount(0); // 清空表格数据

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM Books WHERE title LIKE ? OR author LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookTableModel.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBoolean("available")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 查看借阅情况的方法
    private void viewBorrowedBooks() {
        JFrame borrowedBooksFrame = new JFrame("借阅情况");
        borrowedBooksFrame.setLayout(new BorderLayout());

        DefaultTableModel borrowedTableModel = new DefaultTableModel(new String[]{"借阅ID", "用户ID", "图书ID", "借阅日期", "归还日期"}, 0);
        JTable borrowedTable = new JTable(borrowedTableModel);
        JScrollPane scrollPane = new JScrollPane(borrowedTable);
        borrowedBooksFrame.add(scrollPane, BorderLayout.CENTER);

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM BorrowedBooks";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                borrowedTableModel.addRow(new Object[]{
                        rs.getInt("borrow_id"),
                        rs.getInt("user_id"),
                        rs.getInt("book_id"),
                        rs.getDate("borrow_date"),
                        rs.getDate("return_date")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        borrowedBooksFrame.setSize(800, 400);
        borrowedBooksFrame.setVisible(true);
    }
}

