package com.inspur;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * HBase客户端
 * 
 * @author chenyanpeng
 * @since 2013-08-31 0.1
 * @since 2014-09-12 0.3 添加清屏和显示请求时间功能
 */
@Theme("hbaseclienttheme")
@SuppressWarnings("serial")
@Title("HBase Client")
public class HbaseclientUI extends UI {
	private Button btnGet = new Button("查 询");
	private Button btnScan = new Button("查 询");
	private Button btnPut = new Button("插 入");
	private Button btnCreate = new Button("创 建");
	private Button btnDrop = new Button("删 除");
	private Button btnDelete = new Button("删 除");
	private Button btnDeleteCol = new Button("删 除");
	private Button btnShowGet = new Button("单行记录查询");
	private Button btnShowScan = new Button("范围内查询");
	private Button btnShowPut = new Button("插入记录");
	private Button btnShowDelete = new Button("删除整行记录");
	private Button btnShowDeleteCol = new Button("删除字段");
	private Button btnShowCreate = new Button("创建表");
	private Button btnShowDrop = new Button("删除表");
	private Button btnEnterLab = new Button("HBASE LAB");
	private TextField tfNewTable = new TextField("表名(Table)");
	private TextArea taNewFamily = new TextArea("列族名(Family) (逗号<,>分隔)", "info");
	private TextField tfNewVersion = new TextField("保留版本(Version)", "1");
	private TextField tfDeadTable = new TextField("表名(Table)");
	private TextField tfGetTable = new TextField("表名(Table)");
	private TextField tfGetRow = new TextField("行(RowKey)");
	private TextField tfScanTable = new TextField("表名(Table)");
	private TextField tfScanStartRow = new TextField("起始行(RowKey)");
	private TextField tfScanStopRow = new TextField("结束行(Table)");
	private TextField tfPutTable = new TextField("表名(Table)");
	private TextField tfPutRow = new TextField("行(RowKey)");
	private TextField tfPutQualifer = new TextField("字段名(Qualifier)");
	private TextField tfPutFamily = new TextField("列族名(Family)", "info");
	private TextField tfPutValue = new TextField("值(Value)");
	private TextField tfDelTable = new TextField("表名(Table)");
	private TextField tfDelRow = new TextField("行(RowKey)");
	private TextField tfDelColTable = new TextField("表名(Table)");
	private TextField tfDelColRow = new TextField("行(RowKey)");
	private TextField tfDelColQualifer = new TextField("字段名(Qualifier)");
	private TextField tfDelColFamily = new TextField("列族名(Family)", "info");
	private Button btnClear = new Button("清屏");
	private Label lblHBaseTime = new Label("0ms");
	private Label lblWebTime = new Label("0ms");
	private TextArea taConsole = new TextArea();
	private VerticalLayout leftLayout = new VerticalLayout();
	private VerticalLayout rightLayout = new VerticalLayout();
	private VerticalLayout layGet = new VerticalLayout();
	private VerticalLayout layScan = new VerticalLayout();
	private VerticalLayout layCreate = new VerticalLayout();
	private VerticalLayout layDrop = new VerticalLayout();
	private VerticalLayout layPut = new VerticalLayout();
	private VerticalLayout layDelete = new VerticalLayout();
	private VerticalLayout layDeleteCol = new VerticalLayout();
	private SimpleDateFormat sdf = new SimpleDateFormat("[yy/MM/dd HH:mm:ss]  ");

	@Override
	protected void init(VaadinRequest request) {
		initLayout();
		initEditor();
		initButton();
		btnShowCreate.click();
		btnShowDrop.click();
		btnShowPut.click();
		btnShowGet.click();
		btnShowScan.click();
		btnShowDelete.click();
		btnShowDeleteCol.click();
	}

	private void showOptTime(long beginTime) {
		long msEnd = System.currentTimeMillis();// 完成时间
		long duration = msEnd - beginTime;// 操作时间差
		lblWebTime.setValue((duration < 1 ? "<1" : duration) + "ms");
	}

	/**
	 * 初始化按钮响应事件
	 */
	private void initButton() {
		// 查询单条记录按钮
		btnGet.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				long msBeg = System.currentTimeMillis();// 起始时间
				String table = tfGetTable.getValue();
				String row = tfGetRow.getValue();
				if (table == null || table.isEmpty() || table.trim().isEmpty()) {
					taConsole.setValue(taConsole.getValue()
							+ sdf.format(new Date()) + "表名不能为空.\r\n");
					showOptTime(msBeg);
					return;
				}
				try {
					// 查询行
					String ret = sdf.format(new Date())
							+ HbaseTool.selectRow(table.trim(), row);
					taConsole.setValue(ret + taConsole.getValue());
				} catch (IOException e) {
					taConsole.setValue(sdf.format(new Date()) + e.toString()
							+ "\r\n" + taConsole.getValue());
				}
				showOptTime(msBeg);
			}
		});
		// 范围内查询
		btnScan.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				long msBeg = System.currentTimeMillis();// 起始时间
				String table = tfScanTable.getValue();
				String startrow = tfScanStartRow.getValue();
				String stoprow = tfScanStopRow.getValue();
				if (table == null || table.isEmpty() || table.trim().isEmpty()) {
					taConsole.setValue(sdf.format(new Date()) + "表名不能为空.\r\n"
							+ taConsole.getValue());
					showOptTime(msBeg);
					return;
				}
				// 查询多行
				String ret = sdf.format(new Date())
						+ HbaseTool.scan(table, startrow, stoprow);
				taConsole.setValue(ret + taConsole.getValue());
				showOptTime(msBeg);
			}

		});
		// 插入单条记录
		btnPut.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				long msBeg = System.currentTimeMillis();// 起始时间
				String table = tfPutTable.getValue();
				String family = tfPutFamily.getValue();
				String row = tfPutRow.getValue();
				String qualifer = tfPutQualifer.getValue();
				String value = tfPutValue.getValue();
				if (table == null || table.isEmpty() || table.trim().isEmpty()) {
					taConsole.setValue(sdf.format(new Date()) + "表名不能为空.\r\n"
							+ taConsole.getValue());
					showOptTime(msBeg);
					return;
				}
				// 插入行
				String ret = sdf.format(new Date())
						+ HbaseTool.insertRow(table.trim(), row, family,
								qualifer, value);
				taConsole.setValue(ret + taConsole.getValue());
				showOptTime(msBeg);
			}
		});
		// 创建表按钮
		btnCreate.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				long msBeg = System.currentTimeMillis();// 起始时间
				String table = tfNewTable.getValue();
				String[] family = taNewFamily.getValue().split(",");
				String version = tfNewVersion.getValue();
				if (table == null || table.isEmpty() || table.trim().isEmpty()) {
					taConsole.setValue(sdf.format(new Date()) + "表名不能为空.\r\n"
							+ taConsole.getValue());
					showOptTime(msBeg);
					return;
				}
				try {
					// 创建表
					String ret = sdf.format(new Date())
							+ HbaseTool.createTable(table.trim(), family,
									Integer.parseInt(version));
					taConsole.setValue(ret + taConsole.getValue());
				} catch (NumberFormatException e) {
					taConsole.setValue(sdf.format(new Date()) + "创建表 " + table
							+ " 失败, 因为 " + e.toString() + " .\r\n"
							+ taConsole.getValue());
				} catch (IOException e) {
					taConsole.setValue(sdf.format(new Date()) + "创建表 " + table
							+ " 失败, 因为 " + e.toString() + " .\r\n"
							+ taConsole.getValue());
				}
				showOptTime(msBeg);
			}
		});
		// 删除表按钮
		btnDrop.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				long msBeg = System.currentTimeMillis();// 起始时间
				String table = tfDeadTable.getValue();
				if (table == null || table.isEmpty() || table.trim().isEmpty()) {
					taConsole.setValue(sdf.format(new Date()) + "表名不能为空.\r\n"
							+ taConsole.getValue());
					showOptTime(msBeg);
					return;
				}
				// 删除表
				String ret = sdf.format(new Date())
						+ HbaseTool.deleteTable(table.trim());
				taConsole.setValue(ret + taConsole.getValue());
				showOptTime(msBeg);
			}
		});
		// 删除行按钮
		btnDelete.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				long msBeg = System.currentTimeMillis();// 起始时间
				String table = tfDelTable.getValue();
				String row = tfDelRow.getValue();
				if (table == null || table.isEmpty() || table.trim().isEmpty()
						|| row == null || row.isEmpty() || row.trim().isEmpty()) {
					taConsole.setValue(sdf.format(new Date())
							+ "表名和行键不能为空.\r\n" + taConsole.getValue());
					showOptTime(msBeg);
					return;
				}
				// 删除行
				String ret = sdf.format(new Date())
						+ HbaseTool.deleteRow(table, row, null, null);
				taConsole.setValue(ret + taConsole.getValue());
				showOptTime(msBeg);
			}
		});
		// 删除字段按钮
		btnDeleteCol.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				long msBeg = System.currentTimeMillis();// 起始时间
				String table = tfDelColTable.getValue();
				String row = tfDelColRow.getValue();
				String family = tfDelColFamily.getValue();
				String qua = tfDelColQualifer.getValue();
				if (table == null || table.isEmpty() || table.trim().isEmpty()) {
					taConsole.setValue(sdf.format(new Date()) + "表名不能为空.\r\n"
							+ taConsole.getValue());
					showOptTime(msBeg);
					return;
				}
				if (qua == null || qua.trim().isEmpty()) {
					taConsole.setValue(sdf.format(new Date()) + "字段名不能为空.\r\n"
							+ taConsole.getValue());
					showOptTime(msBeg);
					return;
				}
				// 删除行
				String ret = sdf.format(new Date())
						+ HbaseTool.deleteRow(table, row, family, qua);
				taConsole.setValue(ret + taConsole.getValue());
				showOptTime(msBeg);
			}
		});
		// 隐藏显示创建表模块
		btnShowCreate.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				layCreate.setVisible(!layCreate.isVisible());
			}
		});
		// 隐藏显示删除表模块
		btnShowDrop.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				layDrop.setVisible(!layDrop.isVisible());
			}
		});
		// 隐藏显示查询单条记录模块
		btnShowGet.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				layGet.setVisible(!layGet.isVisible());
			}
		});
		// 隐藏显示插入单条记录模块
		btnShowPut.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				layPut.setVisible(!layPut.isVisible());
			}
		});
		// 隐藏显示范围内查询模块
		btnShowScan.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				layScan.setVisible(!layScan.isVisible());
			}
		});
		// 隐藏显示删除行模块
		btnShowDelete.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				layDelete.setVisible(!layDelete.isVisible());
			}
		});
		// 隐藏显示删除字段模块
		btnShowDeleteCol.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				layDeleteCol.setVisible(!layDeleteCol.isVisible());
			}
		});

		btnEnterLab.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Window lab = new Window("HBase实验室");
				lab.setContent(new BatchScan());
				lab.setWidth(800, Sizeable.Unit.PIXELS);
				lab.setHeight(600, Sizeable.Unit.PIXELS);
				lab.setPositionX(20);
				lab.setPositionY(20);
				addWindow(lab);
			}
		});

		btnClear.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				taConsole.setValue("");
				lblWebTime.setValue("0ms");
			}
		});
	}

	/**
	 * 初始化文本框
	 */
	private void initEditor() {
		tfNewTable.setWidth("100%");
		taNewFamily.setWidth("100%");
		taNewFamily.setRows(2);// 显示两行
		taNewFamily.setWordwrap(true);
		tfNewVersion.setWidth("100%");
		tfDeadTable.setWidth("100%");
		tfGetRow.setWidth("100%");
		tfGetTable.setWidth("100%");
		tfPutTable.setWidth("100%");
		tfPutRow.setWidth("100%");
		tfScanTable.setWidth("100%");
		tfScanStartRow.setWidth("100%");
		tfScanStopRow.setWidth("100%");
		tfPutQualifer.setWidth("100%");
		tfPutFamily.setWidth("100%");
		tfPutValue.setWidth("100%");
		tfDelColQualifer.setWidth("100%");
		tfDelColFamily.setWidth("100%");
		tfDelColTable.setWidth("100%");
		tfDelColRow.setWidth("100%");
		tfDelTable.setWidth("100%");
		tfDelRow.setWidth("100%");
		btnShowCreate.setWidth("100%");
		btnShowDrop.setWidth("100%");
		btnShowGet.setWidth("100%");
		btnShowPut.setWidth("100%");
		btnShowScan.setWidth("100%");
		btnShowDelete.setWidth("100%");
		btnShowDeleteCol.setWidth("100%");
		btnEnterLab.setWidth("100%");

		lblHBaseTime.setWidth("200px");
		lblWebTime.setWidth("200px");
		taConsole.setSizeFull();
		taConsole.setWordwrap(true);
	}

	/**
	 * 初始化布局
	 */
	@SuppressWarnings("deprecation")
	private void initLayout() {
		// 水平分两栏
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		splitPanel.setSplitPosition(300, Unit.PIXELS);// 分割的位置
		splitPanel.setMinSplitPosition(300, Unit.PIXELS);// 左栏的最小宽度
		splitPanel.setMaxSplitPosition(500, Unit.PIXELS);// 左栏的最大宽度
		setContent(splitPanel);

		splitPanel.addComponent(leftLayout);
		splitPanel.addComponent(rightLayout);

		// 创建表模块
		layCreate.addComponent(tfNewTable);
		layCreate.addComponent(taNewFamily);
		layCreate.addComponent(tfNewVersion);
		layCreate.addComponent(btnCreate);
		layCreate.setExpandRatio(tfNewTable, 2);// 设置各组件在模块内占用的空间比
		layCreate.setExpandRatio(taNewFamily, 3);
		layCreate.setExpandRatio(tfNewVersion, 2);
		layCreate.setExpandRatio(btnCreate, 1);

		// 删除表模块
		layDrop.addComponent(tfDeadTable);
		layDrop.addComponent(btnDrop);
		layDrop.setExpandRatio(tfDeadTable, 2);
		layDrop.setExpandRatio(btnDrop, 1);

		// 查询单条记录模块
		layGet.addComponent(tfGetTable);
		layGet.addComponent(tfGetRow);
		layGet.addComponent(btnGet);
		layGet.setExpandRatio(tfGetTable, 2);
		layGet.setExpandRatio(tfGetRow, 2);
		layGet.setExpandRatio(btnGet, 1);

		// 查询多条记录模块
		layScan.addComponent(tfScanTable);
		layScan.addComponent(tfScanStartRow);
		layScan.addComponent(tfScanStopRow);
		layScan.addComponent(btnScan);
		layScan.setExpandRatio(tfScanTable, 2);
		layScan.setExpandRatio(tfScanStartRow, 2);
		layScan.setExpandRatio(tfScanStopRow, 2);
		layScan.setExpandRatio(btnScan, 1);

		// 插入单条记录模块
		layPut.addComponent(tfPutTable);
		layPut.addComponent(tfPutRow);
		layPut.addComponent(tfPutFamily);
		layPut.addComponent(tfPutQualifer);
		layPut.addComponent(tfPutValue);
		layPut.addComponent(btnPut);
		layPut.setExpandRatio(tfPutTable, 2);
		layPut.setExpandRatio(tfPutRow, 2);
		layPut.setExpandRatio(tfPutFamily, 2);
		layPut.setExpandRatio(tfPutQualifer, 2);
		layPut.setExpandRatio(tfPutValue, 2);
		layPut.setExpandRatio(btnPut, 1);

		// 删除单条记录模块
		layDelete.addComponent(tfDelTable);
		layDelete.addComponent(tfDelRow);
		layDelete.addComponent(btnDelete);
		layDelete.setExpandRatio(tfDelTable, 2);
		layDelete.setExpandRatio(tfDelRow, 2);
		layDelete.setExpandRatio(btnDelete, 1);

		// 删除单条记录模块
		layDeleteCol.addComponent(tfDelColTable);
		layDeleteCol.addComponent(tfDelColFamily);
		layDeleteCol.addComponent(tfDelColRow);
		layDeleteCol.addComponent(tfDelColQualifer);
		layDeleteCol.addComponent(btnDeleteCol);
		layDeleteCol.setExpandRatio(tfDelColTable, 2);
		layDeleteCol.setExpandRatio(tfDelColFamily, 2);
		layDeleteCol.setExpandRatio(tfDelColRow, 2);
		layDeleteCol.setExpandRatio(tfDelColQualifer, 2);
		layDeleteCol.setExpandRatio(btnDeleteCol, 1);

		HorizontalLayout fitLayout = new HorizontalLayout();
		fitLayout.setWidth("95%");
		// fitLayout.addComponent(new Label("HBase读写时长:"));
		// fitLayout.addComponent(lblHBaseTime);
		// fitLayout.addComponent(new Label("|"));
		Label label = new Label("WEB访问时长:");
		fitLayout.addComponent(label);
		fitLayout.setExpandRatio(label, 1f);
		fitLayout.setComponentAlignment(label, Alignment.MIDDLE_RIGHT);
		fitLayout.addComponent(lblWebTime);
		fitLayout.setExpandRatio(lblWebTime, 3f);
		fitLayout.addComponent(btnClear);
		fitLayout.setComponentAlignment(btnClear, Alignment.MIDDLE_RIGHT);

		rightLayout.addComponent(fitLayout);
		rightLayout.setComponentAlignment(fitLayout, Alignment.TOP_CENTER);
		rightLayout.addComponent(taConsole);
		rightLayout.setExpandRatio(taConsole, 1f);

		leftLayout.addComponent(btnShowCreate);
		leftLayout.addComponent(layCreate);
		leftLayout.addComponent(btnShowDrop);
		leftLayout.addComponent(layDrop);
		leftLayout.addComponent(btnShowGet);
		leftLayout.addComponent(layGet);
		leftLayout.addComponent(btnShowScan);
		leftLayout.addComponent(layScan);
		leftLayout.addComponent(btnShowPut);
		leftLayout.addComponent(layPut);
		leftLayout.addComponent(btnShowDelete);
		leftLayout.addComponent(layDelete);
		leftLayout.addComponent(btnShowDeleteCol);
		leftLayout.addComponent(layDeleteCol);
		leftLayout.addComponent(btnEnterLab);

		// 设置一个占位组件，防止左侧模块布局混乱（有待改进）
		Label buttom = new Label(" ");
		leftLayout.addComponent(buttom);
		leftLayout.setExpandRatio(buttom, 1);

		leftLayout.setSizeFull();
		rightLayout.setSizeFull();
	}
}