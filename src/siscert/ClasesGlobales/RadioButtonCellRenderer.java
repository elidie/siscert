/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package siscert.ClasesGlobales;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Ing. Maai Nolasco Sánchez.
 * Creado el 02-abr-2013, 14:37:24
 */
public class RadioButtonCellRenderer implements TableCellRenderer 
{
    private final static Border NO_FOCUS_BORDER=BorderFactory.createEmptyBorder(1,1,1,1);  
    private JRadioButton _delegate;  
  
    public RadioButtonCellRenderer() 
    {  
        _delegate=new JRadioButton();  
    }
  
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        if (isSelected)
        {  
            _delegate.setForeground(table.getSelectionForeground());  
            _delegate.setBackground(table.getSelectionBackground());  
        }else {  
            _delegate.setForeground(table.getForeground());  
            _delegate.setBackground(table.getBackground());  
        }

        if (hasFocus)
        {
            _delegate.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));  
            if (!isSelected && table.isCellEditable(row, column))
            {
                Color col;  
                col=UIManager.getColor("Table.focusCellForeground");
                if (col != null)
                    _delegate.setForeground(col);  
                col = UIManager.getColor("Table.focusCellBackground");  
                if (col != null) 
                    _delegate.setBackground(col);  
            }
        }else
            _delegate.setBorder(NO_FOCUS_BORDER);  

        _delegate.setSelected(((Boolean)value).booleanValue());  
        return _delegate;  
    }
}