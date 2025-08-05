/*********** TABLAS USADAS ********
1.- usuarios
2.- escuela
3.- FOLIOS_IMPRE
4.- alumnogrado

5.-  siscert_certificacion
6.-  siscert_folimpre
7.-  siscert_folimpre_vars
8.-  siscert_impcoordenadas
9.-  siscert_impformato
10.- siscert_impleyendas
11.- siscert_imptipografia
12.- siscert_leyendas
13.- siscert_planmodalidad_beta
14.- siscert_sisver
15.- siscert_variables
***********************************/

/*
 * SISCERTConexionInformix.java
 *
 * Última edición: el 11/Nov/2011, 20:48:33
 * v3.0
 */

package siscert.AccesoBD;

import java.awt.Rectangle;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import siscert.ClasesGlobales.SISCERT_CoordenadasImpre;
import siscert.ClasesGlobales.SISCERT_GlobalMethods;
import siscert.ClasesGlobales.SISCERT_ModeloDeTabla;

public class SISCERT_QueriesInformix extends SISCERT_ConexionInformix{
    public SISCERT_QueriesInformix ()
    {
        super();
    }
    public SISCERT_QueriesInformix (String tipoConexion) throws Exception
    {
        super(tipoConexion);
    }
    
    public void setTipoConexionAsLOCAL(){ setConnectionTypeAsLOCAL(); }
    public void setTipoConexionAsINTERNET(){ setConnectionTypeAsINTERNET (); }
    public void setTipoConexionAsODBC(){ setConnectionTypeAsODBC (); }
    public void setTipoConexion ( String tipoConexion ) throws Exception { setConnectionType(tipoConexion); }
    public String getDefaultTipoConexion(){ return getDefaultConectionType(); }
    public void conectar()throws SQLException { connect (); }
    public Connection getConexion () { return getConnection (); }
    public void cerrarConexion()throws SQLException{ closeConnection(); }
    public void conectarConTransaccion()throws SQLException, ClassNotFoundException { connectWithTransaction(); }
    public void cerrarConexionConTransaccion(boolean commit)throws SQLException { closeConnectionWithTransaction(commit); }
    public void hacerCommit () throws SQLException{  doCommit(); }
    public void hacerRollback () throws SQLException{  doRollback(); }
    public String codificarABase64(String cadena) throws UnsupportedEncodingException { return encodeBase64(cadena); }
    public String decodificarBase64(String cadena) { return decodeBase64(cadena); }
    public String obtenerDato(String cadena) throws SQLException { return getData(cadena); }
    public String crearQueryInsert (String tabla, Object datos[][]) {  return makeInsertQuery(tabla, datos);  }

//***********************************************************************************************************************************
//************************************ METODOS USADOS EN EL FORMULARIO LoginUser ****************************************************
//***********************************************************************************************************************************
    
    public void indentificarUsuario (String usuario, String passwdUsr, SISCERT_GlobalMethods global) throws UnsupportedEncodingException, SQLException, Exception
    {
        String passDecodificada;     
        
        this.rs=this.stm.executeQuery("SELECT u.idusuario, u.loginuser, u.pasword, su.cveunidad " +
                                      "FROM usuarios u, siscert_usuarios su " +
                                      "WHERE u.idusuario=su.idusuario AND u.loginuser = '"+usuario+"'");
        if (this.rs.next()) {
            passDecodificada = decodificarBase64 (this.rs.getString("pasword"));//Decodificamos la contraseña de la BD a formato Base64
            if (passwdUsr.equals(passDecodificada)) {
                global.idcapturista = this.rs.getString("idusuario").trim();
                global.capturista = this.rs.getString("loginuser").trim();
                global.cveunidad=this.rs.getString("cveunidad").trim();
            }
        } else
            throw new Exception("SIN_PERMISO");
    }
    
//*****************************************************************************************************************************************
//************************************ METODOS USADOS EN EL FORMULARIO VentanaPrincipal ***************************************************
//*****************************************************************************************************************************************
    public void getCveunidades (JComboBox cveunidades, boolean verCinco9) throws SQLException
    {
        String cveunidad;
        rs = stm.executeQuery("SELECT cveunidad FROM siscert_sisver WHERE cveunidad!='ADMIN' ORDER BY cveunidad");
        while (rs.next()){
            cveunidad = rs.getString("cveunidad");
            if (!cveunidad.equals("CINCO9") || verCinco9)
                cveunidades.addItem(rs.getString("cveunidad"));
        }
    }
    
    public void getFormatosCertActivos (ArrayList<String[]> formatosCertActivos, JComboBox formatos, String cveunidad) throws SQLException
    {
        String fila [];
        String filtro="or idformato=9";
        /*if (cveunidad.equals("DSRVAL"))
            filtro="OR formato='EDUCBAS_JUL14'";*/
        rs = stm.executeQuery("SELECT ordenvisualizacion, idformato, formato FROM siscert_impformato WHERE estatus='A' "+filtro+" AND visible='t' ORDER BY ordenvisualizacion");
        while (rs.next()){
            fila = new String[2];
            fila[0]=rs.getString("idformato");
            fila[1]=rs.getString("formato");
            formatos.addItem(fila[1]);
            formatosCertActivos.add(fila);
        }
    }
                //----------- Hace consulta buscando que contenga caracteres en la curp segÃºn una cadena dada
    public void selecAlumnoSISCERTFiltro (SISCERT_ModeloDeTabla modelSISCERT, String filtro,String caso, String cveUnidad, int cveplan, String idformato, boolean cambiarUnidad) throws SQLException, Exception
    {
        String atributoABuscar="", filtroUnidad="", cicescinilib, orderBy="c.idformato, c.cicescinilib, c.numsolicitud";
        String[] part, part2;
        Object[] fila, filaOculta;
        
        int numsol, posFila=1, numColumnas=modelSISCERT.getColumnCount(), numColumasOcultas = modelSISCERT.getHiddenColumnCount();
        
        if (caso.equals("CONTROL")) {
            part=filtro.trim().split("-");
            atributoABuscar = "idcertiregion >='"+part[0]+"' AND idcertiregion <='"+part[1]+"'"+" ";
            orderBy = "c.idcertiregion ";
        } else if (caso.equals("CURP"))
            atributoABuscar = "curp LIKE '"+filtro.toUpperCase()+"%'";
        else if (caso.equals("NOMBRE")) {
            atributoABuscar=formatSeekToSubquery (filtro);
        } else if (caso.equals("SOLICITUD")) {
            part=filtro.trim().split("-");
            part2=part[1].trim().split("/");                                                            
            atributoABuscar = "numsolicitud BETWEEN "+part[0]+" AND "+part2[0]+" AND cicescinilib="+part2[1]+" "; //AND c.idformato=" +idformato+"
        }
        
        if (cveUnidad.equals("DSRVAL"))                                      //Damos permisos para que Ãºnicamente VALLES CENTRALES tenga acceso a todas las regiones 
            filtroUnidad = " AND (cveunidad ='"+cveUnidad+"' OR cveunidad ='CINCO9') ";
        else
            filtroUnidad = " AND cveunidad ='"+cveUnidad+"' ";

        rs=stm.executeQuery("SELECT  idcertificacion, idcertiregion, NVL(numsolicitud,'') AS numsol, foja, NVL(cicescinilib,'') AS cicescinilib, c.idalu, nombre, "
                                + "NVL(apepat,'') AS apepat, NVL(apemat,'') AS apemat, curp, c.idformato, c.cicinilib_cert, f.descripcion, f.formato, "
                                + "CASE WHEN c.numsolicitud<0 THEN '' ELSE cast(c.numsolicitud as varchar(10)) END AS numsolicitud, c.cicescinilib, "
                                + "c.numsolicitud, "
                                + "NVL((SELECT fi.folionum FROM siscert_folimpre fi, siscert_firmaelec fe " 
                                    + " WHERE fi.cicescinilib = fe.cicescinilib AND fi.idfolimpre = fe.idfolimpre AND fi.idalu=fe.idalu " 
                                    + " AND fi.idalu=c.idalu AND fi.idcertificacion = c.idcertificacion AND fi.cveplan=c.cveplan "
                                    + " AND fi.numsolicitud = c.numsolicitud AND foliodigital is not null),'') AS folio "
                            + "FROM siscert_certificacion c, siscert_impformato f "
                            + "WHERE f.idformato=c.idformato AND "+atributoABuscar + " " 
                            + (cambiarUnidad ? " " : filtroUnidad)
                            + " AND cveplan = "+cveplan + " "
                            + "ORDER BY "+orderBy);
        while (rs.next()){
            fila = new Object[numColumnas];
            filaOculta = new Object[numColumasOcultas];
            
            filaOculta[0] = rs.getString("idcertificacion");
            
            fila[0]=""+(posFila++);
            fila[1]=rs.getString("idcertiregion");
            fila[2]=( (numsol=rs.getInt("numsol"))<=0 )?"": ""+numsol;
            fila[3]=rs.getString("foja");
            fila[4]=((cicescinilib=rs.getString("cicescinilib"))==null || cicescinilib.equals(""))?"":Integer.parseInt(cicescinilib)+"-" + (Integer.parseInt(cicescinilib)+1);//""+(cicescini=rs.getInt("cicescinilib"))+" - " + (cicescini+1);
            fila[5]=rs.getString("idalu");
            fila[6]=rs.getString("nombre");
            fila[7]=rs.getString("apepat");
            fila[8]=rs.getString("apemat");
            fila[9]=rs.getString("curp");
            fila[10]=rs.getInt("idformato")>=8?(rs.getInt("cicinilib_cert")==2012?"EDUCACIÓN BÁSICA":"NIVEL EDUCATIVO"):rs.getString("descripcion");
            fila[11]=rs.getString("formato");
            fila[12]=rs.getString("folio");
            modelSISCERT.addRow(fila, filaOculta);
        }
    }
    public String buscarEnSICEERT(String curp, String cveplan, String idAluSICEEB, boolean verUnidades) throws SQLException
    {
        String mensaje="";        
            
        String idcertiregion;
        rs = stm.executeQuery("SELECT idcertificacion, idcertiregion, cveunidad FROM siscert_certificacion "
                + " WHERE curp='"+curp.toUpperCase()+"' AND cveplan="+cveplan 
                + " AND idalu = "+idAluSICEEB );
        while (rs.next()) {
            idcertiregion = rs.getString("idcertiregion");            
            mensaje = (verUnidades ? "N.Ctrl: "+idcertiregion+", Unidad: "+rs.getString("cveunidad") : 
                    "N.Ctrl: "+idcertiregion+", curp: " + curp + "\n");
        }
        return mensaje;            
    }
    
                //----------- Hace consulta buscando que contenga caracteres en la curp segÃºn una cadena dada
    public int selecDuplicadosImpresosFiltro (SISCERT_ModeloDeTabla modelSISCERT, String filtro,String caso, String cveUnidad, int cveplan, String capturista, boolean verUnidades, int numLlamada) throws SQLException, Exception
    {
        String atributoABuscar="", filtroUnidad;
        String[] part, part2;
        Object []fila;
        int numColumnas=modelSISCERT.getColumnCount(), registros=0;
        
        if (caso.equals("CONTROL"))
            atributoABuscar = "fi.idalu ="+filtro.trim();
        else if (caso.equals("CURP"))
            atributoABuscar = "curp LIKE '"+filtro.toUpperCase()+"%'";
        else if (caso.equals("NOMBRE")){
            atributoABuscar=formatSeekToSubquery (filtro).replace("apepat", "primerape").replace("apemat", "segundoape");
        }else if (caso.equals("FOLIOS")){
            part=filtro.trim().split("-");
            atributoABuscar = "foliolet='"+part[0].toUpperCase().charAt(0)+"' AND folionum BETWEEN "+part[0].substring(1)+" AND "+part[1];
        }else if (caso.equals("SOLICITUD")){
            part=filtro.trim().split("-");
            part2=part[1].trim().split("/");
            atributoABuscar = "numsolicitud BETWEEN "+part[0]+" AND "+part2[0]+" AND cicescinilib="+part2[1];
        }

        if (cveUnidad.equals("DSRVAL")) {                                     //Damos permisos para que Ãºnicamente VALLES CENTRALES tenga acceso a todas las regiones 
            if(verUnidades)
                filtroUnidad = "";
            else
                filtroUnidad = " AND (cveunidad ='"+cveUnidad+"' OR cveunidad = 'CINCO9') ";
        } else
            filtroUnidad = " AND cveunidad ='"+cveUnidad+"' ";
        if(numLlamada==1)
            rs=stm.executeQuery("SELECT  fi.idfolimpre, fi.idalu, fi.numsolicitud, fi.cicescinilib, fi.foliolet, fi.folionum, " +
                        "fi.nombre, fi.primerape, fi.segundoape, fi.curp, fi.cicescini, fi.prom_educprim, fi.promedio, " +
                        "fi.prom_educbasic, fi.folio, fi.cct, fi.fecha, fi.fechainsert, " +
                        "fi.usuario, fe.fechatimbradoieepo, fe.foliodigital " +
                        "FROM siscert_folimpre fi INNER JOIN siscert_firmaelec fe " +
                            "ON (fi.idalu = fe.idalu AND fi.idfolimpre = fe.idfolimpre AND fi.folionum = fe.folionum AND fi.cicescinilib = fe.cicescinilib) " +
                        "WHERE "+atributoABuscar+" "+filtroUnidad+" AND cveplan = "+cveplan + " " +
                        "AND estatus_firma=100 AND foliodigital is not null " +        
                        "ORDER BY fi.idformato, fi.cicescini, fi.numsolicitud");        
        else
            rs=stm.executeQuery("SELECT  fi.idfolimpre, fi.idalu, fi.numsolicitud, fi.foliolet, fi.folionum, " +
                        "fi.nombre, fi.primerape, fi.segundoape, fi.curp, fi.cicescini, fi.cicescfin, fi.prom_educprim, fi.promedio, " +
                        "fi.prom_educbasic, fi.folio, fi.cct, fi.fecha, fi.fechainsert, " +
                        "fi.usuario, '' AS fechatimbradoieepo, '' AS foliodigital " +
                        "FROM siscert_folimpre fi LEFT JOIN siscert_firmaelec fe " +
                            "ON (fi.idalu = fe.idalu AND fi.idfolimpre = fe.idfolimpre AND fi.folionum = fe.folionum AND fi.cicescinilib = fe.cicescinilib " +
                        "AND estatus_firma=100 AND foliodigital is not null) " +
                        "WHERE "+atributoABuscar+" "+filtroUnidad+" AND cveplan = "+cveplan + " " +
                        "AND fi.cicescinilib < 2017" +        
                        "ORDER BY fi.idformato, fi.cicescini, fi.numsolicitud");        
        
        while (rs.next()){
            fila = new Object[numColumnas];
            for (int i=1; i<=numColumnas; i++)
                fila[i-1]=rs.getString(i);
            modelSISCERT.addRow(fila);
            registros += 1;
        }
        return registros;
    }

    public void selecAlumnoSICEEBFiltro (SISCERT_ModeloDeTabla modelSICEEB, String filtro, String caso, String cveunidad, int cveplan) throws SQLException, Exception
    {
        String atributoABuscar="", filtroUnidad ="";
        Object []fila;
        int numColumnas=modelSICEEB.getColumnCount();

        if (caso.equals("CONTROL"))
            atributoABuscar = "idalu ="+filtro.trim();
        else if (caso.equals("CURP"))
            atributoABuscar = "curp LIKE '"+filtro.toUpperCase()+"%'";
        else if (caso.equals("NOMBRE"))
            atributoABuscar=formatSeekToSubquery (filtro);
        
        //if (!cveunidad.equals("DSRVAL"))   filtroUnidad = "AND cveunidad ='"+cveunidad+"'";
        
        rs=stm.executeQuery("SELECT idalu, nombre,apepat,apemat, curp "
                          + "FROM alumno a "
                          + "WHERE "+atributoABuscar+" "+filtroUnidad+" ORDER BY idalu, nombre");
        while (rs.next()){
            fila = new Object[numColumnas];
            fila[0]=rs.getString("idalu");
            fila[1]=rs.getString("nombre");
            fila[2]=rs.getString("apepat");
            fila[3]=rs.getString("apemat");
            fila[4]=rs.getString("curp");
            modelSICEEB.addRow(fila);
        }
    }
    
    public void getFoliosAlumnoSICCEB (String idalu, SISCERT_ModeloDeTabla modelFoliosSICEEB) throws SQLException
    {
        String fila[], filaOculta[];
        int cveplan,cicescinilib;
        rs = stm.executeQuery("SELECT cveplan, foliolet, folionum, (SELECT cct FROM escuela WHERE idcct=fi.idcct) as cct, cicescini, "
                                + "cicescini+1 AS cicescfin, cicescinilib, cicescinilib+1 as cicinilibfin, promediogral, "
                                + "(SELECT estatusgrado FROM alumnogrado WHERE idalu=fi.idalu AND cicescini=fi.cicescini AND grado=fi.grado) as estatusgrado, grupo "
                            + "FROM folios_impre fi "
                            + "WHERE idalu="+idalu);
        while (rs.next()){
            fila = new String[7];
            filaOculta = new String[1];
            cicescinilib = rs.getInt("cicescinilib");
            fila[0]=(cveplan=rs.getInt("cveplan"))==1?"Primaria":(cveplan==2)?"Secundaria":"Preescolar";
            if(cicescinilib >= 2016)
                fila[1] = rs.getString("foliolet")+"20"+rs.getString("folionum");
            else 
                fila[1] = rs.getString("foliolet").substring(1)+rs.getString("folionum");
            fila[2]=rs.getString("cct");
            fila[3]=rs.getString("cicescini") + "-"+rs.getString("cicescfin");
            fila[4]=rs.getString("cicescinilib") + "-"+rs.getString("cicinilibfin");
            fila[5]=rs.getString("promediogral");
            fila[6]=rs.getString("estatusgrado");
            
            filaOculta[0]=rs.getString("grupo");
            modelFoliosSICEEB.addRow(fila,filaOculta);
        }
    }
    
    private String formatSeekToSubquery (String filtro) throws Exception
    {
        String apepatSplit[],apematNombres[]=null, nombresCurp[];
        String atributoABuscar="";
        
        if ( (!filtro.contains("/") && !filtro.contains("*")) || filtro.indexOf("/") != filtro.lastIndexOf("/") || filtro.indexOf("*") != filtro.lastIndexOf("*") || filtro.indexOf("/")>filtro.indexOf("*"))
            throw new Exception ("FORMAT_BUSQNOM");
        filtro = filtro.replace('*', '@'); 
        apepatSplit = filtro.trim().split("/");
        try { apematNombres = apepatSplit[1].trim().split("@"); } catch(ArrayIndexOutOfBoundsException ex){ throw new Exception ("FORMAT_BUSQNOM"); }
        try { if (!apepatSplit[0].equals("")) atributoABuscar += " apepat LIKE \""+apepatSplit[0].toUpperCase()+"%\" "; } catch(ArrayIndexOutOfBoundsException ex){}
        try { if (!apematNombres[0].equals("")) atributoABuscar += (!atributoABuscar.equals("")?" AND":"") + " apemat LIKE \""+apematNombres[0].toUpperCase()+"%\" "; } catch(Exception ex){ if (apematNombres==null) throw new Exception ("FORMAT_BUSQNOM"); }
        try { 
            if (!apematNombres[1].equals("")){  
                if (apematNombres[1].contains("#")){
                    if (apematNombres[1].indexOf("#") == apematNombres[1].length()-1)
                        throw new Exception ("FALTACURP_BUSQNOM");
                    else {
                        nombresCurp = apematNombres[1].trim().split("#");
                        if (!nombresCurp[0].equals(""))
                            atributoABuscar += (!atributoABuscar.equals("")?" AND":"") + " nombre LIKE \""+nombresCurp[0].toUpperCase()+"%\" "; 
                        atributoABuscar += (!atributoABuscar.equals("")?" AND":"") + " curp LIKE \""+nombresCurp[1].toUpperCase()+"%\" "; 
                    }
                }
                else
                    atributoABuscar += (!atributoABuscar.equals("")?" AND":"") + " nombre LIKE \""+apematNombres[1].toUpperCase()+"%\" "; 
            }
        } catch(ArrayIndexOutOfBoundsException ex){  }
        if (atributoABuscar.equals("")) throw new Exception ("FORMAT_BUSQNOM");
        
        return atributoABuscar;
    }

    public void borrarAlumno ( String cveunidad, int cveplan, String noControl, SISCERT_GlobalMethods global) throws SQLException, Exception
    {
        rs = stm.executeQuery("SELECT fi.foliolet,fi.folionum "+
                            "FROM siscert_folimpre fi, siscert_certificacion c "+
                            "WHERE fi.idcertificacion=c.idcertificacion AND fi.estatus_impre!='C' AND c.cveunidad='"+cveunidad+"' AND c.idcertiregion="+noControl+" AND c.cveplan="+cveplan);
        if (rs.next())
            throw new Exception ("CANCELAR_PARA_BORRAR*"+global.intToFolio(rs.getString("foliolet"), rs.getLong("folionum"), 7));
        else
            stm.execute("DELETE FROM siscert_certificacion WHERE cveunidad='"+cveunidad+"' AND cveplan="+cveplan+" AND idcertiregion = '"+noControl+"'");
    }
    
    public boolean cancelarFolio ( String idfolimpre, String idalu, String cicescinilib, int cveplan, String usuario) throws SQLException, Exception
    {
        boolean resultado = false;
        String strQuery;
        
        if(Integer.parseInt(cicescinilib) >= 2017)
            strQuery = "SELECT count(fe.idalu) AS total"
                + " FROM siscert_folimpre fi, siscert_firmaelec fe "
                + " WHERE fi.idfolimpre=fe.idfolimpre AND fi.idalu=fe.idalu AND fi.cicescinilib = fe.cicescinilib "
                + " AND fi.cveplan="+cveplan+" AND fe.idalu="+idalu+" AND fe.cicescinilib="+cicescinilib 
                + " AND fe.idfolimpre = "+idfolimpre+" AND fe.foliodigital is not null AND estatus_firma=100 ";
        else
            strQuery = "SELECT count(fe.idalu) AS total"
                + " FROM siscert_folimpre fi, siscert_firmaelec fe "
                + " WHERE fi.idfolimpre=fe.idfolimpre AND fi.idalu=fe.idalu AND fi.cicescinilib = fe.cicescinilib "
                + " AND fi.cveplan="+cveplan+" AND fe.idalu="+idalu+" AND fe.cicescinilib="+cicescinilib 
                + " AND fe.idfolimpre = "+idfolimpre+" AND fe.foliodigital is not null AND estatus_firma=100 ";

        rs = stm.executeQuery(strQuery);
        
        if(rs.next()) {
            try {
                    stm.execute("INSERT INTO siscert_firmaelec_cance(idfolimpre,idalu,cicescinilib,folionum,idfirmante,cadenaoriginal,fechatimbradoieepo,sellodigitalieepo,"
                            + "fechatimbradosep,sellodigitalsep,estatus,usuario,fechainsert,cicescini,textoenxml,xfoliodigital,sellodec,carpeta,folionum_cersep,"
                            + "estatus_firma,foliodigital,usuario_cance,fecha_cance,hora_cance) "
                        + "SELECT idfolimpre,idalu,cicescinilib,folionum,idfirmante,cadenaoriginal,fechatimbradoieepo,sellodigitalieepo,"
                            + "fechatimbradosep,sellodigitalsep,estatus,usuario,fechainsert,cicescini,textoenxml,xfoliodigital,sellodec,carpeta,"
                            + "folionum_cersep,estatus_firma,foliodigital,'"+usuario+"',date(current), extend(current, hour to minute) "
                        + " FROM siscert_firmaelec "
                        + " WHERE idalu="+idalu+" AND cicescinilib="+cicescinilib 
                        + " AND idfolimpre = "+idfolimpre+" "
                        + " AND foliodigital is not null AND estatus_firma=100" ); 
                    
                    stm.execute("DELETE FROM siscert_firmaelec "
                            + " WHERE idalu="+idalu+" AND cicescinilib="+cicescinilib 
                            + " AND idfolimpre = "+idfolimpre+" "
                            + " AND foliodigital is not null AND estatus_firma=100" );                                         
                    
                    stm.execute("INSERT INTO siscert_folimpre_cance (idfolimpre,idcertificacion,idalu,idfolim_var,foliolet,folionum,cveunidad,"
                        + "cveplan,numsolicitud,cicescinilib,cebas,nombre,"
                        + " primerape,segundoape,idcasocurp,curp,cicescini,cicescfin,prom_educprim,promedio,prom_letra,prom_educbasic,promlet_educbasic," 
                        + " promlet_primsec_edubas,dia_acredi,dia_exped_let,mes_acredi,fecha_acredi_let,curso_acredito,mesanio_exped_let,fecha_exped_let,"
                        + " libro,foja,folio,escuela,cct,fecha,juridico,modalidad,plan_estud,idleyenda_lugvalid,rodac,estatus_impre,idformato,usukgenero," 
                        + " feckgenero,usuario,fechainsert,hora,usuario_cance,fecha_cance,hora_cance)"
                    + " SELECT idfolimpre,idcertificacion,idalu,idfolim_var,foliolet,folionum,cveunidad,"
                        + "cveplan,numsolicitud,cicescinilib,cebas,nombre,"
                        + " primerape,segundoape,idcasocurp,curp,cicescini,cicescfin,prom_educprim,promedio,prom_letra,prom_educbasic,promlet_educbasic," 
                        + " promlet_primsec_edubas,dia_acredi,dia_exped_let,mes_acredi,fecha_acredi_let,curso_acredito,mesanio_exped_let,fecha_exped_let,"
                        + " libro,foja,folio,escuela,cct,fecha,juridico,modalidad,plan_estud,idleyenda_lugvalid,rodac,estatus_impre,idformato,usukgenero," 
                        + " feckgenero,usuario,fechainsert,hora,'"+usuario+"',date(current), extend(current, hour to minute) "
                    + " FROM siscert_folimpre "
                    + " WHERE cveplan="+cveplan+" AND idalu="+idalu+" AND cicescinilib="+cicescinilib 
                    + " AND idfolimpre = "+idfolimpre+" " );  
                    
                    stm.execute("DELETE FROM siscert_folimpre "
                            + " WHERE cveplan="+cveplan+" AND idalu="+idalu+" AND cicescinilib="+cicescinilib 
                            + " AND idfolimpre = "+idfolimpre+" "); 
                    
                    stm.execute(" UPDATE siscert_certificacion SET juridico='CANCELADO' "
                            + " WHERE cveplan = " + cveplan + " AND idalu = " + idalu 
                            + " AND cicescinilib="+cicescinilib + " AND juridico='VERIFICADO' ");
                                        
                    resultado=true;
            } catch(SQLException ex){
                throw new Exception("Error en el proceso de cancelación: "+ex.getMessage());
            }
        }
        return resultado;
    }
                //------------- Consulta para seleccionar los datos necesarios del alumno para cargarlos al formulario y editarlos
    public ResultSet selecAnvRevParaEditar (String noControl, String curp, int cveplan, String cveunidad, boolean verUnidades) throws SQLException
    {
        String atributos="", query="";
        switch (cveplan)
        {
            case 1: atributos = "fiv.delegacion, fiv.cctdeleg, nombre,apepat,apemat,idcasocurp,curp,cebas,dia_acredi,LOWER(mes_acredi) AS mes_acredi,idformato,ai,af,promedio,prom_letra,fiv.lugar_expedicion,NVL(idalu,'') AS idalu,idcertiregion,fiv.delegado,fiv.cargodelegado,NVL(numsolicitud,'') AS numsolicitud, cicescinilib, libro,foja,idformatosfolio, folio,idcct,NVL(tablaescuela,'') AS tablaescuela,escuela AS hescuela,NVL(idhescuela,'') AS idhescuela,escuela,cct,fiv.cotejo,juridico,fecha, idleyenda_lugvalid, c.cveunidad ";
                break;
            case 2: atributos = "fiv.delegacion, fiv.cctdeleg, nombre,apepat,apemat,idcasocurp,curp,cebas,dia_acredi,LOWER(mes_acredi) AS mes_acredi,af,idformato,prom_educprim,promedio,prom_letra,prom_educbasic,promlet_educbasic,fiv.lugar_expedicion,NVL(idalu,'') AS idalu,idcertiregion,fiv.delegado,fiv.cargodelegado,NVL(numsolicitud,'') AS numsolicitud, cicescinilib, regularizado, cicinilib_cert, ciciniestud, libro,foja,idformatosfolio,folio,idcct,NVL(tablaescuela,'') AS tablaescuela,escuela AS hescuela,NVL(idhescuela,'') AS idhescuela,escuela,cct,fiv.cotejo,NVL(plan_estud,'') AS plan_estud,juridico,fecha, idleyenda_lugvalid, c.cveunidad ";
                break;
            case 3: atributos = "fiv.delegacion, fiv.cctdeleg, nombre,apepat,apemat,idcasocurp,curp,LOWER(mes_acredi) AS mes_acredi,idformato,ai,af,fiv.lugar_expedicion,NVL(idalu,'') AS idalu,idcertiregion,fiv.delegado,fiv.cargodelegado,NVL(numsolicitud,'') AS numsolicitud, cicescinilib, libro,foja,idformatosfolio,folio,idcct,NVL(tablaescuela,'') AS tablaescuela, escuela AS hescuela,NVL(idhescuela,'') AS idhescuela,escuela,cct,fiv.cotejo,juridico,fecha, idleyenda_lugvalid, c.cveunidad ";
                break;
        }
        
        rs = stm.executeQuery("SELECT "+atributos+" FROM siscert_certificacion c, siscert_folimpre_vars fiv WHERE c.idfolim_var=fiv.idfolim_var AND idcertiregion = '" + noControl + "' AND curp = '"+curp+"' AND c.cveunidad ='"+cveunidad+"' and cveplan = '"+cveplan+"'");        
        
        if(!rs.next()){
            if(verUnidades)
                query = "SELECT "+atributos+" FROM siscert_certificacion c, siscert_folimpre_vars fiv WHERE c.idfolim_var=fiv.idfolim_var AND idcertiregion = '" + noControl + "' AND curp = '"+curp+"' AND cveplan = '"+cveplan+"'";
            else if(cveunidad.equals("DSRVAL"))    
                query = "SELECT "+atributos+" FROM siscert_certificacion c, siscert_folimpre_vars fiv WHERE c.idfolim_var=fiv.idfolim_var AND idcertiregion = '" + noControl + "' AND curp = '"+curp+"' AND c.cveunidad ='CINCO9' and cveplan = '"+cveplan+"'";            
            rs = stm.executeQuery(query);                        
        }            
        return rs;        
    }
    
                //------------- Consulta para seleccionar los datos necesarios del alumno para importarlos de la BD repSINCE, cargarlos al formulario y editarlos
    public ResultSet selecAnvRevParaImportar (String idalu, String curp, int cveplan, String cveunidad) throws SQLException
    {
        String filtroUnidad = "";
        //String filtroCINCO9 = "";   //Se elimino condicion 16-12-19
        //if (!cveunidad.equals("DSRVAL"))   filtroUnidad = AND fi.cveunidad ='"+cveunidad+"';
        //if (cveunidad.equals("CINCO9"))   filtroCINCO9 = " AND substr(ag.grupo,2,1)='_'";    //Se elimino condicion 16-12-19

        rs = stm.executeQuery("SELECT a.nombre as nombre,a.apepat,NVL(a.apemat,'') as apemat,a.curp,fi.cicescini,fi.cicescinilib, substr(fi.cct,3,3) AS modalidad, fi.idalu, " +
                                "(SELECT promediogral FROM alumnogrado where idalu=fi.idalu and grado=6 and cveplan=1 AND estatusgrado='C') AS promnum_educprim, ag.promediogral, " +
                                "ag.promedioeb, (CASE WHEN cicescinilib>=2016 THEN '20' || fi.folionum ELSE CONCAT(SUBSTR(fi.foliolet,2,1),fi.folionum) END) AS folio, " + //CONCAT (SUBSTR(fi.foliolet,2,1),fi.folionum) 
                                "e.idcct, TRIM(e.cveturno) AS cveturno, TRIM(t.desturno) AS desturno, e.nombre as escuela, fi.cct " +
                              "FROM   alumno a, folios_impre fi,alumnogrado ag, escuela  e, turno t " +
                              "WHERE  a.idalu= fi.idalu AND fi.idalu = ag.idalu AND fi.cicescini=ag.cicescini AND fi.idcct = e.idcct AND t.cveturno=e.cveturno " +
                                      "AND ag.estatusgrado='C' AND fi.idalu = "+idalu+" AND fi.cveplan = "+cveplan+" "+filtroUnidad+" ");   //+filtroCINCO9);
        return rs;
        //checar estatusgrado si es diferente de C, no dejar crearle su cuplicado
        //Revisar el historial académico del alumno
    }
    
                //----------------- Hace una consulta a la tabla de variables
    public void selecDatosDeVariables (String cveunidad, int cveplan, SISCERT_GlobalMethods global) throws SQLException, Exception
    {
        String cotejo[] = {"cotejo_pri","cotejo_sec","cotejo_pre"};
        rs = stm.executeQuery("SELECT delegacion,lugar,cctdelegacion,"+cotejo[cveplan-1]+" as cotejo,delegado,cargodelegado FROM siscert_variables where cveunidad ='"+cveunidad+"' ");
        if (rs.next()){
            global.globalDelegacion = rs.getString("delegacion");
            global.globalLugarExpedicion = rs.getString("lugar");
            global.globalCCTDelegacion = rs.getString("cctdelegacion");
            global.globalCotejo = rs.getString("cotejo");
            global.globalDelegado = rs.getString("delegado");
            global.globalCargoDelegado = rs.getString("cargodelegado");
            global.lugaresValidacion.clear();
            getLugaresValidacion (global.lugaresValidacion);
            global.setHayDatos(true);
        }else{
            global.setHayDatos(false);
            throw new Exception("No se encontraron las variables para esta región");
        }
    }
    
    public void getLugaresValidacion (ArrayList<String[]> leyendas1) throws SQLException
    {
        String leyenda[];
        rs = stm.executeQuery("SELECT idleyenda, leyenda, grupodefault FROM siscert_leyendas WHERE grupo=1 ORDER BY idleyenda");
        while (rs.next()){
            leyenda = new String[4];
            leyenda[0]=rs.getString("idleyenda");
            leyenda[1]=rs.getString("leyenda");
            if (leyenda[1].length()>41)
                leyenda[2]=leyenda[1].substring(0,19) + "..."+ leyenda[1].substring(leyenda[1].length()-19);
            leyenda[3]=rs.getBoolean("grupodefault")?"default":"";
            leyendas1.add(leyenda);
        }
    }
    
//******************************************************************************************************************************************
//************************************ METODOS USADOS EN EL FORMULARIO CalibrarImpresion ***************************************************
//******************************************************************************************************************************************

    public void obtenerCoordenadas (String cveunidad, int cveplan, int idFormatoCert, SISCERT_CoordenadasImpre coordenadas) throws SQLException
    {
        String sqryCvePlan;
        
        sqryCvePlan= idFormatoCert<8?"AND c.cveplan="+cveplan:"";
        rs = stm.executeQuery("SELECT c.idcoordenada,t.tamanio, t.fuente, c.ordenimpre, l.nombreleyenda, l.leyenda, c.x, c.y, c.lado " +
                            "FROM siscert_impcoordenadas c, siscert_impleyendas l, siscert_imptipografia t, siscert_impformato f " +
                            "WHERE c.idleyenda=l.idleyenda AND c.idfuente=t.idfuente AND c.idformato=f.idformato " +
                            "and c.cveunidad='"+cveunidad+"' AND c.idformato='"+idFormatoCert+"' " + sqryCvePlan + " " +
                            "ORDER BY c.ordenimpre");
        while (rs.next())
            coordenadas.addDataCoords(rs.getInt("idcoordenada"), rs.getInt("tamanio"), rs.getString("fuente"), rs.getInt("ordenimpre"), rs.getString("nombreleyenda"), rs.getString("leyenda"), rs.getInt("x"), rs.getInt("y"), rs.getString("lado").charAt(0));
    }

    public void guardarCoordenadas (JLabel objetos [][], String cveunidad, int cveplan ) throws SQLException
    {
        Rectangle rvObj;
        for (int anvRev=0; anvRev<2; anvRev++)
            for (int i=0; i<objetos[anvRev].length; i++)
            {
                if (objetos[anvRev][i].isVisible())
                {
                    rvObj = objetos[anvRev][i].getBounds();
                    stm.execute("UPDATE siscert_impcoordenadas SET x="+rvObj.x + ", " + "y="+rvObj.y+" WHERE idcoordenada="+objetos[anvRev][i].getName());
                }
            }
    }
    
    public void getFormatosfolio (ArrayList<Integer> idsFormatoFolio, JComboBox formatosFolio) throws SQLException
    {
        rs = stm.executeQuery("SELECT idformatosfolio, formatofolio, orden FROM siscert_formatosfolio WHERE mostrar='t' ORDER BY orden");
        while (rs.next())
        {
            idsFormatoFolio.add(rs.getInt("idformatosfolio"));
            formatosFolio.addItem(rs.getString("formatofolio"));
        }
    }
    
    public void getCasoscurp (ArrayList<Integer> idsCasoCurp, JComboBox casosCurp) throws SQLException
    {
        rs = stm.executeQuery("SELECT idcasocurp, casocurp FROM siscert_casocurp");
        while (rs.next())
        {            
            idsCasoCurp.add( rs.getInt("idcasocurp") );
            casosCurp.addItem( rs.getString("casocurp") );
        }
    }

    public void verificarFolioExistente () throws SQLException
    {
        rs = stm.executeQuery("SELECT * FROM siscert_certificacion WHERE ");
        while (rs.next())
        {                        
        }
    }
    
    //metodo obsoleto
    public void guardarAlumnoPree (String idAluSICEEB,String NoControl, String casoConsulta, String cicescinilib, SISCERT_GlobalMethods global, String nombre,String apepat,String apemat, int idcasocurp, String curp, String respaldoCurp,
            String cicloInicial, String cicloFinal, String fecha,
            String libro,String foja, String folio, String idFormatFol_folLet_folNum[], String idCCT, String idHEscuela, String tablaEscuela,String escuela, String cctEscuela, String cveturno, String juridico, String idleyenda_lugarValidacion, boolean actualizarVars, String idFormatoCert, String idformato, String casoNumSolicitud, String numSolicitud, boolean cambioEnCurpONombre) throws SQLException, Exception
    {
        /*String diaLetra, mesAñoLetra, fechaExpedLet,temp="", idfolim_var, casoInsertSICEEB="", numSol;
        
        global.fecha = fecha;
        global.partirFechaEnLetra();
        fechaExpedLet = "A LOS "+ global.diaLetra+" DÍAS DEL MES DE "+global.mesAñoLetra;
        diaLetra = "";
        mesAñoLetra="";
        
        idfolim_var = getIdVars_folImpre(global, idformato);
        
        try
        {
            conn.setAutoCommit(false);
            stm = conn.createStatement();
            
            if (idAluSICEEB.equals("")){
                casoInsertSICEEB="I";   idAluSICEEB=getNuevoIdaluParaSICEEB ();
            }else if (casoConsulta.equals("Editar") || cambioEnCurpONombre)
                casoInsertSICEEB="U";
            if (!casoInsertSICEEB.equals(""))
                idAluSICEEB = insertarAlumnoEnSICEEB (casoInsertSICEEB, idAluSICEEB, idCCT, curp, nombre, apepat, apemat, curpAFecha (curp), curp.substring(10, 11), ((cicloInicial.equals(""))?(Integer.parseInt(cicloFinal)-1):Integer.parseInt(cicloInicial)),((cicloInicial.equals(""))?(Integer.parseInt(cicloFinal)-1):Integer.parseInt(cicloInicial)), 3, cveturno, "3", "10.0", global.capturista, "", "", idFormatFol_folLet_folNum);
            
            if (casoNumSolicitud.equals("Nuevo") || global.numSolicitud.equals(""))
                numSol = obtenerNumSolicitud(global.cveunidad, global.cveplan, idformato, cicescinilib);
            else
                numSol = numSolicitud;
            
            try
            {
                if (casoConsulta.equals("Nuevo") || casoConsulta.equals("Importar"))
                {
                        //---------------- PARA VERIFICAR SI EL ALUMNO YA ESTÃ REGISTRADO
                    //rs = stm.executeQuery("SELECT  numAluRegion FROM Alumno WHERE nombre = '"+nombre+"' AND apepat = '"+apepat+"' AND apemat = '"+apemat+"' AND curp = '"+curp+"' AND cct = '"+cctEscuela+"'; ");

                    stm.executeUpdate("INSERT INTO siscert_certificacion (idalu,cveunidad,cveplan,idfolim_var, idcertiregion, numsolicitud, cicescinilib, cebas, "
                            + "nombre,apepat,apemat,idcasocurp,curp,ai,af,prom_educprim,promedio,prom_letra,prom_educbasic,promlet_educbasic,dia_exped_let,mesanio_exped_let,fecha_exped_let,libro,foja,idformatosfolio,folio,idcct,idhescuela,tablaescuela,escuela,cct,fecha,juridico,idleyenda_lugvalid, idformato, usuario, fechainsert, hora) VALUES" +
                            "('"+idAluSICEEB+"', '"+global.cveunidad+"', "+global.cveplan+", "+idfolim_var + ", "+NoControl+", "+numSol+", "+cicescinilib+", 'f', \""+nombre.trim().toUpperCase()+"\",\""+apepat.trim().toUpperCase()+"\",\""+apemat.trim().toUpperCase()+"\", "+idcasocurp+",'"+curp.trim().toUpperCase()+"'," +
                            ""+cicloInicial.trim()+", "+cicloFinal+",'','','','','','"+diaLetra+"','"+mesAñoLetra+"', '"+fechaExpedLet+"', '"+
                            libro.trim()+"', '"+foja.trim()+"', "+idFormatFol_folLet_folNum[0]+",'"+folio.trim().toUpperCase()+"', "+idCCT+","+idHEscuela+",'"+tablaEscuela+"','"+escuela.trim().toUpperCase()+"', '"+cctEscuela.trim().toUpperCase()+"'," +
                            "'"+fecha+"', '"+juridico.trim().toUpperCase()+"'," + idleyenda_lugarValidacion+","+ idformato+",'"+global.capturista+"',date(current), extend(current, hour to minute))");
                }else if (casoConsulta.equals("Editar")){
                    if (actualizarVars){
                        temp = ", idfolim_var="+idfolim_var+" ";
                    }
                    stm.execute("UPDATE siscert_certificacion SET " +
                            "numsolicitud=" + numSol + ", " +
                            "cicescinilib=" + cicescinilib + ", " +
                            "cebas= 'f', "+
                            "nombre=\""+nombre.toUpperCase()+"\"," +
                            "apepat=\""+apepat.trim().toUpperCase()+"\", " +
                            "apemat=\""+apemat.trim().toUpperCase()+"\", " +
                            "idcasocurp="+idcasocurp+", " +
                            "curp='"+curp.trim().toUpperCase()+"', " +
                            "ai="+cicloInicial.trim()+", " +
                            "af="+cicloFinal+", " +
                            "prom_educprim='', " +
                            "promedio='', " +
                            "prom_letra='', " +
                            "prom_educbasic='', " +
                            "promlet_educbasic='', " +
                            "dia_exped_let='"+diaLetra+"', " +
                            "mesanio_exped_let='"+mesAñoLetra+"', " +
                            "fecha_exped_let='"+fechaExpedLet+"', " +
                            "libro='"+libro.trim()+"', " +
                            "foja='"+foja.trim()+"', " +
                            "idformatosfolio="+idFormatFol_folLet_folNum[0] +", " +
                            "folio='"+folio.trim().toUpperCase()+"', " +
                            "idcct="+idCCT+", " +
                            "idhescuela="+idHEscuela+", " +
                            "tablaescuela='"+tablaEscuela+"', " +
                            "escuela='"+escuela.trim().toUpperCase()+"', " +
                            "cct='"+cctEscuela.trim().toUpperCase()+"', " +
                            "fecha='"+fecha+"', " +
                            "juridico='"+juridico.trim().toUpperCase()+"', " +
                            "idleyenda_lugvalid="+idleyenda_lugarValidacion+", "+
                            "idformato="+idformato+", "+
                            "fechaupdate=date(current) "+
                            temp +
                            " WHERE idcertiregion='"+NoControl+"' AND curp='"+respaldoCurp.toUpperCase()+"' AND cveplan = '"+global.cveplan+"' AND cveunidad = '"+global.cveunidad+"'");
                }
            }catch (SQLException ex){ 
                if (ex.getMessage().toUpperCase().contains("UNIQUE") && ex.getMessage().toUpperCase().contains("UK_CURPNOMREG")) 
                    throw new Exception("CERTIDUP_EXISTENTE*"+verifAlumnoDuplicado (curp, ""+global.cveplan,global.cveunidad)); 
                else if (ex.getMessage().toUpperCase().contains("UNIQUE") && ex.getMessage().toUpperCase().contains("UK_NUMSOLICITUD")) 
                    throw new Exception("NUMSOLICITUD_EXISTENTE*"+numSol); 
                else throw new SQLException(ex); 
            }
            global.numSolicitud = numSol;
            conn.commit();
        }catch (SQLException ex){
            conn.rollback();
            throw new SQLException (ex.getMessage());
        }catch (Exception ex){
            conn.rollback();
            throw new Exception (ex.getMessage());
        }finally { conn.setAutoCommit(true);}*/
    }

    public void guardarAlumnoPrim (String idAluSICEEB, String NoControl, String casoConsulta, String cicescinilib, SISCERT_GlobalMethods global, String nombre,String apepat,String apemat, int idcasocurp, String curp, String respaldoCurp,
            boolean esCEBAS, String diaAcrediCEBAS, String mesAcreditacion, String ai, String anioAcreditacion, String fechaAcrediLetCEBAS, String promedioNum, String promedioLetra, String fecha,
            String libro,String foja, String folio, String idFormatFol_folLet_folNum[], String idCCT, String idHEscuela, String tablaEscuela,String escuela, String cctEscuela, String cveturno,  String juridico, String idleyenda_lugarValidacion, boolean actualizarVars, String idFormatoCert, String casoNumSolicitud, String numSolicitud, boolean cambioEnCurpONombre, String cveUnidad59) throws SQLException, Exception
    {
        String diaLetra, mesAñoLetra, fechaExpedLet, temp="", idfolim_var, casoInsertSICEEB="", numSol,escuela_str="";
        
        global.fecha = fecha;
        global.partirFechaEnLetra();
        //diaLetra = global.diaLetra;
        //mesAñoLetra = global.mesAñoLetra;
        fechaExpedLet = "a los "+ global.diaLetra.toLowerCase()+" días del mes de "+global.mesAñoLetra.toLowerCase();
        fechaExpedLet = (fechaExpedLet.contains("a los un días"))? fechaExpedLet.replace("a los un días", "al primer día"):fechaExpedLet;
        
        diaLetra = "";
        mesAñoLetra="";
                
        idfolim_var = getIdVars_folImpre(global, idFormatoCert);
        
        if (idAluSICEEB.equals("")){
            casoInsertSICEEB="I";   idAluSICEEB=getNuevoIdaluParaSICEEB ();
        }else if (casoConsulta.equals("Editar") || cambioEnCurpONombre)
            casoInsertSICEEB="U";
        if (!casoInsertSICEEB.equals(""))
            idAluSICEEB = insertarAlumnoEnSICEEB (casoInsertSICEEB, idAluSICEEB, idCCT, curp, nombre, apepat, apemat, curpAFecha (curp), curp.substring(10, 11), ((ai.equals(""))?(Integer.parseInt(anioAcreditacion)-1):Integer.parseInt(ai)),((ai.equals(""))?(Integer.parseInt(anioAcreditacion)-1):Integer.parseInt(ai)), 6, cveturno, "1", promedioNum, global.capturista, "", "", idFormatFol_folLet_folNum);

        if (casoNumSolicitud.equals("Nuevo") || global.numSolicitud.equals(""))
            numSol = obtenerNumSolicitud(global.cveunidad, global.cveplan, idFormatoCert, cicescinilib);
        else
            numSol = numSolicitud;
        if(escuela.contains("\"")){
            escuela_str = "'"+escuela.trim().toUpperCase()+"'";
        } else {
            escuela_str = "\""+escuela.trim().toUpperCase()+"\"";
        }
        try 
        {
            if (casoConsulta.equals("Nuevo") || casoConsulta.equals("Importar"))
            {
                Object datosParaQry[][] = new Object[][]{
                    {"idalu", idAluSICEEB},
                    {"cveunidad", "'"+global.cveunidad+"'"},
                    {"cveplan", global.cveplan},
                    {"idcertiregion", NoControl},
                    {"numsolicitud", numSol}, 
                    {"cicescinilib", cicescinilib}, 
                    {"cebas", "'"+(esCEBAS?"t":"f")+"'"}, 
                    {"idfolim_var", idfolim_var},
                    {"nombre", "\""+nombre.trim().toUpperCase()+"\""},
                    {"apepat", "\""+apepat.trim().toUpperCase()+"\""},
                    {"apemat", "\""+apemat.trim().toUpperCase()+"\""},
                    {"idcasocurp", idcasocurp},
                    {"curp", "'"+curp.trim().toUpperCase()+"'"}, 
                    {"dia_acredi", "'"+diaAcrediCEBAS+"'"}, 
                    {"mes_acredi", ((mesAcreditacion.equals(""))?"null":"'"+mesAcreditacion+"'")},
                    {"ai", ((ai.equals(""))?"null":ai)}, 
                    {"af", ((anioAcreditacion.equals(""))?"null":anioAcreditacion)}, 
                    //{"fecha_acredi_let", "'"+fechaAcrediLetCEBAS+"'"}, /*Gestionar*/ //fecha = "PRIMARIA EL " + cbxDiaCEBAS.getSelectedItem()+" DE "+cbxMesAcreditacion.getSelectedItem()+((Integer.parseInt(txtAnioAcreditacion.getText())>=2000)?" DEL ":" DE ")+txtAnioAcreditacion.getText();
                    {"prom_educprim", "''"},
                    {"promedio", "'"+promedioNum.trim().toUpperCase()+"'"},
                    {"prom_letra", "'"+promedioLetra+"'"},
                    {"prom_educbasic", "''"},
                    {"promlet_educbasic", "''"},
                    {"dia_exped_let", "'"+diaLetra+"'"},
                    {"mesanio_exped_let", "'"+mesAñoLetra+"'"},
                    {"fecha_exped_let", "'"+fechaExpedLet+".'"},
                    {"libro", "'"+libro.trim()+"'"},
                    {"foja", "'"+foja.trim()+"'"},
                    {"idformatosfolio", idFormatFol_folLet_folNum[0]},  //-- Ely-06-11
                    {"folio", "'"+folio.trim().toUpperCase()+"'"},
                    {"idcct", idCCT},
                    {"tablaescuela", "'"+tablaEscuela+"'"},
                    {"idhescuela", idHEscuela},
                    {"escuela", escuela_str},
                    {"cct", "'"+cctEscuela.trim().toUpperCase()+"'"},
                    {"fecha", "'"+fecha+"'"},
                    {"juridico", "'"+juridico.trim().toUpperCase()+"'"},
                    {"idleyenda_lugvalid", idleyenda_lugarValidacion}, 
                    {"idformato", idFormatoCert}, 
                    {"usuario", "'"+global.capturista+"'"}, 
                    {"fechainsert", "date(current)"}, 
                    {"hora","extend(current, hour to minute)"}
                };

                stm.executeUpdate( crearQueryInsert("siscert_certificacion",datosParaQry) );

                /*stm.executeUpdate("INSERT INTO siscert_certificacion (idalu,cveunidad,cveplan,idfolim_var,idcertiregion, numsolicitud, cicescinilib, cebas, "
                        + "nombre,apepat,apemat,idcasocurp,curp, dia_acredi, mes_acredi,ai,af,fecha_acredi_let,prom_educprim,promedio,prom_letra,prom_educbasic,"
                        + "promlet_educbasic,dia_exped_let,mesanio_exped_let,fecha_exped_let,libro,foja,idformatosfolio,folio,idcct,idhescuela,tablaescuela,escuela,"
                        + "cct,fecha,juridico,idleyenda_lugvalid, idformato, usuario, fechainsert, hora) VALUES" +
                        "('"+idAluSICEEB+"', '"+global.cveunidad+"', "+global.cveplan+", " +idfolim_var + ", "+NoControl+", "+numSol+", "+cicescinilib+", '"+(esCEBAS?"t":"f")+"', " +
                        "\""+nombre.trim().toUpperCase()+"\",\""+apepat.trim().toUpperCase()+"\",\""+apemat.trim().toUpperCase()+"\","+idcasocurp+",'"+curp.trim().toUpperCase()+"', '"+diaAcrediCEBAS+"', " +  
                        ""+((mesAcreditacion.equals(""))?"null":"'"+mesAcreditacion+"'")+", "+((ai.equals(""))?"null":ai)+", "+((anioAcreditacion.equals(""))?"null":anioAcreditacion)+", '"+fechaAcrediLetCEBAS+"', '', '"+promedioNum.trim().toUpperCase()+"','"+promedioLetra+"','','','"+diaLetra+"','"+mesAñoLetra+"','"+fechaExpedLet+"', "+  //promedioNum.toUpper porque tambiÃ©n se permite A de aprovado
                        "'"+libro.trim()+"','"+foja.trim()+"', "+idFormatFol_folLet_folNum[0]+",'"+folio.trim().toUpperCase()+"', "+idCCT+","+idHEscuela+",'"+tablaEscuela+"','"+escuela.trim().toUpperCase()+"','"+cctEscuela.trim().toUpperCase()+"','"+fecha+"','"+juridico.trim().toUpperCase()+"'," +                    
                        idleyenda_lugarValidacion+","+idFormatoCert+", '"+global.capturista+"', date(current), extend(current, hour to minute))");*/
            }else if (casoConsulta.equals("Editar")){
                if (actualizarVars){
                    temp = ", idfolim_var="+idfolim_var+" ";
                }
                stm.execute("UPDATE siscert_certificacion SET " +
                        "numsolicitud=" + numSol + ", " +
                        "cicescinilib=" + cicescinilib + ", " +
                        "cebas= '"+(esCEBAS?"t":"f")+"', "+
                        "nombre=\""+nombre.toUpperCase()+"\"," +
                        "apepat=\""+apepat.trim().toUpperCase()+"\", " +
                        "apemat=\""+apemat.trim().toUpperCase()+"\", " +
                        "idcasocurp="+idcasocurp+", " +
                        "curp='"+curp.trim().toUpperCase()+"', " +
                        "prom_educprim='', " +
                        "promedio='"+promedioNum.trim()+"', " +
                        "prom_letra='"+promedioLetra+"', " +
                        "prom_educbasic='', " +
                        "promlet_educbasic='', " +
                        "dia_acredi='"+diaAcrediCEBAS+"', "+
                        "mes_acredi="+((mesAcreditacion.trim().equals(""))?"null":"'"+mesAcreditacion.trim()+"'")+", " +
                        "ai="+((ai.equals(""))?"null":ai.trim())+", " +
                        "af="+((anioAcreditacion.equals(""))?"null":anioAcreditacion)+", " +
                        //"fecha_acredi_let='"+fechaAcrediLetCEBAS+"', "+ /*Gestionar*/ //fecha = "PRIMARIA EL " + cbxDiaCEBAS.getSelectedItem()+" DE "+cbxMesAcreditacion.getSelectedItem()+((Integer.parseInt(txtAnioAcreditacion.getText())>=2000)?" DEL ":" DE ")+txtAnioAcreditacion.getText();
                        "dia_exped_let='"+diaLetra+"', " +
                        "mesanio_exped_let='"+mesAñoLetra+"', " +
                        "fecha_exped_let='"+fechaExpedLet+".', " +
                        "libro='"+libro.trim()+"', "+
                        "foja='"+foja.trim()+"', "+
                        "idformatosfolio="+idFormatFol_folLet_folNum[0] +", " +
                        "folio ="+"'"+ folio.trim().toUpperCase()+"',"+
                        "idcct="+idCCT+", " +
                        "idhescuela="+idHEscuela+", " +
                        "tablaescuela='"+tablaEscuela+"', " +
                        "escuela="+escuela_str+", " +
                        "cct='"+cctEscuela.trim().toUpperCase()+"', " +
                        "cveunidad = '"+global.cveunidad.trim().toUpperCase()+"', " +
                        "fecha='"+fecha+"', " +
                        "juridico='"+juridico.trim().toUpperCase()+"', " +
                        "idleyenda_lugvalid="+idleyenda_lugarValidacion+", " +
                        "idformato="+idFormatoCert+", " +
                        "usuario='"+global.capturista+"', " +
                        "fechaupdate=extend(current, year to second) " +//date(current)
                        temp +
                        " WHERE idcertiregion='"+NoControl+"' AND curp='"+respaldoCurp.toUpperCase()+"' AND cveplan = '"+global.cveplan+"' "+
                        ((global.cveunidad.equals("DSRVAL") && !cveUnidad59.equals("DSRVAL") && global.verUnidades==true) ? " AND cveunidad='"+cveUnidad59+"' " : " AND cveunidad='"+global.cveunidad+"'") ); 
            }            
            global.idcertificacion = this.getData("SELECT idcertificacion FROM siscert_certificacion WHERE idcertiregion='"+NoControl+"' AND curp='"+curp.trim().toUpperCase()+"' AND cveplan="+global.cveplan+" AND cveunidad='"+global.cveunidad+"'");
        } catch (SQLException ex){ 
            if (ex.getMessage().toUpperCase().contains("UNIQUE") && ex.getMessage().toUpperCase().contains("UK_CURPNOMREG")) 
                throw new Exception("CERTIDUP_EXISTENTE*"+verifAlumnoDuplicado (curp, ""+global.cveplan,global.cveunidad));
            else if (ex.getMessage().toUpperCase().contains("UNIQUE") && ex.getMessage().toUpperCase().contains("UK_NUMSOLICITUD")) 
                throw new Exception("NUMSOLICITUD_EXISTENTE*"+numSol); 
            else throw new SQLException(ex); 
        }
        global.numSolicitud = numSol;
    }

    public void guardarAlumnoSec (String idAluSICEEB, String NoControl, String casoConsulta, String cicescinidup, String cicinilib_cert, String ciciniestud, boolean esRegularizado, SISCERT_GlobalMethods global, String nombre,String apepat,String apemat, int idcasocurp, String curp, String respaldoCurp,
            boolean esCEBAS, String diaAcrediCEBAS, String diaAcreditacion,String mesAcreditacion,String anioAcreditacion, String fechaAcrediLetCEBAS, String promNum_educprim, String promNum_educSec,String promLet_educSec, String promNum_educbasic, String promLet_educbasic, String fechaExpedicion,
            String libro,String foja,String folio, String idFormatFol_folLet_folNum[], String idCCT, String idHEscuela, String tablaEscuela,String escuela, String cctEscuela, String cveturno, String planEstudios,String juridico, String idleyenda_lugarValidacion, boolean actualizarVars, 
            String idFormatoCert, String casoNumSolicitud, String numSolicitud, boolean cambioEnCurpONombre, String cveUnidad59) throws SQLException, Exception
    {
        Map datosParaQuery = new LinkedHashMap();
        String  fechaExpedLet, temp="", idfolim_var, casoInsertSICEEB="", numSol="",escuela_str="";
        
        global.fecha = fechaExpedicion;
        global.partirFechaEnLetra();
        fechaExpedLet = "a los "+ global.diaLetra.toLowerCase()+" días del mes de "+global.mesAñoLetra.toLowerCase();
        fechaExpedLet = (fechaExpedLet.contains("a los un días"))? fechaExpedLet.replace("a los un días", "al primer día"):fechaExpedLet;
            
        
        idfolim_var = getIdVars_folImpre(global, idFormatoCert);
        try 
        {
            if (idAluSICEEB.equals("")){
                casoInsertSICEEB="I";   idAluSICEEB=getNuevoIdaluParaSICEEB ();
            }else if (casoConsulta.equals("Editar") || cambioEnCurpONombre)
                casoInsertSICEEB="U";
            if (!casoInsertSICEEB.equals(""))
                idAluSICEEB = insertarAlumnoEnSICEEB (casoInsertSICEEB, idAluSICEEB,idCCT, curp, nombre, apepat, apemat, curpAFecha (curp), curp.substring(10, 11), Integer.parseInt(ciciniestud),Integer.parseInt(cicinilib_cert), 3, cveturno, "2", promNum_educSec, global.capturista, "taller", "arte",idFormatFol_folLet_folNum);
                        
            if (casoNumSolicitud.equals("Nuevo") || global.numSolicitud.equals(""))
                numSol = obtenerNumSolicitud(global.cveunidad, global.cveplan, idFormatoCert, cicescinidup);
            else
                numSol = numSolicitud;
            if(escuela.contains("\"")){
                escuela_str = "'"+escuela.trim().toUpperCase()+"'";
            } else {
                escuela_str = "\""+escuela.trim().toUpperCase()+"\"";
            }
            if (casoConsulta.equals("Nuevo") || casoConsulta.equals("Importar"))
            {
                Object datosParaQry[][] = new Object[][]{
                    {"idalu", idAluSICEEB},
                    {"cveunidad", "'"+global.cveunidad+"'"},
                    {"cveplan", global.cveplan},
                    {"idcertiregion", NoControl}, 
                    {"numsolicitud", numSol}, 
                    {"cicescinilib", cicescinidup}, 
                    {"cicinilib_cert", cicinilib_cert.trim()}, 
                    {"ciciniestud", ciciniestud.trim()},
                    {"regularizado", "'"+(esRegularizado?"S":"N")+"'"}, 
                    {"cebas", "'"+(esCEBAS?"t":"f")+"'"},
                    {"idfolim_var", idfolim_var},
                    {"nombre", "\""+nombre.trim().toUpperCase()+"\""},
                    {"apepat", "\""+apepat.trim().toUpperCase()+"\""},
                    {"apemat", "\""+apemat.trim().toUpperCase()+"\""},
                    {"idcasocurp", idcasocurp},
                    {"curp", "'"+curp.trim().toUpperCase()+"'"}, 
                    //{"dia_acredi", "'"+diaAcrediCEBAS+"'"}, 
                    {"dia_acredi", "'"+diaAcreditacion.trim()+"'"}, 
                    {"mes_acredi", "'"+mesAcreditacion.trim().toUpperCase()+"'"}, 
                    {"af", "'"+anioAcreditacion.trim()+"'"}, 
                    //{"fecha_acredi_let", "'"+fechaAcrediLetCEBAS+"'"}, /*Gestionar*/ //fecha = "SECUNDARIA EL " + cbxDiaCEBAS.getSelectedItem()+" DE "+cbxMesAcreditacion.getSelectedItem()+((Integer.parseInt(txtAnioAcreditacion.getText())>=2000)?" DEL ":" DE ")+txtAnioAcreditacion.getText();
                    {"prom_educprim", "'"+promNum_educprim.trim()+"'"},
                    {"promedio", "'"+promNum_educSec.trim()+"'"},
                    {"prom_letra", "'"+promLet_educSec+"'"},
                    {"prom_educbasic", "'"+promNum_educbasic.trim()+"'"},
                    {"promlet_educbasic", "'"+promLet_educbasic.trim()+"'"},
                    {"dia_exped_let", "''"},
                    {"mesanio_exped_let", "''"},
                    {"fecha_exped_let", "'"+fechaExpedLet+".'"},
                    {"libro", "'"+libro.trim()+"'"},
                    {"foja", "'"+foja.trim()+"'"},
                    {"idformatosfolio", idFormatFol_folLet_folNum[0]}, // -- Ely-06-11
                    {"folio", "'"+folio.trim().toUpperCase()+"'"},
                    {"idcct", idCCT},
                    {"tablaescuela", "'"+tablaEscuela+"'"},
                    {"idhescuela", idHEscuela},
                    {"escuela", escuela_str},
                    {"cct", "'"+cctEscuela.trim().toUpperCase()+"'"},
                    {"fecha", "'"+fechaExpedicion+"'"},
                    {"juridico", "'"+juridico.trim().toUpperCase()+"'"},
                    {"idleyenda_lugvalid", idleyenda_lugarValidacion}, 
                    {"idformato", idFormatoCert}, 
                    {"usuario", "'"+global.capturista+"'"}, 
                    {"fechainsert", "date(current)"}, 
                    {"hora","extend(current, hour to minute)"},
                    {"plan_estud", "'"+planEstudios.trim()+"'"}
                };

                stm.executeUpdate( crearQueryInsert("siscert_certificacion",datosParaQry) );

                /*stm.executeUpdate("INSERT INTO siscert_certificacion (idalu, cveunidad,cveplan,idfolim_var,idcertiregion, numsolicitud, cicescinilib, cicinilib_cert, ciciniestud, "
                        + "regularizado, cebas, nombre,apepat,apemat,idcasocurp,curp, dia_acredi, mes_acredi, af, fecha_acredi_let, prom_educprim,promedio,prom_letra,prom_educbasic,"
                        + "promlet_educbasic,dia_exped_let,mesanio_exped_let,fecha_exped_let,libro,foja,idformatosfolio,folio,idcct,idhescuela,tablaescuela,escuela,cct,fecha,"
                        + "plan_estud,juridico,idleyenda_lugvalid, idformato, usuario, fechainsert, hora) VALUES" +
                        "('"+idAluSICEEB+"', '"+global.cveunidad+"', "+global.cveplan+", "+idfolim_var + ", "+NoControl+", "+numSol+", "+cicescinidup+", "+cicinilib_cert.trim()+", "
                        + ""+ciciniestud.trim()+",'"+(esRegularizado?"S":"N")+"', '"+(esCEBAS?"t":"f")+"', \""+nombre.trim().toUpperCase()+"\",\""+apepat.trim().toUpperCase()+"\","
                        + "\""+apemat.trim().toUpperCase()+"\", "+idcasocurp+", '"+curp.trim().toUpperCase()+"', '"+diaAcrediCEBAS+"','"+mesAcreditacion+"', "
                        + "'"+anioAcreditacion.trim()+"','"+fechaAcrediLetCEBAS+"', '"+promNum_educprim.trim()+"','"+promNum_educSec.trim()+"','"+promLet_educSec+"',"
                        + "'" +promNum_educbasic.trim()+"','"+promLet_educbasic.trim()+"','','','"+fechaExpedLet+"', '"+libro.trim()+"','"+foja.trim()+"', "
                        + ""+idFormatFol_folLet_folNum[0]+",'"+folio.trim().toUpperCase()+"', "+idCCT+","+idHEscuela+",'"+tablaEscuela+"','"+escuela.trim().toUpperCase()+"',"
                        + "'"+cctEscuela.trim().toUpperCase()+"','"+fechaExpedicion+"','"+planEstudios.trim()+"','"+juridico.trim().toUpperCase()+"', "+idleyenda_lugarValidacion+","
                        + ""+idFormatoCert+", '"+global.capturista+"', date(current), extend(current, hour to minute))");*/

            }else if (casoConsulta.equals("Editar")){
                if (actualizarVars){
                    temp = ", idfolim_var="+idfolim_var+" ";
                }
                stm.executeUpdate("UPDATE siscert_certificacion SET " +
                        "numsolicitud=" + numSol + ", " +
                        "cicescinilib=" + cicescinidup + ", " +
                        "cicinilib_cert= " + cicinilib_cert.trim() + ", " +
                        "ciciniestud= " + ciciniestud.trim() + ", " +
                        "regularizado= '" + (esRegularizado?"S":"N") + "', " +
                        "cebas= '"+(esCEBAS?"t":"f")+"', "+
                        "nombre=\""+nombre.trim().toUpperCase()+"\", " +
                        "apepat=\""+apepat.trim().toUpperCase()+"\", " +
                        "apemat=\""+apemat.trim().toUpperCase()+"\", " +
                        "idcasocurp="+idcasocurp+", " +
                        "curp='"+curp.trim().toUpperCase()+"', " +
                        "prom_educprim='"+promNum_educprim.trim()+"', " +
                        "promedio='"+promNum_educSec.trim()+"', " +
                        "prom_letra='"+promLet_educSec+"', " +
                        "prom_educbasic='"+promNum_educbasic.trim()+"', " +
                        "promlet_educbasic='"+promLet_educbasic.trim()+"', " +
                        //"dia_acredi='"+diaAcrediCEBAS+"', "+
                        "dia_acredi='"+diaAcreditacion.trim()+"', "+
                        "mes_acredi='"+mesAcreditacion.trim().toUpperCase()+"', " +
                        "af='"+anioAcreditacion+"', " +
                        //"fecha_acredi_let='"+fechaAcrediLetCEBAS+"', "+  /*Gestionar*/ //fecha = "SECUNDARIA EL " + cbxDiaCEBAS.getSelectedItem()+" DE "+cbxMesAcreditacion.getSelectedItem()+((Integer.parseInt(txtAnioAcreditacion.getText())>=2000)?" DEL ":" DE ")+txtAnioAcreditacion.getText();
                        "dia_exped_let='', " +
                        "mesanio_exped_let='', " +
                        "fecha_exped_let='"+fechaExpedLet+".', " +
                        "libro='"+libro.trim()+"', " +
                        "foja='"+foja.trim()+"', " +
                        "idformatosfolio="+idFormatFol_folLet_folNum[0] +", " +
                        "folio ="+"'"+folio.trim().toUpperCase()+"',"+
                        "idcct="+idCCT+", " +
                        "idhescuela="+idHEscuela+", " +
                        "tablaescuela='"+tablaEscuela+"', " +
                        "escuela="+escuela_str+", " +
                        "cct='"+cctEscuela.trim().toUpperCase()+"', " +
                        "fecha='"+fechaExpedicion+"', " +                    
                        "plan_estud='"+planEstudios.trim()+"', " +
                        "juridico='"+juridico.trim().toUpperCase()+"', " +
                        "idleyenda_lugvalid="+idleyenda_lugarValidacion+", "+
                        "idformato="+idFormatoCert+", "+
                        "usuario='"+global.capturista+"', "+                        
                        "fechaupdate=extend(current, year to second) "+//date(current)
                        temp +
                        " WHERE idcertiregion='"+NoControl+"' AND curp='"+respaldoCurp.toUpperCase()+"' AND cveplan = " + global.cveplan  +
                        ((global.cveunidad.equals("DSRVAL") && !cveUnidad59.equals("DSRVAL") && global.verUnidades==true) ? " AND cveunidad='"+cveUnidad59+"' " : " AND cveunidad='"+global.cveunidad+"'") );                                                 
            }
            
            global.idcertificacion = this.getData("SELECT idcertificacion FROM siscert_certificacion WHERE idcertiregion='"+NoControl+"' AND curp='"+curp.trim().toUpperCase()+"' AND cveplan="+global.cveplan+" AND cveunidad='"+global.cveunidad+"'");
        }catch (SQLException ex){ 
            if (ex.getMessage().toUpperCase().contains("UNIQUE") && ex.getMessage().toUpperCase().contains("UK_CURPNOMREG")) 
                throw new Exception("CERTIDUP_EXISTENTE*"+verifAlumnoDuplicado (curp, ""+global.cveplan,global.cveunidad)); 
            else if (ex.getMessage().toUpperCase().contains("UNIQUE") && ex.getMessage().toUpperCase().contains("UK_NUMSOLICITUD")) 
                throw new Exception("NUMSOLICITUD_EXISTENTE*"+numSol); 
            else throw new SQLException(ex); 
        }
        global.numSolicitud = numSol;
    }
    
    public void verificarPermisoConPassword (String passwordParaNoValidarEdad, String capturista, int idobjeto) throws Exception
    {
        String passDecodificada;     
        
        this.rs=this.stm.executeQuery("SELECT u.pasword,u.idusuario " +
                                      "FROM usuarios u, siscert_usuarios su " +
                                      "WHERE u.idusuario=su.idusuario AND u.loginuser = '"+capturista+"'");
        if (this.rs.next()){
            passDecodificada = decodificarBase64 (this.rs.getString("pasword"));//Decodificamos la contraseña de la BD a formato Base64
            if (passwordParaNoValidarEdad.toUpperCase().equals(passDecodificada)){
                rs = stm.executeQuery("SELECT permiso FROM siscert_permisos WHERE idusuario="+rs.getString("idusuario").trim()+" AND idobjeto="+idobjeto);
                if (rs.next())
                    if (!rs.getBoolean("permiso"))
                        throw new Exception("SIN_PERMISO_DE_NO_VALIDAR_EDAD");
            }else
                throw new Exception("PASSWD_INCORRECTA");
        }else
            throw new Exception("PASSWD_INCORRECTA");
    }    
    
        //Obtiene el maximo en la tabla Alumno
    private String getNuevoIdaluParaSICEEB () throws SQLException
    {
        String max = obtenerDato("SELECT MAX(idalu) FROM alumno");
        if (max==null)
            max = "0";
        return ""+(Integer.parseInt(max)+1);
        
    }
    
    private String curpAFecha (String curp)throws Exception
    {
        String siglo;
        siglo = (Integer.parseInt(curp.substring(4,6))>=25)?"19":"20";
        return siglo+curp.substring(4,6)+"/"+curp.substring(6,8)+"/"+curp.substring(8,10);
    }
    
    private String getCveprograma (int cicescini, String cveplan, int grado, String idcct) throws SQLException
    {
        String cveprogEnPlanmod="", modalidad2="", cveprograma="", cveentidad2="", modalidad1="", cveentidad1="";
        boolean entraAPlanmodbeta=false;
        try {
            
            //cveprograma = obtenerDato("SELECT TRIM(cveprograma) FROM ESCUELA WHERE idcct="+idcct);
            modalidad1 = obtenerDato("SELECT modalidad FROM escuela WHERE idcct="+idcct); 
            cveentidad1 = obtenerDato("SELECT cveentidad FROM escuela WHERE idcct="+idcct); 
            if ("".equals(cveprogEnPlanmod = obtenerDato("SELECT TRIM(cveprograma) FROM planmodalidad WHERE cicescini<="+cicescini+" AND "+cicescini+"<=cicescfin AND plan="+cveplan+" AND grado="+grado+" AND modalidad='"+modalidad1+"' AND cveentidad='"+cveentidad1+"'")))
                cveprogEnPlanmod = obtenerDato("SELECT TRIM(cveprograma) FROM siscert_planmodalidad_beta WHERE cicescini<="+cicescini+" AND "+cicescini+"<=cicescfin AND plan="+cveplan+" AND grado="+grado+" AND modalidad='"+modalidad1+"' AND cveentidad='"+cveentidad1+"'");
            /*if (!cveprograma.equals("") && cveprogEnPlanmod.equals(""))
                stm.execute("INSERT INTO siscert_planmodalidad_beta (plan, modalidad, grado, cveprograma, desprograma, cicescini, cicescfin, siscert) SELECT "+cveplan+", SUBSTRING(cct FROM 3 FOR 3) AS modalidad, "+grado+", '"+cveprograma.trim()+"', '', "+cicescini+", "+cicescini+", 'SI' FROM escuela WHERE idcct="+idcct);*/
            if (cveprogEnPlanmod.equals("")){
                rs = stm.executeQuery ("SELECT modalidad, cveprograma, cveentidad FROM escuela WHERE idcct="+idcct);
                if (rs.next()){
                    modalidad2 = rs.getString("modalidad"); cveprograma=rs.getString("cveprograma"); cveentidad2=rs.getString("cveentidad");
                    entraAPlanmodbeta=true;
                    stm.execute("INSERT INTO siscert_planmodalidad_beta (plan, modalidad, grado, cveprograma, desprograma, cicescini, cicescfin, siscert, cveentidad) "
                              + "VALUES ("+cveplan+", "+ (modalidad2==null?null:"'"+modalidad2+"'") +", "+grado+", "+ (cveprograma==null?null:"'"+cveprograma+"'") +", '', "+cicescini+", "+cicescini+", 'SI', "+(cveentidad2==null?null:"'"+cveentidad2+"'")+")");
                    cveprogEnPlanmod = obtenerDato("SELECT TRIM(cveprograma) FROM escuela WHERE idcct="+idcct);
                }
            }
        }catch (SQLException ex) {
            if (entraAPlanmodbeta)
                throw new SQLException (ex+".\n\nSe intentó obtener cveprograma de tabla planmodalidad con ciclo="+cicescini+", plan="+cveplan+", grado="+grado+", modalidad="+modalidad1+", cveentidad="+cveentidad1+" y no se pudo."
                        + "\nSe extrajo de la tabla Escuela con idcct="+idcct+" lo siguiente: modalidad="+modalidad2+", cveprograma="+cveprograma+", cveentidad="+cveentidad2+"."
                        + "\nSe intentó insertar en planmodalidad_beta lo siguiente: cveplan="+cveplan+", modalidad="+modalidad2+", grado="+grado+", cveprograma="+cveprograma+", cicescini="+cicescini+", cveentidad="+cveentidad2+".");
            else
                throw new SQLException (ex);
        }       
        return cveprogEnPlanmod.trim();
    }
    
    private String getCvemat (String cvetipmat, int grado, String cveprograma, int cicescini ) throws SQLException
    {
        String cvemat;
        cvemat = obtenerDato("SELECT ma.cvemat FROM esquemamaterias em, materias ma WHERE em.cvemat=ma.cvemat AND em.cvetipmat=ma.cvetipmat AND em.cveplan=2 AND em.CVETIPMAT='"+cvetipmat+"' AND ma.matdefault AND em.grado="+grado+" AND em.cveprograma='"+cveprograma+"' AND em.cicescini<="+cicescini+" AND "+cicescini+"<=em.cicescfin");
        if (cvemat==null)
            cvemat="";
        return cvemat;
    }
    

    private String insertarAlumnoEnSICEEB (String alum_Estatus, String idalu, String idcct, String curpRaiz, String nombre, String primerApe, String segundoApe, String fechaNacimiento, String sexo, 
            int cicescini, int cicescinilib, int grado, String cveturno, String cveplan, String calif, String usuario, String taller, String arte, String idFormatFol_folLet_folNum[]) throws SQLException, Exception
    {
        int i;
        boolean insertar=true, tieneEscuelaDefault=false;
        String qurySec="", literal,foliolet, temp="", folioNum,cveprograma, cveEntNac="";
        String cvesEntFed[][]={{"AS","01"},{"BC","02"},{"BS","03"},{"CC","04"},{"CL","05"},{"CM","06"},{"CS","07"},{"CH","08"},{"DF","09"},{"DG","10"},{"GT","11"},{"GR","12"},
                               {"HG","13"},{"JC","14"},{"MC","15"},{"MN","16"},{"MS","17"},{"NT","18"},{"NL","19"},{"OC","20"},{"PL","21"},{"QT","22"},{"QR","23"},{"SP","24"},
                               {"SL","25"},{"SR","26"},{"TC","27"},{"TS","28"},{"TL","29"},{"VZ","30"},{"YN","31"},{"ZS","32"},{"NE","34"}};
        
        String letCveplan[]={"","P","S","K"}, cvezona, cct, cveunidad, cveentidad;
        String idscct[]={"","24071","23976","24072"};
        String cveprogramas[]={"","REGP","REGSEST","PREESCOLAR"};
        String entNac = curpRaiz.substring(11, 13).toUpperCase();
        
        if (entNac.equals("OC")) 
            cveEntNac="20";
        else{
            for (int j=0; j<cvesEntFed.length; j++)
                if (entNac.equals(cvesEntFed[j][0])){
                    cveEntNac = cvesEntFed[j][1];
                    break;
                }
        }
        
        if (calif.trim().equals("A")) calif="99";        
        if (idcct.equals("")) { idcct=idscct[Integer.parseInt(cveplan)];  tieneEscuelaDefault=true; }//Idcct default para escuelas que no están en el catálogo
        /***********************************************************************/
        if (alum_Estatus.equals("I"))
        {
            try {
                stm.execute("INSERT INTO alumno values ("+idalu+", 20 , null, 1,"+idcct+", 'I','MEX','','01','ESP','A', 1, "
                        + "'"+curpRaiz.toUpperCase()+"', \""+nombre.trim().toUpperCase()+"\", \""+primerApe.trim().toUpperCase()+"\", \""+segundoApe.trim().toUpperCase()+"\","
                        +" TO_DATE('"+fechaNacimiento+"','%Y/%m/%d'),  "
                        + "'"+sexo.toUpperCase()+"','','---',"+cveEntNac+","+ fechaNacimiento.substring(0, 4)+", null, null, null, "
                        + "0,'FA', null,date(current), '"+curpRaiz.substring(0,16).toUpperCase()+"', '' , '', 'I',null,'','', 0, '"+usuario.toUpperCase()+"', date(current), extend(current, hour to minute),null,'000')");
            }catch (SQLException ex){ 
                //Unique constraint (a211qi14.pk_alumno)
                if (ex.getMessage().toUpperCase().contains("UNIQUE")){
                    rs = stm.executeQuery("SELECT idalu FROM alumno WHERE curp='"+curpRaiz.toUpperCase()+"'");
                    if (rs.next())
                        idalu = rs.getString("idalu");
                    else if (ex.getMessage().toUpperCase().contains("CRIPFECHANOM")){
                        rs = stm.executeQuery("SELECT idalu, curp FROM alumno WHERE crip IS null AND fecnac=TO_DATE('"+fechaNacimiento+"','%Y/%m/%d') AND nombre=\""+nombre.trim().toUpperCase()+"\" AND apepat=\""+primerApe.trim().toUpperCase()+"\" AND apemat=\""+segundoApe.trim().toUpperCase()+"\"");
                        rs.next();
                        throw new Exception ("SINIDALU_CRIPFECHANOM*idalu:"+rs.getString("idalu")+" y curp:"+rs.getString("curp")+"."); 
                    }else
                        throw new SQLException (ex);
                }else
                    throw new SQLException (ex); 
            }
            
            if (tieneEscuelaDefault)
                cveprograma=cveprogramas[Integer.parseInt(cveplan)];
            else if ((cveprograma=getCveprograma (cicescini, cveplan, grado, idcct)).equals(""))
                throw new Exception ("ALUMAT_CVEPROGRAMA");
            
            try
            {
                //Verificamos si está registrado en al"SELECT idalu FROM alumnogrado WHERE idalu="+idalu+" AND grado="+grado+" AND cveplan="+cveplanumnogrado
                rs = stm.executeQuery("SELECT idalu FROM alumnogrado WHERE idalu="+idalu+" AND grado="+grado+" AND cveplan="+cveplan+" AND estatusgrado='C'");
                if (!rs.next()) {                                                //Si no está registrado, intentamos registralo
                    if (obtenerDato("SELECT cicescini FROM cicloescolar WHERE cicescini="+cicescini).trim().equals(""))
                        stm.execute("INSERT INTO cicloescolar (cicescini, cicescfin, descicesc, fecini, fecfin, estatus) VALUES ("+cicescini+" ,"+(cicescini+1)+", '"+cicescini+" - "+(cicescini+1)+"', TO_DATE('"+cicescini+"/09/01','%Y/%m/%d'),TO_DATE('"+(cicescini+1)+"/06/30','%Y/%m/%d'),'I')");
                    stm.execute("INSERT INTO alumnogrado values ("+ idalu+", "+cicescini+" ,"+(cicescini+1)+" ,"+ grado+", 0, 0, 0, "               //   /---El 1 en peso, será un distintivo para indicar que el ingreso fue por SiCEEB web
                            + "'"+cveturno+"', "+idcct+", 'INS','C',"+cveplan+",'"+cveprograma.toUpperCase()+"','A', "+ "'SIN REGISTRAR', '00000', NULL, NULL, 1, 0.00, 0, "
                                + "0.0,0.0,"+calif+", null,'P','NA','N', 1,'E', 0, 0, 0, NULL, NULL, NULL, '', '"+usuario.toUpperCase()+"', date(current), extend(current, hour to minute),date(current))" );
                } else {                                                      //Si ya está registrado
                    //Verificamos si ya tiene registrado un certificado impreso
                    rs = stm.executeQuery("SELECT foliolet, folionum FROM folios_impre WHERE idalu="+idalu+" AND grado="+grado+" AND cveplan="+cveplan);
                    if (rs.next())                                              //Si ya tiene un certificado impreso
                        throw new Exception ("ALU_EN_SICCEB*"+rs.getString("foliolet").substring(1)+rs.getString("folionum"));                  //Que no inserte en folios impre
                }
                //********************** INSERTAMOS EN FOLIOS_IMPRE ******************************************
                literal = idFormatFol_folLet_folNum[1];
                if (idFormatFol_folLet_folNum[2].equals("SIN FOLIO"))
                    folioNum=obtenerDato ("SELECT MIN(folionum)-1 FROM folios_impre");
                else
                    folioNum=idFormatFol_folLet_folNum[2];
                rs = stm.executeQuery("SELECT TRIM(cvezona) AS cvezona, TRIM(cct) AS cct, TRIM(cveunidad) AS cveunidad, cveentidad FROM escuela WHERE idcct="+idcct);
                rs.next();
                cvezona=rs.getString("cvezona"); cct=rs.getString("cct"); cveunidad=rs.getString("cveunidad"); cveentidad=rs.getString("cveentidad");
                if(literal.equals("CE") && cicescinilib>=2016)
                    foliolet = literal;
                else 
                    foliolet = letCveplan[Integer.parseInt(cveplan)]+literal;
                try {                    
                stm.execute ("INSERT INTO folios_impre (complementaria,foliolet,folionum,idalu,idcct,cvezona,cveplan,cct,cveturno,grado,crip,curp,grupo,nombre,apepat,apemat,cicescini,cicescinilib,alos,delmes,promediogral,quienfirma,tratamiento,usuario,fecha,hora,cveunidad, cveentidad) " +
                             "VALUES (NULL,'"+foliolet+"',"+folioNum+","+idalu+","+idcct+",'"+cvezona+"',"+cveplan+",'"+cct+"','"+cveturno+"',"+grado+",'"+curpRaiz.toUpperCase().substring(4,10)+"','"+curpRaiz.toUpperCase()+"','A',\""+nombre.trim().toUpperCase()+"\", \""+primerApe.trim().toUpperCase()+"\", \""+segundoApe.trim().toUpperCase()+"\","+cicescini+","+cicescinilib+",'','',"+calif+",null,null,'"+ usuario.toUpperCase() +"', date(current), extend(current, hour to minute),'"+cveunidad+"', "+cct.substring(0,2)+")");
                }catch (SQLException ex){ 
                    if (ex.getMessage().toUpperCase().contains("UNIQUE")){
                        if (ex.getMessage().contains("ciclibplnfol"))
                            rs = stm.executeQuery("SELECT TRIM(curp) AS curp FROM folios_impre WHERE folionum="+folioNum+" AND cicescinilib="+cicescinilib+" AND cveplan="+cveplan+" AND cveentidad="+cveentidad);
                        else
                            rs = stm.executeQuery("SELECT TRIM(curp) AS curp FROM folios_impre WHERE folionum="+folioNum+" AND cct='"+cct+"' AND cicescini="+cicescini);
                        
                        i=0;
                        while (rs.next()) {
                            if (i>0) temp+=", "; i++;
                            temp+=rs.getString("curp");
                        }
                        throw new Exception("FOLIO_EN_FOLIOSIMPRE*"+temp);
                    }else throw new SQLException(ex);
                } 
                //********************************************************************************************
                try
                {
                    if (!cveplan.equals("3"))
                    {
                        if (cveplan.equals("2")){
                            taller=getCvemat ("EDT", grado, cveprograma, cicescini );
                            arte=getCvemat ("ART", grado, cveprograma, cicescini );
                            if (!taller.equals("") && !arte.equals(""))
                                qurySec = " AND (CVETIPMAT='CBA' OR CVETIPMAT='OPC' OR (CVETIPMAT='AES' AND CVEMAT='040') OR (CVETIPMAT='ART' AND CVEMAT='"+arte+"') OR CVETIPMAT='LEX' OR (CVETIPMAT='EDT' AND CVEMAT='"+taller+"'))";
                            else
                                insertar=false;
                        }

                        if (insertar){
                            try {
                                stm.execute("INSERT INTO alumnomaterias(cvetipmat, cvemat, cveplan, cveprograma, idalu, cicescini, cicescfin, grado,  grupotaller, promedio, estatmat, califant, repetidor, repecont, usuario, fecha, hora) "
                                    + "SELECT cvetipmat, cvemat, cveplan, cveprograma, "+idalu+" AS IDALU, "+cicescini+" AS cicescini, "+ (cicescini+1)+" AS cicescfin, grado,'---' AS grupotaller, 0 AS promedio, 'A' AS estatmat, 0 AS califant, 'E' AS repetidor, "
                                        + "0 AS repecont, '"+usuario.toUpperCase()+"' AS USUARIO, date(current) as fecha, extend(current, hour to minute) as hora "
                                    + "FROM esquemamaterias "
                                    + "WHERE cveplan= "+ cveplan+" AND grado = "+ grado+" AND cveprograma = '"+cveprograma.toUpperCase()+"' "
                                    + " AND cicescini <= "+ cicescini+" AND cicescfin >= "+ cicescini                        
                                    + qurySec);
                            }catch (SQLException ex){
                                if (ex!=null && ex.getMessage()!=null){
                                    if (!ex.getMessage().contains("cicplangdomat")){
                                        if (ex.getMessage().contains("fk_alumnomaterias"))
                                            throw new Exception ("CURSO_EN_OTRO_CICLO*"+cicescini+"-"+(cicescini+1));
                                        else
                                            throw new SQLException (ex);
                                    }
                                }else throw new SQLException (ex);
                            }
                        }
                    }
                }catch (SQLException ex){ throw new SQLException (ex); }
            }catch (SQLException ex){ 
                if (ex != null && ex.getMessage()!=null){
                    if (ex.getMessage().contains("pk_alumnogrado")){ //idalu, cicescini, cicescfin, grado
                        rs = stm.executeQuery("SELECT estatusgrado, idalu FROM alumnogrado WHERE idalu="+idalu+" AND cicescini="+cicescini+" AND cicescfin="+(cicescini+1)+" AND grado="+grado);
                        if (rs.next() && !rs.getString("estatusgrado").equals("C"))
                            throw new Exception("GRADOREP_ESTATUS~"+rs.getString("idalu")+"~"+cicescini+"-"+(cicescini+1)+"");
                        else
                            throw new SQLException (ex);
                    }else
                        throw new SQLException (ex);
                }else
                    throw new SQLException (ex); 
            }
        }else if(alum_Estatus.equals("U")) {
            if (!"SI".equals(obtenerDato("SELECT validado FROM alumno2 WHERE idalu="+idalu))){
                try{
                    rs = stm.executeQuery("SELECT idalu FROM alumno WHERE curp='"+curpRaiz.toUpperCase()+"' AND nombre=\""+nombre.trim().toUpperCase()+"\" AND apepat=\""+primerApe.trim().toUpperCase()+"\" AND apemat=\""+segundoApe.trim().toUpperCase()+"\" AND fecnac=TO_DATE('"+fechaNacimiento+"','%Y/%m/%d') AND sexo='"+sexo.toUpperCase()+"' AND curp16='"+curpRaiz.substring(0,16).toUpperCase()+"'");
                    if (!rs.next()) {
                        stm.execute("UPDATE alumno SET curp='"+curpRaiz.toUpperCase()+"', nombre=\""+nombre.trim().toUpperCase()+"\", apepat=\""+primerApe.trim().toUpperCase()+"\", apemat=\""+segundoApe.trim().toUpperCase()+"\", "
                                +"fecnac=TO_DATE('"+fechaNacimiento+"','%Y/%m/%d'), sexo='"+sexo.toUpperCase()+"', curp16='"+curpRaiz.substring(0,16).toUpperCase()+"',usuario='"+usuario.toUpperCase()+"', fecha=date(current), hora=extend(current, hour to minute) WHERE idalu="+idalu);
                        /*if (!(cveprograma=getCveprograma (cicescini, cveplan, grado, idcct)).equals(""))
                            qurySec="idcct="+idcct+", cveprograma='"+cveprograma+"', ";
                        stm.execute("UPDATE alumnogrado SET "+qurySec+" promediogral="+calif+" WHERE idalu="+idalu+" AND grado="+grado+" AND cicescini="+cicescini+"");*/
                        
                        /*   Instruccion agregada para poner en estatus IA el folio del alumno que sufra cambios en datos personales, comentado el 18-10-19*/
                        rs = stm.executeQuery("SELECT idfolio FROM folios_impre WHERE idalu="+idalu);
                        if (rs.next())
                            stm.execute("UPDATE folios_impre SET estatus='IA', usuario='"+usuario.toUpperCase()+"',fecha=date(current), hora=extend(current, hour to minute) "
                                    + "WHERE idalu="+idalu);
                    }
                }catch (SQLException ex) {
                    if (ex.getMessage().contains("cripfechanom")){
                        rs = stm.executeQuery("SELECT idalu FROM alumno WHERE fecnac=TO_DATE('"+fechaNacimiento+"','%Y/%m/%d') AND nombre=\""+nombre.trim().toUpperCase()+"\" AND apepat=\""+primerApe.trim().toUpperCase()+"\" AND apemat=\""+segundoApe.trim().toUpperCase()+"\"");
                        i=0;
                        while (rs.next()){
                            if (i>0) temp+=", "; i++;
                            temp+=rs.getString("idalu");
                        }
                        throw new Exception("ALUREP_CRIPFECHANOM*"+temp);
                    }else if (ex.getMessage().contains("curpnom")){
                        rs = stm.executeQuery("SELECT idalu FROM alumno WHERE curp='"+curpRaiz.toUpperCase()+"' AND nombre=\""+nombre.trim().toUpperCase()+"\" AND apepat=\""+primerApe.trim().toUpperCase()+"\" AND apemat=\""+segundoApe.trim().toUpperCase()+"\"");
                        if (rs.next())
                            throw new Exception("ALUREP_CURPNOM*"+rs.getString("idalu"));
                    }else
                        throw new SQLException (ex);
                }
            }
        }
        
        return idalu;
    }
    
    //OJO - Implementar que si otra region encuentra esta curp, pase al chamaco a la region y actualice los datos del chamaco
    public String verifAlumnoDuplicado (String curp, String cveplan, String cveunidadDeCapturista) throws SQLException
    {
        String idcertiregion;
        rs = stm.executeQuery("SELECT idcertificacion, idcertiregion, cveunidad FROM siscert_certificacion WHERE curp='"+curp.toUpperCase()+"' AND cveplan="+cveplan+" and cveunidad='"+cveunidadDeCapturista+"'");
        if (rs.next()){
            idcertiregion = rs.getString("idcertiregion");
            return " como N.C.: "+idcertiregion;
        }
        return "";
    }
    
    private String getIdVars_folImpre (SISCERT_GlobalMethods global, String idFormatoImpre) throws SQLException 
    {
        String cotejo="";
        
        if (Integer.parseInt(idFormatoImpre) < 8)
            cotejo = global.globalCotejo;
        
        while(true){
            rs = stm.executeQuery("SELECT idfolim_var FROM siscert_folimpre_vars "
                                + "WHERE cveunidad='"+global.cveunidad+"' AND delegacion='"+global.globalDelegacion+"' "
                                + "AND cctdeleg='"+global.globalCCTDelegacion+"' AND lugar_expedicion='"+global.globalLugarExpedicion+"' "
                                + "AND delegado='"+global.globalDelegado+"' AND cargodelegado='"+global.globalCargoDelegado+"' AND cotejo='"+cotejo+"' ");
            if (rs.next())
                return rs.getString("idfolim_var");                        //Obtenemos el id para las variables
            
            stm.execute("INSERT INTO siscert_folimpre_vars (cveunidad,delegacion,cctdeleg,lugar_expedicion,delegado,cargodelegado,cotejo) values ('"+global.cveunidad+"','"+global.globalDelegacion+"',"
                            + "'"+global.globalCCTDelegacion+"','"+global.globalLugarExpedicion+"','"+global.globalDelegado+"','"+global.globalCargoDelegado+"','"+cotejo+"')");            
        }
    }

                //-----------------  Método público que retorna específicamente la clave que tocará para la siguiente inserción de datos
    public String obtenerIdcertiregion(String cveunidad, int cveplan) throws SQLException
    {
        return obtenerIdTabla ("idcertiregion","siscert_certificacion",cveunidad, cveplan);
    }
    
    public String obtenerNumSolicitud (String cveunidad, int cveplan, String idformato, String cicescinilib) throws SQLException
    {
        String num;
        num = obtenerDato ("SELECT MAX(numsolicitud)+1 FROM siscert_certificacion WHERE cveunidad='"+cveunidad+"' AND cveplan="+cveplan+" AND idformato="+idformato+" AND cicescinilib='"+cicescinilib+"'");
        if(num==null || num.equals("")) {  num="1";  } //Si no hay datos en la tabla se asigna 1
        return num;  //Si no le sumamos (+1)
    }
    
                //-----------------  calcula un id para la tabla solicitada
    private String obtenerIdTabla (String idABuscar, String tabla, String cveunidad, int cveplan)  throws SQLException
    {
        String id;
        int temp;

        rs = stm.executeQuery("SELECT MAX (" + idABuscar + ") as numero FROM " + tabla + " WHERE cveunidad ='"+cveunidad+"' AND cveplan = "+cveplan+";");
        rs.next();
        id=rs.getString("numero");  //Se extrae el nÃºmero para id

        if(id==null) {  id="1";  } //Si no hay datos en la tabla se asigna 1
        else{ temp = Integer.parseInt(id) + 1; id = "" + temp; }  //Si no le sumamos (+1)

        return id;
    }
                //-----------------  Retorna el rango de los Ãºltimos veinte ids en la tabla SISCERT o SICEEB en formato: (max-20)-max
    public String getMaxMinID (String BD, String cveunidad, int cveplan, String idformato) throws SQLException
    {                                                                                 //AND idformato="+idformato+"
        String [][] datos = {{"idcertiregion","siscert_certificacion","cveunidad ='"+cveunidad+"' AND "},{"idalu","folios_impre","cveunidad ='"+cveunidad+"' AND"}};  //BD SISCERT, BD SICEEB
        int i, max, min;
        i = BD.equals("SISCERT")? 0:1;                                          //Para elegir entre tabals del SISCERT O repSINCE
        //if (cveunidad.equals("DSRVAL") && BD.equals("SICEEB"))                 //Si Valles hace una consulta en SISCERT se le quita la reestricciÃ³n de consultar sÃ³lo valles
        //   datos[i][2]="";
        rs = stm.executeQuery("SELECT MAX (" + datos[i][0] + ") as numero FROM " + datos[i][1] + " WHERE "+datos[i][2]+" cveplan = "+cveplan+";");
        rs.next();
        max = rs.getInt("numero");
        rs = stm.executeQuery("SELECT MIN (" + datos[i][0] + ") as numero FROM " + datos[i][1] + " WHERE "+datos[i][2]+" cveplan = "+cveplan+";");
        rs.next();
        min = rs.getInt("numero");
        if (max-20>=min)  min = max - 20;                                        //Para obtener los Ãºltimos 20
        return min+"-"+max;
    }
    
    public String getMaxMinNumSolicitud (String cveunidad, int cveplan, String idformato) throws SQLException
    {
        int max, min;                                                                                                              
        String ciclomax = obtenerDato("SELECT MAX(cicescinilib) FROM siscert_certificacion WHERE cveunidad='"+cveunidad+"' AND cveplan="+cveplan);  //+" AND idformato="+idformato
        rs = stm.executeQuery("SELECT MAX(numsolicitud) AS max_numsolicitud, MIN(numsolicitud) AS min_numsolicitud FROM siscert_certificacion WHERE cveunidad='"+cveunidad+"' AND cveplan="+cveplan+"  AND cicescinilib="+ciclomax);  // AND idformato="+idformato+"
        rs.next();
        max = rs.getInt("max_numsolicitud");
        min = rs.getInt("min_numsolicitud");
        if (max-20>=min)  min = max - 20;                                        //Para obtener los Últimos 20
        return min+"-"+max+"/"+ciclomax;
    }

                //-----------------  Para buscar en el catálogo de escuelas
    public ResultSet consultaPorEscuelas (String textoBuscar, String cveunidad, int cveplan) throws SQLException
    {
        //String algo = "SELECT cct, nombre From escuela WHERE nombre like '%" + textoBuscar.toUpperCase() + "%'  AND cveplan='"+cveplan+"'";
        //if (cveunidad.equals("DSRVAL"))                                         //Damos permisos para que Ãºnicamente VALLES CENTRALES tenga acceso a todas las regiones
            rs = stm.executeQuery("SELECT cct, nombre From escuela WHERE nombre like '%" + textoBuscar.toUpperCase() + "%'  AND cveplan='"+cveplan+"'");
       // else
        //    rs = stm.executeQuery("SELECT cct, nombre From escuela WHERE nombre like '%" + textoBuscar.toUpperCase() + "%' AND cveunidad = '"+cveunidad+"' AND cveplan='"+cveplan+"'");
        return rs;
    }
    
                //-----------------  Para buscar el CCT de una escuela
    public ResultSet consultaPorCCT (String textoBuscar, String cveunidad, int cveplan) throws SQLException
    {
        //String algo = "SELECT cct, nombre From escuela WHERE cct like '%" + textoBuscar.toUpperCase() + "%' AND cveplan='"+cveplan+"'";
        //if (cveunidad.equals("DSRVAL"))                                         //Damos permisos para que Ãºnicamente VALLES CENTRALES tenga acceso a todas las regiones
            rs = stm.executeQuery("SELECT cct, nombre From escuela WHERE cct like '%" + textoBuscar.toUpperCase() + "%' AND cveplan='"+cveplan+"'");
        //else
      //     rs = stm.executeQuery("SELECT cct, nombre From escuela WHERE cct like '%" + textoBuscar.toUpperCase() + "%' AND cveunidad = '"+cveunidad+"' AND cveplan='"+cveplan+"'");
        return rs;
    }
    
    public void getEscuelas (String caso, String textoBuscar, ArrayList<String[]> id_cct_esc_cvet_turno, JComboBox cmbxCCT, JComboBox cmbxEscuela, String cveplan) throws SQLException
    {
        String campoWhere="", fila[];
        
        if (caso.equals("ESCUELA"))                                             //Búsqueda por escuela
            campoWhere = " e.nombre ";
        else if (caso.equals("CCT"))                                            //Búsqueda por CCT
            campoWhere = " e.cct ";

        rs = stm.executeQuery("SELECT e.idcct, TRIM(e.cct) AS cct, TRIM(e.nombre) AS escuela, TRIM(t.cveturno) AS cveturno, TRIM(t.desturno) AS desturno, " +
                              "(TRIM(e.nombre) || ' ('||TRIM(t.desturno)||')') AS nombre " +
                              "FROM escuela e, turno t " +
                              "WHERE t.cveturno=e.cveturno AND "+campoWhere+" like '" + textoBuscar.toUpperCase() + "%' AND cveplan="+cveplan);
        while (rs.next ())
        {
            fila = new String[5];
            fila[0]=rs.getString("idcct");
            fila[1]=rs.getString("cct");
            fila[2]=rs.getString("escuela");
            fila[3]=rs.getString("cveturno");
            fila[4]=rs.getString("desturno");
            cmbxCCT.addItem(fila[1]);
            cmbxEscuela.addItem(rs.getString("nombre"));
            id_cct_esc_cvet_turno.add(fila);
        }
    }
    
    public void getCicloEscolar (JComboBox cmbxCiclo) throws SQLException
    {
        rs = stm.executeQuery("SELECT (cicescini||'-'||cicescfin) AS ciclo from siscert_cicloescolar where estatus='A'");
        if (rs.next ())                    
            cmbxCiclo.addItem(rs.getString("ciclo"));
        else 
            cmbxCiclo.addItem("---");
    }
    
    public void getEscuelaHistorica (String compararPor, String valor, ArrayList<String[]> hid_hcct_hesc_hcvet_hturno, JComboBox hescuela, String cveplan) throws SQLException
    {
        String[] fila;
        String subquery="";
        
        if (compararPor.equals("cct"))
            subquery="e.cct = '"+valor+"'";
        else if (compararPor.equals("idhescuela"))
            subquery="e.idhescuela = "+valor;
        
        rs = stm.executeQuery("SELECT e.idhescuela, TRIM(e.cct) AS cct, TRIM(e.nombre) AS escuela,  TRIM(t.cveturno) AS cveturno, TRIM(t.desturno) AS desturno, " +
                              "(TRIM(e.nombre) || ' ('||TRIM(t.desturno)||')') AS nombre " +
                              "FROM hescuela e, turno t " +
                              "WHERE t.cveturno=e.cveturno AND "+subquery+" AND cveplan="+cveplan);
        while (rs.next())
        {
            fila = new String[5];
            fila[0]=rs.getString("idhescuela");
            fila[1]=rs.getString("cct");
            fila[2]=rs.getString("escuela");
            fila[3]=rs.getString("cveturno");
            fila[4]=rs.getString("desturno");
            hescuela.addItem(rs.getString("nombre").trim());
            hid_hcct_hesc_hcvet_hturno.add(fila);
        }
    }
    
    public String getNombreEscuela (String idcct, String[]nombreEscuela, int pos) throws SQLException
    {
        String nombreescuela="";
        rs = stm.executeQuery("SELECT TRIM(e.nombre) AS escuela,  (TRIM(e.nombre) || ' ('||TRIM(t.desturno)||')') AS nombre From escuela e, turno t WHERE t.cveturno=e.cveturno AND e.idcct= "+idcct );
        if (rs.next()){
            nombreEscuela[pos]=rs.getString("escuela");
            nombreescuela=rs.getString("nombre");
        }
        return nombreescuela;
    }
    
    public void get_cveturnoYturno_FromIdcct (String[]cveYturno, String idcct) throws SQLException
    {
        rs = stm.executeQuery("SELECT e.idcct, TRIM(e.cct) AS cct, (TRIM(e.nombre) || ' ('||TRIM(t.desturno)||')') AS nombre, TRIM(t.cveturno) AS cveturno, TRIM(t.desturno) AS desturno From escuela e, turno t WHERE t.cveturno=e.cveturno and e.idcct = " + idcct);
        if (rs.next ()){
            cveYturno[0]=rs.getString("cveturno");
            cveYturno[1]=rs.getString("desturno");
        }
    }

    public void guardarVariables (int cveplan, String delegacion, String cveUnidad, String lugar, String CCT, String cotejo, String delegado, String cargoDelegado) throws SQLException
    {
        String cotejoAtrib[] = {"cotejo_pri","cotejo_sec","cotejo_pre"};

        stm.execute("UPDATE siscert_variables SET delegacion='" + delegacion.trim() + "', lugar = '" + lugar.trim() +
                    "', cctdelegacion = '"+CCT.trim()+"', "+cotejoAtrib[cveplan-1]+" = '" + cotejo.trim() + "', delegado = '"+ delegado.trim() +
                    "', cargodelegado = '" + cargoDelegado.trim() + "' WHERE cveunidad='" + cveUnidad.trim() + "'");
    }

    //OJO: Usar transacción para usar este método
    public String crearFoliosLibro___ (String letra, String folioIni, String folioFin, String cveunidad, int tipoCert) throws SQLException
    {
        int ini, fin, n = 1;
        String folio="", np="", nombreTabla;
        ini = Integer.parseInt(folioIni);
        fin = Integer.parseInt(folioFin);
        nombreTabla = "siscert_foltemp"+cveunidad.substring(3,6)+tipoCert;
        borrarTablaFoliosLibro (nombreTabla);

        //Extraemos todos los nombres de objetos o componentes
        stm.execute("CREATE TABLE "+nombreTabla+" (" +
                    "numProg CHAR(4) PRIMARY KEY, " +
                    "folio VARCHAR(11))");
        for (int i = ini; i<=fin; i++, n++){
            if (i < 10)
                folio = letra + "000000" + i;
            else if (i<100)
                folio = letra + "00000" + i;
            else if (i<1000)
                folio = letra + "0000" + i;
            else if (i<10000)
                folio = letra + "000" + i;
            else if (i<100000)
                folio = letra + "00" + i;
            else if (i<1000000)
                folio = letra + "0" + i;
            else if (i<10000000)
                folio = letra + "" + i;

            if (n < 10)
                np = "000" + n;
            else if (n<100)
                np = "00" + n;
            else if (n<1000)
                np = "0" + n;
            else if (n<10000)
                np = "" + n;

             stm.executeUpdate("INSERT INTO "+nombreTabla+" (numProg, folio) VALUES ('"+np+"', '"+folio+"')");
        }
        return nombreTabla;
    }

    public void borrarTablaFoliosLibro (String nombreTabla) throws SQLException
    {
        try
        {
            stm.executeUpdate("DROP TABLE " + nombreTabla);
        } catch (SQLException ex )
        {
            if (!ex.getMessage().contains("is not in the database"))
                throw new SQLException(ex);
        }
    }

   
    public String verifVersion (String [] info, String cveUnidad, String sisver, int tipoVersion)throws SQLException
    {
        String versAdminRegisEnBD, versActAdmin, fullversionAdmin, lightversionAdmin, fullverUsr, querySET="";
        boolean registrarVersion=true;
		//************************ ACTUALIZAMOS LOS DATOS PARA ESTA VERSIÓN *********************************
        if (cveUnidad.equals("ADMIN")){
            versAdminRegisEnBD = obtenerDato("SELECT version From siscert_sisver WHERE cveunidad='ADMIN'");  //Seleccionamos la versión de administrador (la más actual)
            if (compararVersion(versAdminRegisEnBD, sisver).equals("ANTERIOR")){
                switch (JOptionPane.showConfirmDialog(null, "Esta versión "+sisver+" que estás usando, es más vieja a la ya publicada: "+versAdminRegisEnBD+"\n¿Deseas publicar esta versión? Elige No para no publicar y entrar, o Cancelar para salir.", "Pregunta emergente", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE))
                {
                    case JOptionPane.YES_OPTION: registrarVersion=true;     break;
                    case JOptionPane.NO_OPTION: registrarVersion=false;     break;
                    case JOptionPane.CANCEL_OPTION: System.exit(0);         break;
                    default: break;
                }
            }
        } else
            querySET=((tipoVersion==1)?"fullversion":"lightversion")+"='"+sisver+"',";
        
        if (registrarVersion)
            stm.executeUpdate("UPDATE since:informix.siscert_sisver SET version = '"+sisver+"', "+querySET+" dateaccess=date(current), houraccess=extend(current, hour to minute) WHERE cveunidad='"+cveUnidad+"'");   //Registramos la versión de este sistema
        //************************ EXTRAEMOS DATOS DE ADMINISTRADOR *********************************
        rs = stm.executeQuery("SELECT version, lightversion, fullversion  From siscert_sisver WHERE cveunidad='ADMIN'");  //Seleccionamos la versión de administrador (la más actual)
        rs.next();
        info[0] = versActAdmin = rs.getString("version");
        lightversionAdmin = rs.getString("lightversion");
        fullversionAdmin = rs.getString("fullversion");
        //************************* EXTRAEMOS DATOS DE USUARIO ***************************************
        rs = stm.executeQuery("SELECT mensaje, nummsgemitidos, locktoupdate, locksistem, fullversion From siscert_sisver WHERE cveunidad='"+cveUnidad+"'");  //Seleccionamos los datos de versión para este usuario
        rs.next();
        info[1] = rs.getString("mensaje").trim();
        info[2] = rs.getString("nummsgemitidos");
        info[3] = rs.getString("locktoupdate");
        info[4] = rs.getString("locksistem");
        fullverUsr = rs.getString("fullversion");
        
        if (info[4].equals("1"))                                                //Si locksistem contiene 1, entonces el sistema se debe bloquear
            return "SIS_LOCK";
        
        if (versActAdmin.equals(sisver)){                                       //Si ya se tiene la versión más actual
            if (!info[2].equals("0"))
                stm.executeUpdate("UPDATE siscert_sisver SET nummsgemitidos=0 WHERE cveunidad='"+cveUnidad+"'");// reiniciamos contador de mensajes emitidos
            stm.executeUpdate("UPDATE siscert_sisver SET  mensaje='' WHERE cveunidad='"+cveUnidad+"'");// Quitamos el mensaje de advertencia por actualización
                
            if (tipoVersion==0 && compararVersion(fullversionAdmin,fullverUsr).equals("ANTERIOR"))   //Si requiere version full y tiene solo actualización y la full requerida es ésta
                return "SIS_FULL_EN_ESTA_VERSION";
        }else {
            stm.executeUpdate("UPDATE siscert_sisver SET nummsgemitidos="+(Integer.parseInt(info[2])+1)+ " WHERE cveunidad='"+cveUnidad+"'"); //Incrementamos contador de mensajes
            
            if (compararVersion(fullversionAdmin, fullverUsr).equals("ANTERIOR"))                     //Si la versión necesita ser full y la versión es anterior a la última necesitada como full
                return "SIS_A_FULL";                                              //Forzamos a que primero muestre mensaje de que bloqueo por versión nueva
            if (compararVersion (lightversionAdmin, sisver).equals("ANTERIOR"))
                return "SIS_A_ACTUALIZACION";
            if (info[3].equals("1"))                                            //Si locktoupdate contiene 1, entonces verificamos que el sistema esté corriendo con una actualización mayor o igual a la requerida
                return "REESTRINGIDO_PARA_ACTUALIZAR";                          //Para que actualice, ya sea full o actualización
            else 
                return "ACTUALIZACION";
        }
        return "";
    }
    
    private String compararVersion (String versionBase, String versionAComparar)
    {
        String versionAdmin[], versionUser[];
        int i=0, versUsr, versBD;
        String tipo="IGUAL";
        
        versionAdmin=versionBase.replace(".", ":").split(":");
        versionUser=versionAComparar.replace(".", ":").split(":");
        
        for (String vers : versionAdmin){
            try { versUsr = Integer.parseInt(versionUser[i]); } catch(ArrayIndexOutOfBoundsException ex){ versUsr=0; }
            try { versBD = Integer.parseInt(vers);  } catch(ArrayIndexOutOfBoundsException ex){ versBD=0; }
            if (versUsr<versBD)
                return "ANTERIOR";
            else if (versUsr>versBD)
                return "POSTERIOR";
            i++;
        }
        
        return tipo;
    }
    

//************************************************************************************************************
//************************** MÉTODO USADOS EN EL FORMULARIO Leyenda1 *****************************************
//************************************************************************************************************
    public void getLeyendas1(SISCERT_ModeloDeTabla modelLeyendas1, String PRELEYENDA_LUGARVALID) throws SQLException
    {
        Object []fila;
        Object []filaOculta;
        
        //javax.swing.JRadioButton rbtnPrueba = new javax.swing.JRadioButton();
        //rbtnPrueba.setSelected(true);
        //rbtnPrueba.setName("rbtnPrueba");
        
        rs = stm.executeQuery("SELECT idleyenda, leyenda AS leyendaCompleta, grupodefault, leyenda FROM siscert_leyendas WHERE grupo=1 and idLeyenda<>1 ORDER BY idleyenda");
        while (rs.next())
        {
            fila = new Object[3];
            filaOculta = new Object[2];
            filaOculta[0]=rs.getString("idleyenda");
            filaOculta[1]=rs.getString("leyendaCompleta");
            
            //rbtnPrueba = new javax.swing.JRadioButton();
            //rbtnPrueba.setSelected(rs.getBoolean("grupodefault"));
            fila[0]=rs.getBoolean("grupodefault");
            fila[1]=rs.getString("leyenda");
            
            fila[1]=(""+fila[1]).replace(PRELEYENDA_LUGARVALID, "").trim();
            //if (fila[1].length()>41)
            //    fila[2]=fila[1].substring(0,19) + "..."+ fila[1].substring(fila[1].length()-19);
            
            modelLeyendas1.addRow(fila,filaOculta);
        }
    }
    
    public void guardarDefaultLeyenda1 (SISCERT_ModeloDeTabla modelLeyendas1) throws SQLException
    {
        for (int i=0; i<modelLeyendas1.getRowCount(); i++){
            if (modelLeyendas1.getHiddenValueAt(i, 0).equals(0))
                stm.execute("INSERT INTO siscert_leyendas (leyenda, grupo, grupodefault) VALUES '"+modelLeyendas1.getHiddenValueAt(i, 1) +"',1,false");
            if (modelLeyendas1.getValueAt(i, 0).equals(true)){
                stm.execute("UPDATE siscert_leyendas set grupodefault=false WHERE grupo=1");
                stm.execute("UPDATE siscert_leyendas set grupodefault=true WHERE leyenda='"+modelLeyendas1.getHiddenValueAt(i, 1)+"' AND grupo=1");
            }
        }
    }
    
    public String agregarLeyenda (String leyenda) throws SQLException
    {
        String newid="";
        
        rs = stm.executeQuery("SELECT count(idleyenda) AS num FROM siscert_leyendas WHERE leyenda='"+leyenda+"' AND grupo=1");
        if (rs.next())
        {
            if (rs.getInt("num")==0){
                rs = stm.executeQuery("SELECT MAX(idleyenda)+1 idleyenda FROM siscert_leyendas");
                if (rs.next())
                    newid = rs.getString("idleyenda");
                else
                    throw new SQLException ("ID_LEYENDA");
                stm.execute("INSERT INTO siscert_leyendas(idleyenda,leyenda, grupo, grupodefault) VALUES("+newid+",'"+leyenda+"', 1, 'F')");
            }else
                throw new SQLException ("REPETIDO");
        }
        return newid;
    }
    
    public void eliminarLeyenda (String idLeyenda) throws SQLException
    {
        stm.execute("DELETE FROM siscert_leyendas WHERE idleyenda="+idLeyenda);
    }
    
    public void editarLeyenda (String leyenda, String idLeyenda) throws SQLException
    {
        rs = stm.executeQuery("SELECT count(idleyenda_lugvalid) as num FROM siscert_certificacion WHERE idleyenda_lugvalid="+idLeyenda);
        if (rs.next())
        {
            if (rs.getInt("num")==0)
                stm.execute("UPDATE siscert_leyendas SET leyenda='"+leyenda+"' WHERE idleyenda='"+idLeyenda+"'");
            else
                throw new SQLException ("USADA");
        }
    }

//**************************************************************************************************************************
//********************************* METODOS USADOS EN EL FORMULARIO EditarFolios y PrepararImpre ***************************
//**************************************************************************************************************************
    public void getFoliosImpresos(int cveplan, SISCERT_ModeloDeTabla modelFolImpres, int casoFolio, String texto1, String texto2, String tipoFolFin, SISCERT_GlobalMethods global) throws SQLException {
        Object []fila;
        Object []filaOculta;
        String queryFolioFin="", rodac, sqry="";
        int i=1;
        if (casoFolio==1){
            if (tipoFolFin.equals(" "))
                sqry = "AND fi.foliolet='"+texto1.substring(0,4)+"' AND fi.folionum="+texto1.substring(4);
            else if (tipoFolFin.equals("Al último folio registrado"))
                sqry = "AND fi.foliolet='"+texto1.substring(0,4)+"' AND fi.folionum>="+texto1.substring(4);
            else if (tipoFolFin.equals("Al folio"))
                sqry = "AND fi.foliolet='"+texto1.substring(0,4)+"' AND fi.folionum BETWEEN "+texto2.substring(4)+" AND "+texto2.substring(4);
        }else if (casoFolio==2)
            sqry = "AND fi.fechainsert BETWEEN TO_DATE('"+texto1+"','%d/%m/%Y') AND TO_DATE('"+texto2+"','%d/%m/%Y')";
        else if (casoFolio==3)
            sqry = "AND fi.curp='"+texto1+"'";

        rs = stm.executeQuery("SELECT fi.idfolimpre, fi.foliolet,fi.folionum,NVL(fi.rodac,'') AS rodac,fi.cicescinilib,fi.nombre,fi.primerape,fi.segundoape,fi.curp, "
                            + "CASE fi.estatus_impre "
                              + "WHEN 'T' THEN 'ANVERSO IMPRESO' "
                              + "WHEN 'C' THEN 'CANCELADO' "
                              + "WHEN 'I' THEN 'IMPRESO' "
                              + "ELSE fi.estatus_impre "
                            + "END AS estatus_impre, "
                            + "f.formato, fi.fechainsert, today-fi.fechainsert AS dias_que_pasaron "
                            + "FROM siscert_folimpre fi, siscert_folimpre_vars fiv, siscert_impformato f "
                            + "WHERE fi.idfolim_var=fiv.idfolim_var AND fi.idformato=f.idformato "
                                    + " AND fi.cveplan="+cveplan+" AND fiv.cveunidad='"+global.cveunidad+"' "
                                    + sqry + " "
                              + "ORDER BY fi.cicescinilib, fi.folionum");
        while (rs.next()){
            fila = new Object[modelFolImpres.getColumnCount()];
            filaOculta = new Object[1];
            fila[0]=true;
            fila[1]=""+(i++);
            fila[2]= rs.getString("idfolimpre");
            fila[3]= global.intToFolio(rs.getString("foliolet"), rs.getLong("folionum"), 7);
            fila[4]= rs.getString("nombre")+" "+rs.getString("primerape")+" "+rs.getString("segundoape");
            fila[5]= rs.getString("curp");
            fila[6]= rs.getString("formato");
            fila[7]= rs.getString("fechainsert");
            filaOculta[0] = rs.getString("dias_que_pasaron");
            modelFolImpres.addRow(fila,filaOculta);
        }
    }
    
    public void getFoliosForCancel(int cveplan, SISCERT_ModeloDeTabla modelFolImpres, String folioIni, String folioFin, SISCERT_GlobalMethods global) throws SQLException {
        Object []fila;
        int numCols = modelFolImpres.getColumnCount();
        rs = stm.executeQuery("SELECT fi.idfolimpre, fi.foliolet,fi.folionum,fi.nombre,fi.primerape,fi.segundoape,fi.curp, CASE fi.estatus_impre WHEN 'T' THEN 'ANVERSO IMPRESO' WHEN 'C' THEN 'CANCELADO' WHEN 'I' THEN 'IMPRESO' ELSE fi.estatus_impre END as estatus_impre, (TO_CHAR(fi.fechaInsert,'%d/%m/%Y')||' '||hora) AS fechaInsert "
                + "FROM siscert_folimpre fi, siscert_folimpre_vars fiv  "
                + "WHERE fi.idfolim_var=fiv.idfolim_var AND fi.foliolet='"+folioIni.charAt(0)+"' AND fi.folionum BETWEEN "+folioIni.substring(1)+" AND "+folioFin.substring(1)+" "
                + "AND fi.estatus_impre!='C' AND fi.cveplan="+cveplan+" AND fiv.cveunidad='"+global.cveunidad+"' ORDER BY fi.folionum");
        while (rs.next()){
            fila = new Object[numCols];
            fila[0]= true;
            fila[1]= rs.getString("idfolimpre");
            fila[2]= global.intToFolio(rs.getString("foliolet"), rs.getLong("folionum"), 7);
            fila[3]= rs.getString("nombre")+" "+rs.getString("primerape")+" "+rs.getString("segundoape");
            fila[4]= rs.getString("curp");
            fila[5]= rs.getString("estatus_impre");
            fila[6]= rs.getString("fechaInsert");
            modelFolImpres.addRow(fila);
        }
    }
    
    //OJO: Usar transacción para usar este método
    public void cancelarFolios___(SISCERT_ModeloDeTabla modelFoliosImpresos) throws SQLException 
    {
        int numFilas=modelFoliosImpresos.getRowCount(), algo;
        String rodac;
        
        for (int i=0; i<numFilas;i++){
            if (modelFoliosImpresos.getValueAt(i,0).equals(true))
            {
                rs = stm.executeQuery("SELECT rodac FROM siscert_folimpre WHERE idfolimpre="+modelFoliosImpresos.getValueAt(i, 1)+" AND estatus_impre!='C'");
                if (rs.next() && (rodac=rs.getString("rodac"))!=null && !rodac.equals("0") && !rodac.equals("-1") ){
                    stm.execute("INSERT INTO rodac_disp (curpenviada, idaluenviado, nombre, apepat, apemat, foliorodac, cveplan, cicescinilib, idalu, ocupado, fecha, tipodoc) "
                            + "SELECT curp, idalu, nombre,primerape,segundoape,rodac,cveplan,cicescinilib, idalu, 'NO' AS ocupado, date(current), 'DUP' FROM siscert_folimpre WHERE idfolimpre="+modelFoliosImpresos.getValueAt(i, 1));
                    stm.execute("DELETE FROM rodac WHERE foliorodac="+rodac);
                }
                stm.execute("UPDATE siscert_folimpre SET estatus_impre='C' WHERE idfolimpre="+modelFoliosImpresos.getValueAt(i, 1));
            }
        }
    }
    
    //OJO: Usar transacción para usar este método
    public void reasignarFolios___(SISCERT_ModeloDeTabla modelFoliosImpresos, String folioIni, SISCERT_GlobalMethods global) throws SQLException 
    {
        int numFilas=modelFoliosImpresos.getRowCount();
        int folioNegativo;
        String literal = (""+folioIni.charAt(0)).toUpperCase();
        Long numero = Long.parseLong(folioIni.substring(1));
        
        try{
            //Liberamos los folios poniéndolos como negativo
            folioNegativo = Integer.parseInt(obtenerDato("SELECT MIN(folionum)-1 FROM siscert_folimpre"));
            for (int i=0; i<numFilas; i++){
                if (modelFoliosImpresos.getValueAt(i,0).equals(true))
                    stm.execute("UPDATE siscert_folimpre SET foliolet='-', folionum="+(folioNegativo--)+" WHERE idfolimpre="+modelFoliosImpresos.getValueAt(i, 1));
            }
            //Actualizamos folios
            for (int i=0; i<numFilas; i++){
                if (modelFoliosImpresos.getValueAt(i,0).equals(true)){
                    stm.execute("UPDATE siscert_folimpre SET foliolet='"+literal+"', folionum="+numero+" WHERE idfolimpre="+modelFoliosImpresos.getValueAt(i, 1));
                    numero++;
                }
            }
        }catch (SQLException ex){
            if (ex==null || ex.getMessage()==null )
                throw new SQLException (ex);
            else if (ex.getMessage().toUpperCase().contains("UNIQUE") && ex.getMessage().toUpperCase().contains("FOLIO_UK"))
                throw new SQLException ("FOLIO_REPETIDO "+global.intToFolio(""+literal, numero, 7));
            else
                throw new SQLException (ex.getMessage());
        }
    }
//****************************************************************************************************************
//*********************************** METODOS USADOS EN EL FORMULARIO Impresion *****************************
//****************************************************************************************************************
    /*public String getIdVars_folImpre_(SISCERT_GlobalMethods global,SISCERT_ModeloDeTabla tablaFolios, int tamFilas, String []idVar) throws SQLException 
    {
        while(true){
            //Buscamos si ya está registrado un id para las variables que usaremos
            rs = stm.executeQuery("SELECT idfolim_var FROM siscert_folimpre_vars "
                                + "WHERE cveunidad='"+global.cveunidad+"' AND delegacion='"+global.globalDelegacion+"' "
                                + "AND cctdeleg='"+global.globalCCTDelegacion+"' AND lugar_expedicion='"+global.globalLugarExpedicion+"' "
                                + "AND delegado='"+global.globalDelegado+"' AND cargodelegado='"+global.globalCargoDelegado+"' AND cotejo='"+global.globalCotejo+"' ");
            if (rs.next()){
                idVar[0]=rs.getString("idfolim_var");                               //Obtenemos el id para las variables
                return "";
            }
            //Si no encontramos el id, entonces vamos a insertar los datos,
            //pero antes, revisamos que al menos uno de los folios que insertaremos no exista en BD, para que valga la pena crearle un id de variable,
            for (int i=0; i<tamFilas; i++)
                if (tablaFolios.getValueAt(i, 0).equals(true) && !tablaFolios.getValueAt(i, 1).equals("")){
                    rs = stm.executeQuery("SELECT idfolimpre FROM siscert_folimpre WHERE foliolet='"+tablaFolios.getValueAt(i, 1).toString().substring(0,1)+"' AND folionum="+tablaFolios.getValueAt(i, 1).toString().substring(1));
                    if (rs.next()) return tablaFolios.getValueAt(i, 1).toString();//si existe el folio, entonces ya no insertamos las variables para crear un nuevo id
                    break;                                                      //Si el primer folio revisado no está repetido, con eso vale la pena crearle un id
                }
            //Insertamos el nuevo grupo de variables
            stm.execute("INSERT INTO siscert_folimpre_vars (cveunidad,delegacion,cctdeleg,lugar_expedicion,delegado,cargodelegado,cotejo) values ('"+global.cveunidad+"','"+global.globalDelegacion+"',"
                            + "'"+global.globalCCTDelegacion+"','"+global.globalLugarExpedicion+"','"+global.globalDelegado+"','"+global.globalCargoDelegado+"','"+global.globalCotejo+"')");            
        }
    }*/
    
    //OJO: Usar transacción para usar este método
    public boolean getAndSaveEnFoliosImpre___ (String folio,String lado, String NoControl,String idVarImpre, SISCERT_GlobalMethods global, boolean isImpresionDePrueba) throws SQLException, Exception
    {
        boolean ok;
        String modalidad, mod, idfolimpre, estatusImpre="T";
        String rodac="0";
        
        //rodac, rodac_rango, rodac_disp
        if (selecAlumnoAnvRevToPrint (NoControl, global.cveunidad, lado, global))
        {
            rs.close();
            if (!isImpresionDePrueba){
                if (global.idFormatCertAImprimir!=2 &&  (global.idAluSICEEB==null || global.idAluSICEEB.equals("")))
                    throw new Exception ("SIN_IDALU*"+global.nombre+" "+global.primerApe+" "+global.segundoApe+", con N.C: "+global.idcertificacion);
                //-------------------- Extraemos la modalidad del CCT del alumno -----------------------------
                mod = (global.cctEscuela.length()>5)?global.cctEscuela.substring(2, 5):"";
                if (mod.equals("DST") || mod.equals("PST")) modalidad = "T";        //Modalidad Secundaria técnica
                else modalidad = (mod.equals("DTV") || mod.equals("PTV"))? "L":"G";  //Modalidad Telesecundaria o General respectivamente
                //--------------------------------------------------------------------------------------------
                //--------------------------- INSERTAMOS DATOS DE ALUMNO -------------------------------------
                if (lado.equals("ANVERSO")){
                    try {
                        if (global.idFormatCertAImprimir!=2 && global.idFormatCertAImprimir<8) //Para formatos menores a 'FORMATO GRIS JUL2016'
                            rodac = obtenerRodac(global, global.cicescinilib);
                        if (global.idFormatCertAImprimir>=8)
                            estatusImpre="I"; //judiridico, cct, escuela, folio

                        Object datosParaQry[][] = new Object[][]{
                            {"idcertificacion",global.idcertificacion},
                            {"idalu",global.idAluSICEEB},
                            {"idfolim_var",global.idfolim_var},
                            {"foliolet","'"+folio.substring(0, 1)+"'"},
                            {"folionum",folio.substring(1)},
                            {"cveunidad","'"+global.cveunidad+"'"},
                            {"cveplan",global.cveplan},
                            {"numsolicitud",global.numSolicitud},
                            {"cicescinilib",global.cicescinilib},
                            {"cebas","'"+(global.cebas?"t":"f")+"'"},
                            {"nombre","\""+global.nombre+"\""},
                            {"primerape","\""+global.primerApe+"\""},
                            {"segundoape","\""+global.segundoApe+"\""},
                            {"idcasocurp",global.idcasocurp},
                            {"curp","'"+global.curp+"'"},
                            {"cicescini",((global.cicescini.equals(""))?"null":global.cicescini)},
                            {"cicescfin",((global.cicescfin.equals(""))?"null":global.cicescfin)},
                            {"prom_educprim","'"+global.promNum_EducPrim.trim()+"'"},
                            {"promedio","'"+global.promedioNum.trim()+"'"},
                            {"prom_letra","'"+global.promedioLetra+"'"},
                            {"prom_educbasic","'"+global.promNum_educBasic.trim()+"'"},
                            {"promlet_educbasic","'"+global.promLet_educBasic.trim()+"'"},
                            {"promlet_primsec_edubas",(global.idFormatCertAImprimir<8)?"null":"'"+global.promedioPrimSecEducBasic+"'"},
                            {"dia_acredi","'"+global.diaAcreditacion+"'"},
                            {"dia_exped_let","'"+global.diaExped_let.trim()+"'"},
                            {"mes_acredi","'"+global.mesAcreditacion.trim()+"'"},
                            {"fecha_acredi_let","'"+global.fechaAcrediLet+"'"},
                            {"curso_acredito",(global.idFormatCertAImprimir<8)?"null":"'"+global.cursoYAcredito+"'"},
                            {"mesanio_exped_let","'"+global.mesAñoExpedicion+"'"},
                            {"fecha_exped_let","'"+global.fechaExpedLet+"'"},

                            {"libro","'"+global.libro+"'"},
                            {"foja","'"+global.foja+"'"},
                            {"folio",(global.idFormatCertAImprimir<8)?"null":"'"+global.folio+"'"},
                            {"escuela",(global.idFormatCertAImprimir<8)?"null":"\""+global.escuela+"\""},
                            {"cct",(global.idFormatCertAImprimir<8)?"null":"'"+global.cctEscuela+"'"},
                            {"fecha","'"+global.fechaExpedicion+"'"},  /*ok*/
                            {"juridico",(global.idFormatCertAImprimir<8)?"null":"'"+global.juridico+"'"},
                            //{"modalidad","null"},
                            //{"plan_estud","null"},

                            {"idleyenda_lugvalid",global.idleyenda_lugvalid},
                            {"rodac","'"+((rodac.equals("0"))?"-1":(global.folioRodac=""+rodac))+"'"},
                            {"estatus_impre","'"+estatusImpre+"'"},
                            {"idformato",global.idFormatCertAImprimir},
                            {"usuario","'"+global.capturista+"'"},
                            {"fechainsert","date(current)"},
                            {"hora","extend(current, hour to minute)"}
                        };

                        stm.executeUpdate( crearQueryInsert("siscert_folimpre",datosParaQry) );

                        /*stm.execute("INSERT INTO siscert_folimpre (idcertificacion, idalu,idfolim_var,foliolet,folionum,cveunidad,cveplan,numsolicitud,cicescinilib,cebas,"
                                        + "nombre,primerape,segundoape,idcasocurp,curp,cicescini,cicescfin,prom_educprim,promedio,prom_letra,prom_educbasic,promlet_educbasic, dia_acredi, "
                                        + "dia_exped_let, mes_acredi, fecha_acredi_let, mesanio_exped_let, fecha_exped_let, fecha, idleyenda_lugvalid, rodac, estatus_impre,idformato,"
                                        + "usuario,fechainsert,hora) "
                                + "VALUES ("+global.idalumno+", "+global.idAluSICEEB+", "+global.idfolim_var+", '"+folio.substring(0, 1)+"', "+folio.substring(1)+", '"+global.cveunidad+"', "
                                + ""+global.cveplan+", "+global.numSolicitud+", "+global.cicescinilib+", '"+(global.cebas?"t":"f")+"', \""+global.nombre+"\",\""+global.primerApe+"\","
                                + "\""+global.segundoApe+"\","+global.idcasocurp+",'"+global.curp+"',"+((global.cicescini.equals(""))?"null":global.cicescini)+", "
                                + ""+((global.cicescfin.equals(""))?"null":global.cicescfin)+", '"+global.promNum_EducPrim.trim()+"', '"+global.promedioNum.trim()+"', "
                                + "'"+global.promedioLetra+"', '"+global.promNum_educBasic.trim()+"', '"+global.promLet_educBasic.trim()+"', '"+global.diaAcreditacion+"', "
                                + "'"+global.diaExped_let.trim()+"', '"+global.mesAcreditacion.trim()+"', '"+global.fechaAcrediLet+"', '"+global.mesAñoExpedicion+"', "
                                + "'"+global.fechaExpedLet+"', '"+global.fechaExpedicion+"', "+global.idleyenda_lugvalid+", '"+((rodac.equals("0"))?"-1":(global.folioRodac=""+rodac))+"', "
                                + "'"+estatusImpre+"',"+global.idFormatCertAImprimir+",'"+global.capturista+"',date(current), extend(current, hour to minute) )");*///2012/03/14
                        if (global.idFormatCertAImprimir!=2 && !rodac.equals("0") && !rodac.equals("-1"))
                            stm.execute("DELETE FROM rodac_disp WHERE foliorodac="+rodac);
                    }catch(SQLException ex){
                        if (ex.getMessage().contains("Unique constraint"))//flio repetido
                        {
                            rs = stm.executeQuery("SELECT CASE fi.estatus_impre WHEN 'T' THEN 'ANVERSO IMPRESO' WHEN 'C' THEN 'CANCELADO' WHEN 'I' THEN 'IMPRESO' ELSE fi.estatus_impre END as estatus_impre, " +
                                    "u.desunidad, CASE fi.cveplan WHEN 3 THEN 'PREESCOLAR' WHEN 2 THEN 'SECUNDARIA' WHEN 1 THEN 'PRIMARIA' END AS nivel, (fi.cicescinilib||'-'||(fi.cicescinilib+1)) AS libro, (nombre||'*'||primerape||'/'||segundoApe) AS nom_tot, curp " +
                                    "FROM  siscert_folimpre fi, siscert_folimpre_vars fiv, unidad u " +
                                    "WHERE fi.idfolim_var=fiv.idfolim_var AND fiv.cveunidad=u.cveunidad AND foliolet='"+folio.substring(0, 1)+"' AND folionum="+folio.substring(1) );
                            if (rs.next())
                                throw new Exception ("FOL_REP*"+folio+"\ncon el que desea imprimir se encuentra con:\nestatus '"+rs.getString("estatus_impre").trim()+"',\nasignado en '"+rs.getString("desunidad").trim()+"',\nde nivel "+rs.getString("nivel")+"\nlibro: "+rs.getString("libro")+"\nAlumno: "+rs.getString("nom_tot")+"\nCURP:"+rs.getString("curp")+".");
                            else
                                throw new SQLException (ex.getMessage());
                        }else
                            throw new SQLException (ex.getMessage());

                    }catch(Exception ex){ throw new Exception (ex.getMessage()); }

                }else if (lado.equals("REVERSO"))
                {
                    rs = stm.executeQuery("SELECT estatus_impre, idfolimpre FROM  siscert_folimpre WHERE foliolet='"+folio.substring(0, 1)+"' AND folionum="+folio.substring(1) +" AND idcertificacion="+global.idcertificacion+" AND idfolim_var="+global.idfolim_var+" AND (today-fechainsert)<=1"); //Tiene un día para mandar a imprimir
                    if (rs.next()){
                        if ((estatusImpre=rs.getString("estatus_impre")).equals("T")){
                            idfolimpre = rs.getString("idfolimpre");
                            stm.execute("UPDATE siscert_folimpre SET libro='"+global.libro+"', foja='"+global.foja+"', folio='"+global.folio+"', escuela='"+global.escuela+"', cct='"+global.cctEscuela+"', "
                                    + "fecha='"+global.fechaExpedicion+"', juridico='"+global.juridico+"', modalidad='"+modalidad+"',  plan_estud='"+global.plan+"', idleyenda_lugvalid="+global.idleyenda_lugvalid+",  "
                                    + "estatus_impre='I' WHERE idfolimpre="+idfolimpre);
                        }else if (estatusImpre.equals("C"))
                            throw new Exception ("FOLIO_CANCELADO*"+folio);
                        else if (estatusImpre.equals("I"))
                            throw new Exception ("FOLIO_REPETIDO*"+folio);
                    } else
                        throw new Exception ("SIN_LADO_ANVERSO*"+folio);
                }
            }
            ok = true;
        }else ok = false;
        
        return ok;
    }
    
            //------------- Consulta para seleccionar los datos necesarios del alumno para cargarlos al formulario y editarlos
    public boolean selecAlumnoAnvRevToPrint (String NoControl, String cveunidad, String lado, SISCERT_GlobalMethods global) throws SQLException, Exception
    {
        String idcertificacion = obtenerIdAlumno(cveunidad, NoControl, ""+global.cveplan);

        rs = stm.executeQuery("SELECT " +
                                "f.idformato, (SELECT grupo FROM folios_impre fi WHERE fi.idalu=c.idalu AND fi.cveplan=c.cveplan) AS grupo, " +
                                "idcertificacion, idalu, idcertiregion, numsolicitud, cicescinilib, cebas, TRIM(nombre) AS nombre, TRIM(apepat) AS apepat, "
                                + "TRIM(NVL(apemat,'')) AS apemat, idcasocurp, curp, NVL(ai,'') AS ai, NVL(af,'') AS af, NVL(prom_educprim,'') AS prom_educprim, "
                                + "NVL(promedio,'') AS promedio, NVL(prom_letra,'') AS prom_letra, NVL(prom_educbasic,'') AS prom_educbasic, "
                                + "NVL(promlet_educbasic,'') AS promlet_educbasic, dia_exped_let, NVL(dia_acredi,'') AS dia_acredi, "
                                + "NVL(mes_acredi,'') AS mes_acredi, NVL(mesanio_exped_let,'') AS mesanio_exped_let, "
                                + "TRIM(libro) AS libro, TRIM(foja) AS foja, folio, TRIM(escuela) AS escuela, "
                                + "TRIM(cct) AS cct, NVL(cicinilib_cert,'') AS cicinilib_cert, fecha, TRIM(juridico) AS juridico, "
                                + "CASE edu_sec WHEN 'X' THEN 'G' ELSE CASE sec_tec WHEN 'X' THEN 'T' ELSE CASE tele_sec WHEN 'X' THEN 'L' ELSE '' END END END AS modalidad,  " +
                                "l.idleyenda, l.leyenda AS lugvalid, NVL(plan_estud,'') AS plan_estud, fiv.idfolim_var, fiv.delegacion, fiv.cctdeleg, fiv.lugar_expedicion, "
                                + "fiv.delegado, fiv.cargodelegado, fiv.cotejo, f.formato " +
                            "FROM siscert_certificacion c, siscert_leyendas l, siscert_folimpre_vars fiv, siscert_impformato f " +
                            "WHERE c.idleyenda_lugvalid=l.idleyenda AND c.idfolim_var=fiv.idfolim_var AND c.idformato=f.idformato AND c.idcertificacion = " + idcertificacion);
        //************** Cargamos los datos del alumno **************
        if (rs.next()){
            global.setDatosCertificado ("IMPRESION",global.cveunidad,lado, rs);
            return true;
        }
        return false;
    }
    
    public boolean getFoliosParaReimprimir (String lado, String idfolimpre, SISCERT_GlobalMethods global) throws SQLException, Exception
    {
        boolean ok=false;
        rs = stm.executeQuery("SELECT " +
                            "'X' AS grupo, " +
                            "fi.idcertificacion, fi.idalu, 'idcertiregion' AS idcertiregion, numsolicitud, cicescinilib, fi.cebas, fi.nombre, fi.primerape AS apepat, " +
                            "fi.segundoape AS apemat, fi.idcasocurp, fi.curp, fi.cicescini AS ai, fi.cicescfin AS af, fi.prom_educprim, fi.promedio, fi.prom_letra, " +
                            "fi.prom_educbasic, fi.promlet_educbasic, fi.dia_exped_let, fi.dia_acredi, fi.mes_acredi, fi.fecha_acredi_let, fi.mesanio_exped_let, fi.fecha_exped_let, " +
                            "NVL(fi.libro,'') AS libro, NVL(fi.foja,'') AS foja, fi.folio, fi.escuela, fi.cct, fi.curso_acredito, fi.fecha, fi.juridico, fi.modalidad, fi.idleyenda_lugvalid AS idleyenda, l.leyenda AS lugvalid, fi.plan_estud, " +
                            "fi.idfolim_var, fiv.delegacion, fiv.cctdeleg, fiv.lugar_expedicion, fiv.delegado, fiv.cargodelegado, fiv.cotejo, fi.idformato, f.formato, fi.rodac " +
                            "FROM siscert_folimpre fi, siscert_leyendas l, siscert_folimpre_vars fiv, siscert_impformato f " +
                            "WHERE fi.idleyenda_lugvalid=l.idleyenda AND fi.idfolim_var=fiv.idfolim_var AND fi.idformato=f.idformato AND " +
                            "fi.idfolimpre=" + idfolimpre);
        if (rs.next())
        {
            global.setDatosCertificado ("REIMPRESION",global.cveunidad, lado, rs);                              //Cargamos los datos del alumno
            global.folioRodac = rs.getString("rodac");
            if (global.idFormatCertAImprimir!=2 &&  (global.idAluSICEEB==null || global.idAluSICEEB.equals("")))
                throw new Exception ("SIN_IDALU*"+global.nombre+" "+global.primerApe+" "+global.segundoApe+", con N.C: "+global.idcertificacion);
            ok = true;
        }else ok = false;

        return ok;
    }
    
    private String obtenerRodac (SISCERT_GlobalMethods global, String cicescinilib) throws SQLException, Exception
    {
        int rodac=-1;
        boolean commit=false;
        try 
        {
            rs = stm.executeQuery("SELECT MIN(foliorodac) as rodac FROM rodac_disp WHERE cicescinilib="+cicescinilib+" AND ocupado='NO' AND cveplan="+global.cveplan+" AND tipodoc='DUP' AND (today-fecha)>=3");
            if (rs.next() && (rodac= rs.getInt("rodac"))!=0){
                stm.execute("UPDATE rodac_disp SET curpenviada='"+global.curp+"', idaluenviado="+global.idAluSICEEB+", nombre=\""+global.nombre+"\", apepat=\""+global.primerApe+"\", apemat=\""+global.segundoApe+"\", cveplan="+global.cveplan+", idalu="+global.idAluSICEEB+", ocupado='SI', fecha=date(current) WHERE cicescinilib="+cicescinilib+" AND foliorodac="+rodac+" AND cveplan="+global.cveplan+" AND tipodoc='DUP'");
                this.hacerCommit(); commit=true;
            }

            if (rodac==0)
            {
                rs = stm.executeQuery("SELECT CASE WHEN (folioact+1)<=foliofin THEN folioact+1 ELSE 0 END AS rodac FROM rodac_rango WHERE cicescinilib="+cicescinilib+" AND estatus='A' AND cveplanrodac = '"+global.cveplan+"' AND tipodoc = 'DUP'");
                if (rs.next()){
                    if ((rodac = rs.getInt("rodac"))!=0){
                        stm.execute("UPDATE rodac_rango SET folioact=folioact+1 WHERE cicescinilib="+cicescinilib+" AND estatus='A' AND cveplanrodac = '"+global.cveplan+"' AND tipodoc = 'DUP'");
                        stm.execute("INSERT INTO rodac_disp (curpenviada, idaluenviado , nombre, apepat, apemat, foliorodac, cveplan, cicescinilib, idalu, ocupado, fecha, tipodoc) "
                                + "VALUES ('"+global.curp+"', "+global.idAluSICEEB+", \""+global.nombre+"\",\""+global.primerApe+"\",\""+global.segundoApe+"\", "+rodac+", "+global.cveplan+", "+cicescinilib+", "+global.idAluSICEEB+", 'SI', date(current), 'DUP')");
                        this.hacerCommit();   commit=true;
                    }
                }
            }

            if (rodac>0){
                stm.execute("INSERT INTO rodac (curpenviada, idaluenviado , nombre, apepat, apemat, foliorodac, cveplan, cicescinilib, idalu, fecha, tipodoc, fecha_aux) "
                                + "VALUES ('"+global.curp+"', "+global.idAluSICEEB+", \""+global.nombre+"\",\""+global.primerApe+"\",\""+global.segundoApe+"\", "+rodac+", "+global.cveplan+", "+cicescinilib+", "+global.idAluSICEEB+", date(current), 'DUP', current)");
            }
            
            if (rodac<=0)
                return "-1";
        }catch (SQLException ex){
            if (commit){
                stm.execute("UPDATE rodac_disp SET ocupado='NO', fecha=date(current) WHERE foliorodac="+rodac+" AND tipodoc='DUP'");
                this.hacerCommit();
            }
            throw new SQLException ("ERROR_RODAC_EXTRACT*"+ex.getMessage());
        }
        return ""+rodac;
    }
   
    public String obtenerIdAlumno(String cveunidad, String NoControl, String cveplan) throws SQLException
    {
        String idTabla = "";
        rs = stm.executeQuery("SELECT idcertificacion FROM siscert_certificacion WHERE idcertiregion = "+NoControl+" AND cveunidad = '" + cveunidad + "' AND cveplan="+cveplan);
        if (rs.next())
            idTabla=rs.getString("idcertificacion");
        return idTabla;
    }
    
//****************************************************************************************************************
//*************************** METODOS USADOS EN EL FORMULARIO AdministrarCoordenadas *****************************
//****************************************************************************************************************
    public void getCveunidades (JComboBox unidadesOrigen, JComboBox unidadesDestino_copiar, JComboBox unidadesDestino_eliminar, JComboBox unidadesDestino_crear, JComboBox cbxCveunidad_NuevoForm) throws SQLException
    {
        String unidades;
        rs = stm.executeQuery("SELECT DISTINCT(cveunidad) AS cveunidad FROM siscert_impcoordenadas ORDER BY cveunidad");
        while (rs.next()){
            unidadesOrigen.addItem(unidades=rs.getString("cveunidad"));
            unidadesDestino_copiar.addItem(unidades);
            unidadesDestino_eliminar.addItem(unidades);
            unidadesDestino_crear.addItem(unidades);
            cbxCveunidad_NuevoForm.addItem(unidades);
        }
    }
    
    public void getFormatosCert (ArrayList<String[]> formatosImpre, JComboBox formatos, JComboBox formato_NuevoForm) throws SQLException
    {
        String fila [];
        rs = stm.executeQuery("SELECT idformato, formato FROM siscert_impformato");
        while (rs.next()){
            fila = new String[2];
            fila[0]=rs.getString("idformato");
            fila[1]=rs.getString("formato");
            formatos.addItem(fila[1]);
            formato_NuevoForm.addItem(fila[1]);
            formatosImpre.add(fila);
        }
    }
    
    public void getFuentes (ArrayList<String> idsFuente, JComboBox fuentes, JComboBox fuente_NuevoForm) throws SQLException
    {
        String fuente;
        rs = stm.executeQuery("SELECT idfuente, tamanio,fuente  FROM siscert_imptipografia");
        while (rs.next()){
            idsFuente.add(rs.getString("idfuente"));
            fuente = rs.getString("tamanio")+" - "+rs.getString("fuente");
            fuentes.addItem(fuente);
            fuente_NuevoForm.addItem(fuente);
        }
    }
    
    public void getCoordenadas (String cveunidad, int cveplan, String formatoImpresion, SISCERT_ModeloDeTabla modelCoordenadas) throws SQLException
    {
        Object []fila ;
        int numCols = modelCoordenadas.getColumnCount();
        String sqryCveplan = (cveplan>0)?"AND c.cveplan="+cveplan:"";
        
        rs = stm.executeQuery("SELECT c.idcoordenada,t.tamanio, t.fuente, c.ordenimpre, l.idleyenda, l.nombreleyenda, l.leyenda, c.x, c.y, c.lado " +
                            "FROM siscert_impcoordenadas c, siscert_impleyendas l, siscert_imptipografia t, siscert_impformato f " +
                            "WHERE c.idleyenda=l.idleyenda AND c.idfuente=t.idfuente AND c.idformato=f.idformato " +
                            "and c.cveunidad='"+cveunidad+"' AND f.formato='"+formatoImpresion+"' " + sqryCveplan + " " +
                            "ORDER BY c.ordenimpre");
        while (rs.next()){
            fila = new Object[numCols];
            fila[0]=rs.getInt("idcoordenada");
            fila[1]=rs.getInt("tamanio");
            fila[2]=rs.getString("fuente");
            fila[3]=rs.getInt("ordenimpre");
            fila[4]=rs.getInt("idleyenda");
            fila[5]=rs.getString("nombreleyenda");
            fila[6]=rs.getString("leyenda");
            fila[7]=rs.getInt("x");
            fila[8]=rs.getInt("y");
            fila[9]=rs.getString("lado");
            modelCoordenadas.addRow(fila);
        }
            
    }
    
    public void getLeyendas (SISCERT_ModeloDeTabla modelLeyendas) throws SQLException
    {
        Object fila[];
        int numCols = modelLeyendas.getColumnCount();
        rs = stm.executeQuery("SELECT idleyenda, nombreleyenda, leyenda FROM siscert_impleyendas ORDER BY idleyenda");
        while (rs.next())
        {
            fila = new Object[numCols];
            fila[0]=rs.getString("idleyenda");
            fila[1]=rs.getString("nombreleyenda");
            fila[2]=rs.getString("leyenda");
            modelLeyendas.addRow(fila);
        }
    }
    
    //OJO: Usar transacción para usar este método
    public void copiarCoordenadas___ (String caso, String idformato, int cveplan, String cveunidadDestino, JTable coordenadas) throws SQLException, Exception
    {
        int []filasSel;
        String filtroCveunidad="";
        if (!cveunidadDestino.equals("TODAS"))
            filtroCveunidad=" AND cveunidad='"+cveunidadDestino+"' ";
        
        if (caso.equals("SELECCION")){
            filasSel = coordenadas.getSelectedRows();
            for (int i=0; i<filasSel.length; i++)
                stm.execute("UPDATE siscert_impcoordenadas SET x="+coordenadas.getValueAt(filasSel[i], 7)+", y="+coordenadas.getValueAt(filasSel[i], 8)+" WHERE cveplan="+cveplan+filtroCveunidad+" AND idformato="+idformato+" AND idleyenda="+coordenadas.getValueAt(filasSel[i], 4));
        }else if (caso.equals("TODO")){
            for (int i=0; i<coordenadas.getRowCount(); i++)
                stm.execute("UPDATE siscert_impcoordenadas SET x="+coordenadas.getValueAt(i, 7)+", y="+coordenadas.getValueAt(i, 8)+" WHERE cveplan="+cveplan+filtroCveunidad+" AND idformato="+idformato+" AND idleyenda="+coordenadas.getValueAt(i, 4));
        }
        
    }
    
    //OJO: Usar transacción para usar este método
    public void eliminarCoordenadas___ (String caso, String idformato, int cveplan, String cveunidadDestino, JTable coordenadas) throws SQLException, Exception
    {
        int []filasSel;
        String filtroCveunidad="";
        if (!cveunidadDestino.equals("TODAS"))
            filtroCveunidad=" AND cveunidad='"+cveunidadDestino+"' ";
        
        if (caso.equals("SELECCION")){
            filasSel = coordenadas.getSelectedRows();
            for (int i=0; i<filasSel.length; i++)
                stm.execute("DELETE FROM siscert_impcoordenadas WHERE cveplan="+cveplan+filtroCveunidad+" AND idformato="+idformato+" AND idleyenda="+coordenadas.getValueAt(filasSel[i], 4));
        }else if (caso.equals("TODO")){
            stm.execute("DELETE FROM siscert_impcoordenadas WHERE cveplan="+cveplan+filtroCveunidad+" AND idformato="+idformato);
        }
        
    }
    
    //OJO: Usar transacción para usar este método
    public void asignarPaqueteDeCoordenadas___ (String cveunidad, int cveplan, String idfuente, String idformato, JTable coordenadas, JComboBox cbxCveunidadOrigen) throws SQLException, Exception
    {
        String cveunidaddest;
        int idCoordenada;
        
        idCoordenada = getNewIdNumTabla("idcoordenada", "siscert_impcoordenadas");
        for (int j=0; j<cbxCveunidadOrigen.getItemCount(); j++){
            cveunidaddest=""+cbxCveunidadOrigen.getItemAt(j);
            if (!cveunidad.equals("TODAS")){ 
                cveunidaddest=cveunidad; j=cbxCveunidadOrigen.getItemCount()+1; 
            }
            rs = stm.executeQuery("SELECT idcoordenada FROM siscert_impcoordenadas WHERE cveunidad='"+cveunidaddest+"' AND cveplan="+cveplan+" AND idformato="+idformato);
            if (!rs.next())
                for (int i=0; i<coordenadas.getRowCount(); i++){
                    stm.execute("INSERT INTO siscert_impcoordenadas (idCoordenada, cveunidad,cveplan,idfuente,idformato,idleyenda,x,y,ordenimpre,lado) values ("+(idCoordenada++)+", '"+cveunidaddest+"',"+cveplan+","+idfuente+","+idformato+","+coordenadas.getValueAt(i, 4)+","+coordenadas.getValueAt(i, 7)+","+coordenadas.getValueAt(i, 8)+","+coordenadas.getValueAt(i, 3)+",'"+coordenadas.getValueAt(i, 9)+"')");
                }
        }

    }
    
    //OJO: Usar transacción para usar este método
    public void crearPaqueteDeCoordenadas___ (String cveunidad, int cveplan, String idformato, String idfuente, JTable coordenadas, JComboBox cbxCveunidadunidades) throws SQLException, Exception
    {
        String cveunidaddest;
        int idCoordenada;
        idCoordenada = getNewIdNumTabla("idcoordenada", "siscert_impcoordenadas");
        for (int j=1; j<cbxCveunidadunidades.getItemCount(); j++){
            cveunidaddest=""+cbxCveunidadunidades.getItemAt(j);
            if (!cveunidad.equals("TODAS")){ cveunidaddest=cveunidad; j=cbxCveunidadunidades.getItemCount()+1; }
            for (int i=0; i<coordenadas.getRowCount(); i++){
                stm.execute("INSERT INTO siscert_impcoordenadas (idcoordenada, cveunidad,cveplan,idfuente,idformato,idleyenda,x,y,ordenimpre, lado) values ("+(idCoordenada++)+",'"+cveunidaddest+"',"+cveplan+","+idfuente+","+idformato+","+coordenadas.getValueAt(i, 0)+","+(""+coordenadas.getValueAt(i, 3)).trim()+","+(""+coordenadas.getValueAt(i, 4)).trim()+","+(i+1)+",'"+(""+coordenadas.getValueAt(i, 5)).toUpperCase()+"')");
            }
        }
    }
    
    //OJO: Usar transacción para usar este método
    public int crearNuevaLeyenda___ (String nombreLeyenda, String leyenda) throws SQLException, Exception
    {
        int idLeyenda=-1;
        
        idLeyenda = getNewIdNumTabla("idleyenda", "siscert_impleyendas");
        stm.execute("INSERT INTO siscert_impleyendas (idleyenda, nombreleyenda, leyenda) " +
                    "VALUES ("+idLeyenda+",'"+nombreLeyenda+"','"+leyenda+"')");
        
        return idLeyenda;
    }
     
//************************************************************************************************************
//*************************** METODOS USADOS EN EL FORMULARIO AgregarCveunidad *******************************
//************************************************************************************************************    
    //OJO: Usar transacción para usar este método
    public void agregarCveunidad__ (String cveunidad, String cveunidadCoordenadas, SISCERT_ModeloDeTabla modelFormatos, SISCERT_ModeloDeTabla modelCveplanes) throws SQLException, Exception
    {
        String id;
        boolean asignoCoordenadas=false;
        int idCoordenada;
        try 
        {
            id = obtenerDato("SELECT MAX(idusuario)+1 FROM siscert_sisver");
            stm.execute("INSERT INTO siscert_sisver (idusuario,cveunidad,version,nummsgemitidos, lightversion, fullversion,locktoupdate,locksistem, mensaje) "+
                        "VALUES ("+id+",'"+cveunidad.toUpperCase()+"','0',0,'0','0',0,0,'')");
            id = obtenerDato("SELECT MAX(idvariable)+1 FROM siscert_variables");
            stm.execute("INSERT INTO siscert_variables (idvariable,cveunidad,delegacion,cctdelegacion,lugar,delegado,cargodelegado,cotejo_sec,cotejo_pri,cotejo_pre ) " + 
                    "VALUES ("+id+",'"+cveunidad.toUpperCase()+"','','','','','','','','')");
            idCoordenada = getNewIdNumTabla("idcoordenada", "siscert_impcoordenadas");
            for (int i=0; i<modelFormatos.getRowCount(); i++)
                if (modelFormatos.getValueAt(i, 0).equals(true)){
                    for (int j=0; j<modelCveplanes.getRowCount(); j++)
                        if (modelCveplanes.getValueAt(j, 0).equals(true)){
                            stm.execute("INSERT INTO siscert_impcoordenadas (idCoordenada, cveunidad, cveplan, idfuente, idformato, idleyenda, x, y, ordenimpre) " + 
                                        "SELECT "+(idCoordenada++)+", '"+cveunidad+"', cveplan, idfuente, idformato, idleyenda, x, y, ordenimpre " + 
                                        "FROM siscert_impcoordenadas WHERE cveunidad='"+cveunidadCoordenadas+"' AND idformato="+modelFormatos.getValueAt(i, 1)+" AND cveplan="+modelCveplanes.getValueAt(j, 1));
                            asignoCoordenadas=true;
                        }
                }
        }catch (SQLException ex){
            if (asignoCoordenadas) 
            {
                this.hacerRollback();
                stm.execute("ALTER TABLE siscert_impcoordenadas MODIFY idcoordenada INTEGER NOT NULL");
                stm.execute("ALTER TABLE siscert_impcoordenadas MODIFY idcoordenada SERIAL NOT NULL");
                this.hacerCommit();
            }
            throw new SQLException (ex.getMessage());
        }catch (Exception ex){
            if (asignoCoordenadas) 
            {
                this.hacerRollback();
                stm.execute("ALTER TABLE siscert_impcoordenadas MODIFY idcoordenada INTEGER NOT NULL");
                stm.execute("ALTER TABLE siscert_impcoordenadas MODIFY idcoordenada SERIAL NOT NULL");
                this.hacerCommit();
            }
            throw new Exception (ex.getMessage());
        }
   }
    
    public void get_cveunidad_cveplan_impformato (JComboBox unidadesOrigen, SISCERT_ModeloDeTabla modelFormatos) throws SQLException
    {
        Object fila[];
        rs = stm.executeQuery("SELECT cveunidad FROM siscert_sisver WHERE cveunidad!='ADMIN' ORDER BY cveunidad");
        while (rs.next()){
            unidadesOrigen.addItem(rs.getString("cveunidad"));
        }
        
        rs = stm.executeQuery("SELECT idformato, formato FROM siscert_impformato");
        while (rs.next()){
            fila = new Object[modelFormatos.getColumnCount()];
            fila[0]=false;
            fila[1]=rs.getString("idformato");
            fila[2]=rs.getString("formato");
            modelFormatos.addRow(fila);
        }
    }
//****************************************************************************************************************
//************************************ METODOS USADOS EN EL FORMULARIO Usuarios **********************************
//****************************************************************************************************************
    public void getUsrSICEEB (String usuario, SISCERT_ModeloDeTabla modelUsrSICEEB) throws SQLException
    {
        Object []fila;
        rs = stm.executeQuery("SELECT idusuario,TRIM(loginuser) AS loginuser, TRIM(NVL(nombreu,'')) AS nombreu, TRIM(NVL(apepat,'')) AS apepat, TRIM(NVL(apemat,'')) AS apemat, TRIM(NVL(curp,'')) AS curp, TRIM(cveunidad) AS cveunidad "
                + "FROM usuarios WHERE loginuser LIKE '"+usuario.toUpperCase()+"%' "
                + "ORDER BY loginuser");
        while (rs.next())
        {
            fila = new Object[modelUsrSICEEB.getColumnCount()];
            fila[0]=rs.getString("idusuario");
            fila[1]=rs.getString("loginuser");
            fila[2]=rs.getString("nombreu")+" "+rs.getString("apepat")+" "+rs.getString("apemat");
            fila[3]=rs.getString("curp");
            fila[4]=rs.getString("cveunidad");
            modelUsrSICEEB.addRow(fila);
        }
    }
    
    public void getFormulariosParaPermiso (JComboBox formularios) throws SQLException
    {
        rs = stm.executeQuery("SELECT DISTINCT(formulario) AS formulario FROM siscert_objetospermiso");
        while (rs.next())
            formularios.addItem(rs.getString("formulario"));
        
    }
    
    public void getUsrSISCERT(SISCERT_ModeloDeTabla modelUsrSISCERT) throws SQLException
    {        
        Object []fila, filaOculta;
        int numCols=modelUsrSISCERT.getColumnCount(), numColsOcultas=modelUsrSISCERT.getHiddenColumnCount();
        rs = stm.executeQuery("SELECT su.idusuario, su.usuario, u.pasword, su.nombre, su.primerape, su.segundoape, su.cveunidad, " +
                                "su.descripcion " +
                              "FROM siscert_usuarios su, usuarios u " +
                              "WHERE su.usuario=u.loginuser AND su.estatus='t'");
        while (rs.next())
        {
            fila = new Object[numCols];
            filaOculta = new Object[numColsOcultas];
            filaOculta[0] = decodificarBase64 (this.rs.getString("pasword"));//Decodificamos la contraseña de la BD a formato Base64
            
            fila[0]=rs.getString("idusuario");
            fila[1]=rs.getString("usuario");
            fila[2]=rs.getString("nombre")+" "+rs.getString("primerape")+" "+rs.getString("segundoape");
            fila[3]=rs.getString("cveunidad");
            fila[4]=rs.getString("descripcion");
            modelUsrSISCERT.addRow(fila,filaOculta);
        }
    }

    //OJO: Usar transacción para usar este método
    public void guardarUsuarios___ (SISCERT_ModeloDeTabla modelUsrSISCERT, String cveunidadIndicada) throws SQLException
    {
        LinkedList<String> idobjs = new LinkedList<String>();
        String idUsrs="", idusuario, idNewUsr;
        int i, idPermiso;

        rs = stm.executeQuery("SELECT idobjeto FROM siscert_objetospermiso");
        while (rs.next())
            idobjs.add(rs.getString("idobjeto"));

        //Insertamos los que estén en la lista usrsMosipa y que no estén en la BD
        for (i=0; i<modelUsrSISCERT.getRowCount(); i++ )
        {
            idusuario = ""+modelUsrSISCERT.getValueAt(i,0);
            if (i!=0) idUsrs += ", ";
            idUsrs += idusuario;
            rs = stm.executeQuery("SELECT idusuario FROM siscert_usuarios WHERE idusuario = "+idusuario);
            if (!rs.next()){//Si no existe el usuario en la BD lo insertamos poniendo en dafault sus permisos en false
                idNewUsr = idusuario;                                       //Obtenemos el id de insersion
                try {
                    stm.execute("INSERT INTO siscert_usuarios (idusuario, usuario, nombre, primerape, segundoape, cveunidad, descripcion, estatus) "
                        + "SELECT idusuario, TRIM(loginuser), TRIM(NVL(nombreu,'')), TRIM(NVL(apepat,'')), TRIM(NVL(apemat,'')), TRIM(cveunidad), '', 't'"
                        + "FROM usuarios WHERE idusuario="+idNewUsr);//Insertamos el usuario
                }catch (SQLException ex){
                    if (ex != null && ex.getMessage().contains("cveunidad"))// Si la CVEUNIDAD  no es válida
                    {
                        if(!cveunidadIndicada.equals("")){
                            stm.execute("INSERT INTO siscert_usuarios (idusuario, usuario, nombre, primerape, segundoape, cveunidad, descripcion, estatus) "
                            + "SELECT idusuario, TRIM(loginuser), TRIM(NVL(nombreu,'')), TRIM(NVL(apepat,'')), TRIM(NVL(apemat,'')), '"+cveunidadIndicada.trim()+"', '', 't'"
                            + "FROM usuarios WHERE idusuario="+idNewUsr);//Insertamos el usuario
                            modelUsrSISCERT.setValueAt(cveunidadIndicada, i, 3);
                            cveunidadIndicada="";
                        }else throw new SQLException(ex+"*"+modelUsrSISCERT.getValueAt(i,1));
                    }else
                        throw new SQLException(ex);
                }

                idPermiso = Integer.parseInt(obtenerDato("SELECT MAX(idpermiso)+1 as idpermiso FROM siscert_permisos")); //Obtenemos el id de mosipa_permisos
                for (String idobj : idobjs)
                    stm.execute("INSERT INTO siscert_permisos (idpermiso, idusuario, idobjeto, permiso) VALUES("+(idPermiso++)+", "+idNewUsr+","+idobj+",'f')");
            }
        }
        //Eliminamos de la BD los que no estén en la lista usrsMosipa y que estén en la BD
        stm.execute("DELETE FROM siscert_permisos WHERE idusuario NOT IN ("+idUsrs+")");
        stm.execute("DELETE FROM siscert_usuarios WHERE idusuario NOT IN ("+idUsrs+")");
        //stm.execute("DELETE FROM mosipa_sisver WHERE idusuario NOT IN (SELECT idUsuario FROM mosipa_usuarios)");
    }
    
    //OJO: Usar transacción para usar este método
    public void guardarPermisos___ (String idusuario, JCheckBox[] checkboxes) throws SQLException
    {
        for (JCheckBox obj : checkboxes)
            stm.execute(
                      "UPDATE siscert_permisos SET permiso = "+((obj.isSelected())?"'t'":"'f'")+"  "
                    + "WHERE idobjeto=(SELECT idobjeto FROM siscert_objetospermiso WHERE componente='"+obj.getName()+"') AND idusuario="+idusuario);
    }

    public void getPermisosDeUsuario(String idusuario, JCheckBox[] checkboxes)throws SQLException
    {
        String objeto;
        LinkedList<JCheckBox> chkboxes = new LinkedList<JCheckBox>(); //Hacemos una copia temporal de los checksboxs para trabajar con ella y no alterar la original
        chkboxes.addAll(Arrays.asList(checkboxes));
        int i;

        rs = stm.executeQuery("SELECT u.usuario,op.componente, p.permiso, op.formulario  "
                + "FROM siscert_permisos p, siscert_usuarios u, siscert_objetospermiso op "
                + "WHERE p.idUsuario=u.idUsuario AND op.idObjeto=p.idObjeto AND u.idusuario='"+idusuario+"'");
        while (rs.next())
        {
            objeto = rs.getString("componente");
            i = 0;
            for (JCheckBox chkbox : chkboxes)
            {
                if (chkbox.getName().equals(objeto))
                {
                    chkboxes.get(i).setSelected(rs.getBoolean("permiso"));
                    chkboxes.remove(i);
                    break;
                }
                else
                    i++;
            }
        }
    }

    //OJO: Usar transacción para usar este método
    public void altaObjetoPermisos___ (String permiso, String nombreFormulario, String nombreObjeto) throws SQLException
    {
        LinkedList<String> idUsuarios = new LinkedList<String>();
        String tmpPetmiso;
        int idPermiso=0;

        //Damos de alta el nuevo objeto en la BD
        String idObjeto = obtenerDato("SELECT MAX(idobjeto)+1 FROM siscert_objetospermiso");
        stm.execute("INSERT INTO siscert_objetospermiso (idobjeto,formulario, componente) VALUES("+idObjeto+",'" + nombreFormulario + "','" + nombreObjeto + "')");
        //Extraemos todos los idusuarios
        rs = stm.executeQuery("SELECT idusuario FROM siscert_usuarios");
        while (rs.next())
            idUsuarios.add(rs.getString(1));

        idPermiso = Integer.parseInt(obtenerDato("SELECT MAX(idpermiso)+1 as idpermiso FROM siscert_permisos"));
        //Asignamos el objeto a cada usuario con permiso indicado por la variable permiso
        if (idUsuarios.size()>0)
            for (String idusuario : idUsuarios)
            {
                tmpPetmiso = (idusuario.equals("899")) ? "t" : permiso;       //Si es SUPERADMIN le damos todos los derechos
                stm.execute("INSERT INTO siscert_permisos (idpermiso,idusuario,idobjeto,permiso) VALUES("+(idPermiso++) + ", " + idusuario + "," + idObjeto + ",'" + tmpPetmiso + "')");
            }
    }
//************************************************************************************************************
//*************************** METODOS USADOS EN EL FORMULARIO CrearCertifOrig ********************************
//************************************************************************************************************    
    //OJO: Usar transacción para usar este método
    public void crearCertificado___ (String idAluSICEEB, String idCCT, String curp, String nombre,String apepat,String apemat, String cicescini, String cicescinilib,
            String cveturno, String cveplan, String promedio, SISCERT_GlobalMethods global,  String idFormatFol_folLet_folNum[]) throws SQLException, Exception
    {
        insertarAlumnoEnSICEEB ("I", idAluSICEEB, idCCT, curp, nombre, 
                apepat, apemat, curpAFecha (curp), curp.substring(10, 11), Integer.parseInt(cicescini), Integer.parseInt(cicescinilib),
                (cveplan.equals("1")?6:3), cveturno, cveplan, promedio, global.capturista, "taller", "arte", idFormatFol_folLet_folNum);
    }
    
    public void getAlumnosSICEEB (String datoABuscar, String caso, SISCERT_ModeloDeTabla modelUsrSISCERT) throws SQLException
    {
        String where="";
        Object []fila;
        if (caso.equals("curp"))
            where = "curp like '"+datoABuscar.toUpperCase()+"%'";
        else if (caso.equals("idalu"))
            where = "idalu = "+datoABuscar.toUpperCase();
        rs = stm.executeQuery("SELECT idalu,TRIM(curp) as curp, TRIM(nombre) AS nombre, TRIM(apepat) AS apepat, TRIM(apemat) AS apemat FROM alumno WHERE "+where);
        while (rs.next()){
            fila = new Object[modelUsrSISCERT.getColumnCount()];
            fila[0]=rs.getString("idalu");
            fila[1]=rs.getString("curp");
            fila[2]=rs.getString("nombre");
            fila[3]=rs.getString("apepat");
            fila[4]=rs.getString("apemat");
            modelUsrSISCERT.addRow(fila);
        }
        
    }
    
    public void getCCTEscuela (String cct, JComboBox cctsEscuela, JComboBox nombresEscuela, ArrayList<String[]> idsCCTYCveturnos) throws SQLException
    {
        String [] fila;
        rs = stm.executeQuery("SELECT e.idcct, e.cveturno, (TRIM(e.cct) || ' ('||TRIM(t.desturno)||')') AS cct, "
                + "(TRIM(e.nombre) || ' ('||TRIM(t.desturno)||')') AS nombre "
                + "FROM escuela e, turno t WHERE t.cveturno=e.cveturno AND cct like '"+cct.toUpperCase()+"%'");
        while (rs.next())
        {
            fila = new String[2];
            fila[0]=rs.getString("idcct");
            fila[1]=rs.getString("cveturno");
            idsCCTYCveturnos.add(fila);
            cctsEscuela.addItem(rs.getString("cct"));
            nombresEscuela.addItem(rs.getString("nombre"));
        }
    }
    
    public void getEstudiosDeEsteAlumno (String idalu, int cveplan, SISCERT_ModeloDeTabla modelCicloDeEstudio, SISCERT_ModeloDeTabla modelDatosCertificado) throws SQLException
    {
        String [] fila;
        //String niveles[]={"Pri","Sec","Pre"};
        rs = stm.executeQuery("SELECT (SELECT cct FROM escuela WHERE idcct=ag.idcct) as cct, (SELECT TRIM(nombre) FROM escuela WHERE idcct=ag.idcct) as nombre, cicescini, cicescfin, grado, cveturno, promediogral, estatusgrado, (SELECT cveunidad FROM escuela WHERE idcct=ag.idcct) as  unidad FROM alumnogrado ag where idalu="+idalu/*+" AND cveplan="+cveplan*/+" ORDER BY cicescini");
        while (rs.next()){
            fila = new String[8];
            fila[0]=rs.getString("cct");
            fila[1]=rs.getString("nombre");
            fila[2]=rs.getString("cicescini")+"-"+rs.getString("cicescfin");
            fila[3]=rs.getString("grado");
            fila[4]=rs.getString("cveturno");
            fila[5]=rs.getString("promediogral");
            fila[6]=rs.getString("estatusgrado");
            fila[7]=rs.getString("unidad");
            modelCicloDeEstudio.addRow(fila);
        }
        rs = stm.executeQuery("SELECT (SELECT cct FROM escuela WHERE idcct=fi.idcct) as cct, cicescinilib, cicescinilib+1 as cicinilibfin, foliolet, folionum, curp, promediogral, usuario, cveunidad FROM folios_impre fi WHERE idalu="+idalu/*+" AND cveplan="+cveplan*/);
        while (rs.next()){
            fila = new String[8];
            fila[0]=rs.getString("cct");
            fila[1]=rs.getString("cicescinilib") + "-"+rs.getString("cicinilibfin");
            fila[2]=rs.getString("foliolet").substring(1);
            fila[3]=rs.getString("folionum");
            fila[4]=rs.getString("curp");
            fila[5]=rs.getString("promediogral");
            fila[6]=rs.getString("usuario");
            fila[7]=rs.getString("cveunidad");
            modelDatosCertificado.addRow(fila);
        }
    }
    
    public Map getCveprograma (String plan, String grado, String idcct, String cicescini, String cveentidad) throws SQLException
    {
        Map datos = new HashMap();
        rs = stm.executeQuery("SELECT cveprograma, desprograma, cicescfin, modalidad " 
                + "FROM planmodalidad "
                + "WHERE plan="+plan+" AND grado="+grado+" AND modalidad=(SELECT modalidad FROM escuela WHERE idcct="+idcct+") "
                    + "AND cicescini<="+cicescini+" AND cicescfin>="+cicescini+" AND cveentidad="+cveentidad);
        if (rs.next()){
            datos.put("cveprograma",rs.getString("cveprograma").trim());
            datos.put("desprograma",rs.getString("desprograma"));
            datos.put("cicescfin",rs.getString("cicescfin").trim());
            datos.put("modalidad",rs.getString("modalidad").trim());
        }
        return datos;
    }
    
    public Map getNormatividad (String cveplan, String cveprograma, String grado) throws SQLException
    {
        Map datos = new HashMap();
        rs = stm.executeQuery("SELECT cveplan, edadadmmin, edadadmmax "
                            + "FROM normatividad "
                            + "WHERE cveplan="+cveplan+" AND cveprograma = '"+cveprograma+"' AND grado="+grado);
        if (rs.next()){
            datos.put("edadMin",rs.getString("edadadmmin").trim());
            datos.put("edadMax",rs.getString("edadadmmax"));
        }
        return datos;
    }
//************************************************************************************************************
//*************************** METODOS USADOS EN EL FORMULARIO RelacionarIdalu ********************************
//************************************************************************************************************    
    public void getAlumnosSICEEBySISCERT (String curp, String cveunidad, SISCERT_ModeloDeTabla modelAlumnosSISCERT, SISCERT_ModeloDeTabla modelAlumnosSICEEB) throws SQLException
    {
        Object []fila, filaOculta;
        rs = stm.executeQuery("SELECT idalu, TRIM(curp) as curp, TRIM(nombre) AS nombre, TRIM(apepat) AS apepat, TRIM(apemat) AS apemat FROM alumno WHERE curp like '"+curp.toUpperCase()+"%'");
        while (rs.next()){
            fila = new Object[modelAlumnosSICEEB.getColumnCount()];
            fila[0]=false;
            fila[1]=rs.getString("idalu");
            fila[2]=rs.getString("curp");
            fila[3]=rs.getString("nombre");
            fila[4]=rs.getString("apepat");
            fila[5]=rs.getString("apemat");
            modelAlumnosSICEEB.addRow(fila);
        }
        
        rs = stm.executeQuery("SELECT idcertificacion, NVL(idalu,'') AS idalu, CASE cveplan WHEN 1 THEN 'Primaria' WHEN 2 THEN 'Secundaria' WHEN 3 THEN 'Preescolar' END AS nivel, cct, ciciniestud,cicinilib_cert,folio, TRIM(curp) as curp, promedio, TRIM(nombre) AS nombre, TRIM(apepat) AS apepat, TRIM(apemat) AS apemat, usuario FROM siscert_certificacion WHERE curp like '"+curp.toUpperCase()+"%' AND cveunidad='"+cveunidad+"'");
        while (rs.next()){
            fila = new Object[modelAlumnosSISCERT.getColumnCount()];
            filaOculta = new Object[modelAlumnosSISCERT.getHiddenColumnCount()];
            
            filaOculta[0]=rs.getString("idcertificacion");
            fila[0]=false;
            fila[1]=rs.getString("idalu");
            fila[2]=rs.getString("nivel");
            fila[3]=rs.getString("cct");
            fila[4]=rs.getString("ciciniestud");
            fila[5]=rs.getString("cicinilib_cert");
            fila[6]=rs.getString("folio");
            fila[7]=rs.getString("curp");
            fila[8]=rs.getString("promedio");
            fila[9]=rs.getString("nombre");
            fila[10]=rs.getString("apepat");
            fila[11]=rs.getString("apemat");
            fila[12]=rs.getString("usuario");
            modelAlumnosSISCERT.addRow(fila,filaOculta);
        }
    }
    
    public void getEstudiosDeEsteAlumno (String caso, String datoABuscar, int cveplan, SISCERT_ModeloDeTabla modelCicloDeEstudio, SISCERT_ModeloDeTabla modelDatosCertificado) throws SQLException
    {
        String [] fila;
        if (caso.equals("SICEEB"))
        {
            rs = stm.executeQuery("SELECT (SELECT cct FROM escuela WHERE idcct=ag.idcct) as cct, (SELECT TRIM(nombre) FROM escuela WHERE idcct=ag.idcct) as nombre, cicescini, cicescfin, grado, cveturno, promediogral, estatusgrado FROM alumnogrado ag WHERE ag.grado in (3,6) AND idalu="+datoABuscar+" ORDER BY cicescini");
            while (rs.next()){
                fila = new String[8];
                fila[0]=rs.getString("cct");
                fila[1]=rs.getString("nombre");
                fila[2]=rs.getString("cicescini")+"-"+rs.getString("cicescfin");
                fila[3]=rs.getString("grado");
                fila[4]=rs.getString("cveturno");
                fila[5]=rs.getString("promediogral");
                fila[6]=rs.getString("estatusgrado");
                modelCicloDeEstudio.addRow(fila);
            }
            rs = stm.executeQuery("SELECT (SELECT cct FROM escuela WHERE idcct=fi.idcct) as cct, cicescinilib, cicescinilib+1 as cicinilibfin, foliolet, folionum, promediogral, usuario FROM folios_impre fi WHERE idalu="+datoABuscar);
            while (rs.next()){
                fila = new String[8];
                fila[0]=rs.getString("cct");
                fila[1]=rs.getString("cicescinilib") + "-"+rs.getString("cicinilibfin");
                fila[2]=rs.getString("foliolet").substring(1)+rs.getString("folionum");
                fila[3]=rs.getString("promediogral");
                fila[4]=rs.getString("usuario");
                modelDatosCertificado.addRow(fila);
            }
        }
    }
    
    public void asociarIdAlu (String idalu, String idcertificacion) throws SQLException
    {
        stm.execute("UPDATE siscert_certificacion SET idalu="+idalu+" WHERE idcertificacion="+idcertificacion);
    }
    
//************************************************************************************************************
//********************* METODOS USADOS EN EL FORMULARIO AgregarEscuelaHistorica ******************************
//************************************************************************************************************ 
    public void buscarCCTHistoricas (String cct, JTextField txtNombreActual, SISCERT_ModeloDeTabla modelCCTsHistoricas) throws SQLException
    {
        Object []fila;
        int numCols = modelCCTsHistoricas.getColumnCount();
        rs = stm.executeQuery("SELECT idcct, cct, TRIM(nombre) AS nombre, cveturno FROM ESCUELA WHERE CCT = '"+cct+"'");
        if (rs.next())
            txtNombreActual.setText(rs.getString("nombre"));
        
        rs = stm.executeQuery("SELECT DISTINCT TRIM(nombre) as nombre, fecha_cam FROM hescuela WHERE CCT = '"+cct+"' ORDER BY fecha_cam DESC");
        while (rs.next()){
            fila = new Object[numCols];
            fila[0]=rs.getString("nombre");
            fila[1]=rs.getString("fecha_cam");
            if (modelCCTsHistoricas.indexOf(fila[0], 0)==-1)
                modelCCTsHistoricas.addRow(fila);
        }
    }
    
    public String agregarEscuelaHistorica (String cct, String nombreHistorico) throws SQLException
    {
        stm.execute("INSERT INTO HESCUELA(cveprograma, cveplan, idcct, cicescini, cicescfin, cvetipo, cvenivel, " +
                                "cvesistema, cvesubsis, cvetipesc, cvearc, cveentidad, cvemunicipio, cvelocalidad, " +
                                "desmunicipio, deslocalidad, cvepose, modalidad, cveservicio, cveinmueble, cveturno, " +
                                "cvezona, cvesost, estatusesc, cct, nombre, cvecctant, domicilio, entrecalle, " +
                                "ycalle, cp, colonia, poblacion, telefono, fax, email, numincor, fecincorp, " +
                                "fecalta, feccambio, fecactua, fecclaus, motivo, sector, centrodist, incorpora, " +
                                "fecha_oficio, fecha_recep, cveunidad, fecha_cam, observaciones) " +
                         "SELECT cveprograma, cveplan, idcct, cicescini, cicescfin, cvetipo, cvenivel, " +
                                "cvesistema, cvesubsis, cvetipesc, cvearc, cveentidad, cvemunicipio, cvelocalidad, " +
                                "desmunicipio, deslocalidad, cvepose, modalidad, cveservicio, cveinmueble, cveturno, " +
                                "cvezona, cvesost, estatusesc, cct, '"+nombreHistorico+"' AS nombre, cvecctant, domicilio, entrecalle, " +
                                "ycalle, cp, colonia, poblacion, telefono, fax, email, numincor, fecincorp, " +
                                "fecalta, feccambio, fecactua, fecclaus, motivo, sector, centrodist, incorpora, " +
                                "fecha_oficio, fecha_recep, cveunidad, date(current) AS fecha_cam, 'MODIFICACION NOMBRE ESCUELA' AS observaciones " +
                         "FROM ESCUELA  " +
                         "WHERE CCT = '"+cct+"'");
        
        rs=stm.executeQuery("SELECT FIRST 1 DATE(CURRENT) AS fechaHoy FROM siscert_sisver");
        if (rs.next())
            return rs.getString("fechaHoy");
        return "";
    }

//************************************************************************************************************
//**************************** METODOS USADOS EN EL FORMULARIO Auditoria *************************************
//************************************************************************************************************    
    
    public ResultSet generarAuditoria (int []totalFolios, String cicescinilib, String nivelEducativo, String cveunidad, boolean incluirSeccion59, String estatusImpre) throws SQLException
    {
        String cveplan = nivelEducativo.equals("PREESCOLAR")?"3":(nivelEducativo.equals("PRIMARIA")?"1":"2");
        String region =  "AND cveunidad='"+cveunidad+"'", estatus_impre="";
        
        if (cveunidad.equals("TODO EL ESTADO")){
            if (!incluirSeccion59) region = "AND cveunidad<>'CINCO9'";
            else region = " ";
        }

        if (estatusImpre.equals("CANCELADOS")) estatus_impre = " AND estatus_impre='C' ";
        else if (estatusImpre.equals("IMPRESOS")) estatus_impre = " AND estatus_impre='I' ";
        else if (estatusImpre.equals("INCOMPLETOS")) estatus_impre = " AND estatus_impre='T' ";
        else if (estatusImpre.equals("TODOS")) estatus_impre = " ";
        
        rs = stm.executeQuery("SELECT count(folionum) AS totalFolios " +
                              "FROM siscert_folimpre " +
                              "WHERE cicescinilib="+cicescinilib+" AND cveplan="+cveplan+" "+region);
        if (rs.next())
            totalFolios[0]=rs.getInt("totalFolios");
        
        rs = stm.executeQuery("SELECT foliolet, folionum " +
                              "FROM siscert_folimpre " +
                              "WHERE cicescinilib="+cicescinilib+" AND cveplan="+cveplan+" "+region + estatus_impre +
                              "ORDER BY foliolet, folionum");
        return rs;
    }
    
    public Map getFechaYHoraDeBaseDeDatos () throws SQLException
    {
        Map fechaYHora = new HashMap();
        rs = stm.executeQuery("SELECT first 1 TO_CHAR(TODAY, '%d/%m/%Y') AS fecha, TO_CHAR(CURRENT,'%H:%M') AS hora FROM siscert_sisver"); //Para la hora el formato completo sería '%H:%M:%S %F'
        if (rs.next())
        {
            fechaYHora.put("fecha", rs.getString("fecha"));
            fechaYHora.put("hora", rs.getString("hora"));
        }
        return fechaYHora;
    }
//************************************************************************************************************
//************************************ MÉTODOS COMPARTIDOS ***************************************************
//************************************************************************************************************    
    
    
    private String getIdTabla (String idABuscar, String tabla, String condicion)  throws SQLException
    {
        String id="";
        rs = stm.executeQuery("SELECT " + idABuscar + " as numero FROM " + tabla + " WHERE "+condicion);
        if (rs.next())
            id=rs.getString("numero");  //Se extrae el número para id
        return id;
    }
    
    private String getLastIdTabla (String idABuscar, String tabla)  throws SQLException
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
    
    private int getNewIdNumTabla (String columna, String tabla)  throws SQLException
    {
        int id;

        rs = stm.executeQuery("SELECT MAX(" + columna + ") as numero FROM " + tabla);
        rs.next();
        id=rs.getInt("numero")+1;  //Se extrae el número para id
        return id;
    }
    
   
    /*private String crearQueryInsert (String tabla, Object datos[][])
    {
        String qryColumnNames="", qryDataColumns="";
        
        for (int i=0; i<datos.length; i++)
        {
            qryColumnNames+= ((i==0)?"":",") + datos[i][0];
            qryDataColumns+= ((i==0)?"":",") + datos[i][1];
        }
        
        return "INSERT INTO "+tabla+" ("+qryColumnNames + ") VALUES ("+qryDataColumns+")";
    }*/
    
    public boolean[] getEstosPermisos (String usuario, String formulario, String []objetos) throws SQLException
    {
        boolean []permisos=new boolean [objetos.length];
        int i = 0;
        for (String objeto : objetos){
            rs = stm.executeQuery("SELECT u.usuario,op.componente, p.permiso, op.formulario  " +
                "FROM siscert_permisos p, siscert_usuarios u, siscert_objetospermiso op " +
                "WHERE p.idusuario=u.idusuario AND op.idobjeto=p.idobjeto " +
                   "AND u.idusuario='"+usuario+"' AND op.formulario = '"+formulario+"' AND op.componente = '"+objeto+"'");
            if (rs.next())
                permisos[i++]=rs.getBoolean("permiso");
        }
        return permisos;
    }

    
    public void setNumSol() throws SQLException {
        String cveunidad[] = {"CINCO9","DSRAYU","DSRCAN","DSRHUA","DSRISN","DSRIST","DSRPIN","DSRPUE","DSRTLA","DSRTUX","DSRVAL"};
        String ciclos[] = {"2011","2012"} ;
        String idformatos [] = {"2","3","4"};
        String cveplan[]={"1","2","3"};
        int m;
        Statement stm2=null;
        
        try {
            stm2 = this.getNewStatement ();

            for (int i=0; i<cveunidad.length; i++)
                for (int j=0; j<ciclos.length; j++)
                    for (int k=0; k<idformatos.length; k++)
                        for (int l=0; l<cveplan.length; l++)
                        {
                            rs = stm2.executeQuery("select idfolimpre from siscert_folimpre  WHERE cveunidad='"+cveunidad[i]+"' and cicescinilib="+ciclos[j]+" and idformato="+idformatos[k]+" and cveplan="+cveplan[l]+" order by idfolimpre");
                            m=1;
                            while (rs.next())
                                stm.execute("UPDATE siscert_folimpre SET numsolicitud="+(m++)+" WHERE idfolimpre="+rs.getString("idfolimpre"));
                        }
        }finally {  this.closeStatement(stm2, null);  }
    }
    
    //Método para ser eliminado en cuanto ya no se use más
    //OJO: Usar transacción para usar este método
    public void setNumSolInc___() throws SQLException 
    {
        
        Statement stm2=null;
        int i=-1;
        
        try
        {
            stm2 = this.getNewStatement ();
            
            rs = stm2.executeQuery("SELECT idcertificacion, cveunidad, cveplan, idformato, idcertiregion FROM siscert_x_numsol_basu ORDER BY cveunidad, cveplan, idformato, idcertiregion");
            while (rs.next()){
                stm.execute("UPDATE siscert_certificacion set numsolicitud="+i+" where idcertificacion="+rs.getString("idcertificacion"));
                i=i-1;
            }
        }finally {  this.closeStatement(stm2, null);  }
    }

    public Map verificarFolioExistente(String idAluSICEEB, String cveplan) throws SQLException {
      String strFirma = "";
      String strFolios = "";
      Map datos = new HashMap();       
      
        if(!idAluSICEEB.isEmpty()) {
            rs = stm.executeQuery("SELECT fo.cicescinilib,fo.numsolicitud, fo.folionum, fo.juridico, " 
                + " NVL((SELECT foliodigital FROM siscert_firmaelec WHERE idalu = fo.idalu AND idfolimpre=fo.idfolimpre),'') as foliodigital, " 
                + " (SELECT fechatimbradosep FROM siscert_firmaelec WHERE idalu = fo.idalu AND idfolimpre=fo.idfolimpre) as fecha_timbrado, " 
                + " (SELECT estatus_firma FROM siscert_firmaelec WHERE idalu = fo.idalu AND idfolimpre=fo.idfolimpre) as estatus_firma " 
                + " FROM siscert_certificacion c, siscert_folimpre fo " 
                + " WHERE c.idalu = fo.idalu " 
                + " AND c.idcertificacion = fo.idcertificacion " 
                + " AND c.idalu = " + idAluSICEEB +" "
                + " AND c.cveplan=" + cveplan   
                + " AND fo.cicescinilib >= 2017 "    
            );

            while (rs.next()) {            
                if(!rs.getString("foliodigital").isEmpty())
                    strFirma += "\nCicescinilib: " + rs.getString("cicescinilib")
                        + ",\nNúm. solicitud: " + rs.getString("numsolicitud")
                        + ",\nFolio digital: " + rs.getString("foliodigital") 
                        + ",\nFecha timbrado: " + rs.getString("fecha_timbrado") + "\n";
                else
                    strFolios += "\nCicescinilib: " + rs.getString("cicescinilib")
                        + ",\nNúm. Solicitud: " + rs.getString("numsolicitud") + "\n";                        
            }
        }
        
        datos.put("strFirma", strFirma);
        datos.put("strFolios", strFolios);
        
        return datos;
    }   
    
    public String verificarEnCancelados(String idAluSICEEB, String cveplan) throws SQLException {
        String cance = "";
        
        rs = stm.executeQuery("SELECT estatus_firma " 
            + "FROM siscert_certificacion c, siscert_folimpre_cance fo, siscert_firmaelec_cance fc " 
            + "WHERE c.idalu = fo.idalu " 
            + "AND c.idcertificacion = fo.idcertificacion " 
            + "AND c.idalu = " + (idAluSICEEB.isEmpty() ? "0" :idAluSICEEB)  +" "
            + "AND c.cveplan = " + cveplan +" "    
            + "AND fo.idfolimpre = fc.idfolimpre "    
            + "AND fo.idalu = fc.idalu "    
            + "AND fo.cicescinilib = fc.cicescinilib "     
        );
        
        if(rs.next())
            cance = rs.getString("estatus_firma");
        return cance;
    }
}