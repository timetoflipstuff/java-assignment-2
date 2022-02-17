package assignment2;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CustomerDataManagerTest {

    public static void main(String[] args) {

        CustomerDataManager manager;
        try {
            manager = new CustomerDataManager();
            manager.setListener(dataChangeListener);

            int id = 102;
            String editName = "Ankit2";
            System.out.println("Adding data ID : " +id);
            boolean isAdded = manager.addCustomer(id, "Ankit1", "647-967 8119", "ankit@gmail.com", "m9a4y1");

            if (!isAdded){
                System.err.println("Add failed");
                return;
            }

            Optional<CustomerDataModel> item = manager.getCustomerById(id);
            if (item.isEmpty()){
                System.err.println("ERR: Item with ID "+id+" should exists but it doesn't.");
                return;
            }

            if (item.get().id() != id){
                System.err.println("ERR: Item with ID "+id+" requested but got "+ item.get().id()+" instead");
                return;
            }

            boolean isEdited = manager.editCustomer(id, editName, "647-967 8119", "ankit@gmail.com", "m9a4y1");

            if (!isEdited){
                System.err.println("Edit failed");
            }

            item = manager.getCustomerById(id);
            if (item.isEmpty()){
                System.err.println("ERR: Item with ID "+id+" should be present");
                return;
            }

            if (!Objects.equals(item.get().name(), editName)){
                System.err.println("ERR: Item with ID "+id+" should have name updated to "+editName+", but it is ");
                return;
            }

            System.out.println("Test Success :)");

            System.out.println("-----------------------");
            manager.printEntireDatabase();
            System.out.println("-----------------------");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


    private static final DataChangeListener dataChangeListener = new DataChangeListener() {
        @Override
        public void onItemChanged(int pos, CustomerDataModel customerDataModel) {
            System.out.println("Item changed at "+pos+" "+customerDataModel.toString());
        }

        @Override
        public void onItemAdded(CustomerDataModel customerDataModel) {
            System.out.println("Item added "+customerDataModel.toString());

        }
    };
}
