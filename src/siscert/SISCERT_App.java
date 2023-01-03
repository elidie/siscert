package siscert;

import javax.swing.JDialog;
import javax.swing.JFrame;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;
import siscert.Inicio.SISCERT_LoginUser;
import siscert.Inicio.SISCERT_VentanaPrincipal;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 */
public class SISCERT_App 
{
    private JDialog ventanaLoginUser;
    private JFrame frmVentanaPrincipal = null;
    
    private SISCERT_GlobalMethods global;
    private SISCERT_Mensajes mensaje;
    private SISCERT_QueriesInformix qryIfx;
    
    private final String versionSistema = "3.4.3", nombreSistema = "SISCERT";
    private final int tipoVersion = 1;                                          //0:Actualización, 1:Completa
    private final String tituloSistema = nombreSistema+" v"+versionSistema+" - Sistema para impresión de duplicado de certificados y certificaciones.";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SISCERT_App app = new SISCERT_App();
        
        app.mensaje = new SISCERT_Mensajes();
        app.qryIfx = new SISCERT_QueriesInformix();
        //app.ventanaPrincipal();
        app.ventanaLoginUser ();
    }
    
    public void ventanaPrincipal()
    {
        try {
            if (frmVentanaPrincipal == null) {                                  //si aún no se ha creado el formulario
                global = new SISCERT_GlobalMethods("INTERNET", versionSistema,"DSRVAL","ELYLOPEZ","899");
                qryIfx.setTipoConexion(global.tipoConexion);
                frmVentanaPrincipal = new SISCERT_VentanaPrincipal(this.global,this.mensaje,this.qryIfx);        //creamos un formulario
                frmVentanaPrincipal.setLocationRelativeTo(null);                //le damos una localización en la pantalla
                frmVentanaPrincipal.setTitle(tituloSistema);
                frmVentanaPrincipal.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
            }
            frmVentanaPrincipal.setExtendedState(JFrame.MAXIMIZED_BOTH);
            //frmVentanaPrincipal.setResizable(false);
            frmVentanaPrincipal.setVisible(true);
        }catch (Exception ex){ mensaje.General(null,"GENERAL", ""+ex, ""); System.exit(0); }
    }

    public void ventanaLoginUser ()
    {
        if (ventanaLoginUser == null) {                                         //si aún no se ha creado el panel preeliminar Anverso
            //JFrame frmVentanaLoginUser = RSICEEO_App.getApplication().getMainFrame(); //creamos un formulario
            ventanaLoginUser = new SISCERT_LoginUser(new JFrame(), true, this.nombreSistema, tituloSistema, this.versionSistema, tipoVersion, this.mensaje, this.qryIfx);      //creamos el objeto
            ventanaLoginUser.setLocationRelativeTo(null);                       //le damos una localización en la pantalla
            ventanaLoginUser.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            ventanaLoginUser.setResizable(false);
            ventanaLoginUser.setTitle("Inicio de sesión - SISCERT v" + versionSistema);
        }
        ventanaLoginUser.setVisible(true);                    //mostramos la ventana*/
    }
    
}
