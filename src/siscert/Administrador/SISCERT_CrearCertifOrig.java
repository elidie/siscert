package siscert.Administrador;

import java.awt.Cursor;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:24:13 PM
 */
public class SISCERT_CrearCertifOrig extends javax.swing.JDialog {

    private final SISCERT_Mensajes mensaje;
    private final SISCERT_GlobalMethods global;
    private final SISCERT_QueriesInformix conexion;
    private final SISCERT_ModeloDeTabla modelAlumnos, modelCicloDeEstudio, modelDatosCertificado; //tblFoliosAImprimir
    
    private final ArrayList<Integer> idsFormatosfolio = new ArrayList<>();
    private final ArrayList<String[]> idsCCTYCveturnos = new ArrayList<>();
    private boolean lockcbxCctsEscuela;
    
    public SISCERT_CrearCertifOrig(java.awt.Frame parent, boolean modal,SISCERT_Mensajes mensaje, SISCERT_GlobalMethods global, SISCERT_QueriesInformix conexion) {
        super(parent, modal);
        initComponents();
        this.global = global;
        this.mensaje = mensaje;
        this.conexion=conexion;
        //------------------------ CONFIGURAMOS LA TABLA DE MOSTRAR FOLIOS A CANCELAR ------------------------
        modelAlumnos = new SISCERT_ModeloDeTabla(new String[]{"idalu","CURP","Nombre","primerApe", "segundoApe"});
        tblAlumnos.setModel(modelAlumnos);
        modelAlumnos.setScrollHorizontal(tblAlumnos, crlpAlumnos);
        modelCicloDeEstudio = new SISCERT_ModeloDeTabla(new String[]{"cct","escuela","ciclo", "grado", "turno","prom", "est", "unidad"});
        tblCicloDeEstudio.setModel(modelCicloDeEstudio);
        modelCicloDeEstudio.setScrollHorizontal(tblCicloDeEstudio, crlpCicloDeEstudio);
        modelDatosCertificado = new SISCERT_ModeloDeTabla(new String[]{"cct","libro","flet","fnum", "curp","prom","usuario","cveunidad"});
        tblDatosCertificado.setModel(modelDatosCertificado);
        modelCicloDeEstudio.setScrollHorizontal(tblDatosCertificado, crlpDatosCertificado);
        cbxNombresEscuela.setEnabled(false);
        lockcbxCctsEscuela = false;
        cbxNivel.setSelectedItem(null);
        getCasosFolio ();
    }
    
    private void crearCertificado ()
    {
        int filaSel;
        String idcct, cveturno, idscct[]={"24071","23976","24072"}; //Primaria, Secundaria, Preescolar
                
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try
        {
            conexion.conectar();
            if (validarEntradas ())
            {
                filaSel = tblAlumnos.getSelectedRow();
                try { idcct=idsCCTYCveturnos.get(cbxCCTsEscuela.getSelectedIndex())[0]; }catch (IndexOutOfBoundsException ex) { idcct=idscct[cbxNivel.getSelectedIndex()]; }
                try { cveturno=idsCCTYCveturnos.get(cbxCCTsEscuela.getSelectedIndex())[1]; }catch (IndexOutOfBoundsException ex) { cveturno="100"; }

                /*conexion.crearCertificado (""+tblAlumnos.getValueAt(filaSel,0),idcct,""+tblAlumnos.getValueAt(filaSel,1), ""+tblAlumnos.getValueAt(filaSel,2),
                        ""+tblAlumnos.getValueAt(filaSel,3),""+tblAlumnos.getValueAt(filaSel,4), txtCiciniCEE.getText().trim(),txtCiciniCLF.getText().trim(),cveturno, ""+(cbxNivel.getSelectedIndex()+1),
                        txtPromedio.getText().trim(), this.global, getFolio());*/
                mensaje.CrearCertifOrig(this, "CERTIFICADO_CREADO", "", "");
            }
        }catch (SQLException ex){  mensaje.General(this,"CONEXION",ex.getMessage(),"");  }
        catch (Exception ex){
            if(ex.getMessage().contains("FOLIO_EN_FOLIOSIMPRE"))
                mensaje.CrearCertifOrig(this,"FOLIO_EN_FOLIOSIMPRE", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), ""); 
            else if (ex.getMessage().contains("ALU_EN_SICCEB"))
                mensaje.CrearCertifOrig(this,"ALU_EN_SICCEB", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
            else if (ex.getMessage().contains("GRADOREP_ESTATUS"))
                mensaje.ModuloCertificacion(this,"GRADOREP_ESTATUS", ex.getMessage().split("~")[1], ex.getMessage().split("~")[2]);
            else
                mensaje.General(this,"GENERAL", ex.getMessage(), ""); 
        }
        finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        }
    }
    
    private void getAlumnosSICEEB ()
    {
        if (!txtDatoABuscar.getText().equals(""))
        {
            try
            {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                conexion.conectar();
                modelAlumnos.removeAllItems();
                limpiarObjetos ();
                conexion.getAlumnosSICEEB (txtDatoABuscar.getText().trim(), ""+cbxCasoBusqueda.getSelectedItem(), modelAlumnos);
            }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
            catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
            finally {
                try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
            }
        } else
            mensaje.CrearCertifOrig(this, "DATO_A_BUSCAR", "", "");
    }
    
    private void limpiarObjetos ()
    {
        modelCicloDeEstudio.removeAllItems();
        modelDatosCertificado.removeAllItems();
        cbxCCTsEscuela.removeAllItems();
        cbxNombresEscuela.removeAllItems();
        txtCiciniCEE.setText("");
        txtCiciniCLF.setText("");
        lblCicfinCEE.setText("");
        lblCicfinCLF.setText("");
        chkEsRegularizado.setSelected(false);
        cbxNivel.setSelectedItem(null);
        cbxFormatosFolio.setSelectedItem(null);
        txtFolio.setText("");
        txtPromedio.setText("");
    }
    
    private void getCCTsConSusNombres ()
    {
        String texto="";
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try
        {
            texto = JOptionPane.showInputDialog(this,"Introdusca todo o parte del texto a buscar.", "Búsqueda parcial", JOptionPane.QUESTION_MESSAGE);
            if (texto!=null){
                conexion.conectar();
                cbxNombresEscuela.removeAllItems();
                cbxCCTsEscuela.removeAllItems();
                idsCCTYCveturnos.clear();
                lockcbxCctsEscuela = true;
                conexion.getCCTEscuela (texto.trim(), cbxCCTsEscuela, cbxNombresEscuela, idsCCTYCveturnos);
                lockcbxCctsEscuela = false;
            }
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
        this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
    }
    
    private void getCasosFolio ()
    {
        try {       //---------- Guardamos datos del alumno
            conexion.conectar ();            
            conexion.getFormatosfolio(idsFormatosfolio,cbxFormatosFolio);
            cbxFormatosFolio.setSelectedIndex(0);
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
    }
    
    private boolean validarEntradas() throws Exception
    {
        String msgFol, modalidad="", mensajeEdad="", cct, idcct;
        String idscct[]={"24071","23976","24072"}; //Primaria, Secundaria, Preescolar
        
        if( modelAlumnos.getRowCount()<=0 )
            return mensaje.CrearCertifOrig(this,"BUSCAR_ALUMNO","","");
        if ( tblAlumnos.getSelectedRow()== -1)
            return mensaje.CrearCertifOrig(this,"SEL_ALUMNO","","");
        if ( cbxNivel.getSelectedIndex()== -1)
            return mensaje.General(this,"CAMPO_VACIO","'NIVEL'","");
        if ( cbxCCTsEscuela.getSelectedIndex()== -1)
            switch (JOptionPane.showConfirmDialog(this, "No ha especificado una CCT, ¿Desea que el sistema le asigne una default?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
            {
                case JOptionPane.YES_OPTION: break;
                case JOptionPane.NO_OPTION: return false;
            }
        if ( txtCiciniCEE.getText().trim().equals(""))
            return mensaje.General(this,"CAMPO_VACIO","'CEE'","");
        if ( txtCiciniCLF.getText().trim().equals(""))
            return mensaje.General(this,"CAMPO_VACIO","'CLF'","");
        //if (Integer.parseInt(txtCiciniCEE.getText())>=1991)
        //    return mensaje.CrearCertifOrig(this,"CICLO_PERMITIDO","","");
       
        //***************************** VERIFICAMOS LA NORMATIVIDAD DE EDADES **************************
        if (cbxCCTsEscuela.getSelectedIndex()==-1){
            cct="00";
            idcct=idscct[cbxNivel.getSelectedIndex()];
            modalidad=cbxNivel.getSelectedIndex()==0?"DPR":(cbxNivel.getSelectedIndex()==1?"DES":"DJN");
        }else{
            cct = ""+cbxCCTsEscuela.getSelectedItem();
            idcct=idsCCTYCveturnos.get(cbxCCTsEscuela.getSelectedIndex())[0];
            if (cct.substring(0, 10).matches("[0-3][0-9][A-Za-z]{3}[0-9]{4}[A-Za-z]") && (cct.substring(0,2).equals("20") || cct.substring(0,2).equals("00")))
                modalidad = (""+cbxCCTsEscuela.getSelectedItem()).substring(2,5);   //Registramos las modalidades del estado de Oaxaca
        }
        if (Integer.parseInt(txtCiciniCLF.getText())<Integer.parseInt(txtCiciniCEE.getText()) && !(modalidad.equals("DML") || modalidad.equals("DBA") || modalidad.equals("HMC") || modalidad.equals("HSL")))
            return mensaje.CrearCertifOrig(this,"CICLO_CLF<CEE","","");
        if (!modalidad.equals(""))                                              //Revisamos únicamente los que son del estado
        {
            try {
                if (!"".equals(mensajeEdad=global.cumpleConLaEdad((""+tblAlumnos.getValueAt(tblAlumnos.getSelectedRow(), 1)).substring(4,10), txtCiciniCEE.getText(), cbxNivel.getSelectedIndex()==0?"1":(cbxNivel.getSelectedIndex()==1?"2":"3"), cbxNivel.getSelectedIndex()==0?"6":"3", idcct, modalidad, cct.substring(0,2), conexion)))
                    return mensaje.CrearCertifOrig(this,"NO_CUMPLE_CON_EDAD",mensajeEdad,"");
            }catch (Exception ex){
                if (ex==null || ex.getMessage()==null)
                    mensaje.General(this,"GENERAL", ""+ex, "");
                if (ex.getMessage().equals("FECHA_NAC"))
                    return mensaje.CrearCertifOrig(this,"FECHA_NAC","",""); 
                else if (ex.getMessage().equals("EDAD_CVEPROGRAMA"))
                    return mensaje.CrearCertifOrig(this, "EDAD_CVEPROGRAMA", "", "");
                else if (ex.getMessage().contains("NORMATIVIDAD_INDEF"))
                    return mensaje.ModuloCertificacion(this, "NORMATIVIDAD_INDEF", ex.getMessage().substring(ex.getMessage().indexOf("*")+1), "");
            }
        }
        //***************************** VERIFICAMOS EL FORMATO DEL FOLIO **************************
        if ( cbxFormatosFolio.getSelectedIndex()== -1 || cbxFormatosFolio.getSelectedIndex() == 0)
            return mensaje.CrearCertifOrig(this,"FORMATO_FOLIO","","");
        if ( txtFolio.getText().trim().equals(""))
            return mensaje.General(this,"CAMPO_VACIO","'FOLIO'","");
        if (!"".equals(msgFol=global.validarFormatoFolio(txtFolio.getText().toString().trim(), idsFormatosfolio.get(cbxFormatosFolio.getSelectedIndex()),""+(Integer.parseInt(txtCiciniCEE.getText())+1))))
        {
            if (msgFol.equals("LET_FOL_Y_CICFIN")){
                if (!chkEsRegularizado.isSelected())
                    return mensaje.Secundaria("LET_FOL_Y_CICFIN", "", "");
            }else
                return mensaje.Primaria (msgFol,"","");
        }
        //*****************************************************************************************
        if ( txtPromedio.getText().trim().equals("") && !cbxNivel.getSelectedItem().equals("PREESCOLAR"))
            return mensaje.General(this,"CAMPO_VACIO","'PROMEDIO'","");
        else if (cbxNivel.getSelectedItem().equals("PREESCOLAR"))
            txtPromedio.setText("10.0");
        
        return true;
    }
    
    //Obtenemos el folio ya particionado
    private String [] getFolio ()
    {
        String idFormatFol_folLet_folNum[] = {"","",""};
        int idfol = idsFormatosfolio.get(cbxFormatosFolio.getSelectedIndex());
        if (idfol==2 || idfol==4){ //(CICLO, ENTIDAD Y 7 NÚMEROS), (CICLO, ENTIDAD Y NÚMEROS)
            idFormatFol_folLet_folNum[0]=""+idfol;
            idFormatFol_folLet_folNum[1]="_";
            idFormatFol_folLet_folNum[2]=txtFolio.getText().trim().substring(3);
        }else if (idfol==3 || idfol==5 ){ //(LETRA Y 7 NÚMEROS), (LETRA Y NÚMEROS)
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
        }
        return idFormatFol_folLet_folNum;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cbxCasoBusqueda = new javax.swing.JComboBox<String>();
        txtDatoABuscar = new javax.swing.JTextField();
        btnBuscarCurp = new javax.swing.JButton();
        crlpAlumnos = new javax.swing.JScrollPane();
        tblAlumnos = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        crlpCicloDeEstudio = new javax.swing.JScrollPane();
        tblCicloDeEstudio = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        crlpDatosCertificado = new javax.swing.JScrollPane();
        tblDatosCertificado = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbxNivel = new javax.swing.JComboBox<String>();
        cbxCCTsEscuela = new javax.swing.JComboBox<String>();
        cbxBuscarCCT = new javax.swing.JButton();
        cbxNombresEscuela = new javax.swing.JComboBox<String>();
        txtCiciniCEE = new javax.swing.JTextField();
        txtCiciniCLF = new javax.swing.JTextField();
        chkEsRegularizado = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cbxFormatosFolio = new javax.swing.JComboBox<String>();
        txtFolio = new javax.swing.JTextField();
        txtPromedio = new javax.swing.JTextField();
        lblCicfinCEE = new javax.swing.JLabel();
        lblCicfinCLF = new javax.swing.JLabel();
        btnCrearCertificado = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Área de búsqueda"));

        cbxCasoBusqueda.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "curp", "idalu" }));

        txtDatoABuscar.setPreferredSize(new java.awt.Dimension(123, 20));

        btnBuscarCurp.setText("Buscar");

        tblAlumnos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        crlpAlumnos.setViewportView(tblAlumnos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(crlpAlumnos, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbxCasoBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDatoABuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBuscarCurp)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxCasoBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDatoABuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarCurp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(crlpAlumnos, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Ciclo en que estudia"));

        tblCicloDeEstudio.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        crlpCicloDeEstudio.setViewportView(tblCicloDeEstudio);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(crlpCicloDeEstudio, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(crlpCicloDeEstudio, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de certificado"));

        tblDatosCertificado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        crlpDatosCertificado.setViewportView(tblDatosCertificado);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(crlpDatosCertificado, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(crlpDatosCertificado, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos para el certificado"));

        jLabel1.setText("Nivel:");

        jLabel2.setText("CCT:");

        jLabel3.setText("Escuela:");

        jLabel4.setText("Ciclo donde estudió (CEE):");

        jLabel5.setText("Ciclo de su folio (CLF):");

        cbxNivel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PRIMARIA", "SECUNDARIA", "PREESCOLAR" }));

        cbxBuscarCCT.setText("Buscar...");

        txtCiciniCEE.setPreferredSize(new java.awt.Dimension(49, 20));

        txtCiciniCLF.setPreferredSize(new java.awt.Dimension(49, 20));

        chkEsRegularizado.setText("Es regularizado");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel6.setText("Formato del folio:");

        jLabel7.setText("Folio:");

        jLabel8.setText("Promedio:");

        lblCicfinCEE.setText("lblCicfinCEE");

        lblCicfinCLF.setText("lblCicfinCLF");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkEsRegularizado)
                            .addComponent(cbxNivel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(cbxCCTsEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxBuscarCCT))
                            .addComponent(cbxNombresEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(txtCiciniCEE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblCicfinCEE, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCiciniCLF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCicfinCLF, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(28, 28, 28)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxFormatosFolio, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPromedio, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFolio, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 144, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(cbxNivel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(cbxCCTsEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbxBuscarCCT))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(cbxNombresEscuela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(txtCiciniCEE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblCicfinCEE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtCiciniCLF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5)
                                    .addComponent(lblCicfinCLF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                                .addComponent(chkEsRegularizado))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6)
                                    .addComponent(cbxFormatosFolio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtFolio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtPromedio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8)))))
                    .addComponent(jSeparator1))
                .addContainerGap())
        );

        btnCrearCertificado.setText("Crear certificado");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCrearCertificado)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCrearCertificado)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarCurp;
    private javax.swing.JButton btnCrearCertificado;
    private javax.swing.JButton cbxBuscarCCT;
    private javax.swing.JComboBox<String> cbxCCTsEscuela;
    private javax.swing.JComboBox<String> cbxCasoBusqueda;
    private javax.swing.JComboBox<String> cbxFormatosFolio;
    private javax.swing.JComboBox<String> cbxNivel;
    private javax.swing.JComboBox<String> cbxNombresEscuela;
    private javax.swing.JCheckBox chkEsRegularizado;
    private javax.swing.JScrollPane crlpAlumnos;
    private javax.swing.JScrollPane crlpCicloDeEstudio;
    private javax.swing.JScrollPane crlpDatosCertificado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblCicfinCEE;
    private javax.swing.JLabel lblCicfinCLF;
    private javax.swing.JTable tblAlumnos;
    private javax.swing.JTable tblCicloDeEstudio;
    private javax.swing.JTable tblDatosCertificado;
    private javax.swing.JTextField txtCiciniCEE;
    private javax.swing.JTextField txtCiciniCLF;
    private javax.swing.JTextField txtDatoABuscar;
    private javax.swing.JTextField txtFolio;
    private javax.swing.JTextField txtPromedio;
    // End of variables declaration//GEN-END:variables

}
