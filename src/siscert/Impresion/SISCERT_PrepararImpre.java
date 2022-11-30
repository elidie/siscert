package siscert.Impresion;

import java.awt.Cursor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_Excepcion;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;
import webservices.WsfirmaDatosReturn;
import webservices.WsfirmaDatosReturn.DatosReturn;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:20:30 PM
 */
public class SISCERT_PrepararImpre extends javax.swing.JDialog {
    
    private final SISCERT_Mensajes mensaje;
    private final SISCERT_GlobalMethods global;
    private final SISCERT_QueriesInformix conexion;
    private final SISCERT_ModeloDeTabla modelFoliosAImprimir, modelFoliosImpresos; //tblFoliosAImprimir
    
    private int idFormatoImpre, cicescinilib;
    private boolean canImprimirDuplicado, canImprimirPrueba, canModifTamHojaImpre;
    private final ArrayList<String[]> formatosCertActivos;
    
    /** Creates new form SISCERT_PrepararImpre */
    public SISCERT_PrepararImpre(java.awt.Frame parent, boolean modal, String printIni, String printFin, ArrayList<String[]> formatosCertActivos, SISCERT_Mensajes mensaje, SISCERT_GlobalMethods global, SISCERT_QueriesInformix conexion, SISCERT_ModeloDeTabla modelSISCERT) {
        super(parent, modal);
        this.global = global;
        this.mensaje = mensaje;
        this.conexion=conexion;
        this.formatosCertActivos = formatosCertActivos;
        initComponents();
        //------------------------ CONFIGURAMOS LA TABLA DE MOSTRAR FOLIOS A CANCELAR ------------------------
        modelFoliosAImprimir = new SISCERT_ModeloDeTabla(new String[]{"Imprimir","Folio","No. Ctrl", "Nombre","CURP","Formato de Impresión"}, new String[]{"idcertificacion"});
        modelFoliosAImprimir.setClases(new Class[]{ Boolean.class,String.class, String.class, String.class, String.class, String.class });
        tblFoliosAImprimir.setModel(modelFoliosAImprimir);
        modelFoliosAImprimir.setScrollHorizontal (tblFoliosAImprimir,jScrollPane1);
        modelFoliosImpresos = new SISCERT_ModeloDeTabla(new String[]{"Imprimir","N.P.","idfolimpre","Folio","Nombre", "CURP", "Formato","Fecha Impre."});
        modelFoliosImpresos.setClases(new Class[]{ Boolean.class,String.class, String.class, String.class, String.class,String.class,String.class, String.class });
        tblFoliosImpresos.setModel(modelFoliosImpresos);
        modelFoliosImpresos.setScrollHorizontal(tblFoliosImpresos, jScrollPane2);
        //----------------------------------------------------------------------
        rbngOpcBusqFolImpre.add(rbnBuscarPorFolio);
        rbngOpcBusqFolImpre.add(rbnBuscarPorFecha);
        rbngOpcBusqFolImpre.add(rbnBuscarPorCurp);
        rbnBuscarPorFolio.setSelected(true);
        rbnBuscarPorFolio.setSelected(false);
        verificarPermisos();
        //----------------------------------------------------------------------
        rellenarTablaDeImpresion(modelSISCERT, printIni, printFin);
        //smnuImprimir.setEnabled(false);
        //btnImprimirAnverso.setEnabled(false);
    }
    

    private boolean FoliarFirmarEImprimir ()
    {
        SISCERT_Reporte reporte = null;
        String idscertificacion="";
        boolean hayFolios=false;
        
        int filas=modelFoliosAImprimir.getRowCount();
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try {
            if (filas<=0)
                throw new SISCERT_Excepcion("SIN_QUE_REIMPRIMIR");
            //Obtenemos los idcertiregion de los alumnos que se van a imprimir
            for (int i=0; i<filas; i++)
                if (modelFoliosAImprimir.getValueAt(i, 0).equals(true)){
                    idscertificacion += (i>0?", ":"")+ modelFoliosAImprimir.getHiddenValueAt(i, 0);
                    hayFolios = true;
                }
            
            if (!hayFolios)
                throw new SISCERT_Excepcion("SIN_QUE_REIMPRIMIR");

            /*try {
                throw new Exception(pingWebService("MAAI"));
            }catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }*/
            
            String formato = formatosCertActivos.get(0)[1];
            WsfirmaDatosReturn datosReturn = firmarDuplicado (idscertificacion,""+(Integer.parseInt(formato.substring(formato.length()-4))-1),""+global.cveplan, global.capturista);
            if (datosReturn==null)
                throw new SISCERT_Excepcion (-1,"NO_PUDO_FIRMAR");
            else if (datosReturn.getReturnCase() == 0)
                throw new SISCERT_Excepcion (datosReturn.getReturnCase(), "MENSAJE_WEBSERVICE", datosReturn.getMensaje());
            else if (datosReturn.getReturnCase() == -1)
                throw new SISCERT_Excepcion (datosReturn.getReturnCase(), "ERROR_WEBSERVICE", datosReturn.getMensaje());
            reporte = new SISCERT_Reporte(global.cveplan, true, global.tipoConexion, null);
            reporte.duplicado(""+datosReturn.getStringData1(), true);
        } catch (SISCERT_Excepcion ex){
            if (ex == null || ex.getMensaje()==null)
                mensaje.General(this, "GENERAL", ""+ex, "");
            else if (ex.getMensaje().equals("SIN_QUE_REIMPRIMIR"))
                mensaje.prepararImpresion(this, "SIN_QUE_REIMPRIMIR", "", "");
            else
                mensaje.prepararImpresion(this, ex.getMensaje(), ex.getMensaje2(), ex.getMensaje3());
        } catch (Exception ex) { mensaje.General(this, "GENERAL", ex.getMessage(), ""); }
        finally { try { this.setCursor(Cursor.getDefaultCursor()); if (reporte!=null) reporte.cerrarConexion(); }catch(SQLException ex){} }
        return true;
    }
    
    private static WsfirmaDatosReturn firmarDuplicado(java.lang.String idscertificacion, java.lang.String cicescinilib, java.lang.String cveplan, java.lang.String usuario) {
        webservices.WSFirmarCertificacion_Service service = new webservices.WSFirmarCertificacion_Service();
        webservices.WSFirmarCertificacion port = service.getWSFirmarCertificacionPort();
        return port.firmarDuplicado(idscertificacion, cicescinilib, cveplan, usuario);
    }
    
    private boolean reimprimirFolios ()
    {
        SISCERT_Reporte reporte;
        int filas=modelFoliosImpresos.getRowCount();
        boolean hayFolios=false, puedeReimprimir=true;
        String idsfolimpre="";
        try {
            /******************* VERIFICAMOS SI PUEDE REIMPRIMIR ***********************************************/
            if (filas<=0)
                return mensaje.prepararImpresion(this, "SIN_QUE_REIMPRIMIR", "", "");
            for (int i=0; i<filas; i++)
            {
                if (modelFoliosImpresos.getValueAt(i, 0).equals(true)){
                    if (modelFoliosImpresos.getValueAt(i, 7).equals("CANCELADO"))
                        return mensaje.prepararImpresion(this, "NO_IMPRIMIR_CANCELADOS", ""+modelFoliosImpresos.getValueAt(i, 3), ""+modelFoliosImpresos.getValueAt(i, 1));
                    //if (Integer.parseInt(""+modelFoliosImpresos.getHiddenRowRef(i)[0])>=3)
                    //    return mensaje.prepararImpresion(this, "DIAS_CADUCOS", ""+modelFoliosImpresos.getValueAt(i, 3), ""+modelFoliosImpresos.getValueAt(i, 1));
                    hayFolios = true;
                    idsfolimpre += (i==0?"":", ")+modelFoliosImpresos.getValueAt(i, 2);
                }
            }
            if (!hayFolios)
                return mensaje.prepararImpresion(this, "SIN_QUE_REIMPRIMIR", "", "");
            /***************************************************************************************************/
            reporte = new SISCERT_Reporte(global.cveplan, true, global.tipoConexion, null);
            reporte.duplicado(""+idsfolimpre, true);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        }catch (Exception ex){ mensaje.General(this, "GENERAL", ""+ex, "");  puedeReimprimir=false; }
        finally { this.setCursor(Cursor.getDefaultCursor()); }                  //Cambiamos la forma del puntero a default 
        return puedeReimprimir;
    }
    
    private void rellenarTablaDeImpresion(SISCERT_ModeloDeTabla modelSISCERT, String printIni, String printFin)
    {
        modelFoliosAImprimir.removeAllItems();
        boolean seleccionar=false, formatoSeleccionado=false;
        String formatoATrabajar="";
        
        try {
            for (int i=0; i<modelSISCERT.getRowCount(); i++)
            {
                Object[] fila = new Object[modelFoliosAImprimir.getColumnCount()], filaOculta = new Object[modelFoliosAImprimir.getHiddenColumnCount()];
                if (!seleccionar && modelSISCERT.getValueAt(i, 0).toString().equals(printIni)) seleccionar = true;   //Verificamos el inicio de impresión del usuario
                //Preparamos la nueva tabla de impresión
                if (seleccionar){
                    if (!formatoSeleccionado) { formatoATrabajar=""+modelSISCERT.getValueAt(i, "formatoImp"); formatoSeleccionado=true; }//El formato que está en la primera selección, es la que tomará por default, por si hay varios formatos en la selección
                    
                    if (!formatoATrabajar.equals("FORM. ELEC JUL2017"))
                        throw new SISCERT_Excepcion (0,"FORMATOIMP_NO_ACEPTADO");
                    
                    if (formatoATrabajar.equals(""+modelSISCERT.getValueAt(i, "formatoImp")) && formatoATrabajar.equals("FORM. ELEC JUL2017")){ //Sólo agregaremos a la lista de impresión los que tienen el mismo formato que el primero seleccionado
                        if (modelSISCERT.getValueAt(i, "numSol").equals("")){
                            mensaje.prepararImpresion(this, "SIN_NUM_SOLICITUD", ""+modelSISCERT.getValueAt(i, 0), "");
                            break;
                        }
                        filaOculta[0] = ""+modelSISCERT.getHiddenValueAt(i, "idcertificacion");

                        fila[0] = true;
                        fila[1] = "";
                        fila[2] = modelSISCERT.getValueAt(i, "numCtrl");
                        fila[3] = modelSISCERT.getValueAt(i, "nombre").toString().trim()+" "+modelSISCERT.getValueAt(i, "primerApe").toString().trim()+" "+modelSISCERT.getValueAt(i, "segundoApe").toString().trim();
                        fila[4] = modelSISCERT.getValueAt(i, "curp");
                        fila[5] = modelSISCERT.getValueAt(i, "formatoImp");
                        cicescinilib = Integer.parseInt((""+modelSISCERT.getValueAt(i, "cicescinilib")).substring(0,4)); //Obtenemos el cicescinilib
                        modelFoliosAImprimir.addRow(fila, filaOculta);
                    }
                }
                if (modelSISCERT.getValueAt(i, 0).toString().equals(printFin)) break;                //Verificamos el fin de impresión del usuario
            }

            //Extraemos el idFormatoImpre
            for (String[] formatosCertActivo : formatosCertActivos)
                if (("" + formatosCertActivo[1]).equals(formatoATrabajar)) {
                    this.idFormatoImpre = Integer.parseInt("" + formatosCertActivo[0]);
                    break;
                }
        }catch (SISCERT_Excepcion ex) {
            if (ex.getMensaje().equals("FORMATOIMP_NO_ACEPTADO"))
                mensaje.prepararImpresion(this, "FORMATOIMP_NO_ACEPTADO", "", "");
        }
    }
    
    private void getFoliosImpresos ()
    {
        int casoBuscarPor=0;
        String texto1="", texto2="";
        try
        {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));     //Cambiamos la forma del puntero a reloj de arena
            conexion.conectar();
            modelFoliosImpresos.removeAllItems();
            if (rbnBuscarPorFolio.isSelected()){
                casoBuscarPor = 1;
                texto1=txtFolIniView.getText().trim().toUpperCase();
                texto2=txtFolFinView.getText().trim().toUpperCase();
            }else if (rbnBuscarPorFecha.isSelected()){
                casoBuscarPor = 2;
                texto1=txtFechaIniFol.getText().trim().toUpperCase();
                texto2=txtFechaFinFol.getText().trim().toUpperCase();
            }else if (rbnBuscarPorCurp.isSelected()){
                casoBuscarPor = 3;
                texto1=txtCurpFol.getText().trim().toUpperCase();
            }
            conexion.getFoliosImpresos(global.cveplan, modelFoliosImpresos,casoBuscarPor,texto1,texto2,""+cbxTipoFolFin.getSelectedItem(),this.global);
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }
    
    private void verificarPermisos()
    {
        try {
            String[] objetos = {"imprimirDuplicado", "imprimirPrueba", "modifTamHojaImpre"};
            boolean[] objPermisos;
            int i=0;
            
            conexion.conectar();
            objPermisos = conexion.getEstosPermisos("" + global.idcapturista, "prepararImpresion", objetos);
            
            for (String objeto : objetos){
                if (objeto.equals(objetos[0])){
                    canImprimirDuplicado=objPermisos[i];
                    btnFoliarFirmarEImprimir.setEnabled(canImprimirDuplicado);
                }else if (objeto.equals(objetos[1])){
                    canImprimirPrueba=objPermisos[i];
                }else if (objeto.equals(objetos[2])){
                    canModifTamHojaImpre=objPermisos[i];
                }
                i++;
            }
        } catch(SQLException ex) { mensaje.General(this,"CONEXION", ex.getMessage(), "");  } 
        catch(Exception ex) { mensaje.General(this,"GENERAL", ex.getMessage(), "");  } 
        finally { try { conexion.cerrarConexion(); } catch (SQLException ex) { } }
    }
    
    private void activarDesactivarOpcionesDeBusqueda (String opc)
    {
        boolean buscarPorFolio=false, buscarPorFecha=false, buscarPorCurp=false;
        
        if (opc.equals("FOLIO"))
            buscarPorFolio=true;
        else if (opc.equals("FECHA"))
            buscarPorFecha=true;
        else if (opc.equals("CURP"))
            buscarPorCurp=true;
        
        txtFolIniView.setEnabled(buscarPorFolio);
        cbxTipoFolFin.setEnabled(buscarPorFolio);
        txtFolFinView.setEnabled(false);
        txtFechaIniFol.setEnabled(buscarPorFecha);
        txtFechaFinFol.setEnabled(buscarPorFecha);
        txtCurpFol.setEnabled(buscarPorCurp);
        
        txtFolIniView.setText("");
        cbxTipoFolFin.setSelectedItem(" ");
        txtFolFinView.setText("");
        txtFechaIniFol.setText("");
        txtFechaFinFol.setText("");
        txtCurpFol.setText("");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rbngOpcBusqFolImpre = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFoliosAImprimir = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        btnSubir = new javax.swing.JButton();
        btnBajar = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        btnImpreTodos = new javax.swing.JButton();
        btnImpreNinguno = new javax.swing.JButton();
        btnFoliarFirmarEImprimir = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        txtFolIniView = new javax.swing.JTextField();
        txtFolFinView = new javax.swing.JTextField();
        btnBuscarFoliosImpresos = new javax.swing.JButton();
        cbxTipoFolFin = new javax.swing.JComboBox<>();
        rbnBuscarPorFolio = new javax.swing.JRadioButton();
        rbnBuscarPorFecha = new javax.swing.JRadioButton();
        txtFechaIniFol = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtFechaFinFol = new javax.swing.JTextField();
        rbnBuscarPorCurp = new javax.swing.JRadioButton();
        txtCurpFol = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        btnSelTodosFoliosImpre = new javax.swing.JButton();
        btnSelNingunoFolImpre = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblFoliosImpresos = new javax.swing.JTable();
        btnReimprimir = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        smnuImprimir = new javax.swing.JMenu();
        smnuFoliarFirmarEImprimir_Anverso = new javax.swing.JMenuItem();
        smnuReimprimir = new javax.swing.JMenuItem();
        mnuArchivo_Salir = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mnuEditar_SubirNivel = new javax.swing.JMenuItem();
        mnuEditar_BajarNivel = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuEditar_SelecTodos = new javax.swing.JMenuItem();
        mnuEditar_SelecNinguno = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Selección de folios a imprimir"));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Lista de impresión"));

        tblFoliosAImprimir.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Imprimir", "Folio", "No Ctrl", "Nombre", "CURP"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblFoliosAImprimir.getTableHeader().setReorderingAllowed(false);
        tblFoliosAImprimir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFoliosAImprimir_MouseClicked(evt);
            }
        });
        tblFoliosAImprimir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tblFoliosAImprimir_KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tblFoliosAImprimir);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Orden de los datos"));

        btnSubir.setText("Subir");
        btnSubir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubir_ActionPerformed(evt);
            }
        });

        btnBajar.setText("Bajar");
        btnBajar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBajar_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSubir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBajar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSubir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnBajar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Selección de impresión"));

        btnImpreTodos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/checked_all.png"))); // NOI18N
        btnImpreTodos.setText("Todos");
        btnImpreTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImpreTodos_ActionPerformed(evt);
            }
        });

        btnImpreNinguno.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/unchecked_all.png"))); // NOI18N
        btnImpreNinguno.setText("Ninguno");
        btnImpreNinguno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImpreNinguno_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnImpreTodos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnImpreNinguno, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnImpreTodos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnImpreNinguno)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        btnFoliarFirmarEImprimir.setText("Firmar e Imprimir");
        btnFoliarFirmarEImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFoliarFirmarEImprimir_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnFoliarFirmarEImprimir)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnFoliarFirmarEImprimir)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Folios a imprimir", jPanel1);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Búsqueda de folios"));

        txtFolFinView.setPreferredSize(new java.awt.Dimension(85, 20));

        btnBuscarFoliosImpresos.setText("Buscar");
        btnBuscarFoliosImpresos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarFoliosImpresos_ActionPerformed(evt);
            }
        });

        cbxTipoFolFin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "Al último folio registrado", "Al folio" }));
        cbxTipoFolFin.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxTipoFolFin_ItemStateChanged(evt);
            }
        });

        rbnBuscarPorFolio.setText("Folio inicial:");
        rbnBuscarPorFolio.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbnBuscarPorFolio_ItemStateChanged(evt);
            }
        });

        rbnBuscarPorFecha.setText("Fecha inicial:");
        rbnBuscarPorFecha.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbnBuscarPorFecha_ItemStateChanged(evt);
            }
        });

        txtFechaIniFol.setPreferredSize(new java.awt.Dimension(80, 20));

        jLabel1.setText("Fecha final:");

        txtFechaFinFol.setPreferredSize(new java.awt.Dimension(80, 20));

        rbnBuscarPorCurp.setText("CURP:");
        rbnBuscarPorCurp.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbnBuscarPorCurp_ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(rbnBuscarPorFolio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFolIniView, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxTipoFolFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(rbnBuscarPorFecha)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFechaIniFol, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFechaFinFol, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFolFinView, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnBuscarFoliosImpresos, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(rbnBuscarPorCurp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCurpFol, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtFolIniView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbxTipoFolFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFolFinView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rbnBuscarPorFolio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rbnBuscarPorFecha)
                            .addComponent(txtFechaIniFol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(txtFechaFinFol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnBuscarFoliosImpresos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbnBuscarPorCurp)
                    .addComponent(txtCurpFol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Selección de folios"));

        btnSelTodosFoliosImpre.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/checked_all.png"))); // NOI18N
        btnSelTodosFoliosImpre.setText("Todos");
        btnSelTodosFoliosImpre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelTodosFoliosImpre_ActionPerformed(evt);
            }
        });

        btnSelNingunoFolImpre.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/unchecked_all.png"))); // NOI18N
        btnSelNingunoFolImpre.setText("Ninguno");
        btnSelNingunoFolImpre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelNingunoFolImpre_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSelTodosFoliosImpre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSelNingunoFolImpre, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSelTodosFoliosImpre)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSelNingunoFolImpre)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Folios impresos"));

        tblFoliosImpresos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Imprimir", "N.P.", "idFolimpre", "Folio", "Nombre", "CURP", "Estatus", "Formato", "Fecha"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblFoliosImpresos.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(tblFoliosImpresos);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnReimprimir.setText("Reimprimir");
        btnReimprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReimprimir_ActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(255, 0, 51));
        jLabel2.setText("NOTA: Tiene 2 días como límite para poder reimprimir un(los) folio(s) después de haberlo(s) impreso por primera vez.");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnReimprimir))
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReimprimir)
                    .addComponent(jLabel2))
                .addGap(12, 12, 12))
        );

        jTabbedPane1.addTab("Folios impresos", jPanel2);

        jMenu1.setMnemonic('A');
        jMenu1.setText("Archivo");

        smnuImprimir.setText("Imprimir");

        smnuFoliarFirmarEImprimir_Anverso.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        smnuFoliarFirmarEImprimir_Anverso.setText("Firmar e imprimir");
        smnuFoliarFirmarEImprimir_Anverso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuFoliarFirmarEImprimir_Anverso_ActionPerformed(evt);
            }
        });
        smnuImprimir.add(smnuFoliarFirmarEImprimir_Anverso);

        smnuReimprimir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        smnuReimprimir.setText("Reimprimir");
        smnuReimprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smnuReimprimir_ActionPerformed(evt);
            }
        });
        smnuImprimir.add(smnuReimprimir);

        jMenu1.add(smnuImprimir);

        mnuArchivo_Salir.setText("Salir");
        mnuArchivo_Salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuArchivo_Salir_ActionPerformed(evt);
            }
        });
        jMenu1.add(mnuArchivo_Salir);

        jMenuBar1.add(jMenu1);

        jMenu2.setMnemonic('E');
        jMenu2.setText("Editar");

        mnuEditar_SubirNivel.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        mnuEditar_SubirNivel.setText("Subir un nivel");
        mnuEditar_SubirNivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEditar_SubirNivel_ActionPerformed(evt);
            }
        });
        jMenu2.add(mnuEditar_SubirNivel);

        mnuEditar_BajarNivel.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        mnuEditar_BajarNivel.setText("Bajar un nivel");
        mnuEditar_BajarNivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEditar_BajarNivel_ActionPerformed(evt);
            }
        });
        jMenu2.add(mnuEditar_BajarNivel);
        jMenu2.add(jSeparator1);

        mnuEditar_SelecTodos.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        mnuEditar_SelecTodos.setText("Seleccionar todos");
        mnuEditar_SelecTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEditar_SelecTodos_ActionPerformed(evt);
            }
        });
        jMenu2.add(mnuEditar_SelecTodos);

        mnuEditar_SelecNinguno.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        mnuEditar_SelecNinguno.setText("Seleccionar ninguno");
        mnuEditar_SelecNinguno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEditar_SelecNinguno_ActionPerformed(evt);
            }
        });
        jMenu2.add(mnuEditar_SelecNinguno);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void smnuFoliarFirmarEImprimir_Anverso_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuFoliarFirmarEImprimir_Anverso_ActionPerformed
        FoliarFirmarEImprimir();
    }//GEN-LAST:event_smnuFoliarFirmarEImprimir_Anverso_ActionPerformed

    private void smnuReimprimir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smnuReimprimir_ActionPerformed
        reimprimirFolios ();
    }//GEN-LAST:event_smnuReimprimir_ActionPerformed

    private void mnuArchivo_Salir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuArchivo_Salir_ActionPerformed
        this.dispose();
    }//GEN-LAST:event_mnuArchivo_Salir_ActionPerformed

    private void mnuEditar_SubirNivel_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuEditar_SubirNivel_ActionPerformed
        btnSubir_ActionPerformed(null);
    }//GEN-LAST:event_mnuEditar_SubirNivel_ActionPerformed

    private void mnuEditar_BajarNivel_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuEditar_BajarNivel_ActionPerformed
        btnBajar_ActionPerformed(null);
    }//GEN-LAST:event_mnuEditar_BajarNivel_ActionPerformed

    private void mnuEditar_SelecTodos_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuEditar_SelecTodos_ActionPerformed
        btnImpreTodos_ActionPerformed(null);
    }//GEN-LAST:event_mnuEditar_SelecTodos_ActionPerformed

    private void mnuEditar_SelecNinguno_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuEditar_SelecNinguno_ActionPerformed
        btnImpreNinguno_ActionPerformed(null);
    }//GEN-LAST:event_mnuEditar_SelecNinguno_ActionPerformed

    private void tblFoliosAImprimir_KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblFoliosAImprimir_KeyReleased
        boolean selImpre;    
        int posSel = tblFoliosAImprimir.getSelectedRow();
        selImpre = modelFoliosAImprimir.getValueAt(posSel, 0).equals(true);

        if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_SPACE)
            modelFoliosAImprimir.setValueAt(!selImpre, posSel, 0);
    }//GEN-LAST:event_tblFoliosAImprimir_KeyReleased

    private void tblFoliosAImprimir_MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFoliosAImprimir_MouseClicked
        boolean selImpre;
        int posSel = tblFoliosAImprimir.getSelectedRow();
        selImpre = modelFoliosAImprimir.getValueAt(posSel, 0).equals(true);
        if (modelFoliosAImprimir.getLastColEvent()==0)                          //Si dió click en la columna 0
            modelFoliosAImprimir.setValueAt(!selImpre, posSel, 0);
    }//GEN-LAST:event_tblFoliosAImprimir_MouseClicked

    private void btnSubir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubir_ActionPerformed
        int posSel;
        if ((posSel=tblFoliosAImprimir.getSelectedRow())!=-1){        
            if (posSel>=1)
                modelFoliosAImprimir.interchange(posSel, posSel-1);                 //Intercambiamos lugares
        }    
        if (posSel!=-1){
            if (posSel==0)
                tblFoliosAImprimir.changeSelection(0, 1, false, false);
            else
                tblFoliosAImprimir.changeSelection(posSel-1, 1, false, false);
        }
    }//GEN-LAST:event_btnSubir_ActionPerformed

    private void btnBajar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBajar_ActionPerformed
        int posSel;
        if ((posSel=tblFoliosAImprimir.getSelectedRow())!=-1){        
            if (posSel<modelFoliosAImprimir.getRowCount()-1)                                     
                modelFoliosAImprimir.interchange(posSel, posSel+1);                 //Intercambiamos lugares
        }
        if (posSel!=-1){
            if (posSel==modelFoliosAImprimir.getRowCount()-1)
                tblFoliosAImprimir.changeSelection(posSel, 1, false, false);
            else
                tblFoliosAImprimir.changeSelection(posSel+1, 1, false, false);
        }
    }//GEN-LAST:event_btnBajar_ActionPerformed

    private void btnImpreTodos_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImpreTodos_ActionPerformed
        for (int i=0; i<modelFoliosAImprimir.getRowCount(); i++)
            modelFoliosAImprimir.setValueAt(true, i, 0);
    }//GEN-LAST:event_btnImpreTodos_ActionPerformed

    private void btnImpreNinguno_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImpreNinguno_ActionPerformed
        for (int i=0; i<modelFoliosAImprimir.getRowCount(); i++)
            modelFoliosAImprimir.setValueAt(false, i, 0);
    }//GEN-LAST:event_btnImpreNinguno_ActionPerformed

    private void btnFoliarFirmarEImprimir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFoliarFirmarEImprimir_ActionPerformed
        FoliarFirmarEImprimir();
    }//GEN-LAST:event_btnFoliarFirmarEImprimir_ActionPerformed

    private void rbnBuscarPorFolio_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbnBuscarPorFolio_ItemStateChanged
        activarDesactivarOpcionesDeBusqueda ("FOLIO");
    }//GEN-LAST:event_rbnBuscarPorFolio_ItemStateChanged

    private void rbnBuscarPorFecha_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbnBuscarPorFecha_ItemStateChanged
        activarDesactivarOpcionesDeBusqueda ("FECHA");
    }//GEN-LAST:event_rbnBuscarPorFecha_ItemStateChanged

    private void rbnBuscarPorCurp_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbnBuscarPorCurp_ItemStateChanged
        activarDesactivarOpcionesDeBusqueda ("CURP");
    }//GEN-LAST:event_rbnBuscarPorCurp_ItemStateChanged

    private void cbxTipoFolFin_ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxTipoFolFin_ItemStateChanged
        txtFolFinView.setText("");
        if (cbxTipoFolFin.getSelectedItem().equals("Al folio"))
            txtFolFinView.setEnabled(true);
        else
            txtFolFinView.setEnabled(false);
    }//GEN-LAST:event_cbxTipoFolFin_ItemStateChanged

    private void btnBuscarFoliosImpresos_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarFoliosImpresos_ActionPerformed
        getFoliosImpresos ();
    }//GEN-LAST:event_btnBuscarFoliosImpresos_ActionPerformed

    private void btnReimprimir_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReimprimir_ActionPerformed
        reimprimirFolios ();
    }//GEN-LAST:event_btnReimprimir_ActionPerformed

    private void btnSelTodosFoliosImpre_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelTodosFoliosImpre_ActionPerformed
        for (int i=0; i<modelFoliosImpresos.getRowCount(); i++)
            modelFoliosImpresos.setValueAt(true, i, 0);
    }//GEN-LAST:event_btnSelTodosFoliosImpre_ActionPerformed

    private void btnSelNingunoFolImpre_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelNingunoFolImpre_ActionPerformed
        for (int i=0; i<modelFoliosImpresos.getRowCount(); i++)
            modelFoliosImpresos.setValueAt(false, i, 0);
    }//GEN-LAST:event_btnSelNingunoFolImpre_ActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBajar;
    private javax.swing.JButton btnBuscarFoliosImpresos;
    private javax.swing.JButton btnFoliarFirmarEImprimir;
    private javax.swing.JButton btnImpreNinguno;
    private javax.swing.JButton btnImpreTodos;
    private javax.swing.JButton btnReimprimir;
    private javax.swing.JButton btnSelNingunoFolImpre;
    private javax.swing.JButton btnSelTodosFoliosImpre;
    private javax.swing.JButton btnSubir;
    private javax.swing.JComboBox<String> cbxTipoFolFin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JMenuItem mnuArchivo_Salir;
    private javax.swing.JMenuItem mnuEditar_BajarNivel;
    private javax.swing.JMenuItem mnuEditar_SelecNinguno;
    private javax.swing.JMenuItem mnuEditar_SelecTodos;
    private javax.swing.JMenuItem mnuEditar_SubirNivel;
    private javax.swing.JRadioButton rbnBuscarPorCurp;
    private javax.swing.JRadioButton rbnBuscarPorFecha;
    private javax.swing.JRadioButton rbnBuscarPorFolio;
    private javax.swing.ButtonGroup rbngOpcBusqFolImpre;
    private javax.swing.JMenuItem smnuFoliarFirmarEImprimir_Anverso;
    private javax.swing.JMenu smnuImprimir;
    private javax.swing.JMenuItem smnuReimprimir;
    private javax.swing.JTable tblFoliosAImprimir;
    private javax.swing.JTable tblFoliosImpresos;
    private javax.swing.JTextField txtCurpFol;
    private javax.swing.JTextField txtFechaFinFol;
    private javax.swing.JTextField txtFechaIniFol;
    private javax.swing.JTextField txtFolFinView;
    private javax.swing.JTextField txtFolIniView;
    // End of variables declaration//GEN-END:variables

    
}
