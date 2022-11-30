package siscert.Impresion.Report;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.print.PrinterException;
//import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.swing.JDialog;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import siscert.AccesoBD.SISCERT_QueriesInformix;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 9/01/2018, 09:17:24 AM
 */
public class SISCERT_ReportManager {
    
    private SISCERT_QueriesInformix qryIfx;
    private Frame frameParent;
    private String rutaReporte;
    
    private String nombreReporte, titulo;
    private boolean showPreviewWindow, imprimible, guardable;

    public SISCERT_ReportManager (String rutaReporte, String nombreReporte, String titulo, boolean verPrevisualizacion, boolean imprimible, boolean guardable) throws SQLException, Exception
    {
        initData (rutaReporte, nombreReporte, titulo, verPrevisualizacion, null, imprimible, guardable, null);
    }
    
    public SISCERT_ReportManager (String rutaReporte, String nombreReporte, String titulo, boolean verPrevisualizacion, java.awt.Frame frameParent, boolean imprimible, boolean guardable) throws SQLException, Exception
    {
        initData (rutaReporte, nombreReporte, titulo, verPrevisualizacion, frameParent, imprimible, guardable, null);
    }
    
    public SISCERT_ReportManager (String rutaReporte, String nombreReporte, String titulo, boolean verPrevisualizacion, boolean imprimible, boolean guardable, String tipoConexion) throws SQLException, Exception
    {
        initData (rutaReporte, nombreReporte, titulo, verPrevisualizacion, null, imprimible, guardable, tipoConexion);
    }
    
    public SISCERT_ReportManager (String rutaReporte, String nombreReporte, String titulo, boolean verPrevisualizacion, java.awt.Frame frameParent, boolean imprimible, boolean guardable, String tipoConexion) throws SQLException, Exception
    {
        initData (rutaReporte, nombreReporte, titulo, verPrevisualizacion, frameParent, imprimible, guardable, tipoConexion);
    }
    
    private void initData (String rutaReporte, String nombreReporte, String titulo, boolean mostrarVentanaDePrevisualizacion, java.awt.Frame frameParent, boolean imprimible, boolean guardable, String tipoConexion) throws Exception
    {
        if (tipoConexion!=null && (tipoConexion.equals("INTERNET") || tipoConexion.equals("LOCAL") || tipoConexion.equals("ODBC"))){
            qryIfx = new SISCERT_QueriesInformix(tipoConexion);
            qryIfx.conectar();
        }else
            qryIfx = null;
        
        this.frameParent = frameParent;        
        this.rutaReporte = rutaReporte;                                             //System.getProperty("user.dir") -->  devuelve la ruta de donde se ejecutó el programa
        this.nombreReporte=nombreReporte;
        this.titulo=titulo;
        this.showPreviewWindow=mostrarVentanaDePrevisualizacion;
        this.imprimible=imprimible;
        this.guardable=guardable; 
    }
    
    public int enviarAReporteador (Map parametros) throws JRException, PrinterException, Exception
    {
        return sendToReport (parametros, null);
    }
    
    public int enviarAReporteador (Map parametros, JRBeanCollectionDataSource dataSource) throws JRException, PrinterException, Exception
    {
        return sendToReport (parametros, dataSource);
    }
    
    private int sendToReport (Map parametros, JRBeanCollectionDataSource dataSource) throws JRException, PrinterException, Exception
    {
        //jasperReport = JasperCompileManager.compileReport("CONSTANCIA.jrxml"); // para compilar el jrxml
        //try{masterReport = (JasperReport) JRLoader.loadObject(rutaReporte);}
        //catch (JRException e){}

        //------------ Preparacion del reporte diseñado y compilado con iReport
        JasperPrint jasperPrint;
        if (qryIfx!=null)
            jasperPrint = JasperFillManager.fillReport(this.rutaReporte+this.nombreReporte+".jasper", parametros, this.qryIfx.getConexion());
        else if (dataSource!=null)
            jasperPrint = JasperFillManager.fillReport(this.rutaReporte+this.nombreReporte+".jasper", parametros, dataSource);
        else
            jasperPrint = JasperFillManager.fillReport(this.rutaReporte+this.nombreReporte+".jasper", parametros, new JREmptyDataSource());

        if (showPreviewWindow)
        {
            if (this.frameParent==null) {
                SISCERT_JasperViewer jviewer = new SISCERT_JasperViewer(jasperPrint,false);         //Se lanza el Viewer de Jasper, no termina aplicación al salir con "false", con true sí
                jviewer.setTitle(this.titulo);
                //jviewer.setAlwaysOnTop(Boolean.TRUE);
                jviewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                if (!guardable)
                    jviewer.hideButtonSave();
                if (!imprimible)
                    jviewer.hideButtonPrint();
                
                jviewer.setVisible(true);
                
            }else
            {
                JDialog dlgReporte = new JDialog(frameParent, true);
                SISCERT_ReportViewer jviewer = new SISCERT_ReportViewer(jasperPrint); 
                jviewer.setPrintEnabled(imprimible);
                dlgReporte.add(jviewer);
                //----------------- DIMENSIONAMOS LA VENTANA ------------------\\
                Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();   //Obtenemos las dimensiones del monitor
                dlgReporte.setSize((pantalla.width / 2)+(pantalla.width / 7), (pantalla.height / 2)+(pantalla.height / 4));
                Dimension ventana = dlgReporte.getSize();                           //Se obtienen las dimensiones de la ventana
                //***este es el calculo que hacemos para obtener las coordenadas para centrar la pantalla
                int posx = (pantalla.width - ventana.width) / 2;
                int posy = (pantalla.height - ventana.height) / 2;
                //**** colocamos la ventana en las coordenadas calculadas anteriormente
                dlgReporte.setLocation(posx, posy);
                //dlgReporte.setLocationRelativeTo(null);
                //\\-----------------------------------------------------------//
                dlgReporte.setTitle(titulo);
                //jframe.setIconImage(icono.getImage());
                dlgReporte.setVisible(true);
                return jviewer.getNumImpresiones();
            }
        }else// if (showPreviewWindow.equals("IMPRIMIR")) 
            JasperPrintManager.printReport(jasperPrint,false);                  //Con false no muestra la ventana de díalogo de impresoras y envia a la predeterminada
        return -1;
    }
    
    public void cerrarConexion () throws SQLException
    {
        qryIfx.cerrarConexion ();
    }
}
