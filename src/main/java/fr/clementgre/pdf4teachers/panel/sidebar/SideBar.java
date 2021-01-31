package fr.clementgre.pdf4teachers.panel.sidebar;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class SideBar extends TabPane {

    public static final String TAB_DRAG_KEY = "SideBarTabDrag";

    public static Tab draggingTab = null;

    public static int DEFAULT_WIDTH = 270;
    public static int MAX_WIDTH = 400;

    private static final String STYLE = "-fx-tab-max-width: 22px;";

    private final boolean left;
    public SideBar(boolean left){
        this.left = left;

        setStyle(STYLE);

        setMaxWidth(DEFAULT_WIDTH);
        setMinWidth(0);
        setPrefWidth(DEFAULT_WIDTH);
        setWidth(DEFAULT_WIDTH);

        SplitPane.setResizableWithParent(this, false);

        getTabs().addListener((ListChangeListener<Tab>) c -> {
            c.next();
            if(getTabs().size() == 0){
                setWidthByEditingDivider(0);
                setMaxWidth(0);
            }else if(c.wasAdded() && getTabs().size() == 1){
                Platform.runLater(() -> {
                    setMaxWidth(MAX_WIDTH);
                    if(getWidth() <= 50){
                        setWidthByEditingDivider(DEFAULT_WIDTH);
                    }
                });
            }
        });

        AtomicReference<TabPane> previewLastTabPane = new AtomicReference<>(null);
        AtomicReference<Tab> previewTab = new AtomicReference<>(null);

        setOnDragOver(e -> {
            final Dragboard dragboard = e.getDragboard();
            if(TAB_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){

                if(draggingTab != null){

                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();

                    if(draggingTab.getTabPane() == this){ // Skip if tab is already in preview / already in this tab
                        int actualIndex = getTabs().indexOf(draggingTab);
                        int targetIndex = StringUtils.clamp((int) ((e.getX()-5) / 55), 0, getTabs().size()-1);

                        if(actualIndex != targetIndex){
                            getTabs().remove(draggingTab);
                            getTabs().add(targetIndex, draggingTab);
                            getSelectionModel().select(draggingTab);
                        }

                        return;
                    }

                    previewLastTabPane.set(draggingTab.getTabPane());
                    previewTab.set(draggingTab);


                    draggingTab.getTabPane().getTabs().remove(draggingTab);
                    int targetIndex = StringUtils.clamp((int) ((e.getX()-5) / 55), 0, getTabs().size()-1);
                    getTabs().add(targetIndex, draggingTab);
                    getSelectionModel().select(draggingTab);

                    SideBar.hideDragSpaces();
                }
            }

        });
        setOnDragExited(e -> { // Remove the tab of this TabPane and re-add it into its original TabPane
            final Dragboard dragboard = e.getDragboard();
            if(TAB_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){
                if(draggingTab != null && previewTab.get() != null){ // Check there is a tab who is temporary in this TabPane
                    if(draggingTab.getTabPane() != previewLastTabPane.get()){ // Check the Tab is not already into the target TabPane
                        getTabs().remove(draggingTab);
                        previewLastTabPane.get().getTabs().add(draggingTab);
                        previewLastTabPane.get().getSelectionModel().select(draggingTab);
                    }
                    previewTab.set(null);
                    previewLastTabPane.set(null);
                    SideBar.showDragSpaces();
                }
            }
        });
        setOnDragDropped(event -> { // Complete drop : Make the preview final
            final Dragboard dragboard = event.getDragboard();
            if(TAB_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){
                if(draggingTab != null){
                    previewTab.set(null);
                    previewLastTabPane.set(null);

                    event.setDropCompleted(true);
                    event.consume();
                    SideBar.hideDragSpaces();
                }
            }
        });

    }

    public void setWidthByEditingDivider(double width){
        if(left){
            MainWindow.mainPane.setDividerPosition(0, width / MainWindow.mainPane.getWidth());
        }else{
            MainWindow.mainPane.setDividerPosition(1, (MainWindow.mainPane.getWidth()-width) / MainWindow.mainPane.getWidth());
        }
    }

    public static void moveTab(Tab tab){
        if(isIntoLeftBar(tab)){
            MainWindow.leftBar.getSelectionModel().select(tab);
        }else if(isIntoRightBar(tab)){
            MainWindow.rightBar.getSelectionModel().select(tab);
        }
    }

    public static void selectTab(Tab tab){
        if(isIntoLeftBar(tab)){
            MainWindow.leftBar.getSelectionModel().select(tab);
        }else if(isIntoRightBar(tab)){
            MainWindow.rightBar.getSelectionModel().select(tab);
        }
    }

    public static boolean isIntoLeftBar(Tab tab){
        return MainWindow.leftBar.getTabs().contains(tab);
    }
    public static boolean isIntoRightBar(Tab tab){
        return MainWindow.rightBar.getTabs().contains(tab);
    }

    public static void setupDividers(SplitPane mainPane){
    }

    public static void showDragSpaces(){
        MainWindow.leftBar.showDragSpace();
        MainWindow.rightBar.showDragSpace();
    }

    public void showDragSpace(){
        if(getTabs().size() == 0){
            setStyle(STYLE + "-fx-background-color: #0078d7");
            setWidthByEditingDivider(30);
            setMaxWidth(30);
        }

    }

    public static void hideDragSpaces(){
        MainWindow.leftBar.hideDragSpace();
        MainWindow.rightBar.hideDragSpace();
    }

    public void hideDragSpace(){
        setStyle(STYLE);
        if(getTabs().size() == 0){
            setWidthByEditingDivider(0);
            setMaxWidth(0);
        }
    }
}