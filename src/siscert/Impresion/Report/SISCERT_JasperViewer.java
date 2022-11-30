package siscert.Impresion.Report;

import java.lang.reflect.Field;
import javax.swing.JButton;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

/* 
    Creado el : 5/07/2017, 03:34:07 PM
    Autor     : Ing. Maai Nolasco Sánchez
*/


class SISCERT_JasperViewer extends JasperViewer 
{

    protected SISCERT_JasperViewer(JasperPrint jasperPrint) 
    {
        super(jasperPrint);
    }
    
    protected SISCERT_JasperViewer(JasperPrint jasperPrint, boolean close) 
    {
        super(jasperPrint, close);
    }
    
    protected void hideButtonSave ()
    {
        // remove "save" button
        try {
            // find button
            Field f = this.viewer.getClass().getDeclaredField("btnSave");
            f.setAccessible(true);
            JButton saveButton = (JButton)f.get(this.viewer);
            saveButton.setVisible(false); // or setEnabled(false);
        } catch (Exception e) {
            // silent
        }
    }
    
    protected void hideButtonPrint ()
    {
        // remove "print" button
        try {
            // find button
            Field f = this.viewer.getClass().getDeclaredField("btnPrint");
            f.setAccessible(true);
            JButton printButton = (JButton)f.get(this.viewer);
            printButton.setVisible(false); // or setEnabled(false);
        } catch (Exception e) {
            // silent
        }
    }
}
