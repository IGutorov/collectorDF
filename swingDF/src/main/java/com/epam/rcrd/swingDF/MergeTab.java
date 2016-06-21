package com.epam.rcrd.swingDF;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;

import org.apache.log4j.Logger;

import static com.epam.common.igLib.LibFiles.*;
import static com.epam.common.igLib.LibFormats.*;

import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IMergerStarter;
import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;
import com.epam.rcrd.coreDF.IMergerStarter.TableDesign;
import com.epam.rcrd.coreDF.IConnectionsCore.ICallBack;

import static com.epam.rcrd.coreDF.IMergerStarter.*;
import static com.epam.rcrd.swingDF.AddComponent.*;

abstract class MergeTab implements ICallBack {

    private static final Font                     FONT_MONO_SPACED         = new Font("Lucida Console", Font.PLAIN, 11);
    private static final char                     DEFAULT_HOLD_CHAR        = '_';
    private static final String                   DEFAULT_DATE_MASK        = "##.##.####";

    private static final int                      MAX_COUNT_MERGE_INSTANCE = 100;

    private static final DefaultTableCellRenderer rightRenderer;

    static {
        rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private final JButton                         btnXls;
    private final JButton                         btnAdd                   = new JButton("+");
    private final JTabbedPane                     resultTabbedPane         = new JTabbedPane(JTabbedPane.NORTH,
                                                                                   JTabbedPane.WRAP_TAB_LAYOUT);

    protected final Logger logger;
    protected final IMergerStarter                mergerStarter;

    //  template method pattern
    protected MergeTab(final MainTabbedPane mainTabbedPane, final IConnectionsSetter connectionsSetter,
            final TypeReconciliation mainType) throws Exception {

        mergerStarter = connectionsSetter.getNewMerger(mainType);
        logger = mergerStarter.getLogger();
        
        ProgressLabel progressLabel = new ProgressLabel();
        mergerStarter.registerProgressIndicator((IProgressIndicator) progressLabel);
        mergerStarter.registerCallBack(this);
        final JButton btnStart = new JButton("Start");
        btnXls = getImageButton("excel.png", "Excel");
        btnXls.setVisible(false);
        btnAdd.setVisible(false);
        final JPanel jPanelParams = new JPanel();
        jPanelParams.setMinimumSize(new Dimension(40, 40));

        String pageName = getPageName();

        JSplitPane tabbedPaneComponent = getNewSplitter();
        
        if (mainTabbedPane.getTabCount() > 3)
            pageName += " (" + mainTabbedPane.getTabCount() + ")";
        mainTabbedPane.add(pageName, tabbedPaneComponent, logger);
        tabbedPaneComponent.setTopComponent(jPanelParams);
        tabbedPaneComponent.setBottomComponent(resultTabbedPane);

        addParamsComponentsOnPanel(jPanelParams);

        jPanelParams.add(btnStart);
        jPanelParams.add(progressLabel);
        jPanelParams.add(btnXls);
        jPanelParams.add(btnAdd);

        addSubTabbedPage("Итоги", TableDesign.SummaryTable);
        addSubTabbedPage("Расхождения", TableDesign.DataTable);
        if (mainType == TypeReconciliation.Turns) {
            addSubTabbedPage("Расхождения(вид 2)", TableDesign.DataClassicTable);
        }

        // listeners
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (mainTabbedPane.getTabCount() < MAX_COUNT_MERGE_INSTANCE) {
                    try {
                        addOneMergeTab(mainTabbedPane, connectionsSetter, mainType);
                    } catch (Exception e) {
                        logger.error("add tab failed ", e);
                    }
                    mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
                }
            }
        });

        btnXls.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mergerStarter.showXls();
            }
        });

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setParams();
                if (mergerStarter.isParamsChecked()) {
                    ((JButton) event.getSource()).setEnabled(false);
                    disableParams();
                    mergerStarter.mergeGo();
                }
            }
        });
    }

    @Override
    public void threadCompleted() {
        btnXls.setVisible(mergerStarter.isAvailableXls());
        btnAdd.setVisible(true);
    }

    private void addSubTabbedPage(final String tabbedPageName, final TableDesign tableDesign) {
        if (tableDesign == null) {
            logger.error("tableDesign == null");
            return;
        }
        AbstractTableModel tableModel = mergerStarter.getTableModel(tableDesign);
        if (tableModel == null) {
            logger.error("tableModel == null");
            return;
        }
        JTable jTable = new JTable(tableModel);
        TableRowSorter<AbstractTableModel> rowSorter = new TableRowSorter<AbstractTableModel>(tableModel);
        jTable.setRowSorter(rowSorter);
        jTable.setAutoCreateColumnsFromModel(true);
        setColumnSizes(jTable.getColumnModel(), mergerStarter.getColumnSizes(tableDesign));

        JSplitPane jSplitPane = getNewSplitter();
        jSplitPane.setTopComponent(jTable.getTableHeader());
        jSplitPane.setBottomComponent(new JScrollPane(jTable));
        resultTabbedPane.add(tabbedPageName, jSplitPane);
        cellCentre(jTable.getColumnModel());

        jTable.setCellSelectionEnabled(true);
        jTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }

    private void cellCentre(final TableColumnModel tableColumnModel) {
        Enumeration<TableColumn> tableColumns = tableColumnModel.getColumns();
        while (tableColumns.hasMoreElements()) {
            tableColumns.nextElement().setCellRenderer(rightRenderer);
        }
    }

    private void setColumnSizes(final TableColumnModel tableColumnModel, final Integer[] columnSizes) {
        if (columnSizes == null)
            return;
        int numberColumnSizes = columnSizes.length;
        int numberColumnModel = tableColumnModel.getColumnCount();
        for (int i = 0; i < numberColumnSizes && i < numberColumnModel; i++)
            if (columnSizes[i] != null)
                tableColumnModel.getColumn(i).setPreferredWidth(columnSizes[i]);
    }

    private JButton getImageButton(String imageFileName, String caption) {
        Icon icon = null;
        try {
            Image image = loadIcon(imageFileName);
            if (image != null)
                icon = new ImageIcon(image);
        } catch (IOException e) {
            logger.warn("Error load ImageButton. " + imageFileName, e);
        }
        if (icon != null)
            return new JButton(icon);
        else
            return new JButton(caption);
    }

    protected JFormattedTextField getJFormDateField(final String value) throws Exception {
        return getJFormTextField(DEFAULT_DATE_MASK, DEFAULT_HOLD_CHAR, value);
    }

    protected JFormattedTextField getJFormTextField(final String mask, final char holdChar, final String value)
            throws Exception {
        MaskFormatter maskFormatter = null;
        maskFormatter = new MaskFormatter(mask);
        maskFormatter.setPlaceholderCharacter(holdChar);
        JFormattedTextField result = new JFormattedTextField(maskFormatter); // setFormatText(mask, holdChar);
        result.setFont(FONT_MONO_SPACED);
        if (value != null)
            result.setValue(value);
        return result;
    }

    protected void setCalcDateParam(final String strDate) {
        try {
            mergerStarter.setParam("calcDate", strDate); // as is (dd.MM.yyyy)
        } catch (Exception e) {
            logger.error("setCalcDate failed", e);
        }        
    }

    protected Date getDateFormatTextField(final JFormattedTextField in) {
        Date result = null;
        String strDate = (String) in.getValue();
        if (checkDate104(strDate)) {
            try {
                result = getDate104(strDate);
            } catch (ParseException e) {
                logger.error("Incorrect date format (DD.MM.YYYY). Value = " + strDate, e);
            }
            logger.info("TDate <" + strDate + ">.");
        }
        return result;
    }

    abstract protected String getPageName();

    abstract protected void setParams();

    abstract protected void disableParams();

    abstract protected void addParamsComponentsOnPanel(JPanel jPanelParams);

}
