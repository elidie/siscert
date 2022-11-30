package siscert.ClasesGlobales;

/* 
    Creado el : 16-feb-2017, 17:46:15
    Autor     : Ing. Maai Nolasco Sánchez
*/

public class SISCERT_Excepcion extends Exception {

    static final long serialVersionUID = 0000000001;
    public static final int ERROR_MESSAGE = 0;
    public static final int INFORMATION_MESSAGE = 1;
    public static final int WARNING_MESSAGE = 2;
    public static final int QUESTION_MESSAGE = 3;
    public static final int PLAIN_MESSAGE = -1;
    
    private String mensaje="",mensaje2="",mensaje3="";
    private int numerror=-1;
    private int tipoError = SISCERT_Excepcion.PLAIN_MESSAGE;
    

    public SISCERT_Excepcion() { }

    public SISCERT_Excepcion(String mensaje) {
        this.mensaje=mensaje;
    }
    
    public SISCERT_Excepcion(int numerror,String mensaje) {
        this.numerror = numerror;
        this.mensaje=mensaje;       
    }
    
    public SISCERT_Excepcion(int numerror, int tipoError, String mensaje) {
        this.numerror = numerror;
        this.mensaje=mensaje;     
        this.tipoError = tipoError;
    }
    
    public SISCERT_Excepcion(int numerror, String mensaje1, String mensaje2) {
        this.numerror = numerror;
        this.mensaje=mensaje1;
        this.mensaje2 = mensaje2;
    }
    
    public SISCERT_Excepcion(int numerror, String mensaje1, String mensaje2, String mensaje3) {
        this.numerror = numerror;
        this.mensaje=mensaje1;
        this.mensaje2 = mensaje2;
        this.mensaje3 = mensaje3;
    }
    
    public String getMensaje(){ return mensaje; }
    public String getMensaje2(){ return mensaje2; }
    public String getMensaje3(){ return mensaje3; }
    public int getNumError(){ return numerror; }
    public int getTipoError(){ return tipoError; }
    
    public String getTipoError_toString(){
        switch(tipoError){
            case ERROR_MESSAGE: return "Error";
            case INFORMATION_MESSAGE: return "Información";
            case WARNING_MESSAGE: return "Precaución";
            case QUESTION_MESSAGE: return "Pregunta emergente";
            case PLAIN_MESSAGE: return "";
        }
        return "";
    }
}