package com.hotel.app.controller;

import com.hotel.app.model.*;
import com.hotel.app.repository.DataStore;
import com.hotel.app.util.BookingProcessor;
import com.hotel.app.util.ClockThread;
import com.hotel.app.util.FileHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class DashboardController {

    public Label lblClock, lblInvoiceDetails, lblStatus;
    public Label lblOccupancy, lblTotalBookings, lblTotalRevenue;

    public TableView<Room> roomTable;
    public TableColumn<Room, String> colRoomNo;
    public TableColumn<Room, RoomType> colType;
    public TableColumn<Room, Double> colPrice;
    public TableColumn<Room, Boolean> colAvail;

    public TextField txtRoomNo, txtPrice, txtSearchRooms, txtSearchBookings;
    public ComboBox<RoomType> cbRoomType;

    // Booking
    public TextField txtGuestName, txtGuestContact, txtIdProof;
    public ComboBox<Room> cbAvailableRooms;
    public DatePicker dpCheckIn, dpCheckOut;

    // Billing
    public TableView<Booking> bookingTable;
    public TableColumn<Booking, String> colBookId;
    public TableColumn<Booking, String> colGuestName;
    public TableColumn<Booking, String> colBookRoom;
    public TableColumn<Booking, Boolean> colCheckedOut;

    private DataStore dataStore;
    private ClockThread clockThread;
    private BookingProcessor bookingProcessor;

    private ObservableList<Room> masterRoomList = FXCollections.observableArrayList();
    private ObservableList<Booking> masterBookingList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dataStore = new DataStore();

        // Start Clock
        clockThread = new ClockThread(lblClock);
        clockThread.start();

        // Start processor
        bookingProcessor = new BookingProcessor();
        bookingProcessor.setOnBookingProcessed(() -> {
            Platform.runLater(() -> {
                refreshData();
                lblStatus.setText("Booking Batch Processed Successfully");
            });
        });
        bookingProcessor.startProcessorThread();

        setupRoomTable();
        setupBookingTable();
        setupSearch();
        addContextMenus();
        refreshData();
        lblStatus.setText("System Initialized with JDBC Integrity");
    }

    private void setupSearch() {
        FilteredList<Room> filteredRooms = new FilteredList<>(masterRoomList, p -> true);
        txtSearchRooms.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredRooms.setPredicate(room -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (room.getRoomNumber().contains(newValue))
                    return true;
                if (room.getRoomType().name().toLowerCase().contains(lowerCaseFilter))
                    return true;
                return false;
            });
        });
        roomTable.setItems(filteredRooms);

        FilteredList<Booking> filteredBookings = new FilteredList<>(masterBookingList, p -> true);
        txtSearchBookings.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBookings.setPredicate(booking -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (booking.getGuest().getName().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (booking.getBookingId().toLowerCase().contains(lowerCaseFilter))
                    return true;
                return false;
            });
        });
        bookingTable.setItems(filteredBookings);
    }

    private void addContextMenus() {
        ContextMenu roomMenu = new ContextMenu();
        MenuItem editRoom = new MenuItem("Edit Room Details");
        MenuItem deleteRoom = new MenuItem("Delete Room");
        roomMenu.getItems().addAll(editRoom, deleteRoom);
        roomTable.setContextMenu(roomMenu);
        editRoom.setOnAction(event -> handleEditRoom());
        deleteRoom.setOnAction(event -> handleDeleteRoom());

        ContextMenu guestMenu = new ContextMenu();
        MenuItem editGuest = new MenuItem("Edit Guest Details");
        MenuItem deleteGuest = new MenuItem("Delete Guest Record");
        guestMenu.getItems().addAll(editGuest, deleteGuest);
        bookingTable.setContextMenu(guestMenu);
        editGuest.setOnAction(event -> handleEditGuest());
        deleteGuest.setOnAction(event -> handleDeleteGuest());
    }

    private void handleEditRoom() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtRoomNo.setText(selected.getRoomNumber());
            txtPrice.setText(String.valueOf(selected.getPricePerNight()));
            cbRoomType.setValue(selected.getRoomType());
            lblStatus.setText("Editing Room " + selected.getRoomNumber());
        }
    }

    private void handleDeleteRoom() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean success = dataStore.deleteRoom(selected.getRoomNumber());
            if (success) {
                refreshData();
                lblStatus.setText("Room " + selected.getRoomNumber() + " deleted.");
            } else {
                showAlert("Delete Error", "Room is occupied and cannot be deleted.");
            }
        }
    }

    private void handleEditGuest() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog(selected.getGuest().getName());
            dialog.setTitle("Edit Guest");
            dialog.setHeaderText("Update name and contact");
            dialog.setContentText("New Name:");
            dialog.showAndWait().ifPresent(newName -> {
                TextInputDialog phoneDialog = new TextInputDialog(selected.getGuest().getContact());
                phoneDialog.setTitle("Edit Guest");
                phoneDialog.setContentText("New Phone (10 digits):");
                phoneDialog.showAndWait().ifPresent(newPhone -> {
                    if (newName.matches("^[a-zA-Z\\s]+$") && newPhone.matches("^[0-9]{10}$")) {
                        selected.getGuest().setName(newName);
                        selected.getGuest().setContact(newPhone);
                        dataStore.updateGuest(selected.getGuest());
                        refreshData();
                    } else {
                        showAlert("Validation Error", "Invalid name or phone format.");
                    }
                });
            });
        }
    }

    private void handleDeleteGuest() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean success = dataStore.deleteGuest(selected.getGuest().getId());
            if (success) {
                refreshData();
                lblStatus.setText("Guest record removed.");
            } else {
                showAlert("Delete Error", "Cannot delete a guest with active bookings.");
            }
        }
    }

    private void setupRoomTable() {
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colAvail.setCellValueFactory(new PropertyValueFactory<>("isAvailable"));
        colAvail.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item ? "AVAILABLE" : "OCCUPIED");
                    lbl.setStyle(item
                            ? "-fx-background-color: #238636; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4;"
                            : "-fx-background-color: #da3633; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4;");
                    setGraphic(lbl);
                }
            }
        });
        cbRoomType.setItems(FXCollections.observableArrayList(RoomType.values()));
    }

    private void setupBookingTable() {
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colGuestName.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getName()));
        colBookRoom.setCellValueFactory(cellData -> {
            Room r = cellData.getValue().getRoom();
            return new javafx.beans.property.SimpleStringProperty(r != null ? r.getRoomNumber() : "DELETED");
        });
        // Fixing UI refresh - adding custom cell factory for Status Badges
        colCheckedOut.setCellValueFactory(new PropertyValueFactory<>("checkedOut"));
        colCheckedOut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item ? "YES" : "NO");
                    lbl.setStyle(item
                            ? "-fx-background-color: #238636; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4;"
                            : "-fx-background-color: #8b949e; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4;");
                    setGraphic(lbl);
                }
            }
        });
    }

    public void addRoom(ActionEvent actionEvent) {
        if (txtRoomNo.getText().isEmpty() || txtPrice.getText().isEmpty() || cbRoomType.getValue() == null) {
            showAlert("Validation Error", "Please fill all fields.");
            return;
        }
        try {
            String num = txtRoomNo.getText();
            Double price = Double.parseDouble(txtPrice.getText());
            Room room = new Room(num, cbRoomType.getValue(), price, true);
            dataStore.addRoom(room);
            txtRoomNo.clear();
            txtPrice.clear();
            refreshData();
            lblStatus.setText("Room " + num + " updated in DB");
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Invalid price format.");
        }
    }

    public void bookRoom(ActionEvent actionEvent) {
        if (txtGuestName.getText().isEmpty() || cbAvailableRooms.getValue() == null) {
            showAlert("Validation Error", "Fields missing.");
            return;
        }

        LocalDate in = dpCheckIn.getValue();
        LocalDate out = dpCheckOut.getValue();
        if (out == null || in == null || out.isBefore(in)) {
            showAlert("Date Error", "Invalid arrival/departure window.");
            return;
        }

        String contact = txtGuestContact.getText();
        if (!contact.matches("^[0-9]{10}$")) {
            showAlert("Input Error", "Contact MUST be 10 digits.");
            return;
        }

        Guest guest = new Guest(UUID.randomUUID().toString(), txtGuestName.getText(), contact, txtIdProof.getText());
        Booking booking = new Booking(UUID.randomUUID().toString().substring(0, 8), guest, cbAvailableRooms.getValue(),
                in, out);

        new Thread(() -> {
            bookingProcessor.submitBooking(booking);
        }).start();

        dataStore.addBooking(booking);
        txtGuestName.clear();
        txtGuestContact.clear();
        txtIdProof.clear();
        refreshData();
    }

    public void checkout(ActionEvent actionEvent) {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isCheckedOut()) {
            dataStore.checkoutBooking(selected);

            // Real-time UI Update: Modify in-place to ensure TableView renders immediately
            selected.setCheckedOut(true);
            if (selected.getRoom() != null) {
                selected.getRoom().setIsAvailable(true);
            }

            long nights = ChronoUnit.DAYS.between(selected.getCheckInDate(), selected.getCheckOutDate());
            if (nights <= 0)
                nights = 1;

            double total = 0;
            String roomInfo = "DELETED";
            if (selected.getRoom() != null) {
                double base = selected.getRoom().calculatePrice((int) nights);
                total = (nights > 3) ? selected.getRoom().calculatePrice((int) nights, 10.0) : base;
                roomInfo = selected.getRoom().getRoomNumber();
            }

            lblInvoiceDetails.setText(String.format("Guest: %s\nRoom: %s\nTotal Days: %d\nAmount: ₹%.2f",
                    selected.getGuest().getName(), roomInfo, nights, total));
            lblStatus.setText("Checkout Processed (Data Sanitized).");
            refreshData(); // Sync everything else (stats, counts)
            bookingTable.refresh();
            roomTable.refresh();
        } else if (selected != null) {
            showAlert("Info", "Guest already checked out.");
        }
    }

    public void performBackup(ActionEvent actionEvent) {
        dataStore.performBackup();
        lblStatus.setText("Serialization Backup Created");
    }

    public void exportLogs(ActionEvent actionEvent) {
        FileHelper.exportLogsToCsv();
        lblStatus.setText("Logs exported as CSV");
    }

    private void refreshData() {
        masterRoomList.setAll(dataStore.getRooms());
        masterBookingList.setAll(dataStore.getBookings());

        ObservableList<Room> availRooms = FXCollections.observableArrayList();
        for (Room r : masterRoomList) {
            if (r.getIsAvailable())
                availRooms.add(r);
        }
        cbAvailableRooms.setItems(availRooms);
        updateStats();
    }

    private void updateStats() {
        int total = masterRoomList.size();
        int occupied = 0;
        double revenue = 0;
        for (Room r : masterRoomList) {
            if (!r.getIsAvailable())
                occupied++;
        }
        for (Booking b : masterBookingList) {
            if (b.isCheckedOut() && b.getRoom() != null) {
                long nights = ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate());
                if (nights <= 0)
                    nights = 1;
                revenue += (nights > 3) ? b.getRoom().calculatePrice((int) nights, 10.0)
                        : b.getRoom().calculatePrice((int) nights);
            }
        }

        double occupancyRate = (total > 0) ? (double) occupied / total * 100 : 0;
        lblOccupancy.setText(String.format("%.1f%%", occupancyRate));
        lblTotalBookings.setText(String.valueOf(masterBookingList.size()));
        lblTotalRevenue.setText(String.format("₹%.2f", revenue));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
