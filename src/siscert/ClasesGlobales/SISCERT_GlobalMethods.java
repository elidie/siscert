package siscert.ClasesGlobales;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import siscert.AccesoBD.SISCERT_QueriesInformix;

public class SISCERT_GlobalMethods  {
    private boolean datos;

    //String delegacion, claveTrabajo, lugar, cotejo, delegado, cargoDelegado, diaLetra, mesAñoLetra;
    public String idAluSICEEB,idcertificacion, NoControl, numSolicitud, cicescinilib, diaLetra, mesAñoLetra, fecha;
    public String globalDelegacion, globalCCTDelegacion, globalLugarExpedicion, globalDelegado, globalCargoDelegado, globalCotejo;
    public boolean cebas;
    //String globalDelegacion, globalCCTDelegacion
        //------------- ANVERSO -------------
    public String ieepo, idfolim_var, delegacion, cctDelegacion, nombre, primerApe, segundoApe, idcasocurp, curp, lugarExpedicion, diaExped_let, mesAñoExpedicion, fechaExpedLet, delegado, cargoDelegado;   //TRONCO COMUN
    public String periodoEscolar, cicescini, cicescfin;                                                      //Variante preescolar y primaria
    public String promNum_EducPrim, promedioNum, promedioLetra, promNum_educBasic, promLet_educBasic, folioRodac, promedioPrimSecEducBasic;              //Variante primaria y secundaria
    public String diaAcreditacion, mesAcreditacion, añoAcreditación, fechaAcrediLet, cursoYAcredito;                                    //Variante secundaria
        //------------- REVERSO -------------
    public String libro,foja,folio,escuela,cctEscuela,fechaExpedicion, cotejo, juridico;          //TRONCO COMÚN
    public String plan;                                                                //Variante secundaria
    public String idleyenda_lugvalid, lugarValidacion;
    public ArrayList<String[]> lugaresValidacion = new ArrayList<String[]>();

    public String cveunidad, tipoConexion, idformato;
    public int cveplan, idFormatCertAImprimir;
    public String idcapturista, capturista;
    
    public String tipoLetra;
    public int tamLetra;

    public String urlIconoSistema="imgs/SISCERT.png";                           //url del ícono que lleva el sistema en la barra de título
    public String urlSkinSecuAnv = "imgs\\imgsSecu\\caratAnv2.jpg";
    public String urlSkinSecuRev = "imgs\\imgsSecu\\caratRev2.jpg";
    public String versionSistema;
    
    public String urlGoogleDrive = "https://drive.google.com/drive/folders/1sMHCznTQnizT_xKKgPtcASMP5vIxmhzo?usp=sharing";
                                    
    
    private final String meses[]={"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
    private final String diasALos[]={"CERO","UN","DOS","TRES","CUATRO","CINCO","SEIS","SIETE","OCHO","NUEVE","DIEZ","ONCE","DOCE","TRECE","CATORCE","QUINCE","DIECISÉIS","DIECISIETE","DIECIOCHO","DIECINUEVE","VEINTE","VEINTIÚN","VEINTIDÓS","VEINTITRÉS","VEINTICUATRO","VEINTICINCO",
                           "VEINTISÉIS","VEINTISIETE","VEINTIOCHO","VEINTINUEVE","TREINTA","TREINTA Y UN"};
    private final String diasEl[]={"CERO","UNO","DOS","TRES","CUATRO","CINCO","SEIS","SIETE","OCHO","NUEVE","DIEZ","ONCE","DOCE","TRECE","CATORCE","QUINCE","DIECISÉIS","DIECISIETE","DIECIOCHO","DIECINUEVE","VEINTE","VEINTIUNO","VEINTIDÓS","VEINTITRÉS","VEINTICUATRO","VEINTICINCO",
                           "VEINTISÉIS","VEINTISIETE","VEINTIOCHO","VEINTINUEVE","TREINTA","TREINTA Y UNO"};

    public SISCERT_GlobalMethods (String tipoConexion, String version) throws SQLException
    {
        this.cveunidad = "";
        this.tipoConexion = tipoConexion;
        this.versionSistema = version;
    }
    
    public SISCERT_GlobalMethods (String tipoConexion, String version, String cveunidad, String capturista, String idcapturista) throws SQLException
    {
        this.cveunidad = "";
        this.tipoConexion = tipoConexion;
        this.versionSistema = version;
        this.cveunidad = cveunidad;
        this.capturista = capturista;
        this.idcapturista = idcapturista;
    }

    public void partirFechaEnLetra ()
    {
        String mes;
       
        this.diaLetra = this.diasALos [Integer.parseInt(this.fecha.substring(0, 2))];
        mes = this.meses [Integer.parseInt(this.fecha.substring(3, 5))-1];
        this.mesAñoLetra = mes + " DEL " + convertirAñoEnLetra (this.fecha);
    }
    
    public String convertirFechaEnLetra (String fecha_dd_MM_yyyy, String casoSepMesAnio) //sepMesAnio
    {
        String daylet, mes;
        if (casoSepMesAnio.equals("DE"))
            casoSepMesAnio = " DE ";
        else if (casoSepMesAnio.equals("DEL"))
            casoSepMesAnio = " DEL ";
        daylet = this.diasEl [Integer.parseInt(fecha_dd_MM_yyyy.substring(0, 2))];
        mes = this.meses [Integer.parseInt(fecha_dd_MM_yyyy.substring(3, 5))-1];
        return daylet + " DE " + mes + casoSepMesAnio + convertirAñoEnLetra (fecha_dd_MM_yyyy);
    }
        //--------------- Reconoce el año desde 1901 hasta 2099
    public String convertirAñoEnLetra (String date_dd_MM_yyyy)
    {
        String año, A="", B;
        String decenas [] = {"","","","TREINTA","CUARENTA","CINCUENTA","SESENTA","SETENTA","OCHENTA","NOVENTA"};

        if (date_dd_MM_yyyy.substring(6, 10).equals("2000"))                   //Directo, si es 2000 regresa la palabra DOS MIL
            return "DOS MIL";
        else
        {
            if (date_dd_MM_yyyy.substring(6, 8).equals("19"))                  //Toma los primeros dos números del año
                A = "MIL NOVECIENTOS ";
            else if (date_dd_MM_yyyy.substring(6, 8).equals("20"))
                A = "DOS MIL ";

            if (Integer.parseInt(date_dd_MM_yyyy.substring(8, 10))<31)         //toma los dos últimos días del año
                B = this.diasEl [Integer.parseInt(date_dd_MM_yyyy.substring(8, 10))];
            else
            {
                B = decenas [Integer.parseInt(date_dd_MM_yyyy.substring(8, 9))];
                B += " Y " + this.diasEl[Integer.parseInt(date_dd_MM_yyyy.substring(9, 10))];
            }
            año = A + B;
        }
        return año;
    }
    
    /* Devuelve la fecha en formato dd/MM/aaaa */
    public String convertirTextoAFecha (String day, String month, String year)
    {
        String dia="", mes="", anio;
        String decenas [] = {"CERO","DIEZ","VEINTI","TREINTA","CUARENTA","CINCUENTA","SESENTA","SETENTA","OCHENTA","NOVENTA"};
        
        
        //-------------- Convertimos la palabra del dia en número --------------
        for (int i=0; i<diasALos.length; i++)
            if (day.toUpperCase().equals(diasALos[i]) || i==1 && day.toUpperCase().equals("PRIMER"))
                dia = (i<10?"0":"") + i;
        //-------------- Convertimos la palabra del mes en número --------------
        for (int i=0; i<meses.length; i++)
            if (month.toUpperCase().equals(meses[i]))
                mes = ((i+1)<10?"0":"") + (i+1);
        //-------------- Convertimos la palabra del año en número --------------
        year = year.toUpperCase();
        if (year.contains("MIL NOVECIENTOS")){
            anio = "19";
            year = year.replace("MIL NOVECIENTOS ", "");
        }else{
            anio = "20";
            year = year.replace("DOS MIL ", "");
        }
        
        //Revisamos si es mayor o igual a 21 para convertir las decenas a partir de este número
        for (int i=2; i<10; i++){
            if ( year.contains(decenas[i]) ){
                anio += i;
                year = year.replace(decenas[i], "");
                year = year.replace(" Y ", "");
            }
        }        
           
        year = (year.equals("DÓS")) ? year.replace("DÓS","DOS"): year;
        //Convertimos las unidades o parte de las decenas antes del 21
        for (int i=0; i<=20; i++)            
            if (year.equals(diasEl[i])){
                if ( i < 9){
                    if(anio.substring(0,2).equals("20"))
                        anio += i;
                    else
                        anio = "0"+i;
                }
                else
                    anio += i;
            }
        
        return dia+"/"+mes+"/"+anio;        
    }
    
    //*** Validar la fecha *************************************/
    public String validarFecha (String day, String month, String year)
    {
        String dia="", mes="", anio="";
        
        for (int i=0; i<meses.length; i++)
            if (month.toUpperCase().equals(meses[i]))
                mes = ((i+1)<10?"0":"") + (i+1);
                
        LocalDate today = LocalDate.of(Integer.parseInt(year), Integer.parseInt(mes), Integer.parseInt(day));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");        
        
        return formatter.format(today);
    }
    
    public String convertirPromedioALetra (String promedioNum)
    {
        String numeros [] = {"CERO","UNO","DOS","TRES","CUATRO","CINCO","SEIS","SIETE","OCHO","NUEVE"};
        String promLetra = "";
        if (promedioNum.equals("10"))
            promLetra = "DIEZ";
        else if (promedioNum.toUpperCase().equals("A"))
            promLetra = "ACREDITADO";
        else if (promedioNum.length() == 3)                                      //Tiene formato N.N
            promLetra = numeros [Integer.parseInt(promedioNum.substring(0, 1))] + " PUNTO " + numeros [Integer.parseInt(promedioNum.substring(2, 3))];
        else if (promedioNum.length() == 2)                                 //Tiene formato NN
            promLetra = numeros [Integer.parseInt(promedioNum.substring(0, 1))] + " PUNTO " + numeros [Integer.parseInt(promedioNum.substring(1, 2))];
        else if (promedioNum.length() == 1)                                 //Tiene formato N
            promLetra = numeros [Integer.parseInt(promedioNum)];
        return promLetra;
    }

    public void setDatosCertificado(String caso, String cveunidadUsuario, String lado,ResultSet rsAlumno) throws SQLException, Exception
    {
        String grupo = rsAlumno.getString("grupo");
        if (grupo!=null && grupo.contains("_") && !cveunidadUsuario.equals("CINCO9"))
            throw new Exception ("IMPRESION_CINCONUEVE");
        idFormatCertAImprimir = rsAlumno.getInt("idformato");
        idcertificacion = rsAlumno.getString("idcertificacion");
        idAluSICEEB = rsAlumno.getString("idalu");
        NoControl = rsAlumno.getString("idcertiregion");
        numSolicitud = rsAlumno.getString("numsolicitud");
        cicescinilib = rsAlumno.getString("cicescinilib");
        cebas = rsAlumno.getBoolean("cebas");
        
        //======================= ANVERSO ======================= 
            //----------- TRONCO COMUN --------------\\
        nombre = rsAlumno.getString("nombre");
        primerApe = rsAlumno.getString("apepat");
        segundoApe = rsAlumno.getString("apemat");
        idcasocurp = rsAlumno.getString("idcasocurp");
        curp = rsAlumno.getString("curp");

            //----------------------------------------//
        periodoEscolar = "";  promedioNum = "";  promedioLetra="";  promedioPrimSecEducBasic=""; diaAcreditacion=""; mesAcreditacion="";  añoAcreditación="";
        cicescini=rsAlumno.getString("ai");
        cicescfin=rsAlumno.getString("af");
        if (cveplan == 3 || cveplan == 1)                                   //Variante preescolar y primaria
            periodoEscolar = (cicescini) + (!cicescfin.equals("") ? "-"+(cicescfin) : "");//Operador ternario, si período final no existe, concatenamos con cadena vacía

        promNum_EducPrim=rsAlumno.getString("prom_educprim");
        if (cveplan == 1 || cveplan == 2){                                  //Variante primaria y secundaria
            promedioNum = rsAlumno.getString("promedio"); 
            if (promNum_EducPrim.trim().equals(""))
                promedioLetra = convertirPromedioALetra (promedioNum.trim()); //prom_letra
        }
        promNum_educBasic=rsAlumno.getString("prom_educbasic");
        promLet_educBasic=rsAlumno.getString("promlet_educbasic");
        
        if (idFormatCertAImprimir >= 8)
            if (!promNum_EducPrim.trim().equals(""))
                promedioPrimSecEducBasic = "PROMEDIO DE EDUCACIÓN PRIMARIA "+promNum_EducPrim.trim()+"/PROMEDIO DE EDUCACIÓN SECUNDARIA "+promedioNum.trim();

        diaExped_let = rsAlumno.getString("dia_exped_let");

        if (cveplan == 1 || cveplan == 2){                                                  //Variante secundaria
            diaAcreditacion = rsAlumno.getString("dia_acredi");
            mesAcreditacion = rsAlumno.getString("mes_acredi");
            añoAcreditación = cicescfin;
            if (cebas && idFormatCertAImprimir<8)
                fechaAcrediLet = (cveplan==1?"PRIMARIA":"SECUNDARIA")+" EL " + diaAcreditacion+" DE "+mesAcreditacion+((Integer.parseInt(añoAcreditación)>=2000)?" DEL ":" DE ")+añoAcreditación;
            else
                fechaAcrediLet = "";
            //fechaAcrediLet = rsAlumno.getString("fecha_acredi_let");
        }
        
        mesAñoExpedicion = rsAlumno.getString("mesanio_exped_let");
        if (idFormatCertAImprimir >= 8)
            mesAñoExpedicion = "";
        
        
        //fechaExpedLet = rsAlumno.getString("fecha_exped_let");
        folioRodac = "";

        //======================= REVERSO ======================= 
            //----------- TRONCO COMUN --------------\\
        libro = rsAlumno.getString("libro");
        foja = rsAlumno.getString("foja");
        folio = rsAlumno.getString("folio");
        escuela = rsAlumno.getString("escuela");
        cctEscuela = rsAlumno.getString("cct");
        
        cursoYAcredito="";
        if (caso.equals("IMPRESION")){
            if (idFormatCertAImprimir>=8)
                cursoYAcredito = (cveplan==1?"PRIMARIA":(rsAlumno.getString("cicinilib_cert").equals("2012")?"BÁSICA":"SECUNDARIA"))+(cebas?" EL " + diaAcreditacion+" DE ":" EN ") + mesAcreditacion.trim() + ((Integer.parseInt(añoAcreditación)>=2000)?" DEL ":" DE ") + añoAcreditación.trim()+", EN LA ESCUELA "+escuela+", CON CLAVE DE CENTRO DE TRABAJO "+cctEscuela;
        }else
            cursoYAcredito = rsAlumno.getString("curso_acredito");
        
        fechaExpedicion = rsAlumno.getString("fecha");
        
        fecha = fechaExpedicion;
        this.partirFechaEnLetra();
        fechaExpedLet = "A LOS "+ this.diaLetra+" DÍAS DEL MES DE "+this.mesAñoLetra;
        
        juridico = rsAlumno.getString("juridico");
        //modalidad
        idleyenda_lugvalid=rsAlumno.getString("idleyenda");
        lugarValidacion = rsAlumno.getString("lugvalid");
            //----------------------------------------//           
        plan = "";
        if (cveplan == 2)                                                  //Variante secundaria                              
            plan = rsAlumno.getString("plan_estud");

        idfolim_var = rsAlumno.getString("idfolim_var");
        delegacion = rsAlumno.getString("delegacion");
        cctDelegacion= rsAlumno.getString("cctdeleg");
        lugarExpedicion= rsAlumno.getString("lugar_expedicion");
        delegado= rsAlumno.getString("delegado");
        cargoDelegado = rsAlumno.getString("cargodelegado");
        cotejo= rsAlumno.getString("cotejo");
        
        
        
                //--------- Verificamos la vigencia del formato
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy"); 
        df.setLenient(false);
        java.util.Date fechaExped = df.parse(fechaExpedicion);
        java.util.Date fechaExpIni_1213 = df.parse("08/07/2013"); 
        java.util.Date fechaExpFin_1213 = df.parse("14/07/2014");
        java.util.Date fechaExpIni_1314 = df.parse("16/07/2014"); 
        java.util.Date fechaExpFin_1314 = df.parse("13/07/2015");
        java.util.Date fechaExpIni_1415 = df.parse("14/07/2015"); 
        java.util.Date fechaExpFin_1415 = df.parse("14/07/2016");
        java.util.Date fechaExpIni_1516 = df.parse("18/07/2016"); 
        java.util.Date fechaExpFin_1516 = df.parse("18/07/2017");
        if (cicescinilib.equals("2012")){
            if (fechaExped.before(fechaExpIni_1213) || fechaExped.after(fechaExpFin_1213))
                throw new Exception("VIGENCIA_FORMATO*2012-2013~08/07/2013 y el 14/07/2014");
        }else if (cicescinilib.equals("2013") ){
            if (fechaExped.before(fechaExpIni_1314) || fechaExped.after(fechaExpFin_1314))
                throw new Exception("VIGENCIA_FORMATO*2013-2014~16/07/2014 y el 13/07/2015");
        }else if (cicescinilib.equals("2014") ){
            if (fechaExped.before(fechaExpIni_1415) || fechaExped.after(fechaExpFin_1415))
                throw new Exception("VIGENCIA_FORMATO*2014-2015~14/07/2015 y el 14/07/2016");
        }else if (cicescinilib.equals("2015") ){
            if (fechaExped.before(fechaExpIni_1516) || fechaExped.after(fechaExpFin_1516))
                throw new Exception("VIGENCIA_FORMATO*2015-2016~18/07/2016 y el 18/07/2017");
        }else 
            throw new Exception("CICESCINILIB_INVALIDO");
    }
    
    public String cumpleConLaEdad (String fechaCurp, String cicescini, String cveplan, String grado, String idcct, String modalidad, String cveentidad, SISCERT_QueriesInformix conexion) throws Exception
    {
        int edad;
        Map cveprog_desprog_cicesc_mod, normatividad;
        String msg="";
        
        /*if(Integer.parseInt(fechaCurp.substring(0,2))>=30 && Integer.parseInt(fechaCurp.substring(0,2))<=99)
            fechanac=fechaCurp.substring(4,6)+"/"+fechaCurp.substring(2,4)+"/"+"19"+fechaCurp.substring(0,2);
        else
            fechanac=fechaCurp.substring(4,6)+"/"+fechaCurp.substring(2,4)+"/"+"20"+fechaCurp.substring(0,2);
            
        try { edad = edad(fechanac,cicescini); }catch (ParseException ex) {  throw new Exception ("FECHA_NAC");  }*/
        edad = edad(fechaCurp,cicescini);
        
        cveprog_desprog_cicesc_mod = conexion.getCveprograma(cveplan, grado, idcct, cicescini,cveentidad);
        if (cveprog_desprog_cicesc_mod.isEmpty())
            throw new Exception ("EDAD_CVEPROGRAMA");
        normatividad=conexion.getNormatividad(cveplan, ""+cveprog_desprog_cicesc_mod.get("cveprograma"), grado);
        if (normatividad.isEmpty())
            throw new Exception ("NORMATIVIDAD_INDEF*modalidad:"+cveprog_desprog_cicesc_mod.get("modalidad")+" y cicini:"+cicescini);
        //if (!(edad >= Integer.parseInt(""+normatividad.get("edadMin"))  &&  edad <= Integer.parseInt(""+normatividad.get("edadMax"))))
        if (!edadPermitida (fechaCurp, cicescini, Integer.parseInt(""+normatividad.get("edadMin")), Integer.parseInt(""+normatividad.get("edadMax")), modalidad, Integer.parseInt(cveplan)))
            msg = "("+cveprog_desprog_cicesc_mod.get("cveprograma")+") Edad permitida: de "+normatividad.get("edadMin")+" a "+normatividad.get("edadMax")+" años cumplidos al 31 de diciembre del "+ cicescini+".\nSe ha calculado que este alumno ingresó a "+grado+"o con una edad de "+edad+" años.";
        
        return msg;
    }
    
    /*private int edad1(String fechaNacimiento,String cicescini) throws ParseException
    {
        String FechaNac,FechaAct;
        FechaNac = fechaNacimiento; //global.invFecha(fechaNacimiento, "/");
        FechaAct = "31/12/"+cicescini;
        return (int)this.getDifFechas(FechaNac, FechaAct, 0);
    }
    
    private int edad2(String fechaNacimiento,String cicescini) throws ParseException
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd"); 
        df.setLenient(false);
        Calendar calendario = Calendar.getInstance();
        
        int itemp,itemp2;
        Date FechaNac,FechaAct;
        
        //    FechaNac:=StrToDate(FechaNacimiento);
        FechaNac = df.parse(fechaNacimiento); 
        FechaAct = df.parse(cicescini+"/12/31");
        
        calendario.setTime(FechaAct);        itemp = calendario.get(Calendar.YEAR);
        calendario.setTime(FechaNac);       itemp2 = calendario.get(Calendar.YEAR);
        
        if ( FechaAct.before(FechaNac) ) 
            return itemp-itemp2-1;
        else 
            return itemp-itemp2;
    }*/
    
    public int edad (String fechaCurp, String cicescini)
    {
        int anioNac, anioAct = Integer.parseInt(cicescini);
        
        if(Integer.parseInt(fechaCurp.substring(0,2))>=30 && Integer.parseInt(fechaCurp.substring(0,2))<=99)
            anioNac=Integer.parseInt("19"+fechaCurp.substring(0,2));
        else
            anioNac=Integer.parseInt("20"+fechaCurp.substring(0,2));
        
        int itemp = anioAct;
        int iTemp2 = anioNac;

        if ( anioAct < anioNac )
            return itemp-iTemp2-1;
        else 
            return itemp-iTemp2;
    }
    
    public boolean edadPermitida (String fechaCurp, String cicescini, int edadMin, int edadMax, String modalidad, int cveplan)
    {
        boolean permitido;
        int anioNac, anioAct = Integer.parseInt(cicescini);

        if(Integer.parseInt(fechaCurp.substring(0,2))>=30 && Integer.parseInt(fechaCurp.substring(0,2))<=99)
            anioNac=Integer.parseInt("19"+fechaCurp.substring(0,2));
        else
            anioNac=Integer.parseInt("20"+fechaCurp.substring(0,2));

        int itemp = anioAct;
        int iTemp2 = anioNac;

        /*if ( anioAct < anioNac )
            caso = itemp-iTemp2-1;
        else 
            caso = itemp-iTemp2;*/

        if ( ((itemp-iTemp2)< edadMin) || ((itemp-iTemp2)> edadMax) )
            permitido=false;
        else
            permitido=true;

        if (cveplan==1){
        //solicitud para los DBA y HMC 27 de julio del 2012
         if ( (modalidad.equals("DBA") || modalidad.equals("HMC")) && ((itemp-iTemp2)>=13  && (itemp-iTemp2)<100) )               //lo k cumpla con la edad minima
            permitido=true;
        }else if (cveplan==2){
            if ( (modalidad.equals("DBA") ||  modalidad.equals("HMC")) && ((itemp-iTemp2)>=15  && (itemp-iTemp2)<100) ) {               //lo k cumpla con la edad minima
                permitido=true;
            }
        }

        if ( modalidad.equals("DML")  && (itemp-iTemp2)>= edadMin)      //lo k cumpla con la edad minima
            permitido=true;
        
        return permitido;
    }
        
    public boolean hayDatos ()
    {
       return this.datos;
    }
    
    public void setHayDatos(boolean datos)
    {
        this.datos = datos;
    }
    
        //----------- Para utilizar en el mètodo KeyTyped de un JTextBox
    /**
     * Para utilizar en el método KeyTyped de un JTextBox
     * Obtiene el texto que debería contener un JTextBox despues de llamarse el método KeyTyped
     * @param evt El evento java.awt.event.KeyEvent que brinda el método KeyTyped, 
     * @param textBox El textbox que obtuvo el evento KeyTyped
     * @return String La cadena de caracteres 
     */
    public String getTypedText (java.awt.event.KeyEvent evt, javax.swing.JTextField textBox)
    {
        String cadena;
        int pos = textBox.getCaretPosition();                                   //Obtenemos la posición del cursor
        if (evt.getKeyChar() =='\b' || evt.getKeyChar()=='\u007f' )             //back space o supr
            cadena = textBox.getText();
        else 
            cadena = textBox.getText().substring(0, pos)+ evt.getKeyChar()+textBox.getText().substring(pos); //Colocamos el caracter introducido en el lugar donde insertó el usuario
        return cadena;
    }
    
        //Un caso general del metodo revisarTextoPermitido
    public boolean formatearTextbox (java.awt.event.KeyEvent evt,String caso, javax.swing.JTextField textField, int numChars)
    {
        boolean cambios=false;
        if(revisarTextoPermitido (evt.getKeyChar(), caso))
        {
            this.limitText(evt, textField, numChars);
            cambios=true;
        }else
            evt.consume();
        return cambios;
    }
    
    //---------- Controla el límite de caracteres aceptados en un campo de texto, si la longitud se sobrepasa se sustituye
    //----------- al último caracter en la cadena por el último introducido
    public String limitText (java.awt.event.KeyEvent evt, javax.swing.JTextField textBox, int maxLength)
    {
        String cadena;
        int pos = textBox.getCaretPosition();                                   //Obtenemos la posición del cursor
        if (evt.getKeyChar() =='\b' || evt.getKeyChar()=='\u007f' )             //back space o supr
            cadena = textBox.getText();
        else 
            cadena = textBox.getText().substring(0, pos)+ evt.getKeyChar()+textBox.getText().substring(pos); //Colocamos el caracter introducido en el lugar donde insertó el usuario

        if (cadena.length()>maxLength){                                         //Si la cantidad de texto sobrepasa a la establecida
            if (cadena.length()-1 == pos)                                       //Si insertó al final, 
                cadena = cadena.substring(0, maxLength-1) + evt.getKeyChar();   //reemplazamos el último caracter que estaba por el que acaba de introducir el usuario
            else
                cadena = cadena.substring(0, maxLength);                        //Si no, simplemente recortamos
            textBox.setText(cadena);                                            //Actualizamos cambios en el textBox
            textBox.setSelectionStart(pos+1);                                   //Mostramos el cursor en la posición de inserción del nuevo caracter
            textBox.setSelectionEnd(pos+1);
            evt.consume();
        }
        return cadena;
    }
    
    //---------- Controla el límite de caracteres aceptados en un campo de texto
    public void limitarTexto(javax.swing.JTextField campo, int maxLength)
    {
        if (campo.getText().length() >= maxLength)                    //Si sobrepasa al tamaño
            campo.setText(campo.getText().substring(0, maxLength-1));  //No dejamos que la cadena aumente de caracteres
    }
    
            //--------------Devuelve false si el caso de regla de escritura no es correcto y true si lo es
    public boolean revisarTextoPermitido (char caracter, String tipo)
    {
        if (caracter =='\b' || caracter=='\u007f')                              //back space y supr
            return true;
        else if (tipo.equals("ALFABETICO")) {
            if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z') && (caracter!=' ')
                    && caracter!='Á' && caracter!='É' && caracter!='Í' && caracter!='Ó' && caracter!='Ú'
                    && caracter!='á' && caracter!='é' && caracter!='í' && caracter!='ó' && caracter!='ú'
                    && caracter!='ñ' && caracter!='Ñ'
                    && caracter!='ä' && caracter!='Ä' && caracter!='ë' && caracter!='Ë'&& caracter!='ï' && caracter!='Ï'&& caracter!='ö' && caracter!='Ö'&& caracter!='ü' && caracter!='Ü'
                    && caracter!='-' && caracter!='_' && caracter!='\'' && caracter!='.')
                return false; }        
        else if (tipo.equals("ALFANUMERICO")) {
            if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z') && (caracter!=' ')
                    && (caracter!='ñ' && caracter!='Ñ')
                    && (caracter<'0'||caracter >'9'))
                return false;
       }else if (tipo.equals("LITERALES")) {
            if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z')                    
                    && caracter!='ñ' && caracter!='Ñ' )
                return false; 
       }else if (tipo.equals("NUMERICO")) {
           if((caracter<'0'||caracter >'9'))
                return false; 
       }else if (tipo.equals("DECIMAL")) {
           if((caracter<'0'||caracter >'9') && caracter!='.')
                return false; 
       }else if (tipo.equals("PROMEDIO")) {
            if((caracter<'0'||caracter >'9') && caracter!='A'&&caracter !='a' )
                return false; 
       }else if (tipo.equals("CURP")) {
            if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z')
                    && (caracter<'0'||caracter >'9'))
                return false; 
       }else if (tipo.equals("LIBRO_FOJA")) {
           if((caracter<'0'||caracter >'9') && caracter!='*')
                return false; 
       }else if (tipo.equals("FOLIO")) {
           if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z') && caracter!='*'
                    && (caracter!='ñ' && caracter!='Ñ')
                    && (caracter<'0'||caracter >'9'))
                return false; 
       }else if (tipo.equals("FOLIO2")) {
           if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z')// && caracter!='*'
                    && (caracter!='ñ' && caracter!='Ñ')
                    && (caracter<'0'||caracter >'9'))
                return false; 
       }else if (tipo.equals("JURIDICO")) {
           if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z') && caracter!='*'&& caracter!='-'
                   && caracter!='/' && caracter!='.' && caracter!=' '
                   && (caracter<'0'||caracter >'9'))
                return false; 
       }else if (tipo.equals("NO_CONTROL")) {
            if((caracter<'0'||caracter >'9') && caracter!='-')
                return false; 
       }else if (tipo.equals("FOLIO(S)")) {
            if((caracter<'A'||caracter >'Z')&&(caracter<'a'||caracter >'z')
                    && (caracter!='ñ' && caracter!='Ñ')
                    &&(caracter<'0'||caracter >'9') && caracter!='-')
                return false; 
       }else if (tipo.equals("NUM_SOLICITUD")) {
            if((caracter<'0'||caracter >'9') && caracter!='-'&& caracter!='/')
                return false; 
       }else if (tipo.equals("FECHA_GION")) {
            if((caracter<'0'||caracter >'9') && caracter!='-')
                return false; 
       }else if (tipo.equals("FECHA_DIAGONAL")) {
            if((caracter<'0'||caracter >'9') && caracter!='/')
                return false; 
       }else return false;
       return true;
    }
    
    public String getIP_MACAddress (String caso) throws UnknownHostException, SocketException
    {
        InetAddress ip;

        ip = InetAddress.getLocalHost();
        System.out.println("Current IP address : " + ip.getHostAddress());

        NetworkInterface network = NetworkInterface.getByInetAddress(ip);

        byte[] mac = network.getHardwareAddress();

        System.out.print("Current MAC address : ");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
        }
        System.out.println(sb.toString());

        if (caso.equals("IP"))
            return ""+ip;
        else if(caso.equals("MAC_ADDRESS"))
            return sb.toString();
        
        return "";
    }
    
    public String intToFolio(String letra, Long i, int tamMax)
    {
        String ceros="000000";
        String fol=""+i;
        int tamRelleno=0;
        
        int tamNum = (""+i).length();
        if (tamNum<tamMax){
            tamRelleno = tamMax-tamNum;
            for (int j=0; j<tamRelleno; j++)
                fol = "0"+fol;
        }
        
        return letra + fol;
        
        /*if (i < 10)
            fol = letra + ceros + i;
        else if (i<100)
            fol = letra + ceros.substring(1) + i;
        else if (i<1000)
            fol = letra + ceros.substring(2) + i;
        else if (i<10000)
            fol = letra + ceros.substring(3) + i;
        else if (i<100000)
            fol = letra + ceros.substring(4) + i;
        else if (i<1000000)
            fol = letra + ceros.substring(5) + i;
        else if (i<10000000)
            fol = letra + ceros.substring(6) + i;
        else if (i<100000000)
            fol = letra + ceros.substring(7) + i;
        
        return fol;*/
    }
    
    /*Método que tiene la función de validar la curp*/
    public void validarCasocurp (String curp, String nombre, String primerApe, String segundoApe, int caso, String casoLlamada) throws Exception
    {
        SISCERT_ValidarCurp valcurp;
        switch (caso)
        {
            case 1: case 2: case 4: 
                    if (curp.length() < 16 || curp.length() == 17)
                        throw new Exception ("CURP_INCOMPLETA");
                    if (curp.length() == 18){
                        if(!curp.matches("[A-Z][A,E,I,O,U,X][A-Z]{2}[0-9]{2}[0-1][0-9][0-3][0-9][M,H][A-Z]{2}[B,C,D,F,G,H,J,K,L,M,N,Ñ,P,Q,R,S,T,V,W,X,Y,Z]{3}[0-9,A-Z][0-9]")) //"[A-Z]{1}[A,E,I,O,U]{1}[A-Z]{2}[0-9]{2}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}[H,M][A-Z]{5}[A-Z,0-9]{1}[0-9]{1}"
                            throw new Exception ("FORMATO_CURP");
                    }else if (curp.length() == 16){
                        if(!curp.matches("[A-Z][A,E,I,O,U,X][A-Z]{2}[0-9]{2}[0-1][0-9][0-3][0-9][M,H][A-Z]{2}[B,C,D,F,G,H,J,K,L,M,N,Ñ,P,Q,R,S,T,V,W,X,Y,Z]{3}")) //"[A-Z]{1}[A,E,I,O,U]{1}[A-Z]{2}[0-9]{2}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}[H,M][A-Z]{5}[A-Z,0-9]{1}[0-9]{1}"
                            throw new Exception ("FORMATO_CURP");
                    }
                    if (!this.isFecha(curp.substring(4, 10), "yyMMdd"))
                        throw new Exception ("CURP_FECHAINVALIDA");
                    if (!curp.substring(11, 13).matches("AS|BC|BS|CC|CS|CH|CL|CM|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE"))
                        throw new Exception ("CURP_ENTIDAD_INVALIDA");
                    valcurp = new SISCERT_ValidarCurp(nombre, primerApe, segundoApe, curp.substring(4, 6), curp.substring(6,8), curp.substring(8, 10), curp.substring(10, 11), curp.substring(11, 13), casoLlamada);
                    if (!curp.substring(0, 16).equals(valcurp.curp()) && casoLlamada.equals("Nuevo"))
                        throw new Exception ("CURP_INCONGRUENTE");
                break;
            case 3: 
                    if (curp.length() < 16 || curp.length() == 17)
                        throw new Exception ("CURP_INCOMPLETA");
                    if (curp.length() == 18){
                        if(!curp.matches("[A-Z][A,E,I,O,U,X][A-Z]{2}[0-9]{2}[0-1][0-9][0-3][0-9][M,H][A-Z]{2}[B,C,D,F,G,H,J,K,L,M,N,Ñ,P,Q,R,S,T,V,W,X,Y,Z]{3}[0-9,A-Z][0-9]")) //"[A-Z]{1}[A,E,I,O,U]{1}[A-Z]{2}[0-9]{2}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}[H,M][A-Z]{5}[A-Z,0-9]{1}[0-9]{1}"
                            throw new Exception ("FORMATO_CURP");
                    }else if (curp.length() == 16){
                        if(!curp.matches("[A-Z][A,E,I,O,U,X][A-Z]{2}[0-9]{2}[0-1][0-9][0-3][0-9][M,H][A-Z]{2}[B,C,D,F,G,H,J,K,L,M,N,Ñ,P,Q,R,S,T,V,W,X,Y,Z]{3}")) //"[A-Z]{1}[A,E,I,O,U]{1}[A-Z]{2}[0-9]{2}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}[H,M][A-Z]{5}[A-Z,0-9]{1}[0-9]{1}"
                            throw new Exception ("FORMATO_CURP");
                    }
                    if (!this.isFecha(curp.substring(4, 10), "yyMMdd"))
                        throw new Exception ("CURP_FECHAINVALIDA");
                    if (!curp.substring(11, 13).matches("AS|BC|BS|CC|CS|CH|CL|CM|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE"))
                        throw new Exception ("CURP_ENTIDAD_INVALIDA");
                break;
        }
    }
   
    public String validarFormatoFolio (String folio, int caso, String anioAcred)
    {
        switch (caso){
            case 2: //CICLO, ENTIDAD Y 7 NÚMEROS
                if (!folio.matches("[0-9]{10}"))
                    return "FORMATO_FOLIO";
                if (!anioAcred.equals("") && folio.charAt(0)!=anioAcred.charAt(3))
                    return "LET_FOL_Y_CICFIN";
                break;
            case 3: //LETRA Y 7 NÚMEROS
                if (!folio.matches("[A-Za-z|ñ|Ñ]{1}[0-9]{7}"))
                    return "FORMATO_FOLIO";
                break;
            case 4: //CICLO, ENTIDAD Y NÚMEROS
                if (!folio.matches("[0-9]{3}[0-9]+"))
                    return "FORMATO_FOLIO";
                if (!anioAcred.equals("") && folio.charAt(0)!=anioAcred.charAt(3))
                    return "LET_FOL_Y_CICFIN";
                break;
            case 5: //LETRA Y NÚMEROS
                if (!folio.matches("[A-Za-z|ñÑ][0-9]+"))
                    return "FORMATO_FOLIO";
                if (folio.substring(1).length()>7)
                    return "CANT_MAX_NUM_FOL";
                break;
            case 6: //SÓLO NÚMEROS
                if(!folio.matches("[0-9]+"))
                    return "FORMATO_FOLIO";
                if (folio.length()>8)
                    return "CANT_MAX_NUM_FOL";
                break;
            case 7: //SIN FOLIO
                if (!folio.equals("SIN FOLIO"))
                    return "FORMATO_FOLIO";
                break;
            case 8: //LETRA Y 8 NÚMEROS
                if (!folio.matches("[A-Za-z|ñ|Ñ]{1}[0-9]{8}"))
                    return "FORMATO_FOLIO";
                break;
            case 9: //LETRA, CICLO, ENTIDAD Y 7 NÚMEROS
                if (!folio.matches("[A-Za-z|ñ|Ñ]{1}[0-9]{10}"))
                    return "FORMATO_FOLIO";
                if (!anioAcred.equals("") && folio.charAt(1)!=anioAcred.charAt(3))  //Verificamos que el ciclo en que termina esté en el folio
                    return "LET_FOL_Y_CICFIN";
                if (!folio.substring(2,4).equals("20"))  //Verificamos que la entidad sea 20
                    return "ENTIDAD_EN_FOLIO";
                break;
        }
        return "";
    }
    
    public boolean isFecha (String fecha, String formato)
    {
        try {
            java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat(formato);
            formatoFecha.setLenient(false);
            formatoFecha.parse(fecha);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
    * Calcula la diferencia entre dos fechas. Devuelve el resultado en días, meses o años según sea el valor del parámetro 'tipo'
    * @param fechaIni Fecha inicial
    * @param fechaAct Fecha final
    * @param caso 0=TotalAños; 1=TotalMeses; 2=TotalDías; 3=MesesDelAnio; 4=DiasDelMes
    * @return numero de días, meses o años de diferencia
    * @throws java.text.ParseException
    */
    public long getDifFechas(String fechaIni, String fechaAct, int caso) throws ParseException 
    {
        int anios,mesesPorAnio,diasPorMes,diasTipoMes=0;
        long returnValue;
        
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); 
        df.setLenient(false);
        java.util.Date fechaInicio = df.parse(fechaIni); 
        java.util.Date fechaFin = df.parse(fechaAct);
        //----- Fecha inicio
        Calendar calendarInicio = Calendar.getInstance();
        calendarInicio.setTime(fechaInicio);
        int diaInicio = calendarInicio.get(Calendar.DAY_OF_MONTH);
        int mesInicio = calendarInicio.get(Calendar.MONTH) + 1; // 0 Enero, 11 Diciembre
        int anioInicio = calendarInicio.get(Calendar.YEAR);
        //----- Fecha fin
        Calendar calendarFin = Calendar.getInstance();
        calendarFin.setTime(fechaFin);
        int diaFin = calendarFin.get(Calendar.DAY_OF_MONTH);
        int mesFin = calendarFin.get(Calendar.MONTH) + 1; // 0 Enero, 11 Diciembre
        int anioFin = calendarFin.get(Calendar.YEAR);
        //
        // Calculo de días del mes
        //
        if(mesInicio==2)
            diasTipoMes = ((anioFin % 4 == 0) && ((anioFin % 100 != 0) || (anioFin % 400 == 0))) ? 29 : 28;
        else if(mesInicio <= 7)                                             // De Enero a Julio los meses pares tienen 30 y los impares 31
            diasTipoMes = (mesInicio % 2==0) ? 30 : 31;
        else if(mesInicio > 7)                                              // De Agosto a Diciembre los meses pares tienen 31 y los impares 30
            diasTipoMes = (mesInicio % 2 == 0) ? 31 : 30;
        //
        // Calculo de diferencia de año, mes y dia
        //
        if ((anioInicio > anioFin) || (anioInicio == anioFin && mesInicio > mesFin) || (anioInicio == anioFin && mesInicio == mesFin && diaInicio > diaFin)) { // La fecha de inicio es posterior a la fecha fin
                return -1;			
        } else {
            if (mesInicio <= mesFin) {
                anios = anioFin - anioInicio;
                if (diaInicio <= diaFin) {
                    mesesPorAnio = mesFin - mesInicio;
                    diasPorMes = diaFin - diaInicio;
                } else {
                    if (mesFin == mesInicio)
                        anios = anios - 1;
                    mesesPorAnio = (mesFin - mesInicio - 1 + 12) % 12;
                    diasPorMes = diasTipoMes - (diaInicio - diaFin);
                }
            } else {
                anios = anioFin - anioInicio - 1;
                if (diaInicio > diaFin) {
                    mesesPorAnio = mesFin - mesInicio - 1 + 12;
                    diasPorMes = diasTipoMes - (diaInicio - diaFin);
                } else {
                    mesesPorAnio = mesFin - mesInicio + 12;
                    diasPorMes = diaFin - diaInicio;
                }
            }
        }
        //
        // Totales
        //
        switch (caso) {
            case 0: returnValue = anios;  break;                                // Total Años
            case 1: returnValue = anios * 12 + mesesPorAnio;   break;           // Total Meses
            case 2:                                                             // Total Dias (se calcula a partir de los milisegundos por día)
                long millsecsPerDay = 86400000; // Milisegundos al día
                returnValue = (fechaFin.getTime() - fechaInicio.getTime()) / millsecsPerDay;
                break;
            case 3: returnValue = mesesPorAnio;  break;                         // Meses del año
            case 4: returnValue = diasPorMes;  break;                           // Dias del mes
            default: returnValue = -1; break;
        }

       return returnValue;
       
        /* //Otra manera más fácil, pero no estoy seguro si da el día exacto
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); 
        long anios = 0;
        long fechaInicial = df.parse(fechaIni).getTime(); //Tanto fecha inicial como fecha final son Date. 
        long fechaFinal = df.parse(fechaAct).getTime(); 
        long diferencia = fechaFinal - fechaInicial; 
        double dias = Math.floor(diferencia / (1000 * 60 * 60 * 24));
        //if (fechaFinal < fechaInicial)
        anios = (long)dias/(365+1);
        return anios;*/
    }
    
    //Invierte una fecha, si esta en formato dd/mm/aaaa la convierte a aaaa/mm/dd y biceversa
    public String invFecha (String fecha, String separador)
    {
        String nuevaFecha="";
        if (separador.equals(""))                                              //Si no se especifica un separador el default será '/'
            separador = "/";
        if (fecha.equals(""))
            return "";
        String []fechaSplit = fecha.split(separador);
        //Verificamos que haya un formato correcto
        if (fecha.length()==10 && fechaSplit.length==3)
            nuevaFecha = fechaSplit[2]+separador+fechaSplit[1]+separador+fechaSplit[0];
        return nuevaFecha;
    }
    
    /*public void setFechaToJDatChooser (com.toedter.calendar.JDateChooser dtcFecha, String fecha)
    {
        int dia, mes, anio;
        dia = Integer.parseInt(fecha.substring(0, 2));
        mes = Integer.parseInt(fecha.substring(3, 5))-1;
        anio = Integer.parseInt(fecha.substring(6, 10));

        Calendar c = Calendar.getInstance();
        c.set(anio, mes, dia);
        dtcFecha.setDate(c.getTime());
    }*/
    
    public void descargarSistema (String url)
    {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }catch (java.io.IOException e) {  System.out.println(e.getMessage());  }
    }
}
