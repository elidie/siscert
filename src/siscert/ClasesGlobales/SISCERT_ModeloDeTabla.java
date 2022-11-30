/*
 * 
 * Creado por Ing. Maai Nolasco Sanchez.
 * 22/Oct/2011
 * última edición: 24/Nov/16
 * Versión: 3.0.1
 * 
 */

package siscert.ClasesGlobales;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class SISCERT_ModeloDeTabla extends AbstractTableModel {
    //--------------- Nombres de variables que es forzoso que se nombren así --------
    String[] columnNames;
    String[] hiddenColumnNames;
    ArrayList<Object[]> data;                                                   //Este arreglo sólo es utilizable si ModeloDeTabla hereda a AbstractTableModel
    ArrayList<Object[]> hiddenData;                                             
    Map alias, hiddenAlias;
    
    Class types[];
    boolean canEdit[];
    //--------------- Mis variables ---------------
    boolean isSetEditableVector, isSetClassesVector,isSetEditableDefault, isSetClaseDefault;   //Banderas que nos indicarán si el usuario estableció si es editable o tipo de clase respectivamente, si no devolvemos un defalut
    boolean editableDefault;
    Class claseDefault;
    int rowEvent, colEvent;
    
    private ListSelectionModel listSelectionModel=null;
    private int selectedRow=-1, lastSelectedRow=-1;

    public SISCERT_ModeloDeTabla (String[] columnNames)
    {
        this.columnNames = columnNames;
        this.hiddenColumnNames = null;
        initCommonData ();
    }
    
    public SISCERT_ModeloDeTabla (ArrayList<String> columnNames)
    {
        this.columnNames = (String[])columnNames.toArray();
        this.hiddenColumnNames = null;
        initCommonData ();
    }
    
    public SISCERT_ModeloDeTabla (int numColumnNames)
    {
        columnNames = new String[numColumnNames];
        for (int i=0; i<numColumnNames; i++)
            columnNames[i]="Col "+i;
        this.hiddenColumnNames = null;
        initCommonData ();
    }
    
    public SISCERT_ModeloDeTabla (String[] columnNames, String[] hiddenColumnNames)
    {
        this.columnNames = columnNames;
        this.hiddenColumnNames = hiddenColumnNames;
        initCommonData ();
    }
    
    public SISCERT_ModeloDeTabla (ArrayList<String> columnNames, ArrayList<String> hiddenColumnNames)
    {
        this.columnNames = (String[])columnNames.toArray();
        this.hiddenColumnNames = (String[])hiddenColumnNames.toArray();
        initCommonData ();
    }
    
    public SISCERT_ModeloDeTabla (int numColumnNames, int numHiddenColumnNames)
     {
        columnNames = new String[numColumnNames];
        for (int i=0; i<numColumnNames; i++)
            columnNames[i]="Col "+i;
        hiddenColumnNames = new String[numHiddenColumnNames];
        for (int i=0; i<numHiddenColumnNames; i++)
            hiddenColumnNames[i]="HCol "+i;
        initCommonData ();
     }
    
    private void initCommonData ()
    {
        this.data = new ArrayList<Object[]>();
        this.hiddenData = new ArrayList<Object[]>();
        this.alias=null;
        this.hiddenAlias=null;
        this.isSetEditableVector=false;
        this.isSetClassesVector=false;
        this.isSetClaseDefault=false;
        this.isSetEditableDefault=false;
        this.rowEvent=-1; this.colEvent=-1;
    }
//-----------------------------------------------------------------------------
//---------------- Métodos que necesitan ser implementados --------------------
//-----------------------------------------------------------------------------
    public int getColumnCount() {  return columnNames.length;  }
    public int getRowCount() {  return data.size();  }
    
    @Override 
    public String getColumnName(int col) {  return columnNames[col];  }
    
    @Override
     public Class getColumnClass(int c) {
         if (!isSetClassesVector)                                               //Si el usuario no ha establecido qué clase es para cada columna
             if (isSetClaseDefault)
                 return this.claseDefault;                                      //Default establecido por el usuario
             else
                 try{
                    return getValueAt(0, c).getClass();                         //La primer fila rige por default el tipo de dato
                 }catch (NullPointerException ex){ return String.class; };      //Si la tabla tiene datos nulos y no se pudo identificar el tipo de dato que contiene, el default será String
         return types[c];
     }
    
    @Override
     public boolean isCellEditable(int row, int col) {  
        this.rowEvent=row;
        this.colEvent=col; 
        
        if (!isSetEditableVector)                                               //Si el usuario no ha establecido un vector de clase que es para cada columna
             if (isSetEditableDefault)
                 return this.editableDefault;                                   //Default establecido por el usuario
             else
                return false;                                                   //El dafult es false
                  
         return canEdit[col];
     }
     public Object getValueAt(int row, int col) {  
        Object []fila=data.get(row);
        return fila[col];  
     }
     
    @Override
     public void setValueAt(Object value, int row, int col) {
         Object []fila=data.get(row);
         fila[col]=value;
         fireTableCellUpdated(row, col);
     }
    

     //-----------------------------------------------------------------------------
     //------------------------------ Mis métodos ----------------------------------
     //-----------------------------------------------------------------------------
    
    public void setClases (Class[] classes){  
         if (columnNames.length != classes.length) 
            throw new IllegalArgumentException("Error en ModeloDeTabla.setClases (...): El arreglo de clases que desea establecer debe tener la misma longitud que la cantidad de columnas: "+columnNames.length);
         
         this.types  = classes;  isSetClassesVector=true; this.isSetClaseDefault=false; }

     public void setEditables (boolean []editable){ 
         if (editable==null || editable.length==0)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setEditables (...): El arreglo de posiciones editables no debe ser null o tener tamaño 0 ***\n");
         else if (columnNames.length != editable.length )
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setEditables (...): El arreglo de poder editar con tamaño "+editable.length+" que desea establecer debe tener la misma longitud que la cantidad de columnas ya establecidas: "+columnNames.length+" ***\n");
         
         this.canEdit = editable; isSetEditableVector=true; this.isSetEditableDefault=false;
     }
     
     public void setEditables (int []editable){ 
         boolean[] edits=new boolean[columnNames.length];
         
         if (editable==null || editable.length==0)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setEditables (...): El arreglo de posiciones editables no debe ser null o tener tamaño 0 ***\n");
         else if ( editable.length>=columnNames.length )
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setEditables (...): El arreglo de poder editar con tamaño "+editable.length+" que desea establecer debe tener un longitud no mayor a la cantidad de columnas ya establecidas: "+columnNames.length+" ***\n");
         
         for (int i=0; i<editable.length; i++)
             if (editable[i]>=columnNames.length)
                 throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setEditables (...): Ha especificado en la posición "+i+" un tamaño ("+editable[i]+") mayor al número de columnas ya establecidas: "+columnNames.length+" ***\n");
             else
                 edits[editable[i]]=true;
             
         this.canEdit = edits; isSetEditableVector=true; this.isSetEditableDefault=false;
     }

     public void setDefaultClass (Class claseDefault){ this.claseDefault = claseDefault; this.isSetClaseDefault=true; isSetClassesVector=false;}

     public void setDefaultEditable(boolean editableDefault){ this.editableDefault = editableDefault; this.isSetEditableDefault=true; isSetEditableVector=false; }

     public void setDefaultClass (){ this.claseDefault = null; this.isSetClaseDefault=false; isSetClassesVector=false;}                     //Reseteamos valores a default

     public void setDefaultEditable(){ this.editableDefault = false; this.isSetEditableDefault=false; isSetEditableVector=false; }          //Reseteamos valores a default

     /**
     * Establece un alias a la columna con posición indicada
     * @param AliasPoscol arreglo tipo Clave-Valor indicando alias-Numcol respectivamente
     */
     public void setAlias (String AliasPoscol[][])
     {
         int pos, i, posColMax = columnNames.length-1;
         if (alias == null)
            alias = new HashMap();
         else
             alias.clear();
         
         if (posColMax==-1){
             if (alias != null) { alias.clear(); alias = null; } 
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setAlias (...): Aun no se han definido columnas para la tabla. ***\n");
         }
         for (i=0; i<AliasPoscol.length; i++){
             try { 
                 pos = Integer.parseInt(AliasPoscol[i][1]);
                 if (pos<=-1 || pos>posColMax){
                     if (alias != null) { alias.clear(); alias = null; }
                     throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setAlias (...): La posición de columna "+i+" para el alias "+AliasPoscol[i][0]+" no existe. ***\n");
                 }
                 alias.put(AliasPoscol[i][0], pos);
             }catch (NumberFormatException ex){
                 if (alias != null) { alias.clear(); alias = null; }
                 throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setAlias (...): La posición "+i+" para el alias "+AliasPoscol[i][0]+" no es correcta. ***\n");
             }
         }
     }
     
     /**
     * Establece un alias a la columna oculta con posición indicada
     * @param HiddenaliasPoscol arreglo tipo Clave-Valor indicando alias-Numcol respectivamente
     */
     public void setHiddenAlias (String HiddenaliasPoscol[][])
     {
         int pos, i, posHidColMax = hiddenColumnNames.length-1;
         if (hiddenAlias == null)
            hiddenAlias = new HashMap();
         else
             hiddenAlias.clear();
         
         if (posHidColMax==-1){
             if (hiddenAlias != null) { hiddenAlias.clear(); hiddenAlias = null; }
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setHiddenAlias (...): Aun no se han definido columnas para la tabla. ***\n");
         }for (i=0; i<HiddenaliasPoscol.length; i++){
             try { 
                 pos = Integer.parseInt(HiddenaliasPoscol[i][1]);
                 if (pos<=-1 || pos>posHidColMax){
                     if (hiddenAlias != null) { hiddenAlias.clear(); hiddenAlias = null; }
                     throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setHiddenAlias (...): La posición de columna "+i+" para el alias oculto "+HiddenaliasPoscol[i][0]+" no existe. ***\n");
                 }
                 hiddenAlias.put(HiddenaliasPoscol[i][0], pos);
             }catch (NumberFormatException ex){
                 if (hiddenAlias != null) { hiddenAlias.clear(); hiddenAlias = null; }
                 throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setHiddenAlias (...): La posición "+i+" para el alias oculto "+HiddenaliasPoscol[i][0]+" no es correcta. ***\n");
             }
         }
     }
     
     /**
     * Establece alias para la columna y columna oculta
     * @param AliasPoscol arreglo tipo Clave-Valor indicando alias-Numcol respectivamente
      *@param HiddenaliasPoscol arreglo tipo Clave-Valor indicando aliasOculto-Numcol respectivamente
     */
     public void setAlias (String AliasPoscol[][], String HiddenaliasPoscol[][])
     {
         setAlias (AliasPoscol);
         setHiddenAlias (HiddenaliasPoscol);
     }
     
     public int getPosColAlias (String strAlias)
     {
         int posCol;
         if (alias==null)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getPosColAlias (): Aun no se han definido alias para la tabla. ***\n");
         posCol = (Integer)this.alias.get(strAlias);
         if  (posCol==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getPosColAlias (alias): El alias "+alias+" no existe. ***\n");
         return posCol;
     }
     
     public int getPosColHiddenAlias (String strHiddenAlias)
     {
         int posCol;
         if (this.hiddenAlias==null)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getPosColHiddenAlias (): Aun no se han definido alias para la tabla. ***\n");
         posCol = (Integer)this.hiddenAlias.get(strHiddenAlias);
         if  (posCol==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getPosColHiddenAlias (alias): El alias oculto "+alias+" no existe. ***\n");
         return posCol;
     }
     
     public Object getHiddenValueAt(int row, int col) {  
        if (hiddenData.size() == data.size() ){
             Object []fila=hiddenData.get(row);
            return fila[col];
        }else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenValueAt (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
     }
     
     /**
      * Devuelve el valor oculto que está en la fila y alias de columna oculta especificado
      * @param row número de fila
      * @param alias nombre del alias de la columna oculta
      * @return el objeto contenido en la posición indicada
      */
     public Object getHiddenValueAt(int row, String alias)
     {
         int posCol;
         if (hiddenAlias==null)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenValueAt (row,alias): Aun no se ha definido alias. ***\n");
         posCol = (Integer)hiddenAlias.get(alias);
         if  (posCol==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenValueAt (row,alias): El alias "+alias+" no existe. ***\n");
         return getHiddenValueAt(row, posCol);
     }
     
     /**
      * Devuelve el valor que está en la fila y alias de columna especificado
      * @param row número de fila
      * @param alias alias de la columna
      * @return el objeto contenido en la posición indicada
      */
     public Object getValueAt(int row, String alias)
     {
         int posCol;
         if (this.alias==null)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenValueAt (row,alias): Aun no se ha definido alias. ***\n");
         posCol = (Integer)this.alias.get(alias);
         if  (posCol==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenValueAt (row,alias): El alias "+alias+" no existe. ***\n");
         return getValueAt(row, posCol);
     }
    
    /**
     * Establece el valor oculto en la fila y columna especificada
     * @param hiddenValue valor no reflejado en JTable. El valor que se desea establecer
     * @param row número de fila
     * @param col número de columna
     */
    public void setHiddenValueAt(Object hiddenValue, int row, int col) 
    {
        if (hiddenData.size() == data.size() ){ 
            Object []fila=hiddenData.get(row);
            fila[col]=hiddenValue;
        }else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setValueAt (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
    }
    
    /**
      * Devuelve el valor que está en la fila y alias de columna especificado
      * @param hidenValue valor no reflejado en JTable. El valor que se desea establecer
      * @param row número de fila
      * @param alias alias de la columna oculta
      */
     public void setHiddenValueAt(Object hidenValue, int row, String alias)
     {
         int posCol;
         if (this.hiddenAlias==null)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setHiddenValueAt (hidenValue,row,alias): Aun no se ha definido alias oculto. ***\n");
         posCol = (Integer)this.hiddenAlias.get(alias);
         if  (posCol==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setHiddenValueAt (hidenValue,row,alias): El alias oculto "+alias+" no existe. ***\n");
         setHiddenValueAt(hidenValue, row, posCol);
     }
     
     /**
      * Devuelve el valor que está en la fila y alias de columna especificado
      * @param value valor reflejado en JTable. El valor que se desea establecer
      * @param row número de fila
      * @param alias nombre del alias de la columna
      */
     public void setValueAt(Object value, int row, String alias)
     {
         int posCol;
         if (this.alias==null)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setHiddenValueAt (hidenValue,row,alias): Aun no se ha definido alias. ***\n");
         posCol = (Integer)this.alias.get(alias);
         if  (posCol==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setHiddenValueAt (hidenValue,row,alias): El alias "+alias+" no existe. ***\n");
         setValueAt(value, row, posCol);
     }
    
    /**
     * Establece dos valores, el visible y el oculto a la misma vez en la fila y columna especificada respectivamente
     * @param value valor reflejado en JTable. El valor que se desea establecer
     * @param hiddenValue valor no reflejado en JTable. El valor que se desea establecer
     * @param row número de fila
     * @param col número de columna
     * @param hiddencol número de columna oculta
     */
    public void setValueAt(Object value, Object hiddenValue, int row, int col, int hiddencol) {
        if (hiddenData.size() == data.size() ){ 
            setValueAt(value, row, col);
            Object []fila=hiddenData.get(row);
            fila[hiddencol]=hiddenValue;
        }else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setValueAt (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
     }
     
    /**
     * Establece dos valores, el visible y el oculto a la misma vez en la fila y columna especificada respectivamente
     * @param value valor reflejado en JTable. El valor que se desea establecer
     * @param hiddenValue valor no reflejado en JTable. El valor que se desea establecer
     * @param row número de fila
     * @param aliasCol alias de la columna
     * @param aliasHiddencol alias de la columna oculta
     */
    public void setValueAt(Object value, Object hiddenValue, int row, String aliasCol, String aliasHiddencol) {
        int col, hiddencol;
         if (this.alias==null)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setValueAt (value, hidenValue, row, aliasCol, aliasHiddencol): Aun no se ha definido alias. ***\n");
         col = (Integer)this.alias.get(alias);
         if  (col==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setValueAt (value, hidenValue, row, aliasCol, aliasHiddencol): El alias "+alias+" no existe. ***\n");
         
         if (this.hiddenAlias==null)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setValueAt (value, hidenValue, row, aliasCol, aliasHiddencol): Aun no se ha definido alias oculto. ***\n");
         hiddencol = (Integer)this.hiddenAlias.get(alias);
         if  (hiddencol==-1)
             throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setValueAt (value, hidenValue, row, aliasCol, aliasHiddencol): El alias oculto "+alias+" no existe. ***\n");
         
         setValueAt(value, hiddenValue, row, col, hiddencol);
    }
    
     public void addRowEmpty() {
        int numRows = data.size()+1;
        Object[] row = new Object[columnNames.length];
        data.add(row);
        this.fireTableRowsInserted(numRows, numRows);
    }
     
     public void add_Hidden_RowEmpty() {
        Object[] row;
        if (hiddenData.size() == data.size() ){
            if (hiddenData.size()>0)
                row = new Object[hiddenData.get(0).length];
            else
                row = new Object[1];
            hiddenData.add(row);
            addRowEmpty();
        }else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.add_Hidden_RowEmpty (...): Al arreglo de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
    }

    public void addRow(Object[] row) {                                          //Ejemplo: Object[] row = { "Maai", "Superstar", "Play piano", new Integer(5), new Boolean(false)};
        int numRows = data.size();
        data.add(row);
        this.fireTableRowsInserted(numRows, numRows);
    }
    
    /**
     * Agrega una fila con datos para la tabla y otra que no se verá reflejada en tabla
     * @param row fila que se verá reflejada en el JTable
     * @param hiddenRow fila no reflejada en JTable. Si los nombres de estas columnas no son especificados en el constructor, se asignan nombres default. El nombre default se crea así: 'HCol N', donde N es el índice de posición en la columna
     */
    public void addRow(Object[] row, Object[] hiddenRow) {
        int numRows = data.size();
        
        if (hiddenData.size()>0 && hiddenRow.length!=hiddenData.get(0).length)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.addRow (...): El número de columnas de datos ocultos que desea ingresar es diferente al establecido por primera vez. ***\n");
        else if (hiddenData.size() == numRows ){
            if (hiddenColumnNames==null)                                        //Si no se han establecido nombre de columnas ocultas, 
            {
                hiddenColumnNames = new String[hiddenRow.length];
                for (int i=0; i<hiddenRow.length; i++)
                    hiddenColumnNames[i]="HCol "+i;                             //Establecemos los nombres default para las columnas ocultas
            }
            data.add(row);
            hiddenData.add(hiddenRow);
            this.fireTableRowsInserted(numRows, numRows);
        }else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.addRow (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
    }
    
    public int getHiddenColumnCount() {  
        return (hiddenColumnNames==null)?0:hiddenColumnNames.length;
    }
    public int getHiddenRowCount() {  return hiddenData.size();  }
    
    /**
     * Devuelve el nombre asignado a la columna oculta especificada por el parámetro col.
     * @param col Posición de la columna de la cual se desea saber el nombre asignado.
     * @return String Nombre de la columna. 
     */
    public String getHiddenColumnName(int col) {  
        if (hiddenColumnNames!=null)
            return hiddenColumnNames[col];
        else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenColumnName (...): No se han especificado nombres para columnas ocultas ***\n");
    }
    
    /**
     * Devuelve un Arreglo de tipo String con los nombres de las columnas ocultas, si no existieran devuelve null
     * @return String [] Arreglo con los nombres de las columnas ocultas. null si no existen.
     */
    public String[] getHiddenColumnsName() {  
        return hiddenColumnNames;
    }
    
    /**
     * Obtiene una fila con referencia, es decir, lo que se edite en la fila retornada, se reflejará en el model
     * @param row Número de fila que se desea que retorne
     * @return Las celdas que contiene la fila
     */
    public Object[] getRowRef (int row)
    {
        return data.get(row);
    }
    
    /**
     * Obtiene una fila con referencia del hiddenData, es decir, lo que se edite en la fila retornada, se reflejará en el hiddenData
     * @param row Número de fila que se desea que retorne
     * @return Las celdas que contiene la fila
     */
    public Object[] getHiddenRowRef (int row)
    {
        if (hiddenData.size() == data.size() )
            return hiddenData.get(row);
        else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenRowRef (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
    }
    
    /**
     * Obtiene una fila sin referencia, es decir, lo que se edite en la fila retornada, no repercute en el model
     * @param row Número de fila que se desea que retorne
     * @return Las celdas que contiene la fila
     */
    public Object[] getRowSinRef (int row)
    {
        Object datos[]=new Object[data.get(row).length];
        for (int i=0; i<datos.length; i++)
            datos[i]=data.get(row)[i];
        return datos;
    }
    
    /**
     * Obtiene una fila sin referencia del hiddenData, es decir, lo que se edite en la fila retornada, no repercute en el hiddenData
     * @param row Número de fila que se desea que retorne
     * @return Las celdas que contiene la fila
     */
    public Object[] getHiddenRowSinRef (int row)
    {
        if (hiddenData.size() == data.size() )
        {
            Object datos[]=new Object[hiddenData.get(row).length];
            for (int i=0; i<datos.length; i++)
                datos[i]=hiddenData.get(row)[i];
            return datos;
        }else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getHiddenRowSinRef (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
    }
    
    public void removeRows(int []rows) {                                        //Ejemplo:  int rows[] = tblEmployees.getSelectedRows();
        int numRows = rows.length;
        for (int row=numRows-1; row>=0; row--){
            data.remove(rows[row]);
            this.fireTableRowsDeleted(rows[row], rows[row]);
            if (!hiddenData.isEmpty())
                hiddenData.remove(rows[row]);
        }
        if (data.isEmpty())
            selectedRow = -1;
    }

    public void removeRow(int row) {
        data.remove(row);
        if (!hiddenData.isEmpty())
            hiddenData.remove(row);
        this.fireTableRowsDeleted(row, row);
        if (data.isEmpty())
            selectedRow = -1;
    }
    
    public void removeCol (int col)
    {
        removeCol (col, this.columnNames, data);
        this.fireTableStructureChanged();
        if (data.isEmpty())
            selectedRow = -1;
    }
    
    public void removeHiddenCol (int col)
    {
        removeCol (col, this.hiddenColumnNames, hiddenData);
    }
    
    private void removeCol (int col, String[] nombresCol, ArrayList<Object[]> datos)
    {
        Object datosTemp[];
        if (col>=0 && col<=nombresCol.length){
            //-------------- QUITAMOS LA COLUMNA DE DATOS --------------------
            for (int fila = 0; fila<datos.size(); fila++){
                datosTemp = new Object[nombresCol.length-1];
                for (int columna=0, j=0; columna<nombresCol.length; columna++)
                    if (columna!=col)
                        datosTemp[j++]=datos.get(fila)[columna];
                datos.remove(fila);
                datos.add(fila, datosTemp);
            }
            //-------------- QUITAMOS LA COLUMNA DE TITULOS ------------------
            String nombres[] = new String[nombresCol.length-1];
            for (int i=0, j=0; i<nombresCol.length; i++)
                if (i!=col)
                    nombres[j++]=nombresCol[i];
            nombresCol = nombres;
        }
    }

    public void removeAllItems(){
        data.clear();
        if (!hiddenData.isEmpty())
            hiddenData.clear();
        this.fireTableDataChanged();
        selectedRow = -1;
    }
    
    public void replaceAt(int row, Object []fila)
    {
        if (row>=data.size() || row<0)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.reemplaceAt (...): Posicion "+row+" no existe. ***\n");
        else if (fila.length!=data.get(row).length)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.reemplaceAt (...): La longitud de fila[] es diferente a la longitud de la ya establecida. ***\n");
        else{
            Object []fil=data.get(row);
            for (int i=0; i<columnNames.length;i++)
                fil[i]=fila[i];
            fireTableRowsUpdated(row, row);
        }
            
    }
    
    public void replaceAt(int row, Object []fila, Object []filaOculta)
    {
        if (row>=data.size() || row<0)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.replaceAt (...): Posicion "+row+" no existe. ***\n");
        else if (hiddenData.size() != data.size() )
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.replaceAt (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
        else if (filaOculta.length != hiddenData.get(row).length)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.replaceAt (...): La longitud de filaOculta[] es diferente a la longitud a la ya establecida. ***\n");
        else if (fila.length != data.get(row).length)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.replaceAt (...): La longitud de fila[] es diferente a la longitud de la ya establecida. ***\n");
        else{
            Object []fil=hiddenData.get(row);
            for (int i=0; i<hiddenColumnNames.length;i++)
                fil[i]=filaOculta[i];
            replaceAt(row, fila);
        }
    }
    
    /**
     * Devuelve la posición de la fila en la que se halla provocado un evento, como dar click, oprimir tecla, etc
     * @return La última fila en la que hubo evento
     */
    public int getLastRowEvent ()
    {
        return this.rowEvent;
    }
    
    /**
     * Devuelve la posición de la columna en la que se halla provocado un evento, como dar click, oprimir tecla, etc
     * @return La última columna en la que hubo evento
     */
    public int getLastColEvent ()
    {
        return this.colEvent;
    }
    
    /**
     * Devuelve la posición de la celda y columna en la que se halla provocado un evento, como dar click, oprimir tecla, etc
     * @return La última fila y columna en la que hubo evento
     */
    public int[] getLastRowColEvent ()
    {
        int celda[]={this.rowEvent,this.colEvent};
        return celda;
    }
    
    public void interchange(int rowOrigen, int rowDestino)
    {
        if (rowOrigen<data.size() && rowDestino<data.size()){
            int n=columnNames.length;
            Object []filOrg= data.get(rowOrigen);
            Object []filDest=data.get(rowDestino);
            Object []filTemp=new Object[n];
            
            System.arraycopy(filOrg, 0, filTemp, 0, n);
            System.arraycopy(filDest, 0, filOrg, 0, n);
            fireTableRowsUpdated(rowOrigen, rowOrigen);
            System.arraycopy(filTemp, 0, filDest, 0, n);
            fireTableRowsUpdated(rowDestino, rowDestino);
            if (!hiddenData.isEmpty())
                intercambiarHiddenRows (rowOrigen, rowDestino);
        }else{
            if (rowOrigen>=data.size())
                throw new IllegalArgumentException ("\n\n*** Error en ModeloDeTabla.interchange (...): En el intercambio de filas, la posición de fila origen sobrepasa a las existentes. ***\n"+columnNames.length);
            if (rowDestino>=data.size())
                throw new IllegalArgumentException ("\n\n*** Error en ModeloDeTabla.interchange (...): En el intercambio de filas, la posición de fila destino sobrepasa a las existentes. ***\n"+columnNames.length);
        }
    }
    
    private void intercambiarHiddenRows (int rowOrigen, int rowDestino)
    {
        int n=0;
        if (hiddenData.size() == data.size() )
        {
            Object []filOrg=hiddenData.get(rowOrigen);
            Object []filDest=hiddenData.get(rowDestino);
            Object []filTemp=new Object[(n=hiddenData.get(rowOrigen).length)];
        
            System.arraycopy(filOrg, 0, filTemp, 0, n);
            System.arraycopy(filDest, 0, filOrg, 0, n);
            System.arraycopy(filTemp, 0, filDest, 0, n);
        }else
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.intercambiarHiddenRows (...): Al arrego de datos acultos no se le han introducido datos al mismo tiempo que al vector para JTable ***\n");
    }
    
    /**
     * Retorna el índice de la primer ocurrencia del elemento especificado
     * en la columna especificada, o -1 si esta columna no contiene el elemento.
     * Más específico, retorna el índice más pequeño <tt>i</tt> tal que
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * o -1 if no está el elemento.
     */
    public int indexOf(Object o, int col)
    {
        return seekAt (data, o, col);
    }
    
    /**
     * Retorna el índice de la primer ocurrencia del elemento especificado del hiddenData
     * en la columna especificada, o -1 si esta columna no contiene el elemento.
     * Más específico, retorna el índice más pequeño <tt>i</tt> tal que
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * o -1 if no está el elemento.
     */
    public int indexOfHidden(Object o, int col)
    {
        return seekAt (hiddenData, o, col);
    }
    
    private int seekAt (ArrayList<Object[]> datos, Object o, int col)
    {
        int size = datos.size();
        
        if (o == null) {
	    for (int i = 0; i < size; i++)
		if (datos.get(i)[col] ==null)
		    return i;
	} else {
	    for (int i = 0; i < size; i++)
		if (o.equals(datos.get(i)[col]))
		    return i;
	}
        return -1;
    }
    
    /**Establece el ancho de cada columna, NOTA Siempre mandar a llamar despues de hacer el setModel
     * @param tabla La tabla a la que se le editarán los anchos de columna
     * @param anchos Un arreglo que contendrá el tamaño para cada columna de la tabla
    */
    public void setAnchoDeColumnas (JTable tabla, int []anchos)
    {
        if (anchos.length!=tabla.getColumnCount())
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setAnchoDeColumnas (...): El arreglo anchos de columna con "+anchos.length+" elementos no coincide con las "+tabla.getColumnCount()+" columnas de la tabla "+tabla.getName()+"***\n");
        for(int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
            //tabla.getColumnModel().getColumn(i).setResizable(false);
        }
    }
    
    public void setComponenteEnColumna (JTable tabla, javax.swing.JComponent componente, int columna){
        try {
            javax.swing.table.TableColumn categoriaCoposturaColumn = tabla.getColumnModel().getColumn(columna);        

            if (componente.getClass().equals(javax.swing.JCheckBox.class))
                categoriaCoposturaColumn.setCellEditor(new javax.swing.DefaultCellEditor((javax.swing.JCheckBox)componente));
            else if (componente.getClass().equals(javax.swing.JComboBox.class))
                categoriaCoposturaColumn.setCellEditor(new javax.swing.DefaultCellEditor((javax.swing.JComboBox)componente));
            else if (componente.getClass().equals(javax.swing.JTextField.class))
                categoriaCoposturaColumn.setCellEditor(new javax.swing.DefaultCellEditor((javax.swing.JTextField)componente));
        }catch (ArrayIndexOutOfBoundsException ex){
            throw new IllegalArgumentException ("\n\n*** Error en ModeloDeTabla.setComponenteEnColumna (...): El componente que intenta establecer en la columna "+columna+" no se puede establecer, porque la columna no existe. ***\n");
        }
    }
    
    //******************* Pone un scroll horizontal a un JTable *******************
    public void setScrollHorizontal (JTable tabla,javax.swing.JScrollPane scrollPane)
    {
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //JScrollPane scrollPane = new JScrollPane (tabla);
        //javax.swing.JViewport viewPort = new javax.swing.JViewport();
        //scrollPane.setRowHeaderView(viewPort);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    }
    
    /**
     * 
     * @param tabla La tabla a la que se le aplicará la alineación de columnas
     * @param columnas Un vector con las posiciones de columnas que se le aplicará la alineación
     * @param alineacion Una constante definida en javax.swing.SwingConstants.[...]
     */
    public void alinearColumnas (JTable tabla, int []columnas, int alineacion)
    {
        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
        tcr.setHorizontalAlignment(alineacion);
        for (int i=0; i<tabla.getColumnCount(); i++)
            for (int j=0; j<columnas.length; j++)
                if (i==columnas[j])
                    tabla.getColumnModel().getColumn(i).setCellRenderer(tcr);
    }
    
    /**
     * Activa el evento que escucha la tabla cuando cambia la selección de una fila
     * @param listSelectionModel Recibe el getSelectionModel() del JTable
     */
    public void setSelectionListener (ListSelectionModel listSelectionModel)
    {
        this.listSelectionModel = listSelectionModel;
        activarEscuchaDeSeleccion ();
    }
    
    /**
     * Devuelve la posición de la primer fila seleccionada
     * @return La posición de la fila seleccionada actualmente.
     */
    public int getSelectedRow ()
    {
        if (this.listSelectionModel == null)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getSelectedRow (...): Aún no ha establecido el SelectionListener mediante el método setSelectionListener()***\n");
        if (listSelectionModel.isSelectionEmpty())
            this.selectedRow = -1;
        return this.selectedRow;
    }
    
    /**
     * Devuelve la posición de la primer fila que estaba seleccionada anteriormente
     * @return La posición de la fila seleccionada anteriormente.
     */
    public int getLastSelectedRow ()
    {
        if (this.listSelectionModel == null)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.getSelectedRow (...): Aún no ha establecido el SelectionListener mediante el método setSelectionListener()***\n");
        if (listSelectionModel.isSelectionEmpty()){
            this.selectedRow = -1;
            this.lastSelectedRow = -1;
        }
        return this.lastSelectedRow;
    }
    
    /**
     * Esatblece la selección de una fila en el JTable
     * @param index Posición de la fila a seleccionar.
     */
    public void setSelectedRow (int index)
    {
        if (this.listSelectionModel == null)
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setSelectedRow (...): Aún no ha establecido el SelectionListener mediante el método setSelectionListener()***\n");
        if (index<0 || index>=data.size()){
            throw new IllegalArgumentException("\n\n*** Error en ModeloDeTabla.setSelectedRow (...): La selección que intenta establecer está fuera del rango de número de filas que contiene la tabla ***\n");
        }
        listSelectionModel.setSelectionInterval(index, index);
    }
    
    private void activarEscuchaDeSeleccion ()
    {
        this.listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()){
                    selectedRow = -1;
                    lastSelectedRow=-1;
                }else {
                    selectedRow = lsm.getMinSelectionIndex();
                    lastSelectedRow = e.getLastIndex();
                    //int selectedRow2 = lsm.getMaxSelectionIndex();
                    //System.out.println("Fila sel min: "+selectedRow+", Fila sel max"+selectedRow2);
                    //System.out.println("Selección lastIndex="+e.getLastIndex()+",  Selección firstIndex="+e.getFirstIndex());
                }
            }
        });
    }
}