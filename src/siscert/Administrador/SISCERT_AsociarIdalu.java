package siscert.Administrador;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:23:11 PM
 */
public class SISCERT_AsociarIdalu extends javax.swing.JDialog {

    private SISCERT_Mensajes mensaje;
    private SISCERT_GlobalMethods global;
    private SISCERT_QueriesInformix conexion;
    private SISCERT_ModeloDeTabla modelAlumnosSISCERT, modelAlumnosSICEEB, modelCicloDeEstudioSICEEB, modelDatosCertificadoSICEEB;
            
    /** Creates new form SISCERT_CrearEscuelaHistorica */
    public SISCERT_AsociarIdalu(java.awt.Frame parent, boolean modal, SISCERT_Mensajes mensaje, SISCERT_GlobalMethods global, SISCERT_QueriesInformix conexion) {
        super(parent, modal);
        initComponents();
        this.global = global;
        this.mensaje = mensaje;
        this.conexion=conexion;
        //------------------------ CONFIGURAMOS LA TABLA DE MOSTRAR FOLIOS A CANCELAR ------------------------
        modelAlumnosSISCERT = new SISCERT_ModeloDeTabla(new String[]{"elección","idalu","nivel", "cct","estudió","libro","folio", "CURP", "prom","nombre","primerApe", "segundoApe", "usuario"}, new String[]{"idcertificacion"});
        tblAlumnosSISCERT.setModel(modelAlumnosSISCERT);
        modelAlumnosSISCERT.setScrollHorizontal(tblAlumnosSISCERT, scrlAlumnosSISCERT);
        
        //....................................................................................................
        modelAlumnosSICEEB = new SISCERT_ModeloDeTabla(new String[]{"elección","idalu","CURP","nombre","primerApe", "segundoApe"});
        tblAlumnosSICEEB.setModel(modelAlumnosSICEEB);
        modelAlumnosSICEEB.setScrollHorizontal(tblAlumnosSICEEB, scrlAlumnosSICEEB);
        
        modelCicloDeEstudioSICEEB = new SISCERT_ModeloDeTabla(new String[]{"cct","escuela","ciclo", "grado", "turno","prom", "estatusgrado"});
        tblCicloDeEstudioSICEEB.setModel(modelCicloDeEstudioSICEEB);
        modelCicloDeEstudioSICEEB.setScrollHorizontal(tblCicloDeEstudioSICEEB, scrlCicloDeEstudioSICEEB);
        
        modelDatosCertificadoSICEEB = new SISCERT_ModeloDeTabla(new String[]{"cct","libro","folio", "prom","usuario"});
        tblDatosCertificadoSICEEB.setModel(modelDatosCertificadoSICEEB);
        modelDatosCertificadoSICEEB.setScrollHorizontal(tblDatosCertificadoSICEEB, scrlDatosCertificadoSICEEB);
    }
    
    private boolean asociarIdalu ()
    {
        boolean ok=false;
        int posSelSICEEB, posSelSISCERT;
        String idalu, curpSICEEB, curpSISCERT;
        
        if ((posSelSICEEB=modelAlumnosSICEEB.indexOf(true, 0))==-1)
            return mensaje.AsociarIdalu(this, "NO_SELEC", "SICEEB", "");
        else if ((posSelSISCERT=modelAlumnosSISCERT.indexOf(true, 0))==-1)
            return mensaje.AsociarIdalu(this, "NO_SELEC", "SISCERT", "");
        else if (!modelAlumnosSISCERT.getValueAt(posSelSISCERT, 1).equals(""))
            return mensaje.AsociarIdalu(this, "IDALU_OCUPADO", "", "");
        idalu=""+modelAlumnosSICEEB.getValueAt(posSelSICEEB, 1);
        curpSICEEB=""+modelAlumnosSICEEB.getValueAt(posSelSICEEB, 2);
        curpSISCERT=""+modelAlumnosSISCERT.getValueAt(posSelSISCERT, 6);
        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, "¿Confirma que desea asociar el idalu:"+idalu+" con curp: "+curpSICEEB+" de SICCEB\nal duplicado con curp: "+curpSISCERT+" de SISCERT?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
            return false;
        
        try
        {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            conexion.conectar();
            conexion.asociarIdAlu (""+modelAlumnosSICEEB.getValueAt(posSelSICEEB, 1), ""+modelAlumnosSISCERT.getHiddenValueAt(posSelSISCERT, 0));
            modelAlumnosSISCERT.setValueAt(idalu, posSelSISCERT, 1);
            mensaje.AsociarIdalu(this, "IDALU_ASOCIADO", "", "");
            ok=true;
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        }
        return ok;
    }
    
    private void getAlumnosSICEEBySISCERT ()
    {
        if (!txtCURP.getText().equals(""))
        {
            try
            {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                conexion.conectar();
                modelAlumnosSISCERT.removeAllItems();
                modelAlumnosSICEEB.removeAllItems();
                conexion.getAlumnosSICEEBySISCERT (txtCURP.getText().trim(), global.cveunidad, modelAlumnosSISCERT, modelAlumnosSICEEB);
            }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
            catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
            finally {
                try { conexion.cerrarConexion(); } catch (SQLException ex) { }
                this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
            }
        } else
            mensaje.CrearCertifOrig(this, "DATO_A_BUSCAR", "", "");
    }
    
    private void getEstudiosDeEsteAlumno(String caso, String idalu) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
        try
        {
            conexion.conectar();
            if (caso.equals("SICEEB"))
            {
                modelCicloDeEstudioSICEEB.removeAllItems();
                modelDatosCertificadoSICEEB.removeAllItems();
                conexion.getEstudiosDeEsteAlumno (caso,idalu, global.cveplan, modelCicloDeEstudioSICEEB, modelDatosCertificadoSICEEB);
            }
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        }
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
        txtCURP = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        scrlAlumnosSICEEB = new javax.swing.JScrollPane();
        tblAlumnosSICEEB = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        scrlCicloDeEstudioSICEEB = new javax.swing.JScrollPane();
        tblCicloDeEstudioSICEEB = new javax.swing.JTable();
        scrlDatosCertificadoSICEEB = new javax.swing.JScrollPane();
        tblDatosCertificadoSICEEB = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        scrlAlumnosSISCERT = new javax.swing.JScrollPane();
        tblAlumnosSISCERT = new javax.swing.JTable();
        btnAsociarIdalu = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Área de búsqueda"));

        jLabel1.setText("CURP:");

        txtCURP.setPreferredSize(new java.awt.Dimension(146, 20));
        txtCURP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCURP_KeyPressed(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCURP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(txtCURP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Alumno en SICEEB"));

        jLabel2.setText("Datos del alumno:");

        scrlAlumnosSICEEB.setMaximumSize(new java.awt.Dimension(60, 64));
        scrlAlumnosSICEEB.setMinimumSize(new java.awt.Dimension(60, 64));
        scrlAlumnosSICEEB.setPreferredSize(new java.awt.Dimension(60, 402));

        tblAlumnosSICEEB.setModel(new javax.swing.table.DefaultTableModel(
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
        tblAlumnosSICEEB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblAlumnosSICEEB_MouseClicked(evt);
            }
        });
        tblAlumnosSICEEB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tblAlumnosSICEEB_KeyReleased(evt);
            }
        });
        scrlAlumnosSICEEB.setViewportView(tblAlumnosSICEEB);

        jLabel3.setText("Estudios terminales:");

        tblCicloDeEstudioSICEEB.setModel(new javax.swing.table.DefaultTableModel(
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
        scrlCicloDeEstudioSICEEB.setViewportView(tblCicloDeEstudioSICEEB);

        tblDatosCertificadoSICEEB.setModel(new javax.swing.table.DefaultTableModel(
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
        scrlDatosCertificadoSICEEB.setViewportView(tblDatosCertificadoSICEEB);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(288, 288, 288)
                        .addComponent(jLabel3))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(scrlAlumnosSICEEB, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(scrlCicloDeEstudioSICEEB, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                            .addComponent(scrlDatosCertificadoSICEEB, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrlAlumnosSICEEB, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(scrlCicloDeEstudioSICEEB, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scrlDatosCertificadoSICEEB, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addGap(324, 324, 324))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Alumno en SISCERT"));

        jLabel4.setText("Duplicados:");

        tblAlumnosSISCERT.setModel(new javax.swing.table.DefaultTableModel(
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
        tblAlumnosSISCERT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblAlumnosSISCERT_MouseClicked(evt);
            }
        });
        tblAlumnosSISCERT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tblAlumnosSISCERT_KeyReleased(evt);
            }
        });
        scrlAlumnosSISCERT.setViewportView(tblAlumnosSISCERT);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(scrlAlumnosSISCERT))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrlAlumnosSISCERT, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAsociarIdalu.setText("Asociar idalu");
        btnAsociarIdalu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAsociarIdalu_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAsociarIdalu)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAsociarIdalu)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtCURP_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCURP_KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER){
            getAlumnosSICEEBySISCERT ();
        }
    }//GEN-LAST:event_txtCURP_KeyPressed

    private void btnBuscar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscar_ActionPerformed
        getAlumnosSICEEBySISCERT ();
    }//GEN-LAST:event_btnBuscar_ActionPerformed

    private void tblAlumnosSICEEB_KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblAlumnosSICEEB_KeyReleased
        if (evt.getKeyCode()==KeyEvent.VK_DOWN || evt.getKeyCode()==KeyEvent.VK_UP || evt.getKeyCode()==KeyEvent.VK_ENTER){ //Si oprime las teclas de flecha
            if (tblAlumnosSICEEB.getSelectedRow()!=-1)
                getEstudiosDeEsteAlumno("SICEEB",""+tblAlumnosSICEEB.getValueAt(tblAlumnosSICEEB.getSelectedRow(),1));
        }else if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_SPACE){              //Si oprime la tecla espaciadora
            boolean casoSel;    
            int posSel = tblAlumnosSICEEB.getSelectedRow();
            casoSel = modelAlumnosSICEEB.getValueAt(posSel, 0).equals(true);
            for (int i=0; i<modelAlumnosSICEEB.getRowCount(); i++)                 //Seteamos los checks a false
                modelAlumnosSICEEB.setValueAt(false, i,0);
            modelAlumnosSICEEB.setValueAt(!casoSel, posSel, 0);
        }
    }//GEN-LAST:event_tblAlumnosSICEEB_KeyReleased

    private void tblAlumnosSICEEB_MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAlumnosSICEEB_MouseClicked
        if (tblAlumnosSICEEB.getSelectedRow()!=-1)
            getEstudiosDeEsteAlumno("SICEEB",""+tblAlumnosSICEEB.getValueAt(tblAlumnosSICEEB.getSelectedRow(),1));


        boolean casoSel;
        int posSel = tblAlumnosSICEEB.getSelectedRow();
        casoSel = modelAlumnosSICEEB.getValueAt(posSel, 0).equals(true);
        if (modelAlumnosSICEEB.getLastColEvent()==0){                              //Si dió click en la columna 0
            for (int i=0; i<modelAlumnosSICEEB.getRowCount(); i++)                 //Seteamos los checks a false
                modelAlumnosSICEEB.setValueAt(false, i,0);
            modelAlumnosSICEEB.setValueAt(!casoSel, posSel, 0);
        }
    }//GEN-LAST:event_tblAlumnosSICEEB_MouseClicked

    private void tblAlumnosSISCERT_KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblAlumnosSISCERT_KeyReleased
        boolean casoSel;    
        int posSel = tblAlumnosSISCERT.getSelectedRow();
        casoSel = modelAlumnosSISCERT.getValueAt(posSel, 0).equals(true);

        if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_SPACE){
            for (int i=0; i<modelAlumnosSISCERT.getRowCount(); i++)                 //Seteamos los checks a false
                modelAlumnosSISCERT.setValueAt(false, i,0);
            modelAlumnosSISCERT.setValueAt(!casoSel, posSel, 0);
        }
    }//GEN-LAST:event_tblAlumnosSISCERT_KeyReleased

    private void tblAlumnosSISCERT_MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAlumnosSISCERT_MouseClicked
        boolean casoSel;
        int posSel = tblAlumnosSISCERT.getSelectedRow();
        casoSel = modelAlumnosSISCERT.getValueAt(posSel, 0).equals(true);
        if (modelAlumnosSISCERT.getLastColEvent()==0){                              //Si dió click en la columna 0
            for (int i=0; i<modelAlumnosSISCERT.getRowCount(); i++)                 //Seteamos los checks a false
                modelAlumnosSISCERT.setValueAt(false, i,0);
            modelAlumnosSISCERT.setValueAt(!casoSel, posSel, 0);
        }
    }//GEN-LAST:event_tblAlumnosSISCERT_MouseClicked

    private void btnAsociarIdalu_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAsociarIdalu_ActionPerformed
        asociarIdalu ();
    }//GEN-LAST:event_btnAsociarIdalu_ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAsociarIdalu;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane scrlAlumnosSICEEB;
    private javax.swing.JScrollPane scrlAlumnosSISCERT;
    private javax.swing.JScrollPane scrlCicloDeEstudioSICEEB;
    private javax.swing.JScrollPane scrlDatosCertificadoSICEEB;
    private javax.swing.JTable tblAlumnosSICEEB;
    private javax.swing.JTable tblAlumnosSISCERT;
    private javax.swing.JTable tblCicloDeEstudioSICEEB;
    private javax.swing.JTable tblDatosCertificadoSICEEB;
    private javax.swing.JTextField txtCURP;
    // End of variables declaration//GEN-END:variables

}
