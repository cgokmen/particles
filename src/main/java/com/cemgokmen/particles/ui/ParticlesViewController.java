/*
 * Particles, a self-organizing particle system simulator.
 * Copyright (C) 2018  Cem Gokmen.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cemgokmen.particles.ui;

import com.cemgokmen.particles.algorithms.AlignmentAlgorithm;
import com.cemgokmen.particles.algorithms.ParticleAlgorithm;
import com.cemgokmen.particles.generators.RandomSystemGenerator;
import com.cemgokmen.particles.graphics.GridGraphics;
import com.cemgokmen.particles.graphics.MultipagePDFHandler;
import com.cemgokmen.particles.io.GridIO;
import com.cemgokmen.particles.io.SampleSystemMetadata;
import com.cemgokmen.particles.models.amoebot.gridshapes.ToroidalAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;
import com.cemgokmen.particles.util.PropertyUtils;
import com.cemgokmen.particles.util.Utils;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParticlesViewController {
    private ParticleGrid grid;
    private MultipagePDFHandler multipagePDFHandler;

    @FXML
    private VBox rootVBox;

    @FXML
    private MenuItem newSystemMenuItem;
    @FXML
    private MenuItem openSystemMenuItem;
    @FXML
    private Menu openSampleMenu;
    @FXML
    private MenuItem closeSystemMenuItem;
    @FXML
    private MenuItem saveSystemMenuItem;
    @FXML
    private MenuItem preferencesMenuItem;
    @FXML
    private MenuItem quitMenuItem;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private ListView systemInfoListView;
    @FXML
    private TableView systemPreferenceTableView;
    @FXML
    private TableColumn<PropertyUtils.PropertyWrapper, String> systemPreferenceParameterCol;
    @FXML
    private TableColumn<PropertyUtils.PropertyWrapper, String> systemPreferenceValueCol;

    @FXML
    private ComboBox algorithmComboBox;

    @FXML
    private TextField runIterationsTextField;
    @FXML
    private Button runIterationsButton;

    @FXML
    private Button saveImageButton;
    @FXML
    private Button saveRasterImageButton;
    @FXML
    private Button saveStateButton;
    @FXML
    private Button startMultipageButton;
    @FXML
    private Button addMultipageButton;
    @FXML
    private Button saveMultipageButton;

    @FXML
    private ImageView systemImageView;

    @FXML
    private ListView particleInfoListView;
    @FXML
    private TableView particlePreferenceTableView;
    @FXML
    private TableColumn particlePreferenceParameterCol;
    @FXML
    private TableColumn particlePreferenceValueCol;

    @FXML
    private ProgressBar progressBar;

    private final FileChooser fileChooser = new FileChooser();

    @FXML
    protected void newSystemHandler(ActionEvent actionEvent) {
        this.dialog(Alert.AlertType.ERROR, "Error", "Unimplemented feature", "This feature is not available yet.");
    }

    @FXML
    protected void openSystemHandler(ActionEvent actionEvent) {
        File file = this.fileChooser.showOpenDialog(this.rootVBox.getScene().getWindow());
        if (file != null) {
            try {
                this.loadSystem(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                this.dialog(Alert.AlertType.ERROR, "Error", "File not found", "The file you selected could not be read.");
            }
        }
    }

    @FXML
    protected void closeSystemHandler(ActionEvent actionEvent) {
        // TODO: Easy feature
        this.dialog(Alert.AlertType.ERROR, "Error", "Unimplemented feature", "This feature is not available yet.");
    }

    @FXML
    protected void saveSystemHandler(ActionEvent actionEvent) {
        this.dialog(Alert.AlertType.ERROR, "Error", "Unimplemented feature", "This feature is not available yet.");
    }

    @FXML
    protected void preferencesHandler(ActionEvent actionEvent) {
        this.dialog(Alert.AlertType.ERROR, "Error", "Unimplemented feature", "This feature is not available yet.");
    }

    @FXML
    protected void quitHandler(ActionEvent actionEvent) {
        ((Stage) this.rootVBox.getScene().getWindow()).close();
    }

    @FXML
    protected void aboutHandler(ActionEvent actionEvent) {
        this.dialog(Alert.AlertType.INFORMATION, "About Particles", "Particles v1.0", "Particles is developed by Cem Gokmen as part of an ongoing research project at Georgia Tech. @2018, All Rights Reserved.");
    }

    @FXML
    protected void runIterationsHandler(ActionEvent actionEvent) {
        String input = this.runIterationsTextField.getText();
        try {
            int iterations = Integer.parseInt(input);
            if (iterations <= 0) {
                throw new Exception("Invalid iteration count");
            }

            this.runIterations(iterations);
        } catch (Exception e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "Invalid Iteration Count", "You entered an invalid number of iterations. Please enter a positive integer.");
        }
    }

    @FXML
    protected void saveImageHandler(ActionEvent actionEvent) {
        try {
            File file = this.fileChooser.showSaveDialog(this.rootVBox.getScene().getWindow());
            if (file != null) {
                GridGraphics.saveGridImage(this.grid, file);
                this.dialog(Alert.AlertType.INFORMATION, "Success", "Image saved", "An image of the current grid has been saved.");
            }
        } catch (Exception e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "Could not save image. Was the extension valid?");
        }
    }

    @FXML
    protected void saveStateHandler(ActionEvent actionEvent) {
        this.dialog(Alert.AlertType.ERROR, "Error", "Unimplemented feature", "This feature is not available yet.");
    }

    public void particlePreferenceEditHandler(TableColumn.CellEditEvent cellEditEvent) {
    }

    protected void runIterations(final int n) {
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                int bundle = n / 100;
                int run = 0;
                for (int i = 0; i < 100; i++) {
                    ParticlesViewController.this.grid.runActivations(bundle);
                    run += bundle;
                    this.updateProgress(run, n);
                }

                ParticlesViewController.this.grid.runActivations(n - run);
                this.updateProgress(n, n);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                ParticlesViewController.this.returnFromIterations();
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                ParticlesViewController.this.returnFromIterations();
            }

            @Override
            protected void failed() {
                super.failed();
                ParticlesViewController.this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred during execution.", this.getException().getMessage());
                this.getException().printStackTrace();
                ParticlesViewController.this.returnFromIterations();
            }
        };

        this.progressBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    private void returnFromIterations() {
        this.progressBar.progressProperty().unbind();
        this.progressBar.setProgress(0);

        this.updateSystemData();
    }

    private void gridLoaded() {
        this.updateAlgorithms();
        this.updateSystemInformation();
        this.updateSystemImage();
    }

    private void updateSystemData() {
        this.updateSystemInformation();
        this.updateSystemImage();
    }

    private void updateSystemImage() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgGraphics = (Graphics2D) img.getGraphics();

        GridGraphics.drawGridOntoGraphics(this.grid, imgGraphics, 600, Color.WHITE);

        Image fxImg = SwingFXUtils.toFXImage(img, null);

        this.systemImageView.setImage(fxImg);
    }

    private void updateSystemInformation() {
        this.systemInfoListView.getItems().clear();

        for (Map.Entry<String, String> entry : this.grid.getGridInformation().entrySet()) {
            this.systemInfoListView.getItems().add(entry.getKey() + ": " + entry.getValue());
        }
    }

    private void updateAlgorithms() {
        this.algorithmComboBox.getItems().clear();

        for (Class<? extends ParticleAlgorithm> algorithmClass : this.grid.getCompatibleAlgorithms()) {
            this.algorithmComboBox.getItems().add(algorithmClass);
        }
    }

    private void dialog(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }

    public void algorithmSelectedHandler(ActionEvent actionEvent) {
        try {
            Class<? extends ParticleAlgorithm> klass = (Class<? extends ParticleAlgorithm>) this.algorithmComboBox.getValue();

            if (klass == null) {
                this.grid.assignAllParticlesAlgorithm(null);
                return;
            }

            Constructor<? extends ParticleAlgorithm> constructor = (Constructor<? extends ParticleAlgorithm>) Utils.getZeroParameterPublicConstructor(klass);
            if (constructor == null) {
                throw new Exception("The chosen algorithm class is invalid (does not have a default constructor).");
            }

            ParticleAlgorithm algorithm = constructor.newInstance();
            this.grid.assignAllParticlesAlgorithm(algorithm);
            this.dialog(Alert.AlertType.INFORMATION, "Success", "Algorithm assigned",
                    algorithm.getClass().getName() + " has been assigned to the system.");

            this.systemPreferenceTableView.setItems(FXCollections.observableList(PropertyUtils.getPropertyWrappersFromObject(algorithm, ParticleAlgorithm.class)));

            this.systemPreferenceParameterCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            this.systemPreferenceValueCol.setCellValueFactory(cellData -> cellData.getValue().dataProperty());

            this.systemPreferenceValueCol.setCellFactory(TextFieldTableCell.forTableColumn());
            /*this.systemPreferenceValueCol.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<PropertyUtils.PropertyWrapper, Number>>() {
                        @Override
                        public void handle(TableColumn.CellEditEvent<PropertyUtils.PropertyWrapper, Number> t) {
                            PropertyUtils.PropertyWrapper wrapper = ((PropertyUtils.PropertyWrapper) t.getTableView().getItems().get(
                                    t.getTablePosition().getRow()));
                            //wrapper.propertyProperty().setValue(t.getNewValue());
                        }
                    }
            );*/
            this.systemPreferenceValueCol.setEditable(true);

            this.updateSystemInformation();
        } catch (Exception e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "An unexpected error occurred during algorithm assignment.");
            e.printStackTrace();
        }
    }

    private void loadSystem(InputStream in) {
        ChoiceDialog<Class<? extends ParticleGrid>> dialog = new ChoiceDialog<>(null, new ArrayList<>(GridIO.ALLOWED_GRID_TYPES));

        dialog.setTitle("Grid Type");
        dialog.setHeaderText("Choose a type for the Grid in the file");
        dialog.setContentText("Choose your type:");

        Optional<Class<? extends ParticleGrid>> result = dialog.showAndWait();
        Class<? extends ParticleGrid> gridClass = result.orElse(null);

        this.loadSystem(in, gridClass);
    }

    private void loadSystem(InputStream in, Class<? extends ParticleGrid> gridClass) {
        ChoiceDialog<Class<? extends Particle>> dialog = new ChoiceDialog<>(null, new ArrayList<>(GridIO.ALLOWED_PARTICLE_TYPES));

        dialog.setTitle("Particle Type");
        dialog.setHeaderText("Choose a type for the Particles in the file");
        dialog.setContentText("Choose your type:");

        Optional<Class<? extends Particle>> result = dialog.showAndWait();
        Class<? extends Particle> particleClass = result.orElse(null);

        this.loadSystem(in, gridClass, particleClass);
    }

    private void loadSystem(InputStream in, Class<? extends ParticleGrid> gridClass, Class<? extends Particle> particleClass) {
        try {
            this.grid = GridIO.importParticlesFromInputStream(in, gridClass, particleClass);
            this.gridLoaded();
        } catch (GridIO.InvalidParticleClassException e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "Invalid particle type", "The particle type you chose was invalid.");
        } catch (GridIO.InvalidGridClassException e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "Invalid grid type", "The grid type you chose was invalid.");
        }
    }

    private void loadSampleSystem(SampleSystemMetadata system) {
        try {
            this.grid = GridIO.importSampleSystem(system);
            this.gridLoaded();
        } catch (GridIO.InvalidParticleClassException e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "Invalid particle type", "The particle type was invalid.");
        } catch (GridIO.InvalidGridClassException e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "Invalid grid type", "The grid type was invalid.");
        } catch (IOException e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "Could not load system", "There was an error accessing the sample system.");
        }
    }

    public void initialize() {
        Map<String, Menu> menus = new LinkedHashMap<>();

        for (SampleSystemMetadata system: SampleSystemMetadata.values()) {
            String path = system.humanReadableName;
            String[] sections = path.split("/", 2); // TODO: FIX THIS

            if (!menus.containsKey(sections[0])) {
                menus.put(sections[0], new Menu(sections[0]));
            }

            MenuItem item = new MenuItem(sections[1]);
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    ParticlesViewController.this.loadSampleSystem(system);
                }
            });

            menus.get(sections[0]).getItems().add(item);
        }

        this.openSampleMenu.getItems().addAll(menus.values());
    }

    public void startMultipageHandler(ActionEvent actionEvent) {
        if (this.multipagePDFHandler != null) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "A multipage file is already open. Close that one first!");
            return;
        }

        try {
            File file = this.fileChooser.showSaveDialog(this.rootVBox.getScene().getWindow());
            if (file != null) {
                try {
                    this.multipagePDFHandler = GridGraphics.createMultipagePDF(file);
                    this.dialog(Alert.AlertType.INFORMATION, "Success", "Multipage file opened", "You can now save images onto this file.");
                } catch (FileNotFoundException e) {
                    this.dialog(Alert.AlertType.ERROR, "Error", "File not found", "The file you selected could not be read.");
                }
            }
        } catch (Exception e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "Could not open multipage file.");
        }
    }

    public void addMultipageHandler(ActionEvent actionEvent) {
        if (this.multipagePDFHandler == null) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "A multipage file is not open at the moment. Open one first!");
            return;
        }

        try {
            GridGraphics.drawGridOntoMultipagePDF(this.grid, this.multipagePDFHandler);
        } catch (IOException e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "Could not write to multipage file.");
        }
    }

    public void saveMultipageHandler(ActionEvent actionEvent) {
        if (this.multipagePDFHandler == null) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "A multipage file is not open at the moment. Open one first!");
            return;
        }

        try {
            this.multipagePDFHandler.close();
            this.dialog(Alert.AlertType.INFORMATION, "Success", "Multipage file saved", "You have successfully saved the multipage file.");
            this.multipagePDFHandler = null;
        } catch (IOException e) {
            this.dialog(Alert.AlertType.ERROR, "Error", "An error occurred", "Could not close multipage file.");
        }
    }
}
