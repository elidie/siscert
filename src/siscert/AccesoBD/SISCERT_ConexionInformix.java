/*
 * SISCERT_ConexionInformix.java
 * 
 * Creado por Ing. Maai Nolasco Sanchez.
 *
 * Creado el 31/Oct/2015 12:33
 * última edición: 08/Ene/18 
 * Versión: 3.0.1.e
 *
 */

package siscert.AccesoBD;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 */
class SISCERT_ConexionInformix  {
    //----- Para trabajar con los queries -----
    private Connection conn = null;
    protected Statement stm,stm1;
    protected ResultSet rs,rs1;
    //---------------------------------------------
    private String defaultConnectionType, driver, host, hostODBC, hostInternet, hostLocal, port, db, serverName, user, password, urlDataBase;
    private boolean globalTransactionStarted;
    
    private final String CONEXION_LOCAL = "LOCAL";
    private final String CONEXION_INTERNET = "INTERNET";
    private final String CONEXION_ODBC = "ODBC";

    protected SISCERT_ConexionInformix ()
    {
        ConnectionSettings ();
    }
    
    protected SISCERT_ConexionInformix (String typeConnection) throws Exception
    {
        ConnectionSettings ();
        this.setConnectionType(typeConnection);
    }
    
    private void ConnectionSettings ()
    {
        this.globalTransactionStarted = false;
        
        this.conn = null;
        this.defaultConnectionType = this.CONEXION_LOCAL;
        
        //---------- Driver Informix
        this.driver = "com.informix.jdbc.IfxDriver";
        
        this.hostODBC = "jdbc:odbc:since";
        this.hostInternet = "187.217.217.2";
        //------------------ Datos para conexón a Informix -------------------
        /*this.hostLocal = "172.16.4.4";
        this.serverName = "sinceoax_tcp";
        this.port = "1550";
        this.user = "informix";
        this.password = "dai1314";
        this.db = "since";*/
        //------------------------ Configuración linux ------------------------
        this.hostLocal = "172.16.4.7";
        this.port = "1560";
        this.db = "since";
        this.serverName = "ieepo_tcp4";
        this.user = "informix";
        this.password = "m1x17info";
        
    }

    protected void connect ()throws SQLException
    {
        try {
            if (this.defaultConnectionType.equals(this.CONEXION_LOCAL))
                this.host = this.hostLocal;
            else if (this.defaultConnectionType.equals(this.CONEXION_INTERNET))
                this.host = this.hostInternet;
            else if (this.defaultConnectionType.equals(this.CONEXION_ODBC))
                this.urlDataBase = this.hostODBC;

            if (globalTransactionStarted)
                throw new IllegalArgumentException("\n\n*** Error al crear nueva conexion, no ha cerrado la transacción global previamente iniciada. ***\n");
            
            Class.forName(this.driver);                                         //cargamos el driver de conexión
            
            if (!this.defaultConnectionType.equals(this.CONEXION_ODBC))
                this.urlDataBase = "jdbc:informix-sqli://"+this.host+":"+this.port+"/"+this.db+":INFORMIXSERVER="+serverName+";";
            
            this.conn = DriverManager.getConnection(this.urlDataBase, this.user, this.password);

            if (this.conn != null){
                this.stm= this.conn.createStatement();
                System.out.println("Conexión a base de datos " + this.db + "... Ok");
            }
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "No se encontró el driver de conexión "+this.driver+"."+ex, "Error",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    protected void connectWithTransaction () throws SQLException, ClassNotFoundException
    {
        connect ();
        startGlobalTransaction ();
    }
    
    // ------------------ cerrar objetos Statement y Connection
    protected void closeConnection ()
    {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) {  }
            rs = null;
        }
        if (stm != null) {
            try { stm.close(); } catch (SQLException e) {  }
            stm = null;
        }
        
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) {  }
            conn = null;
        }
    }
    
    protected void closeConnectionWithTransaction (boolean commit) throws SQLException
    {
        closeGlobalTransaction (commit);
        closeConnection ();
    }
    
    protected Statement getNewStatement () throws SQLException
    {
        return this.conn.createStatement();
    }
    
    protected void closeStatement (Statement statement, ResultSet resultSet)
    {
        if (resultSet != null) {
            try { resultSet.close(); } catch (SQLException e) { }
            resultSet = null;
        }
        if (statement != null) {
            try { statement.close(); } catch (SQLException e) { }
            statement = null;
        }
    }
   
    protected Connection getConnection ()
    {
        return conn;
    }
    
    protected String getDefaultConectionType ()
    {
        return this.defaultConnectionType;
    }
    
    protected void setConnectionTypeAsLOCAL ()
    {
        this.defaultConnectionType = this.CONEXION_LOCAL;
    }

    protected void setConnectionTypeAsINTERNET ()
    {
        this.defaultConnectionType = this.CONEXION_INTERNET;
    }

    protected void setConnectionTypeAsODBC ()
    {
        this.defaultConnectionType = this.CONEXION_ODBC;
    }
    
    protected final void setConnectionType (String typeConnection) throws Exception
    {
        if ((""+typeConnection).equals(this.CONEXION_LOCAL) || (""+typeConnection).equals(this.CONEXION_INTERNET) || (""+typeConnection).equals(this.CONEXION_ODBC))
            this.defaultConnectionType = typeConnection;
        else
            throw new Exception("TIPO DE CONEXIÓN NO VÁLIDA");
    }
    
    private void startGlobalTransaction () throws SQLException
    {
        globalTransactionStarted = true;
        conn.setAutoCommit(false);
        stm = conn.createStatement();
    }
    
    private void closeGlobalTransaction (boolean commit) throws SQLException
    {
        if (globalTransactionStarted){
            if (commit)
                conn.commit();
            else
                conn.rollback();
            globalTransactionStarted = false;
        }
    }
    
    protected void doCommit () throws SQLException
    {
        conn.commit();
    }
    
    protected void doRollback () throws SQLException
    {
        conn.rollback();
    }

    protected String encodeBase64(String cadena) throws java.io.UnsupportedEncodingException{
        String originalString = cadena;
        byte[] sendBytes = originalString.getBytes( "8859_1"/* encoding */ );
        Base64 base64 = new Base64();
        String encoded = base64.encode( sendBytes );
        return encoded;
    }
    
    protected String decodeBase64 (String cadena)
    {
        String decodificado="";
        byte[] recivedBytes;
        Base64 base64 = new Base64();
        recivedBytes = base64.decode(cadena );            
        for (int i=0; i<recivedBytes.length; i++)
            decodificado+=(char)recivedBytes[i];
        return decodificado;        
    }
//*****************************************************************************************************************************************
//*****************************************************************************************************************************************
//*****************************************************************************************************************************************
    
    protected String getTableId (String idABuscar, String tabla, String condicion)  throws SQLException
    {
        String id="";
        rs = stm.executeQuery("SELECT " + idABuscar + " as numero FROM " + tabla + " WHERE "+condicion);
        if (rs.next())
            id=rs.getString("numero");  //Se extrae el número para id
        return id;
    }
    
    protected String getLastTableId (String idABuscar, String tabla)  throws SQLException
    {
        String id;
        int temp;

        rs = stm.executeQuery("SELECT MAX(" + idABuscar + ") as numero FROM " + tabla);
        rs.next();
        id=rs.getString("numero");  //Se extrae el número para id

        if(id==null) {  id="1";  } //Si no hay datos en la tabla se asigna 1
        //else{ temp = Integer.parseInt(id) + 1; id = "" + temp; }  //Si no le sumamos (+1)

        return id;
    }
    
    protected int getNewNumTableId (String columna, String tabla)  throws SQLException
    {
        int id;

        rs = stm.executeQuery("SELECT MAX(" + columna + ") as numero FROM " + tabla);
        rs.next();
        id=rs.getInt("numero")+1;  //Se extrae el número para id
        return id;
    }
    
    protected String getData(String consulta) throws SQLException
    {
        String dato = "";
        rs = stm.executeQuery(consulta);
        if (rs.next())
            dato=rs.getString(1);  //Se extrae el número para id
        return dato;
    }
    
    protected String makeInsertQuery (String tabla, Object datos[][])
    {
        String qryColumnNames="", qryDataColumns="";
        
        for (int i=0; i<datos.length; i++)
        {
            qryColumnNames+= ((i==0)?"":",") + datos[i][0];
            qryDataColumns+= ((i==0)?"":",") + datos[i][1];
        }
        
        return "INSERT INTO "+tabla+" ("+qryColumnNames + ") VALUES ("+qryDataColumns+")";
    }
    
    protected void qryToModel (ResultSet resultSet, String[] colsOcultas, boolean hacerTrim, int convertNull, SISCERT_ModeloDeTabla modelTabla) throws SQLException
    {
        ArrayList<String> tempHiddenColsToSel=null;
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int nc=0, numCols=rsmd.getColumnCount();
        int nco=0, numColsOcultas=0, numColsVisibles;
        String dato, columna;
        
        Object[] fila, filaOculta=null;
        
        if (colsOcultas!=null){
            tempHiddenColsToSel = new ArrayList<String>();
            tempHiddenColsToSel.addAll(Arrays.asList(colsOcultas));
            numColsOcultas=colsOcultas.length;
        }
        numColsVisibles = numCols - numColsOcultas;
        
        if (resultSet.next())
        {
            fila = new Object[numColsVisibles];
            if (colsOcultas!=null) filaOculta = new Object[numColsOcultas];
            
            for (int c=1; c<=numCols; c++){
                columna = rsmd.getColumnName(c).toLowerCase();
                dato = resultSet.getString(c);
                if (hacerTrim && dato!=null)
                    dato=dato.trim();
                if ( convertNull != 0 ){                                        //Si trae cero, hacer caso omiso del trato con nulls 
                    if (convertNull == 1 )                                      //Convierte el null a texto
                        dato=""+dato;
                    else if (convertNull == 2  && dato==null)                   //Convierte el null a cadena vacía
                        dato="";
                }
                
                if (colsOcultas!=null && nco<numColsOcultas && tempHiddenColsToSel.indexOf(columna)!=-1) //Si se especificó un filtro de columnas a extraer
                    filaOculta[nco] = dato;
                else                                                            //Si no hay filtro, extraemos todas
                    fila[nc] = dato;
            }
            
            if (colsOcultas==null)
                modelTabla.addRow(fila);
            else
                modelTabla.addRow(fila,filaOculta);
        }
    }
    
    protected ArrayList<Map> qryToArrlmap (ResultSet resultSet, String[] colsASeleccionar, boolean hacerTrim, int convertNull) throws SQLException
    {
        ArrayList<String> colsToSel=null;
        Map fila;
        ArrayList<Map> QQryToArrmap = new ArrayList<Map>();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int numCols=rsmd.getColumnCount();
        String dato, columna;
        
        if (colsASeleccionar!=null){
            colsToSel = new ArrayList<String>();
            colsToSel.addAll(Arrays.asList(colsASeleccionar));
        }
        
        while (resultSet.next())                                                //Recorremos todas las filas
        {
            fila = new HashMap();
            for (int posCol=1; posCol<=numCols; posCol++){                      //Para cada columna
                columna = rsmd.getColumnName(posCol).toLowerCase();
                dato = resultSet.getString(posCol);
                if (hacerTrim && dato!=null)
                    dato=dato.trim();
                if ( convertNull != 0 ){                                        //Si trae cero, hacer caso omiso del trato con nulls 
                    if (convertNull == 1 )                                      //Convierte el null a texto
                        dato=""+dato;
                    else if (convertNull == 2 && dato==null)                    //Convierte el null a cadena vacía
                        dato="";
                }
                if (colsASeleccionar!=null){                                    //Si se especificó un filtro de columnas a extraer
                    if (colsToSel.indexOf(columna)!=-1)                         //Hacemos el filtro
                        fila.put(columna, dato);
                }else                                                           //Si no hay filtro, extraemos todas
                    fila.put(columna, dato);
            }
            QQryToArrmap.add(fila);
        }
        return QQryToArrmap;
    }
    
    /**
     * 
     * @param resultSet El resultset del query
     * @param colsASeleccionar Un arreglo de String que contiene los nombres de columnas a ser extraídos del query
     * @param hacerTrim Si se desea que los datos de tipo String se le haga trim()
     * @param convertNull Número entero que indica el tratamiento que se le hará al dato cuando devuelva null:
     *                  Si se especifica: 0 - Hace caso omiso del trato con nulls.
     *                                    1 - Convierte el null a texto ("null").
     *                                    2 - Convierte el null a cadena vacía ("").
     * @return
     * @throws SQLException 
     */
    protected Map qryToMap (ResultSet resultSet, String[] colsASeleccionar, boolean hacerTrim, int convertNull) throws SQLException
    {
        ArrayList<String> colsToSel=null;
        Map QQryToMap = new HashMap();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int numCols=rsmd.getColumnCount();
        String dato, columna;
        
        if (colsASeleccionar!=null){
            colsToSel = new ArrayList<String>();
            colsToSel.addAll(Arrays.asList(colsASeleccionar));
        }
        
        if (resultSet.next())
        {
            for (int c=1; c<=numCols; c++){
                columna = rsmd.getColumnName(c).toLowerCase();
                dato = resultSet.getString(c);
                if (hacerTrim && dato!=null)
                    dato=dato.trim();
                if ( convertNull != 0 ){                                        //Si trae cero, hacer caso omiso del trato con nulls 
                    if (convertNull == 1 )                                      //Convierte el null a texto
                        dato=""+dato;
                    else if (convertNull == 2  && dato==null)                   //Convierte el null a cadena vacía
                        dato="";
                }
                if (colsASeleccionar!=null){                                    //Si se especificó un filtro de columnas a extraer
                    if (colsToSel.indexOf(columna)!=-1)                         //Hacemos el filtro
                        QQryToMap.put(columna, dato);
                }else                                                           //Si no hay filtro, extraemos todas
                    QQryToMap.put(columna, dato);
            }
        }
        return QQryToMap;
    }
}