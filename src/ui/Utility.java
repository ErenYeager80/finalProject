package ui;

import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import ui.sections.Editor;
import ui.sections.EditorTab;
import ui.sections.FileTree;
import ui.sections.FileView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utility {

    public static void saveTab(TabPane tabPane, FileTree ft) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("X files (*.X)", "*.X"));

        var tab=tabPane.getSelectionModel().getSelectedItem();
        if ( ((EditorTab) tab).getFile()==null) {
            File file = fileChooser.showSaveDialog(null);
            if(file != null) {
                try {
                    FileView fv=new FileView(file.getPath());
                    ((EditorTab) tab).setFile(fv);
                    tab.setText(file.getName());
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write(((Editor) (((VirtualizedScrollPane) tab.getContent()).getContent())).getText());
                    writer.close();

                    ft.addItem(fv);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(((EditorTab) tab).getFile()));
                writer.write(((Editor)(((VirtualizedScrollPane) tab.getContent()).getContent())).getText());
                writer.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
