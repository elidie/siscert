package siscert.Administrador;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:22:59 PM
 */
public class SISCERT_AgregarEscuelaHistorica extends javax.swing.JDialog {

    private final SISCERT_Mensajes mensaje;
    private final SISCERT_GlobalMethods global;
    private final SISCERT_QueriesInformix conexion;
    
    private final SISCERT_ModeloDeTabla modelCCTsHistoricas;
    
    /** Creates new form SISCERT_RelacionarIdalu */
    public SISCERT_AgregarEscuelaHistorica(java.awt.Frame parent, boolean modal, SISCERT_Mensajes mensaje, SISCERT_GlobalMethods global, SISCERT_QueriesInformix conexion) {
        super(parent, modal);
        initComponents();
        this.global = global;
        this.mensaje = mensaje;
        this.conexion=conexion;
        
        //------------------------ CONFIGURAMOS LA TABLA DE MOSTRAR FOLIOS A CANCELAR ------------------------
        modelCCTsHistoricas = new SISCERT_ModeloDeTabla(new String[]{"Nombre","fecha alta"});
        tblCCTsHistoricas.setModel(modelCCTsHistoricas);
        modelCCTsHistoricas.setAnchoDeColumnas(tblCCTsHistoricas, new int[]{100,50});
        modelCCTsHistoricas.setScrollHorizontal(tblCCTsHistoricas, scrlCCTsHistoricas);
        
        btnAgregarNombreHistorico.setEnabled(false);
    }
    
    private boolean agregarNombreHistorico ()
    {
        boolean ok=false;
        String nuevoNombreHistorico="", fechaInsert;
        
        nuevoNombreHistorico = mensaje.inputBox(this, "Nuevo nombre histórico", "Introdusca el nombre histórico", "");
        if (nuevoNombreHistorico==null || nuevoNombreHistorico.equals(""))
            return false;
        if (txtCCT.getText().trim().length()!=10)
            return mensaje.AgregarEscuelaHistorica(this, "TAMAÑO_CCT", "", "");
        if (!txtCCT.getText().trim().toUpperCase().matches("[0-9]{2}[A-Za-z|ñ|Ñ]{3}[0-9]{4}[A-Za-z|ñ|Ñ]{1}"))
            return mensaje.AgregarEscuelaHistorica(this, "CCT_INVALIDA", "", "");
        if (modelCCTsHistoricas.indexOf(nuevoNombreHistorico.trim().toUpperCase(), 0)!=-1)
            return mensaje.AgregarEscuelaHistorica(this, "NOMBRE_EXISTENTE", nuevoNombreHistorico, "");
        try
        {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            conexion.conectar();
            fechaInsert=conexion.agregarEscuelaHistorica (txtCCT.getText().trim().toUpperCase(), nuevoNombreHistorico.trim().toUpperCase());
            modelCCTsHistoricas.addRow(new Object[]{nuevoNombreHistorico.trim().toUpperCase(),fechaInsert});
            mensaje.AgregarEscuelaHistorica(this, "ESCUELA_HISTORICA_AGREGADA", "", "");
            ok=true;
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        }
        return ok;
    }
    
    private boolean buscarCCT ()
    {
        boolean ok=false;
        
        if (txtCCT.getText().trim().length()!=10)
            return mensaje.AgregarEscuelaHistorica(this, "TAMAÑO_CCT", "", "");
        if (!txtCCT.getText().trim().toUpperCase().matches("[0-9]{2}[A-Za-z|ñ|Ñ]{3}[0-9]{4}[A-Za-z|ñ|Ñ]{1}"))
            return mensaje.AgregarEscuelaHistorica(this, "CCT_INVALIDA", "", "");
        
        try
        {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            conexion.conectar();
            modelCCTsHistoricas.removeAllItems();
            txtNombreActual.setText("");
            conexion.buscarCCTHistoricas (txtCCT.getText().trim().toUpperCase(), txtNombreActual, modelCCTsHistoricas);
            btnAgregarNombreHistorico.setEnabled(!txtNombreActual.getText().equals(""));
            ok=true;
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        }
        return ok;
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
        jLabel1 = new javax.swing.JLabel();
        txtCCT = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtNombreActual = new javax.swing.JTextField();
        scrlCCTsHistoricas = new javax.swing.JScrollPane();
        tblCCTsHistoricas = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        btnAgregarNombreHistorico = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Área de búsqueda"));

        jLabel1.setText("CCT:");

        txtCCT.setPreferredSize(new java.awt.Dimension(128, 20));
        txtCCT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCCT_KeyPressed(evt);
            }
        });

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscar_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCCT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnBuscar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCCT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos para la escuela"));

        jLabel2.setText("Nombre actual:");

        txtNombreActual.setEditable(false);
        txtNombreActual.setPreferredSize(new java.awt.Dimension(292, 20));

        tblCCTsHistoricas.setModel(new javax.swing.table.DefaultTableModel(
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
        scrlCCTsHistoricas.setViewportView(tblCCTsHistoricas);

        jLabel3.setText("Nombres históricos:");

        btnAgregarNombreHistorico.setText("Agregar nombre histórico");
        btnAgregarNombreHistorico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarNombreHistorico_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNombreActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3)
                    .addComponent(scrlCCTsHistoricas, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAgregarNombreHistorico)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNombreActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrlCCTsHistoricas, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAgregarNombreHistorico)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtCCT_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCCT_KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER){
            buscarCCT ();
        }
    }//GEN-LAST:event_txtCCT_KeyPressed

    private void btnBuscar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscar_ActionPerformed
        buscarCCT ();
    }//GEN-LAST:event_btnBuscar_ActionPerformed

    private void btnAgregarNombreHistorico_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarNombreHistorico_ActionPerformed
        agregarNombreHistorico ();
    }//GEN-LAST:event_btnAgregarNombreHistorico_ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarNombreHistorico;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane scrlCCTsHistoricas;
    private javax.swing.JTable tblCCTsHistoricas;
    private javax.swing.JTextField txtCCT;
    private javax.swing.JTextField txtNombreActual;
    // End of variables declaration//GEN-END:variables

}
