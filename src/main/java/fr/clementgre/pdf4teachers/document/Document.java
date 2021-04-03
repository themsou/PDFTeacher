package fr.clementgre.pdf4teachers.document;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.render.display.PDFPagesRender;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.document.render.display.PageStatus;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Document{
    
    private File file;
    public Edition edition;
    public ArrayList<PageRenderer> pages = new ArrayList<>();
    
    private int currentPage = -1;
    public int totalPages;
    
    public PDFPagesRender pdfPagesRender;
    
    public Thread documentSaver = new Thread(new Runnable(){
        @Override
        public void run(){
            
            while(true){
                if(Main.settings.regularSave.getValue() != -1){
                    try{
                        Thread.sleep(Main.settings.regularSave.getValue() * 60000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    
                    if(!Edition.isSave()) Platform.runLater(() -> edition.save());
                    
                }else{
                    try{
                        Thread.sleep(60000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }, "Document AutoSaver");
    
    public Document(File file) throws IOException{
        this.file = file;
        
        pdfPagesRender = new PDFPagesRender(file);
        totalPages = pdfPagesRender.getNumberOfPages();
    }
    
    public void showPages(){
        
        for(int i = 0; i < totalPages; i++){
            PageRenderer page = new PageRenderer(i);
            MainWindow.mainScreen.addPage(page);
            pages.add(page);
        }
        pages.get(0).updatePosition(30);
        updateShowsStatus();
    }
    
    public void updateShowsStatus(){
        for(PageRenderer page : pages){
            page.updateShowStatus();
        }
    }
    
    public void updateZoom(){
        for(PageRenderer page : pages){
            page.updateZoom();
        }
    }
    
    public void updateBackgrounds(){
        for(PageRenderer page : pages){
            page.setStatus(PageStatus.HIDE);
        }
        updateShowsStatus();
    }
    
    public void loadEdition(){
        this.edition = new Edition(file, this);
        if(!documentSaver.isAlive()) documentSaver.start();
    }
    
    public ArrayList<Element> getElements(){
        ArrayList<Element> elements = new ArrayList<>();
        for(PageRenderer page : pages){
            elements.addAll(page.getElements());
        }
        return elements;
    }
    // [1] : Elements
    // [2] : Texts
    // [3] : Grades
    // [4] : Graphics
    public int[] countElements(){
        ArrayList<Element> elements = getElements();
        int texts = (int) elements.stream().filter((e) -> e instanceof TextElement).count();
        int grades = (int) elements.stream().filter((e) -> e instanceof GradeElement).count();
        int graphics = (int) elements.stream().filter((e) -> e instanceof GraphicElement).count();
        return new int[]{elements.size(), texts, grades, graphics};
    }
    
    public void updateEdition(){
        MainWindow.mainScreen.setSelected(null);
        for(PageRenderer page : pages){
            page.clearElements();
        }
        MainWindow.textTab.treeView.onFileSection.updateElementsList();
        MainWindow.gradeTab.treeView.clear();
        this.edition.load();
    }
    
    public void close(){
        pdfPagesRender.close();
        for(int i = 0; i < totalPages; i++){
            if(pages.size() > i) pages.get(i).remove();
        }
        pages.clear();
    }
    
    public PageRenderer getPreciseMouseCurrentPage(){
        for(PageRenderer page : pages){
            double bottomY = page.getBottomY();
            if(MainWindow.mainScreen.mouseY < bottomY){
                return page;
            }
        }
        return null;
    }
    
    public boolean save(){
        
        if(Edition.isSave()){
            return true;
        }
        
        if(Main.settings.autoSave.getValue()){
            edition.save();
        }else{
            Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.unsavedEdit.title"));
            alert.setHeaderText(TR.tr("dialog.unsavedEdit.header"));
            alert.setContentText(TR.tr("dialog.unsavedEdit.details"));
            ButtonType yesButton = new ButtonType(TR.tr("actions.yes"), ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(TR.tr("actions.no"), ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType(TR.tr("actions.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
            
            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == yesButton){
                edition.save();
                return true;
            }else{
                return option.get() == noButton;
            }
        }
        return true;
    }
    
    public String getFileName(){
        return file.getName();
    }
    
    public File getFile(){
        return file;
    }
    
    public int getCurrentPage(){
        return currentPage;
    }
    public PageRenderer getCurrentPageObject(){
        return (getCurrentPage() != -1) ? pages.get(getCurrentPage()) : pages.get(0);
    }
    
    public void setCurrentPage(int currentPage){
        this.currentPage = currentPage;
        MainWindow.footerBar.updateCurrentPage();
    }
}