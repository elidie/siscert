package siscert.Inicio;

import java.awt.Cursor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;

/* Notas para agregar una nueva región.
 * 
 * 1.- Agregar la clave de la región en el arreglo correspondiente en el método SISCERTLoginUser.verificar_cveunidad()
 * 2.- Hacer un insert en la tabla para controlar las versiones con la clave de unidad: INSERT INTO siscert_sisver VALUES (10,'DSRISN','',0,0,0,0,'',0,0)
 * 3.- Hacer un insert en la tabla de variables: INSERT INTO siscert_variables (cveunidad) VALUES ('DSRISN')
 * 4.- Insertar las coordenadas de impresion:
 *                   INSERT INTO siscert_coordenadas (cveunidad, objeto, desc, x1, y1, x2, y2, x3, y3, formatoimpre)
 *                   SELECT 'DSRISN', objeto, desc, x1, y1, x2, y2, x3, y3, formatoimpre from siscert_coordenadas where cveunidad='DSRIST' AND formatoimpre='2010-2011'
 */

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:19:02 PM
 */
public class SISCERT_LoginUser extends javax.swing.JDialog {

    private JFrame frmVentanaPrincipal;
    private final java.awt.Frame frmParent;
    private JDialog dlgDescargarNuevaVersion;
    
    private SISCERT_GlobalMethods global;
    private final SISCERT_Mensajes mensaje;
    private final SISCERT_QueriesInformix conexion;

    private final String nombreSistema, tituloSistema, versionSistema;
    private final int tipoVersion;
    
    /** Creates new form SISCERT_LoginUser */
    public SISCERT_LoginUser(java.awt.Frame parent, boolean modal, String nombreSistema, String tituloSistema, String version, int tipoVersion, SISCERT_Mensajes mensaje, SISCERT_QueriesInformix conexion) {
        super(parent, modal);
        initComponents();
        
        this.frmParent = parent;
        this.nombreSistema = nombreSistema;
        this.tituloSistema = tituloSistema;
        this.versionSistema = version;
        this.tipoVersion = tipoVersion;
        this.mensaje = mensaje;
        this.conexion = conexion;
        rbnInternet.setSelected(true);
        //***************** Activamos las variables que se usarán en el sistema
        pnlTipoConexion.setVisible(true);
        
        rbtnGroup.add(rbnLocal);
        rbtnGroup.add(rbnInternet);
        //rbtnGroup.add(rbnODBC);
        //rbnODBC.setVisible(false);
    }
    
    public void ventanaPrincipal() {
        try {
            if (frmVentanaPrincipal == null) {                                      //si aún no se ha creado el formulario
                frmVentanaPrincipal = new SISCERT_VentanaPrincipal(this.global,this.mensaje,this.conexion);                //creamos un formulario
                frmVentanaPrincipal.setLocationRelativeTo(null);                    //le damos una localización en la pantalla
                frmVentanaPrincipal.setTitle(tituloSistema);
                frmVentanaPrincipal.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
            }
            frmVentanaPrincipal.setVisible(true);                  //mostramos la ventana*/
            frmVentanaPrincipal.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } catch (Exception ex){ mensaje.General(this,"GENERAL", ""+ex.getMessage(), "");
                if (frmVentanaPrincipal == null)  System.exit(0);
        }
    }
    
    public void descargarNuevaVersion (String versionAdmin, String caso, String defaultTipoConexion) {
        
        dlgDescargarNuevaVersion = new SISCERT_DescargarNuevaVersion(this.frmParent, true, this.nombreSistema, versionAdmin, caso, defaultTipoConexion); //creamos el objeto
        dlgDescargarNuevaVersion.setLocationRelativeTo(null);                        //le damos una localización en la pantalla
        dlgDescargarNuevaVersion.setTitle(nombreSistema+" v"+versionSistema+". Aviso de versión de sistema");  //Mostramos texto en la barra de título de la ventana
        dlgDescargarNuevaVersion.setResizable(false);
        //dlgSolicitudDeServicios.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(vars.urlIconoSistema));
        dlgDescargarNuevaVersion.setVisible(true);                  //mostramos la ventana*/
    }
    
    public void btnAceptar()
    {
        boolean ok=false;
        String region;
        if (validarCampos ())
        {
            if (rbnLocal.isSelected())
                conexion.setTipoConexionAsLOCAL();
            else if (rbnInternet.isSelected())
                conexion.setTipoConexionAsINTERNET();
            //else if (rbnODBC.isSelected())
            //    conexion.setDefaultTipoConexion(SISCERT_ConexionInformix.CONEXION_ODBC);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            try {
                global = new SISCERT_GlobalMethods (conexion.getDefaultTipoConexion(),versionSistema);
                conexion.conectar ();
                char[] pswC = pswContrasenia.getPassword(); 
                String pass = new String(pswC); 
                //conexion.indentificarUsuario(txtUsuario.getText().toUpperCase(),  String(pswContraseña.getText().toUpperCase(), this.global);
                conexion.indentificarUsuario(txtUsuario.getText().toUpperCase(),  pass.toUpperCase(), this.global);
                if (global.cveunidad.equals(""))   
                    mensaje.loginUser("USR_PASS_INCORRECTO", "", "");
                else {
                    region = global.cveunidad;
                    if (txtUsuario.getText().toUpperCase().equals("ELYLOPEZ"))
                        region="ADMIN";
                    administrarVersion (conexion, region);
                    this.dispose ();
                    conexion.cerrarConexion();
                    ventanaPrincipal();
                }
                ok = true;
            } catch(UnsupportedEncodingException ex) {  mensaje.loginUser("CODIFICAR_PASS", "", ""); } //por si hubo un error al codificar la contraseña 
            catch (SQLException ex){ mensaje.General(this, "CONEXION",ex.getMessage(),""); }
            catch (Exception ex){ 
                if (ex.getMessage().contains("SIN_PERMISO")){ 
                    ok = true;
                    mensaje.loginUser("SIN_PERMISO", "", "");
                }else
                    mensaje.General(this, "GENERAL", ex.getMessage(), ""); 
            } finally {
                try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
                if (!ok) System.exit(0);
            }
        }
    }
    
    public void btnCancelar() {
        System.exit(0);
    }
    
    private boolean validarCampos ()
    {
        char[] pswC = pswContrasenia.getPassword(); 
        String pass = new String(pswC); 
                
        if (!rbnInternet.isSelected() && !rbnLocal.isSelected()/* && !rbnODBC.isSelected()*/)
            return mensaje.loginUser("SIN_TIPO_ACCESO", "", "");
        if (txtUsuario.getText().equals(""))
            return mensaje.loginUser("SIN_USUARIO", "", "");
        //else if (pswContraseña.getText().equals(""))
        else if (pass.equals(""))
            return mensaje.loginUser("SIN_PASS", "", "");
        return true;
    }

    private void administrarVersion (SISCERT_QueriesInformix conexion, String usuario)throws SQLException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, Exception
    {
        String [] info = new String [5];
        String caso;
        caso = conexion.verifVersion (info, usuario, this.versionSistema, this.tipoVersion);
        if (!"".equals(caso))
        {
            if (!info[1].trim().equals(""))   //Si hay un mensaje en la BD para mostrar, lo mostramos
                {  mensaje.loginUser("AVISO", info[1], ""); }

            if (caso.equals("SIS_LOCK"))
            {                                                                   //El sistema se debe bloquear
                mensaje.loginUser("SIS_LOCK", "", "");
                System.exit(0);
            }else
                descargarNuevaVersion (info[0],caso, conexion.getDefaultTipoConexion());
        }
        //checarVersionSiCompleta ();
    }
    
    private void checarVersionSiCompleta () throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        //String value = WinRegistry.readString (WinRegistry.HKEY_LOCAL_MACHINE,"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion","ProductName");//HKEY, Key, ValueName
        //System.out.println("Windows Distribution = " + value);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rbtnGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        pswContrasenia = new javax.swing.JPasswordField();
        pnlTipoConexion = new javax.swing.JPanel();
        rbnLocal = new javax.swing.JRadioButton();
        rbnInternet = new javax.swing.JRadioButton();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Identificación de usuario"));

        jLabel1.setText("Usuario:");

        txtUsuario.setPreferredSize(new java.awt.Dimension(197, 20));
        txtUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtUsuario_KeyTyped(evt);
            }
        });

        jLabel2.setText("Contraseña:");

        pswContrasenia.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                pswContraseniaKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pswContrasenia))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(pswContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pnlTipoConexion.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipo de conexión"));

        rbnLocal.setText("Local");

        rbnInternet.setText("Internet");

        javax.swing.GroupLayout pnlTipoConexionLayout = new javax.swing.GroupLayout(pnlTipoConexion);
        pnlTipoConexion.setLayout(pnlTipoConexionLayout);
        pnlTipoConexionLayout.setHorizontalGroup(
            pnlTipoConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTipoConexionLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(rbnInternet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(rbnLocal)
                .addGap(38, 38, 38))
        );
        pnlTipoConexionLayout.setVerticalGroup(
            pnlTipoConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTipoConexionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTipoConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbnLocal)
                    .addComponent(rbnInternet))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAceptar.setText("Aceptar");
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptar_ActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelar_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(btnAceptar)
                        .addGap(37, 37, 37)
                        .addComponent(btnCancelar))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlTipoConexion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlTipoConexion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAceptar)
                    .addComponent(btnCancelar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtUsuario_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtUsuario_KeyTyped
        if (evt.getKeyChar() == '\n')
            btnAceptar();
    }//GEN-LAST:event_txtUsuario_KeyTyped

    private void btnAceptar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptar_ActionPerformed
        btnAceptar();
    }//GEN-LAST:event_btnAceptar_ActionPerformed

    private void btnCancelar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelar_ActionPerformed
        btnCancelar();
    }//GEN-LAST:event_btnCancelar_ActionPerformed

    private void pswContraseniaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pswContraseniaKeyTyped
         if (evt.getKeyChar() == '\n')
            btnAceptar();
    }//GEN-LAST:event_pswContraseniaKeyTyped



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pnlTipoConexion;
    private javax.swing.JPasswordField pswContrasenia;
    private javax.swing.JRadioButton rbnInternet;
    private javax.swing.JRadioButton rbnLocal;
    private javax.swing.ButtonGroup rbtnGroup;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables

}
