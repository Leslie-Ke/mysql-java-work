import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserFrame extends JFrame {
    private int userId;
    private JTextField searchField;
    private JTable bookTable;
    private JTable borrowedTable;
    private DefaultTableModel bookTableModel;
    private DefaultTableModel borrowedTableModel;

    // 构造方法，设置界面布局和初始化组件
    public UserFrame(int userId) {
        this.userId = userId;
        setTitle("用户面板");
        setLayout(new BorderLayout());

        // 可借阅图书表格
        bookTableModel = new DefaultTableModel(new String[]{"ID", "标题", "作者", "可借阅"}, 0);
        bookTable = new JTable(bookTableModel);
        JScrollPane bookScrollPane = new JScrollPane(bookTable);

        // 借阅图书表格
        borrowedTableModel = new DefaultTableModel(new String[]{"借阅ID", "图书ID", "标题", "作者", "借阅日期"}, 0);
        borrowedTable = new JTable(borrowedTableModel);
        JScrollPane borrowedScrollPane = new JScrollPane(borrowedTable);

        // 分栏面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bookScrollPane, borrowedScrollPane);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

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

        // 借阅和归还按钮面板
        JPanel buttonPanel = new JPanel();
        JButton borrowButton = new JButton("借阅图书");
        borrowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                borrowBook();
            }
        });
        buttonPanel.add(borrowButton);

        JButton returnButton = new JButton("归还图书");
        returnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                returnBook();
            }
        });
        buttonPanel.add(returnButton);

        add(searchPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // 增加窗口大小
        setVisible(true);

        // 加载可借阅的图书和已借阅的图书
        loadBooks();
        loadBorrowedBooks();
    }

    // 加载可借阅的图书
    private void loadBooks() {
        bookTableModel.setRowCount(0); // 清空表格数据

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM Books WHERE available = TRUE";
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

    // 加载用户已借阅的图书
    private void loadBorrowedBooks() {
        borrowedTableModel.setRowCount(0); // 清空表格数据

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT b.borrow_id, b.book_id, k.title, k.author, b.borrow_date " +
                    "FROM BorrowedBooks b JOIN Books k ON b.book_id = k.book_id " +
                    "WHERE b.user_id = ? AND b.return_date IS NULL";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                borrowedTableModel.addRow(new Object[]{
                        rs.getInt("borrow_id"),
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDate("borrow_date")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 借阅图书的方法
    private void borrowBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            int bookId = (int) bookTableModel.getValueAt(selectedRow, 0);

            try (Connection conn = Database.getConnection()) {
                String query = "INSERT INTO BorrowedBooks (user_id, book_id, borrow_date) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setInt(2, bookId);
                stmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                stmt.executeUpdate();

                query = "UPDATE Books SET available = FALSE WHERE book_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, bookId);
                stmt.executeUpdate();

                loadBooks(); // 重新加载图书
                loadBorrowedBooks(); // 重新加载借阅图书
                JOptionPane.showMessageDialog(this, "图书借阅成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "借阅图书时出错", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要借阅的图书", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 归还图书的方法
    private void returnBook() {
        int selectedRow = borrowedTable.getSelectedRow();
        if (selectedRow != -1) {
            int borrowId = (int) borrowedTableModel.getValueAt(selectedRow, 0);
            int bookId = (int) borrowedTableModel.getValueAt(selectedRow, 1);

            try (Connection conn = Database.getConnection()) {
                String query = "UPDATE BorrowedBooks SET return_date = ? WHERE borrow_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                stmt.setInt(2, borrowId);
                stmt.executeUpdate();

                query = "UPDATE Books SET available = TRUE WHERE book_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, bookId);
                stmt.executeUpdate();

                loadBooks(); // 重新加载图书
                loadBorrowedBooks(); // 重新加载借阅图书
                JOptionPane.showMessageDialog(this, "图书归还成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "归还图书时出错", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要归还的图书", "错误", JOptionPane.ERROR_MESSAGE);
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
}

