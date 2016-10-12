package gui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;


public class ExcelFileChooser extends JFileChooser {

    private static final String XLS_FILE_MASK = ".xls";
    private static final String XLSX_FILE_MASK = ".xls";

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
                return file.isFile() && file.getName().contains(XLS_FILE_MASK);
            }

            @Override
            public String getDescription() {
                return XLS_FILE_MASK;
            }
        };
    }

    private FileFilter createXlsxFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().contains(XLSX_FILE_MASK);
            }

            @Override
            public String getDescription() {
                return XLSX_FILE_MASK;
            }
        };
    }
}
