package siscert.ClasesGlobales;

/**
 *
 * @author Administrador
 */
import javax.swing.*; 
import java.awt.*; 
import java.util.ArrayList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class SISCERT_ComboboxToolTipRenderer extends BasicComboBoxRenderer 
{
    ArrayList tooltips;
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            if (-1 < index ){
                if (tooltips!=null && tooltips.size()>0)                        //Si está establecida una lista personalizada
                    list.setToolTipText(tooltips.get(index).toString());
                else if (value!=null)                                           //El valor propio del combo en esa posición
                    list.setToolTipText(""+value);
            }
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setFont(list.getFont());
        setText((value == null) ? "" : value.toString());
        return this;
    }
    
    public void setListToolTip (ArrayList listaToolTip )
    {
        this.tooltips = listaToolTip;
    }
} 
