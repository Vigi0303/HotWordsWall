package com.example.vigi.hotwordswall;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * Created by Vigi on 2015/5/29.
 */
public class SuggestSearch implements Serializable {
	@JSONField(name = "name")
	private String name;

	@JSONField(name = "count")
	private int count;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
