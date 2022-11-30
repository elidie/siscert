/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package siscert.ClasesGlobales;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Ing. Maai Nolasco Sánchez.
 * Creado el 02-abr-2013, 14:38:36
 */
public class RadioButtonEditor extends AbstractCellEditor implements TableCellEditor {  
    private JRadioButton _delegate;  

    public RadioButtonEditor() 
    {
        _delegate=new JRadioButton();
        _delegate.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) {
                //fireEditingStopped();
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object o, boolean isSelected, int row, int column)
    {
        _delegate.setSelected(((Boolean)o).booleanValue());
        return _delegate;
    }
    
    public Object getCellEditorValue()
    {
        return Boolean.valueOf(_delegate.isSelected());
    }
}
