package siscert.Impresion.Report;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;


/* 
    Creado el : 5/07/2017, 03:34:07 PM
    Autor     : Ing. Maai Nolasco Sánchez
*/
class SISCERT_ReportViewer extends JRViewer 
{
    private int intbtnPresionado=0;
    private static final long serialVersionUID = 1271367514255520348L;
    
    protected SISCERT_ReportViewer(JasperPrint jrPrint) 
    {
        super(jrPrint);
        btnPrint.addActionListener(new ActionListener()
        {
            @Override
            //Metodo para controlar el evento click del boton guarar. 
            public void actionPerformed(ActionEvent arg0) 
            {
                System.out.println("presiono");
                intbtnPresionado = 1;
                System.out.println("Valor del la variable intbtnPresionado: "+intbtnPresionado);
            }
        });
    }
    
    ///Metodo para habilitar o deshabilitar el boton guardar
    protected void setPrintEnabled(boolean enabled)
    {
        btnPrint.setEnabled(enabled);
        btnSave.setEnabled(false);
    }
    
    protected int getNumImpresiones ()
    {
        return intbtnPresionado;
    }
    
}
