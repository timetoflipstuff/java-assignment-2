package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Contains all the colors used in this project
 */
class Colors {
    static final Color red = new Color(232, 57, 95);
}

/**
 * Main class of the application
 */
public class UI {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CustomerDataManager dataManager = new CustomerDataManager();
        MainFrame frame = new MainFrame(dataManager);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

/**
 * The core frame of the application
 */
class MainFrame extends JFrame {

    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 400;

    private final CustomersTable customersTable;
    private final CustomerDataManager dataManager;

    public MainFrame(CustomerDataManager dataManager) {
        this.dataManager = dataManager;
        customersTable = new CustomersTable(FRAME_WIDTH, dataManager);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JPanel productsTableHeader = createFirstRow();
        contentPane.add(productsTableHeader);
        contentPane.add(customersTable);
        setContentPane(contentPane);

        // Adding a menu bar onto the frame that allows us to add or modify existing products
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder());
        setJMenuBar(menuBar);
        menuBar.add(createCustomerMenu());
    }

    private JPanel createFirstRow() {
        JPanel panel = new JPanel();
        panel.setBackground(Colors.red);
        panel.setLayout(new GridLayout(1, 5));

        String[] labels = {"ID", "Name", "Phone", "Email", "Postal code"};
        for (String labelText: labels) {
            JLabel label = new JLabel(labelText);
            Font font = label.getFont();
            label.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
            panel.add(label);
        }

        return panel;
    }

    /**
     * Creates the "Customer" menu with "Add" and "Edit" items inside of it
     * @return "Customer" menu
     */
    public JMenu createCustomerMenu() {
        // Creating the Customers menu with Add and Edit submenus
        JMenu productMenu = new JMenu("Products");
        productMenu.add(createAddCustomerMenuItem());
        productMenu.add(createEditCustomerMenuItem());
        return productMenu;
    }

    /**
     * Creates the "Add" menu item
     * @return "Add" menu item
     */
    public JMenuItem createAddCustomerMenuItem() {
        // Creating the Add menu item
        JMenuItem addItem = new JMenuItem("New");
        // Action listener shenanigans
        class MenuItemListener implements ActionListener {
            // The method that gets called whenever the menu item is clicked
            public void actionPerformed(ActionEvent event) {
                // Creating and displaying the panel that will accept user input
                CustomerDialog panel = new CustomerDialog();
                int result = JOptionPane.showConfirmDialog(null, panel,
                        "Please enter customer info", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String resultMessage = "New customer added successfully";
                    // This is the part of code that checks user input, validates it
                    // and either shows a success or an error message based on validation results
                    try {
                        CustomerDataModel customer = panel.getInput();
                        boolean success = dataManager.addCustomer(customer);
                        if (!success) throw new Exception("Not today");
                    } catch(Exception e) {
                        resultMessage = "One of the fields provided is invalid";
                    }
                    JOptionPane.showMessageDialog(null, resultMessage);
                }
            }
        }

        // Adding the listener to the menu item
        ActionListener listener = new MenuItemListener();
        addItem.addActionListener(listener);
        return (addItem);
    }

    /**
     * Creates the "Edit" menu item
     * @return "Edit" menu item
     */
    public JMenuItem createEditCustomerMenuItem() {
        JMenuItem itemExit = new JMenuItem("Edit");
        class MenuItemListener implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                CustomerDialog panel = new CustomerDialog();
                int result = JOptionPane.showConfirmDialog(null, panel,
                        "Please enter customer info", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String resultMessage = "Customer edited successfully";
                    try {
                        CustomerDataModel customer = panel.getInput();
                        boolean success = dataManager.editCustomer(customer);
                        if (!success) throw new Exception("Oops");
                    } catch(Exception e) {
                        resultMessage = "One of the fields provided is invalid";
                    }
                    JOptionPane.showMessageDialog(null, resultMessage);
                }
            }
        }

        ActionListener listener = new MenuItemListener();
        itemExit.addActionListener(listener);
        return (itemExit);
    }
}

/**
 * A modal pop-up dialog that prompts user to enter data about a customer they wish to add/edit.
 */
class CustomerDialog extends JPanel {

    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField emailField;
    private final JTextField postalField;

    public CustomerDialog() {
        super();
        // Setting up input fields and adding them to the panel
        this.idField = new JTextField(5);
        this.nameField = new JTextField(5);
        this.phoneField = new JTextField(5);
        this.emailField = new JTextField(5);
        this.postalField = new JTextField(5);

        this.add(new JLabel("ID: "));
        this.add(this.idField);
        this.add(new JLabel("Name: "));
        this.add(this.nameField);
        this.add(new JLabel("Phone: "));
        this.add(this.phoneField);
        this.add(new JLabel("Email: "));
        this.add(this.emailField);
        this.add(new JLabel("Postal code: "));
        this.add(this.postalField);
    }

    /**
     * Returns the data that user entered
     * @return user-entered data
     * @throws Exception
     */
    public CustomerDataModel getInput() throws Exception {
        // Extracting user input from each field
        int id = Integer.parseInt(idField.getText());
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String postalCode = postalField.getText();

        // Validating the data
        boolean isPhoneValid = !phone.matches(".*[a-zA-Z]+.*"); // Contains alphabetical characters
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || postalCode.isEmpty() || !isPhoneValid) {
            System.out.println("Dialog error");
            throw new Exception("One of the provided fields is invalid"); // This error is handled by the ActionListener
        }

        return new CustomerDataModel(id, name, phone, email, postalCode);
    }
}

/**
 * Panel containing the customers list
 */
class CustomersTable extends Panel implements DataChangeListener, ListCellRenderer<CustomerDataModel> {
    private final DefaultListModel<CustomerDataModel> listModel = new DefaultListModel<>();

    CustomersTable(int width, CustomerDataManager dataManager) {
        // Configuring the panel
        dataManager.setListener(this);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(width, 500));

        // Configuring and adding JList to the panel
        JList<CustomerDataModel> jList = new JList<>(listModel);
        jList.setCellRenderer(this);
        JScrollPane scrollPane = new JScrollPane(jList);
        scrollPane.setBackground(Color.LIGHT_GRAY);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, constraints);

        // Fetching data from Data manager and populating the list
        try {
            listModel.addAll(dataManager.getCustomers());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemChanged(int pos, CustomerDataModel productDataModel) {
        listModel.remove(pos);
        listModel.add(pos, productDataModel);
    }

    @Override
    public void onItemAdded(CustomerDataModel productDataModel) {
        listModel.add(0, productDataModel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends CustomerDataModel> list, CustomerDataModel value, int index, boolean isSelected, boolean cellHasFocus) {
        return createRow(value);
    }

    private JPanel createRow(CustomerDataModel model) {
        // Creating the row
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 5));
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray));

        String[] labels = {
                String.valueOf(model.id()),
                String.valueOf(model.name()),
                String.valueOf(model.phone()),
                String.valueOf(model.email()),
                String.valueOf(model.postalCode())
        };

        // Populating the row
        boolean isFirstColumn = true;
        for (String labelText: labels) {
            JLabel label = new JLabel(labelText);
            if (isFirstColumn) {
                Font font = label.getFont();
                label.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
                isFirstColumn = false;
            }
            panel.add(label);
        }

        return panel;
    }
}