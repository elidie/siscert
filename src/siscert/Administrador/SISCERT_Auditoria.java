package siscert.Administrador;

import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import jxl.HeaderFooter;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import siscert.AccesoBD.SISCERT_QueriesInformix;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_Mensajes;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 * Creado el 21/12/2017, 04:23:26 PM
 */
public class SISCERT_Auditoria extends javax.swing.JDialog {

    private final SISCERT_GlobalMethods global;
    private final SISCERT_QueriesInformix conexion;
    private final SISCERT_Mensajes mensaje;
    
    private boolean verCinco9 = false, verOtrasRegiones = false;
    private boolean lockCbxSeccion=false;

    
    public SISCERT_Auditoria(java.awt.Frame parent, boolean modal, SISCERT_Mensajes mensaje, SISCERT_GlobalMethods global, SISCERT_QueriesInformix conexion) {
        super(parent, modal);
        initComponents();
        
        this.mensaje = mensaje;
        this.global = global;
        this.conexion = conexion;
        
        verificarPermisos();
        inicializaDatos ();
    }
    
    private void inicializaDatos ()
    {
        try {
            conexion.conectar();
            lockCbxSeccion=true;
            cbxNivelEducativo.setSelectedItem(null);
            cbxRegion.addItem("TODO EL ESTADO");
            conexion.getCveunidades(cbxRegion, this.verCinco9);
            cbxRegion.setSelectedItem(global.cveunidad);
            cbxRegion.setEnabled(this.verOtrasRegiones);
            lockCbxSeccion=false;
            cbxRegion_ActionPerformed(null);
            chkIncluirSeccion59.setVisible(this.verCinco9 && this.verOtrasRegiones);
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
    }
    
    private void generarReporte ()
    {
        Map fechaYHora;
        ResultSet rs;
        String clavesNivel[]={"","CA02","CC02","CU02"};
        int []totalFolios = {0};
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));         //Cambiamos la forma del puntero a reloj de arena
            if (validarEntradas ())
            {
                conexion.conectar();
                fechaYHora = conexion.getFechaYHoraDeBaseDeDatos();
                rs = conexion.generarAuditoria (totalFolios, txtPeriodoEscIni.getText().trim(), ""+cbxNivelEducativo.getSelectedItem(), ""+cbxRegion.getSelectedItem(), 
                        chkIncluirSeccion59.isSelected(), ""+cbxEstatusImpre.getSelectedItem());
                writeExcel(txtRutaArchivo.getText(), "Hoja1", rs, ""+fechaYHora.get("fecha"), ""+cbxRegion.getSelectedItem(), clavesNivel[(cbxNivelEducativo.getSelectedItem().equals("PRIMARIA")?1:(cbxNivelEducativo.getSelectedItem().equals("SECUNDARIA")?2:3))],txtPeriodoEscIni.getText()+"-"+txtPeriodoEscFin.getText(), totalFolios[0]);
                mensaje.Auditoria(this, "ARCHIVO_GENERADO", "", "");
            }
        }catch (SQLException ex){ mensaje.General(this,"CONEXION",ex.getMessage(),""); }
        catch (Exception ex){ mensaje.General(this,"GENERAL", ex.getMessage(), ""); }
        finally {
            try { conexion.cerrarConexion(); } catch (SQLException ex) { }
            this.setCursor(Cursor.getDefaultCursor());                              //Cambiamos la forma del puntero a default
        }
    }
    
    private void writeExcel(String excel_file, String sheet_name, ResultSet rs, String fecha, String region, String claveNivel, String periodoEscolar, int totalFolios) throws Exception
    {
        int rowDataIni=0, row=0, column=0;
        
        try{
            WorkbookSettings ws=new WorkbookSettings();
            ws.setLocale(new Locale("es","ES"));
            WritableWorkbook workbook = Workbook.createWorkbook(new File(excel_file),ws);
            workbook.createSheet(sheet_name, 1);
            WritableSheet sheet = workbook.getSheet(sheet_name);
            //sheet.setPageSetup(PageOrientation.PORTRAIT, PaperSize.LETTER, row, row);
            SheetSettings sheetSettings = sheet.getSettings();//new SheetSettings(sheet);
            sheetSettings.setLeftMargin(0.3);                                               //Para 2cm de margen
            sheetSettings.setRightMargin(0.3);                                              //Para 2cm de margen
            
            HeaderFooter header = new HeaderFooter();
            header.getRight().append("Fecha de consulta: "+fecha);
            sheetSettings.setFooter(header);
            
            
            //sheetSettings.setDefaultRowHeight(500);
            //sheetSettings.setProtected(true);
            //sheetSettings.setPassword("test");
            //sheetSettings.setFitToPages(true);  
            
            //Estalecemos el ancho de las columnas
            //Set cell width in CHARS
            sheet.setColumnView(0, 3);                                          //El tamaño es en caracteres, pero más milimétricamente sería: CellView cellView = new CellView(); (o tambien CellView cell = sheet.getColumnView(column);)cellView.setSize(widthInChars * 256); sheet.setColumnView(column, cellView);
            sheet.setColumnView(2, 2);
            sheet.setColumnView(4, 7);
            sheet.setColumnView(5, 2);
            sheet.setColumnView(7, 2);
            sheet.setColumnView(9, 7);
            sheet.setColumnView(10, 2);
            sheet.setColumnView(12, 2);
            sheet.setColumnView(14, 7);
            
            imprimirEncabezadoDeHoja (sheet, 0, ""+cbxEstatusImpre.getSelectedItem(), region, ""+cbxNivelEducativo.getSelectedItem(), claveNivel, periodoEscolar);
            
            String folioLet, folioLetAnt="";
            int cantConsecutivos=1, colFolLet, colFolNum;
            long folioAnt=0, folioActual=0;
            int numFilasOcupTitulos=7, numfilasConDatos=43; //<--- Mover este parámetro para la cantidad de datos por hoja
            int totalDeFoliosPorReporte=0;
            Map nombresCol = new HashMap();
            nombresCol.put(0, "E");  nombresCol.put(5, "J");  nombresCol.put(10, "O");
            
            rowDataIni=numFilasOcupTitulos; row=-1; column=0;
            while (rs.next())
            {
                folioLet = rs.getString("foliolet");
                folioActual = rs.getLong("folionum");
                if (folioAnt+1 == folioActual && folioLetAnt.equals(folioLet)){
                    cantConsecutivos++;
                    colFolLet=2; colFolNum=3;
                    totalDeFoliosPorReporte++;
                }else {
                    cantConsecutivos=1;
                    row++;
                    if (row>0 && row==numfilasConDatos){
                        //Create a formula for adding cells
                        column+=5;
                        row=0;
                        if (column==15){
                            column=0;
                            sheet.addRowPageBreak(rowDataIni+numfilasConDatos+2);
                            imprimirEncabezadoDeHoja (sheet, rowDataIni+numfilasConDatos+3, ""+cbxEstatusImpre.getSelectedItem(), region, ""+cbxNivelEducativo.getSelectedItem(), claveNivel, periodoEscolar);
                            rowDataIni=numFilasOcupTitulos+rowDataIni+numfilasConDatos+3;
                        }
                    }
                    colFolLet=0; colFolNum=1;
                    
                    if (row==0 && column==0){
                        dibujarCuadrícula ( rowDataIni, 0, rowDataIni+numfilasConDatos, 14, sheet);
                        //Ponemos los títulos de las sumatorias
                        sheet.addCell(new jxl.write.Label(3, rowDataIni+numfilasConDatos, "SUBTOT.:",cellFormat(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD), Alignment.RIGHT, null, null, null)));
                        sheet.addCell(new jxl.write.Label(8, rowDataIni+numfilasConDatos, "SUBTOT.:",cellFormat(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD), Alignment.RIGHT, null, null, null)));
                        sheet.addCell(new jxl.write.Label(13, rowDataIni+numfilasConDatos, "SUBTOT.:",cellFormat(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD), Alignment.RIGHT, null, null, null)));
                        sheet.addCell(new jxl.write.Label(13, rowDataIni+numfilasConDatos+1, "TOTAL:",cellFormat(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD), Alignment.RIGHT, null, null, null)));
                        //Ponemos las fórmulas
                        sheet.addCell(new jxl.write.Formula(4, rowDataIni+numfilasConDatos, "SUMA(E"+(rowDataIni+1)+":E"+(rowDataIni+numfilasConDatos)+")", cellFormat(null, null, "ALL", BorderLineStyle.MEDIUM, null)));
                        sheet.addCell(new jxl.write.Formula(9, rowDataIni+numfilasConDatos, "SUMA(J"+(rowDataIni+1)+":J"+(rowDataIni+numfilasConDatos)+")", cellFormat(null, null, "ALL", BorderLineStyle.MEDIUM, null)));
                        sheet.addCell(new jxl.write.Formula(14, rowDataIni+numfilasConDatos, "SUMA(O"+(rowDataIni+1)+":O"+(rowDataIni+numfilasConDatos)+")", cellFormat(null, null, "ALL", BorderLineStyle.MEDIUM, null)));
                        sheet.addCell(new jxl.write.Formula(14, rowDataIni+numfilasConDatos+1, "SUMA(E"+(rowDataIni+numfilasConDatos+1)+",J"+(rowDataIni+numfilasConDatos+1)+",O"+(rowDataIni+numfilasConDatos+1)+")", cellFormat(null, null, "ALL", BorderLineStyle.MEDIUM, null)));
                    }
                }
                
                if (colFolLet==0)
                    totalDeFoliosPorReporte++;
                sheet.addCell(new jxl.write.Label(column+colFolLet, rowDataIni+row, folioLet,cellFormat(null, null, "RIGHT,BOTTOM,LEFT", BorderLineStyle.THIN, null)));
                sheet.addCell(new jxl.write.Number(column+colFolNum, rowDataIni+row, folioActual,cellFormat(null, Alignment.LEFT, "RIGHT,BOTTOM,LEFT", BorderLineStyle.THIN, null)));
                sheet.addCell(new jxl.write.Number(column+4, rowDataIni+row, cantConsecutivos,cellFormat(null, null, "RIGHT,BOTTOM,LEFT", BorderLineStyle.THIN, null)));
                
                folioAnt = folioActual; 
                folioLetAnt = folioLet;
            }
            
            sheet.addCell(new jxl.write.Label(13, rowDataIni+numfilasConDatos+2, "TOTAL GENERAL:",cellFormat(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD), Alignment.RIGHT, null, null, null)));
            sheet.addCell(new jxl.write.Label(13, rowDataIni+numfilasConDatos+3, "TOT. FOL. IMP.:",cellFormat(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD), Alignment.RIGHT, null, null, null)));
            sheet.addCell(new jxl.write.Number(14, rowDataIni+numfilasConDatos+2, totalDeFoliosPorReporte, cellFormat(null, null, "ALL", BorderLineStyle.MEDIUM, null)));
            sheet.addCell(new jxl.write.Number(14, rowDataIni+numfilasConDatos+3, totalFolios, cellFormat(null, null, "ALL", BorderLineStyle.MEDIUM, null)));
            
            workbook.write();
            workbook.close();
        }
        catch(Exception e)
        {
            throw new Exception(e.getMessage());
        }
    }
    
    private void imprimirEncabezadoDeHoja (WritableSheet sheet, int row, String estatus, String region, String nivelEduc, String claveNivel, String periodoEscolar) throws WriteException
    {
        jxl.write.Label dato;
        
        //Título del documento
        sheet.mergeCells(0, row, 14, row);
        dato = new jxl.write.Label(0, row, "RELACIÓN DE DOCUMENTOS "+(estatus.equals("TODOS")?"IMPRESOS, CANCELADOS E INCOMPLETOS":estatus),cellFormat(null, Alignment.CENTRE, null, null, null));
        sheet.addCell(dato);
        //Datos generales
        sheet.addCell(new jxl.write.Label(1, row+2, "ENTIDAD:",cellFormat(null, Alignment.RIGHT, null, null, null)));
        sheet.addCell(new jxl.write.Label(2, row+2, "OAXACA"));
        sheet.addCell(new jxl.write.Label(6, row+2, "REGION:",cellFormat(null, Alignment.RIGHT, null, null, null)));
        sheet.addCell(new jxl.write.Label(7, row+2, region));

        sheet.addCell(new jxl.write.Label(1, row+3, "NIVEL EDUC:",cellFormat(null, Alignment.RIGHT, null, null, null)));
        sheet.addCell(new jxl.write.Label(2, row+3, nivelEduc));
        sheet.addCell(new jxl.write.Label(6, row+3, "CLAVE:",cellFormat(null, Alignment.RIGHT, null, null, null)));
        sheet.addCell(new jxl.write.Label(7, row+3, claveNivel));
        sheet.addCell(new jxl.write.Label(11, row+3, "PERIODO ESCOLAR:",cellFormat(null, Alignment.RIGHT, null, null, null)));
        sheet.addCell(new jxl.write.Label(12, row+3, periodoEscolar));

        //Título de columna de datos
        // selecting the region in Worksheet for merging data
        // merging the region
        sheet.mergeCells(0, row+5, 3, row+5);
        sheet.mergeCells(5, row+5, 8, row+5);
        sheet.mergeCells(10, row+5, 13, row+5);
        sheet.addCell(new jxl.write.Label(0, row+5, "RANGO DE FOLIOS", fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(5, row+5, "RANGO DE FOLIOS", fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(10, row+5, "RANGO DE FOLIOS", fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.mergeCells(0, row+6, 1, row+6);
        sheet.mergeCells(5, row+6, 6, row+6);
        sheet.mergeCells(10, row+6, 11, row+6);
        sheet.addCell(new jxl.write.Label(0, row+6, "DEL",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(5, row+6, "DEL",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(10, row+6, "DEL",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.mergeCells(2, row+6, 3, row+6);
        sheet.mergeCells(7, row+6, 8, row+6);
        sheet.mergeCells(12, row+6, 13, row+6);
        sheet.addCell(new jxl.write.Label(2, row+6, "AL",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(7, row+6, "AL",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(12, row+6, "AL",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.mergeCells(4, row+5, 4, row+6);
        sheet.mergeCells(9, row+5, 9, row+6);
        sheet.mergeCells(14, row+5, 14, row+6);
        sheet.addCell(new jxl.write.Label(4, row+5, "CANT",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(9, row+5, "CANT",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
        sheet.addCell(new jxl.write.Label(14, row+5, "CANT",fontCellFormat (10, true, Alignment.CENTRE, "ALL", BorderLineStyle.MEDIUM, Colour.GREY_25_PERCENT)));
    }
    
    private WritableCellFormat textFormat (int tam, boolean isBold)
    {
        WritableFont wFont=null;
        if (isBold)
            wFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        else
            wFont = new WritableFont(WritableFont.ARIAL, 10);
                    
        WritableCellFormat cellFormat = new WritableCellFormat(wFont);
        
        return cellFormat;
    }
    
    private WritableCellFormat cellFormat (WritableFont wFont, Alignment alineación, String border, BorderLineStyle borderLineStyle, Colour colour ) throws WriteException
    {
        WritableCellFormat wCellFormat;
        if (wFont==null)
            wCellFormat = new WritableCellFormat();
        else
            wCellFormat = new WritableCellFormat(wFont);
        
        if (alineación!=null)
            wCellFormat.setAlignment(alineación);                                   //Alineación de texto: Alignment.CENTRE
        
        if (border!=null)
        {
            if (border.equals("ALL"))
                wCellFormat.setBorder(jxl.format.Border.ALL, borderLineStyle);     //borderLineStyle --> BorderLineStyle.THIN
            else {
                if (border.contains("TOP"))
                    wCellFormat.setBorder(jxl.format.Border.TOP, borderLineStyle);  
                if (border.contains("RIGHT"))
                    wCellFormat.setBorder(jxl.format.Border.RIGHT, borderLineStyle);
                if (border.contains("BOTTOM"))
                    wCellFormat.setBorder(jxl.format.Border.BOTTOM, borderLineStyle);
                if (border.contains("LEFT"))
                    wCellFormat.setBorder(jxl.format.Border.LEFT, borderLineStyle);
            }
        }
        
        if (colour!=null)
            wCellFormat.setBackground(colour);                                      //colour --> Colour.GREY_25_PERCENT,  o tambien  wCellFormat.setBackground(backgroundColor, pattern);  pattern --> Pattern.GRAY_25
        
        return wCellFormat;
    }
    
    private WritableCellFormat fontCellFormat (int tam, boolean isBold, Alignment alineación, String border, BorderLineStyle borderLineStyle, Colour backgorudColor) throws WriteException
    {
        WritableFont wFont=null;
        if (isBold)
            wFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        else
            wFont = new WritableFont(WritableFont.ARIAL, 10);
                    
        return cellFormat (wFont, alineación, border, borderLineStyle, backgorudColor);
    }
    
    private void dibujarCuadrícula ( int filaIni, int colIni, int filaFin, int colFin, WritableSheet sheet) throws WriteException
    {
        for (int i=filaIni; i<filaFin; i++)
            for (int j=colIni; j<=colFin; j++)
            {
                if (i==filaIni)
                    sheet.addCell(new jxl.write.Label(j, i, "", cellFormat(null, null, "RIGHT,BOTTOM,LEFT", BorderLineStyle.THIN, null)));
                else
                    sheet.addCell(new jxl.write.Label(j, i, "", cellFormat(null, null, "ALL", BorderLineStyle.THIN, null)));
            }
    }
    
    public /*static*/ void makeCell(NumberFormat format, WritableFont font, Color backgrd, Border border){
        //public static final NumberFormat numberformatter = new NumberFormat("#,###0.00");  
        //public static final */WritableFont defaultfont = new WritableFont(WritableFont.TAHOMA, 10); 
        //public static final */WritableCellFormat numberCellFormat = new WritableCellFormat(defaultfont, numberformatter);
        //final*/ WritableCellFormat result = new WritableCellFormat(defaultfont, numberformatter);
        //result.setBorder(border);
        //result.setBackground(backgrd);
        //return result;
    }
    
    private void seleccionarRutaArchivo ()
    {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File("."));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel", "xls", "xlsx");
        fc.setFileFilter(filter);
        int respuesta = fc.showOpenDialog(this);
        
        if (respuesta == JFileChooser.APPROVE_OPTION)
        {
            File archivoElegido = fc.getSelectedFile();
            if (archivoElegido.getPath().matches(".*(.xls|.XLS)"))
                txtRutaArchivo.setText(archivoElegido.getPath());
            else
                txtRutaArchivo.setText(archivoElegido.getPath()+".xls");
        }
    }
    
    private boolean validarEntradas ()
    {
        if (txtPeriodoEscIni.getText().trim().equals(""))
            return mensaje.General(this, "CAMPO_VACIO", "'PERÍODO ESCOLAR'", "");
        if (cbxNivelEducativo.getSelectedIndex()==-1)
            return mensaje.General(this, "CAMPO_VACIO", "'NIVEL EDUCATIVO'", "");
        
        if (!txtPeriodoEscIni.getText().trim().matches("[0-9]{4}"))
            return mensaje.Auditoria(this, "PERIODOINI_INVALIDO", txtPeriodoEscIni.getText(), "");
        
        if (txtRutaArchivo.getText().equals(""))
            return mensaje.Auditoria(this, "RUTA_EXCEL", "", "");
        return true;
    }
    
    private void verificarPermisos()
    {
        try {
            String[] objetos = {"verOtrasRegiones", "verCinco9_Auditoria"};
            boolean[] objPermisos;
            int i=0;
            
            conexion.conectar();
            objPermisos = conexion.getEstosPermisos("" + global.idcapturista, "Auditoria", objetos);
            
            for (String objeto : objetos){
                if (objeto.equals(objetos[0])){
                    verOtrasRegiones = objPermisos[i];
                }else if (objeto.equals(objetos[1])){
                    verCinco9 = objPermisos[i];
                }   
                i++;
            }
        } catch(SQLException ex) { mensaje.General(this,"CONEXION", ex.getMessage(), "");  } 
        catch(Exception ex) { mensaje.General(this,"GENERAL", ex.getMessage(), "");  } 
        try { conexion.cerrarConexion(); } catch (SQLException ex) { }
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
        txtPeriodoEscIni = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtPeriodoEscFin = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cbxNivelEducativo = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        cbxRegion = new javax.swing.JComboBox<>();
        chkIncluirSeccion59 = new javax.swing.JCheckBox();
        cbxEstatusImpre = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtRutaArchivo = new javax.swing.JTextField();
        btnSelecRutaDeArchivo = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnGenerarReporte = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Período escolar:");

        txtPeriodoEscIni.setPreferredSize(new java.awt.Dimension(39, 20));
        txtPeriodoEscIni.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPeriodoEscIni_KeyTyped(evt);
            }
        });

        jLabel2.setText("-");

        txtPeriodoEscFin.setEditable(false);
        txtPeriodoEscFin.setPreferredSize(new java.awt.Dimension(39, 20));

        jLabel3.setText("Nivel educativo:");

        cbxNivelEducativo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SECUNDARIA", "PRIMARIA", "PREESCOLAR" }));
        cbxNivelEducativo.setMaximumSize(new java.awt.Dimension(90, 32767));
        cbxNivelEducativo.setMinimumSize(new java.awt.Dimension(90, 20));
        cbxNivelEducativo.setPreferredSize(new java.awt.Dimension(90, 20));

        jLabel4.setText("Región:");

        cbxRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxRegion_ActionPerformed(evt);
            }
        });

        chkIncluirSeccion59.setText("Incluir sección 59");

        cbxEstatusImpre.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText("Estatus:");

        jLabel6.setText("Ruta y nombre del archivo a generar:");

        txtRutaArchivo.setEditable(false);
        txtRutaArchivo.setText("jTextField3");

        btnSelecRutaDeArchivo.setText("Seleccionar...");
        btnSelecRutaDeArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelecRutaDeArchivo_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkIncluirSeccion59)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtPeriodoEscIni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtPeriodoEscFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cbxNivelEducativo, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cbxRegion, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbxEstatusImpre, javax.swing.GroupLayout.Alignment.LEADING, 0, 119, Short.MAX_VALUE))))
                    .addComponent(jLabel6)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtRutaArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSelecRutaDeArchivo)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPeriodoEscIni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtPeriodoEscFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbxNivelEducativo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbxRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkIncluirSeccion59)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxEstatusImpre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRutaArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelecRutaDeArchivo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelar_ActionPerformed(evt);
            }
        });

        btnGenerarReporte.setText("Generar reporte");
        btnGenerarReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarReporte_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGenerarReporte)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnGenerarReporte))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtPeriodoEscIni_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPeriodoEscIni_KeyTyped
        String cicEscIni = "";
        if(global.revisarTextoPermitido (evt.getKeyChar(), "NUMERICO"))
        {
            cicEscIni = global.limitText (evt, txtPeriodoEscIni, 4);
            if (cicEscIni.length()==4 )                                             //Si el usuario ya introdujo los 4 números del período escolar
                txtPeriodoEscFin.setText(""+(Integer.parseInt(cicEscIni)+1));        //Mostramos el periodoEscIni + 1
            else
                txtPeriodoEscFin.setText("");
        }
        else
            evt.consume();
    }//GEN-LAST:event_txtPeriodoEscIni_KeyTyped

    private void cbxRegion_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxRegion_ActionPerformed
        if (!lockCbxSeccion){
            chkIncluirSeccion59.setEnabled(cbxRegion.getSelectedItem().equals("TODO EL ESTADO"));// TODO add your handling code here:
            chkIncluirSeccion59.setSelected(false);
        }
    }//GEN-LAST:event_cbxRegion_ActionPerformed

    private void btnSelecRutaDeArchivo_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecRutaDeArchivo_ActionPerformed
        seleccionarRutaArchivo ();
    }//GEN-LAST:event_btnSelecRutaDeArchivo_ActionPerformed

    private void btnGenerarReporte_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarReporte_ActionPerformed
        generarReporte ();
    }//GEN-LAST:event_btnGenerarReporte_ActionPerformed

    private void btnCancelar_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelar_ActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelar_ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGenerarReporte;
    private javax.swing.JButton btnSelecRutaDeArchivo;
    private javax.swing.JComboBox<String> cbxEstatusImpre;
    private javax.swing.JComboBox<String> cbxNivelEducativo;
    private javax.swing.JComboBox<String> cbxRegion;
    private javax.swing.JCheckBox chkIncluirSeccion59;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtPeriodoEscFin;
    private javax.swing.JTextField txtPeriodoEscIni;
    private javax.swing.JTextField txtRutaArchivo;
    // End of variables declaration//GEN-END:variables

}
