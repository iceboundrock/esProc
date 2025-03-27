package com.scudata.parallel;

import com.scudata.dm.IResource;
import com.scudata.server.unit.UnitServer;

/**
 * 代理接口
 * @author Joancy
 *
 */
public abstract class IProxy implements IResource {
	private int proxyId = UnitServer.nextId();
	
	/**
	 * 取代理编号
	 * @return 代理唯一号
	 */
	public int getProxyId() {
		return proxyId;
	}
	
	/**
	 * 设置代理编号
	 * @param proxyId
	 */
	public void setProxyId(int proxyId) {
		this.proxyId = proxyId;
	}
}