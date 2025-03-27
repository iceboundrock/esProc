package com.scudata.cellset.graph.draw;

/**
 * 双轴堆积柱线图的实现
 * @author Joancy
 *
 */
public class Draw2YColStackedLine extends DrawBase {
	/**
	 * 实现绘图功能
	 */
	public void draw(StringBuffer htmlLink) {
		drawing(this, htmlLink);
	}

	/**
	 * 根据绘图基类db绘图，并将画图后的超链接存入htmlLink
	 * @param db 抽象的绘图基类
	 * @param htmlLink 超链接缓存
	 */
	public static void drawing(DrawBase db,StringBuffer htmlLink) {
//		锚点有重合时，谁在前面，浏览器先找到谁。由于点小，将点的锚点放在柱子前面。 xq 2017年11月13日
		StringBuffer colLink = new StringBuffer();
		int serNum = DrawColStacked.drawing(db,colLink,true);

//		双轴柱线图时，不能原点重合
		db.gp.isOverlapOrigin = false;
		Draw2Y2Line.drawY2Line(db, serNum, htmlLink);
		
		db.outPoints();
		db.outLabels();
		htmlLink.append(colLink.toString());
	}
}
