/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package siscert.ClasesGlobales;

import javax.swing.JOptionPane;

/**
 *
 * @author Ing. Maai Nolasco Sánchez
 */
public class SISCERT_Mensajes {
    public SISCERT_Mensajes ()
    {}

    
    public String inputBox (java.awt.Component parent, String titulo, String mensaje, String inputInit)
    {
        String texto = "";
        if (inputInit.equals(""))
            texto = JOptionPane.showInputDialog(parent,mensaje, titulo, JOptionPane.QUESTION_MESSAGE);
        else
            texto = JOptionPane.showInputDialog(parent,mensaje, inputInit);
        
        if (texto!=null)
            return texto;
        return texto;
        
        //Para poner un combo box
        //Object seleccion = JOptionPane.showInputDialog(unComponentePadre,"Seleccione opcion","Selector de opciones",JOptionPane.QUESTION_MESSAGE,unIcono/* null para icono defecto*/, new Object[] { "opcion 1", "opcion 2", "opcion 3" },"opcion 1");
        
        //Para poner los botones a nuestro gusto
        //int seleccion = JOptionPane.showOptionDialog(unComponentePadre,"Seleccione opcion","Selector de opciones",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,unIcono/* null para icono por defecto.*/,new Object[] { "opcion 1", "opcion 2", "opcion 3" }/* null para YES, NO y CANCEL*/,"opcion 1");
        //if (seleccion != -1) System.out.println("seleccionada opcion " + (seleccion + 1));
    }
    
    public int confirmDialog (String tipo)
    {
        if (tipo.equals("CANCELAR_FOLIOS"))
            return JOptionPane.showConfirmDialog(null, "¿Confirma que desea cancelar todos los folios mostrados?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        else if (tipo.equals("REASIGNAR_FOLIOS"))
            return JOptionPane.showConfirmDialog(null, "¿Confirma que desea reasignar todos los folios mostrados?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        else if (tipo.equals("LET_FOL_Y_CICFIN"))
            return JOptionPane.showConfirmDialog(null, "El primer número del folio no coincide con el año en que terminó este alumno\n¿Es correto este caso?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        else if (tipo.equals("ASOCIAR_IDALU"))
            return JOptionPane.showConfirmDialog(null, "El primer número del folio no coincide con el año en que terminó este alumno\n¿Es correto este caso?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        else if (tipo.equals("CONFIRMAR_NO_VALIDAR_EDAD"))
            return JOptionPane.showConfirmDialog(null, "Usted ha indicado que la edad no debe ser validada, esto quedará bajo su responsabilidad\n\n¿Desea continuar y guardar de todos modos?", "Pregunta emergente", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return -1;
    }
    
    public boolean calibrarImpresion(String tipo, String texto1, String texto2)
    {
        if (tipo.equals("NO_SELEC"))
            JOptionPane.showMessageDialog(null,"Seleccione alguna de las leyendas.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("RANGO"))
            JOptionPane.showMessageDialog(null,"La coordenada introducida está¡ fuera del área de impresión" + texto1,"Precaución",JOptionPane.WARNING_MESSAGE);        
        else if (tipo.equals ("ACTUALIZAR"))
            JOptionPane.showMessageDialog(null, "No se pudo actualizar " + texto1 + " debido a un error de conexión a la Base de Datos", "Error",JOptionPane.ERROR_MESSAGE);        
        return false;
    }
    
    public boolean ventanaPrincipal (String tipo, String texto1, String texto2)
    {
        if (tipo.equals("CVEPLAN"))
            JOptionPane.showMessageDialog(null,"Seleccione un nivel educativo.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("NO_SELEC"))
            JOptionPane.showMessageDialog(null, "Primero seleccione en la tabla "+texto1+" alumno para poder "+texto2+".", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SIN_BUSCAR_POR"))
            JOptionPane.showMessageDialog(null, "Seleccione el tipo de búsqueda (por "+texto1+", CURP o Nombre).", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SIN_RANGO_INI_FIN"))
            JOptionPane.showMessageDialog(null, "Indique un rango de selección usando F1 y F4 para inicio y fin respectivamente.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("REPORTE"))
            JOptionPane.showMessageDialog(null, "No se pudo leer el archivo de reporte "+texto1+".jasper debido a: " + texto2, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NO_TEXTO_DE_BUSQUEDA"))
            JOptionPane.showMessageDialog(null,"Ingrese un texto para realizar la búsqueda.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("NO_CONTROL_ERRONEO"))
            JOptionPane.showMessageDialog(null,"El formato para inicio y fin del No. Control es erróneo.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("IDALU_ERRONEO"))
            JOptionPane.showMessageDialog(null,"Introdusca números para el idalu.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("ACTUALIZAR"))
            JOptionPane.showMessageDialog(null, "No se pudo actualizar " + texto1 + " debido a un error de conexión a la Base de Datos", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("FORMAT_BUSQNOM"))
            JOptionPane.showMessageDialog(null, "El formato para buscar por nombre no es correcto.\nUse: PrimerApe/SegundoApe*nombre(s)#curp\n\nEjemplos:\n     Pérez/López*Juan\n     Pérez/*Juan\n     /López*Juan\n     /*Juan\n     Pérez/*\n     /López*\n     [ejemplos_anteriores]#curp", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("FORMAT_BUSQFOL"))
            JOptionPane.showMessageDialog(null, "El formato para buscar por folio no es correcto.\nUse: LetNumIni-NumFin o LetNum\n\nEjemplos:\n     A123-456\n     A9876", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("FORMAT_BUSQSOLI"))
            JOptionPane.showMessageDialog(null, "El formato para buscar por número de solicitud no es correcto.\nUse: NumSolIni-NumSolFin/CicIniLibro o NumSol/CicIniLibro\n\nEjemplos:\n     123-456/2012\n     123/2012", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CANCELAR_PARA_BORRAR"))
            JOptionPane.showMessageDialog(null, "Este alumno está asignado a un folio de impresión: "+texto1+". \n Si desea eliminarlo tiene que cancelar su folio en el apartado de 'Editar folios.'", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("FORMATO_CERTIFICADO"))
            JOptionPane.showMessageDialog(null, "Seleccione el tipo de Formato de certificado con el que desea trabajar.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FALTACURP_BUSQNOM"))
            JOptionPane.showMessageDialog(null, "Tomando en cuenta el formato de búsqueda que ha escrito, especifique la CURP.\nUse: PrimerApe/SegundoApe*nombre(s)#curp", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ESTATUS_GRADO"))
            JOptionPane.showMessageDialog(null, "El alumno tiene un ESTATUSGRADO diferente de C.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CICLO_ESTUD_Y_FOLIO"))
            JOptionPane.showMessageDialog(null, "El ciclo en que estudió el alumno no coincide con el del folio.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_NO_IMPLEMENTADO"))
            JOptionPane.showMessageDialog(null, "El formato "+texto1+" no está implementado\nen la versión actual de su sistema.\n\nPor favor actualice a la versión compatible si existe alguna.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_INEXISTENTE"))
            JOptionPane.showMessageDialog(null, "El formato "+texto1+" no existe para este nivel.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("IMPORTACION_CINCONUEVE"))
            JOptionPane.showMessageDialog(null, "La importación del alumno no se puede llevar a cabo.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NO_CICLO"))
            JOptionPane.showMessageDialog(null, "No se localizó formato digital para este libro", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("ALUMNO_CAPTURADO"))
            JOptionPane.showMessageDialog(null, "El alumno ya se encuentra registrado en el sistema con: \n"+texto1+"\nConsultelo con el encargado del Área de Duplicados.", "Precaución",JOptionPane.WARNING_MESSAGE);  
        return false;
    }
    
    public boolean loginUser (String tipo, String texto1, String texto2)
    {
        if (tipo.equals("USR_PASS_INCORRECTO"))
            JOptionPane.showMessageDialog(null,"Usuario o contraseña incorrecta.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("SIN_PERMISO"))
            JOptionPane.showMessageDialog(null,"Usuario sin derechos de usar el sistema.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CODIFICAR_PASS"))
            JOptionPane.showMessageDialog(null, "No se pudo codificar la contraseña."+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("SIN_USUARIO"))
            JOptionPane.showMessageDialog(null, "Ingrese un nombre de usuario."+texto1, "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SIN_PASS"))
            JOptionPane.showMessageDialog(null, "Ingrese una contraseña."+texto1, "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("AVISO"))
            JOptionPane.showMessageDialog(null,texto1,"Información",JOptionPane.INFORMATION_MESSAGE);
        else if (tipo.equals("SIS_OBSOLETO"))
            JOptionPane.showMessageDialog(null,"El sistema ha sido bloqueado porque la versión es obsoleta, \nfavor de actualizarlo, disponible en http://www.ieepodai.mx.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SIS_LOCK"))
            JOptionPane.showMessageDialog(null,"No tiene permiso de ingresar al sistema. Contacte al administrador de SISCERT.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SIN_TIPO_ACCESO"))
            JOptionPane.showMessageDialog(null,"Seleccione un tipo de conexión.","Precaución",JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    public boolean General (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals ("GENERAL"))
            JOptionPane.showMessageDialog(parent, ""+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CONEXION"))
            JOptionPane.showMessageDialog(parent, "No se pudo accesar a la Base de Datos debido a un error de conexión.\n"+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("DRIVER_INFORMIX"))
            JOptionPane.showMessageDialog(parent, "No se encontró el driver para informix.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CAMPO_VACIO"))
            JOptionPane.showMessageDialog(parent,"El campo " + texto1 +" no puede estar vacío.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CAMPO_VACIO_O_ENTER"))
            JOptionPane.showMessageDialog(parent,"El campo " + texto1 +" no puede estar vacío, o falta que oprima ENTER\nen dicho campo para establecer el valor.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("ACTUALIZAR"))
            JOptionPane.showMessageDialog(parent, "No se pudo actualizar " + texto1 + " debido a un error de conexión a la Base de Datos.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("ERROR_GUARDAR"))
            JOptionPane.showMessageDialog(parent, "No se pudo guardar la información.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SIN_CERTIFICADO"))
            JOptionPane.showMessageDialog(parent,"El alumno no tiene registrado un certificado impreso para este nivel.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NO_SELEC"))
            JOptionPane.showMessageDialog(parent, "Primero seleccione un"+texto1+" de la lista para poder "+texto2+".", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("H_ESCUELA"))
            JOptionPane.showMessageDialog(parent, "Debe especificar primero una CCT.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("DATO_NULO"))
            JOptionPane.showMessageDialog(parent, "Un dato que es importante para este proceso está vacío."+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("PROCESO_EXITOSO"))
            JOptionPane.showMessageDialog(parent, "Proceso completado con éxito.", "Información",JOptionPane.INFORMATION_MESSAGE);
        return false;
    }
    
    public boolean Libro (String tipo, String texto1, String texto2)
    {
        if(tipo.equals("RANGO_FOLIO"))
            JOptionPane.showMessageDialog(null,"El folio final debe ser mayor al inicial.","Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("MAX_FOLIOS"))
            JOptionPane.showMessageDialog(null,"No se pueden imprimir más de 9999 folios a la vez.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("REPORTE"))
            JOptionPane.showMessageDialog(null, "Error al leer reporte: " + texto1 + ".\n"+texto2, "Error",JOptionPane.ERROR_MESSAGE);

        return false;
    }
    
    public boolean Preescolar (String tipo, String texto1, String texto2)
    {
        if (tipo.equals("RANGO"))
            JOptionPane.showMessageDialog(null,texto1 + " debe contener "+texto2+" caracteres.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("DATO_INVÁLIDO"))
            JOptionPane.showMessageDialog(null,texto1 + " no es válido.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_INCOMPLETA"))
            JOptionPane.showMessageDialog(null,"La CURP debe contener 18 caracteres.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CICLO_ACRED"))
            JOptionPane.showMessageDialog(null,"No es posible hacer una certificación para este ciclo escolar\ndebido a que no se dieron certificados en dicho ciclo.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_CURP"))
            JOptionPane.showMessageDialog(null,"El formato de la curp es incorrecto.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_INCONGRUENTE"))
            JOptionPane.showMessageDialog(null,"La curp no contiene datos congruentes al nombre.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_FECHAINVALIDA"))
            JOptionPane.showMessageDialog(null,"La fecha de la curp no es válida.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_ENTIDAD_INVALIDA"))
            JOptionPane.showMessageDialog(null,"La entidad de la curp no es válida.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALUMAT_CVEPROGRAMA"))
            JOptionPane.showMessageDialog(null,"No se pudo obtener un CVEPROGRAMA para la escuela del alumno,\nporfavor reporte esta escuela al administrador del Sistema.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALU_EN_SICCEB"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SICEEB, por favor impórtelo.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CERTIDUP_EXISTENTE"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SISCERT"+texto1+",\npor favor edítelo desde la ventana principal.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("LET_FOL_Y_CICFIN"))
            JOptionPane.showMessageDialog(null,"El primer número del folio no coincide con el año en que terminó este alumno.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATFOL_INDEFINIDO"))
            JOptionPane.showMessageDialog(null,"Porfavor defina y/o corrija el formato en el que está escrito el folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_FOLIO"))
            JOptionPane.showMessageDialog(null,"El formato del folio que ha especificado no coincide con el folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CANT_MAX_NUM_FOL"))
            JOptionPane.showMessageDialog(null,"No puede ingresar más de 7 números de folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("AGREGAR_A_CATESCU"))
            JOptionPane.showMessageDialog(null,"Por el formato de cct de la escuela no puede tener activa la opcion \"Editar manualmente\",\nya que debería estar en el catálogo, por favor búsquela.\nSi desea otro nombre para esta CCT y no está en histórica,\npor favor repórtela al Administrador del sistema.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FOLIO_EN_FOLIOSIMPRE"))
            JOptionPane.showMessageDialog(null,"El número de folio de certificado que desea ingresar\nya está asignado a CURP(s):"+texto1+".","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CICLO_CLF>CLD"))
            JOptionPane.showMessageDialog(null,"El año en el que acredita el alumno no puede\nser mayor al libro de registro de duplicado (CLD).","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("TIENE_FOLIOS"))
            JOptionPane.showMessageDialog(null,"Al alumno ya se le asigno un o más folios: "+texto1+"\n.Comuniquese con el encargado del Area de Duplicados para más información.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("TIENE_FIRMA"))
            JOptionPane.showMessageDialog(null,"El alumno ya tiene un duplicado vigente: "+texto1+"\nSi desea un formato actualizado o reimpresión del vigente, solicitelo con el encargado del Area de Duplicados.","Precaución",JOptionPane.WARNING_MESSAGE);        
        else if (tipo.equals("PENDIENTE_CANCE"))
            JOptionPane.showMessageDialog(null,"La certificación del alumno sigue en procesos de cancelación. Consultelo con el encardo del Área de Duplicados.","Precaución",JOptionPane.WARNING_MESSAGE);            
        return false;
    }
    
    public boolean Primaria (String tipo, String texto1, String texto2)
    {
        if (tipo.equals("RANGO"))
            JOptionPane.showMessageDialog(null,texto1 + " debe contener "+texto2+" caracteres.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("PROMEDIO"))
            JOptionPane.showMessageDialog(null,"No está permitido que el 'PROMEDIO' sea menor a SEIS.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("DATO_INVÁLIDO"))
            JOptionPane.showMessageDialog(null,texto1 + " no es válido.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_INCOMPLETA"))
            JOptionPane.showMessageDialog(null,"La CURP debe contener 18 caracteres.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_CURP"))
            JOptionPane.showMessageDialog(null,"El formato de la curp es incorrecto.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_INCONGRUENTE"))
            JOptionPane.showMessageDialog(null,"La curp no contiene datos congruentes al nombre.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_FECHAINVALIDA"))
            JOptionPane.showMessageDialog(null,"La fecha de la curp no es válida.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_ENTIDAD_INVALIDA"))
            JOptionPane.showMessageDialog(null,"La entidad de la curp no es válida.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALUMAT_CVEPROGRAMA"))
            JOptionPane.showMessageDialog(null,"No se pudo obtener un CVEPROGRAMA para la escuela del alumno,\nporfavor reporte esta escuela al administrador del Sistema.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALU_EN_SICCEB"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SICEEB, por favor impórtelo.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CERTIDUP_EXISTENTE"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SISCERT"+texto1+",\npor favor edítelo desde la ventana principal.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("LET_FOL_Y_CICFIN"))
            JOptionPane.showMessageDialog(null,"El primer número del folio no coincide con el año en que terminó este alumno.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATFOL_INDEFINIDO"))
            JOptionPane.showMessageDialog(null,"Porfavor defina y/o corrija el formato en el que está escrito el folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_FOLIO"))
            JOptionPane.showMessageDialog(null,"El formato del folio que ha especificado no coincide con el folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CANT_MAX_NUM_FOL"))
            JOptionPane.showMessageDialog(null,"No puede ingresar más de 7 números de folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("AGREGAR_A_CATESCU"))
            JOptionPane.showMessageDialog(null,"Por el formato de cct de la escuela no puede tener activa la opcion \"Editar manualmente\",\nya que debería estar en el catálogo, por favor búsquela.\nSi desea otro nombre para esta CCT y no está en histórica,\npor favor repórtela al Administrador del sistema.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FOLIO_EN_FOLIOSIMPRE"))
            JOptionPane.showMessageDialog(null,"El número de folio de certificado que desea ingresar\nya está asignado a CURP(s):"+texto1+".","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CICLO_CLF>CLD"))
            JOptionPane.showMessageDialog(null,"El año en el que acredita el alumno no puede\nser mayor al libro de registro de duplicado (CLD).","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("TIENE_FOLIOS"))
            JOptionPane.showMessageDialog(null,"Al alumno ya se le asigno uno o más folios: "+texto1+"\n.Comuniquese con el encargado del Area de Duplicados para más información.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("TIENE_FIRMA"))
            JOptionPane.showMessageDialog(null,"El alumno ya tiene un duplicado vigente: "+texto1+"\nSi desea un formato actualizado o reimpresión del vigente, solicitelo con el encargado del Area de Duplicados.","Precaución",JOptionPane.WARNING_MESSAGE);        
        else if (tipo.equals("PENDIENTE_CANCE"))
            JOptionPane.showMessageDialog(null,"La certificación del alumno sigue en procesos de cancelación. Consultelo con el encardo del Área de Duplicados.","Precaución",JOptionPane.WARNING_MESSAGE);            
        return false;
    }
    
    public boolean Secundaria (String tipo, String texto1, String texto2)
    {
        if (tipo.equals("RANGO"))
            JOptionPane.showMessageDialog(null,texto1 + " debe contener "+texto2+" caracteres.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("PROMEDIO"))
            JOptionPane.showMessageDialog(null,"No está permitido que el "+texto1+" sea menor a 60.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("DATO_INVÁLIDO"))
            JOptionPane.showMessageDialog(null,texto1 + " no es válido.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_PARA_CLF"))
            JOptionPane.showMessageDialog(null,"El folio del certificado está en el libro "+texto1 + ",\nel cual no es válido para imprimirse en "+texto2+".","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_INCOMPLETA"))
            JOptionPane.showMessageDialog(null,"La CURP debe contener 18 caracteres.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_CURP"))
            JOptionPane.showMessageDialog(null,"El formato de la curp es incorrecto.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_INCONGRUENTE"))
            JOptionPane.showMessageDialog(null,"La curp no contiene datos congruentes al nombre.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_FECHAINVALIDA"))
            JOptionPane.showMessageDialog(null,"La fecha de la curp no es válida.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CURP_ENTIDAD_INVALIDA"))
            JOptionPane.showMessageDialog(null,"La entidad de la curp no es válida.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALUMAT_CVEPROGRAMA"))
            JOptionPane.showMessageDialog(null,"No se pudo obtener un CVEPROGRAMA para la escuela del alumno,\nporfavor reporte esta escuela al administrador del Sistema.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALU_EN_SICCEB"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SICEEB, por favor impórtelo.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CERTIDUP_EXISTENTE"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SISCERT"+texto1+",\npor favor edítelo desde la ventana principal.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("LET_FOL_Y_CICFIN"))
            JOptionPane.showMessageDialog(null,"El primer número del folio no coincide con el año en que terminó este alumno.","Precaución",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("FORMATFOL_INDEFINIDO"))
            JOptionPane.showMessageDialog(null,"Porfavor defina y/o corrija el formato en el que está escrito el folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_FOLIO"))
            JOptionPane.showMessageDialog(null,"El formato del folio que ha especificado no coincide con el folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CANT_MAX_NUM_FOL"))
            JOptionPane.showMessageDialog(null,"No puede ingresar más de 7 números de folio.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("AGREGAR_A_CATESCU"))
            JOptionPane.showMessageDialog(null,"Por el formato de cct de la escuela no puede tener activa la opcion \"Editar manualmente\",\nya que debería estar en el catálogo, por favor búsquela.\nSi desea otro nombre para esta CCT y no está en histórica,\npor favor repórtela al Administrador del sistema.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FOLIO_EN_FOLIOSIMPRE"))
            JOptionPane.showMessageDialog(null,"El número de folio de certificado que desea ingresar\nya está asignado a CURP(s):"+texto1+".","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("2_PROM_LET"))
            JOptionPane.showMessageDialog(null,"No pueden haber dos promedios con letra.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("VIGENCIA_FORMATO"))
            JOptionPane.showMessageDialog(null,"Si usa folios del ciclo escolar "+texto1+" para este formato,\núnicamente podrá usar fechas de expedición entre el "+texto2+" según normatividad.\nCambie su ciclo CLD o fecha de expedición según su necesidad.","Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("FECHA"))
            JOptionPane.showMessageDialog(null, "La fecha "+texto1+" no es válida.", "Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("FECHA_EXPED_Y_FECHA"))
            JOptionPane.showMessageDialog(null, "La fecha de expedición del lado anverso no coincide con la fecha del lado reverso.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("TIENE_FOLIOS"))
            JOptionPane.showMessageDialog(null,"Al alumno ya se le asigno un o más folios: "+texto1+"\n.Comuniquese con el encargado del Área de Duplicados para más información.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("TIENE_FIRMA"))
            JOptionPane.showMessageDialog(null,"El alumno ya tiene un duplicado vigente: "+texto1+"\nSi desea un formato actualizado o reimpresión del vigente, solicitelo con el encargado del Área de Duplicados.","Precaución",JOptionPane.WARNING_MESSAGE);    
        else if (tipo.equals("PENDIENTE_CANCE"))
            JOptionPane.showMessageDialog(null,"La certificación del alumno sigue en procesos de cancelación. Consultelo con el encardo del Área de Duplicados.","Precaución",JOptionPane.WARNING_MESSAGE);        
        return false;
    }
    
    public boolean ModuloCertificacion (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals("ES_REGULARIZADO"))
            JOptionPane.showMessageDialog(parent,"Indique si el alumno es de regularización o no.","Precaución",JOptionPane.WARNING_MESSAGE);
        if (tipo.equals("FECHA_ACRED_ERRONEA"))
            JOptionPane.showMessageDialog(parent,"La fecha de acreditación no es válida.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("ALU_EN_SICCEB"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SICEEB, por favor impórtelo.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALUREP_CRIPFECHANOM")){
            JOptionPane.showMessageDialog(null,"Ya existe en SICEEB un alumno con curp "+texto1.split(",")[1]+" y mismo nombre pero con idalu: "+texto2+".\nSi ha detectado que este alumno con curp "+texto1.split(",")[0]+" es el mismo que\nel de curp "+texto1.split(",")[1]+" porfavor unifíquelos desde el SICEEB.","Error",JOptionPane.ERROR_MESSAGE);
        }else if (tipo.equals("SINIDALU_CRIPFECHANOM"))
            JOptionPane.showMessageDialog(null,"Este alumno aun no está asociado con un idalu, y al tratar de crear el alumno \nse encontró otro con misma crip, fecha de nacimiento y nombre \ncon "+texto1,"Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALUREP_CURPNOM"))
            JOptionPane.showMessageDialog(null,"Ya existe en SICEEB un alumno con curp "+texto1.split(",")[1]+" y mismo nombre pero con idalu: "+texto2+".\nSi ha detectado que este alumno con curp "+texto1.split(",")[0]+" es el mismo que\nel de curp "+texto1.split(",")[1]+" porfavor unifíquelos desde el SICEEB.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("GRADOREP_ESTATUS"))
            JOptionPane.showMessageDialog(null,"No se podrá crear un certificado para este alumno (idalu="+texto1+"),\nya que se ha detectado que tiene un estatusgrado diferente de C\nen el ciclo escolar que ha especificado ("+texto2+").\nPorfavor verifique su historial académico desde el SICEEB.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NORMATIVIDAD_INDEF"))
            JOptionPane.showMessageDialog(null,"No está definido una normatividad para "+texto1,"Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("LIGAR_CURP_CON_IDALU"))
            JOptionPane.showMessageDialog(null,"Este alumno con curp "+texto1+" aun no esta asociado con su idalu correspondiente.\nPorfavor reporte este caso con el administrador del sistema.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("VIGENCIA_FORMATO"))
            JOptionPane.showMessageDialog(null,"Los folios del ciclo escolar "+texto1+" únicamente pueden tener\nfecha de expedición entre el "+texto2+" según normatividad.\nCambie su ciclo CLD o fecha de expedición según su necesidad.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CURSO_EN_OTRO_CICLO"))
            JOptionPane.showMessageDialog(null,"No se pudo crear su bloque de materias porque al parecer este alumno ya está registrado que cursó\ny acreditó en otro cicloescolar diferente al '"+texto1+"' que usted desea establecer.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NUMFOL_MANUAL_VACIO"))
            JOptionPane.showMessageDialog(null,"Especifique un número de folio o indique al sistema que asigne uno nuevo.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NUMFOL_ACTUAL_VACIO"))
            JOptionPane.showMessageDialog(null,"Actualmente no hay asignado un número de folio,especifique\nuno manualmente o indique al sistema que asigne uno nuevo.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("ENTIDAD_EN_FOLIO"))
            JOptionPane.showMessageDialog(null,"La entidad debe ser 20 en el folio del certificado.","Precaución",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NUMSOLICITUD_EXISTENTE"))
            JOptionPane.showMessageDialog(null,"El número de solicitud "+texto1+", ya está asignado\na otro registro para el mismo ciclo y formato de impresión.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NUEVO_NO_ACEPTABLE"))
            JOptionPane.showMessageDialog(parent, "Por el ciclo en el que estudia y la escuela de donde proviene este alumno,\nes indispensable que sea importado desde SICEEB.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("PASSWORD_INVALIDABLE"))
            JOptionPane.showMessageDialog(parent, "No se pudo validar la contraseña. "+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("PASSWD_INCORRECTA"))
            JOptionPane.showMessageDialog(null,"La contraseña no coincide con el del usuario que está logueado.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("SIN_PERMISO_DE_NO_VALIDAR_EDAD"))
            JOptionPane.showMessageDialog(parent, "Usted no es un usuario con permisos para indicar al sistema que no valide la edad.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("LUGAR_VALIDACIÓN_NO_ACEPTADO"))
            JOptionPane.showMessageDialog(parent, "El lugar de validación sólo se puede establecer cuando\nse expide el duplicado proveniente de otro Estado.", "Error",JOptionPane.ERROR_MESSAGE);
        
        return false;
    }
    
    public boolean Variables (String tipo, String texto1, String texto2)
    {
        
        if (tipo.equals("CAMPOS_VACIOS"))
            JOptionPane.showMessageDialog(null,"Ni un campo puede estar vacío.","Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public boolean LugarDeValidacion (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        
        if (tipo.equals("DEFAULT"))
            JOptionPane.showMessageDialog(parent,"Debe establecer una leyenda default.\nEsta será mostrada a la hora de crear un nuevo duplicado.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("REPETIDO"))
            JOptionPane.showMessageDialog(parent,"La leyenda que ha introducido ya existe.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("USADA"))
            JOptionPane.showMessageDialog(parent,"La leyenda no podrá ser "+texto1+" porque está en uso, asociada a algún certificado.\nDesasocie primero la leyenda del alumno y vuelva a intentar.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("ID_LEYENDA"))
            JOptionPane.showMessageDialog(parent,"No se pudo obtener el id para la leyenda generada.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("SIN_LEYENDA"))
            JOptionPane.showMessageDialog(parent,"No ingresó una leyenda.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("LITERAL_DESIGUAL"))
            JOptionPane.showMessageDialog(parent,"El rango de folios debe ser de la misma literial.","Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public boolean prepararImpresion(java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals ("SIN_FOLIO"))
            JOptionPane.showMessageDialog(parent, "Introdusca un folio inicial.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("SELECCION"))
            JOptionPane.showMessageDialog(parent, "Debe tener seleccionado un folio.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("FORMATO_FOLIO"))
            JOptionPane.showMessageDialog(parent, "El formato del folio "+texto1+" no es correcto.\nVerifique que tenga una letra al principio y "+texto2+" números posteriormente.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("FOLFIN_SOLONUM"))
            JOptionPane.showMessageDialog(parent, "Indique 7 o 10 números para el folio final.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("FOLFIN_MENOR"))
            JOptionPane.showMessageDialog(parent, "El folio final no puede ser menor al inicial.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("NO_IMPRIMIR_CANCELADOS"))
            JOptionPane.showMessageDialog(parent, "Los folios cancelados no pueden ser reimpresos.\nEl folio "+texto1+" con N.P.: "+texto2+" se encuentra con este estatus.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("SIN_QUE_REIMPRIMIR"))
            JOptionPane.showMessageDialog(parent, "No ha especificado folios para reimprimir.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("DIAS_CADUCOS"))
            JOptionPane.showMessageDialog(parent, "No puede reimprimir el folio "+texto1+" con N.P.: "+texto2+",\ndebido a que ha rebasado el límite de 2 días para ser reimpreso.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("SIN_NUM_SOLICITUD"))
            JOptionPane.showMessageDialog(parent, "No puede imprimir un documento si no tiene asignado un Número de Solicitud.\nVerifique el N.P.: "+texto1+".\nEdítelo si desea asignarle un Número de Solicitud.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("4INIDIG_FOLIO"))
            JOptionPane.showMessageDialog(parent, "Parce que sus primeros 4 dígitos del folio no son correctos.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("TAM_FOL_INIFIN"))
            JOptionPane.showMessageDialog(parent, "El folio inicial y final deben tener la misma longitud de caracteres (7 o 10).", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("NO_PUDO_FIRMAR"))
            JOptionPane.showMessageDialog(parent, "No se pudo foliar ni firmar, probablemente se perdió el enlace con el servidor.\n\nPor favor vuelva a intentarlo.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("MENSAJE_WEBSERVICE"))
            JOptionPane.showMessageDialog(parent, texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("ERROR_WEBSERVICE"))
            JOptionPane.showMessageDialog(parent, "Error en el servicio de oficialización.\n"+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("FORMATOIMP_NO_ACEPTADO"))
            JOptionPane.showMessageDialog(parent, "No se puede mandar a imprimir duplicados que no estén ingresados en formato electrónico.", "Error",JOptionPane.ERROR_MESSAGE);
        
        return false;
    }
    
    public boolean editarFolios (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals ("FOL_CANCEL_OK"))
            JOptionPane.showMessageDialog(parent, "Los folios se han cancelado con éxito.", "Información",JOptionPane.INFORMATION_MESSAGE);
        else if (tipo.equals ("FOL_REASIGN_OK"))
            JOptionPane.showMessageDialog(parent, "Los folios se han reasignado con éxito.", "Información",JOptionPane.INFORMATION_MESSAGE);
        else if (tipo.equals ("FOLIO_REPETIDO"))
            JOptionPane.showMessageDialog(parent, "¡La reasignación de folios se cancelará! porque se ha detectado que el folio "+texto1+"\n que desea establecer ya está en uso.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("FOLIO_A_REASIGNAR_ERRONEO"))
            JOptionPane.showMessageDialog(parent, "El formato del folio para reasignar es erroneo.\nVerifique que haya escrito una literal y 7 o 10 números.", "Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public void Impresion(String tipo, String texto1, String texto2) {
        if (tipo.equals ("FOLIO_REPETIDO"))
            JOptionPane.showMessageDialog(null, "¡La impresión se cancelará! porque se ha detectado que el folio "+texto1+"\n con el que desea imprimir ya está usado.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("FOL_REP"))
            JOptionPane.showMessageDialog(null, "¡La impresión se cancelará! porque se ha detectado que el folio "+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("SIN_LADO_ANVERSO"))
            JOptionPane.showMessageDialog(null, "No se ha impreso el lado anverso del alumno: "+texto1+" con No.Ctrol:"+texto2+".", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("FOLIO_CANCELADO"))
            JOptionPane.showMessageDialog(null, "El folio "+texto1+" ya está asignado y aparece como cancelado.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("IMPRE_CANCEL"))
            JOptionPane.showMessageDialog(null, "La impresión ha sido cancelada. "+texto1, "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("COORDENADAS"))
            JOptionPane.showMessageDialog(null, "La impresión se cancelará porque no se pudo cargar las coordenadas \nde impresión debido a un error de conexión a la Base de Datos.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("RODAC_AGOTADO"))
            JOptionPane.showMessageDialog(null, "La impresión se cancelará porque no hay folios RODAC disponibles.\nConsulte con el administrador de sistema.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("ERROR_RODAC_EXTRACT"))
            JOptionPane.showMessageDialog(null, "La impresión se cancelará porque no se pudo obtener el FOLIO RODAC.\n"+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals ("SIN_IDALU"))
            JOptionPane.showMessageDialog(null, "No se puede imprimir al alumno "+texto1+", debido a que no tiene un idalu asignado.\nVuelva a la ventana principal, edite al alumno y en la ventana respectiva permita activar el botón guardar, guarde cambios para\nque el sistema rastree y/o asigne un idalu.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("DATO_NULO"))
            JOptionPane.showMessageDialog(null, "La impresión se cancelará porque un dato que es importante para este proceso está vacío.\n"+texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("VIGENCIA_FORMATO"))
            JOptionPane.showMessageDialog(null,texto1+" el cual debe tener\nfechas de expedición entre el "+texto2+" según normatividad.\nVerifique que tenga una fecha o ciclo correcto.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CICESCINILIB_INVALIDO"))
            JOptionPane.showMessageDialog(null,"La impresión se cancelará debido a:\nEl ciclo escolar CLD que tiene asignado el alumno "+texto1+" no es válido.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("IMPRESION_CINCONUEVE"))
            JOptionPane.showMessageDialog(null,"La impresión para el alumno "+texto1+" se cancelará debido a que se detectó una inconsistencia en sus datos.","Error",JOptionPane.ERROR_MESSAGE);
    }
    
    public boolean SeekWizard(String tipo, String texto1, String texto2){
        if (tipo.equals ("SIN_DATOS"))
            JOptionPane.showMessageDialog(null, "Debe activar e introducir al menos un dato.", "Precaución",JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    public boolean AdministrarCoordenadas(java.awt.Component parent, String tipo, String texto1, String texto2){
        if (tipo.equals ("TABLA_SIN_DATOS"))
            JOptionPane.showMessageDialog(null, "No hay datos en la tabla qué "+texto1+".\nPorfavor "+texto2+" un bloque de coordenadas.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("LEYENDA_DUPLICADA"))
            JOptionPane.showMessageDialog(null, "Esta leyenda ya está ingresada.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("TABLA_SIN_DATOS"))
            JOptionPane.showMessageDialog(null, "No hay nada en la tabla que procesar.", "Precaución",JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    public boolean AgregarCveunidad (java.awt.Component parent, String tipo, String texto1, String texto2){
        if (tipo.equals ("CVEUNIDAD_CREADA"))
            JOptionPane.showMessageDialog(null, "La nueva cveunidad se ha agregado con éxito.", "Información",JOptionPane.INFORMATION_MESSAGE);
        else if (tipo.equals ("SIN_CVEPLAN"))
            JOptionPane.showMessageDialog(null, "Elija almenos un CVEPLAN.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("SIN_FORMATO"))
            JOptionPane.showMessageDialog(null, "Elija almenos un FORMATO.", "Precaución",JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    public boolean Usuarios (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals("USUARIOS"))
            JOptionPane.showMessageDialog(null, "No se pudo extraer todos los usuario del sistema. " + texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("USRSISCERT_REPETIDO"))
            JOptionPane.showMessageDialog(null, "El usuario que intenta agregar ya está registrado. " + texto1, "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SEL_USRSICEEB"))
            JOptionPane.showMessageDialog(null, "Seleccione el usuario que desea agregar.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SEL_USRSISCERTSICEEB"))
            JOptionPane.showMessageDialog(null, "Seleccione el usuario que desea eliminar.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FECHA"))
            JOptionPane.showMessageDialog(null, "La fecha no es válida." + texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("GUARDAR"))
            JOptionPane.showMessageDialog(null, "No se pudieron guardar los datos. " + texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("SIN_USR_A_BUSCAR"))
            JOptionPane.showMessageDialog(null, "Introduzca todo o parte de usuario a buscar. " + texto1, "Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("CAMPOS_VACIOS"))
            JOptionPane.showMessageDialog(null, "Todos los campos deben estar requisitados.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if(tipo.equals("OBJETO_AGREGADO"))
            JOptionPane.showMessageDialog(null, "El objeto se ha dado de alta en la BD con éxito.", "Información",JOptionPane.INFORMATION_MESSAGE);
        return false;
    }
    
    public boolean CrearCertifOrig (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals("DATO_A_BUSCAR"))
            JOptionPane.showMessageDialog(parent, "Ingrese el texto que desea buscar.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("BUSCAR_ALUMNO"))
            JOptionPane.showMessageDialog(parent, "Aún no ha buscado el alumno.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("SEL_ALUMNO"))
            JOptionPane.showMessageDialog(parent, "Debe seleccionar un alumno en la tabla.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("FORMATO_FOLIO"))
            JOptionPane.showMessageDialog(parent, "Especifique el formato del folio.", "Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals ("CERTIFICADO_CREADO"))
            JOptionPane.showMessageDialog(parent, "El certificado se ha creado con éxito.", "Información",JOptionPane.INFORMATION_MESSAGE);
        else if (tipo.equals("FOLIO_EN_FOLIOSIMPRE"))
            JOptionPane.showMessageDialog(parent,"El número de folio de certificado que desea ingresar\nya está asignado a CURP(s):"+texto1+".","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ALU_EN_SICCEB"))
            JOptionPane.showMessageDialog(null,"El alumno ya está registrado en SICEEB con el folio "+texto1,"Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CICLO_PERMITIDO"))
            JOptionPane.showMessageDialog(null,"Sólo puede dar de alta a alumnos que\nestudiaron en ciclos menores a 1991-1992.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CICLO_CLF<CEE"))
            JOptionPane.showMessageDialog(null,"El ciclo CLF no puede ser menor al CEE.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("CICLO_CLF>CLD"))
            JOptionPane.showMessageDialog(null,"El ciclo (CLF) en el que termina el alumno no puede\nser mayor al libro de registro de duplicado (CLD).","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("NO_CUMPLE_CON_EDAD"))
            JOptionPane.showMessageDialog(null,texto1+(!texto2.equals("")?"":"\n\nNOTA: Esta reestricción podría ser omitida si el alumno es importado o editado\ny el año de la CURP no es modificado."),"Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("FECHA_NAC"))
            JOptionPane.showMessageDialog(null,"No se pudo obtener la fecha de nacimiento\nporque la fecha de la curp no es correcta.","Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("EDAD_CVEPROGRAMA"))
            JOptionPane.showMessageDialog(null,"No está definido un cveprograma para la modalidad de escuela en dicho ciclo escolar.","Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("DICTAMEN_INCOMPLETO"))
            JOptionPane.showMessageDialog(null, "Si desea ingresar un dictamen, debe rellenar los dos campos\nreferentes a este concepto. En caso contrario déjelos vacíos.", "Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("FORMATO_NUM_DICTAMEN"))
            JOptionPane.showMessageDialog(null, "El formato para el número del dictamen no es correcto, use:\nDe uno a tres número, luego diagonal y despúes el año a cuatro dígitos.", "Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("FORMATO_FECHA_DICTAMEN"))
            JOptionPane.showMessageDialog(null, "El formato de la fecha de dictamen no es correcto, use: dd/MM/aaaa.", "Error",JOptionPane.ERROR_MESSAGE);
        else if(tipo.equals("FECHA_DICTAMEN_INVALIDA"))
            JOptionPane.showMessageDialog(null, "La fecha de dictámen no es una fecha válida.", "Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public boolean AsociarIdalu (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals("NO_SELEC"))
            JOptionPane.showMessageDialog(parent, "Indique un alumno "+texto1+" con el que desea hacer la asociación.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("IDALU_ASOCIADO"))
            JOptionPane.showMessageDialog(parent, "El idalu se ha asociado correctamente.", "Información",JOptionPane.INFORMATION_MESSAGE);
        else if (tipo.contains("IDALU_OCUPADO"))
            JOptionPane.showMessageDialog(parent, "El duplicado en SISCERT que ha seleccionado ya tiene asignado un idalu.", "Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public boolean AgregarEscuelaHistorica (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals("TAMAÑO_CCT"))
            JOptionPane.showMessageDialog(parent, "Debe introducir los 10 dígitos de la cct.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("CCT_INVALIDA"))
            JOptionPane.showMessageDialog(parent, "El formato de la cct introducida no es correcta.", "Error",JOptionPane.ERROR_MESSAGE);
        else if (tipo.equals("ESCUELA_HISTORICA_AGREGADA"))
            JOptionPane.showMessageDialog(parent, "El nombre histórico se ha agregado correctamente.", "Información",JOptionPane.INFORMATION_MESSAGE);
        else if (tipo.contains("NOMBRE_EXISTENTE"))
            JOptionPane.showMessageDialog(parent, "El nombre "+texto1+" ya está existe.", "Precaución",JOptionPane.WARNING_MESSAGE);
        
        return false;
    }
    
    public boolean Auditoria (java.awt.Component parent, String tipo, String texto1, String texto2)
    {
        if (tipo.equals("PERIODOINI_INVALIDO"))
            JOptionPane.showMessageDialog(parent,texto1 + " del periodo escolar no es válido. Debe introducir 4 dígitos numéricos para el año.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("RUTA_EXCEL"))
            JOptionPane.showMessageDialog(parent,"Ingrese una ruta de destino para el archivo que se generará.","Precaución",JOptionPane.WARNING_MESSAGE);
        else if (tipo.equals("ARCHIVO_GENERADO"))
            JOptionPane.showMessageDialog(parent,"El archivo se ha generado satisfactoriamente.","Información",JOptionPane.INFORMATION_MESSAGE);
        return false;
    }
}
