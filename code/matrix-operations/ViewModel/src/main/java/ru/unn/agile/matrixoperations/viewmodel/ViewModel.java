package ru.unn.agile.matrixoperations.viewmodel;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.unn.agile.matrixoperations.model.Matrix;

import java.util.ArrayList;
import java.util.List;

public class ViewModel {
    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLUMNS_COUNT = 2;

    private final Matrix leftMatrix = new Matrix(DEFAULT_ROWS_COUNT, DEFAULT_COLUMNS_COUNT);
    private final Matrix rightMatrix = new Matrix(DEFAULT_ROWS_COUNT, DEFAULT_COLUMNS_COUNT);
    private Matrix resultMatrix = new Matrix(DEFAULT_ROWS_COUNT, DEFAULT_COLUMNS_COUNT);

    private final IntegerProperty leftMatrixRows = new SimpleIntegerProperty();
    private final IntegerProperty leftMatrixColumns = new SimpleIntegerProperty();
    private final MatrixViewModel leftMatrixViewModel = new MatrixViewModel(leftMatrix);

    private final IntegerProperty rightMatrixRows = new SimpleIntegerProperty();
    private final IntegerProperty rightMatrixColumns = new SimpleIntegerProperty();
    private final MatrixViewModel rightMatrixViewModel = new MatrixViewModel(rightMatrix);

    private final IntegerProperty resultMatrixRows = new SimpleIntegerProperty();
    private final IntegerProperty resultMatrixColumns = new SimpleIntegerProperty();
    private final MatrixViewModel resultMatrixViewModel = new MatrixViewModel(resultMatrix);

    private final BooleanProperty matrixUpdated = new SimpleBooleanProperty();

    private final ObjectProperty<Matrix.Operation> operation = new SimpleObjectProperty<>();

    private final BooleanProperty calculationDisabled = new SimpleBooleanProperty();

    private final ObjectProperty<Status> status = new SimpleObjectProperty<>();
    private final List<ValueChangeListener> valueChangedListeners = new ArrayList<>();

    private final ObjectProperty<ObservableList<Matrix.Operation>> operations =
            new SimpleObjectProperty<>(
                    FXCollections.observableArrayList(Matrix.Operation.values()));

    public enum Status {
        INVALID_LEFT_MATRIX_ROWS("Left matrix has invalid rows count."),
        INVALID_LEFT_MATRIX_COLS("Left matrix has invalid columns count."),
        INVALID_RIGHT_MATRIX_ROWS("Right matrix has invalid rows count."),
        INVALID_RIGHT_MATRIX_COLS("Right matrix has invalid columns count."),
        INVALID_MATRIX_SIZE("Right matrix has invalid size - it not agreed to left matrix"),
        READY("Press 'Calculate'"),
        SUCCESS("Success");

        private final String description;

        Status(final String description) {
            this.description = description;
        }

        public String toString() {
            return description;
        }
    };

    private class ValueChangeListener implements ChangeListener<Number> {
        @Override
        public void changed(final ObservableValue<? extends Number> observable,
                            final Number oldValue, final Number newValue) {
            matrixUpdated.set(false);
            status.set(getInputStatus());
        }
    }

    // FXML needs default c-tor for binding
    public ViewModel() {
        setDefaultDimensions();
        setDefaultOperations();
        setCalculationBinding();
        setValuesListeners();
    }

    public void calculate() {
        if (calculationDisabled.get()) {
            return;
        }

        resultMatrix = operation.get().apply(leftMatrix, rightMatrix);

        resultMatrixRows.setValue(resultMatrix.getRows());
        resultMatrixColumns.setValue(resultMatrix.getColumns());

        resultMatrixViewModel.setMatrix(resultMatrix);

        status.set(Status.SUCCESS);
    }

    public Integer getDefaultRowsCount() {
        return DEFAULT_ROWS_COUNT;
    }

    public Integer getDefaultColumnsCount() {
        return DEFAULT_COLUMNS_COUNT;
    }

    public IntegerProperty leftMatrixRowsProperty() {
        return leftMatrixRows;
    }

    public IntegerProperty leftMatrixColumnsProperty() {
        return leftMatrixColumns;
    }

    public IntegerProperty rightMatrixRowsProperty() {
        return rightMatrixRows;
    }

    public IntegerProperty rightMatrixColumnsProperty() {
        return rightMatrixColumns;
    }

    public IntegerProperty resultMatrixRowsProperty() {
        return resultMatrixRows;
    }

    public IntegerProperty resultMatrixColumnsProperty() {
        return resultMatrixColumns;
    }

    public void reloadLeftMatrix() {
        reloadMatrix(leftMatrix, leftMatrixViewModel,
                leftMatrixRows.get(), leftMatrixColumns.get());
    }

    public void reloadRightMatrix() {
        reloadMatrix(rightMatrix, rightMatrixViewModel,
                rightMatrixRows.get(), rightMatrixColumns.get());
    }

    public ObjectProperty<Matrix.Operation> operationProperty() {
        return operation;
    }

    Matrix.Operation getOperation() {
        return operation.get();
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    public Status getStatus() {
        return status.get();
    }

    public ObjectProperty<ObservableList<Matrix.Operation>> operationsProperty() {
        return operations;
    }

    public final ObservableList<Matrix.Operation> getOperations() {
        return operations.get();
    }

    public BooleanProperty calculationDisabledProperty() {
        return calculationDisabled;
    }

    public final boolean getCalculationDisabled() {
        return calculationDisabled.get();
    }

    public MatrixViewModel leftMatrix() {
        return leftMatrixViewModel;
    }

    public MatrixViewModel rightMatrix() {
        return rightMatrixViewModel;
    }

    public MatrixViewModel resultMatrix() {
        return resultMatrixViewModel;
    }

    private void reloadMatrix(final Matrix m,
                              final MatrixViewModel mvm,
                              final int rows,
                              final int cols) {
        m.updateDimensions(rows, cols);
        mvm.setMatrix(m);
        status.set(getInputStatus());
        matrixUpdated.set(true);
    }

    private void setDefaultDimensions() {
        leftMatrixRows.setValue(DEFAULT_ROWS_COUNT);
        leftMatrixColumns.setValue(DEFAULT_COLUMNS_COUNT);

        rightMatrixRows.setValue(DEFAULT_ROWS_COUNT);
        rightMatrixColumns.setValue(DEFAULT_COLUMNS_COUNT);

        resultMatrixRows.setValue(DEFAULT_ROWS_COUNT);
        resultMatrixColumns.setValue(DEFAULT_COLUMNS_COUNT);

        status.set(Status.READY);
    }

    private void setDefaultOperations() {
        operation.set(Matrix.Operation.ADD);
    }

    private void setCalculationBinding() {
        BooleanBinding couldCalculate = new BooleanBinding() {
            {
                bind(leftMatrixRows,
                        leftMatrixColumns,
                        rightMatrixRows,
                        rightMatrixColumns,
                        operation);
            }
            @Override
            protected boolean computeValue() {
                return getInputStatus() == Status.READY;
            }
        };
        calculationDisabled.bind(couldCalculate.not());
    }

    private void setValuesListeners() {
        operation.addListener(new ChangeListener<Matrix.Operation>() {
            @Override
            public void changed(final ObservableValue<? extends Matrix.Operation> observable,
                                final Matrix.Operation oldValue, final Matrix.Operation newValue) {
                status.set(getInputStatus());
            }
        });

        final List<IntegerProperty> fields = new ArrayList<IntegerProperty>() {
            {
                add(leftMatrixRows);
                add(leftMatrixColumns);
                add(rightMatrixRows);
                add(rightMatrixColumns);
            }
        };
        for (IntegerProperty field : fields) {
            final ValueChangeListener listener = new ValueChangeListener();
            field.addListener(listener);
            valueChangedListeners.add(listener);
        }
    }

    private Status getInputStatus() {
        if (leftMatrixRows.get() <= 0) {
            return Status.INVALID_LEFT_MATRIX_ROWS;
        }
        if (leftMatrixColumns.get() <= 0) {
            return Status.INVALID_LEFT_MATRIX_COLS;
        }
        if (rightMatrixRows.get() <= 0) {
            return Status.INVALID_RIGHT_MATRIX_ROWS;
        }
        if (rightMatrixColumns.get() <= 0) {
            return Status.INVALID_RIGHT_MATRIX_COLS;
        }

        if (operation.get() == Matrix.Operation.ADD) {
            if ((leftMatrixRows.get() == rightMatrixRows.get())
                    && (leftMatrixColumns.get() == rightMatrixColumns.get())) {
                return Status.READY;
            }
        } else {
            if (leftMatrixColumns.get() == rightMatrixRows.get()) {
                return Status.READY;
            }
        }

        return Status.INVALID_MATRIX_SIZE;
    }

}
