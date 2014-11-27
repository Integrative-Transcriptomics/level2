package mayday.Reveal.visualizations.SNPSummaryPlot;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import mayday.Reveal.data.meta.StatisticalTestResult;
import mayday.Reveal.utilities.SNPSorter;
import mayday.core.settings.SettingDialog;
import mayday.core.settings.events.SettingChangeEvent;
import mayday.core.settings.events.SettingChangeListener;
import mayday.core.settings.generic.ComponentPlaceHolderSetting;
import mayday.core.settings.generic.HierarchicalSetting;
import mayday.core.settings.typed.BooleanSetting;
import mayday.core.settings.typed.ColorSetting;
import mayday.core.settings.typed.IntSetting;

/**
 * @author jaeger
 *
 */
public class SNPSummaryPlotSetting extends HierarchicalSetting {

	private SNPSummaryPlot plot;
	
	private IntSetting cellWidth;
	
	private ColorSetting selectionColor;
	
	private BooleanSetting horizontalAggregation;
	private BooleanSetting refWithChange;
	
	private ChangeSortingSetting sortingSetting;
	
	private String currentSNPOrdering = SNPSorter.NONE;
	private String oldSNPOrdering = SNPSorter.GENOMIC_LOCATION;
	private StatisticalTestResult currentSTR = null;
	
	/**
	 * @param plot
	 */
	public SNPSummaryPlotSetting(SNPSummaryPlot plot) {
		super("SNP Summary Plot Setting");
		this.plot = plot;
			
		addSetting(cellWidth = new IntSetting("Cell Width", null, 20));
		addSetting(selectionColor = new ColorSetting("Selection Color", null, Color.RED));
		addSetting(horizontalAggregation = new BooleanSetting("Stacked genotype cohort summary", null, false));
		refWithChange = new BooleanSetting("Reference nucleotide change", null, false);
		
		JButton changeSortingButton = new JButton("Change Sorting");
		changeSortingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sortingSetting = new ChangeSortingSetting(SNPSummaryPlotSetting.this.plot.getData());
				sortingSetting.setOrdering(getSNPOrder());
				SettingDialog sd = new SettingDialog(SNPSummaryPlotSetting.this.plot.getData().getProjectHandler().getGUI(), sortingSetting.getName(), sortingSetting);
				sd.setModal(true);
				sd.setVisible(true);
				if(sd.closedWithOK()) {
					currentSNPOrdering = sortingSetting.getOrdering();
					currentSTR = sortingSetting.getSelectedStatTestResult();
					if(sortingChanged()) {
						SNPSummaryPlotSetting.this.plot.getSNPSorter().sortSNPs(
								SNPSummaryPlotSetting.this.plot.getSNPs(), getSNPOrder(), 
								getSelectedStatTestResult());
						SNPSummaryPlotSetting.this.plot.updatePlot();
					}
				}
			}
		});
		
		addSetting(new ComponentPlaceHolderSetting("Sorting", changeSortingButton));
		
		this.addChangeListener(new SNPSummaryChangeListener());
	}
	
	public SNPSummaryPlotSetting clone() {
		SNPSummaryPlotSetting sss = new SNPSummaryPlotSetting(plot);
		sss.fromPrefNode(this.toPrefNode());
		return sss;
	}
	
	/**
	 * @return the cell width
	 */
	public int getCellWidth() {
		return this.cellWidth.getIntValue();
	}
	
	/**
	 * @return selection color
	 */
	public Color getSelectionColor() {
		return this.selectionColor.getColorValue();
	}
	
	/**
	 * @return snp ordering method
	 */
	public String getSNPOrder() {
		return this.currentSNPOrdering;
	}
	
	private StatisticalTestResult getSelectedStatTestResult() {
		return this.currentSTR;
	}
	
	private class SNPSummaryChangeListener implements SettingChangeListener {
		
		@Override
		public void stateChanged(SettingChangeEvent e) {
			if(e.getSource() == cellWidth) {
				plot.resizePlot();
			} else {
				plot.updatePlot();
			}
		}
	}
	
	/**
	 * @param cellWidth
	 */
	public void setCellWidth(int cellWidth) {
		this.cellWidth.setIntValue(cellWidth);
	}

	/**
	 * @return true if aggregation row should be stacked
	 */
	public boolean getHorizontalAggregation() {
		return this.horizontalAggregation.getBooleanValue();
	}
	
	private boolean sortingChanged() {
		boolean change = !oldSNPOrdering.equals(getSNPOrder());
		if(change) {
			oldSNPOrdering = getSNPOrder();
			return true;
		}
		return false;
	}

	public boolean getRefWithChange() {
		return refWithChange.getBooleanValue();
	}
}
