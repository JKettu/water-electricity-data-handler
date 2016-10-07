package gui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

/**
 * Created by Anton on 20.07.2016.
 */
public class ExcelFileChooser extends JFileChooser {

    public ExcelFileChooser() {
        FileFilter fileFilterXls = createXlsFileFilter();
        FileFilter fileFilterXlsx = createXlsxFileFilter();
        addChoosableFileFilter(fileFilterXls);
        addChoosableFileFilter(fileFilterXlsx);
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setAlwaysOnTop(true);
        return dialog;
    }

    private FileFilter createXlsFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().contains(".xls");
            }

            @Override
            public String getDescription() {
                return ".xls";
            }
        };
    }

    private FileFilter createXlsxFileFilter(){
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().contains(".xlsx");
            }

            @Override
            public String getDescription() {
                return ".xlsx";
            }
        };
    }
}
