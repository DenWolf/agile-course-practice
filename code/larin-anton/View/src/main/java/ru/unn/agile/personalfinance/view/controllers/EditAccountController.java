package ru.unn.agile.personalfinance.view.controllers;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import ru.unn.agile.PersonalFinance.ViewModel.AccountViewModel;
import ru.unn.agile.personalfinance.view.WindowsManager;
import ru.unn.agile.personalfinance.view.utils.Converters;

public class EditAccountController extends DataContextController<AccountViewModel> {
    @FXML
    private TextField nameField;

    @FXML
    private TextField balanceField;

    @FXML
    private Button addButton;

    @FXML
    private void handleAddButton(final ActionEvent actionEvent) {
        getDataContext().save();
        WindowsManager.getInstance().goBack();
    }

    @FXML
    private void handleCancelButton(final ActionEvent actionEvent) {
        WindowsManager.getInstance().goBack();
    }

    @Override
    protected void removeBindings(final AccountViewModel account) {
        Bindings.unbindBidirectional(nameField.textProperty(), account.nameProperty());
        Bindings.unbindBidirectional(balanceField.textProperty(), account.balanceProperty());
        balanceField.disableProperty().unbind();
        addButton.disableProperty().unbind();

        account.revertChanges();
    }

    @Override
    protected void addBindings(final AccountViewModel account) {
        account.startChanging();

        /* nameField.text <-> account.name */
        Bindings.bindBidirectional(nameField.textProperty(), account.nameProperty());

        /* balanceField.text <-> account.balance */
        Bindings.bindBidirectional(
                balanceField.textProperty(),
                account.balanceProperty(),
                Converters.getCurrencyStringConverter());

        /* account.changingProperty -> balanceField.disabled */
        balanceField.disableProperty().bind(account.changingProperty());

        /* account.isAbleToSave -> addButton.disabled */
        addButton.disableProperty().bind(account.ableToSaveProperty().not());
    }
}
