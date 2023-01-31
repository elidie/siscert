package siscert.ClasesGlobales;

/**
 *
 * @author dai
 * 11/Oct/2012
 */

public class SISCERT_ValidarCurp {
    
    String VAPL1, VAPL11, VAPL2,VAPL21, VNOM, VNOM1;
    String VFEC_ANIO, VFEC_MES, VFEC_DIA, VSEXO, VENT, laU;
    String casoLlamada;
    int VLEN;
    
    String [] ATAB1 = {"DA ","DAS ","DE ","DEL ","DER ","DI ","DIE ",
                        "DD ","EL " ,"LA ","LOS ","LAS ","LE ","LES ",
                        "MAC ","MC ","VAN ","VON ","Y "};
    
    String [] ATAB2 = { "BACA","BAKA","BUEI","BUEY","CACA","CACO","CAGA","CAGO",
                        "CAKA","CAKO","COGE","COGI","COJA","COJE","COJI","COJO",
                        "COLA","CULO","FALO","FETO","GETA","GUEI","GUEY","JETA",
                        "JOTO","KACA","KACO","KAGA","KAGO","KAKA","KAKO","KOGE",
                        "KOGI","KOJA","KOJE","KOJI","KOJO","KOLA","KULO","LILO",
                        "LOCA","LOCO","LOKA","LOKO","MALO","MALA","MAME","MAMO",
                        "MEAR","MEAS","MEON","MIAR","MION","MOCO","MOKO","MULA",
                        "MULO","NACA","NACO","PEDA","PEDO","PENE","PIPI","PITO",
                        "POPO","PUTA","PUTO","QULO","RATA","ROBA","ROBE","ROBO",
                        "RUIN","SENO","TETA","VACA","VAGA","VAGO","VAKA","VUEI",
                        "VUEY","WUEI","WUEY"};

    public SISCERT_ValidarCurp (String nombre, String primerApe, String segundoApe, String anioNac, String mesNac, String diaNac, String sexo, String entidad, String casoLlamada)
    {
        this.VNOM = nombre.replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U");
        this.VAPL1 = primerApe.replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ó", "O").replace("Ú", "U");
        this.VAPL2 = segundoApe.replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ó", "O").replace("Ú", "U");
        this.VFEC_ANIO = anioNac;
        this.VFEC_MES = mesNac;
        this.VFEC_DIA = diaNac;
        this.VSEXO = sexo;
        this.VENT = entidad;
        this.casoLlamada = casoLlamada;
    }
    
    public String curp()
    {
        int VI;
        String VLET, VRAIZ;
        //   *** QUITA / ' .
        VAPL11 = P8(VAPL1.trim());
        VAPL21 = P8(VAPL2.trim());
        VNOM1 = P8(VNOM.trim());
        //** QUITA CARACTERES ESPECIALES
        VAPL11 = P7(VAPL11);
        VAPL21=P7(VAPL21);
        VNOM1=P7(VNOM1);
        //** QUITA MARIA Y JOSE
        VNOM1=P9(VNOM1);
        //** QUITA PROPOSICIONES
        VAPL11=P10(VAPL11);
        VAPL21=P10(VAPL21);
        VNOM1=P10(VNOM1);
        //*** QUITO PALABRAS COMPUESTAS
        VAPL11=P11(VAPL11);
        VAPL21=P11(VAPL21);
        VNOM1=P11(VNOM1);
        //*** CREA LAS PRIMERAS 4 LETRAS DE LA RAIZ
        //*** APELLIDO PATERNO
        if (VAPL11.length()==0)
            VRAIZ = "XX";
        else {
            VRAIZ = ""+VAPL11.charAt(0);
            VLET  = "X";
            for (VI=1; VI<VAPL11.length(); VI++)
            {
              if ("AEIOU".contains(""+VAPL11.charAt(VI)))
              {
                 VLET = ""+VAPL11.charAt(VI);
                 break ; //cancela el ciclo
              }
            }
            VRAIZ = VRAIZ+VLET;
        }


        //   *** APELLIDO MATERNO
        if (VAPL21.length()==0) VRAIZ += "X";
        else VRAIZ += VAPL21.charAt(0);
        //   *** NOMBRE
        if (VNOM1.length()==0) VRAIZ += "X";
        else VRAIZ += VNOM1.charAt(0);
        
        //**************** Desactivar curp para caso de nuevo (26-01-2023 indicaciones de Vicente ************/
        /*if(casoLlamada.equals("Nuevo")) {
            for(VI=0; VI<=80; VI++)
            {
                if (VRAIZ.equals(ATAB2[VI]))
                {
                    VRAIZ = VRAIZ.charAt(0)+"X"+VRAIZ.substring(VRAIZ.length()-2,VRAIZ.length());//           RIGHT(VRAIZ,2)
                    break; //EXIT
                }
            }
        }*/
        
        //   *** FECHA NACIMIENTO, SEXO Y E.F.
        VRAIZ += VFEC_ANIO+VFEC_MES+VFEC_DIA+VSEXO+VENT;
       /*         VRAIZ := VRAIZ+RIGHT(STR(VFEC_ANIO,4),2)+
                 REPL('0',2-LEN(LTRIM(STR(VFEC_MES,2))))+ LTRIM(STR(VFEC_MES,2))+
                 REPL('0',2-LEN(LTRIM(STR(VFEC_DIA,2))))+
                 LTRIM(STR(VFEC_DIA,2))+
                 VSEXO+
                 VENT
         */
        /*   IF LEVEL1 = 2 .AND. LEVEL2 = 1
                 VRAIZ = VRAIZ+RIGHT(STR(VFEC_ANIO,4),2)+REPL('0',2-LEN(LTRIM(STR(VFEC_MES,2))))+LTRIM(STR(VFEC_MES,2))+REPL('0',2-LEN(LTRIM(STR(VFEC_DIA,2))))+LTRIM(STR(VFEC_DIA,2))+VSEXO+VENT
           ELSE
              IF level1 = 1 .and. level2 = 3
                 VRAIZ = VRAIZ+RIGHT(STR(VFEC_ANIO,4),2)+REPL('0',2-LEN(LTRIM(STR(VFEC_MES,2))))+LTRIM(STR(VFEC_MES,2))+REPL('0',2-LEN(LTRIM(STR(VFEC_DIA,2))))+LTRIM(STR(VFEC_DIA,2))+VSEXO+VENT
              ELSE
                 VRAIZ = VRAIZ+RIGHT(STR(VFEC_ANIO,4),2)+REPL('0',2-LEN(LTRIM(STR(VFEC_MES,2))))+LTRIM(STR(VFEC_MES,2))+REPL('0',2-LEN(LTRIM(STR(VFEC_DIA,2))))+LTRIM(STR(VFEC_DIA,2))+VSEXO+VENT
              ENDIF
           ENDIF*/
        //   *** CONSONANTES INTERNAS
        VRAIZ = P12(VAPL11,VRAIZ);
        VRAIZ = P12(VAPL21,VRAIZ);
        VRAIZ = P12(VNOM1,VRAIZ);
        //   *** FIN DE RUTINAS
        return VRAIZ;
    }
    
    //** SUSTITUYE CARACTERES ESPECIALES POR X
    private String P7(String VPASO)
    {
        int VI;
        char VLETRA;
        
        for (VI=0; VI<VPASO.length(); VI++)
        {
            VLETRA = VPASO.charAt(VI);
            if (VLETRA =='Ä' || VLETRA =='Ë' || VLETRA =='Ï' || VLETRA=='Ö' || VLETRA=='Ü') 
            {
               if (VLETRA=='Ä') VPASO = VPASO.substring(0,VI)+'A'+VPASO.substring(VI+1);
               if (VLETRA=='Ë') VPASO = VPASO.substring(0,VI)+'E'+VPASO.substring(VI+1);
               if (VLETRA=='Ï') VPASO = VPASO.substring(0,VI)+'I'+VPASO.substring(VI+1);
               if (VLETRA=='Ö') VPASO = VPASO.substring(0,VI)+'O'+VPASO.substring(VI+1);
               if (VLETRA=='Ü') VPASO = VPASO.substring(0,VI)+'U'+VPASO.substring(VI+1);
               laU="si";
            }else
                if ((VLETRA < 65 || VLETRA > 90) && VPASO.charAt(VI)!= ' ')
                    VPASO  = VPASO.substring(0,VI)+'X'+VPASO.substring(VI+1);
        }
        return VPASO;
    }
    
    //** QUITA LAS / Y '
    private String P8(String VPASO)
    {
        int VI;
        
        //VLEN = LEN(VPASO)
        for(VI=0; VI<VPASO.length();VI++)
        {
            if (VPASO.charAt(VI)=='/' || VPASO.charAt(VI)=='\'' || VPASO.charAt(VI)=='.')
            {
                //VLEFT  = LEFT(VPASO,VI-1)
                //VRIGHT = RIGHT(VPASO,VLEN-VI)
                VPASO = VPASO.substring(0,VI)+' '+VPASO.substring(VI+1);
            }
        }
        return VPASO.trim();
    }
    
    //** QUITA JOSE Y MARIA
    private String P9(String VPASO)
    {
        if (VPASO.length()==4 && VPASO.contains("JOSE")) return VPASO;
        if (VPASO.length()==1 && VPASO.contains("J")) return VPASO;
        if (VPASO.length()==2 && VPASO.contains("J ")) return VPASO;
        if (VPASO.length()==5 && VPASO.contains("MARIA")) return VPASO;
        if (VPASO.length()==1 && VPASO.contains("M")) return VPASO;
        if (VPASO.length()==2 && VPASO.contains("M ")) return VPASO;
        if (VPASO.length()==2 && VPASO.contains("MA")) return VPASO;
        if (VPASO.length()==3 && VPASO.contains("MA ")) return VPASO;
        
        if (VPASO.length()>=5 && VPASO.substring(0,5).equals("JOSE ")) VPASO =  VPASO.substring(5);
        else if (VPASO.length()>=3 && VPASO.substring(0,3).equals("J  ")) VPASO =  VPASO.substring(3);
        else if (VPASO.length()>=2 && VPASO.substring(0,2).equals("J ")) VPASO =  VPASO.substring(2);
        else if (VPASO.length()>=6 && VPASO.substring(0,6).equals("MARIA ")) VPASO =  VPASO.substring(6);
        else if (VPASO.length()>=3 && VPASO.substring(0,3).equals("M  ")) VPASO =  VPASO.substring(3);
        else if (VPASO.length()>=2 && VPASO.substring(0,2).equals("M ")) VPASO =  VPASO.substring(2);
        else if (VPASO.length()>=4 && VPASO.substring(0,4).equals("MA  ")) VPASO =  VPASO.substring(4);
        else if (VPASO.length()>=3 && VPASO.substring(0,3).equals("MA ")) VPASO =  VPASO.substring(3);
        
        return VPASO;
    }
    
    //** QUITA PREPOSICIONES
    private String P10 (String VPASO)
    {
        int VI, tamATAB1;
        
        VI = 0;
        while (VI < 19)
        {
            tamATAB1 = ATAB1[VI].length();
            if (VPASO.length()>=tamATAB1 && VPASO.substring(0,tamATAB1).equals(ATAB1[VI]))
            {
              //MessageDlg('PARAM1= '+COPY(VPASO,1,LENGTH(ATAB1[VI]))+#13+#10+'PARAM2= '+ATAB1[VI], mtWarning, [mbOK], 0);
	      VPASO = VPASO.substring(ATAB1[VI].length());
	      VI = 0;
            }else
              VI++;
        }
        return VPASO;
    }
    
    //** QUITA PALABRAS COMPUESTAS
    private String P11(String VPASO)
    {
        int VI;
        
	for(VI=0; VI<VPASO.length(); VI++)
        {
	   if (VPASO.charAt(VI)==' ')
           {
	      VPASO = VPASO.substring(0,VI);
	      break;
           }
        }
        return VPASO;
    }
    
    //** CONSONANTES INTERNAS
    private String P12 (String VPASO, String VRAIZ)
    {
        String VLET;
        int VI;
        
        //  MessageDlg('VPASO= '+VPASO , mtWarning, [mbOK], 0);
        if(VPASO.length()==0)
           VRAIZ = VRAIZ+'X';
        else
        {
           VLET = "X";
           for (VI=1; VI<VPASO.length(); VI++)
           {
              if ("BCDFGHJKLMNPQRSTVWXYZ".contains(""+VPASO.charAt(VI)))
              {
                 VLET = ""+VPASO.charAt(VI);
                 return VRAIZ+VLET ;
              }
           }
           VRAIZ = VRAIZ+VLET ; //POR SI NO ENCUENTRA UNA CONSONANTE LE AGREGA UNA X
        }
        return VRAIZ;
    }
}
