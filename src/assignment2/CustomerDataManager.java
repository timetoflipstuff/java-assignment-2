package assignment2;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

final class Env {
    final static String DATA_FILE = "./src/customers.dat";
    final static boolean CREATE_DB_IF_NOT_EXISTS = true;
}

/**
 * Provides utility to manage customer data and listen to any changes
 */
public class CustomerDataManager {

    private final List<CustomerDataModel> list = new ArrayList<>();
    private DataChangeListener dataChangeListener = null;

    public CustomerDataManager() throws IOException, ClassNotFoundException {
        createDbIfRequired();
        readDatabase();
        //TO get notified when app is shutting down, so we can save data.
        Runtime.getRuntime().addShutdownHook(new Thread(this::performShutdown));
    }

    public void setListener(DataChangeListener dataChangeListener) {
        this.dataChangeListener = dataChangeListener;
    }

    /**
     * @return All customers entries
     */
    public List<CustomerDataModel> getCustomers() {
        return list;
    }

    /**
     * @param id customer ID
     * @return optional customer of id passed
     */
    public Optional<CustomerDataModel> getCustomerById(int id) {
        return list.stream().filter(item -> item.id() == id).findFirst();
    }

    /**
     * Adds customer to database.
     *
     * @param id         Unique Customer ID
     * @param name
     * @param phone
     * @param email
     * @param postalCode
     * @return true is customer added successfully, false if customer exists in database with given ID.
     */
    public boolean addCustomer(int id, String name, String phone, String email, String postalCode) {
        if (list.stream().anyMatch(item -> item.id() == id)) {
            return false;
        }

        CustomerDataModel customerDataModel = new CustomerDataModel(id, name, phone, email, postalCode);
        list.add(0, customerDataModel);
        // notify listeners
        if (dataChangeListener != null){
            dataChangeListener.onItemAdded(customerDataModel);
        }
        return true;
    }

    /**
     * Edits (or rather deletes are re-adds) an existing customer entry
     *
     * @param id         unique customer ID
     * @param name
     * @param phone
     * @param email
     * @param postalCode
     * @return if edit is success
     */
    public boolean editCustomer(int id, String name, String phone, String email, String postalCode) {
        int index = IntStream.range(0, list.size())
                .filter(i -> list.get(i).id() == id)
                .findFirst()
                .orElse(-1);

        if (index == -1) {
            return false;
        } else {
            list.remove(index);
            CustomerDataModel model = new CustomerDataModel(id, name, phone, email, postalCode);
            list.add(index, model);

            if (dataChangeListener != null){
                dataChangeListener.onItemChanged(index, model);
            }
            return true;
        }
    }

    public void printEntireDatabase(){
        for (CustomerDataModel customerDataModel :
                list) {
            System.out.println(customerDataModel.toString());
        }
    }

    //TODO
    public void nukeData() {

    }

    /*
     *
     *
     * Private class utility functions
     */

    private void performShutdown() {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(Env.DATA_FILE))) {
            for (CustomerDataModel customer : list) {
                objectOutputStream.writeObject(customer);
            }
        } catch (IOException e) {
            System.err.println("Data save failed!");
            e.printStackTrace();
        }
    }

    private void createDbIfRequired() {
        if (Env.CREATE_DB_IF_NOT_EXISTS) {

            File data = new File(Env.DATA_FILE);
            if (!data.exists()) {
                try {
                    boolean created = data.createNewFile();
                    System.out.println("Is new db file created? "+ created);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * reads database once at initialisation.
     * EOF is handled, rest are thrown for parents to take care.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readDatabase() throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(Env.DATA_FILE));
            Object obj = null;
            while (true) {
                obj = inputStream.readObject();
                list.add((CustomerDataModel) obj);
            }
        } catch (EOFException e) {
            //we ignore this as this is thrown when file read is complete.
        }
    }
}


/**
 * Product data model that holds single item data and is passed around the project
 */
record CustomerDataModel(int id, String name, String phone, String email,
                         String postalCode) implements Serializable {
}

/**
 * Listener interface that notifies about changes in dataset
 */
interface DataChangeListener {
    /**
     * Called when Item is changed
     *
     * @param pos               position of the item
     * @param customerDataModel updated data model
     */
    void onItemChanged(int pos, CustomerDataModel customerDataModel);

    /**
     * Called when customer is added. Customers are always added at index 0.
     *
     * @param customerDataModel new customer
     */
    void onItemAdded(CustomerDataModel customerDataModel);
}



