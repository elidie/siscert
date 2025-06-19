package siscert.Inicio;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.Administrador.SISCERT_AgregarEscuelaHistorica;
import siscert.Administrador.SISCERT_AsociarIdalu;
import siscert.Administrador.SISCERT_Auditoria;
import siscert.Administrador.SISCERT_CrearCertifOrig;
import siscert.Certificacion.SISCERT_FJUL17_Preescolar;
import siscert.Certificacion.SISCERT_FJUL17_Primaria;
import siscert.Certificacion.SISCERT_FJUL17_Secundaria;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;
import siscert.Impresion.SISCERT_EditarFolios;
import siscert.Impresion.SISCERT_Libro;
import siscert.Impresion.SISCERT_PrepararImpre;
import siscert.Impresion.SISCERT_Reporte;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:19:22 PM
 */
public class SISCERT_VentanaPrincipal extends javax.swing.JFrame 
{
    private JDialog dlgAlumnoPreescolar=null, dlgAlumnoPrimaria=null, dlgAlumnoSecundaria=null;
    private JDialog dlgEditarVariables=null,dlgEditarLeyendas1=null;
    private JDialog dlgCalibrarImpresion=null, dlgImprimirLibro=null, dlgAdministrarCoordenadas=null;
    private JDialog dlgAcercaDe = null;
    private JDialog dlgPrepararImpre = null,dlgEditarFolios = null, dlgSeekWizard = null;
    private JDialog dlgNuevaCVEUNIDAD=null, dlgAdministrarUsuarios=null, dlgGenerarAuditoria=null;
    private JDialog dlgCrearCertifOrig=null, dlgAgregarEscuelaHistorica=null, dlgAsociarIdalu=null;
    
    private final SISCERT_GlobalMethods global;
    private final SISCERT_Mensajes mensaje;
    private final SISCERT_QueriesInformix conexion;
    private final SISCERT_ModeloDeTabla modelSISCERT, modelFolImpre, modelSICEEB, modelFoliosSICEEB;
    
    private int SelecIni, SelecFin;
    private boolean lockCbxBuscarPor, lockcveunidad, permisoGuardarCertificaciones, permisoDeNoValidarEdad, verCinco9;
    private final ArrayList<String[]> formatosCertActivos = new ArrayList<String[]>();
    
    
    public SISCERT_VentanaPrincipal(SISCERT_GlobalMethods global, SISCERT_Mensajes mensaje, SISCERT_QueriesInformix conexion) {
        initComponents();
        
        this.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));        
        this.global = global;
        this.mensaje = mensaje;
        this.conexion = conexion;
        
        
        //---------------- CONFIGURAMOS LAS TABLAS DE FOLIOS IMPRESOS  ------------------
        modelFolImpre = new SISCERT_ModeloDeTabla(new String [] {"idfolimpre", "idalu", "Num. Solicitud","cicescinilib","foliolet", "folionum","nombre", "primerape", "segundoape", "curp", "cicescini", "prom_educprim", "promedio", "prom_educbasic", "folio", "cct", "fecha", "fechainsert","usuario","fechatimbradoieepo","foliodigital"});
        //---------------- CONFIGURAMOS LAS TABLAS DE ALUMNOS SISCERT  ------------------
        modelSISCERT = new SISCERT_ModeloDeTabla(new String [] {"N.P.","No. CONTROL", "NUM. SOL.", "foja","FOLIOS PARA EL LIBRO", "idAlu","NOMBRE", "PRIMER APELLIDO", "SEGUNDO APELLIDO", "CURP", "TIPO EDUC","FORMATO IMPRESIÓN","FOLIO"},new String [] {"idcertificacion"});
        modelSISCERT.setAlias(new String [][]{{"np","0"},{"numCtrl","1"},{"numSol","2"},{"foja","3"},{"cicescinilib","4"},{"idalu","5"},{"nombre","6"},{"primerApe","7"},{"segundoApe","8"},{"curp","9"},{"tipoEduc","10"},{"formatoImp","11"},{"folio","12"}}, new String [][]{{"idcertificacion","0"}});
        tblSISCERT.setModel(modelSISCERT);
        //---------------- CONFIGURAMOS LAS TABLAS DE ALUMNOS SICEEB  ------------------
        modelSICEEB = new SISCERT_ModeloDeTabla(new String [] {"idalu", "NOMBRE", "PRIMER APELLIDO", "SEGUNDO APELLIDO", "CURP"});
        tblSICEEB.setModel(modelSICEEB);
        modelSICEEB.setScrollHorizontal(tblSICEEB, scrlpSICEEB);
        modelSICEEB.setAnchoDeColumnas(tblSICEEB, new int[]{50,90,140,140,130});
        //---------------- CONFIGURAMOS LAS TABLAS DE FOLIOS DE ALUMNOS SICEEB  ------------------
        modelFoliosSICEEB = new SISCERT_ModeloDeTabla(new String [] {"NIVEL","FOLIO", "CCT", "ESTUDIÓ", "LIBRO", "PROMEDIO","ESTATUS IMPRE"},new String[]{"grupo"});
        modelFoliosSICEEB.setHiddenAlias(new String[][]{{"grupo","0"}});
        tblFoliosSICEEB.setModel(modelFoliosSICEEB);
        modelFoliosSICEEB.setScrollHorizontal(tblFoliosSICEEB, scrlpFoliosSICEEB);
        //*****************DESHABILITAMOS BOTON IMPRIMIR HASTA QUE EL USUARIO INTRODUSCA EL RANGO DE IMPRESION
        //Quitar--> btnImprimir.setEnabled(false);
        smnuImprimir_Certificado.setEnabled(false);
        lockCbxBuscarPor = true;        cbxBuscarPor.setSelectedItem(null);     lockCbxBuscarPor = false;
        
        //tbtnPreescolar.setVisible(false);
                
        verificarPermisos();
        getCveunidadesYFormatoscert ();
        cbxFormatoCertif.setVisible(false);
        //cbxFormatoCertif_ItemStateChanged(null);
        
        //Este comando estará escrito aquí para versión 1.2.6 y la clase Empleado.java sólo es de prueba
        //para imprimir un reporte usando como dataSource una lista ligada, eso lo estoy probando en SISCERTReporte.repPrueba()
        //mnuVer_Libro.setVisible(false);
    }
    
    //---------- Según sea la opción del usuario (Preescolar, Primaria, Secundaria) se manda a llamar la ventana para insertar un nuevo alumno
    
    public void NuevoAlumno() {
        String[] dato;
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal("CVEPLAN","","");
        else if (cbxFormatoCertif.getSelectedIndex()==-1)
            mensaje.ventanaPrincipal ("FORMATO_CERTIFICADO", "","");
        else {
            global.NoControl = "";
            try
            {
                if (tbtnPreescolar.isSelected())                                //preescolar
                    alumnoPreescolar ("Nuevo");
                else if (tbtnPrimaria.isSelected())                             //primaria
                    alumnoPrimaria ("Nuevo");
                else if (tbtnSecundaria.isSelected())                           //secundaria
                    alumnoSecundaria ("Nuevo");
            } catch (Exception ex) {
                if (ex.getMessage().contains("SIN_CERTIFICADO"))
                    mensaje.General(this,"SIN_CERTIFICADO", "", "");
                else if (ex.getMessage().contains("FORMATO_CICLO_ACRED"))
                    mensaje.Preescolar ("CICLO_ACRED","","");
                else if (ex.getMessage().contains("FORMATO_PARA_CLF")){
                    dato = ex.getMessage().substring(ex.getMessage().indexOf('*')+1).split("~");
                    mensaje.Secundaria ("FORMATO_PARA_CLF",dato[0],dato[1]);
                }else
                    mensaje.General(this,"GENERAL", ""+ex, "");
            } finally {
                this.setCursor(Cursor.getDefaultCursor());                      //Cambiamos la forma del puntero a default        
            }
        }
    }
    //---------- Según sea la opción del usuario (Preescolar, Primaria, Secundaria) se manda a llamar la ventana para editar un alumno
    public void EditarAlumno()
    {
        String[] dato;
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else if (cbxFormatoCertif.getSelectedIndex()==-1)
            mensaje.ventanaPrincipal ("FORMATO_CERTIFICADO", "","");
        else if (tblSISCERT.getSelectedRow()!= -1){
            global.NoControl = ""+modelSISCERT.getValueAt(tblSISCERT.getSelectedRow(), "numCtrl");   //obtenemos el número de control
            global.curp = ""+modelSISCERT.getValueAt(tblSISCERT.getSelectedRow(), "curp");        //obtenemos la curp
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            try {
                if (tbtnPreescolar.isSelected())                                        //preescolar
                    alumnoPreescolar ("Editar");
                else if (tbtnPrimaria.isSelected())                                     //primaria
                    alumnoPrimaria ("Editar");
                else if (tbtnSecundaria.isSelected())                                   //secundaria
                    alumnoSecundaria ("Editar");
            }catch (Exception ex) {
                if (ex.getMessage().contains("SIN_CERTIFICADO"))
                    mensaje.General(this,"SIN_CERTIFICADO", "", "");
                else if (ex.getMessage().contains("FORMATO_PARA_CLF")){
                    dato = ex.getMessage().substring(ex.getMessage().indexOf('*')+1).split("~");
                    mensaje.Secundaria ("FORMATO_PARA_CLF",dato[0],dato[1]);
                }else
                    mensaje.General(this,"GENERAL", ""+ex, "");
            }finally {
                this.setCursor(Cursor.getDefaultCursor());                      //Cambiamos la forma del puntero a default        
            }
        }else
            mensaje.ventanaPrincipal ("NO_SELEC", "SISCERT un","editarlo");
    }

    public void ReimprimirAlumno()
    {
        String[] dato;
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else if (cbxFormatoCertif.getSelectedIndex()==-1)
            mensaje.ventanaPrincipal ("FORMATO_CERTIFICADO", "","");
        else if (tblSISCERT.getSelectedRow()!= -1){
            global.NoControl = ""+modelSISCERT.getValueAt(tblSISCERT.getSelectedRow(), "numCtrl");   //obtenemos el número de control
            global.curp = ""+modelSISCERT.getValueAt(tblSISCERT.getSelectedRow(), "curp");        //obtenemos la curp
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            /*try {
                if (tbtnPreescolar.isSelected())                                        //preescolar
                    alumnoPreescolar ("Editar");
                else if (tbtnPrimaria.isSelected())                                     //primaria
                    alumnoPrimaria ("Editar");
                else if (tbtnSecundaria.isSelected())                                   //secundaria
                    alumnoSecundaria ("Editar");
            }catch (Exception ex) {
                if (ex.getMessage().contains("SIN_CERTIFICADO"))
                    mensaje.General(this,"SIN_CERTIFICADO", "", "");
                else if (ex.getMessage().contains("FORMATO_PARA_CLF")){
                    dato = ex.getMessage().substring(ex.getMessage().indexOf('*')+1).split("~");
                    mensaje.Secundaria ("FORMATO_PARA_CLF",dato[0],dato[1]);
                }else
                    mensaje.General(this,"GENERAL", ""+ex, "");
            }finally {
                this.setCursor(Cursor.getDefaultCursor());                      //Cambiamos la forma del puntero a default        
            }*/
        }else
            mensaje.ventanaPrincipal ("NO_SELEC", "SISCERT un","editarlo");
    }
        
    public void ImportarAlumno() {
        String[] dato;
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected())) //Si hay algún nivel de escolaridad seleccionado
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else if (cbxFormatoCertif.getSelectedIndex()==-1)
            mensaje.ventanaPrincipal ("FORMATO_CERTIFICADO", "","");
        else if (tblSICEEB.getSelectedRow()== -1)
            mensaje.ventanaPrincipal ("NO_SELEC", "SICEEB un","importalo");
        else {                                                                  //Si está seleccionado algún alumno en la tabla
            global.NoControl = tblSICEEB.getValueAt(tblSICEEB.getSelectedRow(), 0).toString();   //obtenemos el número de control
            global.curp = tblSICEEB.getValueAt(tblSICEEB.getSelectedRow(), 4).toString();        //obtenemos la curp            
            try{
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); //Cambiamos la forma del puntero a reloj de arena
                if (tbtnPreescolar.isSelected()){                                //preescolar
                    verifPuedeImportar ("Preescolar");
                    alumnoPreescolar ("Importar");                    
                } else if (tbtnPrimaria.isSelected()){                             //primaria                    
                    verifPuedeImportar ("Primaria");
                    alumnoPrimaria ("Importar");
                } else if (tbtnSecundaria.isSelected()){                           //secundaria                   
                    verifPuedeImportar ("Secundaria");
                    alumnoSecundaria ("Importar");
                }
            } catch (Exception ex) {
                if (ex==null || ex.getMessage()==null)
                    mensaje.General(this,"GENERAL", ""+ex, "");
                else if (ex.getMessage().contains("SIN_CERTIFICADO"))
                    mensaje.General(this,"SIN_CERTIFICADO", "", "");
                else if (ex.getMessage().contains("FORMATO_PARA_CLF")){
                    dato = ex.getMessage().substring(ex.getMessage().indexOf('*')+1).split("~");
                    mensaje.Secundaria ("FORMATO_PARA_CLF",dato[0],dato[1]);
                }else if (ex.getMessage().equals("ESTATUS_GRADO"))
                    mensaje.ventanaPrincipal("ESTATUS_GRADO", "", "");
                else if (ex.getMessage().equals("CICLO_ESTUD_Y_FOLIO"))
                    mensaje.ventanaPrincipal("CICLO_ESTUD_Y_FOLIO", "", "");
                else if (ex.getMessage().equals("IMPORTACION_CINCONUEVE"))
                    mensaje.ventanaPrincipal("IMPORTACION_CINCONUEVE", "", "");
                else if (ex.getMessage().equals("ALUMNO_CAPTURADO"))
                    mensaje.ventanaPrincipal("ALUMNO_CAPTURADO", global.msg, "");
                else 
                    mensaje.General(this,"GENERAL", ""+ex, "");
            }finally { this.setCursor(Cursor.getDefaultCursor()); }
        }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }
    
    public void Salir ()
    {
        System.exit(0);
    }
    
    private void verifPuedeImportar (String nivel) throws Exception
    {
        int posNivel;
                
        if ((posNivel=modelFoliosSICEEB.indexOf(nivel, 0))!=-1) { 
            if (modelFoliosSICEEB.getValueAt(posNivel, 6) == null)
                throw new Exception ("CICLO_ESTUD_Y_FOLIO");
            if (!(""+modelFoliosSICEEB.getValueAt(posNivel, 6)).trim().equals("C"))
                throw new Exception ("ESTATUS_GRADO");
            /*if ((""+modelFoliosSICEEB.getHiddenValueAt(posNivel, "grupo")).contains("_") && permisoCinco9==false)//!global.cveunidad.equals("CINCO9"))
                throw new  Exception ("IMPORTACION_CINCONUEVE");*/
            if((global.msg=verificarAlumno(nivel)).length()>0)
                throw new Exception ("ALUMNO_CAPTURADO");
        }     
    }

    public void borrarAlumno() {
        if (btnEliminar.isVisible())                                            //Por cuestiones de permisos
        {
            if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
                mensaje.ventanaPrincipal ("CVEPLAN", "","");
            else if (tblSISCERT.getSelectedRow()== -1)
                mensaje.ventanaPrincipal ("NO_SELEC", "SISCERT un","borrarlo");
            else
                switch (JOptionPane.showConfirmDialog(null, "¿Confirma que desea eliminar este alumno?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
                {
                    case JOptionPane.YES_OPTION:
                        try {
                            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); //Cambiamos la forma del puntero a reloj de arena
                            conexion.conectar();
                            conexion.borrarAlumno (this.global.cveunidad, this.global.cveplan, ""+modelSISCERT.getValueAt(tblSISCERT.getSelectedRow(), "numCtrl"),global);  //Borramos al alumno de la base de datos
                            modelSISCERT.removeRow(tblSISCERT.getSelectedRow());
                            txtSelIni.setText("");
                            txtSelFin.setText("");
                            //Quitar--> btnImprimir.setEnabled(false);
                            smnuImprimir_Certificado.setEnabled(false);
                        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
                        catch (Exception ex){ 
                            if (ex.getMessage().contains("CANCELAR_PARA_BORRAR"))
                                mensaje.ventanaPrincipal("CANCELAR_PARA_BORRAR", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
                            else
                            mensaje.General(this,"GENERAL", ex.getMessage(), ""); 
                        }finally{ this.setCursor(Cursor.getDefaultCursor()); }
                        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                        break;
                    case JOptionPane.NO_OPTION: break;
                }
        }
    }
    
    public void cancelarFolio() {
        boolean hacercommit = false;
        if (btnCanceFolio.isVisible())                                            //Por cuestiones de permisos
        {
            if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
                mensaje.ventanaPrincipal ("CVEPLAN", "","");
            else if (tblSISCERT.getSelectedRow()== -1)
                mensaje.ventanaPrincipal ("NO_SELEC", "SISCERT un","CANCELAR duplicado");
            else
                switch (JOptionPane.showConfirmDialog(null, "¿Confirma que desea cancelar el duplicado de este alumno?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
                {
                    case JOptionPane.YES_OPTION:
                        global.idfolimpre = tblSISCERT.getValueAt(tblSISCERT.getSelectedRow(), 0).toString();
                        global.idAluSICEEB = tblSISCERT.getValueAt(tblSISCERT.getSelectedRow(),1).toString();
                        global.cicescinilib = tblSISCERT.getValueAt(tblSISCERT.getSelectedRow(), 3).toString();   //obtenemos el número de control                                                            
                        
                        try {
                            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); //Cambiamos la forma del puntero a reloj de arena
                            conexion.conectarConTransaccion();
                            conexion.cancelarFolio (global.idfolimpre, global.idAluSICEEB,global.cicescinilib, global.cveplan, global.capturista);  //Borramos al alumno de la base de datos
                            modelFolImpre.removeRow(tblSISCERT.getSelectedRow());
                            hacercommit = true;
                        } catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
                        catch (Exception ex){ 
                            /*if (ex.getMessage().contains("CANCELAR_PARA_BORRAR"))
                                mensaje.ventanaPrincipal("CANCELAR_PARA_BORRAR", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
                            else*/ 
                            mensaje.General(this,"GENERAL", ex.getMessage(), ""); 
                        }finally{ this.setCursor(Cursor.getDefaultCursor()); }
                        try {
                            conexion.cerrarConexionConTransaccion(hacercommit); } catch (SQLException ex) { }
                        break;
                    case JOptionPane.NO_OPTION: break;
                }
        }
    }

    //---------- Creamos o mostramos la ventana para editar las variables
    public void EditarVariables() {
        String [] nivel = {"PRIMARIA","SECUNDARIA","PREESCOLAR"};
        
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else {
            //dlgEditarVariables = new SISCERT_Variables(this,true,this.mensaje, this.global, this.conexion);           //creamos un formulario
            dlgEditarVariables.setLocationRelativeTo(null);                     //le damos una localización en la pantalla
            dlgEditarVariables.setResizable(false);
            dlgEditarVariables.setTitle("Edición de variables - " + nivel[global.cveplan-1]);  //Mostramos texto en la barra de título de la ventana
            dlgEditarVariables.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
            dlgEditarVariables.setVisible(true);               //mostramos la ventana*/
        }
    }
    
    public void calibrarImpresion() {
        String [] nivel = {"PRIMARIA","SECUNDARIA","PREESCOLAR"};
        //try {
            if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
                mensaje.ventanaPrincipal ("CVEPLAN", "","");
            else if (cbxFormatoCertif.getSelectedIndex()==-1)
                mensaje.ventanaPrincipal ("FORMATO_CERTIFICADO", "","");
            else{
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
                //dlgCalibrarImpresion = new SISCERT_CalibrarImpresion(this,true, ""+formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0], this.global, this.mensaje, this.conexion); //Mostramos texto en la barra de título de la ventana
                dlgCalibrarImpresion.setLocationRelativeTo(null); //le damos una localización en la pantalla
                dlgCalibrarImpresion.setTitle("Calibrar impresión - " + nivel[global.cveplan-1]); //Mostramos texto en la barra de título de la ventana
                dlgCalibrarImpresion.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
                dlgCalibrarImpresion.setVisible(true);                  //mostramos la ventana*/
            }
        /*} catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (ClassNotFoundException ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }*/
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());   
    }

    
    public void prepararImpresion ()
    {
        if (cbxFormatoCertif.getSelectedIndex()==-1)
            mensaje.ventanaPrincipal ("FORMATO_CERTIFICADO", "","");
        else if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else{
            corregirRangoSelCreciente();
            dlgPrepararImpre = new SISCERT_PrepararImpre(this, true, txtSelIni.getText(),txtSelFin.getText(), formatosCertActivos, this.mensaje, this.global, this.conexion, this.modelSISCERT);/**/ //creamos el objeto
            dlgPrepararImpre.setLocationRelativeTo(null);   //le damos una localización en la pantalla            
            dlgPrepararImpre.setResizable(false);
            dlgPrepararImpre.setTitle("Preparar impresión");        
            dlgPrepararImpre.setVisible(true);  //mostramos la ventana*/
        }
    }
    
    public void editarFolios ()
    {
        String niveles[]={"","PRIMARIA","SECUNDARIA","PREESCOLAR"};
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else{
            corregirRangoSelCreciente();
            dlgEditarFolios = new SISCERT_EditarFolios(this, true, this.mensaje, this.global, this.conexion);/**/ //creamos el objeto
            dlgEditarFolios.setLocationRelativeTo(null);   //le damos una localización en la pantalla            
            dlgEditarFolios.setResizable(false);
            dlgEditarFolios.setTitle("Edición de folios impresos de nivel "+niveles[global.cveplan]+".");        
            dlgEditarFolios.setVisible(true);  //mostramos la ventana*/
        }
    }


    public void imprimirLibro() {        
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else if (cbxFormatoCertif.getSelectedIndex() == -1)
            mensaje.ventanaPrincipal("FORMATO_CERTIFICADO", "", "");
        else{
            dlgImprimirLibro=null;
            dlgImprimirLibro = new SISCERT_Libro(this,true, this.formatosCertActivos, cbxFormatoCertif.getSelectedIndex(), this.mensaje, this.global, this.conexion);
            dlgImprimirLibro.setLocationRelativeTo(null);                       //le damos una localización en la pantalla
            dlgImprimirLibro.setTitle("Configurando impresión de libro");       //Mostramos texto en la barra de título de la ventana
            dlgImprimirLibro.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema)); //Establecemos el ícono del sistema
            dlgImprimirLibro.setResizable(false);                               //Indicamos que el usuario no puede cambiar tamaño de la ventana
            dlgImprimirLibro.setVisible(true);                  //mostramos la ventana*/
        }        
    }

    //---------- Creamos o mostramos la ventana para editar o insertar un alumno de preescolar
    private void alumnoPreescolar (String caso) throws Exception
    {
        String idFormatoCert = ""+formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0];
        dlgAlumnoPreescolar = null;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); //Cambiamos la forma del puntero a reloj de arena
        if (idFormatoCert.equals("2")){ //2010-2011
            //dlgAlumnoPreescolar = new SISCERT_Preescolar(this, true, caso, permisoGuardarCertificaciones, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, this.mensaje, this.global, this.conexion);                         //creamos un formulario
            dlgAlumnoPreescolar.setTitle(caso + " alumno - Educación Preescolar");  //Mostramos texto en la barra de título de la ventana
        }else if (idFormatoCert.equals("4")){ //NIVEL EDUCATIVO
            //dlgAlumnoPreescolar = new SISCERT_NivEduc_Preescolar(this, true, caso, permisoGuardarCertificaciones, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, this.mensaje, this.global, this.conexion);                         //creamos un formulario
            dlgAlumnoPreescolar.setTitle(caso + " alumno - Nivel Educativo Preescolar");  //Mostramos texto en la barra de título de la ventana
        }/*else if (tbtnPreescolar.isSelected() && idFormatoCert.equals("6") )
            mensaje.ventanaPrincipal("FORMATO_INEXISTENTE", ""+cbxFormatoCertif.getSelectedItem(), "");*/
        else if (idFormatoCert.equals("9")){ //FORM. GRIS JUL2016
            dlgAlumnoPreescolar = new SISCERT_FJUL17_Preescolar(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(),cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, ""+cbxFormatoCertif.getSelectedItem(), this.mensaje, this.global, this.conexion);//creamos el formulario            
            dlgAlumnoPreescolar.setTitle(caso + " alumno - Preescolar");    //Mostramos texto en la barra de título de la ventana
            dlgAlumnoPreescolar.setResizable(false);
        }        
        else
            mensaje.ventanaPrincipal("FORMATO_NO_IMPLEMENTADO", ""+cbxFormatoCertif.getSelectedItem(), "");
        dlgAlumnoPreescolar.setLocationRelativeTo(null);                        //le damos una localización en la pantalla
        
        dlgAlumnoPreescolar.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgAlumnoPreescolar.setSize(892, 684);
        dlgAlumnoPreescolar.setLocationRelativeTo(null);
        dlgAlumnoPreescolar.setVisible(true);                  //mostramos la ventana*/
        this.setCursor(Cursor.getDefaultCursor());             //Cambiamos la forma del puntero a default        
    }

    //---------- Creamos o mostramos la ventana para editar o insertar un alumno de primaria
    private void alumnoPrimaria (String caso) throws Exception
    {
        String idFormatoCert = ""+formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0];
        dlgAlumnoPrimaria = null;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); //Cambiamos la forma del puntero a reloj de arena
        if (idFormatoCert.equals("2")){ //2010-2011
            //dlgAlumnoPrimaria = new SISCERT_Primaria(this, true, caso, permisoGuardarCertificaciones, modelSISCERT, tblSISCERT.getSelectedRow(),cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, this.mensaje, this.global, this.conexion);//creamos el formulario
            dlgAlumnoPrimaria.setTitle(caso + " alumno - Educación Primaria");    //Mostramos texto en la barra de título de la ventana
        }else if (idFormatoCert.equals("4") || idFormatoCert.equals("6")){//NIVEL EDUCATIVO
            //dlgAlumnoPrimaria = new SISCERT_NivEduc_Primaria(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(),cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, ""+cbxFormatoCertif.getSelectedItem(), this.mensaje, this.global, this.conexion);//creamos el formulario
            dlgAlumnoPrimaria.setTitle(caso + " alumno - Nivel Educativo Primaria");    //Mostramos texto en la barra de título de la ventana
        }else if (idFormatoCert.equals("8")){ //FORM. GRIS JUL2016
            //dlgAlumnoPrimaria = new SISCERT_FJUL16_Primaria(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(),cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, ""+cbxFormatoCertif.getSelectedItem(), this.mensaje, this.global, this.conexion);//creamos el formulario
            dlgAlumnoPrimaria.setTitle(caso + " alumno - Primaria");    //Mostramos texto en la barra de título de la ventana
            dlgAlumnoPrimaria.setResizable(false);
        }else if (idFormatoCert.equals("9")){ //FORM. GRIS JUL2016
            dlgAlumnoPrimaria = new SISCERT_FJUL17_Primaria(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(),cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, ""+cbxFormatoCertif.getSelectedItem(), this.mensaje, this.global, this.conexion);//creamos el formulario
            dlgAlumnoPrimaria.setTitle(caso + " alumno - Primaria");    //Mostramos texto en la barra de título de la ventana
            dlgAlumnoPrimaria.setResizable(false);
        }else
            mensaje.ventanaPrincipal("FORMATO_NO_IMPLEMENTADO", ""+cbxFormatoCertif.getSelectedItem(), "");
        dlgAlumnoPrimaria.setLocationRelativeTo(null);                          //le damos una localización en la pantalla
        
        dlgAlumnoPrimaria.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgAlumnoPrimaria.setVisible(true);                    //mostramos la ventana*/
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default        
    }

    //---------- Creamos o mostramos la ventana para editar o insertar un alumno de secundaria
    private void alumnoSecundaria (String caso) throws Exception
    {
        String idFormatoCert = ""+formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0];
        dlgAlumnoSecundaria = null;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); //Cambiamos la forma del puntero a reloj de arena
        if (idFormatoCert.equals("2")){ //2010-2011
            //dlgAlumnoSecundaria = new SISCERT_Secundaria(this, true, caso, permisoGuardarCertificaciones, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, this.mensaje, this.global, this.conexion);                          //creamos un formulario
            dlgAlumnoSecundaria.setTitle(caso + " alumno - Educación Secundaria");  //Mostramos texto en la barra de título de la ventana
        }else if (idFormatoCert.equals("4")){//NIVEL EDUCATIVO
            //dlgAlumnoSecundaria = new SISCERT_NivEduc_Secundaria(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, this.mensaje, this.global, this.conexion);                          //creamos un formulario
            dlgAlumnoSecundaria.setTitle(caso + " alumno - Nivel Educativo Secundaria");  //Mostramos texto en la barra de título de la ventana
        }else if (idFormatoCert.equals("3")){//EDUCACION BÁSICA
            //dlgAlumnoSecundaria = new SISCERT_Basic_Secundaria(this, true, caso, permisoGuardarCertificaciones, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, this.mensaje, this.global, this.conexion);                          //creamos un formulario
            dlgAlumnoSecundaria.setTitle(caso + " alumno - Educación Básica Secundaria");  //Mostramos texto en la barra de título de la ventana
        }else if (idFormatoCert.equals("5")){//NE_SEC_OCT14
            //dlgAlumnoSecundaria = new SISCERT_EDUCBAS_JUL14(this, true, caso, permisoGuardarCertificaciones, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, this.mensaje, this.global, this.conexion);                          //creamos un formulario
            dlgAlumnoSecundaria.setTitle(caso + " alumno - Nivel Educativo Secundaria");  //Mostramos texto en la barra de título de la ventana
        }else if (idFormatoCert.equals("6") || idFormatoCert.equals("7")) {//FORM. ROJO JUL2015
            //dlgAlumnoSecundaria = new SISCERT_FJUL15_Secundaria(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, ""+cbxFormatoCertif.getSelectedItem(), this.mensaje, this.global, this.conexion);                          //creamos un formulario
            dlgAlumnoSecundaria.setTitle(caso + " alumno - Secundaria");  //Mostramos texto en la barra de título de la ventana
        } else if (idFormatoCert.equals("8")){//FORM. GRIS JUL2016
            //dlgAlumnoSecundaria = new SISCERT_FJUL16_Secundaria(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, ""+cbxFormatoCertif.getSelectedItem(), this.mensaje, this.global, this.conexion);                          //creamos un formulario
            dlgAlumnoSecundaria.setTitle(caso + " alumno - Secundaria");  //Mostramos texto en la barra de título de la ventana
            dlgAlumnoSecundaria.setResizable(false);
        }else if (idFormatoCert.equals("9")){       //FORM. ELEC JUL2017
            dlgAlumnoSecundaria = new SISCERT_FJUL17_Secundaria(this, true, caso, permisoGuardarCertificaciones, permisoDeNoValidarEdad, modelSISCERT, tblSISCERT.getSelectedRow(), cbxBuscarEn.getSelectedItem().equals("SISCERT"), idFormatoCert, ""+cbxFormatoCertif.getSelectedItem(), this.mensaje, this.global, this.conexion);                          //creamos un formulario
            dlgAlumnoSecundaria.setTitle(caso + " alumno - Secundaria");  //Mostramos texto en la barra de título de la ventana
            dlgAlumnoSecundaria.setResizable(false);
        }else
            mensaje.ventanaPrincipal("FORMATO_NO_IMPLEMENTADO", ""+cbxFormatoCertif.getSelectedItem(), "");
        
        dlgAlumnoSecundaria.setLocationRelativeTo(null);                        //le damos una localización en la pantalla
        dlgAlumnoSecundaria.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgAlumnoSecundaria.setVisible(true);                  //mostramos la ventana*/
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default        
    }
    
       
    private void buscar ()
    {
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))  //Si no hay un nivel escolar (cveplan) seleccionado
                mensaje.ventanaPrincipal ("CVEPLAN", "","");
        if (cbxBuscarPor.getSelectedIndex()==-1)            //Si no se ha elegido una forma de búsqueda
            mensaje.ventanaPrincipal ("SIN_BUSCAR_POR", (cbxBuscarEn.getSelectedItem().equals("SISCERT"))?"No. Control":"idAlu","");
        else if (txtBuscar.getText().equals(""))                            //Si no hay texto qué buscar
            mensaje.ventanaPrincipal ("NO_TEXTO_DE_BUSQUEDA", "","");
        else {
            if (cbxBuscarEn.getSelectedItem().equals("SICEEB"))
                buscarEnSICEEB();
            else if (cbxBuscarEn.getSelectedItem().equals("SISCERT"))
                buscarEnSISCERT();
            else if (cbxBuscarEn.getSelectedItem().equals("DUPLICADOS IMPRESOS"))
                buscarEnDuplicadosImpresos();
        }
    }

    private boolean buscarEnSICEEB ()
    {
        String buscarPor;
        boolean ok=false;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        
        try {
            buscarPor=verifFormatosBuscarPor ();                                //Extraemos el tipo de búsqueda
            conexion.conectar();
            modelSICEEB.removeAllItems();
            modelFoliosSICEEB.removeAllItems();
            modelSISCERT.removeAllItems();
            //Quitar--> btnImprimir.setEnabled(false);
            //Quitar--> smnuImprimir_Certificado.setEnabled(false);
            conexion.selecAlumnoSICEEBFiltro(modelSICEEB, txtBuscar.getText(),buscarPor,global.cveunidad,global.cveplan); //Hacemos la consulta
            ok=true;
        } catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ 
            if (ex.getMessage().contains("FORMAT_BUSQNOM") || ex.getMessage().contains("FALTACURP_BUSQNOM") || ex.getMessage().contains("IDALU_ERRONEO")){
                mensaje.ventanaPrincipal(ex.getMessage(), "", "");
                this.setCursor(Cursor.getDefaultCursor());
                seekWizard ();
            }else
                mensaje.General(this,"GENERAL", ex.getMessage(), ""); 
        }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        return ok;
    }
    
    private void getFoliosAlumnosSICEEB (String idalu)
    {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try
        {
            conexion.conectar();
            modelFoliosSICEEB.removeAllItems();
            conexion.getFoliosAlumnoSICCEB (idalu,modelFoliosSICEEB);
        } catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());
        }
    }


    private boolean buscarEnSISCERT ()
    {
        String buscarPor;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            //----- Extraemos el tipo de búsqueda -----\\
        try {
            buscarPor=verifFormatosBuscarPor ();                                //Extraemos el tipo de búsqueda
            conexion.conectar();
            modelSICEEB.removeAllItems();
            modelFoliosSICEEB.removeAllItems();
            modelSISCERT.removeAllItems();
            modelFolImpre.removeAllItems();
            tblSISCERT.setModel(modelSISCERT);
            tblSISCERT.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            //Quitar--> btnImprimir.setEnabled(true);
            //Quitar--> smnuImprimir_Certificado.setEnabled(true);
            conexion.selecAlumnoSISCERTFiltro(modelSISCERT, txtBuscar.getText(),buscarPor,global.cveunidad,global.cveplan,formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0], global.verUnidades); //Hacemos la consulta
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ 
            if (ex==null || ex.getMessage() == null)
                mensaje.General(this,"GENERAL", ""+ex, ""); 
            else if (ex.getMessage().contains("FORMAT_BUSQNOM") || ex.getMessage().contains("FALTACURP_BUSQNOM") ){
                mensaje.ventanaPrincipal(ex.getMessage(), "", "");
                this.setCursor(Cursor.getDefaultCursor());
                seekWizard ();
            }else if (ex.getMessage().contains("NO_CONTROL_ERRONEO") || ex.getMessage().contains("IDALU_ERRONEO") || ex.getMessage().contains("FORMAT_BUSQFOL") || ex.getMessage().contains("FORMAT_BUSQSOLI"))
                mensaje.ventanaPrincipal(ex.getMessage(), "", "");
            else
                mensaje.General(this,"GENERAL", ex.getMessage(), ""); 
        }finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        }
        return true;
    }
    
    private String verificarAlumno(String nivel)
    { 
        String msg = "", cvenivel = "";
        cvenivel = nivel.equals("Primaria") ? "1" : (nivel.equals("Secundaria")? "2": "3");
        
        try{
            conexion.conectar();
            msg = conexion.buscarEnSICEERT(global.curp, cvenivel, global.NoControl, global.verUnidades);            
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ 
            if (ex.getMessage().contains("NO_CONTROL_ERRONEO") || ex.getMessage().contains("FORMAT_BUSQNOM") || ex.getMessage().contains("FALTACURP_BUSQNOM") || ex.getMessage().contains("FORMAT_BUSQFOL") || ex.getMessage().contains("FORMAT_BUSQSOLI")){
                mensaje.ventanaPrincipal(ex.getMessage(), "", "");
                this.setCursor(Cursor.getDefaultCursor());                
            }else
                mensaje.General(this,"GENERAL", ex.getMessage(), ""); }        
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());    
        return msg;
    }
    
    private boolean buscarEnDuplicadosImpresos ()
    {
        String buscarPor;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        int registros = 0;
        try {
            buscarPor=verifFormatosBuscarPor ();                                //Extraemos el tipo de búsqueda
            conexion.conectar();
            modelSICEEB.removeAllItems();
            modelFoliosSICEEB.removeAllItems();
            modelSISCERT.removeAllItems();
            modelFolImpre.removeAllItems();
            tblSISCERT.setModel(modelFolImpre);
            modelFolImpre.setScrollHorizontal(tblSISCERT, scrlpSISCERT);
            //Quitar--> btnImprimir.setEnabled(false);
            //Quitar--> smnuImprimir_Certificado.setEnabled(false);
            registros = conexion.selecDuplicadosImpresosFiltro(modelFolImpre, txtBuscar.getText().trim(),buscarPor,global.cveunidad,global.cveplan, global.capturista, global.verUnidades, 1); //Hacemos la consulta
            if(registros==0)
                conexion.selecDuplicadosImpresosFiltro(modelFolImpre, txtBuscar.getText().trim(),buscarPor,global.cveunidad,global.cveplan, global.capturista, global.verUnidades, 2); //Hacemos la consulta
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ 
            if (ex.getMessage().contains("NO_CONTROL_ERRONEO") || ex.getMessage().contains("FORMAT_BUSQNOM") || ex.getMessage().contains("FALTACURP_BUSQNOM") || ex.getMessage().contains("FORMAT_BUSQFOL") || ex.getMessage().contains("FORMAT_BUSQSOLI")){
                mensaje.ventanaPrincipal(ex.getMessage(), "", "");
                this.setCursor(Cursor.getDefaultCursor());
                //seekWizard ();
            }else
                mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        return true;
    }
    
    private void seekWizard ()
    {
        dlgSeekWizard=null;
        dlgSeekWizard = new SISCERT_SeekWizard(this,true, txtBuscar, mensaje);
        dlgSeekWizard.setLocationRelativeTo(null);                              //le damos una localización en la pantalla
        dlgSeekWizard.setTitle("Formateando texto de búsqueda");                //Mostramos texto en la barra de título de la ventana
        dlgSeekWizard.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema)); //Establecemos el ícono del sistema
        dlgSeekWizard.setResizable(false);                                      //Indicamos que el usuario no puede cambiar tamaño de la ventana
        dlgSeekWizard.setVisible(true);                    //mostramos la ventana*/
    }
    
    //------------------- Verifica si el formato de lo que se quiere buscar es correcto
    private String verifFormatosBuscarPor () throws Exception
    {
        String caso="";
        String buscarPor = ""+cbxBuscarPor.getSelectedItem();
        String texto = txtBuscar.getText().toUpperCase().trim();
        String part[], part2[];
        
        if (buscarPor.equals("No. Ctrl."))  {
            if (texto.matches("[0-9]+") || texto.matches("[0-9]+[-][0-9]+")){
                part = texto.split("-");
                if (part.length==2) {
                    if (Integer.parseInt(part[1])<Integer.parseInt(part[0])) txtBuscar.setText(part[1]+"-"+part[0]);
                }else txtBuscar.setText(part[0]+"-"+part[0]);
                return "CONTROL";
            }else throw new Exception ("NO_CONTROL_ERRONEO");
        }else if (buscarPor.equals("idAlu")){
            if (texto.matches("[0-9]+"))
                return "CONTROL";
            else throw new Exception ("IDALU_ERRONEO");
        }else if (buscarPor.equals("CURP")){
            return "CURP";
        }else if (buscarPor.equals("Nombre")){
            return "NOMBRE";
        }else if (buscarPor.equals("Folio(s)")){
            if (texto.matches("[A-Z|Ñ]{1}[0-9]+") || texto.matches("[A-Z|Ñ]{1}[0-9]+[-][0-9]+")){
                part = texto.split("-");
                if (part.length==2) {
                    if (Integer.parseInt(part[1])<Integer.parseInt(part[0].substring(1))) txtBuscar.setText(part[0].charAt(0)+part[1]+"-"+part[0].substring(1));
                }else txtBuscar.setText(part[0]+"-"+part[0].substring(1));
                return "FOLIOS";
            }else throw new Exception ("FORMAT_BUSQFOL");
        }else if (buscarPor.equals("No. Solicitud")){
            if (texto.matches("[0-9]+[/][0-9]{4}") || texto.matches("[0-9]+[-][0-9]+[/][0-9]{4}")){
                part = texto.split("-");
                if (part.length==2) {
                    part2 = part[1].split("/");
                    if (Integer.parseInt(part2[0])<Integer.parseInt(part[0])) txtBuscar.setText(part2[0]+"-"+part[0]+"/"+part2[1]);
                }else { part = texto.split("/");  txtBuscar.setText(part[0]+"-"+part[0]+"/"+part[1]); }
                return "SOLICITUD";
            }else throw new Exception ("FORMAT_BUSQSOLI");
        }
        return caso;
    }
    
    private void verificarPermisos()
    {
        try {
            String[] objetos = {"menuAdministrador", "nuevoDuplicado","cambiarCveunidad","eliminarCertif","importar",
                                "editarFoliosImp","menuHerramientas","smnuCrearleCertifAAlumno","smnuAgregarEscuelaHistorica",
                                "smnuAsociarIdalu", "verCinco9","smnuGenerarAuditoria",
                                "reimprimirDuplicado","cancelarDuplicado","imprimir"};
            boolean[] objPermisos;
            int i=0;
            
            conexion.conectar();
            objPermisos=conexion.getEstosPermisos("" + global.idcapturista, "moduloCertificacion", new String []{"guardarCertificacion","noValidarEdad","verUnidades"}); 
            permisoGuardarCertificaciones = objPermisos[0];
            permisoDeNoValidarEdad = objPermisos[1];
            global.verUnidades = objPermisos[2];
            if(global.verUnidades)
                cbxBuscarEn.addItem("DUPLICADOS IMPRESOS");
            
            objPermisos = conexion.getEstosPermisos("" + global.idcapturista, "VentanaPrincipal", objetos);
            
            for (String objeto : objetos){
                if (objeto.equals(objetos[0])){
                    mnuAdministrador.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[1])){
                    btnNuevo.setVisible(objPermisos[i]);
                    smnuNuevo.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[2])){                    
                    lblCambiarCveunidad.setVisible(objPermisos[i]);
                    cbxCambiarCveunidad.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[3])){
                    btnEliminar.setVisible(objPermisos[i]);
                    smnuBorrar.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[4])){
                    btnImportar.setVisible(objPermisos[i]);
                    smnuImportar.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[5])){
                    btnEditarFolsImpres.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[6])){
                    mnuHerramientas.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[7])){
                    smnuCrearleCertifAAlumno.setVisible(objPermisos[i]);
                    jSeparator7.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[8])){
                    smnuAgregarEscuelaHistorica.setVisible(objPermisos[i]);
                    if (!jSeparator7.isVisible())
                        jSeparator7.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[9])){
                    smnuAsociarIdalu.setVisible(objPermisos[i]);
                    if (!jSeparator7.isVisible())
                        jSeparator7.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[10])){
                    verCinco9 = objPermisos[i];
                }else if (objeto.equals(objetos[11])){
                    smnuGenerarAuditoria.setVisible(objPermisos[i]);                
                }else if (objeto.equals(objetos[12])){
                    btnReimprimir.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[13])){
                    btnCanceFolio.setVisible(objPermisos[i]);
                }else if (objeto.equals(objetos[14])){
                    btnImprimir.setVisible(objPermisos[i]);
                }                    
                i++;
            }
        } catch(SQLException ex) { mensaje.General(this,"CONEXION", ex.getMessage(), "");  } 
        catch(Exception ex) { mensaje.General(this,"GENERAL", ex.getMessage(), "");  } 
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
    }
    
    private void getCveunidadesYFormatoscert ()
    {
        try {
            conexion.conectar();
            lockcveunidad = true;
            conexion.getCveunidades(cbxCambiarCveunidad, this.verCinco9);
            conexion.getFormatosCertActivos(formatosCertActivos, cbxFormatoCertif, global.cveunidad);
            cbxCambiarCveunidad.setSelectedItem(global.cveunidad);
            lockcveunidad = false;
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        finally { try { conexion.cerrarConexion(); } catch (SQLException ex) { } }
    }
    
             // ------------------- Para cuando el usuario oprime F1 o F4 sobre la tabla para indicar rango de impresión
             // ------------------ Si el usuario ha introducido principio y fin de impresión se habilita el botón imprimir
    private void checarSiHabilitarBtnImprimir ()
    {
        //if (!txtSelIni.getText().equals("") && !txtSelFin.getText().equals(""))
        //{
            //Quitar--> btnImprimir.setEnabled(true);
            //Quitar--> smnuImprimir_Certificado.setEnabled(true);
        //}
    }

             //----------------- Corrige si la selección inicial en tblSISCERT es más grande en índice que la final, lo invertimos
    private void corregirRangoSelCreciente ()
    {
        int temp;
        if (SelecFin<SelecIni)
        {
            temp = SelecFin;
            SelecFin = SelecIni;
            SelecIni = temp;
            txtSelIni.setText(String.valueOf(modelSISCERT.getValueAt(SelecIni,"np")));
            txtSelFin.setText(String.valueOf(modelSISCERT.getValueAt(SelecFin,"np")));
        }
    }
    
    public void verReporte() throws JRException {
        String reportes[] = {"repPRIMARIA","repSECUNDARIA","repPREESCOLAR"};
        String formatoATrabajar;
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else if (txtSelIni.getText().equals("") || txtSelFin.getText().equals(""))
            mensaje.ventanaPrincipal("NO_SELEC", "SISCERT usando F1 y F4 algunos o algún", "ver el reporte");
        else{                             //Si hay seleccionado alguna fila
            try {                
                    SISCERT_Reporte reporte = new SISCERT_Reporte(global.cveplan,this.conexion,global.tipoConexion);                      //Creamos el objeto de tipo reporte
                    formatoATrabajar=""+modelSISCERT.getValueAt(modelSISCERT.indexOf(txtSelIni.getText(), 0), "formatoImp");
                    reporte.generarReporte(""+modelSISCERT.getValueAt(Integer.parseInt(txtSelIni.getText())-1,"numSol") , ""+modelSISCERT.getValueAt(Integer.parseInt(txtSelFin.getText())-1,"numSol"), global.cveunidad, (""+modelSISCERT.getValueAt(Integer.parseInt(txtSelIni.getText())-1,"cicescinilib")).substring(0, 4), formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0], formatoATrabajar);      //mandamos los datos al reporte para visualizar la credencial
                    reporte.cerrarConexion ();
            } catch (JRException ex) { mensaje.ventanaPrincipal ("REPORTE", reportes[global.cveplan-1],"" + ex); }
              catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
            catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        }
    }
    
    public void reimpresion () throws JRException 
    {
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected())) //Si hay algún nivel de escolaridad seleccionado
            mensaje.ventanaPrincipal ("CVEPLAN", "","");        
        else if (tblSISCERT.getSelectedRow()== -1)
            mensaje.ventanaPrincipal ("NO_SELEC", "SISCERT un","reimprimir");
        else {   
            global.idAluSICEEB = tblSISCERT.getValueAt(tblSISCERT.getSelectedRow(),1).toString();
            global.cicescinilib = tblSISCERT.getValueAt(tblSISCERT.getSelectedRow(), 3).toString();   //obtenemos el número de control
            global.curp = tblSISCERT.getValueAt(tblSISCERT.getSelectedRow(), 9).toString();
            global.idfolimpre = tblSISCERT.getValueAt(tblSISCERT.getSelectedRow(), 0).toString();
            
            try { 
                
                if(Integer.parseInt(global.cicescinilib)<2017)
                    throw new Exception ("NO_CICLO");
            
                SISCERT_Reporte reporte = new SISCERT_Reporte(global.cveplan,this.conexion,global.tipoConexion); //Para certificacion
                reporte.generarReporte(global.idAluSICEEB , global.cicescinilib, global.idfolimpre);      //mandamos los datos al reporte para visualizar la credencial
                reporte.cerrarConexion ();                
            } catch (JRException ex) { mensaje.ventanaPrincipal ("REPORTE", "Reportes de Certificación","" + ex); }
              catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
            catch (Exception ex){ 
                if (ex.getMessage().contains("NO_CICLO") )
                    mensaje.ventanaPrincipal(ex.getMessage(), "", "");
                else
                    mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        }
    }

    public void verReporteCertificacion() throws JRException {
        String reportes = "certificacionSemielectronico_2022";
        String formatoATrabajar;
        if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected()))
            mensaje.ventanaPrincipal ("CVEPLAN", "","");
        else if (txtSelIni.getText().equals("") || txtSelFin.getText().equals(""))
            mensaje.ventanaPrincipal("NO_SELEC", "SISCERT usando F1 y F4 algunos o algún", "ver el reporte");
        else{                             //Si hay seleccionado alguna fila
            try {                
                    SISCERT_Reporte reporte = new SISCERT_Reporte(global.cveplan,this.conexion,global.tipoConexion);                      //Creamos el objeto de tipo reporte
                    formatoATrabajar=""+modelSISCERT.getValueAt(modelSISCERT.indexOf(txtSelIni.getText(), 0), "formatoImp");
                    reporte.generarReporte(""+modelSISCERT.getValueAt(Integer.parseInt(txtSelIni.getText())-1,"numSol") , ""+modelSISCERT.getValueAt(Integer.parseInt(txtSelFin.getText())-1,"numSol"), global.cveunidad, (""+modelSISCERT.getValueAt(Integer.parseInt(txtSelIni.getText())-1,"cicescinilib")).substring(0, 4), formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0], formatoATrabajar);      //mandamos los datos al reporte para visualizar la credencial
                    reporte.cerrarConexion ();
            } catch (JRException ex) { mensaje.ventanaPrincipal ("REPORTE",reportes ,"" + ex); }
              catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
            catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        }
    }
    
    public void setNivelPreescolar()
    {
        try {
            global.cveplan = 3;
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            conexion.conectar();
            conexion.selecDatosDeVariables (this.global.cveunidad, this.global.cveplan, this.global);
            conexion.cerrarConexion();
            tbtnPreescolar.setSelected(true);
            tbtnPrimaria.setSelected(false);
            tbtnSecundaria.setSelected(false);
            limpiarVentanaPrincipal();
            
        } catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }
    
    public void setNivelPrimaria()
    {
        try {
            global.cveplan = 1;
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            conexion.conectar();
            conexion.selecDatosDeVariables (this.global.cveunidad, this.global.cveplan, this.global);
            conexion.cerrarConexion();
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
            tbtnPreescolar.setSelected(false);
            tbtnPrimaria.setSelected(true);
            tbtnSecundaria.setSelected(false);
            limpiarVentanaPrincipal();
        } catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }

    public void setNivelSecundaria() 
    {
        try {
            global.cveplan = 2;
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            conexion.conectar();
            conexion.selecDatosDeVariables (this.global.cveunidad, this.global.cveplan, this.global);
            conexion.cerrarConexion();
            tbtnPreescolar.setSelected(false);
            tbtnPrimaria.setSelected(false);
            tbtnSecundaria.setSelected(true);
            limpiarVentanaPrincipal ();        
        } catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }
    
    private void limpiarVentanaPrincipal ()
    {
        txtSelIni.setText("");
        txtSelFin.setText("");
        txtBuscar.setText("");
        lockCbxBuscarPor = true;
        cbxBuscarPor.setSelectedItem(null);
        lockCbxBuscarPor = false;
        //Quitar--> btnImprimir.setEnabled(false);
        smnuImprimir_Certificado.setEnabled(false);
        modelSISCERT.removeAllItems();
    }
    
    private void setBuscarPor ()
    {
        boolean habilitar=true, imprimir=false;
        cbxBuscarPor.removeAllItems();
        lockCbxBuscarPor=true;
        if (cbxBuscarEn.getSelectedItem().equals("SISCERT")){
            cbxBuscarPor.addItem("No. Ctrl.");
            cbxBuscarPor.addItem("No. Solicitud");
            cbxBuscarPor.addItem("CURP");
            cbxBuscarPor.addItem("Nombre");
            cbxBuscarPor.setSelectedItem(null);
            txtBuscar.requestFocus();
            tblSISCERT.setModel(modelSISCERT);
            tblSISCERT.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            habilitar = true;
            imprimir = false;
        }else if (cbxBuscarEn.getSelectedItem().equals("SICEEB")){
            cbxBuscarPor.addItem("idAlu");
            cbxBuscarPor.addItem("CURP");
            cbxBuscarPor.addItem("Nombre");
            cbxBuscarPor.setSelectedItem(null);
            txtBuscar.requestFocus();
            tblSISCERT.setModel(modelSISCERT);
            tblSISCERT.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            habilitar = false;
            imprimir=false;
        }else if (cbxBuscarEn.getSelectedItem().equals("DUPLICADOS IMPRESOS")){
            cbxBuscarPor.addItem("idAlu");
            cbxBuscarPor.addItem("CURP");
            cbxBuscarPor.addItem("Nombre");
            cbxBuscarPor.addItem("Folio(s)");
            cbxBuscarPor.addItem("No. Solicitud");
            cbxBuscarPor.setSelectedItem(null);
            txtBuscar.requestFocus();
            tblSISCERT.setModel(modelFolImpre);
            modelFolImpre.setScrollHorizontal(tblSISCERT, scrlpSISCERT);
            habilitar = false;
            imprimir=true;
        }
        lockCbxBuscarPor=false;
        modelFolImpre.removeAllItems();
        modelSICEEB.removeAllItems();
        modelFoliosSICEEB.removeAllItems();
        modelSISCERT.removeAllItems();
        btnNuevo.setEnabled(habilitar);
        btnEditar.setEnabled(habilitar);
        btnEliminar.setEnabled(habilitar);
        smnuEditar.setEnabled(habilitar);
        smnuNuevo.setEnabled(habilitar);
        smnuBorrar.setEnabled(habilitar);
        smnuReporte.setEnabled(habilitar);        
        btnImprimir.setEnabled(habilitar);
        smnuImprimir_Certificado.setEnabled(habilitar);
        btnCanceFolio.setEnabled(imprimir);
        btnReimprimir.setEnabled(imprimir);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu7 = new javax.swing.JMenu();
        jMenuItem22 = new javax.swing.JMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        tbtnPreescolar = new javax.swing.JToggleButton();
        tbtnPrimaria = new javax.swing.JToggleButton();
        tbtnSecundaria = new javax.swing.JToggleButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        cbxFormatoCertif = new javax.swing.JComboBox<>();
        btnNuevo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        btnEliminar = new javax.swing.JButton();
        btnCanceFolio = new javax.swing.JButton();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        btnImprimir = new javax.swing.JButton();
        btnReimprimir = new javax.swing.JButton();
        btnEditarFolsImpres = new javax.swing.JButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        lblCambiarCveunidad = new javax.swing.JLabel();
        cbxCambiarCveunidad = new javax.swing.JComboBox<>();
        pnlBusqueda = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cbxBuscarEn = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        cbxBuscarPor = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jSeparator12 = new javax.swing.JSeparator();
        btnBuscar = new javax.swing.JButton();
        pnlSINCE = new javax.swing.JPanel();
        scrlpSICEEB = new javax.swing.JScrollPane();
        tblSICEEB = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        scrlpFoliosSICEEB = new javax.swing.JScrollPane();
        tblFoliosSICEEB = new javax.swing.JTable();
        btnImportar = new javax.swing.JButton();
        pnlSISCERT = new javax.swing.JPanel();
        scrlpSISCERT = new javax.swing.JScrollPane();
        tblSISCERT = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        txtSelIni = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtSelFin = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        smnuNuevo = new javax.swing.JMenuItem();
        smnuImportar = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuImprimir = new javax.swing.JMenu();
        smnuImprimir_Certificado = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mnuVer_Libro = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        smnuGenerarAuditoria = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        smnuSalir = new javax.swing.JMenuItem();
        mnuEdicion = new javax.swing.JMenu();
        smnuEditar = new javax.swing.JMenuItem();
        smnuBorrar = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        smnuEditVariables = new javax.swing.JMenuItem();
        mnuEdicion_EditarLeyendaFinal = new javax.swing.JMenuItem();
        mnuVer = new javax.swing.JMenu();
        smnuReporte = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        smnuPreescolar = new javax.swing.JMenuItem();
        smnuPrimaria = new javax.swing.JMenuItem();
        smnuSecundaria = new javax.swing.JMenuItem();
        mnuHerramientas = new javax.swing.JMenu();
        smnuCalibrarImpresion = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        smnuCrearleCertifAAlumno = new javax.swing.JMenuItem();
        smnuAgregarEscuelaHistorica = new javax.swing.JMenuItem();
        smnuAsociarIdalu = new javax.swing.JMenuItem();
        mnuAdministrador = new javax.swing.JMenu();
        smnuAdministrarCoordenadas = new javax.swing.JMenuItem();
        smnuCrearNuevaCVEUNIDAD = new javax.swing.JMenuItem();
        smnuAdministrarUsuarios = new javax.swing.JMenuItem();
        mnuAyuda = new javax.swing.JMenu();
        smnuDescargarDeGoogleDrive = new javax.swing.JMenuItem();
        smnuAcercaDe = new javax.swing.JMenuItem();

        jMenu7.setText("jMenu7");

        jMenuItem22.setText("jMenuItem22");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(821, 33));
        jToolBar1.setPreferredSize(new java.awt.Dimension(100, 33));

        tbtnPreescolar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/preescolar.png"))); // NOI18N
        tbtnPreescolar.setText("Preescolar");
        tbtnPreescolar.setFocusable(false);
        tbtnPreescolar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        tbtnPreescolar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbtnPreescolar_ActionPerformed(evt);
            }
        });
        jToolBar1.add(tbtnPreescolar);

        tbtnPrimaria.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/primaria.png"))); // NOI18N
        tbtnPrimaria.setText("Primaria");
        tbtnPrimaria.setFocusable(false);
        tbtnPrimaria.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        tbtnPrimaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbtnPrimaria_ActionPerformed(evt);
            }
        });
        jToolBar1.add(tbtnPrimaria);

        tbtnSecundaria.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/secundaria.png"))); // NOI18N
        tbtnSecundaria.setText("Secundaria");
        tbtnSecundaria.setFocusable(false);
        tbtnSecundaria.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        tbtnSecundaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbtnSecundaria_ActionPerformed(evt);
            }
        });
        jToolBar1.add(tbtnSecundaria);
        jToolBar1.add(jSeparator8);

        jLabel1.setText("Formato a trabajar: ");
        jToolBar1.add(jLabel1);

        cbxFormatoCertif.setToolTipText("Establece el formato de impresión con el que se desea crear un nuevo duplicado o editar uno ya hecho.");
        cbxFormatoCertif.setMaximumSize(new java.awt.Dimension(150, 20));
        cbxFormatoCertif.setMinimumSize(new java.awt.Dimension(150, 20));
        cbxFormatoCertif.setPreferredSize(new java.awt.Dimension(150, 20));
        cbxFormatoCertif.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxFormatoCertif_ItemStateChanged(evt);
            }
        });
        jToolBar1.add(cbxFormatoCertif);

        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Nuevo.png"))); // NOI18N
        btnNuevo.setText("Nuevo");
        btnNuevo.setFocusable(false);
        btnNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevo_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNuevo);

        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Editar.png"))); // NOI18N
        btnEditar.setText("Editar");
        btnEditar.setFocusable(false);
        btnEditar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditar_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEditar);
        jToolBar1.add(jSeparator9);

        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/borrar.png"))); // NOI18N
        btnEliminar.setText("Eliminar");
        btnEliminar.setFocusable(false);
        btnEliminar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminar_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEliminar);

        btnCanceFolio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cancelar.png"))); // NOI18N
        btnCanceFolio.setText("Cancelar Folio");
        btnCanceFolio.setEnabled(false);
        btnCanceFolio.setFocusable(false);
        btnCanceFolio.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnCanceFolio.setMaximumSize(new java.awt.Dimension(130, 51));
        btnCanceFolio.setMinimumSize(new java.awt.Dimension(130, 51));
        btnCanceFolio.setName(""); // NOI18N
        btnCanceFolio.setPreferredSize(new java.awt.Dimension(115, 51));
        btnCanceFolio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCanceFolioActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCanceFolio);
        jToolBar1.add(jSeparator10);

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("Imprimir");
        btnImprimir.setFocusable(false);
        btnImprimir.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimir_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnImprimir);

        btnReimprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnReimprimir.setText("Reimprimir");
        btnReimprimir.setEnabled(false);
        btnReimprimir.setFocusable(false);
        btnReimprimir.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnReimprimir.setMaximumSize(new java.awt.Dimension(105, 51));
        btnReimprimir.setMinimumSize(new java.awt.Dimension(105, 51));
        btnReimprimir.setPreferredSize(new java.awt.Dimension(98, 51));
        btnReimprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReimprimirActionPerformed(evt);
            }
        });
        jToolBar1.add(btnReimprimir);

        btnEditarFolsImpres.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editFolios.png"))); // NOI18N
        btnEditarFolsImpres.setText("Editar folios impresos");
        btnEditarFolsImpres.setFocusable(false);
        btnEditarFolsImpres.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnEditarFolsImpres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarFolsImpres_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEditarFolsImpres);
        jToolBar1.add(jSeparator11);

        lblCambiarCveunidad.setText("cveunidad:  ");
        jToolBar1.add(lblCambiarCveunidad);

        cbxCambiarCveunidad.setMaximumSize(new java.awt.Dimension(90, 20));
        cbxCambiarCveunidad.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxCambiarCveunidad_ItemStateChanged(evt);
            }
        });
        jToolBar1.add(cbxCambiarCveunidad);

        pnlBusqueda.setBorder(javax.swing.BorderFactory.createTitledBorder("Búsqueda"));

        jLabel2.setText("Buscar en:");

        cbxBuscarEn.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SISCERT", "SICEEB" }));
        cbxBuscarEn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxBuscarEn_ItemStateChanged(evt);
            }
        });

        jLabel3.setText("Buscar por:");

        cbxBuscarPor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No. Ctrl.", "No. Solicitud", "CURP", "Nombre" }));
        cbxBuscarPor.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxBuscarPor_ItemStateChanged(evt);
            }
        });

        jLabel4.setText(":");

        txtBuscar.setMaximumSize(new java.awt.Dimension(90, 2147483647));
        txtBuscar.setMinimumSize(new java.awt.Dimension(90, 20));
        txtBuscar.setPreferredSize(new java.awt.Dimension(90, 20));
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBuscar_KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBuscar_KeyTyped(evt);
            }
        });

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscar_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlBusquedaLayout = new javax.swing.GroupLayout(pnlBusqueda);
        pnlBusqueda.setLayout(pnlBusquedaLayout);
        pnlBusquedaLayout.setHorizontalGroup(
            pnlBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBusquedaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlBusquedaLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxBuscarEn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3)
                    .addGroup(pnlBusquedaLayout.createSequentialGroup()
                        .addComponent(cbxBuscarPor, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)))
                .addGap(100, 100, 100))
            .addGroup(pnlBusquedaLayout.createSequentialGroup()
                .addGroup(pnlBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlBusquedaLayout.createSequentialGroup()
                        .addGap(115, 115, 115)
                        .addComponent(btnBuscar))
                    .addGroup(pnlBusquedaLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlBusquedaLayout.setVerticalGroup(
            pnlBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBusquedaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cbxBuscarEn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxBuscarPor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBuscar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlSINCE.setBorder(javax.swing.BorderFactory.createTitledBorder("Base de datos SICEEB"));

        scrlpSICEEB.setPreferredSize(new java.awt.Dimension(375, 402));

        tblSICEEB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "idalu", "NOMBRE", "PRIMER APELLIDO", "SEGUNDO APELLIDO", "CURP"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSICEEB.getTableHeader().setReorderingAllowed(false);
        tblSICEEB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSICEEB_MouseClicked(evt);
            }
        });
        tblSICEEB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tblSICEEB_KeyReleased(evt);
            }
        });
        scrlpSICEEB.setViewportView(tblSICEEB);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Folios asignados al alumno seleccionado de SICEEB"));
        jPanel1.setMaximumSize(new java.awt.Dimension(363, 137));
        jPanel1.setMinimumSize(new java.awt.Dimension(363, 137));
        jPanel1.setPreferredSize(new java.awt.Dimension(363, 137));

        tblFoliosSICEEB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "idalu", "NOMBRE", "PRIMER APELLIDO", "SEGUNDO APELLIDO", "CURP"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblFoliosSICEEB.getTableHeader().setReorderingAllowed(false);
        scrlpFoliosSICEEB.setViewportView(tblFoliosSICEEB);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrlpFoliosSICEEB, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrlpFoliosSICEEB, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnImportar.setText("Importar a SISCERT");
        btnImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportar_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSINCELayout = new javax.swing.GroupLayout(pnlSINCE);
        pnlSINCE.setLayout(pnlSINCELayout);
        pnlSINCELayout.setHorizontalGroup(
            pnlSINCELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSINCELayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSINCELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSINCELayout.createSequentialGroup()
                        .addComponent(scrlpSICEEB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE))
                    .addGroup(pnlSINCELayout.createSequentialGroup()
                        .addComponent(btnImportar)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlSINCELayout.setVerticalGroup(
            pnlSINCELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSINCELayout.createSequentialGroup()
                .addGroup(pnlSINCELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSINCELayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrlpSICEEB, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnImportar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlSISCERT.setBorder(javax.swing.BorderFactory.createTitledBorder("Base de datos SISCERT"));

        tblSISCERT.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "No. CONTROL", "NOMBRE", "PRIMER APELLIDO", "SEGUNDO APELLIDO", "FOJA", "CURP"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSISCERT.getTableHeader().setReorderingAllowed(false);
        tblSISCERT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tblSISCERT_KeyPressed(evt);
            }
        });
        scrlpSISCERT.setViewportView(tblSISCERT);

        jLabel5.setText("Inicio de selección: ");

        txtSelIni.setEditable(false);
        txtSelIni.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel6.setText("Fin de selección: ");

        txtSelFin.setEditable(false);
        txtSelFin.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel7.setText("Para escoger el rango de impresión o reporte: De clic en la fila deseada de la tabla, oprima F1 o F4 para inicio o fin respectivamente.");

        javax.swing.GroupLayout pnlSISCERTLayout = new javax.swing.GroupLayout(pnlSISCERT);
        pnlSISCERT.setLayout(pnlSISCERTLayout);
        pnlSISCERTLayout.setHorizontalGroup(
            pnlSISCERTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSISCERTLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSISCERTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrlpSISCERT)
                    .addGroup(pnlSISCERTLayout.createSequentialGroup()
                        .addGroup(pnlSISCERTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSISCERTLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSelIni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSelFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel7))
                        .addGap(0, 519, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlSISCERTLayout.setVerticalGroup(
            pnlSISCERTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSISCERTLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrlpSISCERT, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSISCERTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSelIni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(txtSelFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jMenu1.setText("Archivo");
        jMenu1.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N

        smnuNuevo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        smnuNuevo.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuNuevo.setText("Nuevo duplicado");
        smnuNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuNuevo_ActionPerformed(evt);
            }
        });
        jMenu1.add(smnuNuevo);

        smnuImportar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        smnuImportar.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuImportar.setText("Importar certificado");
        smnuImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuImportar_ActionPerformed(evt);
            }
        });
        jMenu1.add(smnuImportar);
        jMenu1.add(jSeparator4);

        mnuImprimir.setText("Imprimir");
        mnuImprimir.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N

        smnuImprimir_Certificado.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuImprimir_Certificado.setText("Duplicado de certificado");
        smnuImprimir_Certificado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuImprimir_Certificado_ActionPerformed(evt);
            }
        });
        mnuImprimir.add(smnuImprimir_Certificado);
        mnuImprimir.add(jSeparator6);

        mnuVer_Libro.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        mnuVer_Libro.setText("Libro...");
        mnuVer_Libro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuVer_Libro_ActionPerformed(evt);
            }
        });
        mnuImprimir.add(mnuVer_Libro);
        mnuImprimir.add(jSeparator7);

        smnuGenerarAuditoria.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuGenerarAuditoria.setText("Generar reporte de auditoría");
        smnuGenerarAuditoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuGenerarAuditoria_ActionPerformed(evt);
            }
        });
        mnuImprimir.add(smnuGenerarAuditoria);

        jMenu1.add(mnuImprimir);
        jMenu1.add(jSeparator5);

        smnuSalir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        smnuSalir.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuSalir.setText("Salir");
        smnuSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuSalir_ActionPerformed(evt);
            }
        });
        jMenu1.add(smnuSalir);

        jMenuBar1.add(jMenu1);

        mnuEdicion.setText("Edición");
        mnuEdicion.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N

        smnuEditar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        smnuEditar.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuEditar.setText("Editar duplicado de certificado");
        smnuEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuEditar_ActionPerformed(evt);
            }
        });
        mnuEdicion.add(smnuEditar);

        smnuBorrar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        smnuBorrar.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuBorrar.setText("Borrar duplicado de certificado");
        smnuBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuBorrar_ActionPerformed(evt);
            }
        });
        mnuEdicion.add(smnuBorrar);
        mnuEdicion.add(jSeparator1);

        smnuEditVariables.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        smnuEditVariables.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuEditVariables.setText("Editar variables");
        smnuEditVariables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuEditVariables_ActionPerformed(evt);
            }
        });
        mnuEdicion.add(smnuEditVariables);

        mnuEdicion_EditarLeyendaFinal.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        mnuEdicion_EditarLeyendaFinal.setText("Editar leyenda 'Lugar de validación'");
        mnuEdicion_EditarLeyendaFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEdicion_EditarLeyendaFinal_ActionPerformed(evt);
            }
        });
        mnuEdicion.add(mnuEdicion_EditarLeyendaFinal);

        jMenuBar1.add(mnuEdicion);

        mnuVer.setText("Ver");
        mnuVer.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N

        smnuReporte.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        smnuReporte.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuReporte.setText("Reporte");
        smnuReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuReporte_ActionPerformed(evt);
            }
        });
        mnuVer.add(smnuReporte);
        mnuVer.add(jSeparator2);

        smnuPreescolar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        smnuPreescolar.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuPreescolar.setText("Nivel Preescolar");
        smnuPreescolar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuPreescolar_ActionPerformed(evt);
            }
        });
        mnuVer.add(smnuPreescolar);

        smnuPrimaria.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        smnuPrimaria.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuPrimaria.setText("Nivel Primaria");
        smnuPrimaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuPrimaria_ActionPerformed(evt);
            }
        });
        mnuVer.add(smnuPrimaria);

        smnuSecundaria.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
        smnuSecundaria.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuSecundaria.setText("Nivel Secundaria");
        smnuSecundaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuSecundaria_ActionPerformed(evt);
            }
        });
        mnuVer.add(smnuSecundaria);

        jMenuBar1.add(mnuVer);

        mnuHerramientas.setText("Herramientas");
        mnuHerramientas.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N

        smnuCalibrarImpresion.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuCalibrarImpresion.setText("Calibrar caída de impresión");
        smnuCalibrarImpresion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuCalibrarImpresion_ActionPerformed(evt);
            }
        });
        mnuHerramientas.add(smnuCalibrarImpresion);
        mnuHerramientas.add(jSeparator3);

        smnuCrearleCertifAAlumno.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuCrearleCertifAAlumno.setText("Crearle certificado a alumno");
        smnuCrearleCertifAAlumno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuCrearleCertifAAlumno_ActionPerformed(evt);
            }
        });
        mnuHerramientas.add(smnuCrearleCertifAAlumno);

        smnuAgregarEscuelaHistorica.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuAgregarEscuelaHistorica.setText("Agregar escuela histórica");
        smnuAgregarEscuelaHistorica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuAgregarEscuelaHistorica_ActionPerformed(evt);
            }
        });
        mnuHerramientas.add(smnuAgregarEscuelaHistorica);

        smnuAsociarIdalu.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuAsociarIdalu.setText("Asociar idalu");
        smnuAsociarIdalu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuAsociarIdalu_ActionPerformed(evt);
            }
        });
        mnuHerramientas.add(smnuAsociarIdalu);

        jMenuBar1.add(mnuHerramientas);

        mnuAdministrador.setText("Administrador");
        mnuAdministrador.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N

        smnuAdministrarCoordenadas.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuAdministrarCoordenadas.setText("Administrar coordenadas");
        smnuAdministrarCoordenadas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuAdministrarCoordenadas_ActionPerformed(evt);
            }
        });
        mnuAdministrador.add(smnuAdministrarCoordenadas);

        smnuCrearNuevaCVEUNIDAD.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuCrearNuevaCVEUNIDAD.setText("Crear nueva CVEUNIDAD");
        smnuCrearNuevaCVEUNIDAD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuCrearNuevaCVEUNIDAD_ActionPerformed(evt);
            }
        });
        mnuAdministrador.add(smnuCrearNuevaCVEUNIDAD);

        smnuAdministrarUsuarios.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuAdministrarUsuarios.setText("Adminisrtar usuarios y permisos");
        smnuAdministrarUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuAdministrarUsuarios_ActionPerformed(evt);
            }
        });
        mnuAdministrador.add(smnuAdministrarUsuarios);

        jMenuBar1.add(mnuAdministrador);

        mnuAyuda.setText("Ayuda");
        mnuAyuda.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N

        smnuDescargarDeGoogleDrive.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuDescargarDeGoogleDrive.setText("Descargar última versión desde GoogleDrive");
        smnuDescargarDeGoogleDrive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuDescargarDeGoogleDrive_ActionPerformed(evt);
            }
        });
        mnuAyuda.add(smnuDescargarDeGoogleDrive);

        smnuAcercaDe.setFont(new java.awt.Font("Microsoft Sans Serif", 0, 11)); // NOI18N
        smnuAcercaDe.setText("Acerca de ...");
        smnuAcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuAcercaDe_ActionPerformed(evt);
            }
        });
        mnuAyuda.add(smnuAcercaDe);

        jMenuBar1.add(mnuAyuda);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSISCERT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSINCE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlSINCE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBusqueda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlSISCERT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void smnuNuevo_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuNuevo_ActionPerformed
        NuevoAlumno();
    }//GEN-LAST:event_smnuNuevo_ActionPerformed

    private void smnuImportar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuImportar_ActionPerformed
        ImportarAlumno ();
    }//GEN-LAST:event_smnuImportar_ActionPerformed

    private void smnuImprimir_Certificado_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuImprimir_Certificado_ActionPerformed
        prepararImpresion ();
    }//GEN-LAST:event_smnuImprimir_Certificado_ActionPerformed

    private void mnuVer_Libro_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuVer_Libro_ActionPerformed
        imprimirLibro();
    }//GEN-LAST:event_mnuVer_Libro_ActionPerformed

    private void smnuGenerarAuditoria_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuGenerarAuditoria_ActionPerformed
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            dlgGenerarAuditoria = null;
            dlgGenerarAuditoria = new SISCERT_Auditoria(this, true, this.mensaje, this.global, this.conexion);
            dlgGenerarAuditoria.setLocationRelativeTo(null);
            dlgGenerarAuditoria.setResizable(false);
            dlgGenerarAuditoria.setTitle ("Reporte de auditoría.");
            dlgGenerarAuditoria.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
            dlgGenerarAuditoria.setVisible(true);
        }finally { this.setCursor(Cursor.getDefaultCursor()); }
    }//GEN-LAST:event_smnuGenerarAuditoria_ActionPerformed

    private void smnuEditar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuEditar_ActionPerformed
        EditarAlumno();
    }//GEN-LAST:event_smnuEditar_ActionPerformed

    private void smnuBorrar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuBorrar_ActionPerformed
        borrarAlumno();
    }//GEN-LAST:event_smnuBorrar_ActionPerformed

    private void smnuEditVariables_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuEditVariables_ActionPerformed
        EditarVariables();
    }//GEN-LAST:event_smnuEditVariables_ActionPerformed

    private void mnuEdicion_EditarLeyendaFinal_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuEdicion_EditarLeyendaFinal_ActionPerformed
        /*dlgEditarLeyendas1 = null;
        dlgEditarLeyendas1 = new SISCERT_LugarDeValidacion(this, true, this.mensaje, this.global, this.conexion);
        dlgEditarLeyendas1.setLocationRelativeTo(null);                         //le damos una localización en la pantalla
        dlgEditarLeyendas1.setResizable(false);
        dlgEditarLeyendas1.setTitle ("Edición de Lugar de validación.");            //Mostramos texto en la barra de título de la ventana
        dlgEditarLeyendas1.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgEditarLeyendas1.setVisible(true);*/
    }//GEN-LAST:event_mnuEdicion_EditarLeyendaFinal_ActionPerformed

    private void smnuReporte_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuReporte_ActionPerformed
        try {
            verReporte();
        } catch (JRException ex) {
            mensaje.General(this, "GENERAL", ""+ex, "");
        }
    }//GEN-LAST:event_smnuReporte_ActionPerformed

    private void smnuPreescolar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuPreescolar_ActionPerformed
        setNivelPreescolar ();
    }//GEN-LAST:event_smnuPreescolar_ActionPerformed

    private void smnuPrimaria_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuPrimaria_ActionPerformed
        setNivelPrimaria();
    }//GEN-LAST:event_smnuPrimaria_ActionPerformed

    private void smnuSecundaria_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuSecundaria_ActionPerformed
        setNivelSecundaria();
    }//GEN-LAST:event_smnuSecundaria_ActionPerformed

    private void smnuSalir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuSalir_ActionPerformed
        Salir ();
    }//GEN-LAST:event_smnuSalir_ActionPerformed

    private void smnuCalibrarImpresion_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuCalibrarImpresion_ActionPerformed
        calibrarImpresion();
    }//GEN-LAST:event_smnuCalibrarImpresion_ActionPerformed

    private void smnuCrearleCertifAAlumno_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuCrearleCertifAAlumno_ActionPerformed
        dlgCrearCertifOrig = null;
        dlgCrearCertifOrig = new SISCERT_CrearCertifOrig(this, true, this.mensaje, this.global, this.conexion);
        dlgCrearCertifOrig.setLocationRelativeTo(null);
        dlgCrearCertifOrig.setResizable(false);
        dlgCrearCertifOrig.setTitle ("Asignación de certificados.");
        dlgCrearCertifOrig.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgCrearCertifOrig.setVisible(true);
    }//GEN-LAST:event_smnuCrearleCertifAAlumno_ActionPerformed

    private void smnuAgregarEscuelaHistorica_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuAgregarEscuelaHistorica_ActionPerformed
        dlgAgregarEscuelaHistorica=null;
        dlgAgregarEscuelaHistorica = new SISCERT_AgregarEscuelaHistorica(this, true, this.mensaje, this.global, this.conexion);
        dlgAgregarEscuelaHistorica.setLocationRelativeTo(null);
        dlgAgregarEscuelaHistorica.setResizable(false);
        dlgAgregarEscuelaHistorica.setTitle ("Agregar escuela histórica.");
        dlgAgregarEscuelaHistorica.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgAgregarEscuelaHistorica.setVisible(true);
    }//GEN-LAST:event_smnuAgregarEscuelaHistorica_ActionPerformed

    private void smnuAsociarIdalu_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuAsociarIdalu_ActionPerformed
        dlgAsociarIdalu = null;
        dlgAsociarIdalu = new SISCERT_AsociarIdalu(this, true, this.mensaje, this.global, this.conexion);
        dlgAsociarIdalu.setLocationRelativeTo(null);
        dlgAsociarIdalu.setResizable(false);
        dlgAsociarIdalu.setTitle ("Asociación de idalu.");
        dlgAsociarIdalu.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgAsociarIdalu.setVisible(true);
    }//GEN-LAST:event_smnuAsociarIdalu_ActionPerformed
	
    private void smnuAdministrarCoordenadas_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuAdministrarCoordenadas_ActionPerformed
        /*dlgAdministrarCoordenadas = null;
        dlgAdministrarCoordenadas = new SISCERT_AdministrarCoordenadas(this, true, this.mensaje, this.global, this.conexion);
        dlgAdministrarCoordenadas.setLocationRelativeTo(null);                         //le damos una localización en la pantalla
        dlgAdministrarCoordenadas.setResizable(false);
        dlgAdministrarCoordenadas.setTitle ("Administración de coordenadas.");            //Mostramos texto en la barra de título de la ventana
        dlgAdministrarCoordenadas.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        //dlgAdministrarCoordenadas.setBounds(new Rectangle(800, 500));
        SISCERT_App.getApplication().show(dlgAdministrarCoordenadas);*/
    }//GEN-LAST:event_smnuAdministrarCoordenadas_ActionPerformed

    private void smnuCrearNuevaCVEUNIDAD_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuCrearNuevaCVEUNIDAD_ActionPerformed
        dlgNuevaCVEUNIDAD = null;
        //dlgNuevaCVEUNIDAD = new SISCERT_AgregarCveunidad(this, true, this.mensaje, this.global, this.conexion);
        dlgNuevaCVEUNIDAD.setLocationRelativeTo(null);
        dlgNuevaCVEUNIDAD.setResizable(false);
        dlgNuevaCVEUNIDAD.setTitle ("Crear nueva CVEUNIDAD.");
        dlgNuevaCVEUNIDAD.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgNuevaCVEUNIDAD.setVisible(true);
    }//GEN-LAST:event_smnuCrearNuevaCVEUNIDAD_ActionPerformed

    private void smnuAdministrarUsuarios_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuAdministrarUsuarios_ActionPerformed
        dlgAdministrarUsuarios = null;
        //dlgAdministrarUsuarios = new SISCERT_UsuariosYPermisos(this, true, this.mensaje, this.global,this.conexion);
        dlgAdministrarUsuarios.setLocationRelativeTo(null);
        dlgAdministrarUsuarios.setResizable(false);
        dlgAdministrarUsuarios.setTitle ("Administrar usuarios.");
        dlgAdministrarUsuarios.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(global.urlIconoSistema));
        dlgAdministrarUsuarios.setVisible(true);
    }//GEN-LAST:event_smnuAdministrarUsuarios_ActionPerformed

    private void smnuDescargarDeGoogleDrive_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuDescargarDeGoogleDrive_ActionPerformed
        global.descargarSistema (this.global.urlGoogleDrive);
    }//GEN-LAST:event_smnuDescargarDeGoogleDrive_ActionPerformed

    private void smnuAcercaDe_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuAcercaDe_ActionPerformed
        if (dlgAcercaDe==null){
            //dlgAcercaDe = new SISCERT_AcercaDe(this,true,global.versionSistema);                       //creamos un formulario
            dlgAcercaDe.setLocationRelativeTo(null);                            //le damos una localización en la pantalla
            dlgAcercaDe.setResizable(false);
            dlgAcercaDe.setTitle("Acerca de");  //Mostramos texto en la barra de título de la ventana
        }
        dlgAcercaDe.setVisible(true);
    }//GEN-LAST:event_smnuAcercaDe_ActionPerformed
	
    private void tbtnPreescolar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbtnPreescolar_ActionPerformed
        setNivelPreescolar ();
    }//GEN-LAST:event_tbtnPreescolar_ActionPerformed

    private void tbtnPrimaria_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbtnPrimaria_ActionPerformed
        setNivelPrimaria ();
    }//GEN-LAST:event_tbtnPrimaria_ActionPerformed

    private void tbtnSecundaria_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbtnSecundaria_ActionPerformed
        setNivelSecundaria ();
    }//GEN-LAST:event_tbtnSecundaria_ActionPerformed

    private void cbxFormatoCertif_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxFormatoCertif_ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxFormatoCertif_ItemStateChanged

    private void btnNuevo_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevo_ActionPerformed
        NuevoAlumno ();
    }//GEN-LAST:event_btnNuevo_ActionPerformed

    private void btnEditar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditar_ActionPerformed
        EditarAlumno ();
    }//GEN-LAST:event_btnEditar_ActionPerformed

    private void btnEliminar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminar_ActionPerformed
        borrarAlumno ();
    }//GEN-LAST:event_btnEliminar_ActionPerformed

    private void btnImprimir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimir_ActionPerformed
        prepararImpresion ();
    }//GEN-LAST:event_btnImprimir_ActionPerformed

    private void btnEditarFolsImpres_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarFolsImpres_ActionPerformed
        editarFolios ();
    }//GEN-LAST:event_btnEditarFolsImpres_ActionPerformed

    private void cbxCambiarCveunidad_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxCambiarCveunidad_ItemStateChanged
        if (!lockcveunidad)
            global.cveunidad = ""+cbxCambiarCveunidad.getSelectedItem();
    }//GEN-LAST:event_cbxCambiarCveunidad_ItemStateChanged

    private void cbxBuscarEn_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxBuscarEn_ItemStateChanged
        setBuscarPor ();
    }//GEN-LAST:event_cbxBuscarEn_ItemStateChanged

    private void cbxBuscarPor_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxBuscarPor_ItemStateChanged
        if (!lockCbxBuscarPor)
        {
            String buscarPor = ""+cbxBuscarPor.getSelectedItem();
            if (!(tbtnPreescolar.isSelected() || tbtnPrimaria.isSelected() || tbtnSecundaria.isSelected())){
                mensaje.ventanaPrincipal ("CVEPLAN", "","");
                lockCbxBuscarPor=true;    cbxBuscarPor.setSelectedItem(null);     lockCbxBuscarPor=false;
            }else 
            {
                txtBuscar.setText("");
                if ((buscarPor.equals("No. Ctrl.") || buscarPor.equals("idAlu")) && !cbxBuscarEn.getSelectedItem().equals("DUPLICADOS IMPRESOS"))
                {
                    if (cbxBuscarEn.getSelectedItem().equals("SISCERT")){
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
                        try {
                            conexion.conectar();
                            txtBuscar.setText(conexion.getMaxMinID ((cbxBuscarEn.getSelectedItem().equals("SISCERT")?"SISCERT":"SICEEB"),global.cveunidad,global.cveplan,formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0]));
                        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
                        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
                        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
                    }
                }else if (buscarPor.equals("No. Solicitud") && cbxBuscarEn.getSelectedItem().equals("SISCERT")){
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
                        try {
                            conexion.conectar();
                            txtBuscar.setText(conexion.getMaxMinNumSolicitud (global.cveunidad,global.cveplan,formatosCertActivos.get(cbxFormatoCertif.getSelectedIndex())[0]));
                        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
                        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
                        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
                }
            }
            txtBuscar.requestFocus();
        }
    }//GEN-LAST:event_cbxBuscarPor_ItemStateChanged

    private void txtBuscar_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscar_KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            buscar();
            this.setCursor(Cursor.getDefaultCursor());         //Cambiamos la forma del puntero a reloj de arena
        }
    }//GEN-LAST:event_txtBuscar_KeyPressed

    //-------------- Para mostrar seleccionado únicamente rbtnNoCtrl y sugerir un rango de visualización mediante NoControl            //-------------- Para mostrar seleccionado únicamente rbtnCurp            //--------------Para controlar la escritura de número de control en la caja de texto de búsqueda
    private void txtBuscar_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscar_KeyTyped
        if (cbxBuscarPor.getSelectedIndex()!=-1){
            if ((cbxBuscarPor.getSelectedItem().equals("No. Ctrl.") || cbxBuscarPor.getSelectedItem().equals("idAlu"))){
                if (cbxBuscarEn.getSelectedItem().equals("SISCERT") || cbxBuscarEn.getSelectedItem().equals("DUPLICADOS IMPRESOS")){
                    if(!global.revisarTextoPermitido (evt.getKeyChar(), "NO_CONTROL"))
                        evt.consume();
                }else if (cbxBuscarEn.getSelectedItem().equals("SICEEB"))
                    if(!global.revisarTextoPermitido (evt.getKeyChar(), "NUMERICO"))
                        evt.consume();
            }else if (cbxBuscarPor.getSelectedItem().equals("Folio(s)")){
                if(!global.revisarTextoPermitido (evt.getKeyChar(), "FOLIO(S)"))
                    evt.consume();
            }else if (cbxBuscarPor.getSelectedItem().equals("No. Solicitud")){
                if(!global.revisarTextoPermitido (evt.getKeyChar(), "NUM_SOLICITUD"))
                    evt.consume();
            }
        }else{
            mensaje.ventanaPrincipal ("SIN_BUSCAR_POR", (cbxBuscarEn.getSelectedItem().equals("SISCERT"))?"No. Control":"idAlu","");
            evt.consume();
        }
    }//GEN-LAST:event_txtBuscar_KeyTyped

    private void btnBuscar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscar_ActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscar_ActionPerformed

    private void tblSICEEB_KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblSICEEB_KeyReleased
        if (evt.getKeyCode()==KeyEvent.VK_DOWN || evt.getKeyCode()==KeyEvent.VK_UP || evt.getKeyCode()==KeyEvent.VK_ENTER){
            if (tblSICEEB.getSelectedRow()!=-1)
                getFoliosAlumnosSICEEB (""+tblSICEEB.getValueAt(tblSICEEB.getSelectedRow(),0));
        }
    }//GEN-LAST:event_tblSICEEB_KeyReleased

    private void tblSICEEB_MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSICEEB_MouseClicked
        if (tblSICEEB.getSelectedRow()!=-1)
            getFoliosAlumnosSICEEB (""+tblSICEEB.getValueAt(tblSICEEB.getSelectedRow(),0));
    }//GEN-LAST:event_tblSICEEB_MouseClicked

    private void btnImportar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportar_ActionPerformed
        ImportarAlumno ();
    }//GEN-LAST:event_btnImportar_ActionPerformed

    private void tblSISCERT_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblSISCERT_KeyPressed
        int fila = tblSISCERT.getSelectedRow();
        if (cbxBuscarEn.getSelectedIndex()!=-1 && !cbxBuscarEn.getSelectedItem().equals("DUPLICADOS IMPRESOS"))
        {
            tblSISCERT.changeSelection(fila, 6, true, true); //Chanchullo: Para que al oprimir F1 o F2 no se vea como que puede editar la celda de la tabla
            switch(evt.getKeyCode()) {
                case KeyEvent.VK_F1:
                    txtSelIni.setText(String.valueOf(modelSISCERT.getValueAt(fila,"np")));
                    SelecIni = fila;       //Tomamos la posicion de fila inicial seleccionada
                    checarSiHabilitarBtnImprimir();
                    break;
                case KeyEvent.VK_F4:
                    SelecFin = fila;        //Tomamos la posicion de fila final seleccionada
                    txtSelFin.setText(String.valueOf(modelSISCERT.getValueAt(fila,"np")));
                    checarSiHabilitarBtnImprimir();
                    break;
            }
        }
    }//GEN-LAST:event_tblSISCERT_KeyPressed

    private void btnReimprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReimprimirActionPerformed
        try {
            reimpresion ();
        } catch (JRException ex) {
            mensaje.General(this, "GENERAL", ""+ex, "");
        }
    }//GEN-LAST:event_btnReimprimirActionPerformed

    private void btnCanceFolioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCanceFolioActionPerformed
        // TODO add your handling code here:
        cancelarFolio ();
    }//GEN-LAST:event_btnCanceFolioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCanceFolio;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEditarFolsImpres;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnImportar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnReimprimir;
    private javax.swing.JComboBox<String> cbxBuscarEn;
    private javax.swing.JComboBox<String> cbxBuscarPor;
    private javax.swing.JComboBox<String> cbxCambiarCveunidad;
    private javax.swing.JComboBox<String> cbxFormatoCertif;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblCambiarCveunidad;
    private javax.swing.JMenu mnuAdministrador;
    private javax.swing.JMenu mnuAyuda;
    private javax.swing.JMenu mnuEdicion;
    private javax.swing.JMenuItem mnuEdicion_EditarLeyendaFinal;
    private javax.swing.JMenu mnuHerramientas;
    private javax.swing.JMenu mnuImprimir;
    private javax.swing.JMenu mnuVer;
    private javax.swing.JMenuItem mnuVer_Libro;
    private javax.swing.JPanel pnlBusqueda;
    private javax.swing.JPanel pnlSINCE;
    private javax.swing.JPanel pnlSISCERT;
    private javax.swing.JScrollPane scrlpFoliosSICEEB;
    private javax.swing.JScrollPane scrlpSICEEB;
    private javax.swing.JScrollPane scrlpSISCERT;
    private javax.swing.JMenuItem smnuAcercaDe;
    private javax.swing.JMenuItem smnuAdministrarCoordenadas;
    private javax.swing.JMenuItem smnuAdministrarUsuarios;
    private javax.swing.JMenuItem smnuAgregarEscuelaHistorica;
    private javax.swing.JMenuItem smnuAsociarIdalu;
    private javax.swing.JMenuItem smnuBorrar;
    private javax.swing.JMenuItem smnuCalibrarImpresion;
    private javax.swing.JMenuItem smnuCrearNuevaCVEUNIDAD;
    private javax.swing.JMenuItem smnuCrearleCertifAAlumno;
    private javax.swing.JMenuItem smnuDescargarDeGoogleDrive;
    private javax.swing.JMenuItem smnuEditVariables;
    private javax.swing.JMenuItem smnuEditar;
    private javax.swing.JMenuItem smnuGenerarAuditoria;
    private javax.swing.JMenuItem smnuImportar;
    private javax.swing.JMenuItem smnuImprimir_Certificado;
    private javax.swing.JMenuItem smnuNuevo;
    private javax.swing.JMenuItem smnuPreescolar;
    private javax.swing.JMenuItem smnuPrimaria;
    private javax.swing.JMenuItem smnuReporte;
    private javax.swing.JMenuItem smnuSalir;
    private javax.swing.JMenuItem smnuSecundaria;
    private javax.swing.JTable tblFoliosSICEEB;
    private javax.swing.JTable tblSICEEB;
    private javax.swing.JTable tblSISCERT;
    private javax.swing.JToggleButton tbtnPreescolar;
    private javax.swing.JToggleButton tbtnPrimaria;
    private javax.swing.JToggleButton tbtnSecundaria;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtSelFin;
    private javax.swing.JTextField txtSelIni;
    // End of variables declaration//GEN-END:variables

}
