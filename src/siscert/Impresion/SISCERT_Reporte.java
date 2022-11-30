/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package siscert.Impresion;

import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.Impresion.Report.SISCERT_ReportManager;


/**
 * 
 * Creado el 30/12/2017, 11:39:11 AM
 * @author Ing. Maai Nolasco Sánchez
 */
public class SISCERT_Reporte {
            //OBJETOS DEL REPORTIADOR
    private JasperReport masterReport = null;
    private JasperReport jasperReport = null;
    private SISCERT_QueriesInformix conexion;
    private SISCERT_ReportManager reportManager;
            
    private String rutaReporte, nivelEstudios,nombreReporte;
    private String nombreReportes[] = {"repPRIMARIA","repSECUNDARIA"};//,"repPREESCOLAR","certificacionSemielectronica"};
    private String nivel [] = {"Primaria", "Secundaria"};//, "Preescolar"};
    private int cveplan;
    private boolean conectar;
    private String tipoConexion;
    private java.awt.Frame frameParent;

    public SISCERT_Reporte (int cveplan, SISCERT_QueriesInformix conexion, String tipoConexion) throws SQLException
    {
        String nivel [] = {"Primaria", "Secundaria"};//, "Preescolar"};

        this.conexion = conexion;        
        this.cveplan = cveplan;
        this.rutaReporte = "reportes/";       //System.getProperty("user.dir") -->  devuelve la ruta de donde se ejecutó el programa
        this.nivelEstudios = nivel [cveplan-1];
        this.nombreReporte = nombreReportes[cveplan-1];
        this.tipoConexion = tipoConexion;
    }
    
    public SISCERT_Reporte (int cveplan, boolean conectar, String tipoConexion, java.awt.Frame frameParent) throws SQLException, Exception
    {
        //reportManager = new SISCERT_ReportManager ("reportes/", conectar, tipoConexion, frameParent);
        this.cveplan = cveplan;
        this.conectar = conectar;
        this.tipoConexion = tipoConexion;
        this.frameParent = frameParent;
        this.nivelEstudios = nivel [cveplan-1];
    }
    
    public void generarReporte (String i, String f, String cveunidad, String cicinilib, String idformato, String formato) throws  JRException, Exception
    {
         
        reportManager = new SISCERT_ReportManager (rutaReporte, nombreReporte, nivelEstudios, true, true, true,tipoConexion);
        //Pasamos parametros al reporte Jasper.
        Map parametro = new HashMap();
        parametro.put("cveunidad",cveunidad);
        parametro.put("inicio",Integer.parseInt(i));
        parametro.put("fin",Integer.parseInt(f));
        parametro.put("cicescinilib",Integer.parseInt(cicinilib));
        
        reportManager.enviarAReporteador (parametro);
        //reportManager.enviarAReporteador (parametro, rutaReporte,"Reporte de alumnos de nivel " + this.nivelEstudios,conexion.getConexion());
        //reportManager.enviarAReporteador (parametro, nombreReportes[cveplan-1],"Reporte de alumnos de nivel " + this.nivelEstudios,null,"VER", true, false);
        
    }

    public void reporteLibro (String delegacion, String perEsc, String nivelEducativo, String nombreTabla) throws  JRException, Exception
    {        
        String nombreLibro = "";
        
        if (Integer.parseInt(perEsc.substring(0,4))>=2015 )
            nombreLibro += "repLIBRO";
        else{
            if (nivelEducativo.equals("PREESCOLAR")||nivelEducativo.equals("PRIMARIA"))
                nombreLibro += "repLIBRO_PREPRI";
            else if (nivelEducativo.equals("SECUNDARIA"))
                nombreLibro += "repLIBRO_SEC";
        }

        //Pasamos parametros al reporte Jasper.
        Map parametro = new HashMap();
        parametro.put("delegacion",delegacion);
        parametro.put("perEsc",perEsc);
        parametro.put("nivelEducativo",nivelEducativo);
        parametro.put("nombreDocumento","CERTIFICACION DE ESTUDIOS (COMPLETOS)");
        parametro.put("nombreTabla",nombreTabla);
        
        //enviarAReporteador (parametro, nombreLibro,"Libro de nivel "+ nivelEducativo,conexion.getConexion());
        //reportManager.enviarAReporteador (parametro, nombreLibro,"Libro de nivel "+ nivelEducativo,null,"VER", true, false);
    }
    
    public void duplicado (String idsfolimpre, boolean usarFuenteWindows) throws  JRException, PrinterException, Exception
    {
        String nombreCertificado=nombreReportes[3]+(usarFuenteWindows?"_winFont":"_libFont");
        Map parametro = new HashMap();
        
        //----------------------- asignación de los parámetros a enviar ----------------------- 
        parametro.put("idsfolimpre",idsfolimpre);//51235,51344,51375
        
        reportManager = new SISCERT_ReportManager ("reportes/", nombreCertificado, "Certificación electrónica", true, this.frameParent, true, false, this.tipoConexion);
        reportManager.enviarAReporteador (parametro);
    }
    
    public void cerrarConexion () throws SQLException
    {
        reportManager.cerrarConexion ();
    }
}
