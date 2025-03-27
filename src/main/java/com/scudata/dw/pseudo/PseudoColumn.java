package com.scudata.dw.pseudo;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Sequence;

public class PseudoColumn {
	public static final String PD_NAME = "name";
	public static final String PD_EXP = "exp";
	public static final String PD_TYPE = "type";
	public static final String PD_PSEUDO = "pseudo";
	public static final String PD_ENUM = "enum";
	public static final String PD_BITS = "bits";
	public static final String PD_DIM = "dim";
	public static final String PD_FKEY = "fkey";
	public static final String PD_TIME = "tkey";
	
	private String name;//真字段的名称
	private String exp;//真字段的表达式
	private String type;//数据类型
	private String pseudo;//伪字段
	private Sequence _enum;//伪字段对应的枚举列表
	private Sequence bits;//二值维度伪字段名
	private Object dim;//指向的维表
	private String fkey[];//外键字段
	private String tkey;//时间字段
	private String[] dimKey;//维表key字段
	
	public PseudoColumn() {
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	public Sequence get_enum() {
		return _enum;
	}

	public void set_enum(Sequence _enum) {
		this._enum = _enum;
	}

	public Sequence getBits() {
		return bits;
	}

	public void setBits(Sequence bits) {
		this.bits = bits;
	}

	public Object getDim() {
		return dim;
	}

	public void setDim(Object dim) {
		this.dim = dim;
	}

	public String[] getFkey() {
		return fkey;
	}

	public void setFkey(String[] fkey) {
		this.fkey = fkey;
	}
	
	public PseudoColumn(String name, String fkey[], Object dim, String[] dimKey) {
		this.name = name;
		this.fkey = fkey;
		this.dim = dim;
		this.dimKey = dimKey;
	}
	
	public String getTkey() {
		return tkey;
	}
	
	public String getTime() {
		return tkey;
	}
	
	public void setTime(String time) {
		tkey = time;
	}
	
	public PseudoColumn(BaseRecord rec) {
		name = (String) PseudoDefination.getFieldValue(rec, PD_NAME);
		exp = (String) PseudoDefination.getFieldValue(rec, PD_EXP);
		type = (String) PseudoDefination.getFieldValue(rec, PD_TYPE);
		pseudo = (String) PseudoDefination.getFieldValue(rec, PD_PSEUDO);
		_enum = (Sequence) PseudoDefination.getFieldValue(rec, PD_ENUM);
		bits = (Sequence) PseudoDefination.getFieldValue(rec, PD_BITS);
		dim = PseudoDefination.getFieldValue(rec, PD_DIM);
		tkey = (String) PseudoDefination.getFieldValue(rec, PD_TIME);
		
		Object obj = PseudoDefination.getFieldValue(rec, PD_FKEY);
		if (obj != null) {
			Sequence seq = (Sequence) obj;
			fkey = new String[seq.length()];
			seq.toArray(fkey);
		}
		
		/**
		 * 维的pseudo是join计算的结果名称
		 */
		if (dim != null && pseudo == null) {
			if (name != null) {
				pseudo = name;
			} else {
				pseudo = fkey[0];
			}
		}
	}

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof PseudoColumn) {
        	PseudoColumn obj = (PseudoColumn)anObject;
            if (obj.getName() == null) return false;
            return obj.getName().equals(this.getName());
        }
        return false;
    }

	public String[] getDimKey() {
		return dimKey;
	}

	public void setDimKey(String[] dimKey) {
		this.dimKey = dimKey;
	}
}
