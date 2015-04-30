package com.inspur;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.ibm.icu.text.SimpleDateFormat;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * @author chenyanpeng
 * @version 2013-9-24
 * 
 */
public class BatchScan extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private AbsoluteLayout mainLayout;
	@AutoGenerated
	private Button btnClear;
	@AutoGenerated
	private TextArea txaResult;
	@AutoGenerated
	private Button btnNext;
	@AutoGenerated
	private Button btnPrev;
	@AutoGenerated
	private ComboBox cmbBatch;
	@AutoGenerated
	private CheckBox cbxPagedShow;
	@AutoGenerated
	private Button btnGet;
	@AutoGenerated
	private TextField rowkeyInput;
	@AutoGenerated
	private ComboBox cmbTable;
	private int begin = 0;
	private int numberPerPage = 10;
	private boolean searched = false;
	private SimpleDateFormat sdf = new SimpleDateFormat("[yy/MM/dd HH:mm:ss]  ");
	/**
	 * 
	 */
	private static final long serialVersionUID = -7525779555214689084L;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public BatchScan() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		initComponents();
	}

	@SuppressWarnings("serial")
	private void initComponents() {
		List<String> tableNames = HbaseTool.getTables();
		if (tableNames != null && !tableNames.isEmpty()) {
			for (String t : tableNames) {
				cmbTable.addItem(t);
			}
			cmbBatch.setNullSelectionAllowed(false);
			cmbBatch.setTextInputAllowed(false);
			cmbBatch.select(tableNames.get(0));
		}

		cmbBatch.addItem("5");
		cmbBatch.addItem("10");
		cmbBatch.addItem("15");
		cmbBatch.addItem("20");
		cmbBatch.addItem("50");
		cmbBatch.addItem("100");
		cmbBatch.setNullSelectionAllowed(false);
		cmbBatch.setTextInputAllowed(false);
		cmbBatch.select("10");
		cmbBatch.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				numberPerPage = Integer.parseInt((String) cmbBatch.getValue());
			}
		});

		cbxPagedShow.setValue(true);
		cbxPagedShow.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				boolean checked = cbxPagedShow.getValue();
				cmbBatch.setEnabled(checked);
				btnPrev.setEnabled(checked);
				btnNext.setEnabled(checked);
				searched = false;
			}
		});

		btnNext.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String table = (String) cmbTable.getValue();
				String row = rowkeyInput.getValue();
				try {
					String ret = sdf.format(new Date())
							+ HbaseTool.batchSelectRow(table, row, begin, begin
									+ numberPerPage);
					txaResult.setValue(txaResult.getValue() + ret);
					if (!ret.endsWith("\r\n0 row(s).\r\n\r\n")) {
						begin += numberPerPage;
					}
					searched = true;
				} catch (IOException e) {
					txaResult.setValue(txaResult.getValue()
							+ sdf.format(new Date()) + "查询失败: " + e.toString()
							+ "\r\n");
				}
			}
		});

		btnPrev.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (!searched) {
					txaResult.setValue(txaResult.getValue()
							+ "请先单击\"查询\"按钮.\r\n");
					return;
				}
				String table = (String) cmbTable.getValue();
				String row = rowkeyInput.getValue();
				try {
					if (begin > 0) {
						begin -= numberPerPage;
					}
					begin = begin < 0 ? 0 : begin;
					String ret = sdf.format(new Date())
							+ HbaseTool.batchSelectRow(table, row, begin, begin
									+ numberPerPage);
					txaResult.setValue(txaResult.getValue() + ret);

				} catch (IOException e) {
					txaResult.setValue(txaResult.getValue()
							+ sdf.format(new Date()) + "查询失败: " + e.toString()
							+ "\r\n");
				}
			}
		});

		btnGet.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (cbxPagedShow.getValue()) {
					begin = 0;
					btnNext.click();
				} else {
					String table = (String) cmbTable.getValue();
					String row = rowkeyInput.getValue();
					try {
						// 查询行
						String ret = sdf.format(new Date())
								+ HbaseTool.selectRow(table.trim(), row);
						txaResult.setValue(txaResult.getValue() + ret);
						searched = true;
					} catch (IOException e) {
						txaResult.setValue(txaResult.getValue()
								+ sdf.format(new Date()) + e.toString()
								+ "\r\n");
					}
				}
			}
		});

		btnClear.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				txaResult.setValue("");
			}
		});
	}

	@AutoGenerated
	private AbsoluteLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new AbsoluteLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// cmbTable
		cmbTable = new ComboBox();
		cmbTable.setCaption("Hbase Tables");
		cmbTable.setImmediate(false);
		cmbTable.setDescription("HBase数据库中的所有的所有表");
		cmbTable.setWidth("200px");
		cmbTable.setHeight("-1px");
		mainLayout.addComponent(cmbTable, "top:20.0px;left:5.0px;");

		// rowkeyInput
		rowkeyInput = new TextField();
		rowkeyInput.setCaption("RowKey");
		rowkeyInput.setImmediate(false);
		rowkeyInput.setDescription("需要查询行的ROWKEY");
		rowkeyInput.setWidth("200px");
		rowkeyInput.setHeight("-1px");
		rowkeyInput.setTabIndex(1);
		mainLayout.addComponent(rowkeyInput, "top:60.0px;left:5.0px;");

		// btnGet
		btnGet = new Button();
		btnGet.setCaption("查询");
		btnGet.setImmediate(true);
		btnGet.setWidth("200px");
		btnGet.setHeight("-1px");
		btnGet.setTabIndex(6);
		mainLayout.addComponent(btnGet, "top:200.0px;left:5.0px;");

		// cbxPagedShow
		cbxPagedShow = new CheckBox();
		cbxPagedShow.setCaption("分页显示");
		cbxPagedShow.setImmediate(false);
		cbxPagedShow.setWidth("-1px");
		cbxPagedShow.setHeight("-1px");
		cbxPagedShow.setTabIndex(2);
		mainLayout.addComponent(cbxPagedShow, "top:91.0px;left:5.0px;");

		// cmbBatch
		cmbBatch = new ComboBox();
		cmbBatch.setCaption("每页个数");
		cmbBatch.setImmediate(false);
		cmbBatch.setWidth("200px");
		cmbBatch.setHeight("-1px");
		cmbBatch.setTabIndex(3);
		mainLayout.addComponent(cmbBatch, "top:131.0px;left:5.0px;");

		// btnPrev
		btnPrev = new Button();
		btnPrev.setCaption("上一页");
		btnPrev.setImmediate(true);
		btnPrev.setWidth("90px");
		btnPrev.setHeight("-1px");
		mainLayout.addComponent(btnPrev, "top:165.0px;left:5.0px;");

		// btnNext
		btnNext = new Button();
		btnNext.setCaption("下一页");
		btnNext.setImmediate(true);
		btnNext.setWidth("90px");
		btnNext.setHeight("-1px");
		mainLayout.addComponent(btnNext, "top:165.0px;left:115.0px;");

		// txaResult
		txaResult = new TextArea();
		txaResult.setImmediate(false);
		txaResult.setWidth("100.0%");
		txaResult.setHeight("100.0%");
		mainLayout.addComponent(txaResult,
				"top:0.0px;right:10.0px;bottom:40.0px;left:220.0px;");

		// btnClear
		btnClear = new Button();
		btnClear.setCaption("清除结果");
		btnClear.setImmediate(true);
		btnClear.setWidth("-1px");
		btnClear.setHeight("-1px");
		btnClear.setTabIndex(7);
		mainLayout.addComponent(btnClear, "right:10.0px;bottom:10.0px;");

		return mainLayout;
	}

}
