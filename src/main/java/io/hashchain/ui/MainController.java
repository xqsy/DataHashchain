package io.hashchain.ui;

import io.hashchain.core.FingerprintDot;
import io.hashchain.core.FingerprintType;
import io.hashchain.core.HashChain;
import io.hashchain.core.HashRecord;
import io.hashchain.core.PersonData;
import io.hashchain.utils.ChainStorage;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class MainController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField patronymicField;
    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private TextField qualityField;
    @FXML
    private ComboBox<FingerprintType> typeCombo;

    @FXML
    private TableView<HashRecord> chainTable;
    @FXML
    private TableColumn<HashRecord, String> hashColumn;
    @FXML
    private TableColumn<HashRecord, String> prevHashColumn;
    @FXML
    private TableColumn<HashRecord, String> fioColumn;
    @FXML
    private TableColumn<HashRecord, String> birthDateColumn;
    @FXML
    private TableColumn<HashRecord, String> fingerprintTypeColumn;
    @FXML
    private TableColumn<HashRecord, String> qualityColumn;

    private final HashChain hashChain = new HashChain();

    @FXML
    private void initialize() {
        typeCombo.getItems().setAll(FingerprintType.values());

        chainTable.setItems(hashChain.getChain());

        birthDatePicker.setValue(LocalDate.now());
        birthDatePicker.getEditor().setPromptText("дд.мм.гггг");

        fioColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(
                        cd.getValue().getData().getLastName() + " " +
                                cd.getValue().getData().getFirstName() + " " +
                                cd.getValue().getData().getPatronymic()
                )
        );
        birthDateColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(
                        String.valueOf(cd.getValue().getData().getBirthDate())
                )
        );
        fingerprintTypeColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(
                        cd.getValue().getData().getFingerprintDot().getType().getDescription()
                )
        );
        qualityColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(
                        String.valueOf(cd.getValue().getData().getFingerprintDot().getQuality())
                )
        );
        hashColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getHash())
        );
        prevHashColumn.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(
                        cd.getValue().getPreviousHash() == null ? "" : cd.getValue().getPreviousHash()
                )
        );
        
        hashColumn.setSortable(false);
        prevHashColumn.setSortable(false);
        fioColumn.setSortable(false);
        birthDateColumn.setSortable(false);
        fingerprintTypeColumn.setSortable(false);
        qualityColumn.setSortable(false);

        ChainStorage.LoadResult result = ChainStorage.load(hashChain);
        if (!result.success) {
            showError(result.errorMessage + " Приложение будет закрыто.");
            Platform.exit();
        }
    }

    @FXML
    private void onAddRecord() {
        if (!validateInputs()) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronymicField.getText().trim();
        LocalDate birthDate = birthDatePicker.getValue();

        int x = Integer.parseInt(xField.getText().trim());
        int y = Integer.parseInt(yField.getText().trim());
        int quality = Integer.parseInt(qualityField.getText().trim());
        FingerprintType type = typeCombo.getValue();

        FingerprintDot dot = new FingerprintDot(x, y, type, quality);
        PersonData person = new PersonData(firstName, lastName, patronymic, birthDate, dot);

        hashChain.addRecord(person);
        saveToFile();
        clearForm();
    }
    private void saveToFile() {
        try {
            ChainStorage.save(hashChain);
        } catch (IOException e) {
            showError("Ошибка при сохранении цепочки: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteSelected() {
        HashRecord selected = chainTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите запись для удаления.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление записи");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить выбранную запись?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        if (hashChain.getChain().isEmpty()) {
            showWarning("Цепочка пуста.");
            return;
        }

        String oldHash = hashChain.computeChainHash();
        int index = chainTable.getSelectionModel().getSelectedIndex();

        hashChain.getChain().remove(index);
        String newHash = hashChain.computeChainHash();

        if (!newHash.equals(oldHash)) {
            hashChain.getChain().add(index, selected);
            showError("Попытка изменить цепочку нарушает целостность.\n" +
                    "Исходный хеш цепочки: " + oldHash + "\n" +
                    "Новый хеш цепочки: " + newHash + "\n" +
                    "Изменения не применены.");
        } else {
            saveToFile();
            showInfo("Удаление не изменило хеш цепочки.\nХеш: " + oldHash);
        }
    }

    private boolean validateInputs() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String patronymic = patronymicField.getText();
        LocalDate birthDate = birthDatePicker.getValue();

        if (firstName == null || firstName.isBlank() ||
                lastName == null || lastName.isBlank() ||
                patronymic == null || patronymic.isBlank() ||
                birthDate == null ||
                xField.getText().isBlank() ||
                yField.getText().isBlank() ||
                qualityField.getText().isBlank() ||
                typeCombo.getValue() == null) {
            showWarning("Заполните все поля перед добавлением записи.");
            return false;
        }

        try {
            Integer.parseInt(xField.getText().trim());
            Integer.parseInt(yField.getText().trim());
            Integer.parseInt(qualityField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Поля X, Y и качество должны быть целыми числами.");
            return false;
        }

        return true;
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        patronymicField.clear();
        birthDatePicker.setValue(LocalDate.now());
        xField.clear();
        yField.clear();
        qualityField.clear();
        typeCombo.setValue(null);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
