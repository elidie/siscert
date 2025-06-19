package siscert.Certificacion;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_ComboboxToolTipRenderer;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;
import siscert.ClasesGlobales.SISCERT_ValidarCurp;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:22:18 PM
 */
public class SISCERT_FJUL17_Secundaria extends javax.swing.JDialog {

    private final SISCERT_GlobalMethods global;
    private final SISCERT_QueriesInformix conexion;
    private final SISCERT_Mensajes mensaje;
    
    private String casoLlamada, idAluSICEEB = "";                                                     
    private String bkuDelegacion, bkuCCTDelegacion, bkuLugarExpedicion, bkuDelegado, bkuCargoDelegado, bkuCurp, cveUnidad59;  //bkuCurp se usa en UPDATE, pues en el qery usa curp como clave de búsqueda, y si el usuario la edita..., entonces mejor la respaldamos
    private final String  idFormatoCert, nombreFormatoCert;
    private final boolean permisoDeNoValidarEdad;
    private final Map idscctDefault = new HashMap();
        /*********** PARA ACTUALIZACION DE LA TABLA SISCERT **************/
    private final SISCERT_ModeloDeTabla modelSISCERT;
    private SISCERT_ValidarCurp valcurp;
    private final int posSelTblSISCERT;
    private final boolean buscarEnSISCERTSelect;
    private boolean actualizarTblSISCERT, cambioEnCurpONombre=false;                                               //Para saber si es necesario actualizar la tabla SISCERT debido a alguna edición
    private final ArrayList<Integer> idsFormatosfolio = new ArrayList<Integer>(), idsCasocurp = new ArrayList<Integer>();
    private final ArrayList<String[]> id_cct_esc_cvet_turno = new ArrayList<String[]>(), hid_hcct_hesc_hcvet_hturno = new ArrayList<String[]>();
    private final SISCERT_ComboboxToolTipRenderer toolTipEscuela = new SISCERT_ComboboxToolTipRenderer(), toolTipHEscuela = new SISCERT_ComboboxToolTipRenderer();
    private boolean lockQueryHEscuela, lockInicializacion, entrarACbxEscuelaYCCT;
    private final Frame frameParent;
    
    public SISCERT_FJUL17_Secundaria(java.awt.Frame parent, boolean modal, String casoLlamada, boolean permisoGuardar, boolean permisoDeNoValidarEdad, SISCERT_ModeloDeTabla modelSISCERT, int posSelTblSISCERT, boolean buscarEnSISCERTSelect, String idFormatoCert, String nombreFormatoCert, SISCERT_Mensajes mensaje, SISCERT_GlobalMethods global, SISCERT_QueriesInformix conexion) throws Exception {
        super(parent, modal);
        initComponents();
        
        this.frameParent = parent;
        this.mensaje = mensaje;
        this.global = global;
        this.conexion = conexion;
        this.casoLlamada = casoLlamada;
        btnGuardar.setVisible(permisoGuardar);
        this.permisoDeNoValidarEdad = permisoDeNoValidarEdad;
        //++++Quitar++++-> this.banderaFecha = true;
        entrarACbxEscuelaYCCT = true;
        this.idFormatoCert = idFormatoCert;
        this.nombreFormatoCert = nombreFormatoCert;
                //****************** INCIALIZAMOS LAS VARIABLES PARA ACTUALIZAR TABLA SISCERT ***************/
        this.modelSISCERT = modelSISCERT;
        this.posSelTblSISCERT = posSelTblSISCERT;
        this.buscarEnSISCERTSelect = buscarEnSISCERTSelect;
                //****************** Inicializamos los combos fecha con la fecha actual
        Calendar c = Calendar.getInstance();
        //c.set(c.get(Calendar.YEAR), 11, 31);
        //dtcFecha.setMaxSelectableDate(c.getTime());
        //c.set(c.get(Calendar.YEAR)-1, 01, 01);
        //dtcFecha.setMinSelectableDate(c.getTime());
        //dtcFecha.setDate(c.getTime());
        
        cbxDiaExpedicion.setSelectedIndex(c.get(Calendar.DATE)-1);
        cbxMesExpedicion.setSelectedIndex(c.get(Calendar.MONTH));
        lblAnioExpedicion.setText("del "+global.convertirAñoEnLetra("dd/MM/"+Integer.toString(c.get(Calendar.YEAR))).toLowerCase());
        
        rbngEsRegularizacion.add(rbnNoEsRegularizacion);
        rbngEsRegularizacion.add(rbnSiEsRegularizacion);
        rbngTipoEscuela.add(rbnEscuelaHistorica);
        rbngTipoEscuela.add(rbnEscuelaActual);
        cbxEscuela.setRenderer(toolTipEscuela);
        cbxHEscuela.setRenderer(toolTipHEscuela);
        lblProm_EducPrim.setEnabled(false);
        lblProm_EducSec.setEnabled(false);
        txtProm_EducPrim.setEnabled(false);
        txtProm_EducSec.setEnabled(false);
                //Incializamos el combo de leyendas1
        
        cargarCicloEscolar();
        //Revision--> setLugaresValidacion ();
        getCasosFolio_y_curp ();
                //Incializamos a no CEBAS
        /*Revision--> cbxDiaCEBAS.setVisible(false);
        lblDeCEBAS.setVisible(false);*/
                //Incializamos los idcctDefault y combo escuelaDe
        initIdscctDefault ();
               //****************** INICIALIZAMOS LA BANDERA QUE INDICARÁ SI EL USUARIO HA HECHO CAMBIOS
        cambios(false);
        //lblImgFondoAnv.setIcon(new javax.swing.ImageIcon(vars.urlSkinSecuAnv));
        //lblImgFondoRev.setIcon(new javax.swing.ImageIcon(vars.urlSkinSecuRev));
                //****************** INICIALIZAMOS LA VARIABLE PARA SABER SI LA VENTANA ES PARA NUEVO O EDITAR ALUMNO
        if (casoLlamada.equals("Editar"))
            Editar ();
        else if (casoLlamada.equals("Importar"))
            Importar ();
        else
            Nuevo ();
        
        if (!rbnEscuelaActual.isSelected() && !rbnEscuelaHistorica.isSelected()){
            this.lockInicializacion = true;
            rbnEscuelaActual.setSelected(true);
            this.lockInicializacion = false;
            cambios(false);
        }
        
        if (global.cveunidad.equals("CINCO9")) {
            btnNuevo.setVisible(false);
            //Obsoleto--> smnuNuevo.setVisible(false);
        }
        
        txtNumSolicitud.setEnabled(false);
        cbxTipoEducacion.setEnabled(false);
        //Revision--> chkEsCEBAS.setEnabled(false);
    }
    
    public boolean Guardar()
    {
        boolean guardado = false, hacerCommit=false; 
        String datEscu[], fechaExpedicion,fechaAcreditacion, dictamen, promNum_educprim=""; 
        String promNum_educSec, promLet_educSec="", promNum_educbasic="", promLet_educbasic="",estatus_cance="";
        Map datos = new HashMap();  
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        if (validarEntradas())
        {
            try {       //---------- Guardamos datos del alumno
                conexion.conectarConTransaccion();                              //Creamos una transacción para bloquear las tablas a usar
                if (casoLlamada.equals("Nuevo") || casoLlamada.equals("Importar"))
                    global.NoControl = conexion.obtenerIdcertiregion(global.cveunidad, global.cveplan);
                if (!global.NoControl.equals("")){                                // Si no se pudo asignar una clave, cerramos la ventana
                    global.convertirPromedioALetra(txtPromedioNum.getText());
                    datEscu = getDatosEscuela ();
                    fechaExpedicion = global.convertirTextoAFecha(""+cbxDiaExpedicion.getSelectedItem(),""+cbxMesExpedicion.getSelectedItem(),lblAnioExpedicion.getText().toLowerCase().replace("del ", ""));
                    fechaAcreditacion = global.validarFecha(txtDiaAcreditacion.getText(),""+cbxMesAcreditacion.getSelectedItem(),txtAnioAcreditacion.getText());
                    dictamen = "";//Revision--> txtDictamenNumero.getText().trim().equals("")?"":"DICTAMEN/"+txtDictamenNumero.getText().trim()+"-"+txtDictamenFecha.getText().trim();
                    
                    if (cbxTipoEducacion.getSelectedItem().equals("Secundaria")) {
                        promNum_educSec = getPromedioNum (txtPromedioNum);      promLet_educSec = lblPromedioLetra.getText();
                    } else {
                        promNum_educprim = getPromedioNum (txtProm_EducPrim);   promNum_educSec = getPromedioNum (txtProm_EducSec);
                        promNum_educbasic = getPromedioNum (txtPromedioNum);    promLet_educbasic = lblPromedioLetra.getText();
                    }
                    
                    //Verificar si ya tiene asignado un folio sin ningun cambio en sus datos personales                     
                    datos = conexion.verificarFolioExistente(idAluSICEEB, "2");
                    estatus_cance = conexion.verificarEnCancelados(idAluSICEEB, "2");
                    if(datos.get("strFirma").toString().isEmpty() && datos.get("strFolios").toString().isEmpty() &&
                        (estatus_cance.isEmpty() || 
                            (!estatus_cance.equals("100") && !estatus_cance.equals("99") && !estatus_cance.equals("1"))))
                        conexion.guardarAlumnoSec(idAluSICEEB, global.NoControl, casoLlamada, (""+cbxCicloCLD.getSelectedItem()).substring(0,4), txtCiciniCLF.getText(), 
                                    txtCiciniCEE.getText(), rbnSiEsRegularizacion.isSelected(), global, txtNombre.getText(),txtApePaterno.getText(),txtApeMaterno.getText(), 
                                    idsCasocurp.get(cbxCasocurp.getSelectedIndex()),txtCurp.getText(), bkuCurp, false/*Revision--> chkEsCEBAS.isSelected()*/, 
                                    ""/*Revision--> (chkEsCEBAS.isSelected()?""+cbxDiaCEBAS.getSelectedItem():"")*/,txtDiaAcreditacion.getText(),""+cbxMesAcreditacion.getSelectedItem(),txtAnioAcreditacion.getText(),getFechaLetCEBAS (), 
                                    promNum_educprim, promNum_educSec, promLet_educSec, promNum_educbasic, promLet_educbasic,fechaExpedicion,txtLibro.getText(),txtFoja.getText(),txtFolio.getText(),
                                    getFolio(),datEscu[0],datEscu[1],datEscu[2],datEscu[3], datEscu[4],datEscu[5],"",dictamen, global.lugaresValidacion.get(0/*Revision--> cbxLugarValidacion.getSelectedIndex()*/)[0], 
                                    false/*Revision--> chkActualizarVars.isSelected()*/,this.idFormatoCert, ""+cbxTipoNumsolicitud.getSelectedItem(), txtNumSolicitud.getText().trim(),cambioEnCurpONombre,cveUnidad59);
                    else if(!datos.get("strFirma").toString().isEmpty()) 
                        throw new Exception ("TIENE_FIRMA"); 
                    else if(!datos.get("strFolios").toString().isEmpty())
                        throw new Exception ("TIENE_FOLIOS"); 
                    else if(estatus_cance.equals("100") || estatus_cance.equals("99"))
                        throw new Exception ("PENDIENTE_CANCE");  
                    
                    bkuCurp = txtCurp.getText().trim();                           //Respaldamos el texto del campo curp, por si el usuario cambia la curp en la consulta que hagamos ya no nos afecte
                    cbxTipoNumsolicitud.setSelectedItem("Actual");
                    cambios (false);
                    casoLlamada = "Editar";                                     //Una vez guardado, se pone en estado de actualizar nadamás y no como nuevo
                    //Revision--> chkActualizarVars.setEnabled(true);
                    if (actualizarTblSISCERT && buscarEnSISCERTSelect && this.posSelTblSISCERT!=-1)
                        actualizartblSISCERT ();                                    //Para actualizar la tabla de búsqueda SISCERT
                    guardado = true;
                    hacerCommit = true; //comentado 14-05-2025
                } else
                    mensaje.General(this,"CONEXION", "","");
            }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
            catch (Exception ex){ 
                if (ex.getMessage().contains("ALUMAT_CVEPROGRAMA"))
                    mensaje.Secundaria("ALUMAT_CVEPROGRAMA", "", "");
                else if (ex.getMessage().contains("TIENE_FIRMA"))
                    mensaje.Secundaria("TIENE_FIRMA",datos.get("strFirma").toString(), "");
                else if (ex.getMessage().contains("TIENE_FOLIOS"))
                    mensaje.Secundaria("TIENE_FOLIOS",datos.get("strFolios").toString(), "");
                else if (ex.getMessage().contains("PENDIENTE_CANCE"))
                    mensaje.Secundaria("PENDIENTE_CANCE","", "");
                else if (ex.getMessage().contains("ALU_EN_SICCEB")) {
                    if (casoLlamada.equals("Editar") && idAluSICEEB.equals(""))
                        mensaje.ModuloCertificacion(this,"LIGAR_CURP_CON_IDALU", txtCurp.getText(), "");
                    else
                        mensaje.Secundaria("ALU_EN_SICCEB", "", "");
                }else if (ex.getMessage().contains("CERTIDUP_EXISTENTE"))
                    mensaje.Secundaria("CERTIDUP_EXISTENTE", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
                else if (ex.getMessage().contains("NUMSOLICITUD_EXISTENTE"))
                    mensaje.ModuloCertificacion(this,"NUMSOLICITUD_EXISTENTE", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
                else if (ex.getMessage().contains("FOLIO_EN_FOLIOSIMPRE"))
                    mensaje.Secundaria("FOLIO_EN_FOLIOSIMPRE", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
                else if (ex.getMessage().contains("ALUREP_CRIPFECHANOM"))
                    mensaje.ModuloCertificacion(this,"ALUREP_CRIPFECHANOM", bkuCurp+(!idAluSICEEB.equals("")?" e idalu: "+idAluSICEEB:"")+","+txtCurp.getText(), ex.getMessage().substring(ex.getMessage().indexOf("*")+1));
                else if (ex.getMessage().contains("ALUREP_CURPNOM") && (casoLlamada.equals("Editar") || casoLlamada.equals("Importar")))
                    mensaje.ModuloCertificacion(this,"ALUREP_CURPNOM", bkuCurp+(!idAluSICEEB.equals("")?" e idalu: "+idAluSICEEB:"")+","+txtCurp.getText(), ex.getMessage().substring(ex.getMessage().indexOf("*")+1));
                else if (ex.getMessage().contains("GRADOREP_ESTATUS"))
                    mensaje.ModuloCertificacion(this,"GRADOREP_ESTATUS", ex.getMessage().split("~")[1], ex.getMessage().split("~")[2]);
                else if (ex.getMessage().contains("SINIDALU_CRIPFECHANOM"))
                    mensaje.ModuloCertificacion(this,"SINIDALU_CRIPFECHANOM", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
                else if (ex.getMessage().contains("CURSO_EN_OTRO_CICLO"))
                    mensaje.ModuloCertificacion(this,"CURSO_EN_OTRO_CICLO", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");                                
                else
                    mensaje.General(this,"GENERAL", ex.getMessage(), ""); 
            }
            finally { try { conexion.cerrarConexionConTransaccion(hacerCommit);} catch (SQLException ex) { } }
        }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default        
        return guardado;  //La operación se completó con éxito
    }
        
    //-------- Carga datos de la base de datos al formulario para editarlos
    public final void Editar () throws Exception
    {
        ResultSet rs;
        String fecha, idleyenda_lugarValidacion, strTemp;
        String filaEscu[]={"","","","",""}, cveYturnoEscuela[]={"",""}, idhescuela="", hescuela="";
        int dia, mes, anio, cicinilib, intTemp, idFormato;

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try {
            conexion.conectar();
            rs = conexion.selecAnvRevParaEditar (global.NoControl, global.curp, global.cveplan, global.cveunidad, global.verUnidades);
            rs.next();
                    //************** Cargamos los datos del alumno **************
            bkuDelegacion = rs.getString("delegacion").trim();
            bkuCCTDelegacion = rs.getString("cctdeleg").trim();
            txtNombre.setText(rs.getString("nombre").trim());
            txtApePaterno.setText(rs.getString("apepat").trim());
            txtApeMaterno.setText(rs.getString("apemat").trim());
            cbxCasocurp.setSelectedIndex(idsCasocurp.indexOf(rs.getInt("idcasocurp")));
            txtCurp.setText(rs.getString("curp").trim());
            bkuCurp = txtCurp.getText();                                   //Respaldamos la curp para usarla en el UPDATE
            cveUnidad59 = rs.getString("cveunidad").trim();
            /*Revision--> if (rs.getBoolean("cebas")){
                chkEsCEBAS.setSelected(true);
                cbxDiaCEBAS.setSelectedItem(rs.getString("dia_acredi"));
            }*/
            try { txtDiaAcreditacion.setText(rs.getString("dia_acredi").trim()); } catch (Exception ex){ txtDiaAcreditacion.setText(""); }
            try { cbxMesAcreditacion.setSelectedItem(rs.getString("mes_acredi").trim()); } catch (Exception ex){ cbxMesAcreditacion.setSelectedItem(null); }
            try { txtAnioAcreditacion.setText(rs.getString("af").trim()); } catch (Exception ex){ txtAnioAcreditacion.setText(""); }
            
            
            //Educación básica
            if ( (idFormato=rs.getInt("idformato"))==3 || idFormato==5 || idFormato==7 || (idFormato>=8 && rs.getInt("cicinilib_cert")==2012) )
            {
                cbxTipoEducacion.setSelectedItem("Básica");
                try { txtProm_EducPrim.setText(rs.getString("prom_educprim").replace(".", "").trim());  } catch (NullPointerException ex){}
                try { txtProm_EducSec.setText(rs.getString("promedio").replace(".", "").trim());        } catch (NullPointerException ex){}
                try { txtPromedioNum.setText(rs.getString("prom_educbasic").replace(".", "").trim());   } catch (NullPointerException ex){}
                try { lblPromedioLetra.setText(rs.getString("promlet_educbasic").trim());               } catch (NullPointerException ex){}
            } else { //Nivel educativo
                cbxTipoEducacion.setSelectedItem("Secundaria");
                txtPromedioNum.setText(rs.getString("promedio").replace(".", "").trim());
                lblPromedioLetra.setText(rs.getString("prom_letra").trim());
            }
            
            bkuLugarExpedicion = rs.getString("lugar_expedicion").trim();
            lblIdAlu.setText(idAluSICEEB = rs.getString("idalu").trim());
            lblNoControl.setText(rs.getString("idcertiregion").trim());
            bkuDelegado = rs.getString("delegado").trim();
            bkuCargoDelegado = rs.getString("cargodelegado").trim();
            global.numSolicitud=rs.getString("numsolicitud").trim();
            if (global.numSolicitud.equals(""))
                cbxTipoNumsolicitud.setSelectedItem("Nuevo");
            else
                cbxTipoNumsolicitud.setSelectedItem("Actual");
            cbxCicloCLD.setSelectedItem(null);            
            cbxCicloCLD.setSelectedItem((cicinilib=rs.getInt("cicescinilib"))+"-"+(cicinilib+1));
            if ((strTemp=rs.getString("regularizado"))!=null){
                if (strTemp.equals("S"))
                    rbnSiEsRegularizacion.setSelected(true);
                else
                    rbnNoEsRegularizacion.setSelected(true);
                txtCiciniCLF.setText((intTemp=rs.getInt("cicinilib_cert"))==0?"":""+intTemp);
                lblCicfinCLF.setText(" - "+(intTemp+1));
                txtCiciniCEE.setText((intTemp=rs.getInt("ciciniestud"))==0?"":""+intTemp);
                lblCicfinCEE.setText(" - "+(intTemp+1));
            }else{
                rbnSiEsRegularizacion.setSelected(false);
                rbnNoEsRegularizacion.setSelected(false);
                txtCiciniCLF.setText("");                txtCiciniCEE.setText("");
                lblCicfinCLF.setText("");                lblCicfinCEE.setText("");
            }
            //--------- Verificamos que el ciclo del folio sea el adecuado para el formato a usar
            //if (!txtCiciniCLF.getText().equals("") && (Integer.parseInt(txtCiciniCLF.getText())+1)>=2013)
            //    throw new SQLException("FORMATO_PARA_CLF*"+txtCiciniCLF.getText()+lblCicfinCLF.getText()+"~"+"NIVEL EDUCATIVO");
            txtLibro.setText(rs.getString("libro").trim());
            txtFoja.setText(rs.getString("foja").trim());
            cbxFormatosFolio.setSelectedIndex(idsFormatosfolio.indexOf(rs.getInt("idformatosfolio")));
            txtFolio.setText(rs.getString("folio").trim());
            
            entrarACbxEscuelaYCCT = false;
            cbxEscuela.removeAllItems();        cbxCCTEscuela.removeAllItems();
            id_cct_esc_cvet_turno.clear();      hid_hcct_hesc_hcvet_hturno.clear();
            filaEscu[0]=rs.getString("idcct");
            if (idscctDefault.containsKey(filaEscu[0])){
                chkEditManEscu.setSelected(true);
                cbxEscuelaDe.setSelectedItem(this.idscctDefault.get(filaEscu[0]));
            }else
                cbxEscuelaDe.setEnabled(false);
            if (rs.getString("tablaescuela").trim().equals("H")){
                hescuela = rs.getString("hescuela").trim();
                idhescuela = rs.getString("idhescuela").trim();
            }else
                cbxEscuela.addItem(filaEscu[2]=rs.getString("escuela").trim());
            cbxCCTEscuela.addItem(filaEscu[1]=rs.getString("cct").trim());
            id_cct_esc_cvet_turno.add(filaEscu);
            if (!chkEditManEscu.isSelected())
                setEscuelaDe();
            entrarACbxEscuelaYCCT = true;
            
            //++++Quitar++++-> lblCotejo.setText(rs.getString("cotejo").trim());
            //++++Quitar++++-> cbxPlan.setSelectedItem(rs.getString("plan_estud").trim());
            /*Revision--> if ( (strTemp = rs.getString("juridico").trim()).contains("DICTAMEN") ){
                txtDictamenNumero.setText(strTemp.split("-")[0].replace("DICTAMEN/", ""));
                txtDictamenFecha.setText(strTemp.split("-")[1]);
            }else
                txtDictamenNumero.setText(strTemp);*/
            
                //----------- Cargamos los combos de fecha
            /*fecha = rs.getString("fecha").trim();
            dia = Integer.parseInt(fecha.substring(0, 2))-1;
            mes = Integer.parseInt(fecha.substring(3, 5))-1;
            anio = Integer.parseInt(fecha.substring(6, 10));
            cbxDiaExpedicion.setSelectedIndex(dia);
            cbxMesExpedicion.setSelectedIndex(mes);*/
            
            //++++Quitar++++-> banderaFecha = false;
            //++++Quitar++++-> txtFecha.setText(fecha);
            //++++Quitar++++-> banderaFecha = true;
            
            /*Revision--> idleyenda_lugarValidacion = rs.getString("idleyenda_lugvalid");
            for (int i=0; i<global.lugaresValidacion.size(); i++)
                if (idleyenda_lugarValidacion.equals(global.lugaresValidacion.get(i)[0]))
                    cbxLugarValidacion.setSelectedIndex(i);*/
            
            if (!hescuela.equals("")){
                lockQueryHEscuela=true;
                cbxHEscuela.removeAllItems();
                conexion.getEscuelaHistorica("idhescuela",idhescuela,hid_hcct_hesc_hcvet_hturno, cbxHEscuela,"2");
                cbxEscuela.addItem(conexion.getNombreEscuela (filaEscu[0],filaEscu,2));
                rbnEscuelaHistorica.setSelected(true);
                cbxHEscuela.setSelectedItem(hescuela);
                lockQueryHEscuela=false;
            }
            conexion.get_cveturnoYturno_FromIdcct(cveYturnoEscuela, filaEscu[0]);
            filaEscu[3]=cveYturnoEscuela[0];    filaEscu[4]=cveYturnoEscuela[1];
            
            actualizarTblSISCERT = true;
            //Revision--> chkActualizarVars.setEnabled(true);
            reestablecer_lblVar ();
            cambios (false);
        }catch (SQLException ex){ 
            if (ex.getMessage().contains("FORMATO_PARA_CLF"))
                throw new Exception(ex.getMessage());
            else
                mensaje.General(this,"CONEXION",ex.getMessage(),""); 
        }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default        
    }
    
    public final void Nuevo()
    {
        boolean inicializar = true;
        if (btnGuardar.isEnabled())
            switch (JOptionPane.showConfirmDialog(this, "Ha efectuado cambios en los datos ¿Desea guardarlos?", "Pregunta emergente", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE))
            {
                case JOptionPane.YES_OPTION:  inicializar = Guardar (); break;
                case JOptionPane.NO_OPTION: inicializar = true;  break;
                default: inicializar = false;  break;
            }
        if (inicializar)
        {
                // Limpiamos los objetos del anverso
            bkuDelegacion = global.globalDelegacion;
            bkuCCTDelegacion = global.globalCCTDelegacion;
            txtNombre.setText("");
            txtApePaterno.setText("");
            txtApeMaterno.setText("");
            cbxCasocurp.setSelectedIndex(0);
            txtCurp.setText("");
            bkuCurp = "";
            //Revision--> chkEsCEBAS.setSelected(false);
            cbxTipoEducacion.setSelectedItem("Secundaria");
            cbxMesAcreditacion.setSelectedItem(null);
            txtAnioAcreditacion.setText("");
            txtPromedioNum.setText("");
            lblPromedioLetra.setText("");
            bkuLugarExpedicion=global.globalLugarExpedicion;
            bkuDelegado = global.globalDelegado;
            bkuCargoDelegado = global.globalCargoDelegado;
                // Limpiamos los objetos del reverso
            if (cbxCicloCLD.getItemCount()>1)
                cbxCicloCLD.setSelectedItem(null);
            else
                cbxCicloCLD.setSelectedIndex(0);            
            
            cbxTipoNumsolicitud.setSelectedItem("Nuevo");
            rbnNoEsRegularizacion.setSelected(false);
            rbnSiEsRegularizacion.setSelected(false);
            txtCiciniCLF.setText("");                txtCiciniCEE.setText("");
            lblCicfinCLF.setText("");                lblCicfinCEE.setText("");
            //++++Quitar++++-> txtLibro.setText("");
            //++++Quitar++++-> txtFoja.setText("");
            cbxFormatosFolio.setSelectedIndex(0);
            txtFolio.setText("");
            entrarACbxEscuelaYCCT = false;
            id_cct_esc_cvet_turno.clear();  hid_hcct_hesc_hcvet_hturno.clear();
            cbxEscuela.removeAllItems();    cbxCCTEscuela.removeAllItems();     cbxHEscuela.removeAllItems();
            cbxEscuelaDe.setSelectedItem(null);     cbxEscuelaDe.setEnabled(false);
            chkEditManEscu.setSelected(false);
            entrarACbxEscuelaYCCT = true;
            //++++Quitar++++-> lblCotejo.setText(global.globalCotejo);
            //++++Quitar++++-> cbxPlan.setSelectedItem(null);
            //Revision--> txtDictamenNumero.setText("");  txtDictamenFecha.setText("");
            
            Calendar c = Calendar.getInstance();
            cbxDiaExpedicion.setSelectedIndex(c.get(Calendar.DATE)-1);
            cbxMesExpedicion.setSelectedIndex(c.get(Calendar.MONTH));
            lblAnioExpedicion.setText("del "+global.convertirAñoEnLetra("dd/MM/"+Integer.toString(c.get(Calendar.YEAR))).toLowerCase());
        
            actualizarTblSISCERT = false;                                       //Para indicar que ya no vamos a modificar la tabla SISCERT
            reestablecer_lblVar ();
            global.numSolicitud = "";
            lblNoControl.setText("");
            lblIdAlu.setText("");
            txtLibro.setText("");
            txtFoja.setText("");
            cambios (false);
            idAluSICEEB = "";
            casoLlamada = "Nuevo";                                              //Para indicar que el usuario ha solicitado insertar nuevo alumno            
        }
    }
    
    //-------- Carga datos de la base de datos del repSINCE al formulario para editarlos y guardarlos en la BD SISCERT
    public final void Importar () throws Exception
    {
        ResultSet rs;
        String strTemp;
        String []filaEscu=new String[]{"","","","",""};
        int intTemp;

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try {
            conexion.conectar();
            rs = conexion.selecAnvRevParaImportar (global.NoControl, global.curp, global.cveplan, global.cveunidad);
            if (rs.next())
            {
                        //************** Cargamos los datos del alumno **************
                bkuDelegacion = global.globalDelegacion;
                bkuCCTDelegacion = global.globalCCTDelegacion;
                txtNombre.setText(rs.getString("nombre").trim());
                txtApePaterno.setText(rs.getString("apepat").trim());
                txtApeMaterno.setText(rs.getString("apemat").trim());
                cbxCasocurp.setSelectedIndex(0);
                txtCurp.setText(rs.getString("curp").trim());
                bkuCurp = txtCurp.getText();                                   //Respaldamos la curp para usarla en el UPDATE
                cbxMesAcreditacion.setSelectedItem(null);
                
                rbnNoEsRegularizacion.setSelected(false);
                rbnSiEsRegularizacion.setSelected(false);
                txtCiciniCEE.setText((intTemp=rs.getInt("cicescini"))==0?"":""+intTemp);
                lblCicfinCEE.setText(" - "+(intTemp+1));
                txtCiciniCLF.setText((intTemp=rs.getInt("cicescinilib"))==0?"":""+intTemp);
                lblCicfinCLF.setText(" - "+(intTemp+1));
                txtAnioAcreditacion.setText(""+(intTemp+1));
                //--------- Verificamos que el ciclo del folio sea el adecuado para el formato a usar
                if (txtAnioAcreditacion.getText().equals("2013"))
                    cbxTipoEducacion.setSelectedItem("Básica");
                else
                    cbxTipoEducacion.setSelectedItem("Secundaria");
                
                idAluSICEEB = rs.getString("idalu").trim();
                lblNoControl.setText("");
                lblIdAlu.setText(idAluSICEEB);
                txtLibro.setText("");
                txtFoja.setText("");
                
                if (cbxTipoEducacion.getSelectedItem().equals("Secundaria"))
                {
                    try { 
                        if ((strTemp = rs.getString("promediogral").trim()).equals("10.0"))
                            strTemp = "10";
                        txtPromedioNum.setText(strTemp.replace(".", "").trim());
                    }catch (Exception ex ) {txtPromedioNum.setText(""); }
                }
                else 
                {
                    try { if ((strTemp = rs.getString("promediogral").trim()).equals("10.0"))
                                strTemp = "10";
                            txtProm_EducSec.setText(strTemp.replace(".", "").trim());
                    }catch (Exception ex ) {txtProm_EducSec.setText(""); }
                    try { if ((strTemp = rs.getString("promnum_educprim").trim()).equals("10.0"))
                                strTemp = "10";
                            txtProm_EducPrim.setText(strTemp.replace(".", "").trim());
                    }catch (Exception ex ) {txtProm_EducPrim.setText(""); }
                    try { if ((strTemp = rs.getString("promedioeb").trim()).equals("10.0"))
                                strTemp = "10";
                            txtPromedioNum.setText(strTemp.replace(".", "").trim());
                    }catch (Exception ex ) {txtPromedioNum.setText(""); }
                }
                lblPromedioLetra.setText(global.convertirPromedioALetra(txtPromedioNum.getText()));
                
                bkuLugarExpedicion = global.globalLugarExpedicion;
                bkuDelegado = global.globalDelegado;
                bkuCargoDelegado = global.globalCargoDelegado;
                
                if (cbxCicloCLD.getItemCount()>1)
                    cbxCicloCLD.setSelectedItem(null);
                else
                    cbxCicloCLD.setSelectedIndex(0);                
                
                cbxTipoNumsolicitud.setSelectedItem("Nuevo");
                //++++Quitar++++-> txtLibro.setText("");
                //++++Quitar++++-> txtFoja.setText("");
                cbxFormatosFolio.setSelectedIndex(2);
                txtFolio.setText(rs.getString("folio").trim());
                
                entrarACbxEscuelaYCCT = false;
                id_cct_esc_cvet_turno.clear();  hid_hcct_hesc_hcvet_hturno.clear();
                cbxEscuela.removeAllItems();    cbxCCTEscuela.removeAllItems();     cbxHEscuela.removeAllItems();
                filaEscu[0]=rs.getString("idcct");
                filaEscu[3]=rs.getString("cveturno");
                filaEscu[4]=rs.getString("desturno");
                id_cct_esc_cvet_turno.add(filaEscu);
                cbxEscuela.addItem(filaEscu[2]=rs.getString("escuela").trim());    cbxCCTEscuela.addItem(filaEscu[1]=rs.getString("cct").trim());
                setEscuelaDe();
                cbxEscuelaDe.setEnabled(false);
                entrarACbxEscuelaYCCT = true;
                
                //++++Quitar++++-> lblCotejo.setText(global.globalCotejo);
                //++++Quitar++++-> cbxPlan.setSelectedItem(null);
                //++++Quitar++++-> txtJuridico.setText("");
                reestablecer_lblVar ();
                global.numSolicitud = "";
                cambios (false);
            }else{
                throw new SQLException("SIN_CERTIFICADO");
            }
        }catch (SQLException ex){ 
            if (ex.getMessage().contains("SIN_CERTIFICADO"))
                throw new Exception("SIN_CERTIFICADO");
            else if (ex.getMessage().contains("FORMATO_PARA_CLF"))
                throw new Exception(ex.getMessage());
            else
                mensaje.General(this,"CONEXION",ex.getMessage(),""); 
        }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default        
    }
    
    public void Salir()
    {
        if (btnGuardar.isEnabled() && btnGuardar.isVisible())
            switch (JOptionPane.showConfirmDialog(this, "Ha efectuado cambios en los datos ¿Desea guardarlos antes de que se cierre la ventana?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
            {
                case JOptionPane.YES_OPTION:
                    if (!Guardar ())
                        mensaje.General(this,"ERROR_GUARDAR","","");
                    break;
                case JOptionPane.NO_OPTION: break;
            }            
        this.dispose();

    }
    
            // ------------------- Valida que los cuadros de texto no estén vacios o con datos inválidos
    private boolean validarEntradas()
    {
        String msgFol, fecha;
        int mesAcred;
        try  // Mensaje si los campos están vacíos
        {
                    //-----------  Condición si los campos están vacíos excepto apemat y observaciones
            if( txtNombre.getText().trim().equals("") )
                return mensaje.General(this,"CAMPO_VACIO","'NOMBRE'","");
            if (txtApePaterno.getText().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'APELLIDO PATERNO'","");
            if (txtCurp.getText().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'CURP'","");
            if (cbxTipoEducacion.getSelectedIndex()==-1)
                return mensaje.General (this,"CAMPO_VACIO","'TIPO DE EDUCACIÓN'","");
            if (txtDiaAcreditacion.getText().trim().equals(""))
               return mensaje.General (this,"CAMPO_VACIO","'DÍA DE ACREDITACIÓN'","");
            if (cbxMesAcreditacion.getSelectedIndex()==-1)
               return mensaje.General(this,"CAMPO_VACIO","'MES DE ACREDITACIÓN'","");
            /*Revision--> if (chkEsCEBAS.isSelected()){
                if (cbxDiaCEBAS.getSelectedIndex()==-1)
                    return mensaje.General (this,"CAMPO_VACIO","'DÍA DE ACREDITACIÓN'","");
                mesAcred = cbxMesAcreditacion.getSelectedIndex()+1;
                if (!global.isFecha (""+cbxDiaCEBAS.getSelectedItem()+"/"+((mesAcred<10)?"0"+mesAcred:mesAcred)+"/"+txtAnioAcreditacion.getText(), "dd/MM/yyyy"))
                    return mensaje.ModuloCertificacion(this,"FECHA_ACRED_ERRONEA","","");
            }*/
            if (txtAnioAcreditacion.getText().trim().equals(""))
               return mensaje.General (this,"CAMPO_VACIO","'AÑO DE ACREDITACIÓN'","");
            if (txtPromedioNum.getText().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'PROMEDIO'","");
            if (cbxCicloCLD.getSelectedIndex()==-1)
                return mensaje.General (this,"CAMPO_VACIO","'Ciclo CLD'","");
            if (!rbnNoEsRegularizacion.isSelected() && !rbnSiEsRegularizacion.isSelected())
                return mensaje.ModuloCertificacion (this,"ES_REGULARIZADO","","");
            if (txtCiciniCLF.getText().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'Ciclo CLF'","");
            if (txtCiciniCEE.getText().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'Ciclo CEE'","");
            if (txtNumSolicitud.getText().trim().equals("")){
                if ( cbxTipoNumsolicitud.getSelectedItem().equals("Manual"))
                    return mensaje.ModuloCertificacion(this,"NUMFOL_MANUAL_VACIO","'LIBRO'","");
                if ( cbxTipoNumsolicitud.getSelectedItem().equals("Actual"))
                    return mensaje.ModuloCertificacion(this,"NUMFOL_ACTUAL_VACIO","'LIBRO'","");
            }
            //++++Quitar++++-> if (txtLibro.getText().trim().equals(""))
            //++++Quitar++++->     return mensaje.General (this,"CAMPO_VACIO","'LIBRO'","");
            //++++Quitar++++-> if (txtFoja.getText().trim().equals(""))
            //++++Quitar++++->     return mensaje.General (this,"CAMPO_VACIO","'FOJA'","");
            if (txtFolio.getText().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'FOLIO'","");
            try {if (cbxEscuela.getSelectedItem().toString().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'ESCUELA'","");
            }catch (Exception e) {  return mensaje.General (this,"CAMPO_VACIO_O_ENTER","'ESCUELA'","");  }
            if (rbnEscuelaHistorica.isSelected()){
                try {if (cbxHEscuela.getSelectedItem().toString().trim().equals(""))
                    return mensaje.General (this,"CAMPO_VACIO","'ESCUELA HISTÓRICA'","");
                }catch (Exception e) { return mensaje.General (this,"CAMPO_VACIO","'ESCUELA HISTÓRICA'","");  }
            }
            try {if (cbxCCTEscuela.getSelectedItem().toString().trim().equals(""))
                return mensaje.General (this,"CAMPO_VACIO","'CCT'","");
            }catch (Exception e) {  return mensaje.General (this,"CAMPO_VACIO_O_ENTER","'CCT'","");  }
            if (cbxEscuelaDe.getSelectedIndex()==-1)
                return mensaje.General (this,"CAMPO_VACIO","'ESCUELA DE'","");
            
            if (cbxTipoEducacion.getSelectedItem().equals("Básica")){
                if (txtProm_EducPrim.getText().equals(""))
                    return mensaje.General (this,"CAMPO_VACIO","'PROMEDIO PRIMARIA'","");
                if (txtProm_EducSec.getText().equals(""))
                    return mensaje.General (this,"CAMPO_VACIO","'PROMEDIO SECUNDARIA'","");
            }
            //++++Quitar++++-> if (cbxPlan.getSelectedIndex()==-1 && !chkEsCEBAS.isSelected() && cbxTipoEducacion.getSelectedItem().equals("SECUNDARIA"))
            //++++Quitar++++->     return mensaje.General (this,"CAMPO_VACIO","'PLAN'","");
                        //--------- Verificamos que los campos tengan la cantidad exacta de caracteres
            /*Revision--> if ( (txtDictamenNumero.getText().equals("") && !txtDictamenFecha.getText().equals("")) || (!txtDictamenNumero.getText().equals("") && txtDictamenFecha.getText().equals("")) )
                return mensaje.CrearCertifOrig (this,"DICTAMEN_INCOMPLETO","","");*/
            //************* VERIFICAMOS QUE LA CURP SEA CORRECTA ******************
            try{
               global.validarCasocurp(txtCurp.getText().trim().toUpperCase(),txtNombre.getText().toUpperCase().trim(), txtApePaterno.getText().toUpperCase().trim(), txtApeMaterno.getText().toUpperCase().trim(),idsCasocurp.get(cbxCasocurp.getSelectedIndex()), casoLlamada);
            }catch (Exception ex) {   return mensaje.Secundaria(ex.getMessage(),"",""); }
            //*********************************************************************
            if (txtAnioAcreditacion.getText().trim().length() != 4 )
                return mensaje.Secundaria("DATO_INVÁLIDO","El 'AÑO DE ACREDITACIÓN'","");
                //--------- Verificamos que el promedio sea aprobatorio
            if (cbxTipoEducacion.getSelectedItem().equals("Secundaria")){
                if (lblPromedioLetra.getText().trim().equals("") )
                    return mensaje.Secundaria ("DATO_INVÁLIDO","El 'PROMEDIO'","");
                if (txtPromedioNum.getText().trim().length() != 2 )
                    return mensaje.Secundaria ("RANGO","El 'PROMEDIO'","2");
                if (Integer.parseInt(txtPromedioNum.getText().trim()) < 60 && !lblPromedioLetra.getText().equals("DIEZ"))
                    return mensaje.Secundaria ("PROMEDIO","El 'PROMEDIO'","");
            }else{
                //*********************************************************************
                if (!txtProm_EducPrim.getText().trim().equals("") && (!(txtProm_EducPrim.getText().matches("[0-9]{2}") || txtProm_EducPrim.getText().matches("[A,a]"))))
                    return mensaje.Secundaria ("DATO_INVÁLIDO","El 'PROMEDIO EDUC. PRIM.'","");
                if (!(txtProm_EducSec.getText().matches("[0-9]{2}") || txtProm_EducSec.getText().matches("[A,a]")))
                    return mensaje.Secundaria ("DATO_INVÁLIDO","El 'PROMEDIO EDUC. SEC.'","");
                if (!txtProm_EducPrim.getText().trim().equals("") && (txtProm_EducPrim.getText().matches("[A,a]") && txtProm_EducSec.getText().matches("[A,a]")))
                    return mensaje.Secundaria ("2_PROM_LET","","");
                if (!txtPromedioNum.getText().matches("[0-9]{2}") )
                    return mensaje.Secundaria ("RANGO","El 'PROMEDIO'","2");
                if (lblPromedioLetra.getText().trim().equals("") )
                    return mensaje.Secundaria ("DATO_INVÁLIDO","El 'PROMEDIO'","");
                //*********************************************************************
                if (!txtProm_EducPrim.getText().trim().equals("") &&  (!txtProm_EducPrim.getText().matches("[A,a]") && Integer.parseInt(""+txtProm_EducPrim.getText().charAt(0)) < 6 && !txtProm_EducPrim.getText().equals("10")))
                    return mensaje.Secundaria ("PROMEDIO","'PROMEDIO EDUC. PRIM.'","");
                if (txtProm_EducSec.getText().matches("[A,a]") && Integer.parseInt(""+txtProm_EducSec.getText().charAt(0)) < 6 && !txtProm_EducSec.getText().equals("10"))
                    return mensaje.Secundaria ("PROMEDIO","'PROMEDIO EDUC. SEC.'","");
                if (Integer.parseInt(""+txtPromedioNum.getText().charAt(0)) < 6 && !lblPromedioLetra.getText().equals("DIEZ"))
                    return mensaje.Secundaria ("PROMEDIO","'PROMEDIO'","");
            }
            //***************************** VERIFICAMOS EL TAMAÑO DE CLF Y CEE **************************
            if (txtCiciniCLF.getText().trim().length()!=4)
                return mensaje.Secundaria ("RANGO","El campo 'Ciclo CLF'","4");
            if (txtCiciniCEE.getText().trim().length()!=4)
                return mensaje.Secundaria ("RANGO","El campo 'Ciclo CEE'","4");
            //***************************** VERIFICAMOS EL FORMATO DEL FOLIO **************************
            if (idsFormatosfolio.get(cbxFormatosFolio.getSelectedIndex())==1)
                return mensaje.Secundaria ("FORMATFOL_INDEFINIDO","","");
            /*if (!"".equals(msgFol=global.validarFormatoFolio(txtFolio.getText().trim(), idsFormatosfolio.get(cbxFormatosFolio.getSelectedIndex()),txtAnioAcreditacion.getText()))){
                if (msgFol.equals("LET_FOL_Y_CICFIN")){
                    if (!rbnSiEsRegularizacion.isSelected())
                        return mensaje.Secundaria("LET_FOL_Y_CICFIN", "", "");
                }else if (msgFol.equals("ENTIDAD_EN_FOLIO"))
                    return mensaje.ModuloCertificacion(this,msgFol, "", "");
                else
                    return mensaje.Secundaria (msgFol,"","");
            }*/
            
            //-----------------------------------------------------------------------------------------

                        //--------- Verificamos que el ciclo del folio sea el adecuado para el formato a usar
            if (cbxTipoEducacion.getSelectedItem().equals("Secundaria")){
                if ( (Integer.parseInt(txtCiciniCLF.getText().trim())+1) == 2013){
                    if (!false/*Revision--> chkEsCEBAS.isSelected()*/)
                        return mensaje.Secundaria ("FORMATO_PARA_CLF",txtCiciniCLF.getText()+lblCicfinCLF.getText(),"NIVEL EDUCATIVO");
                }
            }else{
                if ( (Integer.parseInt(txtCiciniCLF.getText().trim())+1) != 2013)
                    return mensaje.Secundaria ("FORMATO_PARA_CLF",txtCiciniCLF.getText()+lblCicfinCLF.getText(),"EDUCBAS_OCT14");
            }
                        //--------- Verificamos que el ciclo del folio no sea menor al del que estudió
            try{
                if (Integer.parseInt(txtCiciniCLF.getText())<Integer.parseInt(txtCiciniCEE.getText()) && !id_cct_esc_cvet_turno.get(0)[1].substring(2,5).matches("DML|DBA|HMC|HSL"))
                        return mensaje.CrearCertifOrig(this,"CICLO_CLF<CEE","","");
            }catch (IndexOutOfBoundsException ex){/*Esta excepción ocurre en variable id_cct_esc_cvet_turno cuando se elije cct manual*/}
                        //--------- Verificamos que el ciclo del folio sea menor o igual al libro
            if (Integer.parseInt(txtCiciniCLF.getText())>Integer.parseInt((""+cbxCicloCLD.getSelectedItem()).substring(0,4)))
                    return mensaje.CrearCertifOrig(this,"CICLO_CLF>CLD","","");
                        //--------- Verificamos que si está creando uno nuevo sea sólo para extranjeros o ciclos menores a 2005 o modalidad DBA, HMC Y HSL
            if ( this.casoLlamada.equals("Nuevo") && txtAnioAcreditacion.getText().matches("[0-9]+") && Integer.parseInt(txtAnioAcreditacion.getText())>=2005 )
            {
                try {
                    if ((""+cbxCCTEscuela.getSelectedItem()).trim().matches("20[A-Za-z]{3}[0-9]{4}[A-Za-z]{1}") && !id_cct_esc_cvet_turno.get(0)[1].substring(2,5).matches("DBA|HMC|HSL"))
                        ;//return mensaje.ModuloCertificacion (this,"NUEVO_NO_ACEPTABLE","","");
                }catch (IndexOutOfBoundsException ex){/*Esta excepción ocurre en variable id_cct_esc_cvet_turno cuando se elije cct manual*/}
            }
                        //--------- Verificamos el formato de CCT
            if (cbxCCTEscuela.getSelectedItem().toString().trim().matches("[0-9]{2}[A-Za-z]{3}[0-9]{4}[A-Za-z]{1}") && chkEditManEscu.isSelected())
                return mensaje.Secundaria ("AGREGAR_A_CATESCU","","");
            
            /*Revision--> if ( !txtDictamenNumero.getText().equals("") && !txtDictamenFecha.getText().equals("") ){
                if ( !txtDictamenNumero.getText().trim().matches("[0-9]{1,3}/[0-9]{4}") )
                    return mensaje.CrearCertifOrig(this,"FORMATO_NUM_DICTAMEN","","");
                if ( !txtDictamenFecha.getText().trim().matches("[0-9]{2}/[0-9]{2}/[0-9]{4}") )
                    return mensaje.CrearCertifOrig(this,"FORMATO_FECHA_DICTAMEN","","");
                if ( !global.isFecha(txtDictamenFecha.getText().trim(), "dd/MM/yyyy"))
                    return mensaje.CrearCertifOrig(this,"FECHA_DICTAMEN_INVALIDA","","");
            }
            
            if (!cbxLugarValidacion.getSelectedItem().equals("") && (""+cbxCCTEscuela.getSelectedItem()).substring(0,2).equals("20"))
                return mensaje.ModuloCertificacion (this,"LUGAR_VALIDACIÓN_NO_ACEPTADO","","");*/
            
                        //--------- Verificamos la fecha tenga un valor correcto
            //++++Quitar++++-> try {
                //++++Quitar++++-> if (!global.isFecha(txtFecha.getText().trim(), "dd/MM/yyyy"))
                //++++Quitar++++->     return mensaje.Secundaria ("FECHA","del lado reverso","");
                //++++Quitar++++-> fecha = txtFecha.getText().trim();
                //++++Quitar++++-> if (cbxDiaExpedicion.getSelectedIndex()!= Integer.parseInt(fecha.substring(0, 2))-1 && cbxMesExpedicion.getSelectedIndex()!=Integer.parseInt(fecha.substring(3, 5))-1)
                //++++Quitar++++->     return mensaje.Secundaria ("FECHA_EXPED_Y_FECHA","","");
            //++++Quitar++++-> }catch (Exception ex){ return mensaje.Secundaria ("FECHA","del lado reverso",""); }
                        //--------- Verificamos la vigencia del formato
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy"); 
            df.setLenient(false);
            java.util.Date fechaExped = df.parse(global.convertirTextoAFecha(""+cbxDiaExpedicion.getSelectedItem(),""+cbxMesExpedicion.getSelectedItem(),lblAnioExpedicion.getText().toLowerCase().replace("del ", "")));
            java.util.Date fechaExpIni_1213 = df.parse("08/07/2013"); 
            java.util.Date fechaExpFin_1213 = df.parse("14/07/2014");
            java.util.Date fechaExpIni_1314 = df.parse("16/07/2014"); 
            java.util.Date fechaExpFin_1314 = df.parse("13/07/2015");
            java.util.Date fechaExpIni_1415 = df.parse("14/07/2015"); 
            java.util.Date fechaExpFin_1415 = df.parse("14/07/2016");
            java.util.Date fechaExpIni_1516 = df.parse("14/07/2016"); 
            java.util.Date fechaExpFin_1516 = df.parse("18/07/2017");
            java.util.Date fechaExpIni_1617 = df.parse("26/07/2017"); 
            java.util.Date fechaExpFin_1617 = df.parse("9/07/2018");
            /*if (cbxCicloCLD.getSelectedItem().equals("2012-2013") && (fechaExped.before(fechaExpIni_1213) || fechaExped.after(fechaExpFin_1213)))
                return mensaje.ModuloCertificacion (this,"VIGENCIA_FORMATO","2012-2013","08/07/2013 y el 14/07/2014");
            else if (cbxCicloCLD.getSelectedItem().equals("2013-2014") && (fechaExped.before(fechaExpIni_1314) || fechaExped.after(fechaExpFin_1314)))
                return mensaje.ModuloCertificacion (this,"VIGENCIA_FORMATO",""+cbxCicloCLD.getSelectedItem(),"16/07/2014 y el 13/07/2015");
            else if (cbxCicloCLD.getSelectedItem().equals("2014-2015") && (fechaExped.before(fechaExpIni_1415) || fechaExped.after(fechaExpFin_1415)))
                return mensaje.ModuloCertificacion (this,"VIGENCIA_FORMATO",""+cbxCicloCLD.getSelectedItem(),"14/07/2015 y el 14/07/2016");
            else if (cbxCicloCLD.getSelectedItem().equals("2015-2016") && (fechaExped.before(fechaExpIni_1516) || fechaExped.after(fechaExpFin_1516)))
                return mensaje.ModuloCertificacion (this,"VIGENCIA_FORMATO",""+cbxCicloCLD.getSelectedItem(),"18/07/2017 y el 18/07/2018");
            else if (cbxCicloCLD.getSelectedItem().equals("2016-2017") && (fechaExped.before(fechaExpIni_1617) || fechaExped.after(fechaExpFin_1617)))
                return mensaje.ModuloCertificacion (this,"VIGENCIA_FORMATO",""+cbxCicloCLD.getSelectedItem(),"26/07/2017 y el 18/07/2018"); */
            
            //***************************** VERIFICAMOS LA NORMATIVIDAD DE EDADES **************************
            String  mensajeEdad;
            int cicIniCLF=Integer.parseInt(txtCiciniCLF.getText()), cicIniCLD=Integer.parseInt((""+cbxCicloCLD.getSelectedItem()).substring(0,4));
            
            if (!id_cct_esc_cvet_turno.isEmpty() && cicIniCLF>=cicIniCLD-3) //Vamos a verificar solo los que no sean cct manual y hasta 3 años atrás del ciclo actual
            {
                if (id_cct_esc_cvet_turno.get(0)[1].substring(0,2).equals("20")) //Revisamos únicamente los que son del estado (Entidad 20)
                {
                    if (bkuCurp.equals("") || !bkuCurp.substring(4,6).equals(txtCurp.getText().substring(4,6)))  //Solo a los nuevos, o si le cambiaron el año a la curp al editar o importar
                    {
                        try {
                            conexion.conectar();
                            
                            if (idsCasocurp.get(cbxCasocurp.getSelectedIndex()) == 4){           //Si eligió no validar edad pedimos un password y verificamos permisos
                                if (!permisoDeNoValidarEdad)
                                    return mensaje.ModuloCertificacion(this,"SIN_PERMISO_DE_NO_VALIDAR_EDAD", "", "");
                                if (JOptionPane.NO_OPTION == mensaje.confirmDialog("CONFIRMAR_NO_VALIDAR_EDAD") )
                                    return false;
                                else{
                                    String passwordParaNoValidarEdad = mensaje.inputBox(this, "Ingreso de contraseña", "Introduzca su contraseña de usuario para corroborar\nque tiene permisos de no validar la edad:", "");
                                    if (passwordParaNoValidarEdad==null || passwordParaNoValidarEdad.equals(""))
                                        return false;
                                    conexion.verificarPermisoConPassword (passwordParaNoValidarEdad, global.capturista, 18);
                                }
                            }else
                                if (!"".equals(mensajeEdad=global.cumpleConLaEdad (txtCurp.getText().substring(4,10), ""+cicIniCLD, ""+global.cveplan, "3", id_cct_esc_cvet_turno.get(0)[0], id_cct_esc_cvet_turno.get(0)[1].substring(2,5), "20", conexion)))
                                    return mensaje.CrearCertifOrig(this,"NO_CUMPLE_CON_EDAD",mensajeEdad,bkuCurp);
                            
                        }catch (Exception ex){
                            if (ex==null || ex.getMessage()==null)
                                return mensaje.General(this,"GENERAL", ""+ex, "");
                            else if (ex.getMessage().equals("FECHA_NAC"))
                                return mensaje.CrearCertifOrig(this,"FECHA_NAC","",""); 
                            else if (ex.getMessage().equals("EDAD_CVEPROGRAMA"))
                                return mensaje.CrearCertifOrig(this, "EDAD_CVEPROGRAMA", "", "");
                            else if (ex.getMessage().contains("NORMATIVIDAD_INDEF"))
                                return mensaje.ModuloCertificacion(this, "NORMATIVIDAD_INDEF", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
                            else if (ex.getMessage().contains("PASSWD_INCORRECTA"))
                                return mensaje.ModuloCertificacion(this,"PASSWD_INCORRECTA", "", "");
                            else if (ex.getMessage().contains("SIN_PERMISO_DE_NO_VALIDAR_EDAD"))
                                return mensaje.ModuloCertificacion(this,"SIN_PERMISO_DE_NO_VALIDAR_EDAD", "", "");
                            else
                                return mensaje.General(this,"GENERAL", ""+ex, "");
                        }finally {
                            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                            //this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
                        }
                    }
                }
            }

        } catch(Exception e){  mensaje.General(this, "GENERAL", ""+e, ""); return false;  }
        return true;
    }

    private void cambios (boolean accion)
    {
        btnGuardar.setEnabled(accion);                                          //Si hay cambios se activa el botón, si no lo desactivamos
    }
    
    /*Revision--> private void setLugaresValidacion ()
    { 
        int posDefault =0;
        ArrayList tooltips = new ArrayList();
        SISCERT_ComboboxToolTipRenderer toolTipComboBox = new SISCERT_ComboboxToolTipRenderer();
        for (int i=0; i<global.lugaresValidacion.size(); i++)
        {
            cbxLugarValidacion.addItem(global.lugaresValidacion.get(i)[2]==null?global.lugaresValidacion.get(i)[1]:global.lugaresValidacion.get(i)[2]);
            tooltips.add(global.lugaresValidacion.get(i)[1]);
            if (global.lugaresValidacion.get(i)[3].equals("default"))
                posDefault=i;
        }
        cbxLugarValidacion.setSelectedIndex(posDefault);
        toolTipComboBox.setListToolTip(tooltips);
        cbxLugarValidacion.setRenderer(toolTipComboBox);
    }*/
    
    private void getCasosFolio_y_curp ()
    {
        try {       //---------- Guardamos datos del alumno
            conexion.conectar ();            
            conexion.getFormatosfolio(idsFormatosfolio,cbxFormatosFolio);
            conexion.getCasoscurp(idsCasocurp,cbxCasocurp);
            cbxFormatosFolio.setSelectedIndex(0);
            cbxCasocurp.setSelectedIndex(0);
            cambios (false);
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
    }
    
    private void initIdscctDefault ()
    {
        String ides[][] ={
            {"23976", "MÉXICO - SEC. GEN.","---"},{"30690", "MÉXICO - SEC. TEC.","DST,PST"},{"30691", "MÉXICO - TELESEC.","DTV,PTV"},{"30644", "E.U.A","EUA"},
            {"30647", "OTRO PAÍS", "EXT"}
        };
        String v[];
        
        //Clasificamos por id-descripción
        for (int i=0; i<ides.length; i++){
            idscctDefault.put(ides[i][0], ides[i][1]);
            cbxEscuelaDe.addItem(ides[i][1]);
        }
        //Clasificamos por descripción-id
        for (int i=0; i<ides.length; i++)
            idscctDefault.put(ides[i][1], ides[i][0]);
        //Clasificamos por modalidad-descripción
        for (int i=0; i<ides.length; i++){
            v=ides[i][2].split(",");
            for (int j=0; j<v.length; j++)
                idscctDefault.put(v[j], ides[i][1]);
        }
        
        //Para recorrer los valores
        /*for (Object value : idscctDefault.values()){
            cbxEscuelaDe.addItem(value);
        }*/
        //Para recorrer las claves
        //for (String key : map.keySet()) { /* ... */ }
        
        //Para recorrer las clave-valor
        /*Iterator iterador = idsDefault.entrySet().iterator();
        while (iterador.hasNext()) {
            Map.Entry pairs = (Map.Entry)iterador.next();                       //Integer key = it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());      //System.out.println("Clave: " + key + " -> Valor: " + treeMap.get(key));
            //iterador.remove(); // avoids a ConcurrentModificationException
        }*/
    }
    
    private void reestablecer_lblVar ()
    {
        /*Revision--> lblVarDelegacion.setText(bkuDelegacion);
        lblVarLugarExpedicion.setText(bkuLugarExpedicion);
        lblVarCct.setText(bkuCCTDelegacion);
        lblVarDelegado.setText(bkuDelegado);
        lblVarCargoDelegado.setText(bkuCargoDelegado);*/
    }
    
        //---------------- Hace una consulta por Escuela o por CCT y los carga en los combos respectivos
    private void cargarCCTEscuela (String texto, String caso)
    {
        cbxCCTEscuela.removeAllItems();
        cbxEscuela.removeAllItems();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        id_cct_esc_cvet_turno.clear();
        hid_hcct_hesc_hcvet_hturno.clear();
        try {
            conexion.conectar();
            conexion.getEscuelas(caso, texto, id_cct_esc_cvet_turno, cbxCCTEscuela, cbxEscuela,"2");
            setEscuelaDe ();
            cambios(true);
        } catch (SQLException ex) { mensaje.General(this,"CONEXION", ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }
    
    private void setEscuelaDe ()
    {        
        if (!id_cct_esc_cvet_turno.isEmpty()){
            try {
                String modalidad = (""+cbxCCTEscuela.getSelectedItem()).substring(2,5);
                if (idscctDefault.containsKey(modalidad))
                    cbxEscuelaDe.setSelectedItem(""+idscctDefault.get(modalidad));
                else
                    cbxEscuelaDe.setSelectedItem(""+idscctDefault.get("---"));  //México - SEC. GEN.
            }catch (Exception ex){ cbxEscuelaDe.setSelectedItem(""+idscctDefault.get("---")); }
        }else
            cbxEscuelaDe.setSelectedItem(null);
    }
    
    //Obtenemos el folio ya particionado
    private String [] getFolio ()
    {
        String idFormatFol_folLet_folNum[] = {"","",""};
        int idfol = idsFormatosfolio.get(cbxFormatosFolio.getSelectedIndex());
        if(idfol==2){ //(CICLO, ENTIDAD Y 7 NÚMEROS)
            idFormatFol_folLet_folNum[0]=""+idfol;
            idFormatFol_folLet_folNum[1]="_";
            idFormatFol_folLet_folNum[2]=txtFolio.getText().trim();
        }else if (idfol==4){ //(CICLO, ENTIDAD Y NÚMEROS)
            idFormatFol_folLet_folNum[0]=""+idfol;
            idFormatFol_folLet_folNum[1]="_";
            idFormatFol_folLet_folNum[2]=txtFolio.getText().trim().substring(3);
        }else if (idfol==3 || idfol==5 || idfol==9){ //(LETRA Y 7 NÚMEROS), (LETRA Y NÚMEROS), (LETRA, CICLO, ENTIDAD Y 7 NÚMEROS)
            idFormatFol_folLet_folNum[0]=""+idfol;
            idFormatFol_folLet_folNum[1]=""+txtFolio.getText().trim().toUpperCase().charAt(0);
            idFormatFol_folLet_folNum[2]=txtFolio.getText().trim().substring(1);
        }else if (idfol==6){ //(SÓLO NÚMEROS)
            idFormatFol_folLet_folNum[0]=""+idfol;
            idFormatFol_folLet_folNum[1]="_";
            idFormatFol_folLet_folNum[2]=txtFolio.getText().trim();
        }else if (idfol==7){
            idFormatFol_folLet_folNum[0]=""+idfol;
            idFormatFol_folLet_folNum[1]="_";
            idFormatFol_folLet_folNum[2]="SIN FOLIO";
        }else if(idfol==10) { //(ENTIDAD, CICLO Y 7 NÚMEROS)
            idFormatFol_folLet_folNum[0]=""+idfol;
            idFormatFol_folLet_folNum[1]="CE";
            idFormatFol_folLet_folNum[2]=txtFolio.getText().trim().substring(2);
        }
        return idFormatFol_folLet_folNum;
    }
    
    private String [] getDatosEscuela ()
    {
        int posSelCCT;
        String datosEscuela[];
        String tablaEscuela, escuela, cct, cveturno, idcct, idcctHEscuela="0";

        if (chkEditManEscu.isSelected())
        {
            idcct = ""+idscctDefault.get(cbxEscuelaDe.getSelectedItem());
            idcctHEscuela = "0";
            tablaEscuela="E";
            escuela = ""+cbxEscuela.getSelectedItem();
            cct = ""+cbxCCTEscuela.getSelectedItem();
            cveturno="100";
        }else{
            posSelCCT = cbxCCTEscuela.getSelectedIndex();
            idcct = id_cct_esc_cvet_turno.get(posSelCCT)[0];
            tablaEscuela="E";
            escuela = id_cct_esc_cvet_turno.get(posSelCCT)[2];
            cct = id_cct_esc_cvet_turno.get(posSelCCT)[1];
            cveturno = id_cct_esc_cvet_turno.get(posSelCCT)[3];
            if (rbnEscuelaHistorica.isSelected()) 
            { 
                tablaEscuela = "H"; 
                idcctHEscuela=hid_hcct_hesc_hcvet_hturno.get(cbxHEscuela.getSelectedIndex())[0]; 
                escuela = hid_hcct_hesc_hcvet_hturno.get(cbxHEscuela.getSelectedIndex())[2]; 
            }
        }
        datosEscuela=new String[]{idcct,idcctHEscuela,tablaEscuela,escuela,cct,cveturno};
        return datosEscuela;
    }
    
    private String getFechaLetCEBAS ()
    {
        String fecha;
        /*Revision--> if (chkEsCEBAS.isSelected())
            fecha = "SECUNDARIA EL " + cbxDiaCEBAS.getSelectedItem()+" DE "+cbxMesAcreditacion.getSelectedItem()+((Integer.parseInt(txtAnioAcreditacion.getText())>=2000)?" DEL ":" DE ")+txtAnioAcreditacion.getText();
        else*/
            fecha = "";
        return fecha;
    }
             //---------------- Cargar ciclo escolar activo
    private void cargarCicloEscolar ()
    {
        cbxCicloCLD.removeAllItems();        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try {
            conexion.conectar();
            conexion.getCicloEscolar(cbxCicloCLD);
        } catch (SQLException ex) { mensaje.General(this,"CONEXION", ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }
    
    // ------------------- Devuleve el promedio introducido por el usuario de forma corregida: 83 -> 8.3
    private String getPromedioNum (javax.swing.JTextField texto)
    {
        String promLetra;
        if ((texto.getText().trim().equals("")))
            promLetra = "";
        else if (texto.getText().matches("[A,a]") || texto.getText().equals("10"))
            promLetra = texto.getText().toUpperCase();
        else
            promLetra = texto.getText ().charAt(0)+"."+texto.getText().charAt(1);
        return promLetra;
    }
    
    private void actualizartblSISCERT ()
    {
        Object[] fila = new Object[modelSISCERT.getColumnCount()], filaOculta = new Object[modelSISCERT.getHiddenColumnCount()];
        
        fila[0]=modelSISCERT.getValueAt(posSelTblSISCERT, 0);
        fila[1]=global.NoControl;
        fila[2]=global.numSolicitud;
        fila[3]=txtFoja.getText().trim();
        fila[4]=""+cbxCicloCLD.getSelectedItem();
        fila[5]=idAluSICEEB;
        fila[6]=txtNombre.getText().trim().toUpperCase();
        fila[7]=txtApePaterno.getText().trim().toUpperCase();
        fila[8]=txtApeMaterno.getText().trim().toUpperCase();
        fila[9]=txtCurp.getText().trim().toUpperCase();
        fila[10]=cbxTipoEducacion.getSelectedItem().equals("Básica")?"EDUCACIÓN BÁSICA":"NIVEL EDUCATIVO";
        fila[11]=this.nombreFormatoCert;
        
        filaOculta[0]=global.idcertificacion;
       
        modelSISCERT.replaceAt(posSelTblSISCERT, fila, filaOculta);
        //tblSISCERT.changeSelection(pos,pos,false,false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rbngEsRegularizacion = new javax.swing.ButtonGroup();
        rbngTipoEscuela = new javax.swing.ButtonGroup();
        tpnAnversoVariables = new javax.swing.JTabbedPane();
        scrlAnverso = new javax.swing.JScrollPane();
        pnlAnverso = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        cbxFormatosFolio = new javax.swing.JComboBox<String>();
        jLabel20 = new javax.swing.JLabel();
        txtFolio = new javax.swing.JTextField();
        cbxCicloCLD = new javax.swing.JComboBox<String>();
        jLabel13 = new javax.swing.JLabel();
        rbnNoEsRegularizacion = new javax.swing.JRadioButton();
        rbnSiEsRegularizacion = new javax.swing.JRadioButton();
        lblCLF = new javax.swing.JLabel();
        txtCiciniCLF = new javax.swing.JTextField();
        lblCicfinCLF = new javax.swing.JLabel();
        lblCEE = new javax.swing.JLabel();
        txtCiciniCEE = new javax.swing.JTextField();
        lblCicfinCEE = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        cbxTipoNumsolicitud = new javax.swing.JComboBox<String>();
        txtNumSolicitud = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        lblNoControl = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        lblIdAlu = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        txtLibro = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtFoja = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtApePaterno = new javax.swing.JTextField();
        txtApeMaterno = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnCrearCurpDefault = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtCurp = new javax.swing.JTextField();
        cbxCasocurp = new javax.swing.JComboBox<String>();
        jLabel7 = new javax.swing.JLabel();
        cbxMesAcreditacion = new javax.swing.JComboBox<String>();
        jLabel8 = new javax.swing.JLabel();
        txtAnioAcreditacion = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        chkEditManEscu = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        rbnEscuelaActual = new javax.swing.JRadioButton();
        rbnEscuelaHistorica = new javax.swing.JRadioButton();
        cbxHEscuela = new javax.swing.JComboBox<String>();
        cbxEscuela = new javax.swing.JComboBox<String>();
        btnBuscarNombreEscuela = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        cbxCCTEscuela = new javax.swing.JComboBox<String>();
        btnBuscarCCTEscuela = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        cbxEscuelaDe = new javax.swing.JComboBox<String>();
        jLabel12 = new javax.swing.JLabel();
        txtPromedioNum = new javax.swing.JTextField();
        lblPromedioLetra = new javax.swing.JLabel();
        lblALos = new javax.swing.JLabel();
        cbxDiaExpedicion = new javax.swing.JComboBox<String>();
        lblDiasDelMes = new javax.swing.JLabel();
        cbxMesExpedicion = new javax.swing.JComboBox<String>();
        lblAnioExpedicion = new javax.swing.JLabel();
        txtProm_EducPrim = new javax.swing.JTextField();
        lblProm_EducPrim = new javax.swing.JLabel();
        lblProm_EducSec = new javax.swing.JLabel();
        txtProm_EducSec = new javax.swing.JTextField();
        cbxTipoEducacion = new javax.swing.JComboBox<String>();
        lblDeCEBAS = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtDiaAcreditacion = new javax.swing.JTextField();
        pnlVariables = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        btnSalir = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlAnverso.setMaximumSize(new java.awt.Dimension(819, 563));
        pnlAnverso.setPreferredSize(new java.awt.Dimension(819, 563));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del folio"));

        jLabel17.setText("El folio rojo para este duplicado pertenece al ciclo escolar (CLD):");

        jLabel18.setText("Folio original:");

        jLabel19.setText("Formato del folio:");

        cbxFormatosFolio.setPreferredSize(new java.awt.Dimension(200, 20));
        cbxFormatosFolio.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxFormatosFolio_ItemStateChanged(evt);
            }
        });

        jLabel20.setText("Folio:");

        txtFolio.setPreferredSize(new java.awt.Dimension(105, 20));
        txtFolio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtFolio_KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFolio_KeyTyped(evt);
            }
        });

        cbxCicloCLD.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxCicloCLD_ItemStateChanged(evt);
            }
        });
        cbxCicloCLD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxCicloCLDActionPerformed(evt);
            }
        });

        jLabel13.setText("¿El alumno es de regularización o adulto?");

        rbnNoEsRegularizacion.setText("No");
        rbnNoEsRegularizacion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbnNoEsRegularizacion_ItemStateChanged(evt);
            }
        });

        rbnSiEsRegularizacion.setText("Sí");
        rbnSiEsRegularizacion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbnSiEsRegularizacion_ItemStateChanged(evt);
            }
        });

        lblCLF.setText("Ciclo escolar del libro donde se extrae su folio de primera emisión (CLF):");

        txtCiciniCLF.setPreferredSize(new java.awt.Dimension(48, 20));
        txtCiciniCLF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCiciniCLF_KeyTyped(evt);
            }
        });

        lblCicfinCLF.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCicfinCLF.setText("lblCicfinCLF");

        lblCEE.setText("Ciclo escolar en el que estudió 3er grado (CEE):");

        txtCiciniCEE.setPreferredSize(new java.awt.Dimension(48, 20));
        txtCiciniCEE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCiciniCEE_KeyTyped(evt);
            }
        });

        lblCicfinCEE.setText("lblCicfinCEE");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxCicloCLD, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxFormatosFolio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFolio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbnNoEsRegularizacion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbnSiEsRegularizacion))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblCLF)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCiciniCLF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCicfinCLF))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblCEE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCiciniCEE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCicfinCEE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(cbxCicloCLD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(rbnNoEsRegularizacion)
                    .addComponent(rbnSiEsRegularizacion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCLF)
                    .addComponent(txtCiciniCLF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCicfinCLF))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCEE)
                    .addComponent(txtCiciniCEE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCicfinCEE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19)
                    .addComponent(cbxFormatosFolio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(txtFolio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("No. de Ctrl y Solicitud"));

        jLabel21.setText("Num. solicitud");

        cbxTipoNumsolicitud.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nuevo", "Manual", "Actual" }));
        cbxTipoNumsolicitud.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxTipoNumsolicitud_ItemStateChanged(evt);
            }
        });

        txtNumSolicitud.setPreferredSize(new java.awt.Dimension(60, 20));
        txtNumSolicitud.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNumSolicitud_KeyTyped(evt);
            }
        });

        jLabel22.setText("Número de control:");

        lblNoControl.setText("lblNoCtrl");

        jLabel24.setText("idAlu:");

        lblIdAlu.setText("lblIdAlu");

        jLabel26.setText("Libro:");

        txtLibro.setPreferredSize(new java.awt.Dimension(40, 20));
        txtLibro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtLibro_KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtLibro_KeyTyped(evt);
            }
        });

        jLabel27.setText("Foja:");

        txtFoja.setPreferredSize(new java.awt.Dimension(40, 20));
        txtFoja.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtFoja_KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFoja_KeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxTipoNumsolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNumSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNoControl))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblIdAlu))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLibro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFoja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(cbxTipoNumsolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumSolicitud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(lblNoControl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(lblIdAlu))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(txtLibro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27)
                    .addComponent(txtFoja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setText("Nombre del (de la) interesado(a):");

        txtNombre.setPreferredSize(new java.awt.Dimension(130, 20));
        txtNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNombre_KeyTyped(evt);
            }
        });

        txtApePaterno.setPreferredSize(new java.awt.Dimension(130, 20));
        txtApePaterno.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtApePaterno_KeyTyped(evt);
            }
        });

        txtApeMaterno.setPreferredSize(new java.awt.Dimension(130, 20));
        txtApeMaterno.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtApeMaterno_KeyTyped(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(78, 78, 78));
        jLabel2.setText("Nombre(s):");

        jLabel3.setForeground(new java.awt.Color(78, 78, 78));
        jLabel3.setText("Primer apellido:");

        jLabel4.setForeground(new java.awt.Color(78, 78, 78));
        jLabel4.setText("Segundo apellido:");

        btnCrearCurpDefault.setText("Crear CURP default");
        btnCrearCurpDefault.setToolTipText("Crea una CURP default para este nombre");
        btnCrearCurpDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearCurpDefault_ActionPerformed(evt);
            }
        });

        jLabel5.setForeground(new java.awt.Color(78, 78, 78));
        jLabel5.setText("Caso de CURP");

        jLabel6.setText("CURP:");

        txtCurp.setPreferredSize(new java.awt.Dimension(150, 20));
        txtCurp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCurp_KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCurp_KeyTyped(evt);
            }
        });

        cbxCasocurp.setPreferredSize(new java.awt.Dimension(134, 20));
        cbxCasocurp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxCasocurp_ActionPerformed(evt);
            }
        });

        jLabel7.setText("Cursó y acreditó la Educación");

        cbxMesAcreditacion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" }));
        cbxMesAcreditacion.setPreferredSize(new java.awt.Dimension(86, 20));
        cbxMesAcreditacion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxMesAcreditacion_ItemStateChanged(evt);
            }
        });

        jLabel8.setText("de");

        txtAnioAcreditacion.setPreferredSize(new java.awt.Dimension(60, 20));
        txtAnioAcreditacion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtAnioAcreditacion_KeyTyped(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la escuela"));

        chkEditManEscu.setText("Editar manualmente:");
        chkEditManEscu.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkEditManEscu_ItemStateChanged(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel9.setText("Escuela:");

        rbnEscuelaActual.setText("Actual:");
        rbnEscuelaActual.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbnEscuelaActual_ItemStateChanged(evt);
            }
        });

        rbnEscuelaHistorica.setText("Histórica:");
        rbnEscuelaHistorica.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbnEscuelaHistorica_ItemStateChanged(evt);
            }
        });

        cbxHEscuela.setPreferredSize(new java.awt.Dimension(240, 20));
        cbxHEscuela.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxHEscuela_ItemStateChanged(evt);
            }
        });

        cbxEscuela.setPreferredSize(new java.awt.Dimension(240, 20));
        cbxEscuela.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxEscuela_ItemStateChanged(evt);
            }
        });

        btnBuscarNombreEscuela.setText("Buscar...");
        btnBuscarNombreEscuela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarNombreEscuela_ActionPerformed(evt);
            }
        });

        jLabel10.setText("CCT:");

        cbxCCTEscuela.setPreferredSize(new java.awt.Dimension(230, 20));
        cbxCCTEscuela.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxCCTEscuela_ItemStateChanged(evt);
            }
        });

        btnBuscarCCTEscuela.setText("Buscar...");
        btnBuscarCCTEscuela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarCCTEscuela_ActionPerformed(evt);
            }
        });

        jLabel11.setText("Escuela de:");

        cbxEscuelaDe.setPreferredSize(new java.awt.Dimension(130, 20));
        cbxEscuelaDe.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxEscuelaDe_ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkEditManEscu)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(rbnEscuelaHistorica)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxHEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(rbnEscuelaActual)
                        .addGap(18, 18, 18)
                        .addComponent(cbxEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBuscarNombreEscuela))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(cbxCCTEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuscarCCTEscuela))
                    .addComponent(cbxEscuelaDe, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jSeparator1))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel9)
                                .addComponent(rbnEscuelaActual)
                                .addComponent(cbxEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnBuscarNombreEscuela))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(rbnEscuelaHistorica)
                                .addComponent(cbxHEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cbxCCTEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnBuscarCCTEscuela)
                                .addComponent(jLabel10))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel11)
                                .addComponent(cbxEscuelaDe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(chkEditManEscu)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel12.setText("Promedio final:");

        txtPromedioNum.setPreferredSize(new java.awt.Dimension(60, 20));
        txtPromedioNum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPromedioNum_KeyTyped(evt);
            }
        });

        lblPromedioLetra.setText("lblPromedioLetra");
        lblPromedioLetra.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        lblPromedioLetra.setPreferredSize(new java.awt.Dimension(182, 14));

        lblALos.setText("A los");

        cbxDiaExpedicion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "primer", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte", "veintiún", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve", "treinta", "treinta y un" }));
        cbxDiaExpedicion.setPreferredSize(new java.awt.Dimension(100, 20));
        cbxDiaExpedicion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxDiaExpedicion_ItemStateChanged(evt);
            }
        });

        lblDiasDelMes.setText("días del mes de");

        cbxMesExpedicion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" }));
        cbxMesExpedicion.setPreferredSize(new java.awt.Dimension(86, 20));
        cbxMesExpedicion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxMesExpedicion_ItemStateChanged(evt);
            }
        });

        lblAnioExpedicion.setText("lblAnioExpedicion");

        txtProm_EducPrim.setPreferredSize(new java.awt.Dimension(60, 20));
        txtProm_EducPrim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtProm_EducPrim_KeyTyped(evt);
            }
        });

        lblProm_EducPrim.setText("Promedio de Educación Primaria:");

        lblProm_EducSec.setText("/ promedio de Educación Secundaria:");

        txtProm_EducSec.setPreferredSize(new java.awt.Dimension(60, 20));
        txtProm_EducSec.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtProm_EducSec_KeyTyped(evt);
            }
        });

        cbxTipoEducacion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Secundaria", "Básica" }));
        cbxTipoEducacion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxTipoEducacion_ItemStateChanged(evt);
            }
        });

        lblDeCEBAS.setText("de");

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("el");

        javax.swing.GroupLayout pnlAnversoLayout = new javax.swing.GroupLayout(pnlAnverso);
        pnlAnverso.setLayout(pnlAnversoLayout);
        pnlAnversoLayout.setHorizontalGroup(
            pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAnversoLayout.createSequentialGroup()
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAnversoLayout.createSequentialGroup()
                        .addGap(246, 246, 246)
                        .addComponent(jLabel5))
                    .addGroup(pnlAnversoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCurp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxCasocurp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlAnversoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(pnlAnversoLayout.createSequentialGroup()
                                    .addComponent(jLabel7)
                                    .addGap(10, 10, 10)
                                    .addComponent(cbxTipoEducacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtDiaAcreditacion, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(lblDeCEBAS)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(cbxMesAcreditacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel8)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txtAnioAcreditacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(pnlAnversoLayout.createSequentialGroup()
                                    .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(pnlAnversoLayout.createSequentialGroup()
                                            .addComponent(jLabel1)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtApePaterno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtApeMaterno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAnversoLayout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel2)
                                            .addGap(66, 66, 66)
                                            .addComponent(jLabel3)
                                            .addGap(55, 55, 55)
                                            .addComponent(jLabel4)
                                            .addGap(23, 23, 23)))
                                    .addComponent(btnCrearCurpDefault))
                                .addGroup(pnlAnversoLayout.createSequentialGroup()
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(pnlAnversoLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtPromedioNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblPromedioLetra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlAnversoLayout.createSequentialGroup()
                                .addComponent(lblALos)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxDiaExpedicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblDiasDelMes)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxMesExpedicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblAnioExpedicion))
                            .addGroup(pnlAnversoLayout.createSequentialGroup()
                                .addComponent(lblProm_EducPrim)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtProm_EducPrim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblProm_EducSec)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtProm_EducSec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(85, Short.MAX_VALUE))
        );
        pnlAnversoLayout.setVerticalGroup(
            pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAnversoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtApePaterno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtApeMaterno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnCrearCurpDefault)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtCurp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxCasocurp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbxMesAcreditacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtAnioAcreditacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxTipoEducacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeCEBAS)
                    .addComponent(jLabel14)
                    .addComponent(txtDiaAcreditacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtProm_EducPrim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProm_EducPrim)
                    .addComponent(lblProm_EducSec)
                    .addComponent(txtProm_EducSec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtPromedioNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPromedioLetra, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAnversoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblALos)
                    .addComponent(cbxDiaExpedicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDiasDelMes)
                    .addComponent(cbxMesExpedicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAnioExpedicion))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        scrlAnverso.setViewportView(pnlAnverso);

        tpnAnversoVariables.addTab("Anverso", scrlAnverso);

        javax.swing.GroupLayout pnlVariablesLayout = new javax.swing.GroupLayout(pnlVariables);
        pnlVariables.setLayout(pnlVariablesLayout);
        pnlVariablesLayout.setHorizontalGroup(
            pnlVariablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 870, Short.MAX_VALUE)
        );
        pnlVariablesLayout.setVerticalGroup(
            pnlVariablesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 575, Short.MAX_VALUE)
        );

        tpnAnversoVariables.addTab("Variables", pnlVariables);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        btnSalir.setText("Salir");
        btnSalir.setFocusable(false);
        btnSalir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSalir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalir_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSalir);

        btnNuevo.setText("Nuevo");
        btnNuevo.setFocusable(false);
        btnNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevo_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNuevo);

        btnGuardar.setText("Guardar");
        btnGuardar.setFocusable(false);
        btnGuardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnGuardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardar_ActionPerformed(evt);
            }
        });
        jToolBar1.add(btnGuardar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tpnAnversoVariables)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpnAnversoVariables)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbxFormatosFolio_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxFormatosFolio_ItemStateChanged
        cambios(true);
        if (cbxFormatosFolio.getSelectedItem().equals("SIN FOLIO"))
            txtFolio.setText("SIN FOLIO");
    }//GEN-LAST:event_cbxFormatosFolio_ItemStateChanged

    private void txtFolio_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFolio_KeyPressed
        if (global.revisarTextoPermitido (evt.getKeyChar(), "FOLIO") && txtFolio.getText().equals("SIN FOLIO"))
            txtFolio.setText("");
    }//GEN-LAST:event_txtFolio_KeyPressed

    private void txtFolio_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFolio_KeyTyped
        if(global.revisarTextoPermitido (evt.getKeyChar(), "FOLIO"))
        {
            cambios (true);     global.limitText (evt,txtFolio, 11);
            if (evt.getKeyChar()== '*'){                                    //Si oprime *
                evt.consume();                                              //evt.consume no lo reconoce el KeyPressed
                txtFolio.setText("SIN FOLIO");                              //Mandamos a imprimir el texto: SIN FOLIO
            }
        }else
            evt.consume();
    }//GEN-LAST:event_txtFolio_KeyTyped

    private void cbxCicloCLD_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxCicloCLD_ItemStateChanged
        cambios(true);
    }//GEN-LAST:event_cbxCicloCLD_ItemStateChanged

    private void cbxTipoNumsolicitud_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxTipoNumsolicitud_ItemStateChanged
        if (cbxTipoNumsolicitud.getSelectedItem().equals("Nuevo")){
            txtNumSolicitud.setText("");
            txtNumSolicitud.setEnabled(false);
        }else if (cbxTipoNumsolicitud.getSelectedItem().equals("Manual")){
            txtNumSolicitud.setText(global.numSolicitud);
            txtNumSolicitud.setEnabled(true);
        }else if (cbxTipoNumsolicitud.getSelectedItem().equals("Actual")){
            txtNumSolicitud.setText(global.numSolicitud);
            txtNumSolicitud.setEnabled(false);
        }
        cambios(true);
    }//GEN-LAST:event_cbxTipoNumsolicitud_ItemStateChanged

    private void txtNumSolicitud_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNumSolicitud_KeyTyped
        if(global.formatearTextbox (evt,"NUMERICO", txtNumSolicitud, 10))
            cambios(true);
    }//GEN-LAST:event_txtNumSolicitud_KeyTyped

    //Sólo lo utilizamos para borrar el texto "S/L", en el momento que el usuario oprime algún caracter que no sea '*'
    private void txtLibro_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtLibro_KeyPressed
        if (global.revisarTextoPermitido (evt.getKeyChar(), "LIBRO_FOJA") && txtLibro.getText().equals("S/L"))
            txtLibro.setText("");
    }//GEN-LAST:event_txtLibro_KeyPressed

    private void txtLibro_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtLibro_KeyTyped
        if(global.revisarTextoPermitido (evt.getKeyChar(), "LIBRO_FOJA"))
        {
            cambios (true);     global.limitText (evt,txtLibro, 3);
            if (evt.getKeyChar()== '*'){                                        //Si oprime *
                evt.consume();                                                  //evt.consume no lo reconoce el KeyPressed
                txtLibro.setText("S/L");                                        //Mandamos a imprimir el texto: S/L
            }
        }else
            evt.consume();                                                      //Borra el caracter no permitido
    }//GEN-LAST:event_txtLibro_KeyTyped

    //Sólo lo utilizamos para borrar el texto "S/F", en el momento que el usuario oprime algún caracter que no sea '*'
    private void txtFoja_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFoja_KeyPressed
        if (global.revisarTextoPermitido (evt.getKeyChar(), "LIBRO_FOJA") && txtFoja.getText().equals("S/F"))
            txtFoja.setText("");
    }//GEN-LAST:event_txtFoja_KeyPressed

    private void txtFoja_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFoja_KeyTyped
        if(global.revisarTextoPermitido (evt.getKeyChar(), "LIBRO_FOJA"))
        {
            cambios (true);     global.limitText (evt,txtFoja, 3);
            if (evt.getKeyChar()== '*'){                                        //Si oprime *
                evt.consume();                                                  //evt.consume no lo reconoce el KeyPressed
                txtFoja.setText("S/F");                                         //Mandamos a imprimir el texto: S/F
            }
        }else
            evt.consume();                                                      //Borra el caracter no permitido
    }//GEN-LAST:event_txtFoja_KeyTyped

    private void txtNombre_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombre_KeyTyped
        if (global.formatearTextbox(evt, "ALFABETICO", txtNombre, 40)){
            cambios(true);
            cambioEnCurpONombre = true;
        }
    }//GEN-LAST:event_txtNombre_KeyTyped

    private void txtApePaterno_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtApePaterno_KeyTyped
        if (global.formatearTextbox(evt, "ALFABETICO", txtApePaterno, 30)){
            cambios(true);
            cambioEnCurpONombre = true;
        }
    }//GEN-LAST:event_txtApePaterno_KeyTyped

    private void txtApeMaterno_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtApeMaterno_KeyTyped
        if (global.formatearTextbox(evt, "ALFABETICO", txtApeMaterno, 30)){
            cambios(true);
            cambioEnCurpONombre = true;
        }
    }//GEN-LAST:event_txtApeMaterno_KeyTyped

    private void btnCrearCurpDefault_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearCurpDefault_ActionPerformed
        valcurp = new SISCERT_ValidarCurp(txtNombre.getText().trim().toUpperCase(), txtApePaterno.getText().trim().toUpperCase(), txtApeMaterno.getText().trim().toUpperCase(), "50", "01", "01", "-", "**",casoLlamada);
        txtCurp.setText(valcurp.curp());
    }//GEN-LAST:event_btnCrearCurpDefault_ActionPerformed

    private void txtCurp_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCurp_KeyPressed
        if ((evt.getKeyCode() == KeyEvent.VK_V) && ((evt.getModifiers() | KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK)) { //Si oprime Ctrl + v    (Pegar)
            cambios(true);
            cambioEnCurpONombre = true;
        }
    }//GEN-LAST:event_txtCurp_KeyPressed

    private void txtCurp_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCurp_KeyTyped
        if (global.formatearTextbox(evt, "CURP", txtCurp, 18)){
            cambios(true);
            cambioEnCurpONombre = true;
        }
    }//GEN-LAST:event_txtCurp_KeyTyped

    private void cbxCasocurp_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCasocurp_ActionPerformed
        cambios(true);
    }//GEN-LAST:event_cbxCasocurp_ActionPerformed

    private void cbxMesAcreditacion_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxMesAcreditacion_ItemStateChanged
        cambios(true);
    }//GEN-LAST:event_cbxMesAcreditacion_ItemStateChanged

    private void txtAnioAcreditacion_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAnioAcreditacion_KeyTyped
        String texto = this.global.getTypedText(evt,txtAnioAcreditacion);
        if (global.formatearTextbox(evt, "NUMERICO", txtAnioAcreditacion, 4)){
            if (rbnNoEsRegularizacion.isSelected() && texto.length()==4){
                txtCiciniCLF.setText(""+(Integer.parseInt(texto)-1));
                txtCiciniCEE.setText(""+(Integer.parseInt(texto)-1));
                lblCicfinCLF.setText(" - "+texto);
                lblCicfinCEE.setText(" - "+texto);
            }else{
                txtCiciniCLF.setText("");
                txtCiciniCEE.setText("");
                lblCicfinCLF.setText("");
                lblCicfinCEE.setText("");
            }
            cambios(true);
        }
    }//GEN-LAST:event_txtAnioAcreditacion_KeyTyped

    private void chkEditManEscu_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkEditManEscu_ItemStateChanged
        boolean cambio;
    
        this.lockInicializacion=true;
        rbnEscuelaActual.setSelected(true);
        this.lockInicializacion=false;

        id_cct_esc_cvet_turno.clear();
        hid_hcct_hesc_hcvet_hturno.clear();
        cbxEscuela.removeAllItems();
        cbxHEscuela.removeAllItems();
        cbxCCTEscuela.removeAllItems();
        cambio = chkEditManEscu.isSelected();
        btnBuscarCCTEscuela.setEnabled(!cambio);
        btnBuscarNombreEscuela.setEnabled(!cambio);
        cbxEscuela.setEditable(cambio);
        cbxCCTEscuela.setEditable(cambio);
        rbnEscuelaHistorica.setEnabled(!cambio);
        cbxEscuelaDe.setSelectedItem(null);     
        cbxEscuelaDe.setEnabled(cambio);

        cambios (true);
    }//GEN-LAST:event_chkEditManEscu_ItemStateChanged

    private void rbnEscuelaActual_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbnEscuelaActual_ItemStateChanged
        if (rbnEscuelaActual.isSelected()){
            cbxHEscuela.setEnabled(false);
            cbxHEscuela.setSelectedItem(null);
            cbxEscuela.setEnabled(true);
            btnBuscarNombreEscuela.setEnabled(true);
            cbxCCTEscuela.setEnabled(true);
            btnBuscarCCTEscuela.setEnabled(true);
            cambios (true);
        }
    }//GEN-LAST:event_rbnEscuelaActual_ItemStateChanged

    private void rbnEscuelaHistorica_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbnEscuelaHistorica_ItemStateChanged
        if (rbnEscuelaHistorica.isSelected() ){
            cbxHEscuela.setEnabled(true);
            cbxEscuela.setEnabled(false);
            btnBuscarNombreEscuela.setEnabled(false);
            cbxCCTEscuela.setEnabled(false);
            btnBuscarCCTEscuela.setEnabled(false);
            if (!this.lockInicializacion && cbxCCTEscuela.getSelectedIndex()==-1)
            {
                mensaje.General(this.frameParent, "H_ESCUELA", "", "");
                rbnEscuelaActual.requestFocus();
                rbnEscuelaActual.setSelected(true);
            }else if (!lockQueryHEscuela){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try{
                    conexion.conectar();
                    cbxHEscuela.removeAllItems();
                    hid_hcct_hesc_hcvet_hturno.clear();
                    conexion.getEscuelaHistorica("cct",""+cbxCCTEscuela.getSelectedItem(),hid_hcct_hesc_hcvet_hturno, cbxHEscuela,"2");
                    cambios (true);
                } catch (SQLException ex) { mensaje.General(this.frameParent, "ACTUALIZAR", "el plan de estudios",ex.getMessage()); this.dispose(); }  // Si no se pudo asignar una clave, cerramos la ventana
                catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
                try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                finally { this.setCursor(Cursor.getDefaultCursor()); }
            }
        }
    }//GEN-LAST:event_rbnEscuelaHistorica_ItemStateChanged

    private void cbxHEscuela_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxHEscuela_ItemStateChanged
        cambios(true);
    }//GEN-LAST:event_cbxHEscuela_ItemStateChanged

    private void cbxEscuela_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxEscuela_ItemStateChanged
        if (entrarACbxEscuelaYCCT)
        {
            entrarACbxEscuelaYCCT = false;
            if (cbxCCTEscuela.getItemCount()==cbxEscuela.getItemCount() && cbxCCTEscuela.getSelectedIndex()!=cbxEscuela.getSelectedIndex() && cbxEscuela.getSelectedIndex()>=0)
               cbxCCTEscuela.setSelectedIndex(cbxEscuela.getSelectedIndex());            
            setEscuelaDe ();
            cambios (true);
            entrarACbxEscuelaYCCT = true;
        }
    }//GEN-LAST:event_cbxEscuela_ItemStateChanged

    private void btnBuscarNombreEscuela_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarNombreEscuela_ActionPerformed
        String texto;
        texto = JOptionPane.showInputDialog(this,"Introdusca todo o parte del texto a buscar.", "Búsqueda parcial", JOptionPane.QUESTION_MESSAGE);
        if (texto!=null)
            cargarCCTEscuela (texto,"ESCUELA");
        else
            cbxEscuelaDe.setSelectedItem(null);
        
        cbxEscuelaDe.setEnabled(false);
    }//GEN-LAST:event_btnBuscarNombreEscuela_ActionPerformed

    private void cbxCCTEscuela_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxCCTEscuela_ItemStateChanged
        if (entrarACbxEscuelaYCCT)
        {
            entrarACbxEscuelaYCCT = false;
            if (cbxCCTEscuela.getItemCount()==cbxEscuela.getItemCount() && cbxCCTEscuela.getSelectedIndex()!=cbxEscuela.getSelectedIndex() && cbxCCTEscuela.getSelectedIndex()>=0)
               cbxEscuela.setSelectedIndex(cbxCCTEscuela.getSelectedIndex());    
            setEscuelaDe ();
            cambios (true);
            entrarACbxEscuelaYCCT = true;
        }
    }//GEN-LAST:event_cbxCCTEscuela_ItemStateChanged

    private void btnBuscarCCTEscuela_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarCCTEscuela_ActionPerformed
        String texto;
        texto = JOptionPane.showInputDialog(this,"Introdusca todo o parte del texto a buscar.", "Búsqueda parcial", JOptionPane.QUESTION_MESSAGE);
        if (texto!=null)
            cargarCCTEscuela (texto, "CCT");
        else
            cbxEscuelaDe.setSelectedItem(null);
        
        cbxEscuelaDe.setEnabled(false);
    }//GEN-LAST:event_btnBuscarCCTEscuela_ActionPerformed

    private void cbxEscuelaDe_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxEscuelaDe_ItemStateChanged
        cambios (true);
    }//GEN-LAST:event_cbxEscuelaDe_ItemStateChanged

    private void txtPromedioNum_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPromedioNum_KeyTyped
        String promedio;
        if(global.revisarTextoPermitido (evt.getKeyChar(), "NUMERICO"))
            {
                cambios (true);    promedio=global.limitText (evt,txtPromedioNum, 2);
                if (promedio.trim().length()==2){                               //Si el usuario ya introdujo los 2 números del promedio
                    lblPromedioLetra.setText(global.convertirPromedioALetra(promedio));               //Mostramos el periodoEscIni + 1
                }
                else
                    lblPromedioLetra.setText("");                               //No mostramos nada como año del período final
                    //-----------------------------------------------------------------------\\
        }
        else
            evt.consume();
    }//GEN-LAST:event_txtPromedioNum_KeyTyped

    private void cbxDiaExpedicion_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxDiaExpedicion_ItemStateChanged
        cambios(true);
        if (cbxDiaExpedicion.getSelectedItem()!=null && cbxDiaExpedicion.getSelectedItem().equals("primer")){
            lblALos.setText("Al");
            lblDiasDelMes.setText("día del mes de");
        }else{
            lblALos.setText("A los");
            lblDiasDelMes.setText("días del mes de");
        }
    }//GEN-LAST:event_cbxDiaExpedicion_ItemStateChanged

    private void cbxMesExpedicion_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxMesExpedicion_ItemStateChanged
        cambios(true);
    }//GEN-LAST:event_cbxMesExpedicion_ItemStateChanged

    private void btnSalir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalir_ActionPerformed
        Salir();
    }//GEN-LAST:event_btnSalir_ActionPerformed

    private void btnNuevo_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevo_ActionPerformed
        Nuevo();
    }//GEN-LAST:event_btnNuevo_ActionPerformed

    private void btnGuardar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardar_ActionPerformed
        Guardar();
    }//GEN-LAST:event_btnGuardar_ActionPerformed

    private void rbnNoEsRegularizacion_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbnNoEsRegularizacion_ItemStateChanged
        if (rbnNoEsRegularizacion.isSelected())
        {
            if (txtAnioAcreditacion.getText().length()==4) 
            {
                txtCiciniCLF.setText(""+(Integer.parseInt(txtAnioAcreditacion.getText())-1));
                txtCiciniCEE.setText(""+(Integer.parseInt(txtAnioAcreditacion.getText())-1));
                lblCicfinCLF.setText(" - "+txtAnioAcreditacion.getText());
                lblCicfinCEE.setText(" - "+txtAnioAcreditacion.getText());
            }else { 
                txtCiciniCLF.setText("");
                txtCiciniCEE.setText("");
                lblCicfinCLF.setText("");
                lblCicfinCEE.setText("");
            }
            txtCiciniCLF.setEnabled(false);
            txtCiciniCEE.setEnabled(false);
            lblCLF.setEnabled(false);
            lblCEE.setEnabled(false);
            lblCicfinCLF.setEnabled(false);
            lblCicfinCEE.setEnabled(false);
            cambios(true);
        }
    }//GEN-LAST:event_rbnNoEsRegularizacion_ItemStateChanged

    private void rbnSiEsRegularizacion_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbnSiEsRegularizacion_ItemStateChanged
        if (rbnSiEsRegularizacion.isSelected())
        {
            if (!casoLlamada.equals("Importar")){
                txtCiciniCLF.setText("");
                txtCiciniCEE.setText("");
                lblCicfinCLF.setText("");
                lblCicfinCEE.setText("");
            }
            lblCLF.setEnabled(true);
            lblCEE.setEnabled(true);
            txtCiciniCLF.setEnabled(true);
            txtCiciniCEE.setEnabled(true);
            lblCicfinCLF.setEnabled(true);
            lblCicfinCEE.setEnabled(true);
            cambios(true);
        }
    }//GEN-LAST:event_rbnSiEsRegularizacion_ItemStateChanged

    private void txtCiciniCLF_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCiciniCLF_KeyTyped
        String cicEscIni;
        if(global.revisarTextoPermitido (evt.getKeyChar(), "NUMERICO"))
        {
            cambios (true);
            cicEscIni = global.limitText (evt, txtCiciniCLF, 4);
            if (cicEscIni.length()==4 ){                                             //Si el usuario ya introdujo los 4 números del período escolar
                lblCicfinCLF.setText(" - "+(Integer.parseInt(cicEscIni)+1));        //Mostramos el periodoEscIni + 1
                if (Integer.parseInt(cicEscIni)==2012)
                    cbxTipoEducacion.setSelectedIndex(1);                       //Básica
                else
                    cbxTipoEducacion.setSelectedIndex(0);                       //Secundaria
                    
            }else
                lblCicfinCLF.setText("");
        }
        else
            evt.consume();
    }//GEN-LAST:event_txtCiciniCLF_KeyTyped

    private void txtCiciniCEE_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCiciniCEE_KeyTyped
        String cicEscIni;
        if(global.revisarTextoPermitido (evt.getKeyChar(), "NUMERICO"))
        {
            cambios (true);
            cicEscIni = global.limitText (evt, txtCiciniCEE, 4);
            if (cicEscIni.length()==4 )                                             //Si el usuario ya introdujo los 4 números del período escolar
                lblCicfinCEE.setText(" - "+(Integer.parseInt(cicEscIni)+1));        //Mostramos el periodoEscIni + 1
            else
                lblCicfinCEE.setText("");
        }
        else
            evt.consume();
    }//GEN-LAST:event_txtCiciniCEE_KeyTyped

    private void cbxTipoEducacion_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxTipoEducacion_ItemStateChanged
        if (cbxTipoEducacion.getSelectedItem()== null){
            //Revision--> lblCursoYAcreditoEn_El.setText("");
            lblProm_EducPrim.setEnabled(false);
            lblProm_EducSec.setEnabled(false);
            txtProm_EducPrim.setEnabled(false);
            txtProm_EducSec.setEnabled(false);

            //++++Quitar++++-> lblPlan1.setEnabled(false);
            //++++Quitar++++-> lblPlan2.setEnabled(false);
            //++++Quitar++++-> cbxPlan.setEnabled(false);
        }else { 
            //Revision--> lblCursoYAcreditoEn_El.setText("EN");
            lblProm_EducPrim.setEnabled(cbxTipoEducacion.getSelectedItem().equals("Básica"));
            lblProm_EducSec.setEnabled(cbxTipoEducacion.getSelectedItem().equals("Básica"));
            txtProm_EducPrim.setEnabled(cbxTipoEducacion.getSelectedItem().equals("Básica"));
            txtProm_EducSec.setEnabled(cbxTipoEducacion.getSelectedItem().equals("Básica"));

            //++++Quitar++++-> lblPlan1.setEnabled(cbxTipoEducacion.getSelectedItem().equals("SECUNDARIA"));
            //++++Quitar++++-> lblPlan2.setEnabled(cbxTipoEducacion.getSelectedItem().equals("SECUNDARIA"));
            //++++Quitar++++-> cbxPlan.setEnabled(cbxTipoEducacion.getSelectedItem().equals("SECUNDARIA"));
        }

        txtProm_EducPrim.setText("");
        txtProm_EducSec.setText("");
        //++++Quitar++++-> cbxPlan.setSelectedItem(null);
        cambios(true);
    }//GEN-LAST:event_cbxTipoEducacion_ItemStateChanged

    private void txtProm_EducPrim_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtProm_EducPrim_KeyTyped
        if(global.formatearTextbox (evt,"NUMERICO", txtProm_EducPrim, 2))
            cambios(true);
    }//GEN-LAST:event_txtProm_EducPrim_KeyTyped

    private void txtProm_EducSec_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtProm_EducSec_KeyTyped
        if(global.formatearTextbox (evt,"NUMERICO", txtProm_EducSec, 2))
            cambios(true);
    }//GEN-LAST:event_txtProm_EducSec_KeyTyped

    private void cbxCicloCLDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCicloCLDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxCicloCLDActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarCCTEscuela;
    private javax.swing.JButton btnBuscarNombreEscuela;
    private javax.swing.JButton btnCrearCurpDefault;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnSalir;
    private javax.swing.JComboBox<String> cbxCCTEscuela;
    private javax.swing.JComboBox<String> cbxCasocurp;
    private javax.swing.JComboBox<String> cbxCicloCLD;
    private javax.swing.JComboBox<String> cbxDiaExpedicion;
    private javax.swing.JComboBox<String> cbxEscuela;
    private javax.swing.JComboBox<String> cbxEscuelaDe;
    private javax.swing.JComboBox<String> cbxFormatosFolio;
    private javax.swing.JComboBox<String> cbxHEscuela;
    private javax.swing.JComboBox<String> cbxMesAcreditacion;
    private javax.swing.JComboBox<String> cbxMesExpedicion;
    private javax.swing.JComboBox<String> cbxTipoEducacion;
    private javax.swing.JComboBox<String> cbxTipoNumsolicitud;
    private javax.swing.JCheckBox chkEditManEscu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblALos;
    private javax.swing.JLabel lblAnioExpedicion;
    private javax.swing.JLabel lblCEE;
    private javax.swing.JLabel lblCLF;
    private javax.swing.JLabel lblCicfinCEE;
    private javax.swing.JLabel lblCicfinCLF;
    private javax.swing.JLabel lblDeCEBAS;
    private javax.swing.JLabel lblDiasDelMes;
    private javax.swing.JLabel lblIdAlu;
    private javax.swing.JLabel lblNoControl;
    private javax.swing.JLabel lblProm_EducPrim;
    private javax.swing.JLabel lblProm_EducSec;
    private javax.swing.JLabel lblPromedioLetra;
    private javax.swing.JPanel pnlAnverso;
    private javax.swing.JPanel pnlVariables;
    private javax.swing.JRadioButton rbnEscuelaActual;
    private javax.swing.JRadioButton rbnEscuelaHistorica;
    private javax.swing.JRadioButton rbnNoEsRegularizacion;
    private javax.swing.JRadioButton rbnSiEsRegularizacion;
    private javax.swing.ButtonGroup rbngEsRegularizacion;
    private javax.swing.ButtonGroup rbngTipoEscuela;
    private javax.swing.JScrollPane scrlAnverso;
    private javax.swing.JTabbedPane tpnAnversoVariables;
    private javax.swing.JTextField txtAnioAcreditacion;
    private javax.swing.JTextField txtApeMaterno;
    private javax.swing.JTextField txtApePaterno;
    private javax.swing.JTextField txtCiciniCEE;
    private javax.swing.JTextField txtCiciniCLF;
    private javax.swing.JTextField txtCurp;
    private javax.swing.JTextField txtDiaAcreditacion;
    private javax.swing.JTextField txtFoja;
    private javax.swing.JTextField txtFolio;
    private javax.swing.JTextField txtLibro;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNumSolicitud;
    private javax.swing.JTextField txtProm_EducPrim;
    private javax.swing.JTextField txtProm_EducSec;
    private javax.swing.JTextField txtPromedioNum;
    // End of variables declaration//GEN-END:variables

}
